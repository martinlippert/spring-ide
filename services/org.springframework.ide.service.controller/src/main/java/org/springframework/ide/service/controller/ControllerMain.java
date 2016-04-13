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
package org.springframework.ide.service.controller;

import java.net.URL;

import org.springframework.ide.service.internal.ProjectRegistry;

/**
 * @author Martin Lippert
 */
public class ControllerMain {
	
	public static void main(String[] args) {
		ProjectRegistry projectRegistry = new ProjectRegistry();
		BackChannel backChannel = new BackChannel(System.out, System.err);
		new CommandExecuter(System.in, backChannel, projectRegistry, getAgentClasspath()).run();
	}

	public static URL[] getAgentClasspath() {
		return new URL[] {};
	}

}
