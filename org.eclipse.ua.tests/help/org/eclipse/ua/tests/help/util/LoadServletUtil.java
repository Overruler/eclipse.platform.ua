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

package org.eclipse.ua.tests.help.util;

import java.io.InputStream;
import java.net.URL;

import junit.framework.Assert;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.help.internal.server.WebappManager;

public class LoadServletUtil {
	
	private static long uniqueParam = System.currentTimeMillis();
	
	/**
	 * Start a server for use in the performance tests
	 * @throws Exception
	 */
	public static void startServer() throws Exception {
		WebappManager.start("help");
		int port = WebappManager.getPort();
		URL url = new URL("http", "localhost", port, "/help/index.jsp");
		InputStream input = url.openStream();
		int firstbyte = input.read();
		Assert.assertTrue(firstbyte > 0);
		input.close();
	}
	
	/**
	 * Stop the server created by startServer()
	 * @throws CoreException
	 */
	public static void stopServer() throws CoreException {
		WebappManager.stop("help");
	}
	
	/**
	 * Read an html page generated by a servlet
	 * @param paragraphs the number of paragraphs in the page
	 * @throws Exception
	 */
	public static void readLoadServlet(int paragraphs) throws Exception {
		int port = WebappManager.getPort();
		// Use a unique parameter to defeat caching
		++uniqueParam;
		URL url = new URL("http", "localhost", port, 
				"/help/loadtest?value=" + uniqueParam + "&repeat=" + paragraphs);
		InputStream input = url.openStream();
		int nextChar;
		long value = 0;
		// The loadtest servlet  returns the uniqueParam in an opening comment such as <!--1234-->
		// Read this to verify that we are not getting a cached page
		boolean inFirstComment = true;
        do {
		    nextChar = input.read();
		    if (inFirstComment) {
		    	if (nextChar == '>') {
		    		inFirstComment = false;
		    	} else if (Character.isDigit((char) nextChar)) {
		    		value = value * 10 + (nextChar - '0');
		    	}
		    }
        } while (nextChar != '$');
        Assert.assertEquals(uniqueParam, value);
        input.close();
	}

}