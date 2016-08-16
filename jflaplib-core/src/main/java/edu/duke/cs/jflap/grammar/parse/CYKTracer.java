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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.duke.cs.jflap.grammar.CNFConverter;
import edu.duke.cs.jflap.grammar.Grammar;
import edu.duke.cs.jflap.grammar.LambdaProductionRemover;
import edu.duke.cs.jflap.grammar.Production;
import edu.duke.cs.jflap.grammar.UnitProductionRemover;
import edu.duke.cs.jflap.grammar.UselessProductionRemover;
import edu.duke.cs.jflap.gui.environment.GrammarEnvironment;
import edu.duke.cs.jflap.gui.grammar.GrammarInputPane;
import edu.duke.cs.jflap.gui.grammar.transform.ChomskyPane;
import edu.duke.cs.jflap.gui.grammar.transform.LambdaController;
import edu.duke.cs.jflap.gui.grammar.transform.LambdaPane;
import edu.duke.cs.jflap.gui.grammar.transform.UnitController;
import edu.duke.cs.jflap.gui.grammar.transform.UnitPane;
import edu.duke.cs.jflap.gui.grammar.transform.UselessController;
import edu.duke.cs.jflap.gui.grammar.transform.UselessPane;

/**
 * Class for converting CNF-converted productions back to their original
 * productions
 *
 * @author Kyung Min (Jason) Lee
 *
 */
public class CYKTracer {

	private final Grammar myOriginalGrammar;
	private final ArrayList<Production> myTrace;
	private final ArrayList<Production> myAnswer;
	private HashMap<ArrayList<Production>, Production> myLambdaStepMap;
	private HashMap<ArrayList<Production>, Production> myUnitStepMap;
	private ArrayList<Production> myTempCNF;
	private HashMap<Production, ArrayList<Production>> myCNFMap;

	public CYKTracer(final Grammar grammar, final ArrayList<Production> trace) {
		myOriginalGrammar = grammar;
		myTrace = trace;
		myAnswer = new ArrayList<>();
		initializeLambdaStepMap();
	}

	private void backTrackToCNF() {
		if (myCNFMap == null) {
			backTrackToUnit();
			return;
		}

		// System.out.println("MAP : "+myCNFMap);
		List<Integer> visited = new ArrayList<>();
		for (int i = 0; i < myTrace.size(); i++) {
			final Production target = myTrace.get(i);
			if (myCNFMap.keySet().contains(target)) {
				myAnswer.add(target);
				visited.add(1);
			} else {
				for (final Production p : myCNFMap.keySet()) {
					if (myCNFMap.get(p).contains(target)) {
						// System.out.println(p+" -> " + myCNFMap.get(p)+ "
						// contains "+target);
						visited = searchForRest(myCNFMap.get(p), p, visited);
					}
				}
			}
		}
		// System.out.println("After Backtracking CNF = "+myAnswer);

	}

	private void backTrackToLambda() {
		if (myLambdaStepMap == null) {
			return;
		}
		int index = 0;
		while (index < myAnswer.size()) {
			for (final ArrayList<Production> key : myLambdaStepMap.keySet()) {
				if (myLambdaStepMap.get(key).equals(myAnswer.get(index))) {
					// System.out.println("Found it = "+key);
					// System.out.println("For = "+myAnswer.get(index));
					myAnswer.remove(index);
					int c = 0;
					for (final Production p : key) {
						myAnswer.add(index + c, p);
						c++;
					}
					index = index + key.size() - 1;
				}
			}
			index++;
		}
		// System.out.println("After Backtracking Lambda = "+myAnswer);

	}

	private void backTrackToUnit() {
		if (myUnitStepMap == null) {
			return;
		}
		int index = 0;
		while (index < myAnswer.size()) {
			for (final ArrayList<Production> key : myUnitStepMap.keySet()) {
				if (myUnitStepMap.get(key).equals(myAnswer.get(index))) {
					myAnswer.remove(index);
					int c = 0;
					for (final Production p : key) {
						myAnswer.add(index + c, p);
						c++;
					}
					index = index + key.size() - 1;
				}
			}
			index++;
		}
		// System.out.println("After Backtracking Unit Step = "+myAnswer);

	}

	private void convertToCNF(final CNFConverter converter, Production p) {
		if (!converter.isChomsky(p)) {
			final List<Production> temp = converter.replacements(p);

			for (int j = 0; j < temp.size(); j++) {
				p = temp.get(j);
				convertToCNF(converter, p);
			}
		} else {
			myTempCNF.add(p);
		}
	}

	private void finalizeCNFMap(final HashMap<Production, Production> map) {
		for (final Production p : myCNFMap.keySet()) {
			final ArrayList<Production> temp = new ArrayList<>();
			for (final Production pp : myCNFMap.get(p)) {
				temp.add(map.get(pp));
			}
			myCNFMap.put(p, temp);
		}
	}

	public List<Production> getAnswer() {

		/*
		 * Collections.sort(myAnswer, new Comparator<Production>(){ public int
		 * compare(Production o1, Production o2) { return
		 * (o2.getRHS().length()-o1.getRHS().length()); } });
		 */
		return new ArrayList<>(myAnswer);
	}

	// always str1's length is longer than str2. (Assumption)
	private ArrayList<String> getDifferentVariable(final String str1, final String str2) {

		final ArrayList<String> result = new ArrayList<>();
		final char[] char1 = str1.toCharArray();
		final char[] char2 = str2.toCharArray();

		int index = 0;
		boolean breakOut = false;
		for (final char element : char1) {
			// //System.out.println("index = "+index);
			if (index == char2.length) {
				breakOut = true;
				break;
			}
			// //System.out.println(char1.get(i)+" and "+char2[index]);

			if (element != char2[index]) {
				result.add("" + element);
				index--;
				// //System.out.println("EEEE "+char1.get(i));
			}
			index++;
		}
		if (breakOut) {
			for (int i = index; i < char1.length; i++) {
				result.add("" + char1[i]);
			}
		}
		return result;
	}

	private void initializeChomskyMap(Grammar g) {
		// //System.out.println("Chomsky = "+g);
		final CNFConverter converter = new CNFConverter(g);

		final List<Production> p = g.getProductions();
		boolean chomsky = true;
		for (int i = 0; i < p.size(); i++) {
			chomsky &= converter.isChomsky(p.get(i));
		}

		if (!chomsky) {

			myCNFMap = new HashMap<>();
			final GrammarEnvironment env = new GrammarEnvironment(new GrammarInputPane(g));
			final ChomskyPane cp = new ChomskyPane(env, g);
			final ArrayList<Production> resultList = new ArrayList<>();
			cp.doAll();
			for (int i = 0; i < p.size(); i++) {
				myTempCNF = new ArrayList<>();
				final CNFConverter cv = new CNFConverter(g);

				convertToCNF(cv, p.get(i));
				myCNFMap.put(p.get(i), myTempCNF);
				resultList.addAll(myCNFMap.get(p.get(i)));
			}
			// System.out.println("Initial CNF Map = "+myCNFMap);

			// System.out.println(resultList);
			List<Production> pp = new ArrayList<>();
			final HashMap<Production, Production> originalToCNF = new HashMap<>();
			for (int i = 0; i < pp.size(); i++) {
				pp.add(resultList.get(i));
			}
			pp = CNFConverter.convert(pp);
			// System.out.println("CONverted : "+Arrays.asList(pp));
			for (int i = 0; i < pp.size(); i++) {
				originalToCNF.put(resultList.get(i), pp.get(i));
			}
			// System.out.println("ORiginal = "+originalToCNF);
			finalizeCNFMap(originalToCNF);
			g = cp.getGrammar();

			// System.out.println("FINAL CNF Map = "+myCNFMap);

		}
		// System.out.println(g);
	}

	private void initializeLambdaStepMap() {
		final Set<String> lambdaDerivers = LambdaProductionRemover.getCompleteLambdaSet(myOriginalGrammar);
		Grammar g = myOriginalGrammar;
		// System.out.println("LD = "+lambdaDerivers);
		if (lambdaDerivers.size() > 0) {

			myLambdaStepMap = new HashMap<>();
			final HashMap<String, Production> directLambdaProductions = new HashMap<>();
			final HashMap<String, ArrayList<Production>> indirectLambdaProductions = new HashMap<>();

			final GrammarEnvironment env = new GrammarEnvironment(new GrammarInputPane(myOriginalGrammar));
			final LambdaPane lp = new LambdaPane(env, myOriginalGrammar);
			final LambdaController controller = new LambdaController(lp, myOriginalGrammar);

			controller.doStep();

			// TODO: Check that this is fixed once GUI package is fixed.
			for (final Production production : (HashSet<Production>) controller.getLambdaSet()) {
				directLambdaProductions.put(production.getLHS(), production);
			}

			// //System.out.println("DIRECT = "+directLambdaProductions);
			final List<Production> p = lp.getGrammar().getProductions();

			for (final String key : directLambdaProductions.keySet()) {
				for (int i = 0; i < p.size(); i++) {
					if (p.get(i).getRHS().equals(key)) {
						final ArrayList<Production> temp = new ArrayList<>();
						temp.add(p.get(i));
						temp.add(directLambdaProductions.get(key));
						indirectLambdaProductions.put(p.get(i).getLHS(), temp);
					}
				}
			}
			// //System.out.println("INDIRECT = "+indirectLambdaProductions);

			for (int i = 0; i < p.size(); i++) {
				final List<Production> p2 = LambdaProductionRemover.getProductionsToAddForProduction(p.get(i),
						lambdaDerivers);
				// //System.out.println("Expanding From : "+p.get(i));
				for (int j = 0; j < p2.size(); j++) {
					final ArrayList<Production> temp = new ArrayList<>();

					if (!p2.get(j).equals(p.get(i))) {
						temp.add(p.get(i));
						final ArrayList<String> variables = getDifferentVariable(p.get(i).getRHS(), p2.get(j).getRHS());
						// //System.out.println(p2.get(j)+" Variables =
						// "+variables);
						for (int pp = 0; pp < variables.size(); pp++) {
							if (directLambdaProductions.keySet().contains(variables.get(pp))) {
								temp.add(directLambdaProductions.get(variables.get(pp)));
							} else {
								if (indirectLambdaProductions.keySet().contains(variables.get(pp))) {
									temp.addAll(indirectLambdaProductions.get(variables.get(pp)));
								} else {
									reportError();
								}
							}
						}
						myLambdaStepMap.put(temp, p2.get(j));
					}
					// //System.out.println(temp);
				}
				// //System.out.println();
			}
			controller.doAll();
			g = controller.getGrammar();
		}
		// System.out.println("LAMBDA step Map = "+myLambdaStepMap);
		intializeUnitStepMap(g);
	}

	private void intializeUnitStepMap(Grammar g) {
		final List<Production> units = UnitProductionRemover.getUnitProductions(g);
		if (units.size() > 0) {

			myUnitStepMap = new HashMap<>();

			final GrammarEnvironment env = new GrammarEnvironment(new GrammarInputPane(g));
			final UnitPane up = new UnitPane(env, g);
			final UnitController controller = new UnitController(up, g);
			controller.doStep();
			final HashMap<String, Production> removedUnitProductions = new HashMap<>();

			for (int i = 0; i < units.size(); i++) {
				removedUnitProductions.put(units.get(i).getLHS(), units.get(i));
			}
			// System.out.println("UNIT = "+removedUnitProductions);

			final Grammar unitless = UnitProductionRemover.getUnitProductionlessGrammar(controller.getGrammar(),
					UnitProductionRemover.getVariableDependencyGraph(g));
			final List<Production> temp = unitless.getProductions();
			final ArrayList<Production> productionsToAdd = new ArrayList<>();
			for (int i = 0; i < temp.size(); i++) {
				productionsToAdd.add(temp.get(i));
			}
			// Now the grammar without unit productions
			g = controller.getGrammar();
			final List<Production> p = g.getProductions();
			for (int i = 0; i < p.size(); i++) {
				if (productionsToAdd.contains(p.get(i))) {
					productionsToAdd.remove(p.get(i));
				}
			}
			// System.out.println(productionsToAdd);

			for (int i = 0; i < productionsToAdd.size(); i++) {
				final ArrayList<Production> tempToAdd = new ArrayList<>();
				final String var1 = productionsToAdd.get(i).getLHS();
				if (removedUnitProductions.get(var1) == null) {
					reportError();
				} else {
					tempToAdd.add(removedUnitProductions.get(var1));
					String var2 = removedUnitProductions.get(var1).getRHS();
					boolean isDone = false;

					for (int pp = 0; pp < p.size(); pp++) {
						if (p.get(pp).getLHS().equals(var2)) {
							final String tempStr = p.get(pp).getRHS();
							if (tempStr.equals(productionsToAdd.get(i).getRHS())) {
								tempToAdd.add(p.get(pp));
								isDone = true;
								break;
							}
						}
					}
					while (isDone == false && removedUnitProductions.keySet().contains(var2)) {
						tempToAdd.add(removedUnitProductions.get(var2));
						var2 = removedUnitProductions.get(var2).getRHS();
						for (int pp = 0; pp < p.size(); pp++) {
							if (p.get(pp).getLHS().equals(var2)) {
								final String tempStr = p.get(pp).getRHS();
								if (tempStr.equals(productionsToAdd.get(i).getRHS())) {
									tempToAdd.add(p.get(pp));
									isDone = true;
									break;
								}
							}
						}
					}
				}
				myUnitStepMap.put(tempToAdd, productionsToAdd.get(i));
			}
			controller.doAll();
			g = controller.getGrammar();
		}
		// System.out.println("UNIT STEP MAP = "+myUnitStepMap);
		removeUseless(g);
	}

	private void removeUseless(Grammar g) {
		final Grammar g2 = UselessProductionRemover.getUselessProductionlessGrammar(g);

		final List<Production> p1 = g.getProductions();
		final List<Production> p2 = g2.getProductions();
		if (p1.size() > p2.size()) {

			final GrammarEnvironment env = new GrammarEnvironment(new GrammarInputPane(g));
			final UselessPane up = new UselessPane(env, g);
			final UselessController controller = new UselessController(up, g);
			controller.doAll();
			g = controller.getGrammar();
		}
		initializeChomskyMap(g);
	}

	private void reportError() {
		// System.out.println("ERROR ~ ERROR!");
	}

	private List<Integer> searchForRest(final ArrayList<Production> list, final Production p,
			final List<Integer> visited) {
		final HashSet<Production> visitedProd = new HashSet<>();
		// System.out.println("Searching through "+list);
		final List<Integer> original = new ArrayList<>(visited);
		int count = 0;
		for (int i = 0; i < myTrace.size(); i++) {
			if (list.contains(myTrace.get(i)) && visited.get(i) == 0 && !visitedProd.contains(myTrace.get(i))) {
				// System.out.println("FOUDN = "+myTrace.get(i));
				visited.set(i, 1);
				visitedProd.add(myTrace.get(i));
				count++;
			}
		}
		if (count == list.size()) {
			myAnswer.add(p);
			return visited;
		} else {
			return original;
		}
	}

	public void traceBack() {
		// System.out.println("ANSWER NOW = "+myTrace);
		backTrackToCNF();
		backTrackToUnit();
		backTrackToLambda();

		// System.out.println("size is = "+myAnswer.size());
		if (myAnswer.size() == 0) {
			myAnswer.addAll(myTrace);
			// System.out.println("final answer");
			// System.out.println(myAnswer);
		}
	}
}
