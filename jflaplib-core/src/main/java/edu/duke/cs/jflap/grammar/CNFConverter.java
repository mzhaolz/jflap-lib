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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.google.common.collect.Lists;

/**
 * Converts grammars to Chomsky normal form.
 *
 * @author Thomas Finley
 */
public class CNFConverter {
	/**
	 * This is not a directory of productions in the grammar as such, but rather
	 * more of a catalog of those strings that certain left hand sides mapped to
	 * at one point. The idea is to avoid, as much as is sensibly possible,
	 * redundency in variable assignments.
	 */
	private class ProductionDirectory {
		/** The productions. */
		private final List<Production> productions = new ArrayList<>();

		/** The map of LHS to multiple RHS stored as sets. */
		private final Map<String, Set<String>> lhsToRhs = new TreeMap<>();

		/**
		 * The map of RHS to a LHS, given that the RHS of the production is
		 * unique to the LHS of the production (that is, that LHS maps only to
		 * this RHS).
		 */
		private final Map<String, String> rhsToLhs = new HashMap<>();

		/**
		 * Instantiates a production directory.
		 */
		public ProductionDirectory(final Grammar grammar) {
			final List<Production> p = grammar.getProductions();
			// Create the map of LHSes to RHSes.
			for (int i = 0; i < p.size(); i++) {
				final String lhs = p.get(i).getLHS(), rhs = p.get(i).getRHS();
				if (rhs.indexOf('(') != -1) {
					throw new IllegalArgumentException("Grammar has the ( character, which is reserved.");
				}
				if (rhs.indexOf(')') != -1) {
					throw new IllegalArgumentException("Grammar has the ) character, which is reserved.");
				}
				// Add it to the list of productions.
				productions.add(p.get(i));
				// Check the right hand side map.
				Set<String> rhses = lhsToRhs.get(lhs);
				if (rhses == null) {
					rhses = new HashSet<>();
					lhsToRhs.put(lhs, rhses);
				}
				rhses.add(rhs);
			}
			// Creates the map of RHSes to LHSes.
			final Iterator<Map.Entry<String, Set<String>>> it = lhsToRhs.entrySet().iterator();
			while (it.hasNext()) {
				final Map.Entry<String, Set<String>> entry = it.next();
				final Set<String> rhses = entry.getValue();
				final Iterator<String> it2 = rhses.iterator();
				final String rhs = it2.next();
				if (it2.hasNext()) {
					continue;
				}
				rhsToLhs.put(rhs, entry.getKey());
			}
		}

		/**
		 * After the first initialization, this can be used to add productions.
		 * The left hand side must not have been in any production added before,
		 * or in the initializations.
		 *
		 * @param production
		 *            the production to add
		 */
		public void add(final Production production) {
			final String lhs = production.getLHS(), rhs = production.getRHS();
			// Does the RHS already have a unique mapping?
			if (rhsToLhs.containsKey(rhs)) {
				throw new IllegalArgumentException(rhs + " already represented by " + rhsToLhs.get(rhs));
			}
			// Add the production.
			Set<String> rhses = lhsToRhs.get(lhs);
			if (rhses == null) {
				rhses = new HashSet<>();
				lhsToRhs.put(lhs, rhses);
			}
			rhses.add(rhs);
			rhsToLhs.put(rhs, lhs);
		}

		/**
		 * Returns a LHS for a given RHS, if that RHS is unique for a LHS.
		 *
		 * @param lhs
		 *            the RHS to check for a unique representation of
		 * @return a RHS that maps to this LHS uniquely, i.e., for LHS->RHS,
		 *         there is no other rule predicated on LHS
		 */
		public String getLeft(final String rhs) {
			return rhsToLhs.get(rhs);
		}
	}

	public static Grammar convert(final Grammar grammar) {
		Grammar g;
		try {
			g = grammar.getClass().newInstance();
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
		g.addProductions(convert(grammar.getProductions()));
		g.setStartVariable(grammar.getStartVariable());
		return g;
	}

	/**
	 * Given an array of productions, this returns the proper replacements of
	 * the "()" rules.
	 *
	 * @param list
	 *            the array of productions
	 * @return an equivalent set of productions
	 * @throws UnsupportedOperationException
	 *             if the number of variables needed exceeds 26
	 */
	public static List<Production> convert(final List<Production> list) {
		// Figure out what we need, and what's available.
		final TreeSet<String> vars = new TreeSet<>(); // Set of available vars.
		for (char c = 'A'; c <= 'Z'; c++) {
			vars.add("" + c);
		}
		final TreeSet<String> unresolved = new TreeSet<>(); // Set of vars
															// needing
		// conversion.
		for (int i = 0; i < list.size(); i++) {
			final List<String> tokens = separateString(list.get(i).getRHS());
			for (int j = 0; j < tokens.size(); j++) {
				if (tokens.get(i).length() == 1) {
					vars.remove(tokens.get(j));
				} else {
					unresolved.add(tokens.get(j));
				}
			}
			vars.remove(list.get(i).getLHS());
		}
		// Can it be done?
		final int needed = unresolved.size() + 26 - vars.size();
		if (unresolved.size() > vars.size()) {
			// We canna do it, captain!
			throw new UnsupportedOperationException("26 variables available, but " + needed + " needed!");
		}
		// Build the replacement map.
		final HashMap<String, String> replacements = new HashMap<>();
		final Iterator<String> it = unresolved.iterator(), it2 = vars.iterator();
		while (it.hasNext()) {
			replacements.put(it.next(), it2.next());
		}
		// Make the substitutions.
		final List<Production> pnew = new ArrayList<>();
		for (int i = 0; i < list.size(); i++) {
			final List<String> tokens = separateString(list.get(i).getRHS());
			String rhs = "";
			for (int j = 0; j < tokens.size(); j++) {
				if (tokens.get(i).length() == 1) {
					rhs += tokens.get(j);
				} else {
					rhs += replacements.get(tokens.get(j));
				}
			}
			String lhs = list.get(i).getLHS();
			if (lhs.length() != 1) {
				lhs = replacements.get(lhs);
			}
			pnew.add(new Production(lhs, rhs));
		}
		return pnew;
	}

	/**
	 * Breaks a string of symbols into separate symbols.
	 *
	 * @param string
	 *            the string of symbols
	 * @return the array of separate symbols
	 * @throws IllegalArgumentException
	 *             if the string has anything that interferes with productions
	 */
	public static List<String> separateString(final String string) {
		final LinkedList<String> list = new LinkedList<>();
		for (int i = string.length() - 1; i >= 0; i--) {
			final int start = i;
			if (string.charAt(i) != ')') {
				list.addFirst(string.substring(i, i + 1));
				continue;
			}
			while (i > 0 && string.charAt(i) != '(') {
				i--;
			}
			if (i > 0) {
				i--;
			}
			list.addFirst(string.substring(i, start + 1));
		}
		return list;
	}

	/**
	 * In the {@link #getLeft} function, a new production may be added. After
	 * the function is called, this variable will hold if an addition was made.
	 */
	private boolean leftAdded;

	/** The number of variable things assigned already. */
	private int numVariables = 0;

	/** The production directory. */
	private final ProductionDirectory productionDirectory;

	/** The grammar we're converting. */
	private final Grammar grammar;

	/**
	 * Instantiates a new chomsky normal converter.
	 *
	 * @param grammar
	 *            the grammar to convert
	 */
	public CNFConverter(final Grammar grammar) {
		this.grammar = grammar;
		productionDirectory = new ProductionDirectory(grammar);
	}

	/**
	 * Shortens a production to two sides.
	 */

	/**
	 * De-terminalizes a production.
	 *
	 * @param production
	 *            a production to "determinalize"
	 * @return the determinalized production
	 */
	public List<Production> determinalize(final Production production) {
		final List<String> tokens = separateString(production.getRHS());
		final List<Production> list = new ArrayList<>();
		String rhs = "";
		for (int i = 0; i < tokens.size(); i++) {
			if (grammar.isTerminal(tokens.get(i))) {
				final String newR = getLeft(tokens.get(i));
				if (leftAdded) {
					list.add(new Production(newR, tokens.get(i)));
				}
				rhs += newR;
			} else {
				rhs += tokens.get(i);
			}
		}
		list.add(0, new Production(production.getLHS(), rhs));
		return list;
	}

	/**
	 * Given a symbol string A, even a single symbol, this will return a LHS of
	 * a productions where LHS->A is an okay production to map to A.
	 *
	 * @param string
	 *            the symbol string
	 * @return a valid left hand side for that string
	 */
	private String getLeft(final String right) {
		String left = productionDirectory.getLeft(right);
		leftAdded = false;
		if (left != null) {
			return left;
		}
		leftAdded = true;
		left = grammar.isTerminal(right) ? "B(" + right + ")" : "D(" + (++numVariables) + ")";
		final Production p = new Production(left, right);
		productionDirectory.add(p);
		return left;
	}

	/**
	 * Returns if a production is in the proper Chomsky normal form.
	 *
	 * @param production
	 *            the production to test
	 * @return if the production maps to either two variables, or a single
	 *         terminal
	 */
	public boolean isChomsky(final Production production) {
		final List<String> tokens = separateString(production.getRHS());
		switch (tokens.size()) {
		case 1:
			return grammar.isTerminal(tokens.get(0));
		case 2:
			return !(grammar.isTerminal(tokens.get(0)) || grammar.isTerminal(tokens.get(1)));
		default:
			return false;
		}
	}

	/**
	 * Returns the array of productions needed to replace a production.
	 *
	 * @param production
	 *            the production to replace
	 * @return the array of productions that will replace this production
	 * @throws IllegalArgumentException
	 *             if the production does not need to be replaced
	 */
	public List<Production> replacements(final Production production) {
		final String rhs = production.getRHS(), lhs = production.getLHS();
		if (rhs.length() == 1) {
			// Given that unit productions have been removed, this
			// must be a terminal production.
			throw new IllegalArgumentException(production + " is a terminal production!");
		}
		final List<String> tokens = separateString(rhs);
		// Do we need to determinalize this?
		for (int i = 0; i < tokens.size(); i++) {
			if (grammar.isTerminal(tokens.get(i))) {
				return determinalize(production);
			}
		}
		// No termianls to resolve...
		if (tokens.size() == 2) {
			throw new IllegalArgumentException(production + " has two variables already!");
		} else if (tokens.size() < 2) {
			throw new IllegalArgumentException(production + " is in bad form!");
		}
		// Now we're all right.
		final String remainder = rhs.substring(tokens.get(0).length());
		final String left = getLeft(remainder);
		if (leftAdded) {
			return Lists.newArrayList(new Production(lhs, tokens.get(0) + left), new Production(left, remainder));
		}
		return Lists.newArrayList(new Production(lhs, tokens.get(0) + left));
	}
}
