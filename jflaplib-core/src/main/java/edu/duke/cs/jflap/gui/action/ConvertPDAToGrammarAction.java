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

package edu.duke.cs.jflap.gui.action;

import edu.duke.cs.jflap.automata.Automaton;
import edu.duke.cs.jflap.automata.State;
import edu.duke.cs.jflap.automata.Transition;
import edu.duke.cs.jflap.automata.pda.PDATransition;
import edu.duke.cs.jflap.automata.pda.PushdownAutomaton;
import edu.duke.cs.jflap.gui.environment.AutomatonEnvironment;
import edu.duke.cs.jflap.gui.environment.EnvironmentFrame;
import edu.duke.cs.jflap.gui.environment.Universe;
import edu.duke.cs.jflap.gui.grammar.automata.ConvertController;
import edu.duke.cs.jflap.gui.grammar.automata.ConvertPane;
import edu.duke.cs.jflap.gui.grammar.automata.PDAConvertController;
import edu.duke.cs.jflap.gui.viewer.SelectionDrawer;
import edu.duke.cs.jflap.gui.viewer.ZoomPane;

import java.awt.BorderLayout;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

/**
 * This action handles the conversion of an PDA to a context free grammar.
 *
 * @author Thomas Finley
 */
public class ConvertPDAToGrammarAction extends ConvertAutomatonToGrammarAction {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new <CODE>ConvertFSAToGrammarAction</CODE>.
     *
     * @param environment
     *            the environment
     */
    public ConvertPDAToGrammarAction(AutomatonEnvironment environment) {
        super(environment);
    }

    /**
     * Checks the PDA to make sure it's ready to be converted.
     */
    @Override
    protected boolean checkAutomaton() {
        EnvironmentFrame frame = Universe.frameForEnvironment(getEnvironment());
        JPanel messagePanel = new JPanel(new BorderLayout());
        SelectionDrawer drawer = new SelectionDrawer(getAutomaton());
        JLabel messageLabel = new JLabel();
        ZoomPane zoom = new ZoomPane(drawer);
        JPanel tempPanel = new JPanel(new BorderLayout());
        tempPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
        zoom.setPreferredSize(new java.awt.Dimension(300, 200));
        tempPanel.add(zoom, BorderLayout.CENTER);
        messagePanel.add(tempPanel, BorderLayout.CENTER);
        messagePanel.add(messageLabel, BorderLayout.SOUTH);
        // Check the final states.
        List<State> finalStates = getAutomaton().getFinalStates();
        if (finalStates.size() != 1) {
            JOptionPane.showMessageDialog(frame, "There must be exactly one final state!",
                    "Final State Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        // Are all transitions to the final state okay?
        List<Transition> toFinal = getAutomaton().getTransitionsToState(finalStates.get(0));
        HashSet<PDATransition> bad = new HashSet<>();
        for (int i = 0; i < toFinal.size(); i++) {
            PDATransition t = (PDATransition) toFinal.get(i);
            if (!t.getStringToPop().equals("Z")) {
                bad.add(t);
            }
        }
        if (bad.size() != 0) {
            drawer.clearSelected();
            Iterator<PDATransition> it = bad.iterator();
            while (it.hasNext()) {
                drawer.addSelected(it.next());
            }
            messageLabel.setText("Transitions to final must pop only 'Z'.");
            JOptionPane.showMessageDialog(frame, messagePanel, "Final Transitions Error",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
        // Are the transitions okay?
        List<Transition> transitions = getAutomaton().getTransitions();
        bad.clear();
        for (int i = 0; i < transitions.size(); i++) {
            PDATransition t = (PDATransition) transitions.get(i);
            if ( // t.getInputToRead().length() != 1 ||
            t.getStringToPop().length() != 1
                    || (t.getStringToPush().length() != 2 && t.getStringToPush().length() != 0)) {
                bad.add(t);
            }
        }
        if (bad.size() != 0) {
            drawer.clearSelected();
            Iterator<PDATransition> it = bad.iterator();
            while (it.hasNext()) {
                drawer.addSelected(it.next());
            }
            messageLabel.setText("Transitions must pop 1 and push 0 or 2.");
            JOptionPane.showMessageDialog(frame, messagePanel, "Transitions Error",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    /**
     * This object is only applicable to pushdown automatons.
     *
     * @param object
     *            the object to test
     * @return <CODE>true</CODE> if the object is a pushdown automaton,
     *         <CODE>false</CODE> otherwise
     */
    public static boolean isApplicable(Object object) {
        return object instanceof PushdownAutomaton;
    }

    /**
     * Initializes the convert controller.
     *
     * @param pane
     *            the convert pane that holds the automaton pane and the grammar
     *            table
     * @param drawer
     *            the selection drawer of the new view
     * @param automaton
     *            the automaton that's being converted; note that this will not
     *            be the exact object returned by <CODE>getAutomaton</CODE>
     *            since a clone is made
     * @return the convert controller to handle the conversion of the automaton
     *         to a grammar
     */
    @Override
    protected ConvertController initializeController(ConvertPane pane, SelectionDrawer drawer,
            Automaton automaton) {
        return new PDAConvertController(pane, drawer, (PushdownAutomaton) automaton);
    }
}
