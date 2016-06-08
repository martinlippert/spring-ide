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
package org.springframework.ide.service.eclipse.config;

import org.eclipse.jdt.launching.IVMInstall;
import org.springframework.ide.service.eclipse.process.EnvironmentConfiguration;

/**
 * @author Martin Lippert
 */
public class ServiceProcessConfiguration {
	
	private final IVMInstall jdk;
	private final String vmargs;
	private final EnvironmentConfiguration environment;
	private final String name;
	
	public ServiceProcessConfiguration(IVMInstall jdk, String vmargs, EnvironmentConfiguration environment) {
		this.jdk = jdk;
		this.vmargs = vmargs;
		this.environment = environment;
		this.name = "ToolingServiceProcess";
	}
	
	public IVMInstall getJdkConfiguration() {
		return this.jdk;
	}
	
	public String getVmargs() {
		return this.vmargs;
	}
	
	public EnvironmentConfiguration getEnvironment() {
		return this.environment;
	}

	public String getName() {
		return this.name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((environment == null) ? 0 : environment.hashCode());
		result = prime * result + ((jdk == null) ? 0 : jdk.getName().hashCode());
		result = prime * result + ((vmargs == null) ? 0 : vmargs.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ServiceProcessConfiguration other = (ServiceProcessConfiguration) obj;
		if (environment == null) {
			if (other.environment != null)
				return false;
		} else if (!environment.equals(other.environment))
			return false;
		if (jdk == null) {
			if (other.jdk != null)
				return false;
		} else if (!jdk.getName().equals(other.jdk.getName()))
			return false;
		if (vmargs == null) {
			if (other.vmargs != null)
				return false;
		} else if (!vmargs.equals(other.vmargs))
			return false;
		return true;
	}

}
