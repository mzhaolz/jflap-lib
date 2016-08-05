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

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class generates {@link edu.duke.cs.jflap.grammar.parse.LLParseTable}s
 * for grammars.
 *
 * @author Thomas Finley
 */
public class LLParseTableGenerator {
  /**
   * Can't instantiate this bad boy sparky.
   */
  private LLParseTableGenerator() {}

  /**
   * Generates a parse table for a particular grammar.
   *
   * @param grammar
   *            the grammar for which a complete parse table should be
   *            generated
   */
  public static LLParseTable generate(Grammar grammar) {
    LLParseTable table = new LLParseTable(grammar);
    Map<String, Set<String>> first = Operations.first(grammar), follow = Operations.follow(grammar);
    List<Production> productions = grammar.getProductions();
    for (int i = 0; i < productions.size(); i++) {
      String alpha = productions.get(i).getRHS();
      String A = productions.get(i).getLHS();
      Set<?> firsts = Operations.first(first, alpha);
      Iterator<?> it = firsts.iterator();
      while (it.hasNext()) {
        String a = (String) it.next();
        if (!a.equals("")) table.addEntry(A, a, alpha);
      }
      if (!firsts.contains("")) continue;
      Set<?> follows = follow.get(A);
      it = follows.iterator();
      while (it.hasNext()) table.addEntry(A, (String) it.next(), alpha);
    }
    return table;
  }
}
