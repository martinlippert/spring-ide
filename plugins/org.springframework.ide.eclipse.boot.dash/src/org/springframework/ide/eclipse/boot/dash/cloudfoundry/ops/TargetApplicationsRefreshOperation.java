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
package org.springframework.ide.eclipse.boot.dash.cloudfoundry.ops;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudfoundry.client.lib.domain.CloudApplication;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudAppInstances;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryBootDashModel;

/**
 * This performs a "two-tier" refresh as fetching list of
 * {@link CloudApplication} can be slow, especially if also fetching each app's
 * instances.
 * <p/>
 * This refresh operation only fetches a "basic" shallow list of
 * {@link CloudApplication}, which may be quicker to resolve, and notifies the
 * model when element changes occur.
 *
 * <p/>
 * It also launches a separate refresh job that may take longer to complete
 * which is fetching instances and app running state.
 *
 * @see AppInstancesRefreshOperation
 */
public final class TargetApplicationsRefreshOperation extends CloudOperation {

	public TargetApplicationsRefreshOperation(CloudFoundryBootDashModel model) {
		super("Refreshing list of Cloud applications for: " + model.getRunTarget().getName(), model);
	}

	@Override
	synchronized protected void doCloudOp(IProgressMonitor monitor) throws Exception, OperationCanceledException {
//		try {
//
			// 1. Fetch basic list of applications. Should be the "faster" of
			// the
			// two refresh operations

			List<CloudApplication> apps = requests.getApplicationsWithBasicInfo();

			Map<CloudAppInstances, IProject> updatedApplications = new HashMap<CloudAppInstances, IProject>();
			if (apps != null) {

				Map<String, String> existingProjectToAppMappings = this.model.getProjectToAppMappingStore()
						.getMapping();

				for (CloudApplication app : apps) {

					String projectName = existingProjectToAppMappings.get(app.getName());
					IProject project = null;
					if (projectName != null) {
						project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
						if (project == null || !project.isAccessible()) {
							project = null;
						}
					}

					// No stats available at this stage. Just set stats to null
					// for now.
					updatedApplications.put(new CloudAppInstances(app, null), project);
				}
			}

			this.model.updateElements(updatedApplications);

			// 2. Launch the slower app stats/instances refresh operation.
			this.model.getOperationsExecution().runOpAsynch(new AppInstancesRefreshOperation(this.model));

//		} catch (Exception e) {
//			BootDashActivator.log(e);
//		}
	}

	public ISchedulingRule getSchedulingRule() {
		return new RefreshSchedulingRule(model.getRunTarget());
	}
}