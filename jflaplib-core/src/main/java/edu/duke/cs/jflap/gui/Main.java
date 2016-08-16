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

package edu.duke.cs.jflap.gui;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import edu.duke.cs.jflap.file.Decoder;
import edu.duke.cs.jflap.file.ParseException;
import edu.duke.cs.jflap.gui.action.NewAction;
import edu.duke.cs.jflap.gui.action.OpenAction;
import edu.duke.cs.jflap.gui.environment.Profile;
import edu.duke.cs.jflap.gui.environment.Universe;

/**
 * This is the class that starts JFLAP.
 *
 * @author Thomas Finley
 * @author Moti Ben-Ari
 * @modified by Kyung Min (Jason) Lee
 */
public class Main {

	private static boolean dontQuit; // Don't quit when Quit selected

	public static boolean getDontQuit() {
		return dontQuit;
	}

	/**
	 * This method loads from the preferences file, if one exists.
	 */
	private static void loadPreferences() {
		final Profile current = Universe.curProfile;
		String path = "";
		try {
			path = new File(".").getCanonicalPath();
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		path = path + "/jflapPreferences.xml";
		current.pathToFile = path;

		if (new File(path).exists()) {
			final File file = new File(path);
			final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder;
			try {
				builder = factory.newDocumentBuilder();
				Document doc;
				try {
					doc = builder.parse(file);

					// Set the empty string constant
					Node parent = doc.getDocumentElement().getElementsByTagName(current.EMPTY_STRING_NAME).item(0);
					if (parent != null) {
						final String empty = parent.getTextContent();
						if (empty.equals(current.lambdaText)) {
							current.setEmptyString(current.lambda);
						} else if (empty.equals(current.epsilonText)) {
							current.setEmptyString(current.epsilon);
						}
					}

					// Then set the Turing final state constant
					parent = doc.getDocumentElement().getElementsByTagName(Profile.TURING_FINAL_NAME).item(0);
					if (parent != null) {
						final String turingFinal = parent.getTextContent();
						if (turingFinal.equals("true")) {
							current.setTransitionsFromTuringFinalStateAllowed(true);
						} else {
							current.setTransitionsFromTuringFinalStateAllowed(false);
						}
					}

					// set the Turing Acceptance ways.
					parent = doc.getDocumentElement().getElementsByTagName(Profile.ACCEPT_FINAL_STATE).item(0);
					if (parent != null) {
						final String acceptFinal = parent.getTextContent();
						if (acceptFinal.equals("true")) {
							current.setAcceptByFinalState(true);
						} else {
							current.setAcceptByFinalState(false);
						}
					}

					parent = doc.getDocumentElement().getElementsByTagName(Profile.ACCEPT_HALT).item(0);
					if (parent != null) {
						final String acceptHalt = parent.getTextContent();
						if (acceptHalt.equals("true")) {
							current.setAcceptByHalting(true);
						} else {
							current.setAcceptByHalting(false);
						}
					}

					// set the AllowStay option
					parent = doc.getDocumentElement().getElementsByTagName(Profile.ALLOW_STAY).item(0);
					if (parent != null) {
						final String allowStay = parent.getTextContent();
						if (allowStay.equals("true")) {
							current.setAllowStay(true);
						} else {
							current.setAllowStay(false);
						}
					}

					// Now set the Undo amount
					parent = doc.getDocumentElement().getElementsByTagName(Profile.UNDO_AMOUNT_NAME).item(0);
					if (parent != null) {
						final String number = parent.getTextContent();
						current.setNumUndo(Integer.parseInt(number));
					}

				} catch (final SAXException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (final IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} catch (final ParserConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * Starts JFLAP. This sets various system properties. If there are command
	 * line arguments, this will attempt to open them as JFLAP files. If there
	 * are no arguments, this will call on
	 * {@link edu.duke.cs.jflap.gui.action.NewAction#showNew} to display a
	 * choice for a new structure.
	 *
	 * @param args
	 *            the command line arguments, which may hold files to open
	 */
	public static void main(final String[] args, final boolean dont) {

		dontQuit = dont;
		// Make sure we're not some old version.
		try {
			final String v = System.getProperty("java.specification.version");
			final double version = Double.parseDouble(v) + 0.00001;
			if (version < 1.5) {
				javax.swing.JOptionPane.showMessageDialog(null, "Java 1.5 or higher required to run JFLAP!\n"
						+ "You appear to be running Java " + v + ".\n" + "This program will now exit.");
				System.exit(0);
			}
		} catch (final SecurityException e) {
			// Eh, that shouldn't happen.
		}

		// Set the AWT exception handler. This may not work in future
		// Java versions.
		try {
			System.setProperty("sun.awt.exception.handler", "gui.ThrowableCatcher");
		} catch (final SecurityException e) {
			System.err.println("Warning: could not set the " + "AWT exception handler.");
		}

		// Apple is stupid.
		try {
			// Well, Apple WAS stupid...
			if (System.getProperty("os.name").startsWith("Mac OS")
					&& System.getProperty("java.specification.version").equals("1.3")) {
				System.setProperty("com.apple.hwaccel", "false");
			}
		} catch (final SecurityException e) {
			// Bleh.
		}
		// Sun is stupider.
		try {
			System.setProperty("java.util.prefs.syncInterval", "2000000");
		} catch (final SecurityException e) {
			// Well, not key.
		}
		// Prompt the user for newness.
		NewAction.showNew();
		if (args.length > 0) {
			if (args[0].equals("text")) {
			}

			for (final String arg : args) {
				final List<Decoder> codecs = Universe.CODEC_REGISTRY.getDecoders();
				try {
					OpenAction.openFile(new File(arg), codecs);
				} catch (final ParseException e) {
					System.err.println("Could not open " + arg + ": " + e.getMessage());
				}
			}
		}
		loadPreferences();
	}
}
