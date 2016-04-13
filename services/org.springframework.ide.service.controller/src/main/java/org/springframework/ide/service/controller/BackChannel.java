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
package org.springframework.ide.service.controller;

import java.io.PrintStream;

/**
 * @author Martin Lippert
 */
public class BackChannel {
	
	private final PrintStream out;
	private final PrintStream err;

	/**
	 * @param out The output stream to write general messages to
	 * @param err The error stream to write back errors
	 */
	public BackChannel(PrintStream out, PrintStream err) {
		this.out = out;
		this.err = err;
	}
	
	/**
	 * send back a general message through the general channel
	 * 
	 * @param error The message as a JSON formatted string
	 */
	public void sendMessage(String message) {
		this.out.println(message);
		this.out.flush();
	}
	
	/**
	 * send back an error through the error channel
	 * 
	 * @param error The error as general String
	 */
	public void sendError(String error) {
		this.err.println(error);
		this.err.flush();
	}

	/**
	 * send back an exception through the error channel
	 * 
	 * @param error The exception to send
	 */
	public void sendException(Exception exception) {
		this.err.println(exception.getMessage());
		exception.printStackTrace(this.err);
		this.err.flush();
	}
}
