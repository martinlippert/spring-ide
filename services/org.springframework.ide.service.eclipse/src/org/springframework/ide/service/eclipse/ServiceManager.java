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
package org.springframework.ide.service.eclipse;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.JavaCore;
import org.springframework.ide.service.eclipse.builder.ClasspathChangedListener;
import org.springframework.ide.service.eclipse.config.ServiceConfiguration;
import org.springframework.ide.service.eclipse.config.ServiceConfigurationStorage;
import org.springframework.ide.service.eclipse.config.ServiceProcessConfiguration;
import org.springframework.ide.service.eclipse.process.ServiceProcess;
import org.springframework.ide.service.eclipse.process.ServiceProcessManager;
import org.springframework.ide.service.eclipse.process.ToolingService;

/**
 * @author Martin Lippert
 */
public class ServiceManager {

	private final ServiceProcessManager serviceProcessManager;
	private final Map<ServiceConfiguration, ToolingService> toolingServices;

	public ServiceManager(ServiceProcessManager serviceProcessManager) {
		this.serviceProcessManager = serviceProcessManager;
		this.toolingServices = new ConcurrentHashMap<>();
		
		JavaCore.addElementChangedListener(new ClasspathChangedListener());
	}

	public ToolingService getToolingService(ServiceConfiguration serviceConfiguration) throws Exception {
		ServiceProcessConfiguration processConfig = serviceConfiguration.getProcessConfig();
		
		if (!this.serviceProcessManager.isServiceProcessRunning(processConfig)) {
			ServiceProcess serviceProcess = this.serviceProcessManager.startServiceProcess(processConfig);
			ToolingService service = new ToolingService(serviceConfiguration, serviceProcess);
			ToolingService oldService = toolingServices.put(serviceConfiguration, service);
			
			if (oldService != null) {
				oldService.close();
			}

			return service;
		}
		else {
			ToolingService service = toolingServices.get(serviceConfiguration);
			if (service == null) {
				ServiceProcess serviceProcess = this.serviceProcessManager.getServiceProcess(serviceConfiguration.getProcessConfig());
				service = new ToolingService(serviceConfiguration, serviceProcess);
				toolingServices.put(serviceConfiguration, service);
			}
			
			return service;
		}
	}
	
	public ServiceConfiguration[] getServiceConfigs(IProject project) {
		ServiceConfiguration[] configs = ServiceConfigurationStorage.readConfigs(project);
		return configs;
	}
	
	public void classpathChanged(IProject project) {
		for (ToolingService toolingService : toolingServices.values()) {
			toolingService.setClasspathChanged(project);
		}
	}
	
}
