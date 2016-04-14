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
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.URL;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.springframework.ide.service.commands.PingCommand;
import org.springframework.ide.service.controller.BackChannel;
import org.springframework.ide.service.controller.CommandExecuter;
import org.springframework.ide.service.internal.ProjectRegistry;

/**
 * @author Martin Lippert
 */
public class TestSimplePingCommand {

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
	public void testSimpleEmptyProjectSetup() throws Exception {
		CommandExecuter executer = new CommandExecuter(pipedInputStream, backchannel, projectRegistry, new URL[0]);
		executer.addCommand(new PingCommand());
		executer.run();
		
		writer.println("{\"command-name\" : \"ping\"}");
		writer.flush();
		
		Thread.sleep(1000);
		
		JSONObject response = new JSONObject(resultOut.toString());
		assertTrue(response.has("pong"));
		assertEquals("pong", response.get("pong"));
	}

}
