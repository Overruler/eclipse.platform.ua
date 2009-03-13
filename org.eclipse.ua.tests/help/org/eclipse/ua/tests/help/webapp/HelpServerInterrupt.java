/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ua.tests.help.webapp;

import java.io.InputStream;
import java.net.URL;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.eclipse.help.internal.server.WebappManager;

/**
 * Test to see if the help server is interruptable
 */

public class HelpServerInterrupt extends TestCase {
	
	private static boolean enableTimeout = true;
	private int iterations;
	private int sleepTime = 10; 
	private class ServerStarter extends Thread {
		
		private Exception exception = null;

		public synchronized void run() {
			try {
				WebappManager.start("help");
			} catch (Exception e) {
				exception = e;
			}
		}

		public Exception getException() {
			return exception;
		}
	}

	protected void setUp() throws Exception {
		super.setUp();
	}
	
	public void testServer() throws Exception {
		WebappManager.stop("help");
		startServerWithInterrupt();
		checkServer();
		WebappManager.stop("help");
		System.out.println("Iteration Count = " + iterations);
	}	
	
	private void startServerWithInterrupt() throws Exception {
		ServerStarter starter = new ServerStarter();
		starter.start();
		// Now wait for the thread to complete
		iterations = 0;
		do {
			iterations++;
			if (enableTimeout && sleepTime * iterations > 10000) {
				fail("Test did not complete within 10 seconds");
			}
			starter.interrupt();			
			Thread.sleep(sleepTime);			
        } while (starter.isAlive());
		Exception exception = starter.getException();
		if (exception != null) {
			throw exception;
		}
	}
	
	private void checkServer() throws Exception {
		int port = WebappManager.getPort();
		URL url = new URL("http", "localhost", port, "/help/index.jsp");
		InputStream input = url.openStream();
		int firstbyte = input.read();
		Assert.assertTrue(firstbyte > 0);
		input.close();
	}
	
	public void testServerMultiple() throws Exception {
		for (sleepTime = 10; sleepTime < 500; sleepTime = sleepTime + 10) {
		    testServer();
		}
	}
		
}