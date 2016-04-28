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
package org.springframework.ide.service.eclipse.process;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Martin Lippert
 */
public class ServiceManager {
	
	private Map<ServiceProcessConfiguration, ServiceProcess> services;
	
	public ServiceManager() {
		this.services = new ConcurrentHashMap<>();
		
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override
			public void run() {
				ServiceManager.this.services.values().forEach((process) -> process.kill());
			}
		}));
	}

	public void startServiceProcess(ServiceProcessConfiguration processConfig) throws Exception {
		ServiceProcess process = services.get(processConfig);
		if (process != null) {
			process.kill();
		}
		
		ServiceProcess newProcess = ServiceProcessFactory.createServiceProcess(processConfig);
		this.services.put(processConfig, newProcess);
	}

	public void stopServiceProcess(ServiceProcessConfiguration processConfig) throws Exception {
		ServiceProcess process = services.get(processConfig);
		if (process != null) {
			process.kill();
			services.remove(processConfig);
		}
	}

	public boolean isServiceProcessRunning(ServiceProcessConfiguration processConfig) {
		ServiceProcess process = services.get(processConfig);
		return process != null && process.isAlive();
	}
	
	public ServiceProcess getServiceProcess(ServiceProcessConfiguration processConfiguration) {
		return services.get(processConfiguration);
	}

	public ServiceProcess[] getServiceProcesses() {
		return this.services.values().toArray(new ServiceProcess[this.services.size()]);
	}

}
