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

import static org.junit.Assert.*;

import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.junit.Test;
import org.springframework.ide.service.eclipse.Activator;
import org.springframework.ide.service.eclipse.config.ServiceConfiguration;
import org.springframework.ide.service.eclipse.config.ServiceConfigurationStorage;
import org.springframework.ide.service.eclipse.config.ServiceProcessConfiguration;

/**
 * @author Martin Lippert
 */
public class ServiceConfigurationStorageTest {

	@Test
	public void testEmptyFile() {
		InputStream stream = this.getClass().getResourceAsStream("emptyfile.serviceconfig");
		ServiceConfiguration[] configs = ServiceConfigurationStorage.readConfigs(stream);
		
		assertNotNull(configs);
		assertEquals(0, configs.length);
	}
	
	@Test
	public void testNonJSONFile() {
		InputStream stream = this.getClass().getResourceAsStream("nonjsonfile.serviceconfig");
		ServiceConfiguration[] configs = ServiceConfigurationStorage.readConfigs(stream);
		
		assertNotNull(configs);
		assertEquals(0, configs.length);
	}

	@Test
	public void testEmptyJSONFile() {
		InputStream stream = this.getClass().getResourceAsStream("emptyjsonfile.serviceconfig");
		ServiceConfiguration[] configs = ServiceConfigurationStorage.readConfigs(stream);
		
		assertNotNull(configs);
		assertEquals(0, configs.length);
	}
	
	@Test
	public void testSimpleServiceConfig() throws Exception {
		IProject project = null; 
		try {
			project = ResourcesPlugin.getWorkspace().getRoot().getProject("testproject");
			project.create(null);
			
			InputStream stream = this.getClass().getResourceAsStream("simple-testproject.serviceconfig");
			ServiceConfiguration[] configs = ServiceConfigurationStorage.readConfigs(stream);
			
			assertNotNull(configs);
			assertEquals(1, configs.length);
			assertEquals(project, configs[0].getProject());
			assertEquals("testconfigroot", configs[0].getConfigRoot());
			
			IVMInstall vm = JavaRuntime.getDefaultVMInstall();
			
			ServiceProcessConfiguration processConfig = configs[0].getProcessConfig();
			assertEquals(vm, processConfig.getJdkConfiguration());
		}
		finally {
			project.delete(true,  null);
		}
	}

	@Test
	public void testMultipleServiceConfig() throws Exception {
		IProject project = null; 
		try {
			project = ResourcesPlugin.getWorkspace().getRoot().getProject("testproject");
			project.create(null);
			
			InputStream stream = this.getClass().getResourceAsStream("multiple-testproject.serviceconfig");
			ServiceConfiguration[] configs = ServiceConfigurationStorage.readConfigs(stream);
			
			assertNotNull(configs);
			assertEquals(3, configs.length);

			assertEquals(project, configs[0].getProject());
			assertEquals(project, configs[1].getProject());
			assertEquals(project, configs[2].getProject());

			assertEquals("testconfigroot1", configs[0].getConfigRoot());
			assertEquals("testconfigroot2", configs[1].getConfigRoot());
			assertEquals("testconfigroot3", configs[2].getConfigRoot());
			
			IVMInstall vm = JavaRuntime.getDefaultVMInstall();
			
			ServiceProcessConfiguration processConfig1 = configs[0].getProcessConfig();
			ServiceProcessConfiguration processConfig2 = configs[1].getProcessConfig();
			ServiceProcessConfiguration processConfig3 = configs[2].getProcessConfig();

			assertEquals(vm, processConfig1.getJdkConfiguration());
			assertEquals(vm, processConfig2.getJdkConfiguration());
			assertEquals(vm, processConfig3.getJdkConfiguration());
		}
		finally {
			project.delete(true,  null);
		}
	}
	
	@Test
	public void testReadRealProjectConfig() throws Exception {
		IProject project = null;
		try {
			project = ResourcesPlugin.getWorkspace().getRoot().getProject("RealTestProject");
			project.create(null);
			project.open(null);
			
			IFile configFile = project.getFile(".serviceconfig");
			InputStream source = this.getClass().getResourceAsStream("real-project.serviceconfig");
			configFile.create(source, true, null);
			
			ServiceConfiguration[] configs = ServiceConfigurationStorage.readConfigs(project);
			
			assertNotNull(configs);
			assertEquals(3, configs.length);

			assertEquals(project, configs[0].getProject());
			assertEquals(project, configs[1].getProject());
			assertEquals(project, configs[2].getProject());

			assertEquals("testconfigroot1", configs[0].getConfigRoot());
			assertEquals("testconfigroot2", configs[1].getConfigRoot());
			assertEquals("testconfigroot3", configs[2].getConfigRoot());
			
			IVMInstall vm = JavaRuntime.getDefaultVMInstall();
			
			ServiceProcessConfiguration processConfig1 = configs[0].getProcessConfig();
			ServiceProcessConfiguration processConfig2 = configs[1].getProcessConfig();
			ServiceProcessConfiguration processConfig3 = configs[2].getProcessConfig();

			assertEquals(vm, processConfig1.getJdkConfiguration());
			assertEquals(vm, processConfig2.getJdkConfiguration());
			assertEquals(vm, processConfig3.getJdkConfiguration());
		}
		finally {
			project.delete(true, null);
		}
		
	}

}
