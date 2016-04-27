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

import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.ide.service.internal.ProjectRegistry;

/**
 * @author Martin Lippert
 */
public class CommandExecuter {

	private final InputStream input;
	private final ExecutorService executerService;
	private final ConcurrentMap<String, Command> commands;
	private final BackChannel backchannel;
	private final ProjectRegistry projectRegistry;
	private final URL[] agentClasspath;

	/**
	 * @param projectRegistry 
	 * @param inputStream stream with incoming commands and messages
	 */
	public CommandExecuter(InputStream input, BackChannel backchannel, ProjectRegistry projectRegistry, URL[] agentClasspath) {
		this.input = input;
		this.backchannel = backchannel;
		this.projectRegistry = projectRegistry;
		this.agentClasspath = agentClasspath;
		
		this.executerService = Executors.newFixedThreadPool(10);
		this.commands = new ConcurrentHashMap<>();
	}
	
	public void addCommand(Command serviceCommand) {
		this.commands.put(serviceCommand.getName(), serviceCommand);
	}

	/**
	 * start the central command executer
	 */
	public void run() {
		final JSONTokener commandParser = new JSONTokener(this.input);

		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					try {
						JSONObject message = new JSONObject(commandParser);
						String commandName = (String) message.get("command-name");
						Command command = commands.get(commandName);

						if (command != null) {
							executeCommand(command, message);
						}
						else {
							JSONObject errorMessage = new JSONObject();
							errorMessage.put("status", "command unknown: " + commandName);
							backchannel.sendError(errorMessage.toString());
						}
					} catch (JSONException e) {
						JSONObject errorMessage = new JSONObject();
						errorMessage.put("status", "error parsing JSON message: " + e.toString());
						backchannel.sendError(errorMessage.toString());
						backchannel.sendException(e);
					}
				}
			}
		});
		thread.setDaemon(false);
		thread.start();
	}
	
	private void executeCommand(final Command command, final JSONObject message) {
		executerService.execute(new Runnable() {
			@Override
			public void run() {
				command.run(message, projectRegistry, agentClasspath, backchannel);
			}
		});
	}


}
