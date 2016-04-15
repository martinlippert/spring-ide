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
package org.springframework.ide.service.internal;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * @author Martin Lippert
 */
public class Project {

	private final String projectName;
	private final URL[] classpath;
	private final URL[] sourcepath;
	private final String[] springConfigFiles;
	private final URL[] agentClasspath;

	/**
	 * Constructs a new value object for a project with the given configuration (parses the path parameters)
	 * 
	 * @param projectName The name of the project
	 * @param classpath The classpath containing the libs as a single string using ; as a delimiter
	 * @param sourcepath A sequence of paths for compiled classes as a single string, using ; as the delimiter
	 * @param springConfigFiles A sequence of paths that point to spring config files that should be taken into account
	 * @param agentClasspath the classpath of the spring tooling agent
	 */
	public Project(String projectName, String classpath, String sourcepath, String springConfigFiles, URL[] agentClasspath) {
		this.projectName = projectName;
		this.agentClasspath = agentClasspath;
		this.classpath = parseURLs(classpath);
		this.sourcepath = parseURLs(sourcepath);
		this.springConfigFiles = parseStrings(springConfigFiles);
	}
	
	public String getProjectName() {
		return projectName;
	}
	
	public URL[] getClasspath() {
		return classpath;
	}
	
	public URL[] getSourcepath() {
		return sourcepath;
	}
	
	public String[] getSpringConfigFiles() {
		return springConfigFiles;
	}

	public URL[] getAgentClasspath() {
		return agentClasspath;
	}

	private URL[] parseURLs(String classpath) {
		StringTokenizer tokens =  new StringTokenizer(classpath, ";");
		List<URL> urls = new ArrayList<URL>();
		while (tokens.hasMoreTokens()) {
			String nextToken = tokens.nextToken();
			try {
				URL url = new URL(nextToken);
				urls.add(url);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
		return (URL[]) urls.toArray(new URL[urls.size()]);
	}

	private String[] parseStrings(String stringSequence) {
		StringTokenizer tokens =  new StringTokenizer(stringSequence, ";");
		List<String> result = new ArrayList<>();
		while (tokens.hasMoreTokens()) {
			String nextToken = tokens.nextToken();
			result.add(nextToken);
		}
		return (String[]) result.toArray(new String[result.size()]);
	}

}
