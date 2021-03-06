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

import org.cloudfoundry.client.lib.CloudFoundryException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;

public class CloudErrors {

	/**
	 * check if access token error
	 *
	 * @param e
	 * @return true if access token error. False otherwise
	 */
	public static boolean isAccessTokenError(Exception e) {
		return hasCloudError(e, "access_denied") || hasCloudError(e, "Error requesting access token")
				|| (hasCloudError(e, "access") && hasCloudError(e, "token"));
	}

	public static boolean isBadRequest(Exception e) {
		return hasCloudError(e, "400");
	}

	/**
	 * check 404 error. For example, application does not exist
	 *
	 * @param t
	 * @return true if 404 error. False otherwise
	 */
	public static boolean isNotFoundException(Exception e) {
		return hasCloudError(e, "404");
	}

	/**
	 * check 503 service error.
	 *
	 * @param t
	 * @return true if 404 error. False otherwise
	 */
	public static boolean is503Error(Exception e) {
		return hasCloudError(e, "503");
	}

	public static boolean hasCloudError(Exception e, String error) {

		CloudFoundryException cloudError = getCloudFoundryError(e);

		if (cloudError != null) {
			return hasCloudError(cloudError, error);
		} else {
			return hasError(e, error);
		}
	}

	protected static CloudFoundryException getCloudFoundryError(Throwable t) {
		if (t == null) {
			return null;
		}
		CloudFoundryException cloudException = null;
		if (t instanceof CloudFoundryException) {
			cloudException = (CloudFoundryException) t;
		} else {
			Throwable cause = t.getCause();
			if (cause instanceof CloudFoundryException) {
				cloudException = (CloudFoundryException) cause;
			}
		}
		return cloudException;
	}

	protected static boolean isHostTaken(Exception e) {
		if (isBadRequest(e)) {
			CloudFoundryException cfe = (CloudFoundryException) e;
			return hasCloudError(cfe, "host") && hasCloudError(cfe, "taken");
		}
		return false;
	}

	public static void checkAndRethrowCloudException(Exception e, String errorPrefix) throws Exception {
		// Special case for CF exceptions:
		// CF exceptions may not contain the error in the message but rather
		// the description
		if (e instanceof CloudFoundryException) {
			String message = null;
			if (isHostTaken(e)) {
				message = "Another URL is required: the host is already taken by another existing application. Please change the URL, and restart or redeploy the application.";
			} else {
				message = resolveCloudError((CloudFoundryException) e);
			}
			if (errorPrefix != null) {
				message = errorPrefix + ": " + message;
			}
			IStatus status = BootDashActivator.createErrorStatus(e, message);
			throw new CoreException(status);
		} else {
			throw e;
		}
	}

	protected static boolean hasCloudError(CloudFoundryException e, String pattern) {
		String message = e.getDescription();
		if (message != null && message.contains(pattern)) {
			return true;
		}
		return hasError(e, pattern);
	}

	protected static boolean hasError(Exception exception, String pattern) {
		String message = exception.getMessage();
		return message != null && message.contains(pattern);
	}

	protected static String getCloudErrorMessage(CloudFoundryException e) {
		String message = e.getDescription();
		if (message == null || message.trim().length() == 0) {
			message = e.getMessage();
		}
		return message;
	}

	protected static String resolveCloudError(CloudFoundryException e) {
		String message = getCloudErrorMessage(e);
		if (message == null || message.trim().length() == 0) {
			message = "Cloud operation failure of type: " + e.getClass().getName();
		}
		return message;
	}

}
