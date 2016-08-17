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

package edu.duke.cs.jflap.gui.environment;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFileChooser;

import com.google.common.collect.Lists;

import edu.duke.cs.jflap.file.CodecRegistry;
import edu.duke.cs.jflap.file.XMLCodec;

/**
 * The <CODE>Universe</CODE> class serves as a large global "registry" for the
 * active windows and their associated environments.
 *
 * @author Thomas Finley
 */
public class Universe {
	/** The mapping of environments to frames. */
	private static Map<Environment, EnvironmentFrame> environmentToFrame = new HashMap<>();

	/** The mapping of files to frames. */
	private static Map<String, EnvironmentFrame> fileToFrame = new HashMap<>();

	/** The universal JFileChooser. */
	public static JFileChooser CHOOSER = null;

	/**
	 * The number of frames that have been registered... this is used to
	 * describe the untitled frames with something unique.
	 */
	private static int numberRegistered = 0;

	/**
	 * This is the file listener that should be added to the environments when
	 * their frames are created to ensure that no file is opened twice.
	 */
	private static FileChangeListener FILE_LISTENER = e -> {
		// We must update the index.
		final File oldFile = e.getOldFile();
		final EnvironmentFrame frame = frameForEnvironment((Environment) e.getSource());
		if (oldFile != null) {
			fileToFrame.remove(getPath(oldFile));
		}
		final Environment env = (Environment) e.getSource();
		final File newFile = env.getFile();
		if (newFile == null) {
			return;
		}
		fileToFrame.put(getPath(newFile), frame);
	};

	/** The registry of codecs universally used for saving. */
	public static final CodecRegistry CODEC_REGISTRY = new CodecRegistry();

	static {
		try {
			CHOOSER = new JFileChooser(System.getProperties().getProperty("user.dir"));
		} catch (final java.security.AccessControlException e) {
			// Nothing to do.
		}
		// Create the codec registry.
		final XMLCodec xc = new XMLCodec();
		CODEC_REGISTRY.add(xc);
		// CODEC_REGISTRY.add(new SerializedCodec(xc));
		// CODEC_REGISTRY.add(new JFLAP3Codec());
		// CODEC_REGISTRY.add(new LenoreSystemsCodec());
	}

	public static Profile curProfile = new Profile();

	/**
	 * Given an environment, this returns the frame associated with that
	 * environment.
	 *
	 * @param environment
	 *            an environment that may have some frame
	 * @return the environment frame associated with this environment, or
	 *         <CODE>null</CODE> if there is no frame associated with this
	 *         environment
	 */
	public static EnvironmentFrame frameForEnvironment(final Environment environment) {
		return environmentToFrame.get(environment);
	}

	/**
	 * Given a file, this returns the frame associated with that file.
	 *
	 * @param file
	 *            a file that may be an active file for some environment
	 * @return the environment frame associated with this file, or
	 *         <CODE>null</CODE> if there is no frame associated with this file
	 */
	public static EnvironmentFrame frameForFile(final File file) {
		if (file == null) {
			return null;
		}
		return fileToFrame.get(getPath(file));
	}

	/**
	 * Returns a list of the registered environment frames.
	 *
	 * @return an array containing all registered environment frames
	 */
	public static List<EnvironmentFrame> frames() {
		return Lists.newArrayList(environmentToFrame.values());
	}

	/**
	 * Returns the path for a file. This attempts to retrieve the canonical
	 * path, but if that fails (it shouldn't) returns the absolute path
	 *
	 * @param file
	 *            the file to get the path for
	 * @return the canonical path, or alternatively the absolute path
	 */
	private static String getPath(final File file) {
		try {
			return file.getCanonicalPath();
		} catch (final IOException e) {
			return file.getAbsolutePath();
		}
	}

	/**
	 * Returns the number of currently open frames that hold a representation of
	 * a structure (i.e. automaton, grammar, or regular expression).
	 *
	 * @return the number of currently open frames
	 */
	public static int numberOfFrames() {
		return environmentToFrame.size();
	}

	/**
	 * Registers an environment frame.
	 *
	 * @param frame
	 *            the environment frame to register
	 * @return an integer for the number of frames that have been registered
	 *         sofar, including this one
	 */
	public static int registerFrame(final EnvironmentFrame frame) {
		final Environment env = frame.getEnvironment();
		environmentToFrame.put(env, frame);
		final File file = env.getFile();
		if (file != null) {
			fileToFrame.put(getPath(file), frame);
		}
		// Adds the listener that changes this object in the event
		// that the file of an environment changes.
		env.addFileChangeListener(FILE_LISTENER);
		// Hide the new dialog box.
		edu.duke.cs.jflap.gui.action.NewAction.hideNew();

		return ++numberRegistered;
	}

	/**
	 * Unregisters an environment frame.
	 *
	 * @param frame
	 *            the environment frame to unregister
	 */
	public static void unregisterFrame(final EnvironmentFrame frame) {
		try {
			fileToFrame.remove(getPath(frame.getEnvironment().getFile()));
		} catch (final NullPointerException e) {
			// The environment doesn't have a file.
		}
		environmentToFrame.remove(frame.getEnvironment());

		// If there are no other frames open, prompt for newness.
		if (numberOfFrames() == 0) {
			edu.duke.cs.jflap.gui.action.NewAction.showNew();
		}
	}

	/**
	 * This class needn't have multiple instances, so we disable the main
	 * constructor.
	 */
	private Universe() {
	}
}