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

import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

import org.springframework.ide.service.controller.BackChannel;

/**
 * @author Martin Lippert
 */
public class ProjectSetup {
	
	private static final String AGENT_MAIN_CLASS = "org.springframework.ide.service.agent.AgentMain";
	private static final String AGENT_MAIN_METHOD = "main";
	private static final Class<?>[] AGENT_MAIN_PARAMETERS = new Class<?>[] {String.class, URL[].class, Object.class};
	
	private final Project project;
	
	private URLClassLoader libLoader;
	private URLClassLoader sourceLoader;
	private URLClassLoader agentLoader;
	private Class<?> agentMainClass;
	private BackChannel backchannel;

	public ProjectSetup(Project project, BackChannel backchannel) {
		this.project = project;
		this.backchannel = backchannel;
	}
	
	public String getProjectName() {
		return this.project.getProjectName();
	}
	
	public void start() {
		libLoader = new URLClassLoader(this.project.getClasspath(), this.getClass().getClassLoader());
		sourceLoader = new URLClassLoader(this.project.getSourcepath(), libLoader);
		agentLoader = new URLClassLoader(this.project.getAgentClasspath(), sourceLoader);
		
		try {
			agentMainClass = agentLoader.loadClass(AGENT_MAIN_CLASS);
			Method agentMainMethod = agentMainClass.getMethod(AGENT_MAIN_METHOD, AGENT_MAIN_PARAMETERS);
			agentMainMethod.invoke(null, this.project.getProjectName(), (Object[]) this.project.getSpringConfigFiles(), backchannel);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

}
