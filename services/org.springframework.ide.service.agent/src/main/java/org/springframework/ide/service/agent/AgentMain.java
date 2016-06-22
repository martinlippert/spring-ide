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

/**
 * @author Martin Lippert
 */
public class AgentMain {

	public static BackChannel backchannel;
	public static ModelCreator modelCreator;
	
	public static void main(String projectName, String[] configFiles, Object backChannel) {
		backchannel = new BackChannel(backChannel);
		modelCreator = new ModelCreator(projectName, configFiles, backchannel);
	}
	
	public static void createModel() {
		modelCreator.createModel();
	}
	
	public static String[] getBeanNames(String typeHint) {
		return modelCreator.getBeanNames(typeHint);
	}

}
