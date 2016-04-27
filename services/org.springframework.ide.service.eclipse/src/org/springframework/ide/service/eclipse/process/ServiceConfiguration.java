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

import org.eclipse.core.resources.IProject;

/**
 * @author Martin Lippert
 */
public class ServiceConfiguration {
	
	private final IProject project;
	private final String configRoot;
	private final ServiceProcessConfiguration processConfig;

	public ServiceConfiguration(IProject project, String configRoot, ServiceProcessConfiguration processConfig) {
		this.project = project;
		this.configRoot = configRoot;
		this.processConfig = processConfig;
	}
	
	public IProject getProject() {
		return project;
	}
	
	public String getConfigRoot() {
		return configRoot;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((configRoot == null) ? 0 : configRoot.hashCode());
		result = prime * result + ((processConfig == null) ? 0 : processConfig.hashCode());
		result = prime * result + ((project == null) ? 0 : project.hashCode());
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
		ServiceConfiguration other = (ServiceConfiguration) obj;
		if (configRoot == null) {
			if (other.configRoot != null)
				return false;
		} else if (!configRoot.equals(other.configRoot))
			return false;
		if (processConfig == null) {
			if (other.processConfig != null)
				return false;
		} else if (!processConfig.equals(other.processConfig))
			return false;
		if (project == null) {
			if (other.project != null)
				return false;
		} else if (!project.equals(other.project))
			return false;
		return true;
	}
	
}
