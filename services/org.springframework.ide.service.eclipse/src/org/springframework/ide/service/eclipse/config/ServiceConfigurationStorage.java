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
package org.springframework.ide.service.eclipse.config;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.ide.service.eclipse.process.EnvironmentConfiguration;

/**
 * @author Martin Lippert
 */
public class ServiceConfigurationStorage {
	
	private static final String SERVICE_CONFIG_FILE = "tools-service-config.json";

	public static ServiceConfiguration[] readConfigs(IProject project) {
		try {
			IResource serviceConfigfile = project.findMember(SERVICE_CONFIG_FILE);
			if (serviceConfigfile.exists() && serviceConfigfile.getType() == IResource.FILE) {
				IFile configFile = (IFile) serviceConfigfile;
					return readConfigs(configFile.getContents());
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static ServiceConfiguration[] readConfigs(InputStream configFile) {
		List<ServiceConfiguration> result = new ArrayList<>();
		
		JSONTokener tokener = new JSONTokener(new InputStreamReader(configFile));
		
		try {
			
			JSONObject rootJSON = new JSONObject(tokener);

			String projectName = rootJSON.optString("project-name");
			if (projectName != null && projectName.length() > 0) {
				IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
				if (project.exists()) {
				
					JSONArray configJSON = rootJSON.getJSONArray("service-configs");
					int configCount = configJSON.length();
					
					for (int i = 0; i < configCount; i++) {
						JSONObject serviceConfigJSON = (JSONObject) configJSON.getJSONObject(i);
						ServiceConfiguration serviceConfig = readConfig(project, serviceConfigJSON);
						if (serviceConfig != null) {
							result.add(serviceConfig);
						}
					}
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return (ServiceConfiguration[]) result.toArray(new ServiceConfiguration[result.size()]);
	}
	
	protected static ServiceConfiguration readConfig(IProject project, JSONObject serviceConfigJSON) throws JSONException {
		String configRoot = serviceConfigJSON.optString("config-root");
		ServiceProcessConfiguration processConfig = readProcessConfig(serviceConfigJSON.getJSONObject("process-config"));
		
		return new ServiceConfiguration(project, configRoot, processConfig);
	}

	protected static ServiceProcessConfiguration readProcessConfig(JSONObject jsonObject) {
		IVMInstall jdk = JavaRuntime.getDefaultVMInstall();
		String vmargs = "";
		EnvironmentConfiguration environment = new EnvironmentConfiguration();

		return new ServiceProcessConfiguration(jdk, vmargs, environment);
	}

}
