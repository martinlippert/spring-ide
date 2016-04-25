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
public class ServiceManager implements IServiceManager {
	
	private Map<ServiceConfiguration, ServiceProcess> services;
	
	public ServiceManager() {
		this.services = new ConcurrentHashMap<>();
	}

	@Override
	public void startService(ServiceConfiguration serviceConfig) {
		ServiceProcess process = services.get(serviceConfig);
		if (process != null) {
			process.kill();
		}
		
		ServiceProcess newProcess = ServiceProcessBuilder.createServiceProcess(serviceConfig);
		this.services.put(serviceConfig, newProcess);
	}

	@Override
	public void stopService(ServiceConfiguration serviceConfig) {
		ServiceProcess process = services.get(serviceConfig);
		if (process != null) {
			process.kill();
			services.remove(serviceConfig);
		}
	}

	@Override
	public boolean isServiceRunning(ServiceConfiguration serviceConfig) {
		ServiceProcess process = services.get(serviceConfig);
		return process != null && process.isAlive();
	}
	
	@Override
	public ServiceProcess getServiceProcess(ServiceConfiguration serviceConfiguration) {
		return services.get(serviceConfiguration);
	}

	@Override
	public ServiceProcess[] getServiceProcesses() {
		return this.services.values().toArray(new ServiceProcess[this.services.size()]);
	}

}
