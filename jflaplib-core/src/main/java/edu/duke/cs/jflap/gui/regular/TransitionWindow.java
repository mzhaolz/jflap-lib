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

package edu.duke.cs.jflap.gui.regular;

import edu.duke.cs.jflap.automata.Transition;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

/**
 * This shows a bunch of transitions for the step of the conversion when the
 * states of the automaton are being removed one by one. A
 * {@link edu.duke.cs.jflap.gui.regular.FSAToREController} object is reported
 * back to when certain actions happen in the window.
 *
 * @see edu.duke.cs.jflap.gui.regular.FSAToREController#finalizeStateRemove
 * @see edu.duke.cs.jflap.gui.regular.FSAToREController#finalize
 *
 * @author Thomas Finley
 */
public class TransitionWindow extends JFrame {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new <CODE>TransitionWindow</CODE>.
     *
     * @param controller
     *            the FSA to RE controller object
     */
    public TransitionWindow(FSAToREController controller) {
        super("Transitions");
        this.controller = controller;
        // Init the GUI.
        setSize(250, 400);
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(new JLabel("Select to see what transitions were combined."),
                BorderLayout.NORTH);
        getContentPane().add(new JScrollPane(table), BorderLayout.CENTER);
        getContentPane().add(new JButton(new AbstractAction("Finalize") {
            /**
             *
             */
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                TransitionWindow.this.controller.finalizeStateRemove();
            }
        }), BorderLayout.SOUTH);
        // Have the listener to the transition.
        table.getSelectionModel().addListSelectionListener(e -> {
            if (table.getSelectedRowCount() != 1) {
                TransitionWindow.this.controller.tableTransitionSelected(null);
                return;
            }
            Transition t = transitions.get(table.getSelectedRow());
            TransitionWindow.this.controller.tableTransitionSelected(t);
            ;
        });
    }

    /**
     * Returns the transition this transition window displays.
     *
     * @return the array of transitions displayed by this window
     */
    public List<Transition> getTransitions() {
        return transitions;
    }

    /**
     * Sets the array of transitions the table in this window displays, and
     * shows the window.
     *
     * @param list
     *            the new array of transitions
     */
    public void setTransitions(List<Transition> list) {
        transitions = list;
        table.setModel(new TransitionTableModel(list));
    }

    /** The controller object for this window. */
    private FSAToREController controller;

    /** The array of transitions displayed. */
    private List<Transition> transitions = new ArrayList<>();

    /** The table object that displays the transitions. */
    private JTable table = new JTable(new TransitionTableModel());
}
