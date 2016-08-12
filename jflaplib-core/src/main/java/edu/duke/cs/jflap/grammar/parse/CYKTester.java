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

import com.google.common.collect.Lists;

import java.util.List;

/**
 * CYK Parser tester.
 *
 * @author Kyung Min (Jason) Lee
 *
 */
public class CYKTester {

    public static void main(String[] args) {
        List<Production> productions = Lists.newArrayList(new Production("S", "AD"),
                new Production("D", "SC"), new Production("A", "a"), new Production("C", "b"),
                new Production("S", "CB"), new Production("B", "CE"), new Production("E", "CB"),
                new Production("S", "SS"), new Production("S", "b"), new Production("B", "CC"));

        Grammar g = new UnrestrictedGrammar();
        g.addProductions(productions);
        g.setStartVariable("S");
        String target = "abbbb";
        System.out.println("aa");
        CYKParser parser = new CYKParser(g);
        System.out.println(parser.solve(target));
        System.out.println("Trace = " + parser.getTrace());
    }
}
