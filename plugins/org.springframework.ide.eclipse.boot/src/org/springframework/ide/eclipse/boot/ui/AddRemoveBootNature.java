/*******************************************************************************
 * Copyright (c) 2015 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.springframework.ide.eclipse.boot.core.BootActivator;
import org.springframework.ide.eclipse.boot.core.SpringBootNature;
import org.springframework.ide.eclipse.core.SpringCore;
import org.springsource.ide.eclipse.commons.core.util.NatureUtils;

/**
 * @author Kris De Volder
 */
public class AddRemoveBootNature implements IObjectActionDelegate {

	private List<IProject> projects;

	@Override
	public void run(IAction action) {
		try {
			if (projects!=null && !projects.isEmpty()) {
				final IProject[] projects = this.projects.toArray(new IProject[this.projects.size()]);
				final boolean remove = projects[0].hasNature(SpringBootNature.NATURE_ID);
				Job job = new Job("Add Boot Nature to projects") {
					@Override
					protected IStatus run(IProgressMonitor monitor) {
						String addRemove = remove ? "Remove" : "Add";
						if (projects!=null && projects.length>0) {
							monitor.beginTask(addRemove+" Boot Nature to projects", projects.length);
							for (IProject project : projects) {
								try {
									monitor.subTask(addRemove+" Boot Nature to '"+project.getName()+"'");
									if (remove) {
										NatureUtils.remove(project, SpringBootNature.NATURE_ID, new SubProgressMonitor(monitor, 1));
									} else { //add
										NatureUtils.ensure(project, new SubProgressMonitor(monitor, 1), SpringBootNature.NATURE_ID);
									}
								} catch (CoreException e) {
									BootActivator.log(e);
								}
							}
						}
						return Status.OK_STATUS;
					}
				};
				job.setRule(ResourcesPlugin.getWorkspace().getRuleFactory().buildRule());
				job.setPriority(Job.INTERACTIVE);
				job.schedule();
			}
		} catch (Exception e) {
			BootActivator.log(e);
		}
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		projects = getProjects(selection);
		boolean enable = projects!=null && !projects.isEmpty();
		action.setEnabled(enable);
		if (enable) {
			IProject p = projects.get(0);
			if (hasNature(p)) {
				action.setText("Remove Boot Nature");
			} else {
				action.setText("Add Boot Nature");
			}
		} else {
			action.setText("Add/Remove Boot Nature");
		}
	}

	private boolean hasNature(IProject p) {
		try {
			return p.hasNature(SpringBootNature.NATURE_ID);
		} catch (CoreException e) {
			BootActivator.log(e);
		}
		return false;
	}

	private List<IProject> getProjects(ISelection selection) {
		ArrayList<IProject> projects = new ArrayList<>();
		try {
			if (selection instanceof IStructuredSelection) {
				IStructuredSelection ss = (IStructuredSelection) selection;
				if (ss.size()>0) {
					for (Object e : ss.toArray()) {
						IProject p = asProject(e);
						if (p!=null && p.isAccessible() && p.hasNature(SpringCore.NATURE_ID)) {
							projects.add((IProject) e);
						}
					}
				}
			}
		} catch (Exception e) {
			BootActivator.log(e);
		}
		return projects;
	}

	private IProject asProject(Object e) {
		if (e instanceof IProject) {
			return (IProject) e;
		} else if (e instanceof IAdaptable) {
			return (IProject)((IAdaptable) e).getAdapter(IProject.class);
		}
		return null;
	}

	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}

}
