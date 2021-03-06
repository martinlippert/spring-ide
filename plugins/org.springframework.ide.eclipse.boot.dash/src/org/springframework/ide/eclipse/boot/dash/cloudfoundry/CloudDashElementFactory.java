/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.cloudfoundry;

import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFService;
import org.springframework.ide.eclipse.boot.dash.metadata.IPropertyStore;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModelContext;

public class CloudDashElementFactory {

	private final IPropertyStore modelStore;

	private final CloudFoundryBootDashModel model;

	public CloudDashElementFactory(BootDashModelContext context, IPropertyStore modelStore,
			CloudFoundryBootDashModel model) {
		this.modelStore = modelStore;
		this.model = model;
	}

	public CloudServiceDashElement createService(CFService service) {
		return new CloudServiceDashElement(model, service, modelStore);
	}
}
