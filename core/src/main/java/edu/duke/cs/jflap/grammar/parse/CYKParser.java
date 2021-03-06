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
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import edu.duke.cs.jflap.grammar.CNFConverter;
import edu.duke.cs.jflap.grammar.Grammar;
import edu.duke.cs.jflap.grammar.LambdaProductionRemover;
import edu.duke.cs.jflap.grammar.Production;
import edu.duke.cs.jflap.grammar.UnitProductionRemover;
import edu.duke.cs.jflap.grammar.UselessProductionRemover;

/**
 * CYK Parser It parses grammar that is in CNF form and returns whether the
 * String is accepted by language or not.
 *
 * @author Kyung Min (Jason) Lee
 *
 */
public class CYKParser {

	/** Start variable of the grammar */
	private static String START_VARIABLE;

	private static Grammar convert(Grammar grammar) {
		if (grammar.isConverted()) {
			return grammar;
		}

		grammar = UselessProductionRemover.getUselessProductionlessGrammar(grammar);
		grammar = LambdaProductionRemover.getLambdaProductionlessGrammar(grammar);
		grammar = UnitProductionRemover.getUnitProductionlessGrammar(grammar);
		return CNFConverter.convert(grammar);
	}

	/**
	 * Thread safe, hopefully.
	 */
	public static boolean convertAndSolve(Grammar grammar, final String target) {
		// may not be thread safe
		grammar = convert(grammar);
		// CYKParser doesn't handle empty string.
		if (target.equals("")) {
			return BruteParser.get(grammar, target).start();
		}
		// lots of repeated work to construct a new CYKParser for the same
		// grammars
		return new CYKParser(grammar).solve(target);
	}

	/** Production array that will contain all the productions of grammar */
	private final List<Production> myProductions;

	/** Length of the input String */
	private int myTargetLength;

	/** Productions that leads to the answer */
	private ArrayList<Production> myAnswerProductions;

	/** Map to store the result of subparts */
	private HashMap<String, HashSet<String>> myMap;

	/** Input string that CYK is trying to parse */
	private String myTarget;

	private OrderCorrectly myOrderComparator;

	/**
	 * Constructor for CYK Parser
	 *
	 * @param grammar
	 *            Grammar that is going to be used in CYK Parsing (It has to be
	 *            in CNF Form)
	 */
	public CYKParser(final Grammar grammar) {
		myProductions = grammar.getProductions();
		START_VARIABLE = grammar.getStartVariable();
	}

	/**
	 * Helper method of solve method that checks the surrounding production
	 *
	 * @param x
	 * @param y
	 */
	private void checkProductions(final int x, final int y) {
		final HashSet<String> tempSet = new HashSet<>();

		for (int i = 0; i < myProductions.size(); i++) {
			for (int k = x; k < y; k++) {
				final String key1 = x + "," + k;
				final String key2 = (k + 1) + "," + y;
				for (final String A : myMap.get(key1)) {
					for (final String B : myMap.get(key2)) {
						final String target = A + B;
						if (myProductions.get(i).getRHS().equals(target)) {
							final HashSet<String> temp2Set = new HashSet<>();
							tempSet.add(myProductions.get(i).getLHS());
							temp2Set.add("0" + A + "/" + key1);
							temp2Set.add("1" + B + "/" + key2);

							final String tempKey = x + "," + y + myProductions.get(i).getLHS();
							if (myMap.get(tempKey) != null) {
								temp2Set.addAll(myMap.get(tempKey));
							}

							myMap.put(tempKey, temp2Set);
						}
					}
				}
			}
		}
		final String key = x + "," + y;
		myMap.put(key, tempSet);
	}

	/**
	 * Helper method of getTrace method which recursively backtracks how Parser
	 * achieved the target String
	 *
	 * @param variable
	 *            Variable that we are chekcing
	 * @param location
	 *            Location of the variable
	 */
	private void getMoreProductions(final String variable, final String location) {
		// //System.out.println("WHOLE MAP = "+myMap);
		if (myMap.get(location + variable) == null) {
			// //System.out.println("Location inside = "+location);
			// //System.out.println("Variable inside = "+variable);
			final int loc = Integer.parseInt(location.substring(0, location.indexOf(",")));
			myAnswerProductions.add(new Production(variable, myTarget.substring(loc, loc + 1)));
			return;
		}

		/*
		 * //System.out.println("Map = "+myMap.get(location+variable));
		 * //System.out.println("Location = "+location);
		 * //System.out.println("Variable = "+variable);
		 */

		final ArrayList<String> optionsA = new ArrayList<>();
		final ArrayList<String> optionsB = new ArrayList<>();

		final List<String> A = new ArrayList<>(Collections.nCopies(2, null));
		final List<String> B = new ArrayList<>(Collections.nCopies(2, null));
		for (final String var : myMap.get(location + variable)) {
			if (var.startsWith("0")) {
				optionsA.add(var);
			} else {
				optionsB.add(var);
			}
		}
		Collections.sort(optionsA, myOrderComparator);
		Collections.sort(optionsB, myOrderComparator);

		// //System.out.println("AAA = "+optionsA);
		// //System.out.println("BBB = "+optionsB);

		boolean isDone = false;
		for (int i = 0; i < optionsA.size(); i++) {
			int index = optionsA.get(i).indexOf("/");
			final String a = optionsA.get(i).substring(1, index);
			final String locA = optionsA.get(i).substring(index + 1);

			for (int j = 0; j < optionsB.size(); j++) {
				index = optionsB.get(i).indexOf("/");
				final String b = optionsB.get(i).substring(1, index);
				final String locB = optionsB.get(i).substring(index + 1);

				final Production p = new Production(variable, a + b);
				for (int k = 0; k < myProductions.size(); k++) {
					if (myProductions.get(k).getLHS().equals(p.getLHS())
							&& myProductions.get(k).getRHS().equals(p.getRHS())) {
						A.set(0, a);
						A.set(1, locA);
						B.set(0, b);
						B.set(1, locB);
						isDone = true;
						break;
					}
				}
				if (isDone) {
					break;
				}
			}
			if (isDone) {
				break;
			}
		}

		// //System.out.println("Selected = "+A.get(0)+" at "+A.get(1));
		// //System.out.println("Selected = "+B.get(0)+" at "+B.get(1));

		myAnswerProductions.add(new Production(variable, A.get(0) + B.get(0)));
		getMoreProductions(A.get(0), A.get(1));
		getMoreProductions(B.get(0), B.get(1));
	}

	/**
	 * Method for getting the trace of how the parser achieved the target String
	 *
	 * @return ArrayList of Productions that was applied to attain target String
	 */
	public ArrayList<Production> getTrace() {
		myAnswerProductions = new ArrayList<>();
		myOrderComparator = new OrderCorrectly();

		// System.out.println("WHOLE MAP = "+myMap);

		getMoreProductions(START_VARIABLE, "0," + (myTargetLength - 1));

		// System.out.println(myAnswerProductions);

		return myAnswerProductions;
	}

	/**
	 * Check whether the grammar accepts the string or not using DP
	 */
	public boolean solve(final String target) {
		myMap = new HashMap<>();
		final int targetLength = target.length();
		myTargetLength = targetLength;
		myTarget = target;

		if (target.equals("")) {
			return false;
		}

		for (int i = 0; i < targetLength; i++) {
			final String a = target.substring(i, i + 1);
			final HashSet<String> temp = new HashSet<>();
			int count = 0;

			for (int j = 0; j < myProductions.size(); j++) {
				if (myProductions.get(j).getRHS().equals(a)) {
					count++;
					temp.add(myProductions.get(j).getLHS());
				}
			}
			final String key = i + "," + i;
			myMap.put(key, temp);
			if (count == 0) {
				return false;
			}
			count = 0;
		}
		// System.out.println(myMap);

		int increment = 1;
		for (int i = 0; i < targetLength; i++) {
			for (int j = 0; j < targetLength; j++) {
				if (targetLength <= j + increment) {
					break;
				}
				final int k = j + increment;
				checkProductions(j, k);

				// System.out.print(myMap.get(j+","+k));
			}
			// System.out.println();
			increment++;
		}

		if (increment == 2) {
			return myMap.get("0," + (targetLength - 1)).contains(START_VARIABLE);
		}

		if (myMap.get("0," + (targetLength - 1)).contains(START_VARIABLE)) {
			return true;
		} else {
			return false;
		}
	}
}

final class OrderCorrectly implements Comparator<Object> {
	@Override
	public int compare(final Object o1, final Object o2) {
		final String str1 = (String) o1;
		final String str2 = (String) o2;
		final int index1 = str1.indexOf("/");
		final String loc1 = str1.substring(index1 + 1);
		final int index1_1 = loc1.indexOf(",");
		final int lc1_1 = Integer.parseInt(loc1.substring(0, index1_1));
		final int lc1_2 = Integer.parseInt(loc1.substring(index1_1 + 1));

		final int index2 = str2.indexOf("/");
		final String loc2 = str2.substring(index2 + 1);
		final int index2_1 = loc2.indexOf(",");
		final int lc2_1 = Integer.parseInt(loc2.substring(0, index2_1));
		final int lc2_2 = Integer.parseInt(loc2.substring(index2_1 + 1));

		if (lc1_1 == lc2_1) {
			return lc1_2 - lc2_2;
		}

		return lc1_1 - lc2_1;
	}
}
