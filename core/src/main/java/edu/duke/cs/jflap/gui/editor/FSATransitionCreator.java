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

package edu.duke.cs.jflap.gui.editor;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import edu.duke.cs.jflap.automata.State;
import edu.duke.cs.jflap.automata.Transition;
import edu.duke.cs.jflap.automata.fsa.FSATransition;
import edu.duke.cs.jflap.gui.viewer.AutomatonPane;

/**
 * This is a transition creator for finite state automata.
 *
 * @author Thomas Finley
 */
public class FSATransitionCreator extends TableTransitionCreator {
	/**
	 * Instantiates a transition creator.
	 *
	 * @param parent
	 *            the parent object that any dialogs or windows brought up by
	 *            this creator should be the child of
	 */
	public FSATransitionCreator(final AutomatonPane parent) {
		super(parent);
	}

	/**
	 * Creates a new table model.
	 *
	 * @param transition
	 *            the transition to create the model for
	 */
	@Override
	protected TableModel createModel(final Transition transition) {
		final FSATransition t = (FSATransition) transition;
		return new AbstractTableModel() {
			/**
			 *
			 */
			private static final long serialVersionUID = 1L;

			String s = t.getLabel();

			@Override
			public int getColumnCount() {
				return 1;
			}

			@Override
			public String getColumnName(final int c) {
				return "Label";
			}

			@Override
			public int getRowCount() {
				return 1;
			}

			@Override
			public Object getValueAt(final int row, final int column) {
				return s;
			}

			@Override
			public boolean isCellEditable(final int r, final int c) {
				return true;
			}

			@Override
			public void setValueAt(final Object o, final int r, final int c) {
				s = (String) o;
			}
		};
	}

	/**
	 * Initializes a new empty transition.
	 *
	 * @param from
	 *            the from state
	 * @param to
	 *            to too state
	 */
	@Override
	protected Transition initTransition(final State from, final State to) {
		return new FSATransition(from, to, "");
	}

	/**
	 * Modifies a transition according to what's in the table.
	 */
	@Override
	public Transition modifyTransition(final Transition t, final TableModel model) {
		// EDebug.print("ModifyTransitionCalled");
		final String s = (String) model.getValueAt(0, 0);
		try {
			return new FSATransition(t.getFromState(), t.getToState(), s);
		} catch (final IllegalArgumentException e) {
			reportException(e);
			return null;
		}
	}
}
