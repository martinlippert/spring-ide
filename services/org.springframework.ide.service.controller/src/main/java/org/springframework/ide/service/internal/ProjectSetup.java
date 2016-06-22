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

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URLClassLoader;

import org.springframework.ide.service.controller.BackChannel;

/**
 * @author Martin Lippert
 */
public class ProjectSetup {
	
	private static final String AGENT_MAIN_CLASS = "org.springframework.ide.service.agent.AgentMain";
	
	private static final String AGENT_MAIN_METHOD = "main";
	private static final Class<?>[] AGENT_MAIN_PARAMETERS = new Class<?>[] {String.class, String[].class, Object.class};

	private static final String AGENT_CREATE_MODEL_METHOD = "createModel";
	private static final Class<?>[] AGENT_CREATE_MODEL_PARAMETERS = new Class<?>[] {};
	
	private static final String AGENT_GET_BEAN_NAMES_METHOD = "getBeanNames";
	private static final Class<?>[] AGENT_GET_BEAN_NAMES_PARAMETERS = new Class<?>[] {String.class};
	
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
	
	public void start() throws Exception {
		libLoader = new URLClassLoader(this.project.getClasspath(), this.getClass().getClassLoader());
		sourceLoader = new URLClassLoader(this.project.getSourcepath(), libLoader);
		agentLoader = new URLClassLoader(this.project.getAgentClasspath(), sourceLoader);
		
		agentMainClass = agentLoader.loadClass(AGENT_MAIN_CLASS);
		Method agentMainMethod = agentMainClass.getMethod(AGENT_MAIN_METHOD, AGENT_MAIN_PARAMETERS);
		agentMainMethod.invoke(null, this.project.getProjectName(), (String[]) this.project.getSpringConfigFiles(), backchannel);
	}
	
	public void update() throws Exception {
		URLClassLoader oldSourceLoader = this.sourceLoader;
		URLClassLoader oldAgentLoader = this.agentLoader;
		
		cleanupModel();

		sourceLoader = new URLClassLoader(this.project.getSourcepath(), libLoader);
		agentLoader = new URLClassLoader(this.project.getAgentClasspath(), sourceLoader);
		
		agentMainClass = agentLoader.loadClass(AGENT_MAIN_CLASS);
		Method agentMainMethod = agentMainClass.getMethod(AGENT_MAIN_METHOD, AGENT_MAIN_PARAMETERS);
		agentMainMethod.invoke(null, this.project.getProjectName(), (String[]) this.project.getSpringConfigFiles(), backchannel);
		
		oldSourceLoader.close();
		oldAgentLoader.close();
	}

	public void cleanupModel() throws Exception {
		// call the model cleanup method via reflection
	}

	public void createModel() throws Exception {
		Method createModelMethod = agentMainClass.getMethod(AGENT_CREATE_MODEL_METHOD, AGENT_CREATE_MODEL_PARAMETERS);
		createModelMethod.invoke(null,  new Object[] {});
	}
	
	public String[] getBeanNames(String typeHint) throws Exception {
		Method getBeanNamesMethod = agentMainClass.getMethod(AGENT_GET_BEAN_NAMES_METHOD, AGENT_GET_BEAN_NAMES_PARAMETERS);
		return (String[]) getBeanNamesMethod.invoke(null,  new Object[] {typeHint});
	}

	public void close() {
		try {
			backchannel = null;
			agentMainClass = null;
			libLoader.close();
			sourceLoader.close();
			agentLoader.close();
		} catch (IOException e) {
		}
	}

}