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
package org.springframework.ide.service.commands;

import java.net.URL;

import org.json.JSONObject;
import org.springframework.ide.service.controller.BackChannel;
import org.springframework.ide.service.controller.Command;
import org.springframework.ide.service.internal.ProjectRegistry;

/**
 * @author Martin Lippert
 */
public class GetFullModelCommand implements Command {
	
	@Override
	public String getName() {
		return "get-full-model";
	}

	@Override
	public void run(JSONObject command, ProjectRegistry projectRegistry, URL[] agentClasspath, BackChannel backchannel) {
		long startTime = System.currentTimeMillis();
		
		String projectName = (String) command.get("project-name");
		
		if (projectRegistry.has(projectName)) {
			// TODO: get the model as json document
			JSONObject model = new JSONObject();
			model.put("project-name", projectName);
			model.put("model-root", "this is the root of all evil");
			
			long endTime = System.currentTimeMillis();

			JSONObject response = new JSONObject();
			response.put("project-name", projectName);
			response.put("spring-model", model);
			response.put("execution-time", (endTime - startTime));
			backchannel.sendMessage(response.toString());
		}
		else {
			long endTime = System.currentTimeMillis();

			JSONObject response = new JSONObject();
			response.put("status", "project doesn't exist");
			response.put("project-name", projectName);
			response.put("execution-time", (endTime - startTime));
			backchannel.sendMessage(response.toString());
		}
	}

}
