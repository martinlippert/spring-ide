/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.cloudfoundry.deployment;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.AnnotationModel;
import org.eclipse.jface.text.source.ISharedTextColors;
import org.eclipse.jface.text.source.OverviewRuler;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.VerticalRuler;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Sash;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ListDialog;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;
import org.eclipse.ui.handlers.IHandlerActivation;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.model.BaseWorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.AnnotationPreference;
import org.eclipse.ui.texteditor.DefaultMarkerAnnotationAccess;
import org.eclipse.ui.texteditor.DocumentProviderRegistry;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.IElementStateListener;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.ui.texteditor.MarkerAnnotationPreferences;
import org.eclipse.ui.texteditor.SourceViewerDecorationSupport;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.ApplicationManifestHandler;
import org.springframework.ide.eclipse.cloudfoundry.manifest.editor.ManifestYamlSourceViewerConfiguration;
import org.springframework.ide.eclipse.editor.support.util.ShellProviders;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.livexp.core.ValueListener;
import org.yaml.snakeyaml.composer.Composer;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.parser.ParserImpl;
import org.yaml.snakeyaml.reader.StreamReader;
import org.yaml.snakeyaml.resolver.Resolver;

/**
 * Cloud Foundry Application deployment properties dialog. Allows user to select
 * manifest YAML file or enter deployment manifest YAML manually.
 *
 * @author Alex Boyko
 *
 */
public class DeploymentPropertiesDialog extends TitleAreaDialog {

	final static private String DIALOG_LIST_HEIGHT_SETTING = "ManifestFileDialog.listHeight"; //$NON-NLS-1$
	final static private String NO_MANIFEST_SELECETED_LABEL = "Deployment manifest file not selected"; //$NON-NLS-1$
	final static private String YML_EXTENSION = "yml"; //$NON-NLS-1$
	final static private String[] FILE_FILTER_NAMES = new String[] {"Manifest YAML files - *manifest*.yml", "YAML files - *.yml", "All files - *.*"};
	final static private String SAVE_BTN_LABEL = "Save";
	final static private String DISCARD_BTN_LABEL = "Discard";
//	final static private String LATER_BTN_LABEL = "Later";


	private static abstract class DeepFileFilter extends ViewerFilter {

		@Override
		public boolean select(Viewer viewer, Object parent, Object element) {
			if (element instanceof IResource && !((IResource)element).isDerived()) {
				if (element instanceof IFile) {
					return acceptFile((IFile)element);
				}
				if (element instanceof IContainer) {
					try {
						IContainer container = (IContainer) element;
						for (IResource resource : container.members()) {
							boolean select = select(viewer, container, resource);
							if (select) {
								return true;
							}
						}
					} catch (CoreException e) {
						// ignore
					}
				}
			}
			return false;
		}

		abstract protected boolean acceptFile(IFile file);

	}

	final static private ViewerFilter YAML_FILE_FILTER = new DeepFileFilter() {
		@Override
		protected boolean acceptFile(IFile file) {
			return YML_EXTENSION.equals(file.getFileExtension());
		}
	};
	final static private ViewerFilter MANIFEST_YAML_FILE_FILTER = new DeepFileFilter() {
		@Override
		protected boolean acceptFile(IFile file) {
			return file.getName().toLowerCase().contains("manifest") && YML_EXTENSION.equals(file.getFileExtension());
		}
	};
	final static private ViewerFilter ALL_FILES = new ViewerFilter() {
		@Override
		public boolean select(Viewer viewer, Object parent, Object element) {
			return (element instanceof IResource) && !((IResource)element).isDerived();
		}
	};
	final static private ViewerFilter[][] RESOURCE_FILTERS = new ViewerFilter[][] {
		{MANIFEST_YAML_FILE_FILTER},
		{YAML_FILE_FILTER},
		{ALL_FILES}
	};

	final static private int DEFAULT_WORKSPACE_GROUP_HEIGHT = 200;

	final private String appName;

	private IProject project;
	private boolean readOnly;
	private final boolean noModeSwitch;
	private Map<String, Object> cloudData;
	private CloudApplicationDeploymentProperties deploymentProperties;
	private String defaultYaml;
	private LiveVariable<FileEditorInput> fileModel;
	private LiveVariable<Boolean> manifestTypeModel;
	private Set<FileEditorInput> mustSaveFiles;
	private Set<TextFileDocumentProvider> docProviders;

	private SourceViewerDecorationSupport fileYamlDecorationSupport;
	private SourceViewerDecorationSupport manualYamlDecorationSupport;
	private Label fileLabel;
	private Sash resizeSash;
	private SourceViewer fileYamlViewer;
	private SourceViewer manualYamlViewer;
	private TreeViewer workspaceViewer;
	private Button refreshButton;
	private Button buttonFileManifest;
	private Button buttonManualManifest;
	protected Group fileGroup;
	private Group yamlGroup;
	private Composite fileYamlComposite;
	private Composite manualYamlComposite;
	private Combo fileFilterCombo;
	private IHandlerService service;
	private List<IHandlerActivation> activations;
	private EditorActionHandler[] handlers = new EditorActionHandler[] {
			new EditorActionHandler(ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS, SourceViewer.CONTENTASSIST_PROPOSALS),
			new EditorActionHandler(IWorkbenchCommandConstants.EDIT_UNDO, SourceViewer.UNDO),
			new EditorActionHandler(IWorkbenchCommandConstants.EDIT_REDO, SourceViewer.REDO),
	};

	private ISharedTextColors colorsCache = new ISharedTextColors() {

		private Map<RGB, Color> colors = new HashMap<RGB, Color>();

		@Override
		public Color getColor(RGB rgb) {
			Color color = colors.get(rgb);
			if (color == null) {
				color = new Color(getShell().getDisplay(), rgb);
				colors.put(rgb, color);
			}
			return color;
		}

		@Override
		public void dispose() {
			for (Color color : colors.values()) {
				if (!color.isDisposed()) {
					color.dispose();
				}
			}
			colors.clear();
		}

	};

	private ISelectionChangedListener selectionListener = new ISelectionChangedListener() {
		@Override
		public void selectionChanged(final SelectionChangedEvent e) {
			FileEditorInput currentInput = fileModel.getValue();
			if (currentInput == null || !currentInput.getFile().equals(workspaceViewer.getStructuredSelection().getFirstElement())) {
				if (saveOrDiscardIfNeeded(false)) {
					IResource resource = e.getSelection().isEmpty() ? null : (IResource) ((IStructuredSelection) e.getSelection()).toArray()[0];
					fileModel.setValue(resource instanceof IFile ? new FileEditorInput((IFile) resource) : null);
					refreshButton.setEnabled(manifestTypeModel.getValue() && !workspaceViewer.getSelection().isEmpty());
				} else {
					/*
					 * Can assume that fileModel has a valid file. Cancelled save.
					 * Reject selection change, select previously selected.
					 */
					workspaceViewer.setSelection(new StructuredSelection(new IFile[] { fileModel.getValue().getFile() }));
				}
			}
		}
	};

	private IElementStateListener dirtyStateListener = new IElementStateListener() {

		@Override
		public void elementMoved(Object arg0, Object arg1) {
		}

		@Override
		public void elementDirtyStateChanged(final Object file, final boolean dirty) {
			if (fileModel.getValue() != null && fileModel.getValue().equals(file)) {
				PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						if (!fileLabel.isDisposed()) {
							fileLabel.setText(fileModel.getValue().getFile().getFullPath().toOSString() + (dirty ? "*" : ""));
						}
					}
				});
			}
		}

		@Override
		public void elementDeleted(Object arg0) {
		}

		@Override
		public void elementContentReplaced(Object file) {
			if (file.equals(fileModel.getValue())) {
				PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						updateManifestFile();
					}
				});
			}
		}

		@Override
		public void elementContentAboutToBeReplaced(Object arg0) {
		}
	};

	public DeploymentPropertiesDialog(Shell parentShell, Map<String, Object> cloudData, IProject project, IFile manifest, String defaultYaml, boolean readOnly, boolean noModeSwitch) {
		super(parentShell);
		this.cloudData = cloudData;
		this.project = project;
		this.defaultYaml = defaultYaml;
		this.readOnly = readOnly;
		this.noModeSwitch = noModeSwitch;
		this.service = (IHandlerService) PlatformUI.getWorkbench().getAdapter(IHandlerService.class);
		this.activations = new ArrayList<IHandlerActivation>(handlers.length);
		this.docProviders = new HashSet<>();
		this.mustSaveFiles = new HashSet<>();
		this.appName = ApplicationManifestHandler.getDefaultName(cloudData);
		manifestTypeModel = new LiveVariable<>();
		manifestTypeModel.setValue(manifest != null);
		fileModel = new LiveVariable<>();
		IFile foundManifest = findManifestYamlFile(project);
		fileModel.setValue(manifest == null ? (foundManifest == null ? null : new FileEditorInput(foundManifest)) : new FileEditorInput(manifest));
	}

	@Override
	protected Control createDialogArea(Composite parent) {
	    setTitle("Select Deployment Manifest");
		Composite container = (Composite) super.createDialogArea(parent);
		final Composite composite = new Composite(container, parent.getStyle());
		composite.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
		composite.setLayout(new GridLayout());

		createModeSwitchGroup(composite);

		createFileGroup(composite);

		createResizeSash(composite);

		createYamlContentsGroup(composite);

		activateHanlders();

		fileModel.addListener(new ValueListener<FileEditorInput>() {
			@Override
			public void gotValue(LiveExpression<FileEditorInput> exp, final FileEditorInput value) {
				updateManifestFile();
			}
		});

		manifestTypeModel.addListener(new ValueListener<Boolean>() {
			@Override
			public void gotValue(LiveExpression<Boolean> exp, Boolean value) {
				GridData gridData;
				buttonFileManifest.setSelection(value);
				buttonManualManifest.setSelection(!value);

				refreshButton.setEnabled(value && !workspaceViewer.getSelection().isEmpty());
				workspaceViewer.getControl().setEnabled(value);
				fileLabel.setEnabled(value);

//				if (!readOnly) {
					gridData = GridDataFactory.copyData((GridData) fileGroup.getLayoutData());
					gridData.exclude = !value;
					fileGroup.setVisible(value);
					fileGroup.setLayoutData(gridData);
					gridData = GridDataFactory.copyData((GridData) resizeSash.getLayoutData());
					gridData.exclude = !value;
					resizeSash.setVisible(value);
					resizeSash.setLayoutData(gridData);
					fileGroup.getParent().layout();
//				}

				fileYamlComposite.setVisible(value);
				gridData = GridDataFactory.copyData((GridData) fileYamlComposite.getLayoutData());
				gridData.exclude = !value;
				fileYamlComposite.setLayoutData(gridData);
				manualYamlComposite.setVisible(!value);
				gridData = GridDataFactory.copyData((GridData) manualYamlComposite.getLayoutData());
				gridData.exclude = value;
				manualYamlComposite.setLayoutData(gridData);
				yamlGroup.layout();
				yamlGroup.getParent().layout();

				validate();
			}
		});

		parent.pack(true);

		/*
		 * Reveal the selected manifest file in the workspace viewer now when
		 * controls are created and laid out
		 */
		if (!workspaceViewer.getSelection().isEmpty()) {
			workspaceViewer.setSelection(workspaceViewer.getSelection(), true);
		}
		return container;
	}

	private void createModeSwitchGroup(Composite composite) {
		Group typeGroup = new Group(composite, SWT.NONE);
		typeGroup.setVisible(!noModeSwitch);
		typeGroup.setText("Manifest Type");
		typeGroup.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).exclude(noModeSwitch).create());
		typeGroup.setLayout(new GridLayout(2, true));

		buttonFileManifest = new Button(typeGroup, SWT.RADIO);
		buttonFileManifest.setText("File");
		buttonFileManifest.setSelection(manifestTypeModel.getValue());
		buttonFileManifest.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				manifestTypeModel.setValue(buttonFileManifest.getSelection());;
			}
		});
		buttonFileManifest.setLayoutData(GridDataFactory.fillDefaults().create());

		buttonManualManifest = new Button(typeGroup, SWT.RADIO);
		buttonManualManifest.setText("Manual");
		buttonManualManifest.setSelection(!manifestTypeModel.getValue());
		buttonManualManifest.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				manifestTypeModel.setValue(!buttonManualManifest.getSelection());;
			}
		});
		buttonManualManifest.setLayoutData(GridDataFactory.fillDefaults().create());
	}

	private void createFileGroup(Composite composite) {
		fileGroup = new Group(composite, SWT.NONE);
		fileGroup.setText("Workspace File");
		fileGroup.setLayout(new GridLayout(2, false));
		int height = DEFAULT_WORKSPACE_GROUP_HEIGHT;
		try {
			height = getDialogBoundsSettings().getInt(DIALOG_LIST_HEIGHT_SETTING);
		} catch (NumberFormatException e) {
			// ignore exception
		}
		fileGroup.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).hint(SWT.DEFAULT, height).create());

		workspaceViewer = new TreeViewer(fileGroup);
		workspaceViewer.setContentProvider(new BaseWorkbenchContentProvider());
		workspaceViewer.setLabelProvider(new WorkbenchLabelProvider());
		workspaceViewer.setInput(ResourcesPlugin.getWorkspace().getRoot());
		workspaceViewer.getControl().setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
		if (fileModel.getValue() == null) {
			workspaceViewer.reveal(project.getFile("manifest.yml"));
		} else {
			workspaceViewer.reveal(fileModel.getValue());
			workspaceViewer.setSelection(new StructuredSelection(new IResource[] { fileModel.getValue().getFile() }), false);
		}
		workspaceViewer.addSelectionChangedListener(selectionListener);

		Composite fileButtonsComposite = new Composite(fileGroup, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
		fileButtonsComposite.setLayout(layout);
		fileButtonsComposite.setLayoutData(GridDataFactory.fillDefaults().create());

		refreshButton = new Button(fileButtonsComposite, SWT.PUSH);
		refreshButton.setImage(BootDashActivator.getDefault().getImageRegistry().get(BootDashActivator.REFRESH_ICON));
		refreshButton.setText("Refresh");
		refreshButton.setEnabled(false);
		refreshButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				refreshManifests();
			}
		});
		refreshButton.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());

		fileFilterCombo = new Combo(fileGroup, SWT.DROP_DOWN | SWT.BORDER | SWT.READ_ONLY);
		fileFilterCombo.setItems(FILE_FILTER_NAMES);
		int selectionIndex = 0;
		FileEditorInput manifestFile = fileModel.getValue();
		if (manifestFile != null) {
			selectionIndex = RESOURCE_FILTERS.length - 1;
			for (int i = 0; i < RESOURCE_FILTERS.length; i++) {
				boolean accept = true;
				for (ViewerFilter filter : RESOURCE_FILTERS[i]) {
					accept = filter.select(null, null, fileModel.getValue().getFile());
					if (!accept) {
						break;
					}
				}
				if (accept) {
					selectionIndex = i;
					break;
				}
			}
		}
		workspaceViewer.setFilters(RESOURCE_FILTERS[selectionIndex]);
		fileFilterCombo.select(selectionIndex);
		fileFilterCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// Remove selection listener to set selection from current pathModel value
				workspaceViewer.removeSelectionChangedListener(selectionListener);
				workspaceViewer.setFilters(RESOURCE_FILTERS[fileFilterCombo.getSelectionIndex()]);
				// Add the selection listener back after the initial value has been set
				workspaceViewer.addSelectionChangedListener(selectionListener);
			}
		});
//		fileFilterCombo.setLayoutData(GridDataFactory.fillDefaults().grab(false, false).minSize(0, 0).create());
	}

	private void createResizeSash(Composite composite) {
		resizeSash = new Sash(composite, SWT.HORIZONTAL);
		resizeSash.setLayoutData(GridDataFactory.fillDefaults().hint(SWT.DEFAULT, 4).grab(true, false).create());
		resizeSash.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				GridData listLayoutData = (GridData) fileGroup.getLayoutData();
				int newHeight = listLayoutData.heightHint + e.y - resizeSash.getBounds().y;
				if (newHeight < listLayoutData.minimumHeight) {
					newHeight = listLayoutData.minimumHeight;
					e.doit = false;
				}
				listLayoutData.heightHint = newHeight;
				fileGroup.setLayoutData(listLayoutData);
				fileGroup.getParent().layout();
			}
		});
	}

	@SuppressWarnings("unchecked")
	private void createYamlContentsGroup(Composite composite) {
		yamlGroup = new Group(composite, SWT.NONE);
		yamlGroup.setText("YAML Content");
		yamlGroup.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
		yamlGroup.setLayout(new GridLayout());

		fileYamlComposite = new Composite(yamlGroup, SWT.NONE);
		fileYamlComposite.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
		GridLayout layout = new GridLayout(3, false);
		layout.marginWidth = 0;
		fileYamlComposite.setLayout(layout);

		fileLabel = new Label(fileYamlComposite, SWT.WRAP);
		fileLabel.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).align(SWT.FILL, SWT.CENTER).span(3, SWT.DEFAULT).create());

		DefaultMarkerAnnotationAccess fileMarkerAnnotationAccess = new DefaultMarkerAnnotationAccess();
		OverviewRuler fileOverviewRuler = new OverviewRuler(fileMarkerAnnotationAccess, 12, colorsCache);
		VerticalRuler fileVerticalRuler = /*new VerticalRuler(16, mnualMarkerAnnotationAccess)*/null;
		fileYamlViewer = new SourceViewer(fileYamlComposite, fileVerticalRuler, fileOverviewRuler, true,
				SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION);
		fileYamlViewer.configure(new ManifestYamlSourceViewerConfiguration(ShellProviders.from(composite)));
		fileYamlViewer.getControl().setLayoutData(GridDataFactory.fillDefaults().grab(true, true).hint(SWT.DEFAULT, 200).create());
		fileYamlDecorationSupport = new SourceViewerDecorationSupport(fileYamlViewer, fileOverviewRuler, fileMarkerAnnotationAccess, colorsCache);

		manualYamlComposite = new Composite(yamlGroup, SWT.NONE);
		manualYamlComposite.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
		layout = new GridLayout();
		layout.marginWidth = 0;
		manualYamlComposite.setLayout(layout);

		Label manualYamlDescriptionLabel = new Label(manualYamlComposite, SWT.WRAP);
		manualYamlDescriptionLabel.setText(readOnly ? "Preview of the contents of the auto-generated deployment manifest:" : "Edit deployment manifest contents:");
		manualYamlDescriptionLabel.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());


		DefaultMarkerAnnotationAccess manualMarkerAnnotationAccess = new DefaultMarkerAnnotationAccess();
		OverviewRuler manualOverviewRuler = new OverviewRuler(manualMarkerAnnotationAccess, 12, colorsCache);
		VerticalRuler manualVerticalRuler = /*new VerticalRuler(16, mnualMarkerAnnotationAccess)*/null;
		manualYamlViewer = new SourceViewer(manualYamlComposite, manualVerticalRuler,
				manualOverviewRuler, true,
				SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION);
		manualYamlViewer.configure(new ManifestYamlSourceViewerConfiguration(ShellProviders.from(composite)));
		manualYamlViewer.getControl().setLayoutData(GridDataFactory.fillDefaults().grab(true, true).hint(SWT.DEFAULT, 200).create());
		manualYamlViewer.getControl().setEnabled(!readOnly);
		manualYamlDecorationSupport = new SourceViewerDecorationSupport(manualYamlViewer, manualOverviewRuler, manualMarkerAnnotationAccess, colorsCache);
		manualYamlViewer.setDocument(new Document(defaultYaml == null ? "" : defaultYaml), new AnnotationModel());

		/*
		 * Set preferences for viewers decoration support
		 */
		for (AnnotationPreference preference : (List<AnnotationPreference>) new MarkerAnnotationPreferences().getAnnotationPreferences()) {
			manualYamlDecorationSupport.setAnnotationPreference(preference);
			fileYamlDecorationSupport.setAnnotationPreference(preference);
		}
		manualYamlDecorationSupport.install(EditorsUI.getPreferenceStore());
		fileYamlDecorationSupport.install(EditorsUI.getPreferenceStore());

		/*
		 * Set the proper Font on the YAML viewers
		 */
		fileYamlViewer.getTextWidget().setFont(JFaceResources.getTextFont());
		manualYamlViewer.getTextWidget().setFont(JFaceResources.getTextFont());
	}

	private boolean saveOrDiscardIfNeeded(boolean cancellable) {
		FileEditorInput file = fileModel.getValue();
		TextFileDocumentProvider docProvider = file == null ? null : getTextDocumentProvider(file.getFile());
		boolean persisted = true;
		if (docProvider != null && file != null && file.exists() && docProvider.canSaveDocument(file)) {
			List<String> buttonLabels = new ArrayList<>();
			buttonLabels.add(SAVE_BTN_LABEL);
			buttonLabels.add(DISCARD_BTN_LABEL);
			if (cancellable) {
				buttonLabels.add(IDialogConstants.CANCEL_LABEL);
			}
			int result = new MessageDialog(getShell(), "Changes Detected", null,
					"Masnifest file '" + file.getFile().getFullPath().toOSString()
							+ "' has been changed. Do you want to save changes or discard them?",
					MessageDialog.QUESTION, buttonLabels.toArray(new String[buttonLabels.size()]), buttonLabels.indexOf(SAVE_BTN_LABEL)).open();
			if (result >= 0 && SAVE_BTN_LABEL.equals(buttonLabels.get(result))) {
				try {
					docProvider.saveDocument(new NullProgressMonitor(), file, docProvider.getDocument(file), true);
					mustSaveFiles.remove(file);
				} catch (CoreException e) {
					BootDashActivator.log(e);
				}
			} else if (result >= 0 && DISCARD_BTN_LABEL.equals(buttonLabels.get(result))) {
				try {
					docProvider.resetDocument(file);
					mustSaveFiles.remove(file);
				} catch (CoreException e) {
					BootDashActivator.log(e);
				}
			} else {
				/*
				 * Cancel is pressed
				 */
				persisted = false;
			}
		}
		return persisted;
	}

//	private void performSaveIfNeeded() {
//		FileEditorInput file = fileModel.getValue();
//		TextFileDocumentProvider docProvider = file == null ? null : getTextDocumentProvider(file.getFile());
//		if (docProvider != null && file != null && docProvider.canSaveDocument(file)) {
//			List<String> buttonLabels = Arrays.asList(new String[] {
//				SAVE_BTN_LABEL,
//				DISCARD_BTN_LABEL,
//				LATER_BTN_LABEL
//			});
//			int result = new MessageDialog(getShell(), "Changes Detected", null,
//					"Masnifest file '" + file.getFile().getFullPath().toOSString()
//							+ "' has been changed. Do you want to save changes now, later or discard them?",
//					MessageDialog.QUESTION_WITH_CANCEL, buttonLabels.toArray(new String[buttonLabels.size()]), 2)
//							.open();
//			if (SAVE_BTN_LABEL.equals(buttonLabels.get(result))) {
//				try {
//					docProvider.saveDocument(new NullProgressMonitor(), file, docProvider.getDocument(file), true);
//					mustSaveFiles.remove(file);
//				} catch (CoreException e) {
//					BootDashActivator.log(e);
//				}
//			} else if (DISCARD_BTN_LABEL.equals(buttonLabels.get(result))) {
//				try {
//					docProvider.resetDocument(file);
//					mustSaveFiles.remove(file);
//				} catch (CoreException e) {
//					BootDashActivator.log(e);
//				}
//			} else {
//				/*
//				 * Gather files that must be saved, files that are not opened for
//				 * editing in other editors. Keep them to revert changes when the
//				 * dialog is closed.
//				 * NOTE: canSave and mustSave would return the same result for this doc provider
//				 */
//				if (docProvider.mustSaveDocument(file)) {
//					mustSaveFiles.add(file);
//				}
//			}
//		}
//	}

	private void activateHanlders() {
		if (service != null) {
			for (EditorActionHandler handler : handlers) {
				activations.add(service.activateHandler(handler.getActionId(), handler));
			}
		}
	}

	private void deactivateHandlers() {
		if (service != null) {
			for (IHandlerActivation activation : activations) {
				service.deactivateHandler(activation);
			}
			activations.clear();
		}
	}

	private void refreshManifests() {
		IResource selectedResource = (IResource) ((IStructuredSelection) workspaceViewer.getSelection()).getFirstElement();
		final IResource resourceToRefresh = selectedResource instanceof IFile ? selectedResource.getParent() : selectedResource;
		Job job = new Job("Find all YAML files for project '" + project.getName() + "'") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				IStatus status = Status.OK_STATUS;
				try {
					resourceToRefresh.refreshLocal(IResource.DEPTH_INFINITE, monitor);
				} catch (CoreException e) {
					status = e.getStatus();
				}
				getParentShell().getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						// Remove selection listener to set selection from current pathModel value
						workspaceViewer.removeSelectionChangedListener(selectionListener);
						workspaceViewer.refresh(resourceToRefresh);
						updateManifestFile();
						// Add the selection listener back after the initial value has been set
						workspaceViewer.addSelectionChangedListener(selectionListener);
					}
				});
				return status;
			}
		};
		job.setRule(project);
		job.schedule();
	}

	private void updateManifestFile() {
		fileYamlViewer.setDocument(new Document(""));
		final FileEditorInput input = fileModel.getValue();
		if (input == null) {
			fileLabel.setText(NO_MANIFEST_SELECETED_LABEL);
			fileYamlViewer.getControl().setEnabled(false);
			validate();
		} else {
			final IFile file = input.getFile();
			fileLabel.setText(file.getFullPath().toOSString());
			Job job = new Job("Loading YAML manifest file") {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					final TextFileDocumentProvider docProvider = getTextDocumentProvider(file);
					if (docProvider == null) {
						showBlankContent();
					} else {
						try {
							IDocument doc = docProvider.getDocument(input);
							if (doc == null) {
								docProvider.connect(input);
								doc = docProvider.getDocument(input);
							}
							/*
							 * Parse manifest YAML
							 */
							Composer composer = new Composer(new ParserImpl(new StreamReader(new InputStreamReader(
									doc == null ? file.getContents() : new ByteArrayInputStream(doc.get().getBytes())))),
									new Resolver());
							final Node root = composer.getSingleNode();
							getShell().getDisplay().asyncExec(new Runnable() {
								@Override
								public void run() {
									fileYamlViewer.getControl().setEnabled(root != null);
									if (root == null) {
										fileYamlViewer.setDocument(new Document(""));
									} else {
										fileYamlViewer.setDocument(docProvider.getDocument(input), docProvider.getAnnotationModel(input));
										fileLabel.setText(file.getFullPath().toOSString() + (docProvider.canSaveDocument(input) ? "*" : ""));
									}
									validate();
								}
							});
						} catch (final Throwable t) {
							showBlankContent();
						}
					}
					return Status.OK_STATUS;
				}
			};
			job.setRule(file);
			job.schedule();
		}
	}

	private void showBlankContent() {
		getShell().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				fileYamlViewer.setDocument(new Document(""));
				fileYamlViewer.getControl().setEnabled(false);
				validate();
			}
		});
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	private void validate() {
		if (manifestTypeModel.getValue()) {
			setMessage("Choose an existing deployment manifest YAML file from the local file system.", IMessageProvider.INFORMATION);
		} else {
			if (readOnly) {
				setMessage("Current generated deployment manifest", IMessageProvider.INFORMATION);
			} else {
				setMessage("Enter deployment manifest YAML manually", IMessageProvider.INFORMATION);
			}
		}

		String error = null;
		if (manifestTypeModel.getValue()) {
			if (fileModel.getValue() == null) {
				error = "Deployment manifest file not selected";
			} else if (fileYamlViewer.getDocument().get().isEmpty()) {
				error = "Unable to load deployment manifest YAML file";
			}
		}
		setErrorMessage(error);
	}

	@Override
	public void setErrorMessage(String newErrorMessage) {
		super.setErrorMessage(newErrorMessage);
		if (getButton(IDialogConstants.OK_ID) != null) {
			getButton(IDialogConstants.OK_ID).setEnabled(newErrorMessage == null);
		}
	}

	public IFile getManifest() {
		if (manifestTypeModel.getValue()) {
			FileEditorInput input = fileModel.getValue();
			if (input != null) {
				return input.getFile();
			}
		}
		return null;
	}

	public String getManifestContents() {
		return manifestTypeModel.getValue() ? fileYamlViewer.getDocument().get() : manualYamlViewer.getDocument().get();
	}

	@Override
	protected IDialogSettings getDialogBoundsSettings() {
		return DialogSettings.getOrCreateSection(BootDashActivator.getDefault().getDialogSettings(), "ManifestFileDialog");
	}

	@Override
	public boolean close() {
		getDialogBoundsSettings().put(DIALOG_LIST_HEIGHT_SETTING, ((GridData)fileGroup.getLayoutData()).heightHint);
		boolean close = super.close();
		dispose();
		return close;
	}

	protected void dispose() {
		/*
		 * Remove element state listeners from document providers.
		 */
		for (TextFileDocumentProvider p : docProviders) {
			p.removeElementStateListener(dirtyStateListener);
		}

		/*
		 * Check if current file must be saved and add to the list all must save files
		 */
//		FileEditorInput input = fileModel.getValue();
//		TextFileDocumentProvider docProvider = input == null ? null : getTextDocumentProvider(input.getFile());
//		if (input != null && docProvider != null && docProvider.mustSaveDocument(input)) {
//			mustSaveFiles.add(input);
//		}
		saveOrDiscardIfNeeded(false);

		/*
		 * Deactivate handlers for key bindings
		 */
		deactivateHandlers();

		/*
		 * dispose SWT and JFace resources
		 */
		colorsCache.dispose();
		if (manualYamlDecorationSupport != null) {
			manualYamlDecorationSupport.dispose();
		}
		if (fileYamlDecorationSupport != null) {
			fileYamlDecorationSupport.dispose();
		}

		/*
		 * Revert changes to unsaved files that are not opened by any other editors
		 */
//		for (FileEditorInput inputFile : mustSaveFiles) {
//			docProvider = inputFile == null ? null : getTextDocumentProvider(inputFile.getFile());
//			if (docProvider != null && docProvider.mustSaveDocument(inputFile)) {
//				try {
//					docProvider.resetDocument(inputFile);
//				} catch (CoreException e) {
//					BootDashActivator.log(e);
//				}
//			}
//		}
	}

	@Override
	protected void okPressed() {
		/*
		 * Save the manifest file if it's dirty
		 */
		FileEditorInput value = fileModel.getValue();
		TextFileDocumentProvider docProvider = value == null ? null : getTextDocumentProvider(value.getFile());
		if (value != null && docProvider != null && docProvider.canSaveDocument(value)) {
			if (!MessageDialog.openConfirm(getShell(), "Save Changes",
					"Manifest file '" + value.getFile().getFullPath()
							.toOSString()
					+ "' has unsaved changes. Changes must be saved before using the file to deploy the application.")) {
				return;
			}
			try {
				docProvider.saveDocument(new NullProgressMonitor(), value, docProvider.getDocument(value), true);
				mustSaveFiles.remove(value);
			} catch (CoreException e) {
				BootDashActivator.log(e);
				MessageDialog.openError(getShell(), "Error Saving Manifest", "Error occurred saving the manifest file '" + value.getFile().getFullPath().toOSString() + "'.\nError: " + e.getMessage());
				return;
			} catch (Throwable t) {
				BootDashActivator.log(t);
				MessageDialog.openError(getShell(), "Errors in the Manifest File", "Error occurred parsing the manifest file '" + value.getFile().getFullPath().toOSString() + "'.\nError: " + t.getMessage());
				return;
			}
		}
		try {
			List<CloudApplicationDeploymentProperties> propsList = new ApplicationManifestHandler(project, cloudData, getManifest()) {
				@Override
				protected InputStream getInputStream() throws Exception {
					return new ByteArrayInputStream(getManifestContents().getBytes());
				}
			}.load(new NullProgressMonitor());

			List<String> appNames = new ArrayList<>();
			for (CloudApplicationDeploymentProperties p : propsList) {
				String name = p.getAppName();
				if (name != null && !appNames.contains(name)) {
					appNames.add(name);
				}
			}

			if (appNames.isEmpty()) {
				MessageDialog.openError(getShell(), "Invalid Manifest", "Manifest YAML does not contain any applications");
				return;
			} else {
				if (appName == null || appNames.size() > 1) {
					ListDialog dialog = new ListDialog(getShell());
					dialog.setContentProvider(ArrayContentProvider.getInstance());
					dialog.setLabelProvider(new LabelProvider());
					dialog.setTitle("Select Name");
					dialog.setMessage("Select application name:");
					dialog.setInput(appNames);
					if (dialog.open() == IDialogConstants.OK_ID && dialog.getResult().length > 0) {
						String applicationName = (String) dialog.getResult()[0];
						for (CloudApplicationDeploymentProperties p : propsList) {
							if (applicationName.equals(p.getAppName())) {
								deploymentProperties = p;
								break;
							}
						}
					} else {
						/*
						 * Cancel everything
						 */
						return;
					}
				} else {
					deploymentProperties = propsList.get(0);
				}
			}
			super.okPressed();
		} catch (Exception e) {
			MessageDialog.openError(getShell(), "Invalid YAML content", e.getMessage());
		}
	}

	private TextFileDocumentProvider getTextDocumentProvider(IFile file) {
		TextFileDocumentProvider textDocProvider = null;
		if (file != null) {
			IDocumentProvider docProvider = DocumentProviderRegistry.getDefault().getDocumentProvider(new FileEditorInput(file));
			if (docProvider instanceof TextFileDocumentProvider) {
				textDocProvider = (TextFileDocumentProvider) docProvider;
				if (!docProviders.contains(textDocProvider)) {
					textDocProvider.addElementStateListener(dirtyStateListener);
					docProviders.add(textDocProvider);
				}
			}
		}
		return textDocProvider;
	}

	public CloudApplicationDeploymentProperties getCloudApplicationDeploymentProperties() {
		return deploymentProperties;
	}

	@Override
	protected int getDialogBoundsStrategy() {
		return DIALOG_PERSISTSIZE;
	}

	@Override
	protected Point getInitialSize() {
		Point size = super.getInitialSize();
		/*
		 * If manual mode is selected fileGroup is missing and not accounted in
		 * the size of the dialog shell. Add its height here manually if dialog
		 * size was not persisted previously
		 */
		GridData fileGroupLayoutData = (GridData)fileGroup.getLayoutData();
		if (fileGroupLayoutData.exclude) {
			try {
				/*
				 * Hack: check if dialog width/height was persisted. If
				 * persisted then no need to calculate dialog size
				 */
				getDialogBoundsSettings().getInt("DIALOG_WIDTH");
			} catch (NumberFormatException e) {
				/*
				 * Exception is thrown if dialog width/height cannot be read
				 * from storage
				 */
				size.y += fileGroupLayoutData.heightHint;
			}
		}
		return size;
	}

	private class EditorActionHandler extends AbstractHandler {

		private String actionId;
		private int operationId;

		public EditorActionHandler(String actionId, int operationId) {
			super();
			this.actionId = actionId;
			this.operationId = operationId;
		}

		public String getActionId() {
			return actionId;
		}

		@Override
		public Object execute(ExecutionEvent arg0) throws ExecutionException {
			if (manualYamlViewer.isEditable() && manualYamlViewer.getControl().isVisible()
					&& manualYamlViewer.getTextWidget().isFocusControl()) {
				manualYamlViewer.doOperation(operationId);
			} else if (fileYamlViewer.isEditable() && fileYamlViewer.getControl().isVisible()
					&& fileYamlViewer.getTextWidget().isFocusControl()) {
				fileYamlViewer.doOperation(operationId);
			}
			return null;
		}
	}

	public static IFile findManifestYamlFile(IProject project) {
		if (project == null) {
			return null;
		}
		IFile file = project.getFile("manifest.yml");
		if (file.exists()) {
			return file;
		}
		IFile yamlFile = null;
		try {
			for (IResource r : project.members()) {
				if (r instanceof IFile) {
					file = (IFile) r;
					if (MANIFEST_YAML_FILE_FILTER.select(null, project, file)) {
						return file;
					} else if (YAML_FILE_FILTER.select(null, project, file)) {
						yamlFile = file;
					}
				}
			}
		} catch (CoreException e) {
			// ignore
		}
		return yamlFile;
	}

}
