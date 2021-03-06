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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import edu.duke.cs.jflap.automata.Automaton;
import edu.duke.cs.jflap.automata.fsa.FiniteStateAutomaton;
import edu.duke.cs.jflap.automata.pda.PushdownAutomaton;
import edu.duke.cs.jflap.automata.turing.TuringMachine;
import edu.duke.cs.jflap.file.DataException;
import edu.duke.cs.jflap.file.Decoder;
import edu.duke.cs.jflap.file.ParseException;
import edu.duke.cs.jflap.grammar.Grammar;
import edu.duke.cs.jflap.gui.environment.Environment;
import edu.duke.cs.jflap.gui.environment.EnvironmentFrame;
import edu.duke.cs.jflap.gui.environment.FrameFactory;
import edu.duke.cs.jflap.gui.environment.Universe;

/**
 * The <CODE>TestTuringAction</CODE> is an action to load a structure from a
 * file, and create a new environment with that object.
 *
 * @author Stephen Reading
 */
public class TestAction extends RestrictedAction {
	class inputFilter extends javax.swing.filechooser.FileFilter {
		@Override
		public boolean accept(final File file) {
			final String filename = file.getName();
			return filename.endsWith(".txt");
		}

		@Override
		public String getDescription() {
			return "*.txt";
		}
	}

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public static ArrayList<Object> myObjects = new ArrayList<>();

	public static EnvironmentFrame myEnvFrame;

	public static Environment openFile(final File file, final List<Decoder> codecs, final boolean makeFrame) {
		ParseException p = null;

		for (final int i = 0; i < codecs.size();) {
			try {
				// System.out.println("openFile(File, Codec[], boolean)
				// called");
				final Serializable object = codecs.get(i).decode(file, null);
				if (object instanceof Automaton) {
					final Automaton auto = (Automaton) object;
					try {
						auto.setFilePath(file.getCanonicalPath());
					} catch (final IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				if (object instanceof Grammar) {
					try {
						final Grammar gram = (Grammar) object;
						gram.setFilePath(file.getCanonicalPath());
					} catch (final IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				myObjects.add(object);
				if (makeFrame) {
					myEnvFrame = FrameFactory.createFrame(object);
					if (myEnvFrame == null) {
						return null;
					}
					myEnvFrame.getEnvironment().setFile(file);
					myEnvFrame.getEnvironment().setEncoder(codecs.get(i).correspondingEncoder());
					return myEnvFrame.getEnvironment();
				} else if (object instanceof Automaton) {
					((Automaton) object).setEnvironmentFrame(myEnvFrame);
				} else if (object instanceof Grammar) {
					((Grammar) object).setEnvironmentFrame(myEnvFrame);
				}

			} catch (final ParseException e) {
				p = e;
			}
			return null;
		}
		if (codecs.size() != 1) {
			p = new ParseException("No format could read the file!");
		}
		throw p;
	}

	private final JFileChooser fileChooser;

	/**
	 * Instantiates a new <CODE>Turing Test Action</CODE>.
	 */
	public TestAction() {
		// super("Test Turing Machines", null);
		super("Batch Test", null);
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_A, MAIN_MENU_MASK));
		fileChooser = Universe.CHOOSER;
	}

	public TestAction(final String name, final int key) {
		super(name, null);
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(key, MAIN_MENU_MASK));
		fileChooser = Universe.CHOOSER;
	}

	@Override
	public void actionPerformed(final ActionEvent event) {
		Component source = null;
		try {
			source = (Component) event.getSource();
		} catch (final Throwable e) {
			// Might not be a component, or the event may be null.
			// Who cares.
		}
		performAction(source);
	}

	public boolean checkRightType(final Environment env, final Component source) {
		if (env.myObjects.size() == 0) {
			return false;
		}
		final Object first = env.myObjects.get(0);
		final int firstType = findType(first);
		for (int i = 0; i < env.myObjects.size(); i++) {
			final int curType = findType(env.myObjects.get(i));
			if (curType == 0) {
				JOptionPane.showMessageDialog(source,
						"This feature only works with Finite Automata, Pushdown Automata, and Turing Machines", "Error",
						JOptionPane.ERROR_MESSAGE);
				return false;
			} else if (firstType != curType) {
				JOptionPane.showMessageDialog(source, "Files Must Be The Same Type", "Error",
						JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}
		return true;
	}

	public ArrayList<Object> chooseFile(final Component source, final boolean fromPerformAction) {
		final File tempFile = fileChooser.getCurrentDirectory();
		fileChooser.setCurrentDirectory(tempFile.getParentFile());
		fileChooser.setCurrentDirectory(tempFile);
		fileChooser.rescanCurrentDirectory();
		fileChooser.setMultiSelectionEnabled(true);
		fileChooser.setDialogTitle("Choose Files Of The Same Type To Test");
		List<Decoder> codecs = null;
		codecs = OpenAction.makeFilters();

		// Open the dialog.
		final int result = fileChooser.showOpenDialog(source);
		if (result != JFileChooser.APPROVE_OPTION) {
			return null;
		}
		final File[] files = fileChooser.getSelectedFiles();

		// Serializable last; // Local variable is never read, Jinghui
		// commenting it out.

		for (int k = 0; k < files.length; k++) {
			final File file = files[k];
			if (!file.getAbsolutePath().endsWith(".jff")) {
				JOptionPane.showMessageDialog(source, "One of the files does not end in .jff", "Read Error",
						JOptionPane.ERROR_MESSAGE);
				return null;
			}
			try {
				if (k == 0) {
					openFile(file, codecs, fromPerformAction);
				} else {
					openFile(file, codecs, false);
				}

			} catch (final ParseException e) {
				JOptionPane.showMessageDialog(source, e.getMessage(), "Read Error", JOptionPane.ERROR_MESSAGE);
			} catch (final DataException e) {
				JOptionPane.showMessageDialog(source, e.getMessage(), "Data Error", JOptionPane.ERROR_MESSAGE);
			}
		}

		myEnvFrame.getEnvironment().setMultipleObjects(myObjects);

		return myObjects;
	}

	protected void displayMultipleRunPane(final Environment env, final Object obj) {
		if (obj instanceof FiniteStateAutomaton) {
			final BatchMultipleSimulateAction act = new BatchMultipleSimulateAction((Automaton) obj, env);
			act.performAction(env);
		} else if (obj instanceof TuringMachine) {
			final MultipleOutputSimulateAction act = new MultipleOutputSimulateAction((Automaton) obj, env);
			act.performAction(env);
		} else if (obj instanceof PushdownAutomaton) {
			final MultipleSimulateAction act = new MultipleSimulateAction((Automaton) obj, env);
			act.performAction(env);
		} else if (obj instanceof Grammar) {
			final MultipleSimulateAction act = new MultipleSimulateAction((Grammar) obj, env);
			act.performAction(env);
		}
	}

	private int findType(final Object auto) {
		int type = 0;
		if (auto instanceof TuringMachine) {
			type = 1;
		} else if (auto instanceof PushdownAutomaton) {
			type = 2;
		} else if (auto instanceof FiniteStateAutomaton) {
			type = 3;
		} else if (auto instanceof Grammar) {
			type = 4;
		}
		return type;
	}

	public void performAction(final Component source) {
		ArrayList<Object> files = null;
		boolean sameType = false;
		while (files == null || sameType == false) {
			if (myEnvFrame != null) {
				myEnvFrame.getEnvironment().myObjects.clear();
			}
			files = chooseFile(source, true);
			/*
			 * Jinghui fixing bug, added try/catch block around line sameType =
			 * ... If we click cancel on the first box, myEnvFrame is null, so
			 * we add this block to prevent the stack trace from showing up.
			 */
			try {
				sameType = checkRightType(myEnvFrame.getEnvironment(), source);
			} catch (final NullPointerException e) {
				/*
				 * If user chooses "Cancel" in the first file selection window,
				 * program just returns to the menu. This is to prevent null
				 * pointer exceptions.
				 */
				return;
			}
			/*
			 * Here, return if files is false, so we can cancel.
			 */
			if (files == null) {
				return;
			}
		}

		fileChooser.setDialogTitle("Choose file of strings to test on machines");
		fileChooser.setFileFilter(new inputFilter());
		final int result = fileChooser.showOpenDialog(source);
		// open text file of inputs and outputs

		final ArrayList<String> testStrings = new ArrayList<>();
		final ArrayList<String> transStrings = new ArrayList<>();
		if (result != JFileChooser.APPROVE_OPTION) {
		} else {
			final File textFile = fileChooser.getSelectedFile();
			try {
				final BufferedReader in = new BufferedReader(new FileReader(textFile));
				String str;
				String[] strings;
				final Object first = myEnvFrame.getEnvironment().myObjects.get(0);
				int numberInputs = 1;
				if (first instanceof TuringMachine) {
					final TuringMachine firstTM = (TuringMachine) first;
					numberInputs = firstTM.tapes;
				}
				boolean reject = false;
				while ((str = in.readLine()) != null) {
					reject = false;
					if (str.equals("")) {
						continue;
					}
					strings = str.split(" ");
					for (int k = 0; k < numberInputs; k++) {
						testStrings.add(strings[k]);
						if (strings[strings.length - 1].toLowerCase().equals("reject")) {
							reject = true;
							transStrings.add("~");
							continue;
						}
						if (strings.length > (k + 1 + numberInputs)) {
							transStrings.add(strings[k + numberInputs]);
						} else {
							transStrings.add("~");
						}
					}
					if (reject && transStrings.size() > 0) {
						// transStrings.remove(transStrings.size()-1);
						transStrings.add("reject");
					} else {
						transStrings.add("accept");
					}
				}

				in.close();
			} catch (final IOException e) {
			}
		}
		myEnvFrame.getEnvironment().myTestStrings = testStrings;
		myEnvFrame.getEnvironment().myTransducerStrings = transStrings;
		displayMultipleRunPane(myEnvFrame.getEnvironment(), myObjects.get(0));
	}
}
