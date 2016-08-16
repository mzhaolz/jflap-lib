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

package edu.duke.cs.jflap.gui.minimize;

import java.awt.Component;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.JOptionPane;
import javax.swing.tree.DefaultTreeModel;

import edu.duke.cs.jflap.automata.State;
import edu.duke.cs.jflap.automata.Transition;
import edu.duke.cs.jflap.automata.fsa.FiniteStateAutomaton;
import edu.duke.cs.jflap.automata.fsa.Minimizer;
import edu.duke.cs.jflap.gui.environment.FrameFactory;
import edu.duke.cs.jflap.gui.viewer.SelectionDrawer;

/**
 * This is the class for controlling the building of the minimized automaton,
 * after the tree has been built.
 *
 * @author Thomas Finley
 */
class BuilderController {
	/** The original dfa. */
	private final FiniteStateAutomaton dfa;

	/** The dfa being built. */
	private final FiniteStateAutomaton minDfa;

	/** The selection drawer for the DFA being built. */
	private final SelectionDrawer drawer;

	/** The minimizer object. */
	private final Minimizer minimizer;

	/** The built minimize tree. */
	private final DefaultTreeModel tree;

	/** The component. */
	private final Component view;

	/** The set of transitions that must be created. */
	Set<Transition> remainingTransitions;

	/**
	 * Instantiates a new <CODE>BuilderController</CODE>.
	 *
	 * @param dfa
	 *            the original DFA being minimized
	 * @param minDfa
	 *            the DFA being built; this should initially have just the
	 *            states and nothing else
	 * @param drawer
	 *            the selection drawer for the DFA being built
	 * @param minimizer
	 *            the minimizer object handling the grit of the minimization
	 *            process
	 * @param tree
	 *            the completed minimizer tree
	 * @param view
	 *            the view that the automaton is minimized in
	 */
	public BuilderController(final FiniteStateAutomaton dfa, final FiniteStateAutomaton minDfa,
			final SelectionDrawer drawer, final Minimizer minimizer, final DefaultTreeModel tree,
			final Component view) {
		this.dfa = dfa;
		this.minDfa = minDfa;
		this.drawer = drawer;
		this.minimizer = minimizer;
		this.tree = tree;
		this.view = view;

		determineTransitions();
		initTransitionListener();
	}

	/**
	 * This will add all remaining transitions to the automaton.
	 */
	public void complete() {
		if (remainingTransitions.size() == 0) {
			JOptionPane.showMessageDialog(view, "All transitions are in place already!");
			return;
		}
		final Iterator<?> it = new HashSet<Object>(remainingTransitions).iterator();
		while (it.hasNext()) {
			final Transition t = (Transition) it.next();
			minDfa.addTransition(t);
		}
	}

	/**
	 * Sets up the remaining transitions set with all the transitions that need
	 * to be created.
	 */
	private void determineTransitions() {
		remainingTransitions = new HashSet<>();
		final List<State> states = minDfa.getStates();
		for (final State si : states) {
			remainingTransitions.addAll(minimizer.getTransitionsForState(si, minDfa, dfa, tree));
		}
	}

	/**
	 * This will check if the automaton is done. If it is, it will bring it up
	 * in its own window.
	 */
	public void done() {
		final int remain = remainingTransitions.size();
		if (remain != 0) {
			final Transition t = remainingTransitions.iterator().next();
			drawer.addSelected(t.getFromState());
			JOptionPane.showMessageDialog(view, remain + " transition" + (remain == 1 ? "" : "s") + " remain "
					+ "to be placed.\n" + "One comes from the state highlighted.");
			drawer.clearSelected();
			return;
		}
		JOptionPane.showMessageDialog(view,
				"The minimized automaton is fully built!\n" + "It will now be placed in a new window.");
		FrameFactory.createFrame(minDfa.clone());
	}

	/**
	 * This will add one transition (picked quasi-randomly, or at least
	 * unpredictably, as the first off the map iterator) to the automaton being
	 * built.
	 */
	public void hint() {
		if (remainingTransitions.size() == 0) {
			JOptionPane.showMessageDialog(view, "All transitions are in place already!");
			return;
		}
		final Iterator<?> it = new HashSet<Object>(remainingTransitions).iterator();
		final Transition t = (Transition) it.next();
		minDfa.addTransition(t);
	}

	/**
	 * Sets up the listener of transitions that will monitor the automaton begin
	 * created for newly created transitions.
	 */
	private void initTransitionListener() {
		minDfa.addTransitionListener(e -> {
			if (!e.isAdd()) {
				return;
			}
			final Transition transition = e.getTransition();
			if (!remainingTransitions.contains(transition)) {
				JOptionPane.showMessageDialog(view, "That transition is not correct!");
				minDfa.removeTransition(transition);
			} else {
				remainingTransitions.remove(transition);
			}
		});
	}
}
