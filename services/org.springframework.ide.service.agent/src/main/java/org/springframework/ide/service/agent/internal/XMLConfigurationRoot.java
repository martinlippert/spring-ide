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

import org.springframework.ide.service.agent.BackChannel;
import org.springframework.ide.service.agent.ConfigurationRoot;

/**
 * @author Martin Lippert
 */
public class XMLConfigurationRoot implements ConfigurationRoot {

	private final String configElement;
	private final BackChannel backChannel;
	private final String projectName;

	public XMLConfigurationRoot(String configElement, String projectName, BackChannel backChannel) {
		this.configElement = configElement;
		this.projectName = projectName;
		this.backChannel = backChannel;
	}

	@Override
	public void createModel() {
	}

}
