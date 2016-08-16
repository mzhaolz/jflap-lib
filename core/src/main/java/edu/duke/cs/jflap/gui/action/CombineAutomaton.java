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

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;

import edu.duke.cs.jflap.automata.Automaton;
import edu.duke.cs.jflap.automata.State;
import edu.duke.cs.jflap.automata.Transition;
import edu.duke.cs.jflap.automata.mealy.MooreMachine;
import edu.duke.cs.jflap.automata.turing.TuringMachine;
import edu.duke.cs.jflap.gui.environment.AutomatonEnvironment;
import edu.duke.cs.jflap.gui.environment.Environment;
import edu.duke.cs.jflap.gui.environment.EnvironmentFrame;
import edu.duke.cs.jflap.gui.environment.FrameFactory;
import edu.duke.cs.jflap.gui.environment.Universe;
import edu.duke.cs.jflap.gui.viewer.AutomatonDrawer;

/**
 * This combines two automatons into a new automaton.
 *
 * @author Thomas Finley
 */
public class CombineAutomaton extends AutomatonAction {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/** The environment. */
	private final AutomatonEnvironment environment;

	/**
	 * Instantiates a new action to combine automatons.
	 *
	 * @param environment
	 *            the automaton environment
	 */
	public CombineAutomaton(final AutomatonEnvironment environment) {
		super("Combine Automata", null);
		this.environment = environment;
	}

	/**
	 * Creates a new automaton.
	 *
	 * @param e
	 *            the action event
	 */
	@Override
	public void actionPerformed(final ActionEvent e) {
		final JComboBox<EnvironmentFrame> combo = new JComboBox<>();
		// Figure out what existing environments in the program have
		// the type of automaton that we need.
		final List<EnvironmentFrame> frames = Universe.frames();
		for (int i = 0; i < frames.size(); i++) {
			final Environment env = frames.get(i).getEnvironment();
			if (env == environment || !(env instanceof AutomatonEnvironment)
					|| environment.getObject().getClass() != env.getObject().getClass()) {
				continue;
			}
			if (environment.getObject() instanceof TuringMachine) {
				final TuringMachine t1 = (TuringMachine) environment.getObject();
				final TuringMachine t2 = (TuringMachine) env.getObject();
				if (t1.tapes() != t2.tapes()) {
					continue;
				}
			}
			combo.addItem(frames.get(i));
		}
		if (combo.getItemCount() == 0) {
			JOptionPane.showMessageDialog(Universe.frameForEnvironment(environment),
					"No other automatons of this type around!");
			return;
		}
		// Prompt the user.
		final int result = JOptionPane.showOptionDialog(Universe.frameForEnvironment(environment), combo, "Combine Two",
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
		if (result != JOptionPane.YES_OPTION && result != JOptionPane.OK_OPTION) {
			return;
		}

		final Automaton other = (Automaton) ((EnvironmentFrame) combo.getSelectedItem()).getEnvironment().getObject();

		final Automaton newOne = environment.getAutomaton().clone();

		add(newOne, other);

		FrameFactory.createFrame(newOne);
	}

	/**
	 * Appends other to the <CODE>newOne</CODE> automaton.
	 *
	 * @param newOne
	 */
	private void add(final Automaton newOne, final Automaton other) {
		final AutomatonDrawer d1 = new AutomatonDrawer(newOne), d2 = new AutomatonDrawer(other);
		Rectangle2D bounds1 = d1.getBounds(), bounds2 = d2.getBounds();
		if (bounds1 == null) {
			bounds1 = new Rectangle2D.Float();
		}
		if (bounds2 == null) {
			bounds2 = new Rectangle2D.Float();
		}
		final double d = bounds1.getY() + bounds1.getHeight() - bounds2.getY() + 20.0;
		final List<State> otherStates = other.getStates();
		final Map<State, State> otherToNew = new HashMap<>();
		for (int i = 0; i < otherStates.size(); i++) {
			final State s = otherStates.get(i);
			final Point p = new Point(s.getPoint().x, s.getPoint().y + (int) d);
			final State s2 = newOne.createState(p);
			if (other.isFinalState(s)) {
				newOne.addFinalState(s2);
			}
			s2.setLabel(s.getLabel());

			/*
			 * To maintain Moore machine state output.
			 */
			if (newOne instanceof MooreMachine && other instanceof MooreMachine) {
				final MooreMachine m = (MooreMachine) newOne;
				final MooreMachine n = (MooreMachine) other;
				m.setOutput(s2, n.getOutput(s));
			}

			otherToNew.put(s, s2);
		}
		final List<Transition> otherTransitions = other.getTransitions();
		for (int i = 0; i < otherTransitions.size(); i++) {
			final Transition t = otherTransitions.get(i);
			final State from = otherToNew.get(t.getFromState()), to = otherToNew.get(t.getToState());
			newOne.addTransition(t.copy(from, to));
		}
	}
}
