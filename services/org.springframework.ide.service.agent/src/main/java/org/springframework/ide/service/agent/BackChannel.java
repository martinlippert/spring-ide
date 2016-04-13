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
package org.springframework.ide.service.agent;

import java.lang.reflect.Method;

/**
 * @author Martin Lippert
 */
public class BackChannel {
	
	private Method sendMessageMethod;
	private Method sendErrorMethod;
	private Method sendExceptionMethod;
	
	private Object channel;

	public BackChannel(Object channel) {
		this.channel = channel;

		try {
			sendMessageMethod = channel.getClass().getMethod("sendMessage", String.class);
			sendErrorMethod = channel.getClass().getMethod("sendError", String.class);
			sendExceptionMethod = channel.getClass().getMethod("sendException", Exception.class);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * send back a general message through the general channel
	 * 
	 * @param error The message as a JSON formatted string
	 */
	public void sendMessage(String message) {
		try {
			sendMessageMethod.invoke(channel, message);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * send back an error through the error channel
	 * 
	 * @param error The error as general String
	 */
	public void sendError(String error) {
		try {
			sendErrorMethod.invoke(channel, error);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * send back an exception through the error channel
	 * 
	 * @param error The exception to send
	 */
	public void sendException(Exception exception) {
		try {
			sendExceptionMethod.invoke(channel, exception);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
