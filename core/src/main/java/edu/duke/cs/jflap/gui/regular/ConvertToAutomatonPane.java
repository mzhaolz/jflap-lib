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

package edu.duke.cs.jflap.gui.regular;

import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.util.LinkedList;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import edu.duke.cs.jflap.automata.State;
import edu.duke.cs.jflap.automata.fsa.FSATransition;
import edu.duke.cs.jflap.automata.fsa.FiniteStateAutomaton;
import edu.duke.cs.jflap.gui.editor.ArrowNontransitionTool;
import edu.duke.cs.jflap.gui.editor.Tool;
import edu.duke.cs.jflap.gui.editor.ToolBox;
import edu.duke.cs.jflap.gui.environment.RegularEnvironment;
import edu.duke.cs.jflap.gui.environment.Universe;
import edu.duke.cs.jflap.gui.viewer.SelectionDrawer;
import edu.duke.cs.jflap.regular.Discretizer;

/**
 * This is the pane that holds the tools necessary for the conversion of a
 * regular expression to a finite state automaton.
 *
 * @author Thomas Finley
 */
public class ConvertToAutomatonPane extends JPanel {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The environment that holds the regular expression. The regular expression
	 * from the environment is itself not modified.
	 */
	RegularEnvironment environment;

	/**
	 * The automaton being built, which will be modified throughout this
	 * process.
	 */
	private final FiniteStateAutomaton automaton = new FiniteStateAutomaton();

	/** The controller object. */
	private REToFSAController controller;

	/** The frame that holds the environment. */
	JFrame frame;

	/** The labels holding the current directions. */
	JLabel mainLabel = new JLabel();

	JLabel detailLabel = new JLabel();

	/** The actions. */
	AbstractAction doStepAction = new AbstractAction("Do Step") {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(final ActionEvent e) {
			controller.completeStep();
		}
	};

	AbstractAction doAllAction = new AbstractAction("Do All") {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(final ActionEvent e) {
			controller.completeAll();
		}
	};

	AbstractAction exportAction = new AbstractAction("Export") {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(final ActionEvent e) {
			controller.export();
		}
	};

	AbstractAction exportAction2 = new AbstractAction("Export Now") {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(final ActionEvent e) {
			controller.exportToTab();
		}
	};

	/**
	 * Creates a new conversion pane for the conversion of a regular expression
	 * to an automaton.
	 *
	 * @param environment
	 *            the environment that this convert pane will be a part of
	 */
	public ConvertToAutomatonPane(final RegularEnvironment environment) {
		this.environment = environment;
		Universe.frameForEnvironment(environment);

		setLayout(new BorderLayout());

		final JPanel labels = new JPanel(new BorderLayout());
		labels.add(mainLabel, BorderLayout.NORTH);
		labels.add(detailLabel, BorderLayout.SOUTH);
		mainLabel.setText(" ");
		detailLabel.setText(" ");

		add(labels, BorderLayout.NORTH);
		final SelectionDrawer automatonDrawer = new SelectionDrawer(automaton);

		// Do the initialization of the automaton.
		final State initialState = automaton.createState(new Point(60, 40));
		final State finalState = automaton.createState(new Point(450, 250));
		automaton.setInitialState(initialState);
		automaton.addFinalState(finalState);
		final FSATransition initialTransition = new FSATransition(initialState, finalState, Discretizer.delambda(
				environment.getExpression().asString().replace('!', Universe.curProfile.getEmptyString().charAt(0))));
		automaton.addTransition(initialTransition);

		controller = new REToFSAController(this, automaton);

		final edu.duke.cs.jflap.gui.editor.EditorPane ep = new edu.duke.cs.jflap.gui.editor.EditorPane(automatonDrawer,
				(ToolBox) (view, drawer) -> {
					final LinkedList<Tool> tools = new LinkedList<>();
					tools.add(new ArrowNontransitionTool(view, drawer));
					tools.add(new RegularToAutomatonTransitionTool(view, drawer, controller));
					tools.add(new DeexpressionifyTransitionTool(view, drawer, controller));
					return tools;
				});

		final JToolBar bar = ep.getToolBar();
		bar.addSeparator();
		bar.add(doStepAction);
		bar.add(doAllAction);
		bar.add(exportAction);
		// bar.add(exportAction2);

		add(ep, BorderLayout.CENTER);
	}
}
