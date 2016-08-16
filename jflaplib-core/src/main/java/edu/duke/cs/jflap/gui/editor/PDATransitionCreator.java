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

import edu.duke.cs.jflap.automata.State;
import edu.duke.cs.jflap.automata.Transition;
import edu.duke.cs.jflap.automata.pda.PDATransition;
import edu.duke.cs.jflap.gui.viewer.AutomatonPane;

import com.google.common.collect.ImmutableList;

import java.util.List;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

/**
 * This is the creator of transitions in push down automata.
 *
 * @author Thomas Finley
 */
public class PDATransitionCreator extends TableTransitionCreator {
    /**
     * Instantiates a new <CODE>PDATransitionCreator</CODE>.
     *
     * @param parent
     *            the parent of whatever dialogs/windows get brought up by this
     *            creator
     */
    public PDATransitionCreator(AutomatonPane parent) {
        super(parent);
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
    protected Transition initTransition(State from, State to) {
        return new PDATransition(from, to, "", "", "");
    }

    /**
     * Creates a new table model.
     *
     * @param transition
     *            the transition to create the model for
     * @return a table model for the transition
     */
    @Override
    protected TableModel createModel(Transition transition) {
        final PDATransition t = (PDATransition) transition;
        return new AbstractTableModel() {
            /**
             *
             */
            private static final long serialVersionUID = 1L;

            @Override
            public Object getValueAt(int row, int column) {
                return s.get(column);
            }

            @Override
            public void setValueAt(Object o, int r, int c) {
                s.set(c, (String) o);
            }

            @Override
            public boolean isCellEditable(int r, int c) {
                return true;
            }

            @Override
            public int getRowCount() {
                return 1;
            }

            @Override
            public int getColumnCount() {
                return 3;
            }

            @Override
            public String getColumnName(int c) {
                return NAME.get(c);
            }

            List<String> s = ImmutableList.of(t.getInputToRead(), t.getStringToPop(),
                    t.getStringToPush());
        };
    }

    private static final List<String> NAME = ImmutableList.of("Read", "Pop", "Push");

    /**
     * Modifies a transition according to what's in the table.
     */
    @Override
    public Transition modifyTransition(Transition transition, TableModel model) {
        String input = (String) model.getValueAt(0, 0);
        String pop = (String) model.getValueAt(0, 1);
        String push = (String) model.getValueAt(0, 2);
        PDATransition t = (PDATransition) transition;
        try {
            return new PDATransition(t.getFromState(), t.getToState(), input, pop, push);
        } catch (IllegalArgumentException e) {
            reportException(e);
            return null;
        }
    }
}
