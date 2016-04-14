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
import org.junit.Before;
import org.junit.Test;
import org.springframework.ide.service.commands.SetupProjectCommand;
import org.springframework.ide.service.controller.BackChannel;
import org.springframework.ide.service.controller.CommandExecuter;
import org.springframework.ide.service.internal.ProjectRegistry;

/**
 * @author Martin Lippert
 */
public class TestProjectSetup {
	
	private ByteArrayOutputStream resultOut;
	private ByteArrayOutputStream resultErr;
	private BackChannel backchannel;
	private ProjectRegistry projectRegistry;
	private PrintWriter writer;
	private PipedInputStream pipedInputStream;

	@Before
	public void setup() throws Exception {
		resultOut = new ByteArrayOutputStream();
		resultErr = new ByteArrayOutputStream();
		
		backchannel = new BackChannel(new PrintStream(resultOut), new PrintStream(resultErr));
		projectRegistry = new ProjectRegistry();

		PipedOutputStream pipe = new PipedOutputStream();
		pipedInputStream = new PipedInputStream(pipe);		
		writer = new PrintWriter(pipe, true);
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
		
		writer.println(setupMessage.toString());
		writer.flush();
		
		Thread.sleep(1000);
		
		JSONObject response = new JSONObject(resultOut.toString());
		assertTrue(response.has("status"));
		assertEquals("project setup failed with exception: java.lang.ClassNotFoundException: org.springframework.ide.service.agent.AgentMain", response.get("status"));
		assertEquals("TestProjectName", response.get("project-name"));
		
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
		
		writer.println(setupMessage.toString());
		writer.flush();
		
		Thread.sleep(1000);
		
		JSONObject response = new JSONObject(resultOut.toString());
		assertTrue(response.has("status"));
		assertEquals("project setup complete", response.get("status"));
		assertEquals("TestProjectName", response.get("project-name"));
		
		String error = resultErr.toString();
		assertTrue(error.length() == 0);
	}

}
