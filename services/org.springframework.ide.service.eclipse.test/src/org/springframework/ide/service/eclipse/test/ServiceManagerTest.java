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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.ide.service.eclipse.Activator;
import org.springframework.ide.service.eclipse.config.ServiceProcessConfiguration;
import org.springframework.ide.service.eclipse.process.EnvironmentConfiguration;
import org.springframework.ide.service.eclipse.process.MessageListener;
import org.springframework.ide.service.eclipse.process.ServiceManager;
import org.springframework.ide.service.eclipse.process.ServiceProcess;

/**
 * @author Martin Lippert
 */
public class ServiceManagerTest {
	
	private IProject project;
	private IVMInstall jdk;


	@Before
	public void createSimpleEmptyProject() throws Exception {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		project = root.getProject("sampleProject");
		project.create(null);
		
		jdk = getJDK();
	}
	
	@After
	public void destroyProject() throws Exception {
		project.delete(true, null);
	}

	@Test
	public void testNoExternalServiceProcess() throws Exception {
		ServiceManager manager = Activator.getDefault().getServiceManager();
		
		ServiceProcessConfiguration processConfig = new ServiceProcessConfiguration(jdk, "randomargs", new EnvironmentConfiguration());
		assertFalse(manager.isServiceProcessRunning(processConfig));
	}
	
	@Test
	public void testStartStopExternalProcess() throws Exception {
		ServiceManager manager = Activator.getDefault().getServiceManager();
		
		ServiceProcessConfiguration processConfig = new ServiceProcessConfiguration(jdk, "randomargs", new EnvironmentConfiguration());
		manager.startServiceProcess(processConfig);
		assertTrue(manager.isServiceProcessRunning(processConfig));
		ServiceProcess serviceProcess = manager.getServiceProcess(processConfig);

		assertNotNull(serviceProcess);
		assertTrue(serviceProcess.isAlive());
		
		manager.stopServiceProcess(processConfig);
		assertFalse(manager.isServiceProcessRunning(processConfig));
		
		serviceProcess.getInternalProcess().waitFor(500, TimeUnit.MILLISECONDS);
		assertFalse(serviceProcess.isAlive());
	}

	@Test
	public void testSendPingToExternalProcess() throws Exception {
		ServiceManager manager = Activator.getDefault().getServiceManager();
		
		ServiceProcessConfiguration processConfig = new ServiceProcessConfiguration(jdk, "randomargs", new EnvironmentConfiguration());
		manager.startServiceProcess(processConfig);
		assertTrue(manager.isServiceProcessRunning(processConfig));
		ServiceProcess serviceProcess = manager.getServiceProcess(processConfig);
		
		final CountDownLatch latch = new CountDownLatch(1);
		final List<JSONObject> responses = new ArrayList<>();
		
		serviceProcess.addMessageListener(new MessageListener() {
			@Override
			public void messageReceived(JSONObject message) {
				responses.add(message);
				latch.countDown();
			}
		});
		
		JSONObject pingMessage = new JSONObject();
		pingMessage.put("command-name", "ping");
		serviceProcess.sendMessage(pingMessage);
		
		latch.await(300, TimeUnit.MILLISECONDS);
		
		assertEquals(1, responses.size());
		JSONObject response = responses.get(0);
		assertNotNull(response);
		assertTrue(response.has("pong"));
		assertEquals("pong", response.getString("pong"));

		manager.stopServiceProcess(processConfig);
	}

	private IVMInstall getJDK() {
		return JavaRuntime.getDefaultVMInstall();
	}
	
}
