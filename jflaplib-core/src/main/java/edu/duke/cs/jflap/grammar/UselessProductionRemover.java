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

package edu.duke.cs.jflap.grammar;

import edu.duke.cs.jflap.automata.State;
import edu.duke.cs.jflap.automata.Transition;
import edu.duke.cs.jflap.automata.UnreachableStatesDetector;
import edu.duke.cs.jflap.automata.vdg.VDGTransition;
import edu.duke.cs.jflap.automata.vdg.VariableDependencyGraph;
import edu.duke.cs.jflap.grammar.cfg.ContextFreeGrammar;

import com.google.common.collect.Lists;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * As it stands now, the code in here is almost completely useless. Through
 * conjunction with <CODE>gui.grammar.transform.UselessController</CODE> it
 * manages to do the correct thing (I hope), but in the interest of correctness
 * this code should be reformed; I have too much to do right now to figure out
 * where the hell he was going with some of this garbage... TWF
 */
public class UselessProductionRemover {
  /**
   * Creates instance of <CODE>UselessProductionRemover</CODE>.
   */
  private UselessProductionRemover() {}

  /**
   * Returns set of all useful variables in <CODE>grammar</CODE>. A grammar is
   * considered useful if it can derive a string.
   *
   * @param grammar
   *            the grammar
   * @return set of all useful variables in <CODE>grammar</CODE>. A grammar is
   *         considered useful if it can derive a string.
   */
  public static Set<String> getCompleteUsefulVariableSet(Grammar grammar) {
    Set<String> set = getNewUsefulVariableSet();
    while (areMoreVariablesThatBelongInUsefulVariableSet(grammar, set)) {
      String variable = getVariableThatBelongsInUsefulVariableSet(grammar, set);
      addToUsefulVariableSet(variable, set);
    }
    return set;
  }

  /**
   * Returns empty set.
   *
   * @return empty set.
   */
  private static Set<String> getNewUsefulVariableSet() {
    return new HashSet<String>();
  }

  /**
   * Adds <CODE>variable</CODE> to <CODE>set</CODE>.
   *
   * @param variable
   *            the variable
   * @param set
   *            the set
   */
  public static void addToUsefulVariableSet(String variable, Set<String> set) {
    set.add(variable);
  }

  /**
   * Returns the set of variables that are the predicate of rules that are
   * only terminal strings.
   */
  public static Set<String> getTerminalProductions(Grammar grammar) {
    Set<String> terminalDerivers = new TreeSet<>();
    List<Production> p = grammar.getProductions();
    for (int i = 0; i < p.size(); i++) {
      String lhs = p.get(i).getLHS();
      if (terminalDerivers.contains(lhs)) continue;
      String rhs = p.get(i).getRHS();
      for (int k = 0; k < rhs.length(); k++) {
        char ch = rhs.charAt(k);
        if (ProductionChecker.isVariable(ch)) {
          lhs = null;
          break;
        }
      }
      if (lhs == null) continue;
      terminalDerivers.add(lhs);
    }
    return terminalDerivers;
  }

  /**
   * Get a grammar with only those variables that derive terminals, directly
   * or indirectly. This is not the same as a useless production-less grammar.
   *
   * @param grammar
   *            the grammar to get the reformed grammar for
   */
  public static Grammar getTerminalGrammar(Grammar grammar) {
    Grammar g = new ContextFreeGrammar();
    Set<String> terminalVars = getCompleteUsefulVariableSet(grammar);
    List<Production> prods = grammar.getProductions();
    for (int i = 0; i < prods.size(); i++) {
      Set<String> v = new HashSet<>(prods.get(i).getVariables());
      v.removeAll(terminalVars);
      if (v.size() > 0) continue;
      // Production has no variables that aren't terminal derivers!
      g.addProduction(prods.get(i));
    }
    g.setStartVariable(grammar.getStartVariable());
    return g;
  }

  /**
   * Returns a variable that belongs in the set of useful variables for
   * <CODE>grammar</CODE> that is not already in <CODE>set</CODE>.
   *
   * @param grammar
   *            the grammar
   * @param set
   *            the set of useful variables in <CODE>grammar</CODE>.
   * @return a variable that belongs in the set of useful variables for
   *         <CODE>grammar</CODE> that is not already in <CODE>set</CODE>.
   */
  public static String getVariableThatBelongsInUsefulVariableSet(Grammar grammar, Set<String> set) {
    List<String> variables = grammar.getVariables();
    for (int k = 0; k < variables.size(); k++) {
      if (belongsInUsefulVariableSet(variables.get(k), grammar, set)
          && !set.contains(variables.get(k))) return variables.get(k);
    }
    return null;
  }

  /**
   * Returns true if <CODE>set</CODE> contains a variable equivalent to
   * <CODE>ch</CODE>.
   *
   * @param ch
   *            the character
   * @param set
   *            the set of useful variables
   * @return true if <CODE>set</CODE> contains a variable equivalent to
   *         <CODE>ch</CODE>.
   */
  private static boolean isInUsefulVariableSet(char ch, Set<String> set) {
    Iterator<String> it = set.iterator();
    while (it.hasNext()) {
      String variable = it.next();
      char var = variable.charAt(0);
      if (ch == var) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns true if <CODE>production</CODE> can derive a string. (i.e. if all
   * letters on the right hand side of the production are either terminals or
   * useful variables (variables in <CODE>set</CODE>).
   *
   * @param production
   *            the production
   * @param set
   *            the set of useful variables
   * @return true if <CODE>production</CODE> can derive a string. (i.e. if all
   *         letters on the right hand side of the production are either
   *         terminals or useful variables (variables in <CODE>set</CODE>).
   */
  private static boolean isUsefulProduction(Production production, Set<String> set) {
    String rhs = production.getRHS();
    for (int k = 0; k < rhs.length(); k++) {
      char ch = rhs.charAt(k);
      if (!ProductionChecker.isTerminal(ch) && !isInUsefulVariableSet(ch, set)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Returns true if <CODE>production</CODE> contains only terminals and
   * variables in <CODE>set</CODE>, the set of useful variables. This includes
   * both the left and right hand side of the production.
   *
   * @param production
   *            the production
   * @param set
   *            the set of useful variables
   * @return true if <CODE>production</CODE> contains only terminals and
   *         variables in <CODE>set</CODE>, the set of useful variables. This
   *         includes both the left and right hand side of the production.
   */
  public static boolean isValidProduction(Production production, Set<String> set) {
    String lhs = production.getLHS();
    for (int k = 0; k < lhs.length(); k++) {
      if (!isInUsefulVariableSet(lhs.charAt(k), set)) return false;
    }
    return isUsefulProduction(production, set);
  }

  /**
   * Returns true if <CODE>variable</CODE> belongs in the set of useful
   * variables, even if it is already in <CODE>set</CODE>. This function
   * examines all productions in <CODE>grammar</CODE> with variable on the
   * left hand side, and determines if any of those productions are useful.
   *
   * @param variable
   *            the variable
   * @param grammar
   *            the grammar
   * @param set
   *            the set of useful variables
   * @return true if <CODE>variable</CODE> belongs in the set of useful
   *         variables, even if it is already in <CODE>set</CODE>.
   */
  public static boolean belongsInUsefulVariableSet(
      String variable, Grammar grammar, Set<String> set) {
    List<Production> productions = GrammarChecker.getProductionsOnVariable(variable, grammar);
    for (int k = 0; k < productions.size(); k++) {
      if (isUsefulProduction(productions.get(k), set)) return true;
    }
    return false;
  }

  /**
   * Returns true if there are more variables (i.e. other than the ones
   * already in the set) that belong in the set of useful variables
   * <CODE>set</CODE>.
   *
   * @param grammar
   *            the grammar
   * @param set
   *            the set of useful variables.
   * @return true if there are more variables (i.e. other than the ones
   *         already in the set) that belong in the set of useful variables
   *         <CODE>set</CODE>.
   */
  public static boolean areMoreVariablesThatBelongInUsefulVariableSet(
      Grammar grammar, Set<String> set) {
    if (getVariableThatBelongsInUsefulVariableSet(grammar, set) == null) return false;
    return true;
  }

  /**
   * Returns the set of all useful productions (i.e. productions that can
   * derive strings) in <CODE>grammar</CODE> based on the useful variables,
   * contained in <CODE>usefulVariableSet</CODE>.
   *
   * @param grammar
   *            the grammar
   * @param usefulVariableSet
   *            the set of useful variables
   * @return the set of all useful productions (i.e. productions that can
   *         derive strings) in <CODE>grammar</CODE> based on the useful
   *         variables, contained in <CODE>usefulVariableSet</CODE>.
   */
  public static Set<Production> getCompleteProductionWithUsefulVariableSet(
      Grammar grammar, Set<String> usefulVariableSet) {
    Set<Production> set = getNewProductionWithUsefulVariableSet();
    List<Production> productions = grammar.getProductions();
    for (int k = 0; k < productions.size(); k++) {
      if (belongsInProductionWithUsefulVariableSet(productions.get(k), usefulVariableSet)) {
        set.add(productions.get(k));
      }
    }
    return set;
  }

  /**
   * Returns an empty set.
   *
   * @return an empty set.
   */
  public static Set<Production> getNewProductionWithUsefulVariableSet() {
    return new HashSet<Production>();
  }

  /**
   * Returns true if <CODE>production</CODE> belongs in set of useful
   * productions (i.e. if <CODE>production</CODE> contains only terminals and
   * variables in <CODE>usefulVariableSet</CODE>.
   *
   * @param production
   *            the production
   * @param usefulVariableSet
   *            the set of useful variables
   * @return true if <CODE>production</CODE> belongs in set of useful
   *         productions (i.e. if <CODE>production</CODE> contains only
   *         terminals and variables in <CODE>usefulVariableSet</CODE>.
   */
  public static boolean belongsInProductionWithUsefulVariableSet(
      Production production, Set<String> usefulVariableSet) {
    if (isValidProduction(production, usefulVariableSet)) {
      return true;
    }
    return false;
  }

  /**
   * Adds <CODE>production</CODE> to <CODE>set</CODE>.
   */
  public static void addToProductionWithUsefulVariableSet(
      Production production, Set<Production> set) {
    set.add(production);
  }

  /**
   * Adds a state for every variable in <CODE>grammar</CODE> to
   * <CODE>graph</CODE>, and sets the state that represents the start variable
   * ("S") to the initial state.
   *
   * @param graph
   *            the variable dependency graph
   * @param grammar
   *            the grammar.
   */
  public static void initializeVariableDependencyGraph(
      VariableDependencyGraph graph, Grammar grammar) {
    List<String> variables = Lists.newArrayList(getCompleteUsefulVariableSet(grammar));
    for (int k = 0; k < variables.size(); k++) {
      double theta = 2.0 * Math.PI * k / variables.size();
      Point point =
          new Point(200 + (int) (180.0 * Math.cos(theta)), 200 + (int) (180.0 * Math.sin(theta)));
      State state = graph.createState(point);
      state.setName(variables.get(k));
      if (variables.get(k).equals(grammar.getStartVariable())) graph.setInitialState(state);
    }
  }

  /**
   * Returns true if <CODE>v1</CODE> is dependent on <CODE>v2</CODE>. (i.e. if
   * <CODE>v2</CODE> is on the right hand side of any production in
   * <CODE>grammar</CODE> that has <CODE>v1</CODE> on the left hand side).
   *
   * @param v1
   *            the variable on the left hand side
   * @param v2
   *            the variable on the right hand side
   * @param grammar
   *            the grammar
   * @return true if <CODE>v1</CODE> is dependent on <CODE>v2</CODE>. (i.e. if
   *         <CODE>v2</CODE> is on the right hand side of any production in
   *         <CODE>grammar</CODE> that has <CODE>v1</CODE> on the left hand
   *         side).
   */
  public static boolean isDependentOn(String v1, String v2, Grammar grammar) {
    List<Production> productions = GrammarChecker.getProductionsOnVariable(v1, grammar);
    for (int k = 0; k < productions.size(); k++) {
      if (ProductionChecker.isVariableInProduction(v2, productions.get(k))) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns a transition between the states that represent <CODE>v1</CODE>
   * and <CODE>v2</CODE> in <CODE>graph</CODE>.
   *
   * @param v1
   *            a variable
   * @param v2
   *            a variable
   * @param graph
   *            the variable dependency graph
   * @return a transition between the states that represent <CODE>v1</CODE>
   *         and <CODE>v2</CODE> in <CODE>graph</CODE>.
   */
  public static Transition getTransition(String v1, String v2, VariableDependencyGraph graph) {
    State from = getStateForVariable(v1, graph);
    State to = getStateForVariable(v2, graph);
    return new VDGTransition(from, to);
  }

  /**
   * Returns the state in <CODE>graph</CODE> that represents
   * <CODE>variable</CODE> (i.e the state whose name is
   * <CODE>variable</CODE>).
   *
   * @param variable
   *            the variable
   * @param graph
   *            the variable dependency graph.
   * @return the state in <CODE>graph</CODE> that represents
   *         <CODE>variable</CODE> (i.e the state whose name is
   *         <CODE>variable</CODE>).
   */
  public static State getStateForVariable(String variable, VariableDependencyGraph graph) {
    List<State> states = graph.getStates();
    for (int k = 0; k < states.size(); k++) {
      State state = states.get(k);
      if (state.getName().equals(variable)) return state;
    }
    return null;
  }

  /**
   * Returns the variable dependency graph for <CODE>grammar</CODE>.
   *
   * @param grammar
   *            the grammar
   * @return the variable dependency graph for <CODE>grammar</CODE>.
   */
  public static VariableDependencyGraph getVariableDependencyGraph(Grammar grammar) {
    VariableDependencyGraph graph = new VariableDependencyGraph();
    initializeVariableDependencyGraph(graph, grammar);
    List<String> variables = new ArrayList<>(getCompleteUsefulVariableSet(grammar));
    for (int k = 0; k < variables.size(); k++) {
      String v1 = variables.get(k);
      for (int i = 0; i < variables.size(); i++) {
        String v2 = variables.get(i);
        if (i != k && isDependentOn(v1, v2, grammar)) {
          Transition trans = getTransition(v1, v2, graph);
          graph.addTransition(trans);
        }
      }
    }
    return graph;
  }

  /**
   * Returns a set of transitions that represent all the dependencies
   * determined by <CODE>production</CODE>.
   *
   * @param production
   *            the production
   * @param graph
   *            the variable dependency graph
   * @return a set of transitions that represent all the dependencies
   *         determined by <CODE>production</CODE>.
   */
  public static List<Transition> getTransitionsForProduction(
      Production production, VariableDependencyGraph graph) {
    List<Transition> list = new ArrayList<>();
    String v1 = production.getLHS();
    String rhs = production.getRHS();
    for (int k = 0; k < rhs.length(); k++) {
      char ch = rhs.charAt(k);
      if (ProductionChecker.isVariable(ch)) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(ch);
        list.add(getTransition(v1, buffer.toString(), graph));
      }
    }
    return list;
  }

  /**
   * Returns the set of variables in <CODE>grammar</CODE> whose productions
   * can never be reached from the start symbol. This is determined by the
   * variable dependency graph <CODE>graph</CODE>.
   *
   * @param grammar
   *            the grammar
   * @param graph
   *            the variable dependency graph
   * @return the set of variables in <CODE>grammar</CODE> whose productions
   *         can never be reached from the start symbol. This is determined by
   *         the variable dependency graph <CODE>graph</CODE>.
   */
  public static List<String> getUselessVariables(Grammar grammar, VariableDependencyGraph graph) {
    List<String> list = new ArrayList<>();
    UnreachableStatesDetector usd = new UnreachableStatesDetector(graph);
    List<State> states = usd.getUnreachableStates();
    for (int k = 0; k < states.size(); k++) {
      list.add(states.get(k).getName());
    }
    return list;
  }

  /**
   * Removes all productions from <CODE>grammar</CODE> that contain
   * <CODE>variable</CODE>, either on the left or right hand sides.
   *
   * @param variable
   *            the variable
   * @param grammar
   *            the grammar
   */
  public static void removeProductionsForVariable(String variable, Grammar grammar) {
    List<Production> productions = GrammarChecker.getProductionsWithVariable(variable, grammar);
    for (int k = 0; k < productions.size(); k++) {
      grammar.removeProduction(productions.get(k));
    }
  }

  /**
   * Returns a grammar, equivalent to <CODE>grammar</CODE> that contains no
   * useless productions.
   *
   * @param grammar
   *            the grammar
   * @return a grammar, equivalent to <CODE>grammar</CODE> that contains no
   *         useless productions.
   */
  public static Grammar getUselessProductionlessGrammar(Grammar grammar) {
    Grammar g = new ContextFreeGrammar();
    g.setStartVariable(grammar.getStartVariable());
    if (!getCompleteUsefulVariableSet(grammar).contains(grammar.getStartVariable())) return g;
    grammar = getTerminalGrammar(grammar);
    VariableDependencyGraph graph = getVariableDependencyGraph(grammar);
    Set<String> useless = new HashSet<>(getUselessVariables(g, graph));
    List<Production> p = grammar.getProductions();
    for (int i = 0; i < p.size(); i++) {
      Set<String> variables = new HashSet<>(p.get(i).getVariables());
      variables.retainAll(useless);
      if (variables.size() > 0) continue;
      g.addProduction(p.get(i));
    }
    return g;
  }

  /** the start symbol. */
  protected static final String START_SYMBOL = "S";
}
