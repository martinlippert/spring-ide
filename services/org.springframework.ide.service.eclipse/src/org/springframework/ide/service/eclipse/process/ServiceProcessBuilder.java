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

/**
 * @author Martin Lippert
 */
public class ServiceProcessBuilder {
	
	/**
	 * create a new service process for the given service configuration
	 */
	public static ServiceProcess createServiceProcess(ServiceConfiguration serviceConfig) {
		return new ServiceProcess(null);
	}
 	
}
