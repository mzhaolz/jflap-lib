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

package edu.duke.cs.jflap.automata.graph;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.List;

import edu.duke.cs.jflap.automata.Automaton;
import edu.duke.cs.jflap.automata.State;
import edu.duke.cs.jflap.automata.Transition;

/**
 * This extension of the graph makes it easier for a graph to be built from an
 * automaton. The vertex objects used are the states themselves. The analogy to
 * an edge is, naturally, transitions. This class of graph has a method to, once
 * graph vertices are moved around, to synchronize the locations of the
 * automaton states to the positions of the graph nodes, thus making graph
 * layout algorithms simpler to apply.
 *
 * @author Thomas Finley
 */
public class AutomatonGraph extends Graph<State> {
	/**
	 * Constructures a graph using an automaton.
	 *
	 * @param automaton
	 *            the automaton to build the graph from
	 */
	public AutomatonGraph(final Automaton automaton) {
		super();
		final List<State> states = automaton.getStates();
		final List<Transition> transitions = automaton.getTransitions();
		for (final State state : states) {
			addVertex(state, state.getPoint());
		}
		for (final Transition trans : transitions) {
			addEdge(trans.getFromState(), trans.getToState());
		}
	}

	/**
	 * Moves the states of the underlying automaton to synchronize with the
	 * positions of the corresponding vertices in the graph.
	 */
	public void moveAutomatonStates() {
		for (final State state : vertices()) {
			final Point2D point = pointForVertex(state);
			state.setPoint(new Point((int) point.getX(), (int) point.getY()));
		}
	}
}
