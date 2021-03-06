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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.JPanel;

import edu.duke.cs.jflap.automata.Automaton;
import edu.duke.cs.jflap.automata.LambdaCheckerFactory;
import edu.duke.cs.jflap.automata.LambdaTransitionChecker;
import edu.duke.cs.jflap.automata.Transition;
import edu.duke.cs.jflap.gui.editor.ArrowDisplayOnlyTool;
import edu.duke.cs.jflap.gui.environment.Environment;
import edu.duke.cs.jflap.gui.environment.Universe;
import edu.duke.cs.jflap.gui.environment.tag.CriticalTag;
import edu.duke.cs.jflap.gui.viewer.AutomatonPane;
import edu.duke.cs.jflap.gui.viewer.SelectionDrawer;

/**
 * This is an action that will highlight all states that have
 * lambda-transitions.
 *
 * @author Thomas Finley
 */
public class LambdaHighlightAction extends AutomatonAction {
	/**
	 * A class that exists to make integration with the help system feasible.
	 */
	private class LambdaPane extends JPanel {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		public LambdaPane(final AutomatonPane ap) {
			super(new BorderLayout());
			add(ap, BorderLayout.CENTER);
			add(new JLabel(Universe.curProfile.getEmptyString() + "-transitions are highlighted."), BorderLayout.NORTH);
			final ArrowDisplayOnlyTool tool = new ArrowDisplayOnlyTool(ap, ap.getDrawer());
			ap.addMouseListener(tool);
		}
	}

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/** The automaton to find the lambda-transitions of. */
	private final Automaton automaton;

	/** The environment to add the pane with the highlighted lambdas to. */
	private final Environment environment;

	public LambdaHighlightAction(final Automaton automaton, final Environment environment) {
		super("Highlight " + Universe.curProfile.getEmptyString() + "-Transitions", null);
		this.automaton = automaton;
		this.environment = environment;
	}

	/**
	 * Highlights states with lambda transitions.
	 */
	@Override
	public void actionPerformed(final ActionEvent event) {
		final List<Transition> t = automaton.getTransitions();
		final Set<Transition> lambdas = new HashSet<>();
		final LambdaTransitionChecker checker = LambdaCheckerFactory.getLambdaChecker(automaton);
		for (int i = 0; i < t.size(); i++) {
			if (checker.isLambdaTransition(t.get(i))) {
				lambdas.add(t.get(i));
			}
		}

		// Create the selection drawer thingie.
		final SelectionDrawer as = new SelectionDrawer(automaton);
		final Iterator<Transition> it = lambdas.iterator();
		while (it.hasNext()) {
			final Transition lt = it.next();
			as.addSelected(lt);
		}

		// Put that in the environment.
		final LambdaPane pane = new LambdaPane(new AutomatonPane(as));
		environment.add(pane, Universe.curProfile.getEmptyString() + "-Transitions", new CriticalTag() {
		});
		environment.setActive(pane);
	}
}
