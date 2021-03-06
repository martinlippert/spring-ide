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
package org.springframework.ide.eclipse.boot.dash.test.mocks;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.mockito.Mockito;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFApplication;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFOrganization;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFService;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

public class MockCFSpace extends CFSpaceData {

	//TODO: the methods in this class should prolly be synchronized somehow. It manipulates mutable
	//  data and is called from multiple threads.

	private Map<String, CFService> servicesByName = new HashMap<>();
	private Map<String, MockCFApplication> appsByName = new HashMap<>();
	private MockCloudFoundryClientFactory owner;

	public MockCFSpace(MockCloudFoundryClientFactory owner, String name, UUID guid, CFOrganization org) {
		super(name, guid, org);
		this.owner = owner;
	}

	public List<CFService> getServices() {
		return ImmutableList.copyOf(servicesByName.values());
	}

	public ImmutableList<CFApplication> getApplicationsWithBasicInfo() {
		Builder<CFApplication> builder = ImmutableList.builder();
		for (MockCFApplication app : appsByName.values()) {
			builder.add(app.getBasicInfo());
		}
		return builder.build();
	}

	public MockCFApplication defApp(String name) {
		MockCFApplication existing = appsByName.get(name);
		if (existing==null) {
			appsByName.put(name, existing = Mockito.spy(new MockCFApplication(owner, name)));
		}
		return existing;
	}

	public CFService defService(String name) {
		CFService existing = servicesByName.get(name);
		if (existing==null) {
			servicesByName.put(name, new CFServiceData(
					name
			));
		}
		return existing;
	}

	public MockCFApplication getApplication(UUID appGuid) {
		for (MockCFApplication app : appsByName.values()) {
			if (app.getGuid().equals(appGuid)) {
				return app;
			}
		}
		return null;
	}

	public MockCFApplication getApplication(String appName) {
		MockCFApplication app = appsByName.get(appName);
		if (app!=null) {
			return app;
		}
		return null;
	}


	public boolean removeApp(String name) {
		return appsByName.remove(name)!=null;
	}

	public boolean add(MockCFApplication app) {
		String name = app.getName();
		if (appsByName.get(name)==null) {
			appsByName.put(name, app);
			return true;
		}
		return false;
	}

}
