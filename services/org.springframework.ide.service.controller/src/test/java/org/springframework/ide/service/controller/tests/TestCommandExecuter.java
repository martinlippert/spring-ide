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

import java.io.ByteArrayOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.URL;

import org.junit.Test;
import org.springframework.ide.service.commands.PingCommand;
import org.springframework.ide.service.controller.BackChannel;
import org.springframework.ide.service.controller.CommandExecuter;
import org.springframework.ide.service.internal.ProjectRegistry;

/**
 * @author Martin Lippert
 */
public class TestCommandExecuter {

	@Test
	public void testBasicPingPongInteraction() throws Exception {
		ByteArrayOutputStream resultOut = new ByteArrayOutputStream();
		ByteArrayOutputStream resultErr = new ByteArrayOutputStream();
		
		BackChannel backchannel = new BackChannel(new PrintStream(resultOut), new PrintStream(resultErr));
		ProjectRegistry projectRegistry = new ProjectRegistry();

		PipedOutputStream pipe = new PipedOutputStream();
		PrintWriter writer = new PrintWriter(pipe, true);

		CommandExecuter executer = new CommandExecuter(new PipedInputStream(pipe), backchannel, projectRegistry, new URL[0]);
		executer.addCommand(new PingCommand());
		executer.run();
		
		writer.println("{\"command-name\" : \"ping\"}");
		writer.flush();
		
		Thread.sleep(1000);
		
		String pingResult = resultOut.toString();
		assertEquals("{'pong' : pong}\n", pingResult);
	}

}
