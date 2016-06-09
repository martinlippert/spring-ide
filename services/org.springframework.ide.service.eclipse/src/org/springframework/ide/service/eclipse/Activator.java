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
package org.springframework.ide.service.eclipse;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.springframework.ide.service.eclipse.process.ServiceProcessManager;

public class Activator extends AbstractUIPlugin {

	public static final String PLUGIN_ID = "org.springframework.ide.service.eclipse"; //$NON-NLS-1$
	private static Activator plugin;
	
	private final ServiceProcessManager serviceProcessManager;
	private final ServiceManager serviceManager;
	
	public Activator() {
		this.serviceProcessManager = new ServiceProcessManager();
		this.serviceManager = new ServiceManager(this.serviceProcessManager);
	}

	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	public static Activator getDefault() {
		return plugin;
	}

	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}
	
	public ServiceManager getServiceManager() {
		return this.serviceManager;
	}
	
	public ServiceProcessManager getServiceProcessManager() {
		return this.serviceProcessManager;
	}

}
