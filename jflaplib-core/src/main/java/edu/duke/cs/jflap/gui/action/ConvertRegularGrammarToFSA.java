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

import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.List;

import javax.swing.JOptionPane;

import edu.duke.cs.jflap.automata.State;
import edu.duke.cs.jflap.automata.Transition;
import edu.duke.cs.jflap.automata.fsa.FiniteStateAutomaton;
import edu.duke.cs.jflap.automata.graph.AutomatonGraph;
import edu.duke.cs.jflap.automata.graph.LayoutAlgorithm;
import edu.duke.cs.jflap.automata.graph.layout.GEMLayoutAlgorithm;
import edu.duke.cs.jflap.grammar.Production;
import edu.duke.cs.jflap.grammar.reg.RightLinearGrammar;
import edu.duke.cs.jflap.grammar.reg.RightLinearGrammarToFSAConverter;
import edu.duke.cs.jflap.gui.environment.GrammarEnvironment;
import edu.duke.cs.jflap.gui.environment.Universe;
import edu.duke.cs.jflap.gui.environment.tag.CriticalTag;
import edu.duke.cs.jflap.gui.grammar.convert.ConvertPane;

/**
 * This is the action that initiates the conversion of right linear grammar to
 * an FSA.
 *
 * @see edu.duke.cs.jflap.gui.grammar.convert.ConvertPane
 * @see edu.duke.cs.jflap.grammar.reg.RightLinearGrammarToFSAConverter
 *
 * @author Thomas Finley
 */
public class ConvertRegularGrammarToFSA extends GrammarAction {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/** The grammar environment. */
	private final GrammarEnvironment environment;

	/**
	 * Instantiates a new <CODE>GrammarOutputAction</CODE>.
	 *
	 * @param environment
	 *            the grammar environment
	 */
	public ConvertRegularGrammarToFSA(final GrammarEnvironment environment) {
		super("Convert Right-Linear Grammar to FA", null);
		this.environment = environment;
	}

	/**
	 * Performs the action.
	 */
	@Override
	public void actionPerformed(final ActionEvent e) {
		// Construct the regular grammar.
		final RightLinearGrammar grammar = (RightLinearGrammar) environment.getGrammar(RightLinearGrammar.class);
		if (grammar == null) {
			return;
		}
		if (grammar.getProductions().size() == 0) {
			JOptionPane.showMessageDialog(Universe.frameForEnvironment(environment), "The grammar should exist.");
			return;
		}

		// Create the initial automaton.
		final FiniteStateAutomaton fsa = new FiniteStateAutomaton();
		final RightLinearGrammarToFSAConverter convert = new RightLinearGrammarToFSAConverter();
		convert.createStatesForConversion(grammar, fsa);
		final AutomatonGraph graph = new AutomatonGraph(fsa);
		// Create the map of productions to transitions.
		final HashMap<Production, Transition> ptot = new HashMap<>();
		final List<Production> prods = grammar.getProductions();
		for (int i = 0; i < prods.size(); i++) {
			final Transition t = convert.getTransitionForProduction(prods.get(i));
			graph.addEdge(t.getFromState(), t.getToState());
			ptot.put(prods.get(i), t);
		}
		// Add the view to the environment.
		final ConvertPane cp = new ConvertPane(grammar, fsa, ptot, environment);
		environment.add(cp, "Convert to FA", new CriticalTag() {
		});
		final LayoutAlgorithm<State> layout = new GEMLayoutAlgorithm<>();
		layout.layout(graph, null);
		graph.moveAutomatonStates();
		environment.setActive(cp);
		environment.validate();
		cp.getEditorPane().getAutomatonPane().fitToBounds(20);
	}
}
