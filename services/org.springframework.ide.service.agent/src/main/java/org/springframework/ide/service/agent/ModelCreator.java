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
package org.springframework.ide.service.agent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.ide.service.agent.internal.JavaConfigurationRoot;
import org.springframework.ide.service.agent.internal.ModelBuildingApplicationContext;
import org.springframework.ide.service.agent.internal.XMLConfigurationRoot;

/**
 * @author Martin Lippert
 */
public class ModelCreator {

	private ConfigurationRoot[] configRoots;

	public ModelCreator(String projectName, String[] configFiles, BackChannel backChannel) {
		List<ConfigurationRoot> roots = new ArrayList<>();
		for (int i = 0; i < configFiles.length; i++) {
			if (configFiles[i] != null && configFiles[i].startsWith(ConfigurationRoot.JAVA_CONFIG_ROOT_PREFIX)) {
				roots.add(new JavaConfigurationRoot(configFiles[i], projectName, backChannel));
			}
			else if (configFiles[i] != null && configFiles[i].startsWith(ConfigurationRoot.XML_CONFIG_ROOT_PREFIX)) {
				roots.add(new XMLConfigurationRoot(configFiles[i], projectName, backChannel));
			}
		}
		
		this.configRoots = (ConfigurationRoot[]) roots.toArray(new ConfigurationRoot[roots.size()]);
	}
	
	public void createModel() {
		for (int i = 0; i < configRoots.length; i++) {
			configRoots[i].createModel();
		}
	}

	/**
	 * @param typeHint fully qualified type for beans to match this type or null, if no hint is available
	 */
	public String[] getBeanNames(String typeHint) {
		Set<String> result = new LinkedHashSet<>();
		
		for (int i = 0; i < configRoots.length; i++) {
			ModelBuildingApplicationContext appContext = configRoots[i].getAppContext();
			
			Class<?> typeHintClass = null;
			try {
				typeHintClass = this.getClass().getClassLoader().loadClass(typeHint);
			}
			catch (Exception e) {
			}

			String[] beanNames = appContext.getBeanNamesForType(typeHintClass);
			result.addAll(Arrays.asList(beanNames));
		}

		return (String[]) result.toArray(new String[result.size()]);
	}

}
