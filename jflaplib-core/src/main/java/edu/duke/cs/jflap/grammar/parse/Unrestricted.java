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

package edu.duke.cs.jflap.grammar.parse;

import edu.duke.cs.jflap.grammar.Grammar;
import edu.duke.cs.jflap.grammar.Production;
import edu.duke.cs.jflap.grammar.UnrestrictedGrammar;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class is a utility class for determining some facts about unrestricted
 * grammars. As structures equivalent in power to Turing machines, a brute force
 * parse of an unrestricted grammar may, in some situations, not be recognized.
 *
 * @author Thomas Finley
 */
public class Unrestricted {
  /**
   * Dang class aint for instantiation! Get along, lil doggie.
   */
  private Unrestricted() {}

  /**
   * Given a string and a smaller set, this returns the minimum length that
   * the string can derive as indicated by the smaller set.
   *
   * @param string
   *            the string to get the "smaller"
   * @param smaller
   *            the "smaller" set, as returned by {@link #smallerSymbols}
   */
  public static int minimumLength(String string, Set<String> smaller) {
    int length = 0;
    for (int j = 0; j < string.length(); j++)
      if (!smaller.contains(string.substring(j, j + 1))) length++;
    return length;
  }

  /**
   * Counts the number of characters in a given string.
   *
   * @param s
   *            the string
   * @param c
   *            the character
   * @return the number of occurances of the character in the string
   */
  private static int count(String s, char c) {
    int count = 0;
    for (int i = 0; i < s.length(); i++) if (s.charAt(i) == c) count++;
    return count;
  }

  /**
   * Returns a set of those symbols in the grammar that can derive some string
   * smaller than it. For a normal grammar, of course, this would be just
   * those variables with, but for an unrestricted grammar this can include
   * the symbol <I>b</I> and <I>c</I> where <I>babca - aa</I> is a rule.
   * <I>a</I> is not included because there are <I>a</I> terminals in the
   * result.
   *
   * @param grammar
   *            the grammar to find the "small" symbols for
   */
  public static Set<String> smallerSymbols(Grammar grammar) {
    Set<String> smaller = new HashSet<>();
    List<Production> prods = grammar.getProductions();
    boolean added;
    do {
      added = false;
      for (int i = 0; i < prods.size(); i++) {
        String left = prods.get(i).getLHS();
        String right = prods.get(i).getRHS();
        int rightLength = minimumLength(right, smaller);
        int leftLength = minimumLength(left, smaller);
        if (leftLength > rightLength) {
          for (int j = 0; j < left.length(); j++) {
            String symbol = left.substring(j, j + 1);
            char s = symbol.charAt(0);
            if (smaller.contains(symbol)) continue;
            if (count(left, s) <= count(right, s)) continue;
            smaller.add(symbol);
            added = true;
          }
        }
      }
    } while (added);
    return smaller;
  }

  /**
   * Returns if a grammar is truly unrestricted.
   *
   * @param grammar
   *            the grammar to test
   * @return if a grammar is unrestricted
   */
  public static boolean isUnrestricted(Grammar grammar) {
    List<Production> prods = grammar.getProductions();
    for (int i = 0; i < prods.size(); i++) if (prods.get(i).getLHS().length() != 1) return true;
    return false;
  }

  /**
   * Given an unrestricted grammar, this will return an unrestricted grammar
   * with fewer productions that accepts the same language.
   *
   * @param grammar
   *            the input grammar
   * @return a grammar with productions some subset of the original grammar,
   *         or <CODE>null</CODE> in the special case where no production with
   *         just the start variable on the LHS exists (i.e. the grammar
   *         accepts no language, though if a grammar accepts no language this
   *         method is NOT gauranteed to return <CODE>null</CODE>)
   */
  public static UnrestrictedGrammar optimize(Grammar grammar) {
    String startVariable = grammar.getStartVariable();
    List<Production> prods = grammar.getProductions();
    // Which symbols in the grammar may possibly lead to just
    // terminals? First, we just add all those symbols with just
    // terminals on the right hand side.
    Set<String> terminating = new HashSet<>();
    // Add those variables that lead to success.
    List<Boolean> added = new ArrayList<>();
    for (int i = 0; i < prods.size(); i++) {
      added.set(i, false);
      if (prods.get(i).getVariablesOnRHS().size() == 0) {
        terminating.addAll(prods.get(i).getSymbols());
        added.set(i, true);
      }
    }
    // Repeat
    boolean changed;
    do {
      changed = false;
      // If a production has only "terminating" variables, add it.
      for (int i = 0; i < prods.size(); i++) {
        List<String> l = prods.get(i).getVariablesOnRHS();
        if (!added.get(i) && terminating.containsAll(l)) {
          terminating.addAll(prods.get(i).getSymbols());
          added.set(i, changed = true);
        }
      }
    } while (changed);
    UnrestrictedGrammar g = new UnrestrictedGrammar();
    g.setStartVariable(grammar.getStartVariable());
    // Need to find a production with just the start var on LHS.
    int i;
    for (i = 0; i < prods.size(); i++)
      if (added.get(i) && prods.get(i).getLHS().equals(startVariable)) break;
    if (i == prods.size()) return null;
    g.addProduction(prods.get(i));
    added.set(i, false);
    for (i = 0; i < prods.size(); i++) if (added.get(i)) g.addProduction(prods.get(i));
    return g;
  }
}
