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
import edu.duke.cs.jflap.automata.turing.TuringMachine;
import edu.duke.cs.jflap.automata.turing.TuringToGrammarConverter;
import edu.duke.cs.jflap.grammar.ConvertedUnrestrictedGrammar;
import edu.duke.cs.jflap.grammar.Grammar;
import edu.duke.cs.jflap.grammar.Production;
import edu.duke.cs.jflap.gui.environment.FrameFactory;
import edu.duke.cs.jflap.gui.viewer.SelectionDrawer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javax.swing.JOptionPane;

/**
 * Controller for conversion
 *
 * @author Kyung Min (Jason) Lee
 *
 */
public class TuringConvertController extends ConvertController {

  private TuringToGrammarConverter converter;
  private TuringMachine myTuringMachine;

  /**
   * Instantiates a <CODE>PDAConvertController</CODE> for an automaton.
   *
   * @param pane
   *            the convert pane that holds the automaton pane and the grammar
   *            table
   * @param drawer
   *            the selection drawer where the automaton is made
   * @param automaton
   *            the automaton to build the <CODE>PDAConvertController</CODE>
   *            for
   */
  public TuringConvertController(
      ConvertPane pane, SelectionDrawer drawer, TuringMachine automaton) {
    super(pane, drawer, automaton);
    myTuringMachine = automaton;
    converter = new TuringToGrammarConverter();
    // converter.initializeConverter();

    pane.getTable().getColumnModel().getColumn(0).setMinWidth(150);

    pane.getTable().getColumnModel().getColumn(0).setMaxWidth(250);
    fillMap();
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
  protected Production[] getProductions(State state) {
    if (myTuringMachine.isInitialState(state)) {
      Transition[] tm = myTuringMachine.getTransitions();
      return converter.createProductionsForInit(state, tm);
    }
    return new Production[0];
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
  protected Production[] getProductions(Transition transition) {
    /*
     * return (Production[]) converter.createProductionsForTransition(
     * transition, getAutomaton())
     */
    return converter.createProductionsForTransition(transition, myTuringMachine.getFinalStates());
  }

  @Override
  protected ConvertedUnrestrictedGrammar getGrammar() {
    // TODO Auto-generated method stub

    // Put error check here

    int rows = getModel().getRowCount();
    ConvertedUnrestrictedGrammar grammar = new ConvertedUnrestrictedGrammar();
    grammar.setStartVariable("S");
    ArrayList<Production> productions = new ArrayList<Production>();
    for (int i = 0; i < rows; i++) {
      Production production = getModel().getProduction(i);
      if (production == null) continue;
      // production = converter.getSimplifiedProduction(production);
      productions.add(production);
    }

    Collections.sort(
        productions,
        new Comparator<Object>() {
          public int compare(Object o1, Object o2) {
            Production p1 = (Production) o1, p2 = (Production) o2;
            if ("S".equals(p1.getLHS())) {
              if (p1.getLHS().equals(p2.getLHS())) return 0;
              else return -1;
            }
            if ("S".equals(p2.getLHS())) return 1;
            return p2.getLHS().compareTo(p1.getRHS());
          }

          public boolean equals(Object o) {
            return false;
          }
        });
    for (int i = 0; i < productions.size(); i++) {
      grammar.addProduction(productions.get(i));
    }

    /*
     * UselessProductionRemover remover = new UselessProductionRemover();
     *
     * Grammar g2 = UselessProductionRemover
     * .getUselessProductionlessGrammar(grammar);
     *
     * if (g2.getTerminals().length==0) { System.out.
     * println("Error : This grammar does not accept any Strings. "); }
     * Production[] p1 = grammar.getProductions(); Production[] p2 =
     * g2.getProductions(); if (p1.length > p2.length) { UselessPane up =
     * new UselessPane(new GrammarEnvironment(null), grammar);
     * UselessController controller=new UselessController(up, grammar);
     * controller.doAll(); grammar=controller.getGrammar(); }
     */
    return grammar;
  }

  /**
   * Called by when export grammar button is clicked
   */
  public Grammar exportGrammar() {
    // Are any yet unconverted?
    if (objectToProduction.keySet().size() != alreadyDone.size()) {
      highlightUntransformed();
      JOptionPane.showMessageDialog(
          convertPane,
          "Conversion unfinished!  Objects to convert are highlighted.",
          "Conversion Unfinished",
          JOptionPane.ERROR_MESSAGE);
      changeSelection();
      return null;
    }
    try {
      ConvertedUnrestrictedGrammar g = getGrammar();
      ArrayList<Production> prods = new ArrayList<Production>();
      Production[] temp = g.getProductions();
      for (int i = 0; i < temp.length; i++) prods.add(temp[i]);

      Collections.sort(
          prods,
          new Comparator<Production>() {
            public int compare(Production o1, Production o2) {
              if (o1.getLHS().equals("S")) return -1;
              return (o1.getRHS().length() - o2.getRHS().length());
            }
          });
      ConvertedUnrestrictedGrammar gg = new ConvertedUnrestrictedGrammar();
      for (int i = 0; i < temp.length; i++) temp[i] = prods.get(i);
      gg.setStartVariable("S");
      gg.addProductions(temp);
      FrameFactory.createFrame(gg, 0);
      return gg;
    } catch (GrammarCreationException e) {
      JOptionPane.showMessageDialog(
          convertPane, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
      return null;
    }
  }
}
