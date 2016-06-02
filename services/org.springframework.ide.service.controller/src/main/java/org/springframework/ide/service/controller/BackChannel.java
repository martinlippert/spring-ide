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
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * @author Martin Lippert
 */
public class BackChannel {
	
	private final PrintStream out;
	private final PrintStream err;
	
	private final BlockingQueue<String> sendQueue;
	private final Thread sendThread;
	
	private final BlockingQueue<Error> errorQueue;
	private final Thread errorThread;
	
	/**
	 * @param out The output stream to write general messages to
	 * @param err The error stream to write back errors
	 */
	public BackChannel(PrintStream out, PrintStream err) {
		this.out = out;
		this.err = err;
		
		this.sendQueue = new ArrayBlockingQueue<>(100);
		this.sendThread = new Thread(
				new Runnable() {
					@Override
					public void run() {
					     try {
					         while (true) {
					        	 	String message = sendQueue.take();
					        		BackChannel.this.out.print(message);
					        		BackChannel.this.out.flush();
					        	 }
					     }
					     catch (InterruptedException ex) {
					     }
					}
				});
		this.sendThread.start();

		this.errorQueue = new ArrayBlockingQueue<>(100);
		this.errorThread = new Thread(
				new Runnable() {
					@Override
					public void run() {
					     try {
					         while (true) {
					        	 	Error error = errorQueue.take();
					        	 	if (error.message != null) {
						        		BackChannel.this.err.print(error);
					        	 	}
					        	 	if (error.exception != null) {
					        	 		error.exception.printStackTrace(BackChannel.this.err);
					        	 	}
					        		BackChannel.this.err.flush();
					        	 }
					     }
					     catch (InterruptedException ex) {
					     }
					}
				});
		this.errorThread.start();

	}
	
	/**
	 * send back a general message through the general channel
	 * 
	 * @param error The message as a JSON formatted string
	 */
	public void sendMessage(String message) {
		this.sendQueue.offer(message);
	}
	
	/**
	 * send back an error through the error channel
	 * 
	 * @param errorMessage The error as general String
	 */
	public void sendError(String errorMessage) {
		Error error = new Error();
		error.message = errorMessage;
		
		this.errorQueue.offer(error);
	}

	/**
	 * send back an exception through the error channel
	 * 
	 * @param error The exception to send
	 */
	public void sendException(Exception exception) {
		Error error = new Error();
		error.message = exception.getMessage();
		error.exception = exception;
		
		errorQueue.offer(error);
	}
	
	public void close() {
		this.sendThread.interrupt();
		this.errorThread.interrupt();
	}
	
	/**
	 * internal class to capture errors (either messages or excetions or both)
	 * to queue them up for delivery
	 */
	protected static class Error {
		public String message;
		public Exception exception;
	}

}
