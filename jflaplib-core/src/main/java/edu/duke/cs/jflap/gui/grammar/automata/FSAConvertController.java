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

package edu.duke.cs.jflap.gui.grammar.automata;

import edu.duke.cs.jflap.automata.State;
import edu.duke.cs.jflap.automata.Transition;
import edu.duke.cs.jflap.automata.fsa.FSAToRegularGrammarConverter;
import edu.duke.cs.jflap.automata.fsa.FiniteStateAutomaton;
import edu.duke.cs.jflap.grammar.Grammar;
import edu.duke.cs.jflap.grammar.Production;
import edu.duke.cs.jflap.grammar.reg.RegularGrammar;
import edu.duke.cs.jflap.gui.viewer.SelectionDrawer;

import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * This controls the conversion of a finite state automaton to a regular
 * grammar.
 *
 * @author Thomas Finley
 */
public class FSAConvertController extends ConvertController {
    /**
     * Instantiates a <CODE>FSAConvertController</CODE> for an automaton.
     *
     * @param pane
     *            the convert pane that holds the automaton pane and the grammar
     *            table
     * @param drawer
     *            the selection drawer where the automaton is made
     * @param automaton
     *            the automaton to build the <CODE>FSAConvertController</CODE>
     *            for; this automaton should be editable
     */
    public FSAConvertController(ConvertPane pane,
            SelectionDrawer drawer,
            FiniteStateAutomaton automaton) {
        super(pane, drawer, automaton);
        converter = new FSAToRegularGrammarConverter();
        converter.initializeConverter(automaton);
        fillMap();
        // Sets the labels.
        List<State> states = automaton.getStates();
        for (int i = 0; i < states.size(); i++) {
            states.get(i).setLabel(converter.variableForState(states.get(i)));
        }
    }

    /**
     * Returns the productions for a particular state. This method will only be
     * called once.
     *
     * @param state
     *            the state to get the productions for
     * @return an array containing the productions that correspond to a
     *         particular state
     */
    @Override
    protected List<Production> getProductions(State state) {
        if (!getAutomaton().isFinalState(state)) {
            return new ArrayList<>();
        }
        return Lists
                .newArrayList(converter.getLambdaProductionForFinalState(getAutomaton(), state));
    }

    /**
     * Returns the productions for a particular transition. This method will
     * only be called once.
     *
     * @param transition
     *            the transition to get the productions for
     * @return an array containing the productions that correspond to a
     *         particular transition
     */
    @Override
    protected List<Production> getProductions(Transition transition) {
        return Lists.newArrayList(converter.getProductionForTransition(transition));
    }

    /**
     * Returns the grammar that's the result of this conversion.
     *
     * @return the grammar that's the result of this conversion
     */
    @Override
    protected Grammar getGrammar() {
        int rows = getModel().getRowCount();
        RegularGrammar grammar = new RegularGrammar();
        grammar.setStartVariable("S");
        ArrayList<Production> productions = new ArrayList<>();
        for (int i = 0; i < rows; i++) {
            Production production = getModel().getProduction(i);
            if (production == null) {
                continue;
            }
            productions.add(production);
        }
        Collections.sort(productions, new Comparator<Object>() {
            @Override
            public int compare(Object o1, Object o2) {
                Production p1 = (Production) o1, p2 = (Production) o2;
                if ("S".equals(p1.getLHS())) {
                    if (p1.getLHS().equals(p2.getLHS())) {
                        return 0;
                    } else {
                        return -1;
                    }
                }
                if ("S".equals(p2.getLHS())) {
                    return 1;
                }
                return p1.getLHS().compareTo(p2.getRHS());
            }

            @Override
            public boolean equals(Object o) {
                return false;
            }
        });
        Iterator<Production> it = productions.iterator();
        while (it.hasNext()) {
            grammar.addProduction(it.next());
        }
        return grammar;
    }

    /** The converter object from which we get the productions. */
    private FSAToRegularGrammarConverter converter;
}
