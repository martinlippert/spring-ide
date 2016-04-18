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

import org.springframework.boot.SpringApplication;
import org.springframework.ide.service.agent.BackChannel;
import org.springframework.ide.service.agent.ConfigurationRoot;

/**
 * @author Martin Lippert
 */
public class JavaConfigurationRoot implements ConfigurationRoot {

	private final String configElement;
	private final BackChannel backChannel;
	private final String projectName;

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
				Class<?> rootClass = this.getClass().getClassLoader().loadClass(rootClassName);
				SpringApplication application = new SpringApplication(rootClass);
				application.setApplicationContextClass(ModelBuildingContextClass.class);
				application.run(new String[] {});
			} catch (ClassNotFoundException e) {
				backChannel.sendException(e);
			}
		}
	}
	
}
