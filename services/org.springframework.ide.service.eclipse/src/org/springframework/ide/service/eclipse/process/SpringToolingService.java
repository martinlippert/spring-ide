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

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

import org.eclipse.core.resources.IProject;
import org.json.JSONObject;

/**
 * This is the main Spring Tooling API for the external Spring tooling as a service.
 * 
 * @author Martin Lippert
 */
public class SpringToolingService {

	private PrintWriter writer;

	public SpringToolingService(ServiceConfiguration serviceConfiguration, InputStream inputStream, OutputStream outputStream, InputStream errorStream) {
		this.writer = new PrintWriter(outputStream);
	}
	
	public void setup(ServiceConfiguration serviceConfiguration) throws Exception {
		JSONObject setupMessage = new JSONObject();
		setupMessage.put("command-name", "setup-project");
		setupMessage.put("project-name", serviceConfiguration.getProject().getName());
		setupMessage.put("project-classpath", getProjectClasspath(serviceConfiguration.getProject()));
		setupMessage.put("project-sourcepath", getSourcePath(serviceConfiguration.getProject()));
		setupMessage.put("spring-config-files", serviceConfiguration.getConfigRoot());
		
		writer.print(setupMessage.toString());
		writer.flush();
	}

	private String getSourcePath(IProject project) {
		return null;
	}

	private String getProjectClasspath(IProject project) {
		return null;
	}

	public void fullUpdate() {
	}

}
