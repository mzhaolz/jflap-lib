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

import java.util.List;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import com.google.common.collect.ImmutableList;

import edu.duke.cs.jflap.automata.State;
import edu.duke.cs.jflap.automata.Transition;
import edu.duke.cs.jflap.automata.pda.PDATransition;
import edu.duke.cs.jflap.gui.viewer.AutomatonPane;

/**
 * This is the creator of transitions in push down automata.
 *
 * @author Thomas Finley
 */
public class PDATransitionCreator extends TableTransitionCreator {
	private static final List<String> NAME = ImmutableList.of("Read", "Pop", "Push");

	/**
	 * Instantiates a new <CODE>PDATransitionCreator</CODE>.
	 *
	 * @param parent
	 *            the parent of whatever dialogs/windows get brought up by this
	 *            creator
	 */
	public PDATransitionCreator(final AutomatonPane parent) {
		super(parent);
	}

	/**
	 * Creates a new table model.
	 *
	 * @param transition
	 *            the transition to create the model for
	 * @return a table model for the transition
	 */
	@Override
	protected TableModel createModel(final Transition transition) {
		final PDATransition t = (PDATransition) transition;
		return new AbstractTableModel() {
			/**
			 *
			 */
			private static final long serialVersionUID = 1L;

			List<String> s = ImmutableList.of(t.getInputToRead(), t.getStringToPop(), t.getStringToPush());

			@Override
			public int getColumnCount() {
				return 3;
			}

			@Override
			public String getColumnName(final int c) {
				return NAME.get(c);
			}

			@Override
			public int getRowCount() {
				return 1;
			}

			@Override
			public Object getValueAt(final int row, final int column) {
				return s.get(column);
			}

			@Override
			public boolean isCellEditable(final int r, final int c) {
				return true;
			}

			@Override
			public void setValueAt(final Object o, final int r, final int c) {
				s.set(c, (String) o);
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
		return new PDATransition(from, to, "", "", "");
	}

	/**
	 * Modifies a transition according to what's in the table.
	 */
	@Override
	public Transition modifyTransition(final Transition transition, final TableModel model) {
		final String input = (String) model.getValueAt(0, 0);
		final String pop = (String) model.getValueAt(0, 1);
		final String push = (String) model.getValueAt(0, 2);
		final PDATransition t = (PDATransition) transition;
		try {
			return new PDATransition(t.getFromState(), t.getToState(), input, pop, push);
		} catch (final IllegalArgumentException e) {
			reportException(e);
			return null;
		}
	}
}
