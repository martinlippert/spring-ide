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
package org.springframework.ide.service.eclipse.process;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.core.resources.IPathVariableManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.json.JSONObject;
import org.springframework.ide.service.eclipse.config.ServiceConfiguration;

/**
 * This is the main Spring Tooling API for the external Spring tooling as a service.
 * 
 * @author Martin Lippert
 */
public class ToolingService {

	private static final String FILE_SCHEME = "file";

	private final ServiceProcess serviceProcess;
	private final ServiceConfiguration serviceConfiguration;
	private final MessageListener messageListener;
	
	private Map<String, IRequestCallback> waitingRequests;
	
	private boolean initialized;
	private boolean updateClasspath;

	public ToolingService(ServiceConfiguration serviceConfiguration, ServiceProcess serviceProcess) {
		this.serviceConfiguration = serviceConfiguration;
		this.serviceProcess = serviceProcess;
		this.waitingRequests = new ConcurrentHashMap<>();
		
		this.messageListener = new MessageListener() {
			@Override
			public void messageReceived(JSONObject message) {
				System.out.println("tooling message received: " + message.toString());
				
				if (message.has("requestID")) {
					String requestID = message.optString("requestID");
					IRequestCallback callback = waitingRequests.get(requestID);
					if (callback != null) {
						waitingRequests.remove(requestID);
						callback.responseReceived(message);
					}
				}
			}
		};
		
		this.serviceProcess.addMessageListener(messageListener);
		this.initialized = false;
		this.updateClasspath = false;
	}
	
	public void close() {
		this.serviceProcess.removeMessageListener(messageListener);
	}
	
	public void triggerFullBuild() throws Exception {
		setup(true);
	}
	
	public void triggerIncrementalBuild(IResourceDelta delta) throws Exception {
		if (!this.initialized || this.updateClasspath) {
			setup(true);
		}
		else {
			triggerUpdate();
		}
	}
	
	public void requestBeanNames(String typeHint, IRequestCallback callback) throws Exception {
		String requestID = UUID.randomUUID().toString();
		
		JSONObject requestMessage = new JSONObject();
		requestMessage.put("command-name", "get-spring-bean-names");
		requestMessage.put("project-name", serviceConfiguration.getProject().getName());
		requestMessage.put("requestID", requestID);
		requestMessage.put("type-hint", typeHint);
		
		this.waitingRequests.put(requestID, callback);
	}

	private void triggerUpdate() throws Exception {
		JSONObject setupMessage = new JSONObject();
		setupMessage.put("command-name", "update-project");
		setupMessage.put("project-name", serviceConfiguration.getProject().getName());
		setupMessage.put("project-sourcepath", getSourcePath(serviceConfiguration.getProject()));
		setupMessage.put("spring-config-files", serviceConfiguration.getConfigRoot());
		
		this.serviceProcess.sendMessage(setupMessage);
	}

	public void setup(boolean forceUpdate) throws Exception {
		synchronized(this) {
			JSONObject setupMessage = new JSONObject();
			setupMessage.put("command-name", "setup-project");
			setupMessage.put("project-name", serviceConfiguration.getProject().getName());
			setupMessage.put("project-classpath", getProjectClasspath(serviceConfiguration.getProject()));
			setupMessage.put("project-sourcepath", getSourcePath(serviceConfiguration.getProject()));
			setupMessage.put("spring-config-files", serviceConfiguration.getConfigRoot());
			setupMessage.put("force-update", forceUpdate);
			
			this.serviceProcess.sendMessage(setupMessage);
			this.initialized = true;
			this.updateClasspath = false;
		}
	}

	public void setClasspathChanged(IProject project) {
		if (this.serviceConfiguration.getProject().equals(project)) {
			this.updateClasspath = true;
		}
	}

	private String getSourcePath(IProject project) {
		StringBuilder sourcePath = new StringBuilder();
		
		IJavaProject jp = JavaCore.create(project);

		try {
			IClasspathEntry[] classpath = jp.getResolvedClasspath(true);
			
			for (int i = 0; i < classpath.length; i++) {
				IClasspathEntry path = classpath[i];
				if (path.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
					IPath sourceOutputPath = path.getOutputLocation();
					try {
						URL url = covertPathToUrl(project, sourceOutputPath);
						if (url != null) {
							if (sourcePath.length() > 0) {
								sourcePath.append(";");
							}
							sourcePath.append(url.toString());
						}
					} catch (MalformedURLException e) {
						e.printStackTrace();
					}
				}
			}
			
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		
		return sourcePath.toString();
	}

	private String getProjectClasspath(IProject project) {
		StringBuilder result = new StringBuilder();
		
		IJavaProject jp = JavaCore.create(project);
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

		try {
			IClasspathEntry[] classpath = jp.getResolvedClasspath(true);
			
			for (int i = 0; i < classpath.length; i++) {
				IClasspathEntry path = classpath[i];
				if (path.getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
					IPath entryPath = path.getPath();
					File file = entryPath.toFile();
					
					try {
						URL url;
						if (file.exists()) {
							url = file.toURI().toURL();
						}
						else {
							// case for project relative links
							String projectName = entryPath.segment(0);
							IProject pathProject = root.getProject(projectName);
							url = covertPathToUrl(pathProject, entryPath);
						}

						if (url != null) {
							if (result.length() > 0) {
								result.append(";");
							}
							result.append(url.toString());
						}
					}
					catch (MalformedURLException e) {
						e.printStackTrace();
					}
				}
			}
			
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		
		return result.toString();
	}

	private static URL covertPathToUrl(IProject project, IPath path) throws MalformedURLException {
		if (path != null && project != null && path.removeFirstSegments(1) != null
				&& project.findMember(path.removeFirstSegments(1)) != null) {

			URI uri = project.findMember(path.removeFirstSegments(1)).getRawLocationURI();

			if (uri != null) {
				String scheme = uri.getScheme();
				if (FILE_SCHEME.equalsIgnoreCase(scheme)) {
					return toURL(uri);
				}
				else {
					IPathVariableManager variableManager = ResourcesPlugin.getWorkspace().getPathVariableManager();
					return toURL(variableManager.resolveURI(uri));
				}
			}
		}
		return null;
	}

	private static URL toURL(URI uri) throws MalformedURLException {
		File file = new File(uri);
		if (file.isDirectory()) {
			return new URL(uri.toString() + File.separator);
		}
		else {
			return uri.toURL();
		}
	}

}
