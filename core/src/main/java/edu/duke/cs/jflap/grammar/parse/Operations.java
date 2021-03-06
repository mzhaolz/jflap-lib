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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import javax.swing.JOptionPane;

import com.google.common.collect.Lists;

import edu.duke.cs.jflap.grammar.Grammar;
import edu.duke.cs.jflap.grammar.Production;
import edu.duke.cs.jflap.grammar.cfg.ContextFreeGrammar;

/**
 * This is a utility class for finding out things about a grammar for purposes
 * of parsing.
 *
 * @author Thomas Finley
 */
public class Operations {
	/** The cached first sets, maps from grammars to first sets. */
	private static WeakHashMap<Grammar, Map<String, Set<String>>> CACHED_FIRST = new WeakHashMap<>();

	/** The cached follow sets, maps from grammars to follow sets. */
	private static WeakHashMap<Grammar, Map<String, Set<String>>> CACHED_FOLLOW = new WeakHashMap<>();

	/**
	 * The cached variables to productions maps, maps from grammars to maps from
	 * variables to productions on that variable.
	 */
	private static WeakHashMap<Grammar, Map<String, Set<Production>>> CACHED_VPMAP = new WeakHashMap<>();

	/** The terminal used to indicate the position in an item. */
	public static final char ITEM_POSITION = '\u00B7';

	/**
	 * Given a set of items, this produces the closure of that set.
	 *
	 * @param grammar
	 *            the grammar for which this production is taking place
	 * @param items
	 *            the set of items
	 * @return a set containing the closure of those items
	 */
	public static Set<Production> closure(final Grammar grammar, Set<Production> items) {
		items = new HashSet<>(items);
		Set<Production> closure = new HashSet<>(items);

		final Map<String, Set<Production>> vp = getVariableProductionMap(grammar);

		while (true) {
			final Set<Production> currentStep = new HashSet<>();
			final Iterator<Production> it = closure.iterator();
			while (it.hasNext()) {
				final Production item = it.next();
				// Find what's on this production.
				int p = item.getRHS().indexOf(ITEM_POSITION);
				p++; // We want what's after this.
				if (p == item.getRHS().length()) {
					continue;
				}
				// We want all productions with this variable.
				final String var = item.getRHS().substring(p, p + 1);
				final Set<Production> ps = vp.get(var);
				if (ps == null) {
					continue;
				}
				final Iterator<Production> pIt = ps.iterator();
				while (pIt.hasNext()) {
					final Production cp = pIt.next();
					currentStep.add(new Production(var, ITEM_POSITION + cp.getRHS()));
				}
			}
			if (!items.addAll(currentStep)) {
				return items;
			}
			closure = currentStep;
		}
	}

	/**
	 * Calculate the first sets of a grammar.
	 *
	 * @param grammar
	 *            the grammar to calculate first sets for
	 * @return a map of symbols in the grammar to the first sets of that symbol
	 *         for this grammar
	 */
	public static Map<String, Set<String>> first(final Grammar grammar) {
		if (CACHED_FIRST.containsKey(grammar)) {
			return CACHED_FIRST.get(grammar);
		}
		final Map<String, Set<String>> first = new HashMap<>();
		// Put the terminals in the map.
		final List<String> terminals = grammar.getTerminals();
		for (int i = 0; i < terminals.size(); i++) {
			final Set<String> termSet = new HashSet<>();
			termSet.add(terminals.get(i));
			first.put(terminals.get(i), termSet);
		}
		// Put the variables in the map as empty sets.
		final List<String> variables = grammar.getVariables();
		for (int i = 0; i < variables.size(); i++) {
			first.put(variables.get(i), new HashSet<>());
		}

		// Repeatedly go over the productions until there is no more
		// change.
		boolean hasChanged = true;
		final List<Production> productions = grammar.getProductions();
		while (hasChanged) {
			hasChanged = false;
			for (int i = 0; i < productions.size(); i++) {
				final String variable = productions.get(i).getLHS();
				final String rhs = productions.get(i).getRHS();
				final Set<String> firstRhs = first(first, rhs);
				if (setForKey(first, variable).addAll(firstRhs)) {
					hasChanged = true;
				}
			}
		}
		CACHED_FIRST.put(grammar, Collections.unmodifiableMap(first));
		return first(grammar);
	}

	/**
	 * Given a first map as returned by {@link #first(Grammar)} and a string
	 * containing some sequence of symbols, return the first for that sequence.
	 *
	 * @param firstSets
	 *            the map of single symbols to a map
	 * @param sequence
	 *            a string of symbols
	 * @return the first set for that sequence of symbols
	 */
	public static Set<String> first(final Map<String, Set<String>> firstSets, final String sequence) {
		final Set<String> first = new HashSet<>();
		if (sequence.equals("")) {
			first.add("");
		}
		for (int j = 0; j < sequence.length(); j++) {
			final Set<String> s = setForKey(firstSets, sequence.substring(j, j + 1));
			if (!s.contains("")) {
				// Doesn't contain lambda. Add it and get the
				// hell out of dodge.
				first.addAll(s);
				break;
			}
			// Does contain lambda. Damn it.
			if (j != sequence.length() - 1) {
				s.remove("");
			}
			first.addAll(s);
			if (j != sequence.length() - 1) {
				s.add("");
			}
		}
		return first;
	}

	/**
	 * Given a grammar, this will return the follow mappings. This returns a map
	 * from the non-terminals in the grammar to the follow sets.
	 *
	 * @param grammar
	 *            the grammar to calculate follow sets for
	 * @return the map of non-terminals to the follow sets
	 */
	public static Map<String, Set<String>> follow(final Grammar grammar) {
		if (CACHED_FOLLOW.containsKey(grammar)) {
			return CACHED_FOLLOW.get(grammar);
		}
		final Map<String, Set<String>> follow = new HashMap<>();
		// Add the mapping from the initial variable to the end of
		// string character.
		final Set<String> initialSet = new HashSet<>();
		initialSet.add("$");
		follow.put(grammar.getStartVariable(), initialSet);
		// Make every follow mapping empty for now.
		final List<String> variables = grammar.getVariables();
		for (int i = 0; i < variables.size(); i++) {
			if (!variables.get(i).equals(grammar.getStartVariable())) {
				follow.put(variables.get(i), new HashSet<>());
			}
		}
		// Get the first sets.
		final Map<String, Set<String>> firstSets = first(grammar);
		// Iterate repeatedly over the productions until we're
		// completely done.
		final List<Production> productions = grammar.getProductions();
		boolean hasChanged = true;
		while (hasChanged) {
			hasChanged = false;
			for (int i = 0; i < productions.size(); i++) {
				final String variable = productions.get(i).getLHS();
				final String rhs = productions.get(i).getRHS();
				for (int j = 0; j < rhs.length(); j++) {
					final String rhsVariable = rhs.substring(j, j + 1);
					if (!grammar.isVariable(rhsVariable)) {
						continue;
					}
					final Set<String> firstFollowing = first(firstSets, rhs.substring(j + 1));
					// Is lambda in that following the variable? For
					// A->aBb where lambda is in FIRST(b), everything
					// in FOLLOW(A) is in FOLLOW(B).
					if (firstFollowing.remove("")) {
						if (setForKey(follow, rhsVariable).addAll(setForKey(follow, variable))) {
							hasChanged = true;
						}
					}
					// For A->aBb, everything in FIRST(b) except
					// lambda is put in FOLLOW(B).
					if (setForKey(follow, rhsVariable).addAll(firstFollowing)) {
						hasChanged = true;
					}
				}
			}
		}
		CACHED_FOLLOW.put(grammar, Collections.unmodifiableMap(follow));
		return follow(grammar);
	}

	/**
	 * This will return an augmented grammar, given a grammar.
	 *
	 * @param grammar
	 *            the grammar to augment
	 * @return the grammar augmented
	 */
	public static Grammar getAugmentedGrammar(final Grammar grammar) {
		final String start = grammar.getStartVariable();
		final Grammar g = new ContextFreeGrammar();
		g.setStartVariable(start);
		final List<Production> prods = grammar.getProductions();
		final Production startProduction = new Production(start, start);
		try {
			g.addProduction(startProduction);
		} catch (final IllegalArgumentException e) {
			return null;
		}
		startProduction.setLHS(start + "'");
		for (int i = 0; i < prods.size(); i++) {
			g.addProduction(prods.get(i));
		}
		return g;
	}

	/**
	 * Returns all the symbols possible to do a goto for on a particular set of
	 * items.
	 *
	 * @param items
	 *            the set of items
	 * @return an array containing all the symbols one can do a goto on for this
	 *         item set
	 */
	public static List<String> getCanGoto(final Set<Production> items) {
		final Iterator<Production> it = items.iterator();
		final Set<String> symbols = new HashSet<>();
		while (it.hasNext()) {
			final Production item = it.next();
			final int position = item.getRHS().indexOf(ITEM_POSITION) + 1;
			if (position == item.getRHS().length()) {
				continue;
			}
			symbols.add(item.getRHS().substring(position, position + 1));
		}
		return Lists.newArrayList(symbols);
	}

	/**
	 * Given a production, this returns the list of productions with the various
	 * permutations of items, with the item position indicator in every
	 * position.
	 *
	 * @param production
	 *            the production
	 * @return an array of productions, each indicating an item
	 */
	public static List<Production> getItems(final Production production) {
		final StringBuffer sb = new StringBuffer(production.getRHS());
		final String rhs = production.getRHS();
		final List<Production> items = new ArrayList<>();
		for (int i = 0; i <= rhs.length(); i++) {
			sb.insert(i, ITEM_POSITION);
			items.add(new Production(production.getLHS(), sb.toString()));
			sb.deleteCharAt(i);
		}
		return items;
	}

	/**
	 * Given a grammar, returns a mapping of variables in the grammar to a set
	 * of productions on that variable.
	 *
	 * @param grammar
	 *            the grammar to get a map for
	 * @return the map of variables to productions
	 */
	public static Map<String, Set<Production>> getVariableProductionMap(final Grammar grammar) {
		if (CACHED_VPMAP.containsKey(grammar)) {
			return Collections.unmodifiableMap(CACHED_VPMAP.get(grammar));
		}
		final Map<String, Set<Production>> vp = new HashMap<>();
		CACHED_VPMAP.put(grammar, vp);
		final List<Production> p = grammar.getProductions();
		for (int i = 0; i < p.size(); i++) {
			if (!vp.containsKey(p.get(i).getLHS())) {
				vp.put(p.get(i).getLHS(), new HashSet<Production>());
			}
			vp.get(p.get(i).getLHS()).add(p.get(i));
		}
		return getVariableProductionMap(grammar);
	}

	/**
	 * Given a grammar, a set of items, and a grammar symbol, return the goto of
	 * this set on that symbol for this grammar.
	 *
	 * @param grammar
	 *            the grammar to calculate goto on
	 * @param items
	 *            the set of items (productions) for goto
	 * @param symbol
	 *            the symbol to use for goto
	 */
	public static Set<Production> goTo(final Grammar grammar, final Set<Production> items, final String symbol) {
		final Set<Production> more = new HashSet<>();
		final Iterator<Production> it = items.iterator();
		while (it.hasNext()) {
			final Production item = it.next();
			int p = item.getRHS().indexOf(ITEM_POSITION);
			p++; // We want what's after this.
			if (p == item.getRHS().length()) {
				continue;
			}
			// We want all productions with this variable.
			final String var = item.getRHS().substring(p, p + 1);
			if (!var.equals(symbol)) {
				continue;
			}

			final String newRhs = item.getRHS().substring(0, p - 1) + item.getRHS().substring(p, p + 1) + ITEM_POSITION
					+ item.getRHS().substring(p + 1, item.getRHS().length());
			more.add(new Production(item.getLHS(), newRhs));
		}
		return closure(grammar, more);
	}

	/**
	 * This returns if a grammar is LL(1).
	 *
	 * @param grammar
	 *            the grammar to test
	 * @return if the grammar is LL(1)
	 */
	public static boolean isLL1(final Grammar grammar) {
		final Map<String, Set<String>> first = first(grammar);
		final Map<String, Set<String>> follow = follow(grammar);
		if (follow == null) {
			JOptionPane.showMessageDialog(null, "JFLAP failed to find a follow set.", "Error",
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
		final Map<String, List<Production>> varToProd = new HashMap<>();

		List<Production> productions = grammar.getProductions();
		for (int i = 0; i < productions.size(); i++) {
			final String variable = productions.get(i).getLHS();
			if (!varToProd.containsKey(variable)) {
				varToProd.put(variable, new ArrayList<>());
			}
			varToProd.get(variable).add(productions.get(i));
		}
		final List<String> variables = grammar.getVariables();
		for (int i = 0; i < variables.size(); i++) {
			final Set<String> followVar = follow.get(variables.get(i));
			final List<Production> varList = varToProd.get(variables.get(i));
			if (varList == null) {
				JOptionPane.showMessageDialog(null,
						"JFLAP failed to find a variable.  You may have used a variable on the right hand side without providing a derivation for it.",
						"Error", JOptionPane.ERROR_MESSAGE);
				return false;
			}
			productions = varList;
			for (int j = 0; j < productions.size(); j++) {
				final String alpha = productions.get(j).getRHS();
				final Set<String> alphaFirst = first(first, alpha);
				for (int k = j + 1; k < productions.size(); k++) {
					final String beta = productions.get(k).getRHS();
					final Set<String> betaFirst = first(first, beta);
					// Condition 1 & 2
					if (betaFirst.removeAll(alphaFirst)) {
						return false;
					}
					// Condition 3
					if (betaFirst.contains("")) {
						if (alphaFirst.removeAll(followVar)) {
							return false;
						}
					}
					if (alphaFirst.contains("")) {
						if (betaFirst.removeAll(followVar)) {
							return false;
						}
					}
				}
			}
		}
		return true;
	}

	/**
	 * Given a map to sets and a key, return the set.
	 */
	private static Set<String> setForKey(final Map<String, Set<String>> map, final String key) {
		return map.get(key);
	}

	/**
	 * Dang class ain't for the instantiating!
	 */
	private Operations() {
	}
}
