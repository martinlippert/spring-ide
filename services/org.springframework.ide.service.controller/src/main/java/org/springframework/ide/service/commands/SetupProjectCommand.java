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
import org.springframework.ide.service.internal.Project;
import org.springframework.ide.service.internal.ProjectRegistry;
import org.springframework.ide.service.internal.ProjectSetup;

/**
 * @author Martin Lippert
 */
public class SetupProjectCommand implements Command {
	
	@Override
	public String getName() {
		return "setup-project";
	}

	@Override
	public void run(JSONObject command, ProjectRegistry projectRegistry, URL[] agentClasspath, BackChannel backchannel) {
		long startTime = System.currentTimeMillis();
		
		String projectName = command.getString("project-name");
		String classpath = command.getString("project-classpath");
		String sourcepath = command.getString("project-sourcepath");
		String springConfigFiles = command.getString("spring-config-files");
		boolean forceUpdate = command.getBoolean("force-update");
		
		if (forceUpdate && projectRegistry.has(projectName)) {
			ProjectSetup projectSetup = projectRegistry.get(projectName);
			projectSetup.close();
			projectRegistry.remove(projectName);
		}
		
		if (!projectRegistry.has(projectName)) {
			Project project = new Project(projectName, classpath, sourcepath, springConfigFiles, agentClasspath);
			ProjectSetup projectSetup = new ProjectSetup(project, backchannel);
			
			try {
				projectRegistry.add(projectSetup);
				projectSetup.start();
				projectSetup.createModel();
				
				long endTime = System.currentTimeMillis();
			
				JSONObject response = new JSONObject();
				response.put("status", "project setup complete");
				response.put("project-name", projectName);
				response.put("execution-time", (endTime - startTime));
				backchannel.sendMessage(response.toString());
			}
			catch (Exception e) {
				long endTime = System.currentTimeMillis();
				
				e.printStackTrace();

				JSONObject response = new JSONObject();
				response.put("status", "project setup failed with exception: " + e.toString());
				response.put("project-name", projectName);
				response.put("execution-time", (endTime - startTime));
				backchannel.sendMessage(response.toString());
				backchannel.sendException(e);
			}
		}
		else {
			long endTime = System.currentTimeMillis();

			JSONObject response = new JSONObject();
			response.put("status", "project already exists");
			response.put("project-name", projectName);
			response.put("execution-time", (endTime - startTime));
			backchannel.sendMessage(response.toString());
		}
	}

}
