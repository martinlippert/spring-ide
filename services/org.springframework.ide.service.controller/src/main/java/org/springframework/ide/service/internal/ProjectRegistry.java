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
package org.springframework.ide.service.internal;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Martin Lippert
 */
public class ProjectRegistry {
	
	private ConcurrentMap<String, ProjectSetup> projects;
	
	public ProjectRegistry() {
		this.projects = new ConcurrentHashMap<>();
	}
	
	public void add(ProjectSetup projectSetup) {
		ProjectSetup addedProjectSetup = this.projects.putIfAbsent(projectSetup.getProjectName(), projectSetup);
		if (projectSetup != addedProjectSetup) {
			throw new RuntimeException("project already exists");
		}
	}
	
	public ProjectSetup get(String projectName) {
		return this.projects.get(projectName);
	}
	
	public void remove(String projectName) {
		this.projects.remove(projectName);
	}
	
	public boolean has(String projectName) {
		return this.projects.containsKey(projectName);
	}

}
