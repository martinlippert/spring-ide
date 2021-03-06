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

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipFile;

import org.cloudfoundry.client.lib.ApplicationLogListener;
import org.cloudfoundry.client.lib.StreamingLogToken;
import org.cloudfoundry.client.lib.domain.ApplicationStats;
import org.cloudfoundry.client.lib.domain.Staging;
import org.osgi.framework.Version;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudAppInstances;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFApplication;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFApplicationArchive;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFApplicationStats;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFBuildpack;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFClientParams;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFCloudDomain;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFOrganization;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFService;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFSpace;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFStack;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.ClientRequests;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CloudFoundryClientFactory;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.deployment.CloudApplicationDeploymentProperties;
import org.springsource.ide.eclipse.commons.cloudfoundry.client.diego.SshClientSupport;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

public class MockCloudFoundryClientFactory extends CloudFoundryClientFactory {

	private Map<String, CFOrganization> orgsByName = new LinkedHashMap<>();
	private Map<String, MockCFSpace> spacesByName = new LinkedHashMap<>();
	private Map<String, MockCFDomain> domainsByName = new LinkedHashMap<>();
	private Map<String, MockCFBuildpack> buildpacksByName = new LinkedHashMap<>();
	private Map<String, MockCFStack> stacksByName = new LinkedHashMap<>();

	/**
	 * Becomes non-null if notImplementedStub is called, used to check that the tests
	 * only use parts of the mocking harness that are actually implemented.
	 */
	private Exception notImplementedStubCalled = null;
	private long startDelay = 0;

	public MockCloudFoundryClientFactory() {
		defDomain("cfmockapps.io"); //Lost of functionality may assume there's at least one domain so make sure we have one.
		defBuildpacks("java-buildpack", "ruby-buildpack", "funky-buildpack", "another-buildpack");
		defStacks("cflinuxfs2", "windows2012R2");
	}

	public void defStacks(String... names) {
		for (String n : names) {
			defStack(n);
		}
	}

	public MockCFStack defStack(String name) {
		MockCFStack stack = new MockCFStack(name);
		stacksByName.put(name, stack);
		return stack;
	}

	@Override
	public ClientRequests getClient(CFClientParams params) throws Exception {
		return new MockClient(params);
	}

	public MockCFDomain defDomain(String name) {
		MockCFDomain it = new MockCFDomain(name);
		domainsByName.put(name, it);
		return it;
	}

	public MockCFSpace defSpace(String orgName, String spaceName) {
		String key = orgName+"/"+spaceName;
		MockCFSpace existing = spacesByName.get(key);
		if (existing==null) {
			CFOrganization org = defOrg(orgName);
			spacesByName.put(key, existing= new MockCFSpace(this,
					spaceName,
					UUID.randomUUID(),
					org
			));
		}
		return existing;
	}

	public CFOrganization defOrg(String orgName) {
		CFOrganization existing = orgsByName.get(orgName);
		if (existing==null) {
			orgsByName.put(orgName, existing = new CFOrganizationData(
					orgName,
					UUID.randomUUID()
			));
		}
		return existing;
	}

	public void assertOnlyImplementedStubsCalled() throws Exception {
		if (notImplementedStubCalled!=null) {
			throw notImplementedStubCalled;
		}
	}

	private class MockClient implements ClientRequests {

		private CFClientParams params;
		private boolean connected = true;

		public MockClient(CFClientParams params) {
			this.params = params;
		}

		private void notImplementedStub() {
			IllegalStateException e = new IllegalStateException("CF Client Stub Not Yet Implemented");
			if (notImplementedStubCalled==null) {
				notImplementedStubCalled = e;
			}
			throw e;
		}

		@Override
		public Map<CFApplication, CFApplicationStats> waitForApplicationStats(List<CFApplication> appsToLookUp,
				long timeToWait) throws Exception {
			Builder<CFApplication, CFApplicationStats> builder = ImmutableMap.builder();
			for (CFApplication app : appsToLookUp) {
				builder.put(app, getSpace().getApplication(app.getGuid()).getStats());
			}
			return builder.build();
		}

		@Override
		public void uploadApplication(String appName, ZipFile archive) throws Exception {
			MockCFApplication app = getSpace().getApplication(appName);
			if (app==null) {
				throw errorAppNotFound(appName);
			}
			//TODO: we just ignore the data. If/when we have tests that wanna do something with this may not be
			// good enough anymore.
		}

		@Override
		public void updateApplicationUris(String appName, List<String> urls) throws Exception {
			MockCFApplication app = getSpace().getApplication(appName);
			if (app==null) {
				throw errorAppNotFound(appName);
			}
			app.setUris(urls);
		}

		@Override
		public void updateApplicationStaging(String appName, Staging staging) throws Exception {
			notImplementedStub();
		}

		@Override
		public void updateApplicationServices(String appName, List<String> services) throws Exception {
			notImplementedStub();
		}

		@Override
		public void updateApplicationMemory(String appName, int memory) throws Exception {
			notImplementedStub();

		}

		@Override
		public void updateApplicationInstances(String appName, int instances) throws Exception {
			notImplementedStub();
		}

		@Override
		public void updateApplicationEnvironment(String appName, Map<String, String> environmentVariables)
				throws Exception {
			MockCFApplication app = getSpace().getApplication(appName);
			if (app==null) {
				throw errorAppNotFound(appName);
			}
			app.setEnv(environmentVariables);
		}

		@Override
		public StreamingLogToken streamLogs(String appName, ApplicationLogListener logConsole) {
			//TODO: This 'log streamer' is a total dummy for now. It doesn't stream any data and canceling it does nothing.
			return new StreamingLogToken() {
				@Override
				public void cancel() {
				}
			};
		}

		@Override
		public void stopApplication(String appName) throws Exception {
			MockCFApplication app = getSpace().getApplication(appName);
			if (app==null) {
				throw errorAppNotFound(appName);
			}
			app.stop();
		}

		@Override
		public void restartApplication(String appName) throws Exception {
			MockCFApplication app = getSpace().getApplication(appName);
			if (app==null) {
				throw errorAppNotFound(appName);
			}
			app.restart();
		}

		@Override
		public void logout() {
			connected = false;
		}

		@Override
		public SshClientSupport getSshClientSupport() throws Exception {
			notImplementedStub();
			return null;
		}

		@SuppressWarnings("unchecked")
		@Override
		public List<CFSpace> getSpaces() throws Exception {
			checkConnected();
			@SuppressWarnings("rawtypes")
			List hack = ImmutableList.copyOf(spacesByName.values());
			return hack;
		}

		@Override
		public List<CFService> getServices() throws Exception {
			checkConnected();
			return getSpace().getServices();
		}

		private MockCFSpace getSpace() throws IOException {
			checkConnected();
			if (params.getOrgName()==null) {
				throw errorNoOrgSelected();
			}
			if (params.getSpaceName()==null) {
				throw errorNoSpaceSelected();
			}
			MockCFSpace space = spacesByName.get(params.getOrgName()+"/"+params.getSpaceName());
			if (space==null) {
				throw errorSpaceNotFound(params.getOrgName()+"/"+params.getSpaceName());
			}
			return space;
		}

		@Override
		public CloudAppInstances getExistingAppInstances(UUID guid) throws Exception {
			MockCFApplication app = getSpace().getApplication(guid);
			if (app!=null) {
				return new CloudAppInstances(app.getBasicInfo(), app.getStats());
			}
			return null;
		}

		@Override
		public CloudAppInstances getExistingAppInstances(String appName) throws Exception {
			MockCFApplication app = getSpace().getApplication(appName);
			if (app!=null) {
				return new CloudAppInstances(app.getBasicInfo(), app.getStats());
			}
			return null;
		}

		@Override
		public List<CFCloudDomain> getDomains() throws Exception {
			checkConnected();
			return ImmutableList.<CFCloudDomain>copyOf(domainsByName.values());
		}

		@Override
		public List<CFBuildpack> getBuildpacks() throws Exception {
			checkConnected();
			return ImmutableList.<CFBuildpack>copyOf(buildpacksByName.values());
		}

		@Override
		public List<CFApplication> getApplicationsWithBasicInfo() throws Exception {
			checkConnected();
			return getSpace().getApplicationsWithBasicInfo();
		}

		@Override
		public CFApplication getApplication(String appName) throws Exception {
			checkConnected();
			MockCFApplication app = getSpace().getApplication(appName);
			if (app!=null) {
				return app.getBasicInfo();
			}
			return null;
		}

		@Override
		public Version getApiVersion() {
			notImplementedStub();
			return null;
		}

		@Override
		public void deleteApplication(String name) throws Exception {
			if (!getSpace().removeApp(name)) {
				throw errorAppNotFound(name);
			}
		}

		@Override
		public void createApplication(CloudApplicationDeploymentProperties deploymentProperties) throws Exception {
			String appName = deploymentProperties.getAppName();
			CFApplication existing = getApplication(appName);
			if (existing!=null) {
				throw errorAppAlreadyExists(appName);
			}
			//TODO: should check that services exist (and pretend to bind them to that app).

			//Code in the 'real' client does this:
//			client.createApplication(deploymentProperties.getAppName(),
//					new Staging(deploymentProperties.getCommand(), deploymentProperties.getBuildpack(),
//							deploymentProperties.getStack(), deploymentProperties.getTimeout()),
//					deploymentProperties.getDiskQuota(),
//					deploymentProperties.getMemory(),
//					new ArrayList<>(deploymentProperties.getUris()),
//					deploymentProperties.getServices()
//			);

			MockCFSpace space = getSpace();
			MockCFApplication app = new MockCFApplication(MockCloudFoundryClientFactory.this, appName);
			app.setCommand(deploymentProperties.getCommand());
			app.setBuildpackUrl(deploymentProperties.getBuildpack());
			app.setStack(deploymentProperties.getStack());
			app.setTimeout(deploymentProperties.getTimeout());
			app.setDiskQuota(deploymentProperties.getDiskQuota());
			app.setMemory(deploymentProperties.getMemory());
			app.setUris(deploymentProperties.getUris());
			app.setServices(deploymentProperties.getServices());

			space.add(app);
		}

		@Override
		public String getHealthCheck(UUID appGuid) throws IOException {
			checkConnected();
			MockCFApplication app = getApplication(appGuid);
			if (app == null) {
				throw errorAppNotFound("GUID: "+appGuid.toString());
			} else {
				return app.getHealthCheck();
			}
		}

		private MockCFApplication getApplication(UUID appGuid) throws IOException {
			return getSpace().getApplication(appGuid);
		}

		private void checkConnected() throws IOException {
			if (!connected) {
				throw errorClientNotConnected();
			}
		}

		@Override
		public void setHealthCheck(UUID guid, String hcType) {
			notImplementedStub();
		}

		@Override
		public void updateApplicationDiskQuota(String appName, int diskQuota) throws Exception {
			notImplementedStub();
		}

		@Override
		public List<CFStack> getStacks() throws Exception {
			checkConnected();
			return ImmutableList.<CFStack>copyOf(stacksByName.values());
		}
	}

	public void defBuildpacks(String... names) {
		for (String n : names) {
			defBuildpack(n);
		}
	}

	public MockCFBuildpack defBuildpack(String n) {
		MockCFBuildpack it = new MockCFBuildpack(n);
		buildpacksByName.put(n, it);
		return it;
	}

	//////////////////////////////////////////////////
	// Exception creation methods

	protected IOException errorAppNotFound(String detailMessage) throws IOException {
		return new IOException("App not found: "+detailMessage);
	}

	protected IOException errorClientNotConnected() {
		return new IOException("CF Client not Connected");
	}

	protected IOException errorNoOrgSelected() {
		return new IOException("No org selected");
	}

	protected IOException errorNoSpaceSelected() {
		return new IOException("No space selected");
	}

	protected  IOException errorSpaceNotFound(String detail) {
		return new IOException("Space not found: "+detail);
	}

	protected IOException errorAppAlreadyExists(String detail) {
		return new IOException("App already exists: "+detail);
	}

	public void setAppStartDelay(TimeUnit timeUnit, int howMany) {
		startDelay = timeUnit.toMillis(howMany);
	}

	/**
	 * @return The delay that a simulated 'start' of an app should take before returning. Given in milliseconds.
	 */
	public long getStartDelay() {
		return startDelay;
	}

}
