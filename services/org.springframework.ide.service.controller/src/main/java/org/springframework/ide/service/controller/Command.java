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

import org.json.JSONObject;
import org.springframework.ide.service.internal.ProjectRegistry;

/**
 * @author Martin Lippert
 */
public interface Command {

	String getName();
	void run(JSONObject command, ProjectRegistry projectRegistry, URL[] agentClasspath, BackChannel backchannel);

}
