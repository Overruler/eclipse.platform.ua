/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ua.tests.cheatsheet.composite;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllCompositeTests {

	public static Test suite() {
		TestSuite suite = new TestSuite(
				"org.eclipse.ua.tests.cheatsheet.AllCompositeTests");
		//$JUnit-BEGIN$
		suite.addTestSuite(TestCompositeParser.class);
		suite.addTestSuite(TestState.class);
		suite.addTestSuite(TestTaskGroups.class);
		suite.addTestSuite(TestPersistence.class);
		suite.addTestSuite(TestMarkupParser.class);
		suite.addTestSuite(TestCheatSheetManagerEvents.class);
		suite.addTestSuite(TestSuccessors.class);
		suite.addTestSuite(TestTaskEvents.class);
		suite.addTestSuite(TestDependency.class);
		//$JUnit-END$
		return suite;
	}

}
