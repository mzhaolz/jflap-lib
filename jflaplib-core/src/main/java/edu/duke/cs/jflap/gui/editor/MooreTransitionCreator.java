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
import edu.duke.cs.jflap.automata.mealy.MooreTransition;
import edu.duke.cs.jflap.gui.viewer.AutomatonPane;

/**
 * This is a transition creator for Moore machines.
 *
 * @author Jinghui Lim
 *
 */
public class MooreTransitionCreator extends MealyTransitionCreator {
	/**
	 * Column title.
	 */
	private static final String NAME = "Label";

	/**
	 * Instantiates a new transition creator.
	 *
	 * @param parent
	 *            the parent object that any dialogs or windows brought up by
	 *            this creator should be the child of
	 */
	public MooreTransitionCreator(final AutomatonPane parent) {
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
		final MooreTransition t = (MooreTransition) transition;
		return new AbstractTableModel() {
			/**
			 *
			 */
			private static final long serialVersionUID = 1L;
			String s[] = new String[] { t.getLabel() };

			@Override
			public int getColumnCount() {
				return 1;
			}

			@Override
			public String getColumnName(final int c) {
				return NAME;
			}

			@Override
			public int getRowCount() {
				return 1;
			}

			@Override
			public Object getValueAt(final int r, final int c) {
				return s[c];
			}

			@Override
			public boolean isCellEditable(final int r, final int c) {
				return true;
			}

			@Override
			public void setValueAt(final Object o, final int r, final int c) {
				s[c] = (String) o;
			}
		};
	}

	/**
	 * Initializes an empty transition.
	 *
	 * @param from
	 *            the from state
	 * @param to
	 *            the to state
	 */
	@Override
	protected Transition initTransition(final State from, final State to) {
		return new MooreTransition(from, to, "");
	}

	/**
	 * Modifies a transition according to what is in the table.
	 *
	 * @param transition
	 *            transition to modify
	 * @param model
	 *            table to get information from
	 */
	@Override
	public Transition modifyTransition(final Transition transition, final TableModel model) {
		final String label = (String) model.getValueAt(0, 0);
		final MooreTransition t = (MooreTransition) transition;
		try {
			return new MooreTransition(t.getFromState(), t.getToState(), label);
		} catch (final IllegalArgumentException e) {
			reportException(e);
			return null;
		}
	}
}
