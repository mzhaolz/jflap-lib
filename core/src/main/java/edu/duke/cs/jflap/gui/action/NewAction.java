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

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import edu.duke.cs.jflap.automata.mealy.MealyMachine;
import edu.duke.cs.jflap.automata.mealy.MooreMachine;
import edu.duke.cs.jflap.gui.environment.FrameFactory;
import edu.duke.cs.jflap.gui.environment.Universe;
import edu.duke.cs.jflap.gui.menu.MenuBarCreator;
import edu.duke.cs.jflap.gui.pumping.CFPumpingLemmaChooser;
import edu.duke.cs.jflap.gui.pumping.RegPumpingLemmaChooser;

/**
 * The <CODE>NewAction</CODE> handles when the user decides to create some new
 * environment, that is, some sort of new automaton, or grammar, or regular
 * expression, or some other such editable object.
 *
 * @author Thomas Finley
 */
public class NewAction extends RestrictedAction {
	/** The dialog box that allows one to create new environments. */
	private static class NewDialog extends JFrame {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * Instantiates a <CODE>NewDialog</CODE> instance.
		 */
		public NewDialog() {
			// super((java.awt.Frame)null, "New Document");
			super("JFLAP 7.0");
			getContentPane().setLayout(new GridLayout(0, 1));
			initMenu();
			initComponents();
			setResizable(false);
			pack();
			this.setLocation(50, 50);

			addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(final WindowEvent event) {
					if (Universe.numberOfFrames() > 0) {
						NewDialog.this.setVisible(false);
					} else {
						QuitAction.beginQuit();
					}
				}
			});
		}

		private void initComponents() {
			JButton button = null;
			// Let's hear it for sloth!

			button = new JButton("Finite Automaton");
			button.addActionListener(e -> createWindow(new edu.duke.cs.jflap.automata.fsa.FiniteStateAutomaton()));
			getContentPane().add(button);

			button = new JButton("Mealy Machine");
			button.addActionListener(e -> createWindow(new MealyMachine()));
			getContentPane().add(button);
			button = new JButton("Moore Machine");
			button.addActionListener(e -> createWindow(new MooreMachine()));
			getContentPane().add(button);

			button = new JButton("Pushdown Automaton");
			button.addActionListener(e -> {
				final Object[] possibleValues = { "Multiple Character Input", "Single Character Input" };
				final Object selectedValue = JOptionPane.showInputDialog(null, "Type of PDA Input", "Input",
						JOptionPane.INFORMATION_MESSAGE, null, possibleValues, possibleValues[0]);
				if (selectedValue == possibleValues[0]) {
					createWindow(new edu.duke.cs.jflap.automata.pda.PushdownAutomaton());
				} else if (selectedValue == possibleValues[1]) {
					createWindow(new edu.duke.cs.jflap.automata.pda.PushdownAutomaton(true));
				}
			});
			getContentPane().add(button);

			button = new JButton("Turing Machine");
			button.addActionListener(e -> createWindow(new edu.duke.cs.jflap.automata.turing.TuringMachine(1)));
			getContentPane().add(button);

			button = new JButton("Multi-Tape Turing Machine");
			button.addActionListener(new ActionListener() {
				private List<Integer> INTS = null;

				@Override
				public void actionPerformed(final ActionEvent e) {
					if (INTS == null) {
						INTS = new ArrayList<>();
						for (int i = 0; i < 4; i++) {
							INTS.add(new Integer(i + 2));
						}
					}
					final Number n = (Number) JOptionPane.showInputDialog(NewDialog.this.getContentPane(),
							"How many tapes?", "Multi-tape Machine", JOptionPane.QUESTION_MESSAGE, null, INTS.toArray(),
							INTS.get(0));
					if (n == null) {
						return;
					}
					createWindow(new edu.duke.cs.jflap.automata.turing.TuringMachine(n.intValue()));
				}
			});
			getContentPane().add(button);

			button = new JButton("Grammar");
			button.addActionListener(e -> createWindow(new edu.duke.cs.jflap.grammar.cfg.ContextFreeGrammar()));
			getContentPane().add(button);

			button = new JButton("L-System");
			button.addActionListener(e -> createWindow(new edu.duke.cs.jflap.grammar.lsystem.LSystem()));
			getContentPane().add(button);

			button = new JButton("Regular Expression");
			button.addActionListener(e -> createWindow(new edu.duke.cs.jflap.regular.RegularExpression()));
			getContentPane().add(button);

			button = new JButton("Regular Pumping Lemma");
			button.addActionListener(e -> createWindow(new RegPumpingLemmaChooser()));
			getContentPane().add(button);

			button = new JButton("Context-Free Pumping Lemma");
			button.addActionListener(e -> createWindow(new CFPumpingLemmaChooser()));
			getContentPane().add(button);
		}

		private void initMenu() {
			// Mini menu!
			final JMenuBar menuBar = new JMenuBar();
			JMenu menu = new JMenu("File");
			if (Universe.CHOOSER != null) {
				MenuBarCreator.addItem(menu, new OpenAction());
			}
			try {
				final SecurityManager sm = System.getSecurityManager();
				if (sm != null) {
					sm.checkExit(0);
				}
				MenuBarCreator.addItem(menu, new QuitAction());
			} catch (final SecurityException e) {
				// Well, can't exit anyway.
			}
			menuBar.add(menu);
			menu = new JMenu("Help");
			MenuBarCreator.addItem(menu, new NewHelpAction());
			MenuBarCreator.addItem(menu, new AboutAction());
			menuBar.add(menu);
			menu = new JMenu("Batch");
			MenuBarCreator.addItem(menu, new TestAction());
			menuBar.add(menu);
			menu = new JMenu("Preferences");

			final JMenu tmPrefMenu = new JMenu("Turing Machine Preferences");
			tmPrefMenu.add(Universe.curProfile.getTuringFinalCheckBox());
			tmPrefMenu.add(Universe.curProfile.getAcceptByFinalStateCheckBox());
			tmPrefMenu.add(Universe.curProfile.getAcceptByHaltingCheckBox());
			tmPrefMenu.add(Universe.curProfile.getAllowStayCheckBox());

			MenuBarCreator.addItem(menu, new EmptyStringCharacterAction());
			// menu.add(Universe.curProfile.getTuringFinalCheckBox());
			menu.add(new SetUndoAmountAction());

			menu.add(tmPrefMenu);

			menuBar.add(menu);
			setJMenuBar(menuBar);
		}
	}

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/** The universal dialog. */
	private static NewDialog DIALOG = null;

	/**
	 * Dispose of environment dialog by Moti Ben-Ari
	 */
	public static void closeNew() {
		DIALOG.dispose();
		DIALOG = null;
	}

	/**
	 * Called once a type of editable object is choosen. The editable object is
	 * passed in, the dialog is hidden, and the window is created.
	 *
	 * @param object
	 *            the object that we are to edit
	 */
	private static void createWindow(final Serializable object) {
		DIALOG.setVisible(false);
		FrameFactory.createFrame(object);
	}

	/**
	 * Hides the new environment dialog.
	 */
	public static void hideNew() {
		DIALOG.setVisible(false);
	}

	/**
	 * Shows the new environment dialog.
	 */
	public static void showNew() {
		if (DIALOG == null) {
			DIALOG = new NewDialog();
		}
		DIALOG.setVisible(true);
		DIALOG.toFront();
	}

	/**
	 * Instantiates a new <CODE>NewAction</CODE>.
	 */
	public NewAction() {
		super("New...", null);
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_N, MAIN_MENU_MASK));
	}

	/**
	 * Shows the new machine dialog box.
	 *
	 * @param event
	 *            the action event
	 */
	@Override
	public void actionPerformed(final ActionEvent event) {
		showNew();
	}
}
