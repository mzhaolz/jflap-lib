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

import com.google.common.collect.Lists;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A graph data structure. The idea behind the graph data structure is that a
 * vertex is just some sort of data structure whose type is not important, and
 * associated with a point. There is therefore no explicit node structure.
 *
 * @author Thomas Finley
 */
public class Graph<T> {
    /** Creates a new empty graph structure. */
    public Graph() {
    }

    /** Clears all vertices and edges. */
    public void clear() {
        verticesToPoints.clear();
        verticesToNeighbors.clear();
    }

    /** Returns the degree of a vertex. */
    public int degree(T vertex) {
        return adjacent(vertex).size();
    }

    /** Returns the number of vertices. */
    public int numberOfVertices() {
        return verticesToPoints.size();
    }

    /** Returns the set of vertices a vertex is adjacent to. */
    public Set<T> adjacent(T from) {
        if (!verticesToNeighbors.containsKey(from))
            verticesToNeighbors.put(from, new HashSet<T>());
        return verticesToNeighbors.get(from);
    }

    /** Adds an edge between two vertices. */
    public void addEdge(T vertex1, T vertex2) {
        adjacent(vertex1).add(vertex2);
        adjacent(vertex2).add(vertex1);
    }

    /** Removes an edge between two vertices. */
    public void removeEdge(T vertex1, T vertex2) {
        adjacent(vertex1).remove(vertex2);
        adjacent(vertex2).remove(vertex1);
    }

    /** Returns if an edge exists between two vertices. */
    public boolean hasEdge(T vertex1, T vertex2) {
        return adjacent(vertex1).contains(vertex2);
    }

    /** Adds a vertex. */
    public void addVertex(T vertex, Point2D point) {
        verticesToPoints.put(vertex, (Point2D) point.clone());
    }

    /** Removes a vertex. */
    public void removeVertex(T vertex) {
        Set<T> others = adjacent(vertex);
        Iterator<T> it = others.iterator();
        while (it.hasNext())
            adjacent(it.next()).remove(vertex);
        verticesToNeighbors.remove(vertex);
        verticesToPoints.remove(vertex);
    }

    /** Moves a vertex to a new point. */
    public void moveVertex(T vertex, Point2D point) {
        addVertex(vertex, point);
    }

    /** Returns the point for a given vertex. */
    public Point2D pointForVertex(Object vertex) {
        return verticesToPoints.get(vertex);
    }

    /** Returns the list of vertex objects. */
    public List<T> vertices() {
        return Lists.newArrayList(verticesToPoints.keySet());
    }

    /**
     * Returns the list of vertex points. The order they appear is not
     * necessarily the same as the vertices.
     */
    public List<Point2D> points() {
        return Lists.newArrayList(verticesToPoints.values());
    }

    /** Reforms the points so they are enclosed within a certain frame. */
    public void moveWithinFrame(Rectangle2D bounds) {
        List<T> vertices = vertices();
        if (vertices.size() == 0)
            return;
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
            p = new Point2D.Double(
                    (p.getX() - minx) * bounds.getWidth() / (maxx - minx) + bounds.getX(),
                    (p.getY() - miny) * bounds.getHeight() / (maxy - miny) + bounds.getY());
            moveVertex(vertices.get(i), p);
        }
    }

    /** Returns a string description of the graph. */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(super.toString() + "\n");
        sb.append(verticesToPoints);
        return sb.toString();
    }

    protected Map<T, Point2D> verticesToPoints = new HashMap<T, Point2D>();

    protected Map<T, HashSet<T>> verticesToNeighbors = new HashMap<>();
}
