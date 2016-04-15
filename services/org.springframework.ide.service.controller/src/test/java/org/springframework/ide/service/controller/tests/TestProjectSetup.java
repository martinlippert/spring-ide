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
package org.springframework.ide.service.controller.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.URL;

import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.Before;
import org.junit.Test;
import org.springframework.ide.service.commands.GetFullModelCommand;
import org.springframework.ide.service.commands.SetupProjectCommand;
import org.springframework.ide.service.controller.BackChannel;
import org.springframework.ide.service.controller.CommandExecuter;
import org.springframework.ide.service.internal.ProjectRegistry;

/**
 * @author Martin Lippert
 */
public class TestProjectSetup {
	
	/**
	 * response timeout in ms
	 */
	private static final int TIMEOUT = 1000;

	private ByteArrayOutputStream resultErr;
	private BackChannel backchannel;
	private ProjectRegistry projectRegistry;
	
	private PrintWriter writer;
	private JSONTokener jsonResponseParser;

	private PipedInputStream pipedInputStream;
	private PipedOutputStream pipedOutputStream;

	@Before
	public void setup() throws Exception {
		resultErr = new ByteArrayOutputStream();
		
		PipedOutputStream outPipe = new PipedOutputStream();
		pipedInputStream = new PipedInputStream(outPipe);		
		writer = new PrintWriter(outPipe, true);
		
		PipedInputStream inPipe = new PipedInputStream();
		pipedOutputStream = new PipedOutputStream(inPipe);
		jsonResponseParser = new JSONTokener(inPipe);

		backchannel = new BackChannel(new PrintStream(pipedOutputStream), new PrintStream(resultErr));
		projectRegistry = new ProjectRegistry();
	}

	@Test
	public void testAgentClassNotFoundProblem() throws Exception {
		CommandExecuter executer = new CommandExecuter(pipedInputStream, backchannel, projectRegistry, new URL[0]);
		executer.addCommand(new SetupProjectCommand());
		executer.run();

		JSONObject setupMessage = new JSONObject();
		setupMessage.put("command-name", "setup-project");
		setupMessage.put("project-name", "TestProjectName");
		setupMessage.put("project-classpath", "");
		setupMessage.put("project-sourcepath", "");
		setupMessage.put("spring-config-files", "");
		
		writer.print(setupMessage.toString());
		writer.flush();
		
		Thread.sleep(TIMEOUT);
		
		JSONObject response = new JSONObject(jsonResponseParser);
		assertTrue(response.has("status"));
		assertEquals("project setup failed with exception: java.lang.ClassNotFoundException: org.springframework.ide.service.agent.AgentMain", response.get("status"));
		assertEquals("TestProjectName", response.get("project-name"));
		
		assertTrue(response.has("execution-time"));
		long time = response.getLong("execution-time");
		assertTrue(time >= 0);
		
		System.out.println("execution time: " + time);
		
		String error = resultErr.toString();
		assertTrue(error.length() > 0);
		assertTrue(error.contains("java.lang.ClassNotFoundException: org.springframework.ide.service.agent.AgentMain"));
	}

	@Test
	public void testSimpleEmptyProject() throws Exception {
		File parentDir = new File("").getAbsoluteFile().getParentFile();
		File agentClassDir = new File(parentDir, "org.springframework.ide.service.agent/target/classes");
		URL agentClasspath = agentClassDir.toURI().toURL();
		
		CommandExecuter executer = new CommandExecuter(pipedInputStream, backchannel, projectRegistry, new URL[] {agentClasspath});
		executer.addCommand(new SetupProjectCommand());
		executer.run();

		JSONObject setupMessage = new JSONObject();
		setupMessage.put("command-name", "setup-project");
		setupMessage.put("project-name", "TestProjectName");
		setupMessage.put("project-classpath", "");
		setupMessage.put("project-sourcepath", "");
		setupMessage.put("spring-config-files", "");
		
		writer.print(setupMessage.toString());
		writer.flush();
		
		Thread.sleep(TIMEOUT);
		
		JSONObject response = new JSONObject(jsonResponseParser);
		assertTrue(response.has("status"));
		assertEquals("project setup complete", response.get("status"));
		assertEquals("TestProjectName", response.get("project-name"));
		
		assertTrue(response.has("execution-time"));
		long time = response.getLong("execution-time");
		assertTrue(time >= 0);
		
		System.out.println("execution time: " + time);
		
		String error = resultErr.toString();
		assertTrue(error.length() == 0);
	}

	@Test
	public void testSimpleBootProject() throws Exception {
		File parentDir = new File("").getAbsoluteFile().getParentFile();
		File agentClassDir = new File(parentDir, "org.springframework.ide.service.agent/target/classes");
		URL agentClasspath = agentClassDir.toURI().toURL();
		
		CommandExecuter executer = new CommandExecuter(pipedInputStream, backchannel, projectRegistry, new URL[] {agentClasspath});
		executer.addCommand(new SetupProjectCommand());
		executer.run();

		String projectJAR = getProjectJAR("simple-spring-project", "0.0.1-SNAPSHOT");
		
		JSONObject setupMessage = new JSONObject();
		setupMessage.put("command-name", "setup-project");
		setupMessage.put("project-name", "TestProjectName");
		setupMessage.put("project-classpath", projectJAR);
		setupMessage.put("project-sourcepath", "");
		setupMessage.put("spring-config-files", "");
		
		writer.print(setupMessage.toString());
		writer.flush();
		
		Thread.sleep(TIMEOUT);
		
		JSONObject response = new JSONObject(jsonResponseParser);
		assertTrue(response.has("status"));
		assertEquals("project setup complete", response.get("status"));
		assertEquals("TestProjectName", response.get("project-name"));
		
		assertTrue(response.has("execution-time"));
		long time = response.getLong("execution-time");
		assertTrue(time >= 0);
		
		System.out.println("execution time: " + time);
		
		String error = resultErr.toString();
		assertTrue(error.length() == 0);
	}

	@Test
	public void testSimpleBootProjectBuildingModel() throws Exception {
		File parentDir = new File("").getAbsoluteFile().getParentFile();
		File agentClassDir = new File(parentDir, "org.springframework.ide.service.agent/target/classes");
		URL agentClasspath = agentClassDir.toURI().toURL();
		
		CommandExecuter executer = new CommandExecuter(pipedInputStream, backchannel, projectRegistry, new URL[] {agentClasspath});
		executer.addCommand(new SetupProjectCommand());
		executer.addCommand(new GetFullModelCommand());
		executer.run();

		String projectJAR = getProjectJAR("simple-spring-project", "0.0.1-SNAPSHOT");
		
		JSONObject setupMessage = new JSONObject();
		setupMessage.put("command-name", "setup-project");
		setupMessage.put("project-name", "TestProjectName");
		setupMessage.put("project-classpath", projectJAR);
		setupMessage.put("project-sourcepath", "");
		setupMessage.put("spring-config-files", "java:com.example.SimpleSpringProjectApplication");
		
		writer.print(setupMessage.toString());
		writer.flush();
		
		Thread.sleep(TIMEOUT);

		JSONObject getModelMessage = new JSONObject();
		getModelMessage.put("command-name", "get-full-model");
		getModelMessage.put("project-name", "TestProjectName");
		getModelMessage.put("spring-config-file", "java:com.example.SimpleSpringProjectApplication");
		
		writer.print(getModelMessage.toString());
		writer.flush();
		
		Thread.sleep(TIMEOUT);
		
		JSONObject setupresponse = new JSONObject(jsonResponseParser);
		assertTrue(setupresponse.has("status"));
		assertEquals("project setup complete", setupresponse.get("status"));
		assertEquals("TestProjectName", setupresponse.get("project-name"));
		
		JSONObject modelresponse = new JSONObject(jsonResponseParser);
		assertEquals("TestProjectName", modelresponse.get("project-name"));
		assertTrue(modelresponse.has("spring-model"));
		JSONObject model = (JSONObject) modelresponse.get("spring-model");
		assertEquals("TestProjectName", model.get("project-name"));
		assertTrue(model.has("model-root"));
		
		String error = resultErr.toString();
		assertTrue(error.length() == 0);
	}

	private String getProjectJAR(String projectName, String buildID) {
		URL projectJAR = this.getClass().getClassLoader().getResource("projects/" + projectName + "/target/" + projectName + "-" + buildID + ".jar");
		return projectJAR.toString();
	}
}
