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

package edu.duke.cs.jflap.automata.graph.layout;

import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import edu.duke.cs.jflap.automata.graph.Graph;
import edu.duke.cs.jflap.automata.graph.LayoutAlgorithm;

/**
 * This algorithm places all vertices of the graph in a spiral, while applying a
 * little effort taken to minimize edge intersections.
 *
 * @see LayoutAlgorithm
 * @author Chris Morgan
 */
public class SpiralLayoutAlgorithm<V> extends LayoutAlgorithm<V> {
	/**
	 * The graph used for this LayoutAlgorithm
	 */
	private Graph<V> graph;

	/**
	 * Assigns some default values. To have different values, use the other
	 * constructor.
	 */
	public SpiralLayoutAlgorithm() {
		super();
	}

	/**
	 * Constructor allowing the user to customize certain values.
	 *
	 * @param pSize
	 *            value for <code>size</code>.
	 * @param vDim
	 *            value for <code>vertexDim</code>.
	 * @param vBuffer
	 *            value for <code>vertexBuffer</code>.
	 */
	public SpiralLayoutAlgorithm(final Dimension pSize, final Dimension vDim, final double vBuffer) {
		super(pSize, vDim, vBuffer);
	}

	@Override
	public void layout(final Graph<V> g, final Set<V> notMoving) {
		graph = g;
		final List<V> vertices = getMovableVertices(graph, notMoving);
		if (graph == null || vertices.size() == 0) {
			return;
		}

		/*
		 * After checking to see if the graph has movable vertices, sort the
		 * vertices degree in the graph, and then insert them in a VertexChain
		 * to minimize edge intersections.
		 */
		final VertexChain<V> chain = new VertexChain<>(graph);
		Collections.sort(vertices, (o1, o2) -> {
			if (graph.degree(o1) == graph.degree(o2)) {
				return 0;
			} else if (graph.degree(o1) > graph.degree(o2)) {
				return -1;
			} else {
				return 1;
			}
		});
		for (int i = 0; i < vertices.size(); i++) {
			chain.addVertex(vertices.get(i));
		}

		/*
		 * Now, calculate the polar coordinate positions of each element in the
		 * vertex, ordering them by their order in the chain. "posShift" is the
		 * distance between vertices next to each other in the chain.
		 */
		double r, theta, posShift;
		r = 0;
		theta = 0;
		posShift = (Math.sqrt(Math.pow(vertexDim.getHeight(), 2) + Math.pow(vertexDim.getWidth(), 2))) + vertexBuffer;
		for (int i = 0; i < vertices.size(); i++) {
			r = Math.sqrt(Math.pow(r, 2) + Math.pow(posShift, 2));
			theta = theta + Math.asin(posShift / r);
			graph.moveVertex(chain.get(i), new Point2D.Double(r, theta));
		}

		// Finally, convert the points to Cartesian points, and make sure they
		// all fit onto the screen
		polarToCartesian(graph, vertices);
		shiftOntoScreen(graph, size, vertexDim, true);
	}
}
