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

import java.awt.Color;
import java.awt.Graphics;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.google.common.collect.Lists;

import edu.duke.cs.jflap.automata.Automaton;
import edu.duke.cs.jflap.automata.State;
import edu.duke.cs.jflap.automata.Transition;
import edu.duke.cs.jflap.automata.event.AutomataStateEvent;

/**
 * An extension of the <CODE>AutomatonDrawer</CODE> that allows the selection
 * (i.e. highlighting) of states.
 *
 * @author Thomas Finley
 */
public class SelectionDrawer extends AutomatonDrawer {
	/** The color to draw selected states in. */
	protected static final Color SELECTED_COLOR = StateDrawer.STATE_COLOR.darker().darker();

	/** The set of selected states, and the set of selected transitions. */
	private final Set<State> selected = new HashSet<>();

	private final Set<Transition> selectedTransitions = new HashSet<>();

	/** This set of listeners. */
	private final Set<ChangeListener> listeners = new HashSet<>();

	/**
	 * Instantiates a new selection drawer with no states selected.
	 *
	 * @param automaton
	 *            the automaton to select
	 */
	public SelectionDrawer(final Automaton automaton) {
		super(automaton);
	}

	/**
	 * Adds a change listener to this object that listens to changes in what is
	 * selected in this selection drawer.
	 *
	 * @param listener
	 *            the listener to add
	 */
	public void addChangeListener(final ChangeListener listener) {
		listeners.add(listener);
	}

	/**
	 * Adds a state to the selected states.
	 *
	 * @param state
	 *            the state to add
	 */
	public void addSelected(final State state) {
		// Automaton replacer = null;
		// if(state.getParentBlock()!=null){
		// replacer =
		// (Automaton)state.getAutomaton().getBlockMap().get(state.getParentBlock().getInternalName());
		// }
		// if (state.getAutomaton() != getAutomaton() && replacer !=
		// getAutomaton())
		// throw new IllegalArgumentException
		// ("State to select not in correct automaton!");
		if (!selected.contains(state)) {
			selected.add(state);
			distributeChangeEvent();
		}
	}

	/**
	 * Adds a transition to the selected transitions.
	 *
	 * @param transition
	 *            the transition to add
	 */
	public void addSelected(final Transition transition) {
		if (transition.getFromState().getAutomaton() != getAutomaton()) {
			throw new IllegalArgumentException("Transition to select not in correct automaton!");
		}
		if (!selectedTransitions.contains(transition)) {
			selectedTransitions.add(transition);
			distributeChangeEvent();
		}
	}

	/**
	 * Clears all selected states, so that there are no selected states.
	 */
	public void clearSelected() {
		if (selected.size() + selectedTransitions.size() > 0) {
			selected.clear();
			selectedTransitions.clear();
			distributeChangeEvent();
		}
	}

	/**
	 * Clears all selected transitions, so that there are no selected
	 * transitions.
	 */
	public void clearSelectedTransitions() {
		selectedTransitions.clear();
	}

	/**
	 * Distributes a <CODE>ChangeEvent</CODE> to all listeners when the
	 * selection has changed.
	 */
	protected void distributeChangeEvent() {
		final ChangeEvent e = new ChangeEvent(this);
		final Iterator<ChangeListener> it = listeners.iterator();
		while (it.hasNext()) {
			it.next().stateChanged(e);
		}
	}

	/**
	 * If a state is selected, draw it somewhat darker than the others. If it is
	 * not, then simply use the regular means for drawing a state.
	 *
	 * @param g
	 *            the graphics object to draw on
	 * @param state
	 *            the state to draw
	 */
	@Override
	public void drawState(final Graphics g, final State state) {
		if (selected.contains(state)) {
			getStateDrawer().drawState(g, getAutomaton(), state, state.getPoint(), SELECTED_COLOR);
			if (doesDrawStateLabels()) {
				getStateDrawer().drawStateLabel(g, state, state.getPoint(), StateDrawer.STATE_COLOR);
			}
		} else {
			super.drawState(g, state);
		}
	}

	/**
	 * Draws the transitions normally, then draws the highlight for the selected
	 * transitions.
	 *
	 * @param g
	 *            the graphics object to draw upon
	 */
	@Override
	protected void drawTransitions(final Graphics g) {
		final java.awt.Graphics2D g2 = (java.awt.Graphics2D) g;
		super.drawTransitions(g);
		final Iterator<Transition> it = selectedTransitions.iterator();
		while (it.hasNext()) {
			final Transition t = it.next();
			try {
				arrowForTransition(t).drawHighlight(g2);
			} catch (final NullPointerException e) {
				// Then this transition isn't in here.
			}
		}
	}

	/**
	 * Returns an array of the selected states.
	 *
	 * @return an array of the selected states
	 */
	public List<State> getSelected() {
		return Lists.newArrayList(selected);
	}

	/**
	 * Returns an array of the selected transitions.
	 *
	 * @return an array of the selected transitions
	 */
	public List<Transition> getSelectedTransitions() {
		return Lists.newArrayList(selectedTransitions);
	}

	/**
	 * Determines if a particular state is selected.
	 *
	 * @param state
	 *            the state to check for "selectedness"
	 * @return <CODE>true</CODE> if the state is selected, <CODE>false</CODE> if
	 *         it is not
	 */
	public boolean isSelected(final State state) {
		return selected.contains(state);
	}

	/**
	 * Determines if a particular transition is selected.
	 *
	 * @param transition
	 *            the transition to check for "selectedness"
	 * @return <CODE>true</CODE> if the transition is selected,
	 *         <CODE>false</CODE> if it is not
	 */
	public boolean isSelected(final Transition transition) {
		return selectedTransitions.contains(transition);
	}

	/**
	 * Returns the number of selected states.
	 *
	 * @return the number of selected states
	 */
	public int numberSelected() {
		return selected.size();
	}

	/**
	 * Returns the number of selected transitions.
	 *
	 * @return the number of selected transitions
	 */
	public int numberSelectedTransitions() {
		return selectedTransitions.size();
	}

	/**
	 * Removes a change listener from this object.
	 *
	 * @param listener
	 *            the listener to remove
	 */
	public void removeChangeListener(final ChangeListener listener) {
		listeners.remove(listener);
	}

	/**
	 * Removes the state from the selected states.
	 *
	 * @param state
	 *            the state to remove
	 */
	public void removeSelected(final State state) {
		if (selected.contains(state)) {
			selected.remove(state);
			distributeChangeEvent();
		}
	}

	/**
	 * Removes the transition from the selected transitions.
	 *
	 * @param transition
	 *            the transition to set as unselected
	 */
	public void removeSelected(final Transition transition) {
		if (selectedTransitions.contains(transition)) {
			selectedTransitions.remove(transition);
			distributeChangeEvent();
		}
	}

	/**
	 * Retrieves the set of selected states.
	 *
	 * @return the set of selected states
	 */
	protected Set<? extends Serializable> selected() {
		return selected;
	}

	/**
	 * Returns the set of selected transitions.
	 *
	 * @return the set of selected transitions
	 */
	protected Set<? extends Serializable> selectedTransitions() {
		return selectedTransitions;
	}

	/**
	 * Listens for changes in the states of the automaton. In the event that one
	 * has it checks the selected states.
	 *
	 * @param event
	 *            the state event
	 */
	@Override
	protected void stateChange(final AutomataStateEvent event) {
		if (event.isDelete()) {
			selected.remove(event.getState());
		}
		super.stateChange(event);
	}
}
