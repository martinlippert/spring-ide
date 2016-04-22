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
package org.springframework.ide.service.agent.internal;

import org.springframework.boot.logging.LoggingSystem;
import org.springframework.ide.service.agent.BackChannel;
import org.springframework.ide.service.agent.ConfigurationRoot;

/**
 * @author Martin Lippert
 */
public class JavaConfigurationRoot implements ConfigurationRoot {

	private final String configElement;
	private final BackChannel backChannel;
	private final String projectName;

	private ModelBuildingApplicationContext context;

	public JavaConfigurationRoot(String configElement, String projectName, BackChannel backChannel) {
		this.configElement = configElement;
		this.projectName = projectName;
		this.backChannel = backChannel;
	}

	@Override
	public void createModel() {
		String rootClassName = configElement.substring(ConfigurationRoot.JAVA_CONFIG_ROOT_PREFIX.length());
		if (rootClassName.length() > 0) {
			try {
				setupModelCreation(rootClassName);
			} catch (Exception e) {
				backChannel.sendException(e);
			}
		}
	}

	protected void setupModelCreation(String rootClassName) throws Exception {
		ClassLoader classLoader = this.getClass().getClassLoader();
		
		System.setProperty("logging.level.ROOT", "OFF");
		LoggingSystem loggingSystem = LoggingSystem.get(classLoader);
		loggingSystem.beforeInitialize();
		
		// register root bean
		Class<?> rootClass = classLoader.loadClass(rootClassName);

		// create special app context
		context = new ModelBuildingApplicationContext();
		context.setClassLoader(classLoader);
		context.register(rootClass);
		context.refresh();
		
		// this.registry = new DefaultListableBeanFactory();
		// AnnotatedBeanDefinitionReader definitionReader = new
		// AnnotatedBeanDefinitionReader(this.registry);
		// definitionReader.register(rootClass);
	}

}
