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
package org.springframework.ide.service.eclipse.process;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * @author Martin Lippert
 */
public class ServiceProcess {

	private final Process serviceProcess;
	private final ServiceProcessConfiguration processConfiguration;
	
	private final BlockingQueue<JSONObject> sendQueue;
	private final Thread sendThread;
	private final PrintWriter processWriter;
	
	private final Thread receiveThread;
	private final InputStream processInput;
	private final List<MessageListener> messageListeners;
	private final InputStream processError;
	private final Thread errorThread;

	public ServiceProcess(Process serviceProcess, ServiceProcessConfiguration processConfiguration) {
		this.serviceProcess = serviceProcess;
		this.processConfiguration = processConfiguration;
		
		this.messageListeners = new CopyOnWriteArrayList<>();
		
		this.processInput = serviceProcess.getInputStream();
		this.receiveThread = new Thread(new Runnable() {
			@Override
			public void run() {
				final JSONTokener messageParser = new JSONTokener(new InputStreamReader(processInput));
				boolean streamClosed = false;
				while (!streamClosed) {
					try {
						JSONObject message = new JSONObject(messageParser);
						messageReceived(message);
					} catch (JSONException e) {
						if (e.getCause() instanceof IOException && e.getCause().getMessage().equals("Stream closed")) {
							streamClosed = true;
						}
						else {
							e.printStackTrace();
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});
		this.receiveThread.start();
		
		this.processError = serviceProcess.getErrorStream();
		this.errorThread = new Thread(new Runnable() {
			@Override
			public void run() {
				final JSONTokener messageParser = new JSONTokener(new InputStreamReader(processError));
				boolean streamClosed = false;
				while (!streamClosed) {
					try {
						JSONObject message = new JSONObject(messageParser);
						messageReceived(message);
					} catch (JSONException e) {
						if (e.getCause() instanceof IOException && e.getCause().getMessage().equals("Stream closed")) {
							streamClosed = true;
						}
						else {
							e.printStackTrace();
							// TODO: wrong JSON message received
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});
		this.errorThread.start();

		this.processWriter = new PrintWriter(serviceProcess.getOutputStream());
		this.sendQueue = new ArrayBlockingQueue<>(100);
		this.sendThread = new Thread(
				new Runnable() {
					@Override
					public void run() {
					     try {
					         while (true) {
					        	 	JSONObject message = sendQueue.take();
					        	 	processWriter.write(message.toString());
					        	 	processWriter.flush();
					        	 }
					     }
					     catch (InterruptedException ex) {
					     }
					}
				});
		this.sendThread.start();
	}
	
	public ServiceProcessConfiguration getProcessConfiguration() {
		return processConfiguration;
	}

	public boolean isAlive() {
		return this.serviceProcess != null && this.serviceProcess.isAlive();
	}

	public void kill() {
		if (this.serviceProcess != null) {
			this.receiveThread.interrupt();
			this.errorThread.interrupt();
			this.sendThread.interrupt();
			this.serviceProcess.destroyForcibly();
		}
	}

	public Process getInternalProcess() {
		return this.serviceProcess;
	}
	
	public boolean sendMessage(JSONObject message) throws Exception {
		return this.sendQueue.offer(message);
	}

	public void addMessageListener(MessageListener messageListener) {
		this.messageListeners.add(messageListener);
	}
	
	public void removeMessageListener(MessageListener messageListener) {
		this.messageListeners.remove(messageListener);
	}
	
	protected void messageReceived(JSONObject message) {
		this.messageListeners.forEach((listener) -> listener.messageReceived(message));
	}

}
