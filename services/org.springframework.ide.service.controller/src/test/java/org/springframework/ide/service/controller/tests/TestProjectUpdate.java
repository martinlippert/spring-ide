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
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.Before;
import org.junit.Test;
import org.springframework.ide.service.commands.SetupProjectCommand;
import org.springframework.ide.service.commands.UpdateProjectSources;
import org.springframework.ide.service.controller.BackChannel;
import org.springframework.ide.service.controller.CommandExecuter;
import org.springframework.ide.service.internal.ProjectRegistry;

/**
 * @author Martin Lippert
 */
public class TestProjectUpdate {
	
	/**
	 * response timeout in ms
	 */
	private static final int MESSAGE_TIMEOUT = 5000;
	private static final int ERROR_TIMEOUT = 100;

	private ByteArrayOutputStream resultErr;
	private BackChannel backchannel;
	private ProjectRegistry projectRegistry;
	
	private PrintWriter writer;
	private JSONTokener jsonResponseParser;

	private PipedInputStream pipedInputStream;
	private PipedOutputStream pipedOutputStream;

	private ExecutorService executor;

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
		
		executor = Executors.newFixedThreadPool(5);
	}

	@Test
	public void testUpdateSimpleBootProject() throws Exception {
		File parentDir = new File("").getAbsoluteFile().getParentFile();
		File agentClassDir = new File(parentDir, "org.springframework.ide.service.agent/target/classes");
		URL agentClasspath = agentClassDir.toURI().toURL();
		
		CommandExecuter executer = new CommandExecuter(pipedInputStream, backchannel, projectRegistry, new URL[] {agentClasspath});
		executer.addCommand(new SetupProjectCommand());
		executer.addCommand(new UpdateProjectSources());
		executer.run();

		String projectJAR = getProjectJAR("simple-spring-project");
		String projectClasspath = getProjectClasspath("simple-spring-project");
		
		JSONObject setupMessage = new JSONObject();
		setupMessage.put("command-name", "setup-project");
		setupMessage.put("project-name", "TestProjectName");
		setupMessage.put("project-classpath", projectClasspath);
		setupMessage.put("project-sourcepath", projectJAR);
		setupMessage.put("spring-config-files", "java:com.example.SimpleSpringProjectApplication");
		setupMessage.put("force-update", true);
		
		writer.print(setupMessage.toString());
		writer.flush();
		
		JSONObject response = waitForMessage(jsonResponseParser);
		assertTrue(response.has("status"));
		assertEquals("project setup complete", response.get("status"));
		assertEquals("TestProjectName", response.get("project-name"));
		
		assertTrue(response.has("execution-time"));
		long time = response.getLong("execution-time");
		assertTrue(time >= 0);
		
		JSONObject updateMessage = new JSONObject();
		updateMessage.put("command-name", "update-project");
		updateMessage.put("project-name", "TestProjectName");

		writer.print(updateMessage.toString());
		writer.flush();
		
		response = waitForMessage(jsonResponseParser);
		assertTrue(response.has("status"));
		assertEquals("project update complete", response.get("status"));
		assertEquals("TestProjectName", response.get("project-name"));
		
		try {
			String errorMessage = waitForError(resultErr);
			fail("error appreaded: " + errorMessage);
		}
		catch (TimeoutException e) {
		}
	}

	private String getProjectJAR(String projectName) {
		URL projectJAR = this.getClass().getClassLoader().getResource("projects/" + projectName + "/target/classes/");
		return projectJAR.toString();
	}
	
	private String getProjectClasspath(String projectName) throws Exception {
		StringBuilder result = new StringBuilder();

		URL projectLibs = this.getClass().getClassLoader().getResource("projects/" + projectName + "/target/libs");
		
		File libDir = new File(projectLibs.toURI());
		File[] libs = libDir.listFiles();
		for (int i = 0; i < libs.length; i++) {
			result.append(libs[i].toURI().toURL().toString());
			if (i < libs.length - 1) {
				result.append(";");
			}
		}

		return result.toString();
	}
	
	protected JSONObject waitForMessage(final JSONTokener parser) throws Exception {
		Future<JSONObject> task = executor.submit(new Callable<JSONObject>() {
			@Override
			public JSONObject call() throws Exception {
				return new JSONObject(parser);
			}
		});
		return task.get(MESSAGE_TIMEOUT, TimeUnit.MILLISECONDS);
	}

	protected String waitForError(final ByteArrayOutputStream errorStream) throws Exception {
		Future<String> task = executor.submit(new Callable<String>() {
			@Override
			public String call() throws Exception {
				String error = null;
				while (true) {
					error = errorStream.toString();
					if (error != null && error.length() > 0) {
						break;
					}
					Thread.sleep(50);
				}
				return error;
			}
		});
		return task.get(ERROR_TIMEOUT, TimeUnit.MILLISECONDS);
	}

}
