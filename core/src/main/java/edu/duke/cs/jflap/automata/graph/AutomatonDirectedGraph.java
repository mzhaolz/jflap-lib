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

import java.util.Set;

import edu.duke.cs.jflap.automata.Automaton;
import edu.duke.cs.jflap.automata.State;

/**
 * Constructs a graph of an automaton, but does not, for each edge, classify the
 * object to which an edge points as adjacent to the object from which the edge
 * points. It only classifies a "from" object as adjacent to a "to" object. The
 * default <code>degree()</code> method for this graph will return the number of
 * all edges leading from a vertex, including those pointing to itself.
 *
 * @author Chris Morgan
 */
public class AutomatonDirectedGraph extends AutomatonGraph {

	/**
	 * Constructs a directed graph using an automaton.
	 *
	 * @param automaton
	 *            the automaton to build the graph from
	 */
	public AutomatonDirectedGraph(final Automaton automaton) {
		super(automaton);
	}

	/**
	 * Adds an edge between two vertices. Overwrites
	 * <code>Graph.addEdge()</code>.
	 *
	 * @param from
	 *            the object from which this edge is pointing.
	 * @param to
	 *            the object to which this edge is pointing.
	 */
	@Override
	public void addEdge(final State from, final State to) {
		adjacent(from).add(to);
	}

	/**
	 * Returns the number of vertices that point from this object.
	 *
	 * @param from
	 *            the object to get the degree for.
	 * @param excludeSameVertexEdges
	 *            boolean that if <i>true</i> will exclude edges leading from
	 *            and to the same vertex from consideration.
	 */
	public int fromDegree(final State from, final boolean excludeSameVertexEdges) {
		if (!excludeSameVertexEdges) {
			return degree(from);
		}
		int count = 0;
		final Set<State> vertices = verticesToNeighbors.keySet();
		for (final State state : vertices) {
			if (hasEdge(from, state) && !state.equals(from)) {
				count++;
			}
		}
		return count;
	}

	/**
	 * Removes an edge between two vertices. Overwrites
	 * <code>Graph.removeEdge()</code>.
	 *
	 * @param from
	 *            the object from which this edge is pointing.
	 * @param to
	 *            the object to which this edge is pointing.
	 */
	@Override
	public void removeEdge(final State from, final State to) {
		adjacent(from).remove(to);
	}

	/**
	 * Returns the number of vertices that point to this object.
	 *
	 * @param to
	 *            the object to get the degree for.
	 * @param excludeSameVertexEdges
	 *            boolean that if <i>true</i> will exclude edges leading from
	 *            and to the same vertex from consideration.
	 */
	public int toDegree(final State to, final boolean excludeSameVertexEdges) {
		int count = 0;
		final Set<State> vertices = verticesToNeighbors.keySet();
		for (final State state : vertices) {
			if (hasEdge(state, to) && (!state.equals(to) || !excludeSameVertexEdges)) {
				count++;
			}
		}
		return count;
	}
}
