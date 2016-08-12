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

import edu.duke.cs.jflap.automata.State;
import edu.duke.cs.jflap.automata.fsa.FSATransition;
import edu.duke.cs.jflap.automata.fsa.FiniteStateAutomaton;
import edu.duke.cs.jflap.gui.environment.FrameFactory;
import edu.duke.cs.jflap.gui.environment.Universe;
import edu.duke.cs.jflap.regular.Discretizer;

import com.google.common.collect.Lists;

import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.JOptionPane;

/**
 * The regular expression to FSA controller moderates the conversion of a
 * regular expression to an FSA controller.
 *
 * @author Thomas Finley
 */
public class REToFSAController {
    /**
     * Instantiates a new controller.
     *
     * @param pane
     *            the RE to FSA converter pane
     * @param automaton
     *            the fsa being built
     */
    public REToFSAController(ConvertToAutomatonPane pane, FiniteStateAutomaton automaton) {
        convertPane = pane;
        this.automaton = automaton;
        FSATransition t = (FSATransition) automaton.getTransitions().get(0);
        if (requiredAction(t.getLabel()) != 0) {
            toDo.add(t);
        }
        convertPane.exportAction.setEnabled(false);
        nextStep();
    }

    /**
     * This will return the action that are necessary for a given subexpression.
     * If this method returns 0, that indicates that no action is required.
     *
     * @param expression
     *            the expression to check for actions that may be required
     */
    private int requiredAction(String expression) {
        if (expression.length() <= 1) {
            return 0;
        }
        if (Discretizer.or(expression).size() > 1) {
            return DEOR;
        }
        if (Discretizer.cat(expression).size() > 1) {
            return DECAT;
        }
        if (expression.charAt(expression.length() - 1) == '*') {
            return DESTAR;
        }
        if (expression.charAt(0) == '(' && expression.charAt(expression.length() - 1) == ')') {
            return DEPARENS;
        }
        throw new IllegalArgumentException("Expression " + expression + " not recognized!");
    }

    /**
     * Given a transition to replace, and a list of the strings the transition
     * is being broken into, modify the automaton so that that transition is
     * replaced with a sequence of transitions each corresponding to the array.
     *
     * @param transition
     *            the transition to replace
     * @param list
     *            the array of string expressions to replace the transition with
     * @return the array of transitions created
     */
    private List<FSATransition> replaceTransition(FSATransition transition, List<String> list) {
        // Compose the transform.
        AffineTransform at = new AffineTransform();
        Point pStart = transition.getFromState().getPoint();
        Point pEnd = transition.getToState().getPoint();
        at.translate(pStart.x, pStart.y);
        at.scale(pStart.distance(pEnd), pStart.distance(pEnd));
        at.rotate(Math.atan2(pEnd.y - pStart.y, pEnd.x - pStart.x));

        List<FSATransition> t = new ArrayList<>();
        Point2D.Double ps = new Point2D.Double(0.2, 0.0);
        Point2D.Double pe = new Point2D.Double(0.8, 0.0);
        automaton.removeTransition(transition);
        for (int i = 0; i < list.size(); i++) {
            pStart = new Point();
            pEnd = new Point();
            double y = list.size() > 1 ? (i / (list.size() - 1.0) - 0.5) * 0.5 : 0.0;
            pe.y = ps.y = y;
            at.transform(ps, pStart);
            at.transform(pe, pEnd);
            // Clamp bounds.
            pStart.x = Math.max(pStart.x, 20);
            pStart.y = Math.max(pStart.y, 20);
            pEnd.x = Math.max(pEnd.x, 20);
            pEnd.y = Math.max(pEnd.y, 20);
            State s = automaton.createState(pStart);
            State e = automaton.createState(pEnd);
            t.add(new FSATransition(s, e, list.get(i)));
            automaton.addTransition(t.get(i));
            if (requiredAction(t.get(i).getLabel()) != 0) {
                toDo.add(t.get(i));
            }
        }
        return t;
    }

    /**
     * Called when a transition is selected with the deexpressionifier tool.
     *
     * @param transition
     *            the transition that was clicked
     */
    public void transitionCheck(FSATransition transition) {
        if (action != 0) {
            JOptionPane.showMessageDialog(convertPane,
                    "We're already in the process of\n" + "deexpressionifying a transition.",
                    "Already Active", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if ((action = requiredAction(transition.getLabel())) == 0) {
            JOptionPane.showMessageDialog(convertPane, "That's as good as it gets.",
                    "No Action Necessary", JOptionPane.ERROR_MESSAGE);
            return;
        }
        this.transition = transition;
        toDo.remove(transition);
        String label = transition.getLabel();
        switch (action) {
            case DEPARENS: {
                State s1 = transition.getFromState(), s2 = transition.getToState();
                String newLabel = Discretizer.delambda(label.substring(1, label.length() - 1));
                automaton.removeTransition(transition);
                FSATransition t = new FSATransition(s1, s2, newLabel);
                automaton.addTransition(t);
                if (requiredAction(newLabel) != 0) {
                    toDo.add(t);
                }
                action = 0; // That's all that need be done.
                break;
            }
            case DESTAR:
                replacements = replaceTransition(transition, Lists.newArrayList(
                        Discretizer.delambda(label.substring(0, label.length() - 1))));
                transitionNeeded = 4;
                break;
            case DEOR:
                replacements = replaceTransition(transition, Discretizer.or(label));
                transitionNeeded = 2 * replacements.size();
                break;
            case DECAT:
                replacements = replaceTransition(transition, Discretizer.cat(label));
                transitionNeeded = replacements.size() + 1;
                break;
        }
        nextStep();
    }

    /**
     * Creates a lambda-transition between two states.
     *
     * @param from
     *            the from state
     * @param to
     *            the to state
     * @return a lambda-transition between those states
     */
    private FSATransition lambda(State from, State to) {
        return new FSATransition(from, to, "");
    }

    /**
     * Does a step.
     */
    public void completeStep() {
        if (action == 0) {
            Iterator<FSATransition> it = toDo.iterator();
            FSATransition t = it.next();
            transitionCheck(t);
        }
        State from = transition.getFromState();
        State to = transition.getToState();
        switch (action) {
            case DEPARENS:
                // Probably a deparenthesization, or whatever.
                return;
            case DEOR:
                for (int i = 0; i < replacements.size(); i++) {
                    automaton.addTransition(lambda(from, replacements.get(i).getFromState()));
                    automaton.addTransition(lambda(replacements.get(i).getToState(), to));
                }
                break;
            case DECAT:
                automaton.addTransition(lambda(from, replacements.get(0).getFromState()));
                for (int i = 0; i < replacements.size() - 1; i++) {
                    automaton.addTransition(lambda(replacements.get(i).getToState(),
                            replacements.get(i + 1).getFromState()));
                }
                automaton.addTransition(
                        lambda(replacements.get(replacements.size() - 1).getToState(), to));
                break;
            case DESTAR:
                automaton.addTransition(lambda(from, replacements.get(0).getFromState()));
                automaton.addTransition(lambda(replacements.get(0).getToState(), to));
                automaton.addTransition(lambda(from, to));
                automaton.addTransition(lambda(to, from));
                break;
        }
        transitionNeeded = 0;
        nextStep();
    }

    /**
     * Does everything.
     */
    public void completeAll() {
        while (action != 0 || toDo.size() > 0) {
            completeStep();
        }
    }

    /**
     * Exports the automaton.
     */
    public void export() {
        FrameFactory.createFrame((FiniteStateAutomaton) automaton.clone());
    }

    /**
     * Exports the automaton to a tab.
     */
    public void exportToTab() {
        edu.duke.cs.jflap.gui.viewer.AutomatonPane p = new edu.duke.cs.jflap.gui.viewer.AutomatonPane(
                automaton);
        convertPane.environment.add(p, "Current FA");
        convertPane.environment.setActive(p);
    }

    /**
     * Called when the transition tool is used to create a transition between
     * two states.
     *
     * @param from
     *            the from state
     * @param to
     *            the to state
     */
    public void transitionCreate(State from, State to) {
        boolean alreadyHere = automaton.getTransitionsFromStateToState(from, to).size() != 0;
        boolean valid = false;
        switch (action) {
            case 0:
                JOptionPane.showMessageDialog(convertPane,
                        "A transition must be selected with\n"
                                + "the deexpressionifier tool first.",
                        "Invalid Action", JOptionPane.ERROR_MESSAGE);
                return;
            case DEOR:
                if (from == transition.getFromState()) {
                    for (int i = 0; i < replacements.size(); i++) {
                        if (replacements.get(i).getFromState() == to) {
                            valid = true;
                            break;
                        }
                    }
                }
                if (to == transition.getToState()) {
                    for (int i = 0; i < replacements.size(); i++) {
                        if (replacements.get(i).getToState() == from) {
                            valid = true;
                            break;
                        }
                    }
                }
                break;
            case DECAT:
                if (automaton.getTransitionsFromState(from).size() > 0
                        || automaton.getTransitionsToState(to).size() > 0) {
                    if (alreadyHere) {
                        valid = true; // Let someone take care of it.
                    }
                    break;
                }
                int index1 = -1, index2 = -1;
                for (int i = 0; i < replacements.size(); i++) {
                    if (replacements.get(i).getToState() == from) {
                        index1 = i;
                    }
                    if (replacements.get(i).getFromState() == to) {
                        index2 = i;
                    }
                }
                int step = replacements.size() + 1 - transitionNeeded;
                if (index1 == -1 && from != transition.getFromState()) {
                    break;
                }
                if (index2 == -1 && to != transition.getToState()) {
                    break;
                }
                // If last one, make sure right state.
                if (index2 == -1) {
                    if (step != replacements.size()) {
                        JOptionPane.showMessageDialog(convertPane,
                                "That may be correct, but the transitions\n"
                                        + "must be connected in order.",
                                "Out of Order", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    valid = from == replacements.get(replacements.size() - 1).getToState();
                    break;
                }
                // ////System.out.println(index1+", "+index2+", "+step);
                // Make sure this is the right transition.
                if ((step == 0 && index1 != -1)
                        || (step > 0 && replacements.get(step - 1).getToState() != from)) {
                    JOptionPane.showMessageDialog(convertPane,
                            "That may be correct, but the transitions\n"
                                    + "must be connected in order.",
                            "Out of Order", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                // Handle internal cases.
                if (replacements.get(index2).getLabel().equals(replacements.get(step).getLabel())) {
                    FSATransition t = replacements.get(step);
                    replacements.set(step, replacements.get(index2));
                    replacements.set(index2, t);
                    valid = true;
                } else {
                    valid = false;
                }
                break;
            case DESTAR:
                if (to == transition.getToState() && from == transition.getFromState()) {
                    valid = true;
                } else if (from == transition.getToState() && to == transition.getFromState()) {
                    valid = true;
                } else if (to == transition.getToState()
                        && from == replacements.get(0).getToState()) {
                    valid = true;
                } else if (from == transition.getFromState()
                        && to == replacements.get(0).getFromState()) {
                    valid = true;
                }
                break;
        }

        if (!valid) {
            JOptionPane.showMessageDialog(convertPane, "A transition there is invalid.",
                    "Bad Transition", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (alreadyHere) {
            JOptionPane.showMessageDialog(convertPane, "A transition exists here.",
                    "Transition Already Exists", JOptionPane.ERROR_MESSAGE);
            return;
        }
        FSATransition t = new FSATransition(from, to, "");
        automaton.addTransition(t);
        /*
         * if (!toDoTransitions.remove(t)) { System.err.println ("BADNESS!
         * Transition valid, but not confirmed."); }
         */
        transitionNeeded--;

        nextStep();
    }

    private void nextStep() {
        if (transitionNeeded == 0) {
            if (toDo.size() > 0) {
                if (action != 0) {
                    convertPane.mainLabel.setText("Resolution complete.");
                } else {
                    convertPane.mainLabel.setText("Welcome to the converter.");
                }
                convertPane.detailLabel.setText(toDo.size() + " more resolutions needed.");
                action = 0;
                return;
            }
            action = 0;
            // We're all done.
            convertPane.mainLabel.setText("The automaton is complete.");
            convertPane.detailLabel.setText("\"Export\" will put it in a new window.");
            convertPane.exportAction.setEnabled(true);
            convertPane.doStepAction.setEnabled(false);
            convertPane.doAllAction.setEnabled(false);
            return;
        }

        convertPane.detailLabel.setText(transitionNeeded + " more "
                + Universe.curProfile.getEmptyString() + "-transitions needed.");
        switch (action) {
            case DEOR:
                convertPane.mainLabel.setText("De-oring " + transition.getLabel());
                break;
            case DECAT:
                convertPane.mainLabel.setText("De-concatenating " + transition.getLabel());
                break;
            case DESTAR:
                convertPane.mainLabel.setText("De-staring " + transition.getLabel());
                break;
        }
    }

    /** The conversion pane. */
    private ConvertToAutomatonPane convertPane;

    /** The FSA being built. */
    private FiniteStateAutomaton automaton;

    /** The set of transitions that still require expansion. */
    private Set<FSATransition> toDo = new HashSet<>();

    /** The current action, or 0 if no action. */
    private int action = 0;

    /** The transition being replaced. */
    private FSATransition transition = null;

    /** The number of transitions needed for the current step. */
    private int transitionNeeded = 0;

    /** The replacement transitions. */
    private List<FSATransition> replacements = null;

    /**
     * The codes for actions on an expression, which are, in order, that
     * parentheses are to be taken out, that an expression is to be "destarred",
     * or that ors or concatenations are to be broken into their respective
     * parts.
     */
    private static final int DEPARENS = 1, DESTAR = 2, DEOR = 3, DECAT = 4;
}
