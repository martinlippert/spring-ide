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
import org.springframework.ide.service.internal.ProjectSetup;

/**
 * @author Martin Lippert
 */
public class UpdateProjectSources implements Command {

	@Override
	public String getName() {
		return "update-project";
	}
	
	@Override
	public void run(JSONObject command, ProjectRegistry projectRegistry, URL[] agentClasspath, BackChannel backchannel) {
		long startTime = System.currentTimeMillis();
		
		String projectName = command.getString("project-name");
		
		if (projectRegistry.has(projectName)) {
			ProjectSetup projectSetup = projectRegistry.get(projectName);

			try {
				projectSetup.update();
				projectSetup.createModel();

				long endTime = System.currentTimeMillis();

				JSONObject response = new JSONObject();
				response.put("status", "project update complete");
				response.put("project-name", projectName);
				response.put("execution-time", (endTime - startTime));
				backchannel.sendMessage(response.toString());
			}
			catch (Exception e) {
				long endTime = System.currentTimeMillis();
				
				e.printStackTrace();

				JSONObject response = new JSONObject();
				response.put("status", "project update failed with exception: " + e.toString());
				response.put("project-name", projectName);
				response.put("execution-time", (endTime - startTime));
				backchannel.sendMessage(response.toString());
				backchannel.sendException(e);
			}
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
