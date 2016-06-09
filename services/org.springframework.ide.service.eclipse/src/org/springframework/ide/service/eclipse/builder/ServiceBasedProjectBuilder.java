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
package org.springframework.ide.service.eclipse.builder;

import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.springframework.ide.service.eclipse.Activator;
import org.springframework.ide.service.eclipse.ServiceManager;
import org.springframework.ide.service.eclipse.config.ServiceConfiguration;
import org.springframework.ide.service.eclipse.config.ServiceConfigurationStorage;
import org.springframework.ide.service.eclipse.process.ToolingService;

public class ServiceBasedProjectBuilder extends IncrementalProjectBuilder {

	public static final String BUILDER_ID = "org.springframework.ide.service.eclipse.serviceBasedProjectBuilder";

	@Override
	protected IProject[] build(int kind, Map<String, String> args, IProgressMonitor monitor)
			throws CoreException {
		if (kind == FULL_BUILD) {
			fullBuild(monitor);
		} else {
			IResourceDelta delta = getDelta(getProject());
			if (delta == null) {
				fullBuild(monitor);
			} else {
				incrementalBuild(delta, monitor);
			}
		}
		return null;
	}

	protected void clean(IProgressMonitor monitor) throws CoreException {
	}

	protected void fullBuild(final IProgressMonitor monitor)
			throws CoreException {
		ServiceConfiguration[] serviceConfigs = getServiceConfigs(getProject());
		for (int i = 0; i < serviceConfigs.length; i++) {
			fullBuild(serviceConfigs[i], monitor);
		}
	}

	private void fullBuild(ServiceConfiguration serviceConfiguration, IProgressMonitor monitor) {
		try {
			ServiceManager serviceManager = Activator.getDefault().getServiceManager();
			ToolingService toolingService = serviceManager.getToolingService(serviceConfiguration);
			toolingService.triggerFullBuild();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void incrementalBuild(IResourceDelta delta,
			IProgressMonitor monitor) throws CoreException {
		ServiceConfiguration[] serviceConfigs = getServiceConfigs(getProject());
		for (int i = 0; i < serviceConfigs.length; i++) {
			incrementalBuild(serviceConfigs[i], delta, monitor);
		}
	}
	
	private void incrementalBuild(ServiceConfiguration serviceConfiguration, IResourceDelta delta, IProgressMonitor monitor) {
		try {
			ServiceManager serviceManager = Activator.getDefault().getServiceManager();
			ToolingService toolingService = serviceManager.getToolingService(serviceConfiguration);
			toolingService.triggerIncrementalBuild(delta);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private ServiceConfiguration[] getServiceConfigs(IProject project) {
		ServiceConfiguration[] configs = ServiceConfigurationStorage.readConfigs(project);
		return configs;
	}

}
