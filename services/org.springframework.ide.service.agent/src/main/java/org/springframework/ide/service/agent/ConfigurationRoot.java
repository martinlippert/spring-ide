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
public interface ConfigurationRoot {
	
	static String JAVA_CONFIG_ROOT_PREFIX = "java:";
	static String XML_CONFIG_ROOT_PREFIX = "xml:";

	void createModel();

}
