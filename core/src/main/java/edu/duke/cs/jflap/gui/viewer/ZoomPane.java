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

package edu.duke.cs.jflap.gui.viewer;

import java.awt.Rectangle;
import java.util.List;

import edu.duke.cs.jflap.automata.State;
import edu.duke.cs.jflap.automata.Transition;

/**
 * This variant of automaton pane is meant to draw the automaton only with a
 * <CODE>SelectionDrawer</CODE> that restricts the display to what is selected
 * (plus a little extra for padding).
 *
 * @author Thomas Finley
 */
public class ZoomPane extends AutomatonPane {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Instantiates a <CODE>ZoomPane</CODE> for a given
	 * <CODE>SelectionDrawer</CODE>.
	 *
	 * @param drawer
	 *            the selection drawer
	 */
	public ZoomPane(final SelectionDrawer drawer) {
		super(drawer);
		drawer.addChangeListener(e -> transform = null);
	}

	/**
	 * Returns the bounds for the section of the automaton that should be drawn.
	 * In the case of this object this will be restricted to those objects which
	 * are restricted. If no objects are selected, then the default superclass
	 * bounds are returned
	 *
	 * @return the bounds for this zoom pane
	 */
	@Override
	protected Rectangle getAutomatonBounds() {
		final SelectionDrawer d = (SelectionDrawer) drawer;
		final List<State> s = d.getSelected();
		final List<Transition> t = d.getSelectedTransitions();
		// What if nothing is selected?
		if (s.size() + t.size() == 0) {
			return super.getAutomatonBounds();
		}

		Rectangle rect = null;
		if (s.size() != 0) {
			rect = d.getBounds(s.get(0));
			for (final State state : s) {
				rect.add(d.getBounds(state));
			}
		} else {
			rect = d.getBounds(t.get(0));
		}
		for (final Transition element : t) {
			rect.add(d.getBounds(element));
		}
		return rect;
	}
}
