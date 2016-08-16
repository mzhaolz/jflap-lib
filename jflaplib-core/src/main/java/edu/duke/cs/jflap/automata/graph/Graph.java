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

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;

/**
 * A graph data structure. The idea behind the graph data structure is that a
 * vertex is just some sort of data structure whose type is not important, and
 * associated with a point. There is therefore no explicit node structure.
 *
 * @author Thomas Finley
 */
public class Graph<T> {
	protected Map<T, Point2D> verticesToPoints = new HashMap<>();

	protected Map<T, HashSet<T>> verticesToNeighbors = new HashMap<>();

	/** Creates a new empty graph structure. */
	public Graph() {
	}

	/** Adds an edge between two vertices. */
	public void addEdge(final T vertex1, final T vertex2) {
		adjacent(vertex1).add(vertex2);
		adjacent(vertex2).add(vertex1);
	}

	/** Adds a vertex. */
	public void addVertex(final T vertex, final Point2D point) {
		verticesToPoints.put(vertex, (Point2D) point.clone());
	}

	/** Returns the set of vertices a vertex is adjacent to. */
	public Set<T> adjacent(final T from) {
		if (!verticesToNeighbors.containsKey(from)) {
			verticesToNeighbors.put(from, new HashSet<T>());
		}
		return verticesToNeighbors.get(from);
	}

	/** Clears all vertices and edges. */
	public void clear() {
		verticesToPoints.clear();
		verticesToNeighbors.clear();
	}

	/** Returns the degree of a vertex. */
	public int degree(final T vertex) {
		return adjacent(vertex).size();
	}

	/** Returns if an edge exists between two vertices. */
	public boolean hasEdge(final T vertex1, final T vertex2) {
		return adjacent(vertex1).contains(vertex2);
	}

	/** Moves a vertex to a new point. */
	public void moveVertex(final T vertex, final Point2D point) {
		addVertex(vertex, point);
	}

	/** Reforms the points so they are enclosed within a certain frame. */
	public void moveWithinFrame(final Rectangle2D bounds) {
		final List<T> vertices = vertices();
		if (vertices.size() == 0) {
			return;
		}
		Point2D p = pointForVertex(vertices.get(0));
		double minx = p.getX(), miny = p.getY(), maxx = minx, maxy = miny;
		for (int i = 1; i < vertices.size(); i++) {
			p = pointForVertex(vertices.get(i));
			minx = Math.min(minx, p.getX());
			miny = Math.min(miny, p.getY());
			maxx = Math.max(maxx, p.getX());
			maxy = Math.max(maxy, p.getY());
		}
		// Now, scale them!
		for (int i = 0; i < vertices.size(); i++) {
			p = pointForVertex(vertices.get(i));
			p = new Point2D.Double((p.getX() - minx) * bounds.getWidth() / (maxx - minx) + bounds.getX(),
					(p.getY() - miny) * bounds.getHeight() / (maxy - miny) + bounds.getY());
			moveVertex(vertices.get(i), p);
		}
	}

	/** Returns the number of vertices. */
	public int numberOfVertices() {
		return verticesToPoints.size();
	}

	/** Returns the point for a given vertex. */
	public Point2D pointForVertex(final Object vertex) {
		return verticesToPoints.get(vertex);
	}

	/**
	 * Returns the list of vertex points. The order they appear is not
	 * necessarily the same as the vertices.
	 */
	public List<Point2D> points() {
		return Lists.newArrayList(verticesToPoints.values());
	}

	/** Removes an edge between two vertices. */
	public void removeEdge(final T vertex1, final T vertex2) {
		adjacent(vertex1).remove(vertex2);
		adjacent(vertex2).remove(vertex1);
	}

	/** Removes a vertex. */
	public void removeVertex(final T vertex) {
		final Set<T> others = adjacent(vertex);
		final Iterator<T> it = others.iterator();
		while (it.hasNext()) {
			adjacent(it.next()).remove(vertex);
		}
		verticesToNeighbors.remove(vertex);
		verticesToPoints.remove(vertex);
	}

	/** Returns a string description of the graph. */
	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer();
		sb.append(super.toString() + "\n");
		sb.append(verticesToPoints);
		return sb.toString();
	}

	/** Returns the list of vertex objects. */
	public List<T> vertices() {
		return Lists.newArrayList(verticesToPoints.keySet());
	}
}
