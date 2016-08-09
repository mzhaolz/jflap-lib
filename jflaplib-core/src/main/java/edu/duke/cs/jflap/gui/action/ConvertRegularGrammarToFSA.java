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

import java.awt.event.ActionEvent;
import java.util.HashMap;

import javax.swing.JOptionPane;

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

  /**
   * Instantiates a new <CODE>GrammarOutputAction</CODE>.
   *
   * @param environment
   *            the grammar environment
   */
  public ConvertRegularGrammarToFSA(GrammarEnvironment environment) {
    super("Convert Right-Linear Grammar to FA", null);
    this.environment = environment;
  }

  /**
   * Performs the action.
   */
  @Override
  public void actionPerformed(ActionEvent e) {
    // Construct the regular grammar.
    RightLinearGrammar grammar =
        (RightLinearGrammar) environment.getGrammar(RightLinearGrammar.class);
    if (grammar == null) return;
    if (grammar.getProductions().length == 0) {
      JOptionPane.showMessageDialog(
          Universe.frameForEnvironment(environment), "The grammar should exist.");
      return;
    }

    // Create the initial automaton.
    FiniteStateAutomaton fsa = new FiniteStateAutomaton();
    RightLinearGrammarToFSAConverter convert = new RightLinearGrammarToFSAConverter();
    convert.createStatesForConversion(grammar, fsa);
    AutomatonGraph graph = new AutomatonGraph(fsa);
    // Create the map of productions to transitions.
    HashMap<Production, Transition> ptot = new HashMap<Production, Transition>();
    Production[] prods = grammar.getProductions();
    for (int i = 0; i < prods.length; i++) {
      Transition t = convert.getTransitionForProduction(prods[i]);
      graph.addEdge(t.getFromState(), t.getToState());
      ptot.put(prods[i], t);
    }
    // Add the view to the environment.
    final ConvertPane cp = new ConvertPane(grammar, fsa, ptot, environment);
    environment.add(cp, "Convert to FA", new CriticalTag() {});
    LayoutAlgorithm<State> layout = new GEMLayoutAlgorithm<>();
    layout.layout(graph, null);
    graph.moveAutomatonStates();
    environment.setActive(cp);
    environment.validate();
    cp.getEditorPane().getAutomatonPane().fitToBounds(20);
  }

  /** The grammar environment. */
  private GrammarEnvironment environment;
}
