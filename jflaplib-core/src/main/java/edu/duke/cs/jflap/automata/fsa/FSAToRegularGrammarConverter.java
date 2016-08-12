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

package edu.duke.cs.jflap.automata.fsa;

import edu.duke.cs.jflap.automata.Automaton;
import edu.duke.cs.jflap.automata.State;
import edu.duke.cs.jflap.automata.Transition;
import edu.duke.cs.jflap.grammar.Production;
import edu.duke.cs.jflap.grammar.reg.RegularGrammar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * The FSA to regular grammar converter can be used to convert a finite state
 * automaton into an equivalent regular grammar. The fsa and grammar will be
 * equivalent in that they will accept exactly the same language. In order to
 * use this converter you must first call initializeConverter to map the states
 * in the fsa to variables in the regular grammar. Then you can perform the
 * conversion simply by calling convertToRegularGrammar, or you can perform the
 * conversion step by step by repeatedly calling getProductionForTransition for
 * every transition in the automaton and adding the returned productions to the
 * grammar. If you do this for every Transition in the fsa, you will have an
 * equivalent regular grammar.
 *
 * @see edu.duke.cs.jflap.grammar.reg.RegularGrammar
 *
 * @author Ryan Cavalcante
 */
public class FSAToRegularGrammarConverter {
    private final Logger logger = LoggerFactory.getLogger(FSAToRegularGrammarConverter.class);

    /**
     * Creates an instance of <CODE>FSAToRegularGrammarConverter</CODE>
     */
    public FSAToRegularGrammarConverter() {
    }

    /**
     * Maps the states in <CODE>automaton</CODE> to the variables in the grammar
     * the converter will produce.
     *
     * @param automaton
     *            the automaton.
     */
    public void initializeConverter(Automaton automaton) {
        MAP = new HashMap<>();
        List<State> states = automaton.getStates();
        State initialState = automaton.getInitialState();
        // Do the variables.
        VARIABLE = new LinkedList<>();
        for (char c = 'A'; c <= 'Z'; c++) {
            VARIABLE.add("" + c);
        }
        // Map the initial state to S.
        if (initialState != null) {
            VARIABLE.remove(START_VARIABLE);
            MAP.put(initialState, START_VARIABLE);
        }
        // Assign variables to the other states.
        states.remove(initialState);
        states.sort(new Comparator<State>() {
            @Override
            public int compare(State o1, State o2) {
                return o1.getID() - o2.getID();
            }

            @Override
            public boolean equals(Object o) {
                return false;
            }
        });

        states.forEach(state -> MAP.put(state, VARIABLE.removeFirst()));
    }

    /**
     * Returns a production object that is equivalent to
     * <CODE>transition</CODE>.
     *
     * @param transition
     *            the transition.
     * @return a produciton object that is equivalent to
     *         <CODE>transition</CODE>.
     */
    public Production getProductionForTransition(Transition transition) {
        FSATransition trans = (FSATransition) transition;

        State toState = trans.getToState();
        State fromState = trans.getFromState();
        String label = trans.getLabel();
        String lhs = MAP.get(fromState);
        String rhs = label.concat(MAP.get(toState));
        Production production = new Production(lhs, rhs);

        return production;
    }

    /**
     * Returns a lambda production on the variable mapped to <CODE>state</CODE>
     * in <CODE>map</CODE>.
     *
     * @param automaton
     *            the automaton that <CODE>state</CODE> is in.
     * @param state
     *            the state to make the lambda production for.
     * @return a lambda production on the variable mapped to <CODE>state</CODE>
     *         in <CODE>MAP</CODE>.
     */
    public Production getLambdaProductionForFinalState(Automaton automaton, State state) {
        /** Check if state is a final state. */
        if (!automaton.isFinalState(state)) {
            System.err.println(state + " IS NOT A FINAL STATE");
            return null;
        }
        String llhs = MAP.get(state);
        String lrhs = LAMBDA;
        Production lprod = new Production(llhs, lrhs);
        return lprod;
    }

    /**
     * Returns a RegularGrammar object that represents a grammar equivalent to
     * <CODE>automaton</CODE>.
     *
     * @param automaton
     *            the automaton.
     * @return a regular grammar equivalent to <CODE>automaton</CODE>
     */
    public RegularGrammar convertToRegularGrammar(Automaton automaton) {
        /** check if automaton is fsa. */
        if (!(automaton instanceof FiniteStateAutomaton)) {
            logger.error("Attempting to convert non FSA to Regular Grammar");
            return null;
        }

        RegularGrammar grammar = new RegularGrammar();
        /** map states in automaton to variables in grammar. */
        initializeConverter(automaton);
        /**
         * go through all transitions in automaton, creating production for
         * each.
         */
        List<Transition> transitions = automaton.getTransitions();
        for (int k = 0; k < transitions.size(); k++) {
            Production production = getProductionForTransition(transitions.get(k));
            grammar.addProduction(production);
        }

        /**
         * for all final states in automaton, add lambda production.
         */
        List<State> finalStates = automaton.getFinalStates();
        for (int j = 0; j < finalStates.size(); j++) {
            Production lprod = getLambdaProductionForFinalState(automaton, finalStates.get(j));
            grammar.addProduction(lprod);
        }

        return grammar;
    }

    /**
     * Returns the variable in the grammar corresponding to the state.
     *
     * @param state
     *            the state to get the variable for
     * @return the variable in the grammar corresponding to the state, or
     *         <CODE>null</CODE> if there is no variable corresponding to this
     *         state
     */
    public String variableForState(State state) {
        return MAP.get(state);
    }

    /** The map of states in the fsa to variables in the grammar. */
    protected HashMap<State, String> MAP;

    /** The start variable. */
    protected static final String START_VARIABLE = "S";

    /** The string for lambda. */
    protected static final String LAMBDA = "";

    /** The list of unclaimed variable symbols. */
    protected LinkedList<String> VARIABLE;
}
