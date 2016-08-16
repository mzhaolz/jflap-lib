/*
*  JFLAP - Formal Languages and Automata Package
*
*
*  Susan H. Rodger
*  Computer Science Department
*  Duke University
*  August 27, 2009

*  Copyright (c) 2002-2009
*  All rights reserved.

*  JFLAP is open source software. Please see the LICENSE for terms.
*
*/

package edu.duke.cs.jflap.debug;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EDebug {
	private static final Logger logger = LoggerFactory.getLogger(EDebug.class);

	public static boolean setFind = false;

	private static String getClassName() {
		return Thread.currentThread().getStackTrace()[3].getClassName();
	}

	private static String getFileName() {
		return Thread.currentThread().getStackTrace()[3].getClassName();
	}

	private static int getLineNumber() {
		return Thread.currentThread().getStackTrace()[3].getLineNumber();
	}

	private static String getMethodName() {
		return Thread.currentThread().getStackTrace()[3].getMethodName();
	}

	public static void print(final Object s) {
		if (setFind) {
			logger.debug(getFileName() + ":" + getClassName() + ":" + getMethodName() + ":" + getLineNumber());
		}
		logger.debug(s.toString());
	}
}
