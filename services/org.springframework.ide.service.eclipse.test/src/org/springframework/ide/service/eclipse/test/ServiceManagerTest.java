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
package org.springframework.ide.service.eclipse.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.ide.service.eclipse.Activator;
import org.springframework.ide.service.eclipse.process.IServiceManager;
import org.springframework.ide.service.eclipse.process.ServiceConfiguration;
import org.springframework.ide.service.eclipse.process.ServiceProcess;

/**
 * @author Martin Lippert
 */
public class ServiceManagerTest {
	
	private IProject project;


	@Before
	public void createSimpleEmptyProject() throws Exception {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		project = root.getProject("sampleProject");
		project.create(null);
	}
	
	@After
	public void destroyProject() throws Exception {
		project.delete(true, null);
	}

	@Test
	public void testNoExternalServiceProcess() throws Exception {
		IServiceManager manager = Activator.getDefault().getServiceManager();
		
		ServiceConfiguration serviceConfig = new ServiceConfiguration(project, "root");
		assertFalse(manager.isServiceRunning(serviceConfig));
	}
	
	@Test
	public void testStartStopExternalProcess() throws Exception {
		IServiceManager manager = Activator.getDefault().getServiceManager();
		
		ServiceConfiguration serviceConfig = new ServiceConfiguration(project, "root");
		manager.startService(serviceConfig);
		assertTrue(manager.isServiceRunning(serviceConfig));
		ServiceProcess serviceProcess = manager.getServiceProcess(serviceConfig);

		assertNotNull(serviceProcess);
		assertTrue(serviceProcess.isAlive());
		
		manager.stopService(serviceConfig);
		assertFalse(manager.isServiceRunning(serviceConfig));
		assertFalse(serviceProcess.isAlive());
	}

}
