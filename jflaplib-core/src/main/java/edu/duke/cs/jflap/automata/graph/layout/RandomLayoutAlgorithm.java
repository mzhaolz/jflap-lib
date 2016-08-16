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
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import edu.duke.cs.jflap.automata.graph.Graph;
import edu.duke.cs.jflap.automata.graph.LayoutAlgorithm;

/**
 * This algorithm assigns all vertices to random points in the graph, while
 * applying a little effort taken to minimize edge intersections.
 *
 * @see LayoutAlgorithm
 * @author Chris Morgan
 */
public class RandomLayoutAlgorithm<V> extends LayoutAlgorithm<V> {
	/**
	 * A list of all movable vertices.
	 */
	private List<V> vertices;
	/**
	 * A list of all randomly generated points.
	 */
	private List<Point2D> points;
	/**
	 * The <code>VertexChain</code> used to minimize edge collision.
	 */
	private VertexChain<V> chain;

	/**
	 * Assigns some default values. To have different values, use the other
	 * constructor.
	 */
	public RandomLayoutAlgorithm() {
		super();
	}

	/**
	 * Constructor allowing the user to customize certain values. The
	 * <code>vertexDim</code> is not used in this algorithm, but it is here so
	 * the superclass constructor can be used and for the
	 * <code>LayoutAlgorithmFactory</code>.
	 *
	 * @param pSize
	 *            value for <code>size</code>.
	 * @param vDim
	 *            value for <code>vertexDim</code>.
	 * @param vBuffer
	 *            value for <code>vertexBuffer</code>.
	 */
	public RandomLayoutAlgorithm(final Dimension pSize, final Dimension vDim, final double vBuffer) {
		super(pSize, vDim, vBuffer);
	}

	/**
	 * This method creates random points and assigns all movable vertices to the
	 * VertexChain
	 */
	private void assignPointsAndVertices() {
		double x, y;
		final Random random = new Random();
		points = new ArrayList<>();
		for (int i = 0; i < vertices.size(); i++) {
			x = random.nextDouble() * (size.getWidth() - vertexBuffer * 2);
			y = random.nextDouble() * (size.getHeight() - vertexBuffer * 2);
			points.add(new Point2D.Double(x, y));
			chain.addVertex(vertices.get(i));
		}
	}

	/**
	 * The method reassigns the randomly generated points into a new order,
	 * placing them in an order such that the vertices assigned to them will
	 * spiral toward the center as one progresses through the chain.
	 */
	private void findCorrectPointOrder() {
		ArrayList<Point2D> notProcessedPoints, newPointOrder;
		Point2D current, anchor, minPoint;
		double currentTheta, minTheta, anchorTheta;

		anchor = new Point2D.Double(0, 0);
		anchorTheta = 0;
		newPointOrder = new ArrayList<>();
		notProcessedPoints = new ArrayList<>();
		notProcessedPoints.addAll(points);

		// Find the angle of all points relative to the last point placed and
		// "anchorTheta". Then place
		// the point with the minimum angle. "anchorTheta" will slowly rotate
		// around a circle counterclockwise.
		while (notProcessedPoints.size() > 0) {
			minPoint = notProcessedPoints.get(0);
			minTheta = 2 * Math.PI + 1;
			for (int i = 0; i < notProcessedPoints.size(); i++) {
				current = notProcessedPoints.get(i);

				if (current.getY() != anchor.getY()) {
					currentTheta = Math.atan((current.getX() - anchor.getX()) / (current.getY() - anchor.getY()));
				} else if (current.getX() > anchor.getX()) {
					currentTheta = Math.PI / 2;
				} else {
					currentTheta = Math.PI / -2;
				}

				/*
				 * atan -> -pi/2...pi/2. Adding 4pi to the currentTheta,
				 * subtracting the anchorTheta, and taking the remainder when
				 * dividing by pi works for all four quadrants the angle could
				 * be in. The object is to find the smallest absolute polar
				 * theta of the current point from the anchor which is greater
				 * than, or next in a counterclockwise traversal, from the
				 * anchorTheta.
				 */
				currentTheta = (currentTheta + 4 * Math.PI - anchorTheta) % (Math.PI);
				if (currentTheta < minTheta) {
					minTheta = currentTheta;
					minPoint = current;
				}
			}
			anchor = minPoint;
			anchorTheta = (anchorTheta + minTheta) % (2 * Math.PI);
			notProcessedPoints.remove(minPoint);
			newPointOrder.add(minPoint);
		}

		points = newPointOrder;
	}

	@Override
	public void layout(final Graph<V> graph, final Set<V> notMoving) {
		// First, check to see that movable vertices exist
		vertices = getMovableVertices(graph, notMoving);
		if (graph == null || vertices.size() == 0) {
			return;
		}

		// Then, generate random points and assign the vertices to a
		// VertexChain to minimize a few edge collisions
		chain = new VertexChain<>(graph);
		assignPointsAndVertices();

		// Then minimize vertex overlap.
		lessenVertexOverlap();

		// Next, find a more optimal point order with which to match the points
		// to the vertices.
		findCorrectPointOrder();

		// Finally, move all vertices to their corresponding points. Wrap up the
		// algorithm by
		// making sure all points are on the screen.
		for (int i = 0; i < points.size(); i++) {
			graph.moveVertex(chain.get(i), points.get(i));
		}
		shiftOntoScreen(graph, size, vertexDim, true);
	}

	/**
	 * This method shifts the random points away from each other, if needed, in
	 * order to minimize vertex overlap.
	 */
	private void lessenVertexOverlap() {
		// First, sort the vertices by their x and y values
		ArrayList<Point2D> xOrder, yOrder;
		xOrder = new ArrayList<>();
		yOrder = new ArrayList<>();
		xOrder.addAll(points);
		yOrder.addAll(points);
		xOrder.sort((o1, o2) -> {
			if (o1.getX() == o2.getX()) {
				return 0;
			} else if (o1.getX() < o2.getX()) {
				return 1;
			} else {
				return -1;
			}
		});
		yOrder.sort((o1, o2) -> {
			if (o1.getY() == o2.getY()) {
				return 0;
			} else if (o1.getY() < o2.getY()) {
				return 1;
			} else {
				return -1;
			}
		});

		// Then, shift over any points that need to be shifted over
		Point2D point;
		double xBuffer, yBuffer, xDiff, yDiff;
		xBuffer = vertexDim.getWidth() + vertexBuffer;
		yBuffer = vertexDim.getHeight() + vertexBuffer;
		for (int i = 0; i < vertices.size() - 1; i++) {
			xDiff = xOrder.get(i).getX() - xOrder.get(i + 1).getX();
			yDiff = xOrder.get(i).getY() - xOrder.get(i + 1).getY();
			if (xDiff < xBuffer && yDiff < yBuffer) {
				for (int j = i; j >= 0; j--) {
					point = xOrder.get(j);
					point.setLocation(point.getX() + xBuffer - xDiff, point.getY());
				}
			}
			xDiff = yOrder.get(i).getX() - yOrder.get(i + 1).getX();
			yDiff = yOrder.get(i).getY() - yOrder.get(i + 1).getY();
			if (xDiff < xBuffer && yDiff < yBuffer) {
				for (int j = i; j >= 0; j--) {
					point = yOrder.get(j);
					point.setLocation(point.getX(), point.getY() + yBuffer - yDiff);
				}
			}
		}
	}
}
