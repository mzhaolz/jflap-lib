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

package edu.duke.cs.jflap.gui.action;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileFilter;

import edu.duke.cs.jflap.automata.turing.TuringMachine;
import edu.duke.cs.jflap.file.Codec;
import edu.duke.cs.jflap.file.DataException;
import edu.duke.cs.jflap.file.Decoder;
import edu.duke.cs.jflap.file.ParseException;
import edu.duke.cs.jflap.gui.environment.EnvironmentFrame;
import edu.duke.cs.jflap.gui.environment.FrameFactory;
import edu.duke.cs.jflap.gui.environment.Universe;

/**
 * The <CODE>OpenAction</CODE> is an action to load a structure from a file, and
 * create a new environment with that object.
 *
 * @author Thomas Finley
 */
public class OpenAction extends RestrictedAction {
	/** The exception class for when a file could not be read properly. */
	protected static class FileReadException extends RuntimeException {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * Instantiates a file read exception with a given message.
		 *
		 * @param message
		 *            the specific message for why the read failed
		 */
		public FileReadException(final String message) {
			super(message);
		}
	}

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	// ** False causes file to be opened, True causes file to be read but not
	// opened"
	private static boolean openOrRead = false;

	private static Serializable lastObject = null;

	private static File lastFile = null;

	private static boolean lastFileOpened = false;

	/**
	 * Returns the last file opened by the filebrowswer.
	 *
	 * @return
	 */
	public static File getLastFileOpened() {
		return lastFile;
	}

	/**
	 * Returns the last object opened by the filebrowswer.
	 *
	 * @return
	 */
	public static Serializable getLastObjectOpened() {
		return lastObject;
	}

	/**
	 * The open action is completely environment independent.
	 *
	 * @param object
	 *            some object, which is ignored
	 * @return always returns <CODE>true</CODE>
	 */
	public static boolean isApplicable(final Object object) {
		return true;
	}

	public static boolean isOpened() {
		return lastFileOpened;
	}

	public static List<Decoder> makeFilters() {
		// Set up the file filters.
		Universe.CHOOSER.resetChoosableFileFilters();
		final List<Decoder> decoders = Universe.CODEC_REGISTRY.getDecoders();
		final Iterator<Decoder> it = decoders.iterator();
		while (it.hasNext()) {
			Universe.CHOOSER.addChoosableFileFilter((FileFilter) it.next());
		}
		Universe.CHOOSER.setFileFilter(Universe.CHOOSER.getAcceptAllFileFilter());

		// Get the decoders.
		List<Decoder> codecs = null;
		final FileFilter filter = Universe.CHOOSER.getFileFilter();
		if (filter == Universe.CHOOSER.getAcceptAllFileFilter()) {
			codecs = decoders;
		} else {
			codecs = new ArrayList<>();
			codecs.add((Codec) filter);
		}

		return codecs;
	}

	/**
	 * Attempts to open a specified file with a set of codecs.
	 *
	 * @param file
	 *            the file to attempt to open
	 * @param codecs
	 *            the codecs to use
	 * @throws ParseException
	 *             if there was an error with all or one of the codecs
	 */
	public static void openFile(final File file, final List<Decoder> codecs) {
		ParseException p = null;
		for (int i = 0; i < codecs.size(); i++) {
			try {
				final Serializable object = codecs.get(i).decode(file, null);
				if (openOrRead && !(object instanceof TuringMachine)) {
					JOptionPane.showMessageDialog(null, "Only Turing Machine files can be added as building blocks.",
							"Wrong File Type", JOptionPane.ERROR_MESSAGE);
					return;
				}
				lastObject = object;
				lastFile = file;
				// Set the file on the thing.
				if (!openOrRead) {
					final EnvironmentFrame ef = FrameFactory.createFrame(object);
					if (ef == null) {
						return;
					}
					ef.getEnvironment().setFile(file);
					ef.getEnvironment().setEncoder(codecs.get(i).correspondingEncoder());
				}
				return;
			} catch (final ParseException e) {
				p = e;
			}
		}
		if (codecs.size() != 1) {
			p = new ParseException("No format could read the file!");
		}
		throw p;
	}

	public static java.io.Serializable readFileAndCodecs(final File file) {
		OpenAction.setOpenOrRead(true);
		List<Decoder> codecs = null;
		codecs = makeFilters();
		openFile(file, codecs);
		OpenAction.setOpenOrRead(false);
		return OpenAction.getLastObjectOpened();
	}

	/**
	 * @param b
	 */
	public static void setOpenOrRead(final boolean b) {
		openOrRead = b;
	}

	/** The file chooser. */
	private final JFileChooser fileChooser;

	/**
	 * Instantiates a new <CODE>OpenAction</CODE>.
	 */
	public OpenAction() {
		super("Open...", null);
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O, MAIN_MENU_MASK));
		fileChooser = Universe.CHOOSER;
		// this.fileChooser = new JFileChooser
		// (System.getProperties().getProperty("user.dir"));
	}

	/**
	 * If an open is attempted, call the methods that handle the retrieving of
	 * an object, then create a new frame for the environment.
	 *
	 * @param event
	 *            the action event
	 */
	@Override
	public void actionPerformed(final ActionEvent event) {
		Component source = null;
		lastFileOpened = false;
		try {
			source = (Component) event.getSource();
		} catch (final Throwable e) {
			// Might not be a component, or the event may be null.
			// Who cares.
		}

		// Apple is so stupid.

		final File tempFile = fileChooser.getCurrentDirectory();
		fileChooser.setCurrentDirectory(tempFile.getParentFile());
		fileChooser.setCurrentDirectory(tempFile);
		fileChooser.rescanCurrentDirectory();
		fileChooser.setMultiSelectionEnabled(true);
		List<Decoder> codecs = null;
		codecs = makeFilters();

		// Open the dialog.
		final int result = fileChooser.showOpenDialog(source);
		if (result != JFileChooser.APPROVE_OPTION) {
			return;
		}
		final File[] files = fileChooser.getSelectedFiles();
		for (final File file : files) {
			if (!openOrRead) {
				// Is this file already open?
				if (Universe.frameForFile(file) != null) {
					Universe.frameForFile(file).toFront();
					return;
				}
			}
			try {
				openFile(file, codecs);

			} catch (final ParseException e) {
				JOptionPane.showMessageDialog(source, e.getMessage(), "Read Error", JOptionPane.ERROR_MESSAGE);
			} catch (final DataException e) {
				JOptionPane.showMessageDialog(source, e.getMessage(), "Data Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		Universe.CHOOSER.resetChoosableFileFilters();
		lastFileOpened = true;
	}
}
