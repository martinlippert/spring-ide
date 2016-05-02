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
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.jdt.internal.launching.StandardVMType;
import org.eclipse.jdt.launching.IVMInstall;
import org.springframework.ide.service.eclipse.config.ServiceProcessConfiguration;

/**
 * @author Martin Lippert
 */
@SuppressWarnings("restriction")
public class ServiceProcessFactory {
	
	/**
	 * create a new service process for the given service configuration
	 */
	public static ServiceProcess createServiceProcess(ServiceProcessConfiguration processConfig) throws Exception {
		List<String> commands = new ArrayList<>();
		addJavaExecutableCommand(processConfig, commands);
		
//		commands.add("-Xdebug");
//		commands.add("-Xrunjdwp:server=y,transport=dt_socket,address=4000,suspend=n");
		
		addServiceControllerClasspath(processConfig, commands);
		String[] environment = getEnvironmentSettings(processConfig);

		Process process = Runtime.getRuntime().exec(commands.toArray(new String[0]), environment);
		return new ServiceProcess(process, processConfig);
	}

	private static void addJavaExecutableCommand(ServiceProcessConfiguration processConfig, List<String> commands) {
		IVMInstall jdk = processConfig.getJdkConfiguration();
		File javaExecutable = StandardVMType.findJavaExecutable(jdk.getInstallLocation());
		commands.add(javaExecutable.getAbsolutePath());
	}
 	
	private static void addServiceControllerClasspath(ServiceProcessConfiguration processConfig, List<String> commands) {
		URL resource = ServiceProcessFactory.class.getClassLoader().getResource("");
		try {
			URL resolve = FileLocator.toFileURL(resource);
			File binFolder = new File(resolve.toURI());
			File targetFolder = new File(binFolder.getParentFile().getParentFile(), "org.springframework.ide.service.controller/target");

			// add controller classpath
			commands.add("-cp");

			StringBuilder classpath = new StringBuilder();
			File classesFolder = new File(targetFolder, "classes");
			classpath.append(classesFolder.getAbsolutePath() + "/");

			File libs = new File(targetFolder, "libs");
			File[] files = libs.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files[i].getName().endsWith(".jar")) {
					classpath.append(File.pathSeparator);
					classpath.append(files[i].getAbsolutePath());
				}
			}
			commands.add(classpath.toString());

			// add controller main class
			commands.add("org.springframework.ide.service.controller.ControllerMain");

		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	private static String[] getEnvironmentSettings(ServiceProcessConfiguration processConfig) {
		// TODO: add environment variables
		return new String[0];
	}

}
