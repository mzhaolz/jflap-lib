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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;

import edu.duke.cs.jflap.automata.Automaton;
import edu.duke.cs.jflap.automata.NondeterminismDetector;
import edu.duke.cs.jflap.automata.NondeterminismDetectorFactory;
import edu.duke.cs.jflap.automata.State;
import edu.duke.cs.jflap.gui.editor.ArrowDisplayOnlyTool;
import edu.duke.cs.jflap.gui.environment.Environment;
import edu.duke.cs.jflap.gui.environment.tag.CriticalTag;
import edu.duke.cs.jflap.gui.viewer.AutomatonPane;
import edu.duke.cs.jflap.gui.viewer.SelectionDrawer;

/**
 * This is the action used to highlight nondeterministic states.
 *
 * @author Thomas Finley
 */
public class NondeterminismAction extends AutomatonAction {
	/**
	 * A class that exists to make integration with the help system feasible.
	 */
	private class NondeterminismPane extends JPanel {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		public NondeterminismPane(final AutomatonPane ap) {
			super(new BorderLayout());
			ap.addMouseListener(new ArrowDisplayOnlyTool(ap, ap.getDrawer()));
			add(ap, BorderLayout.CENTER);
			add(new JLabel("Nondeterministic states are highlighted."), BorderLayout.NORTH);
		}
	}

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * This action is only applicable to automaton objects.
	 *
	 * @param object
	 *            the object to test for being an automaton
	 * @return <CODE>true</CODE> if this object is an instance of a subclass of
	 *         <CODE>Automaton</CODE>, <CODE>false</CODE> otherwise
	 */
	public static boolean isApplicable(final Object object) {
		return object instanceof Automaton;
	}

	/** The automaton this simulate action runs simulations on! */
	private final Automaton automaton;

	/** The environment that the simulation pane will be put in. */
	private final Environment environment;

	/**
	 * Instantiates a new <CODE>NondeterminismAction</CODE>.
	 *
	 * @param automaton
	 *            the automaton that input will be simulated on
	 * @param environment
	 *            the environment object that we shall add our simulator pane to
	 */
	public NondeterminismAction(final Automaton automaton, final Environment environment) {
		super("Highlight Nondeterminism", null);
		this.automaton = automaton;
		this.environment = environment;
	}

	/**
	 * Performs the action.
	 */
	@Override
	public void actionPerformed(final ActionEvent e) {
		final SelectionDrawer drawer = new SelectionDrawer(automaton);
		final NondeterminismDetector d = NondeterminismDetectorFactory.getDetector(automaton);
		final List<State> nd = d.getNondeterministicStates(automaton);
		for (int i = 0; i < nd.size(); i++) {
			drawer.addSelected(nd.get(i));
		}
		final AutomatonPane ap = new AutomatonPane(drawer);
		final NondeterminismPane pane = new NondeterminismPane(ap);
		environment.add(pane, "Nondeterminism", new CriticalTag() {
		});
		environment.setActive(pane);
	}
}
