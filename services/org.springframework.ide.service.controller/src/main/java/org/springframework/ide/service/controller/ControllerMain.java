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
package org.springframework.ide.service.controller;

import java.io.File;
import java.io.PrintStream;
import java.net.URL;

import org.json.JSONObject;
import org.springframework.ide.service.commands.GetFullModelCommand;
import org.springframework.ide.service.commands.PingCommand;
import org.springframework.ide.service.commands.SetupProjectCommand;
import org.springframework.ide.service.internal.ProjectRegistry;

/**
 * @author Martin Lippert
 */
public class ControllerMain {
	
	public static void main(String[] args) throws Exception {
		String name = args[0];
		
		ProjectRegistry projectRegistry = new ProjectRegistry();
		BackChannel backChannel = new BackChannel(System.out, System.err);
		
		File tempOutFile = File.createTempFile(name + "-stdout", "log");
		File tempErrFile = File.createTempFile(name + "-errout", "log");

		PrintStream outFile = new PrintStream(tempOutFile);
		PrintStream errFile = new PrintStream(tempErrFile);
		
		System.setOut(outFile);
		System.setErr(errFile);
		
		JSONObject logFileMessage = new JSONObject();
		logFileMessage.put("stdout", tempOutFile.getAbsolutePath());
		logFileMessage.put("errout", tempErrFile.getAbsolutePath());
		backChannel.sendMessage(logFileMessage.toString());
		
		CommandExecuter commandExecuter = new CommandExecuter(System.in, backChannel, projectRegistry, getAgentClasspath());
		commandExecuter.addCommand(new PingCommand());
		commandExecuter.addCommand(new SetupProjectCommand());
		commandExecuter.addCommand(new GetFullModelCommand());
		
		commandExecuter.run();
	}

	public static URL[] getAgentClasspath() throws Exception {
		URL url = ControllerMain.class.getClassLoader().getResource("");
		File binFolder = new File(url.toURI());
		File agentClassesFolder = new File(binFolder.getParentFile().getParentFile().getParentFile(), "org.springframework.ide.service.agent/target/classes");
		return new URL[] {agentClassesFolder.toURI().toURL()};
	}
	
}
