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
package org.springframework.ide.eclipse.boot.core;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;

/**
 * A nature that can be added to a Spring project to explicitly tag
 * it as a Spring Boot project. This nature is optional. The spring-boot-specific
 * tooling are activated automatically when a project 'looks like' a spring-boot
 * project based on its classpath. The explicit tagging of a project bootnature
 * makes it possible to bypass the autodetection. This could be useful,
 * for example when the autodetection failed for some reason (e.g. see
 * https://www.pivotaltracker.com/story/show/106866760)
 *
 * @author Kris De Volder
 */
public class SpringBootNature implements IProjectNature {

	public static final String NATURE_ID = "org.springframework.ide.eclipse.boot.bootnature";

	private IProject project;

	@Override
	public void configure() throws CoreException {
		//This nature is a simple tag, nothing to do
	}

	@Override
	public void deconfigure() throws CoreException {
		//This nature is a simple tag, nothing to do
	}

	@Override
	public IProject getProject() {
		return project;
	}

	@Override
	public void setProject(IProject project) {
		this.project = project;
	}

}
