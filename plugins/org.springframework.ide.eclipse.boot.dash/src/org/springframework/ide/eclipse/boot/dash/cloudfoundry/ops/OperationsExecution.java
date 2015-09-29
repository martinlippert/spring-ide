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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.model.Operation;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;
import org.springsource.ide.eclipse.commons.frameworks.core.async.Promise;
import org.springsource.ide.eclipse.commons.frameworks.core.async.Deferred;
import org.springsource.ide.eclipse.commons.frameworks.core.async.Promises;

public class OperationsExecution {

	//TODO: does this belong in here?
	// Better to chain up error handling somehow using the returned 'Promise'.
	private final UserInteractions ui;
	private Promises promises = Promises.getDefault();

	public OperationsExecution(UserInteractions ui) {
		this.ui = ui;
	}

	public <T> Promise<T> runOpAsynch(final Operation<T> op) {
		final Deferred<T> done = promises.create();
		System.out.println("runAsynch: "+op.getName());
		Job job = new Job(op.getName()) {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					T value = op.run(monitor);
					System.out.println("runAsynch: "+op.getName()+" OK: "+value);
					done.resolve(value);
				} catch (Exception e) {
					System.out.println("runAsynch: "+op.getName()+" THROWS: "+e);
					done.reject(e);
					if (!(e instanceof OperationCanceledException)) {
						if (ui != null) {
							String message = e.getMessage() != null && e.getMessage().trim().length() > 0
									? e.getMessage()
									: "Unknown error of type: " + e.getClass().getName()
											+ ". Check Error Log view for further details.";
							ui.errorPopup("Operation failure: ", message);

						}
						BootDashActivator.log(e);
					}
				}
				// Only return OK status to avoid a second error dialogue
				// appearing, which is opened by Eclipse when a job returns
				// error status.
				return Status.OK_STATUS;
			}

		};

		ISchedulingRule rule = op.getSchedulingRule();
		if (op != null) {
			job.setRule(rule);
		}

		job.setPriority(Job.INTERACTIVE);
		job.schedule();
		return done;
	}
}
