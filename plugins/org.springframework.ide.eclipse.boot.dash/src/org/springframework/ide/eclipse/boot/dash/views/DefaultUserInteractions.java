/*******************************************************************************
 * Copyright (c) 2015-2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.views;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Map;

import org.eclipse.compare.CompareEditorInput;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.deployment.CloudApplicationDeploymentProperties;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.deployment.DeploymentPropertiesDialog;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.deployment.MergeManifestDialog;
import org.springframework.ide.eclipse.boot.dash.dialogs.EditTemplateDialog;
import org.springframework.ide.eclipse.boot.dash.dialogs.EditTemplateDialogModel;
import org.springframework.ide.eclipse.boot.dash.dialogs.SelectRemoteEurekaDialog;
import org.springframework.ide.eclipse.boot.dash.dialogs.ToggleFiltersDialog;
import org.springframework.ide.eclipse.boot.dash.dialogs.ToggleFiltersDialogModel;
import org.springframework.ide.eclipse.boot.dash.model.BootDashViewModel;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;
import org.springframework.ide.eclipse.boot.dash.views.sections.BootDashTreeContentProvider;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.livexp.util.ExceptionUtil;
import org.springsource.ide.eclipse.commons.ui.UiUtil;

/**
 * An implementation of 'UserInteractions' that uses real Dialogs, for use in
 * 'production'.
 *
 * @author Kris De Volder
 */
public class DefaultUserInteractions implements UserInteractions {

	public interface UIContext {
		Shell getShell();
	}

	private UIContext context;

	public DefaultUserInteractions(UIContext context) {
		this.context = context;
	}

	@Override
	public ILaunchConfiguration chooseConfigurationDialog(final String dialogTitle, final String message,
			final Collection<ILaunchConfiguration> configs) {
		final LiveVariable<ILaunchConfiguration> chosen = new LiveVariable<ILaunchConfiguration>();
		context.getShell().getDisplay().syncExec(new Runnable() {
			public void run() {
				IDebugModelPresentation labelProvider = DebugUITools.newDebugModelPresentation();
				try {
					ElementListSelectionDialog dialog = new ElementListSelectionDialog(getShell(), labelProvider);
					dialog.setElements(configs.toArray());
					dialog.setTitle(dialogTitle);
					dialog.setMessage(message);
					dialog.setMultipleSelection(false);
					int result = dialog.open();
					labelProvider.dispose();
					if (result == Window.OK) {
						chosen.setValue((ILaunchConfiguration) dialog.getFirstResult());
					}
				} finally {
					labelProvider.dispose();
				}
			}
		});
		return chosen.getValue();
	}

	private Shell getShell() {
		return context.getShell();
	}

	@Override
	public IType chooseMainType(final IType[] mainTypes, final String dialogTitle, final String message) {
		if (mainTypes.length == 1) {
			return mainTypes[0];
		} else if (mainTypes.length > 0) {
			// Take care the UI interactions don't bork if called from non-ui
			// thread.
			final LiveVariable<IType> chosenType = new LiveVariable<IType>();
			getShell().getDisplay().syncExec(new Runnable() {
				public void run() {
					IDebugModelPresentation labelProvider = DebugUITools.newDebugModelPresentation();
					try {
						ElementListSelectionDialog dialog = new ElementListSelectionDialog(getShell(), labelProvider);
						dialog.setElements(mainTypes);
						dialog.setTitle(dialogTitle);
						dialog.setMessage(message);
						dialog.setMultipleSelection(false);
						int result = dialog.open();
						labelProvider.dispose();
						if (result == Window.OK) {
							chosenType.setValue((IType) dialog.getFirstResult());
						}
					} finally {
						labelProvider.dispose();
					}
				}
			});
			return chosenType.getValue();
		}
		return null;
	}

	@Override
	public void errorPopup(final String title, final String message) {
		getShell().getDisplay().asyncExec(new Runnable() {
			public void run() {
				MessageDialog.openError(getShell(), title, message);
			}
		});
	}

	@Override
	public void openLaunchConfigurationDialogOnGroup(final ILaunchConfiguration conf, final String launchGroup) {
		getShell().getDisplay().asyncExec(new Runnable() {
			public void run() {
				IStructuredSelection selection = new StructuredSelection(new Object[] { conf });
				DebugUITools.openLaunchConfigurationDialogOnGroup(getShell(), selection, launchGroup);
			}
		});
	}

	@Override
	public void openUrl(final String url) {
		getShell().getDisplay().asyncExec(new Runnable() {
			public void run() {
				if (url != null) {
					UiUtil.openUrl(url);
				}
			}
		});
	}

	@Override
	public boolean confirmOperation(final String title, final String message) {
		final boolean[] confirm = { false };
		getShell().getDisplay().syncExec(new Runnable() {
			public void run() {
				confirm[0] = MessageDialog.openConfirm(getShell(), title, message);
			}
		});
		return confirm[0];
	}

	@Override
	public String updatePassword(final String userName, final String targetId) {
		final String[] password = new String[1];
		getShell().getDisplay().syncExec(new Runnable() {
			public void run() {
				UpdatePasswordDialog dialog = new UpdatePasswordDialog(getShell(), userName, targetId);
				if (dialog.open() == Window.OK) {
					password[0] = dialog.getPassword();
				}
			}
		});
		return password[0];
	}

	@Override
	public void openDialog(final ToggleFiltersDialogModel model) {
		final Shell shell = getShell();
		shell.getDisplay().syncExec(new Runnable() {
			public void run() {
				ToggleFiltersDialog dlg = new ToggleFiltersDialog("Select Filters", model, shell);
				dlg.open();
			}
		});
	}

	@Override
	public String chooseFile(String title, String file) {
		FileDialog fileDialog = new FileDialog(getShell());
		fileDialog.setText(title);
		fileDialog.setFileName(file);

		String result = fileDialog.open();
		return result;
	}

	@Override
	public String selectRemoteEureka(BootDashViewModel model, String title, String message, String initialValue, IInputValidator validator) {
		SelectRemoteEurekaDialog dialog = new SelectRemoteEurekaDialog(getShell(), new BootDashTreeContentProvider());
		dialog.setInput(model);

	    dialog.setTitle("Select Eureka instance");
	    dialog.setMessage("Select the Eureka instance this local app should be registered with");
	    int open = dialog.open();
	    if (open == Window.OK) {
	    		String result = dialog.getSelectedEurekaURL();
	    		return result;
	    }
		return null;
	}

	@Override
	public CloudApplicationDeploymentProperties promptApplicationDeploymentProperties(final Map<String, Object> cloudData,
			final IProject project, final IFile manifest, final String defaultYaml, final boolean readOnly, final boolean noModeSwicth)
					throws OperationCanceledException {
		final Shell shell = getShell();
		final CloudApplicationDeploymentProperties[] props = new CloudApplicationDeploymentProperties[] { null };

		if (shell != null) {
			shell.getDisplay().syncExec(new Runnable() {

				@Override
				public void run() {
					DeploymentPropertiesDialog dialog = new DeploymentPropertiesDialog(shell, cloudData, project, manifest, defaultYaml, readOnly, noModeSwicth);
					if (dialog.open() == IDialogConstants.OK_ID) {
						props[0] = dialog.getCloudApplicationDeploymentProperties();
					}
				}
			});
		}
		if (props[0] == null) {
			throw new OperationCanceledException();
		} else {
			 return props[0];
		}
	}


	@Override
	public boolean yesNoWithToggle(final String propertyKey, final String title, final String message, final String toggleMessage) {
		final String ANSWER = propertyKey+".answer";
		final String TOGGLE = propertyKey+".toggle";
		final IPreferenceStore store = getPreferencesStore();
		store.setDefault(ANSWER, true);
		boolean toggleState = store.getBoolean(TOGGLE);
		boolean answer = store.getBoolean(ANSWER);
		if (toggleState) {
			return answer;
		}
		final boolean[] dialog = new boolean[2];
		getShell().getDisplay().syncExec(new Runnable() {
			@Override
			public void run() {
				MessageDialogWithToggle result = MessageDialogWithToggle.openYesNoQuestion(getShell(), title , message, toggleMessage, false, null, null);
				dialog[0] = result.getReturnCode()==IDialogConstants.YES_ID;
				dialog[1] = result.getToggleState();
			}
		});
		store.setValue(TOGGLE, dialog[1]);
		store.setValue(ANSWER, dialog[0]);
		return dialog[0];
	}

	@Override
	public boolean confirmWithToggle(final String propertyKey, final String title, final String message, final String toggleMessage) {
		final IPreferenceStore store = getPreferencesStore();
		boolean toggleState = store.getBoolean(propertyKey);
		if (toggleState) {
			return true;
		}
		final boolean[] dialog = new boolean[2];
		getShell().getDisplay().syncExec(new Runnable() {
			@Override
			public void run() {
				MessageDialogWithToggle result = MessageDialogWithToggle.openOkCancelConfirm(getShell(), title , message, toggleMessage, false, null, null);
				dialog[0] = result.getReturnCode()==IDialogConstants.OK_ID;
				dialog[1] = result.getToggleState();
			}
		});
		store.setValue(propertyKey, dialog[0] && dialog[1]);
		return dialog[0];
	}

	protected IPreferenceStore getPreferencesStore() {
		return BootDashActivator.getDefault().getPreferenceStore();
	}

	@Override
	public int openManifestCompareDialog(final CompareEditorInput input, final IRunnableContext context) throws CoreException {
		final int[] result = new int[] { -1 };
		final Exception[] exception = new Exception[] { null };
		getShell().getDisplay().syncExec(new Runnable() {
			@Override
			public void run() {
				try {
					if (context == null) {
						PlatformUI.getWorkbench().getProgressService().run(true, true, input);
					} else {
						context.run(true, true, input);
					}
					result[0] = new MergeManifestDialog(getShell(), input).open();
				} catch (InvocationTargetException | InterruptedException e) {
					exception[0] = e;
				}
			}
		});
		if (exception[0] != null) {
			throw ExceptionUtil.coreException(exception[0]);
		}
		return result[0];
	}

	@Override
	public void openEditTemplateDialog(final EditTemplateDialogModel model) {
		getShell().getDisplay().syncExec(new Runnable() {
			public void run() {
				new EditTemplateDialog(model, getShell()).open();
			}
		});
	}
}
