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

/**
 * @author Martin Lippert
 */
public class ServiceProcess {

	private final Process serviceProcess;
	private final ServiceProcessConfiguration processConfiguration;

	private SpringToolingService toolingService;

	public ServiceProcess(Process serviceProcess, ServiceProcessConfiguration processConfiguration) {
		this.serviceProcess = serviceProcess;
		this.processConfiguration = processConfiguration;
		this.toolingService = null;
	}
	
	public ServiceProcessConfiguration getProcessConfiguration() {
		return processConfiguration;
	}

	public boolean isAlive() {
		return this.serviceProcess != null && this.serviceProcess.isAlive();
	}

	public void kill() {
		if (this.serviceProcess != null) {
			this.serviceProcess.destroyForcibly();
			this.toolingService = null;
		}
	}

	public SpringToolingService connectTo(ServiceConfiguration serviceConfiguration) {
		if (toolingService != null) {
			toolingService = new SpringToolingService(serviceConfiguration, serviceProcess.getInputStream(), serviceProcess.getOutputStream(),
					serviceProcess.getErrorStream());
		}
		return toolingService;
	}
	
	public Process getInternalProcess() {
		return this.serviceProcess;
	}

}
