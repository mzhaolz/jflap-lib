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

package edu.duke.cs.jflap.grammar.lsystem;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;

import edu.duke.cs.jflap.grammar.Grammar;
import edu.duke.cs.jflap.grammar.Production;
import edu.duke.cs.jflap.grammar.UnrestrictedGrammar;

/**
 * The <CODE>LSystem</CODE> class represents L-systems. This does not do any
 * simulation of L-systems, but rather has the minimal mathematical definitions
 * required, i.e., the axiom, replacement rules, with some concession given to
 * define parameters for drawing.
 *
 * @author Thomas Finley
 */

// Oh, I'm just doing fine. Thank you very much. Just very well.
// Oh, just fine! Thank you. Very well. Mmm-hmm! I'm just...
public class LSystem implements Serializable {
	private static final long serialVersionUID = 10000L;

	/**
	 * Given a space delimited string, returns a list of the non-whitespace
	 * tokens.
	 *
	 * @param string
	 *            the string to take tokens from
	 * @return a list containing all tokens of the string
	 */
	public static List<String> tokenify(final String string) {
		final StringTokenizer st = new StringTokenizer(string);
		final ArrayList<String> list = new ArrayList<>();
		while (st.hasMoreTokens()) {
			list.add(st.nextToken());
		}
		return list;
	}

	/** The grammar holding the replacement rules. */
	private Map<String, List<List<String>>> symbolToReplacements;

	/** The mapping of keys to values. */
	private final Map<String, String> values;

	/** The axiom. */
	private final List<String> axiom;

	/** Whether or not the L-system has stochiastic properties. */
	private boolean nondeterministic = false;

	/**
	 * Constructs an empty L-System.
	 */
	public LSystem() {
		this("", new UnrestrictedGrammar(), new HashMap<>());
	}

	/**
	 * Constructs a new L-System.
	 *
	 * @param replacements
	 *            the grammar holding the replacement rules, where each
	 *            production has (on the left hand side) the symbol to replace,
	 *            while on the right hand side is a string containing space
	 *            delimited symbols
	 * @param values
	 *            various parameters controlling drawing in the lsystem
	 * @param axiom
	 *            the start symbols as a space delimited string
	 */
	public LSystem(final String axiom, final Grammar replacements, final Map<String, String> values) {
		this.values = Collections.unmodifiableMap(values);
		initReplacements(replacements);
		this.axiom = tokenify(axiom);
	}

	/**
	 * Returns the list of symbols for the axiom.
	 *
	 * @return the list of symbols for the axiom
	 */
	public List<String> getAxiom() {
		return axiom;
	}

	/**
	 * Returns the array of replacements for a symbol.
	 *
	 * @param symbol
	 *            the symbol to get the replacements for
	 * @return an array of lists, where each list is a list of the strings; the
	 *         array will be empty if there are no replacements
	 */
	public List<List<String>> getReplacements(final String symbol) {
		final List<List<String>> toReturn = symbolToReplacements.get(symbol);
		return toReturn == null ? Collections.emptyList() : toReturn;
	}

	/**
	 * Returns the symbols for which there are replacements.
	 *
	 * @return the set of symbols that have replacements in this L-system
	 */
	public Set<String> getSymbolsWithReplacements() {
		return symbolToReplacements.keySet();
	}

	/**
	 * Returns a mapping of names of parameters for the L-system to their
	 * respective values
	 *
	 * @return the map of names of parameters to the parameters themselves
	 */
	public Map<String, String> getValues() {
		return values;
	}

	/**
	 * Initializes the list of rewriting rules.
	 *
	 * @param replacements
	 *            the grammar holding the replacement rules
	 */
	private void initReplacements(final Grammar replacements) {
		final Map<String, List<List<String>>> reps = new HashMap<>();
		final List<Production> p = replacements.getProductions();
		for (int i = 0; i < p.size(); i++) {
			final String replace = p.get(i).getLHS();
			List<List<String>> currentReplacements = null;
			if (!reps.containsKey(replace)) {
				reps.put(replace, currentReplacements = new ArrayList<>());
			} else {
				currentReplacements = reps.get(replace);
			}
			final List<String> currentSubstitution = tokenify(p.get(i).getRHS());
			try {
				final List<String> lastSubstitution = currentReplacements.get(currentReplacements.size() - 1);
				if (!currentSubstitution.equals(lastSubstitution)) {
					nondeterministic = true;
				}
			} catch (final IndexOutOfBoundsException e) {

			}
			currentReplacements.add(currentSubstitution);
		}
		final Iterator<Map.Entry<String, List<List<String>>>> it = reps.entrySet().iterator();
		symbolToReplacements = new TreeMap<>();
		while (it.hasNext()) {
			final Map.Entry<String, List<List<String>>> entry = it.next();
			final List<List<String>> replacementArray = entry.getValue();
			symbolToReplacements.put(entry.getKey(), replacementArray);
		}
	}

	/**
	 * Returns whether the l-system is nondeterministic, i.e., if there are any
	 * symbols that could result in an ambiguous outcome (a sort of stochiastic
	 * thing).
	 *
	 * @return if the l-system is nondeterministic
	 */
	public boolean nondeterministic() {
		return nondeterministic;
	}
}
