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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Class for converting CNF-converted productions back to their original
 * productions
 *
 * @author Kyung Min (Jason) Lee
 *
 */
public class CYKTracer {

    private Grammar myOriginalGrammar;
    private ArrayList<Production> myTrace;
    private ArrayList<Production> myAnswer;
    private HashMap<ArrayList<Production>, Production> myLambdaStepMap;
    private HashMap<ArrayList<Production>, Production> myUnitStepMap;
    private ArrayList<Production> myTempCNF;
    private HashMap<Production, ArrayList<Production>> myCNFMap;

    public CYKTracer(Grammar grammar, ArrayList<Production> trace) {
        myOriginalGrammar = grammar;
        myTrace = trace;
        myAnswer = new ArrayList<>();
        initializeLambdaStepMap();
    }

    private void initializeLambdaStepMap() {
        Set<String> lambdaDerivers = LambdaProductionRemover
                .getCompleteLambdaSet(myOriginalGrammar);
        Grammar g = myOriginalGrammar;
        // System.out.println("LD = "+lambdaDerivers);
        if (lambdaDerivers.size() > 0) {

            myLambdaStepMap = new HashMap<>();
            HashMap<String, Production> directLambdaProductions = new HashMap<>();
            HashMap<String, ArrayList<Production>> indirectLambdaProductions = new HashMap<>();

            GrammarEnvironment env = new GrammarEnvironment(
                    new GrammarInputPane(myOriginalGrammar));
            LambdaPane lp = new LambdaPane(env, myOriginalGrammar);
            LambdaController controller = new LambdaController(lp, myOriginalGrammar);

            controller.doStep();

            // TODO: Check that this is fixed once GUI package is fixed.
            for (Production production : (HashSet<Production>) controller.getLambdaSet()) {
                directLambdaProductions.put(production.getLHS(), production);
            }

            // //System.out.println("DIRECT = "+directLambdaProductions);
            List<Production> p = lp.getGrammar().getProductions();

            for (String key : directLambdaProductions.keySet()) {
                for (int i = 0; i < p.size(); i++) {
                    if (p.get(i).getRHS().equals(key)) {
                        ArrayList<Production> temp = new ArrayList<>();
                        temp.add(p.get(i));
                        temp.add(directLambdaProductions.get(key));
                        indirectLambdaProductions.put(p.get(i).getLHS(), temp);
                    }
                }
            }
            // //System.out.println("INDIRECT = "+indirectLambdaProductions);

            for (int i = 0; i < p.size(); i++) {
                List<Production> p2 = LambdaProductionRemover
                        .getProductionsToAddForProduction(p.get(i), lambdaDerivers);
                // //System.out.println("Expanding From : "+p.get(i));
                for (int j = 0; j < p2.size(); j++) {
                    ArrayList<Production> temp = new ArrayList<>();

                    if (!p2.get(j).equals(p.get(i))) {
                        temp.add(p.get(i));
                        ArrayList<String> variables = getDifferentVariable(p.get(i).getRHS(),
                                p2.get(j).getRHS());
                        // //System.out.println(p2.get(j)+" Variables =
                        // "+variables);
                        for (int pp = 0; pp < variables.size(); pp++) {
                            if (directLambdaProductions.keySet().contains(variables.get(pp))) {
                                temp.add(directLambdaProductions.get(variables.get(pp)));
                            } else {
                                if (indirectLambdaProductions.keySet()
                                        .contains(variables.get(pp))) {
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
        List<Production> units = UnitProductionRemover.getUnitProductions(g);
        if (units.size() > 0) {

            myUnitStepMap = new HashMap<>();

            GrammarEnvironment env = new GrammarEnvironment(new GrammarInputPane(g));
            UnitPane up = new UnitPane(env, g);
            UnitController controller = new UnitController(up, g);
            controller.doStep();
            HashMap<String, Production> removedUnitProductions = new HashMap<>();

            for (int i = 0; i < units.size(); i++) {
                removedUnitProductions.put(units.get(i).getLHS(), units.get(i));
            }
            // System.out.println("UNIT = "+removedUnitProductions);

            Grammar unitless = UnitProductionRemover.getUnitProductionlessGrammar(
                    controller.getGrammar(), UnitProductionRemover.getVariableDependencyGraph(g));
            List<Production> temp = unitless.getProductions();
            ArrayList<Production> productionsToAdd = new ArrayList<>();
            for (int i = 0; i < temp.size(); i++) {
                productionsToAdd.add(temp.get(i));
            }
            // Now the grammar without unit productions
            g = controller.getGrammar();
            List<Production> p = g.getProductions();
            for (int i = 0; i < p.size(); i++) {
                if (productionsToAdd.contains(p.get(i))) {
                    productionsToAdd.remove(p.get(i));
                }
            }
            // System.out.println(productionsToAdd);

            for (int i = 0; i < productionsToAdd.size(); i++) {
                ArrayList<Production> tempToAdd = new ArrayList<>();
                String var1 = productionsToAdd.get(i).getLHS();
                if (removedUnitProductions.get(var1) == null) {
                    reportError();
                } else {
                    tempToAdd.add(removedUnitProductions.get(var1));
                    String var2 = removedUnitProductions.get(var1).getRHS();
                    boolean isDone = false;

                    for (int pp = 0; pp < p.size(); pp++) {
                        if (p.get(pp).getLHS().equals(var2)) {
                            String tempStr = p.get(pp).getRHS();
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
                                String tempStr = p.get(pp).getRHS();
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
        Grammar g2 = UselessProductionRemover.getUselessProductionlessGrammar(g);

        List<Production> p1 = g.getProductions();
        List<Production> p2 = g2.getProductions();
        if (p1.size() > p2.size()) {

            GrammarEnvironment env = new GrammarEnvironment(new GrammarInputPane(g));
            UselessPane up = new UselessPane(env, g);
            UselessController controller = new UselessController(up, g);
            controller.doAll();
            g = controller.getGrammar();
        }
        initializeChomskyMap(g);
    }

    private void initializeChomskyMap(Grammar g) {
        // //System.out.println("Chomsky = "+g);
        CNFConverter converter = new CNFConverter(g);

        List<Production> p = g.getProductions();
        boolean chomsky = true;
        for (int i = 0; i < p.size(); i++) {
            chomsky &= converter.isChomsky(p.get(i));
        }

        if (!chomsky) {

            myCNFMap = new HashMap<>();
            GrammarEnvironment env = new GrammarEnvironment(new GrammarInputPane(g));
            ChomskyPane cp = new ChomskyPane(env, g);
            ArrayList<Production> resultList = new ArrayList<>();
            cp.doAll();
            for (int i = 0; i < p.size(); i++) {
                myTempCNF = new ArrayList<>();
                CNFConverter cv = new CNFConverter(g);

                convertToCNF(cv, p.get(i));
                myCNFMap.put(p.get(i), myTempCNF);
                resultList.addAll(myCNFMap.get(p.get(i)));
            }
            // System.out.println("Initial CNF Map = "+myCNFMap);

            // System.out.println(resultList);
            List<Production> pp = new ArrayList<>();
            HashMap<Production, Production> originalToCNF = new HashMap<>();
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

    private void finalizeCNFMap(HashMap<Production, Production> map) {
        for (Production p : myCNFMap.keySet()) {
            ArrayList<Production> temp = new ArrayList<>();
            for (Production pp : myCNFMap.get(p)) {
                temp.add(map.get(pp));
            }
            myCNFMap.put(p, temp);
        }
    }

    private void convertToCNF(CNFConverter converter, Production p) {
        if (!converter.isChomsky(p)) {
            List<Production> temp = converter.replacements(p);

            for (int j = 0; j < temp.size(); j++) {
                p = temp.get(j);
                convertToCNF(converter, p);
            }
        } else {
            myTempCNF.add(p);
        }
    }

    // always str1's length is longer than str2. (Assumption)
    private ArrayList<String> getDifferentVariable(String str1, String str2) {

        ArrayList<String> result = new ArrayList<>();
        char[] char1 = str1.toCharArray();
        char[] char2 = str2.toCharArray();

        int index = 0;
        boolean breakOut = false;
        for (char element : char1) {
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

    private void backTrackToCNF() {
        if (myCNFMap == null) {
            backTrackToUnit();
            return;
        }

        // System.out.println("MAP : "+myCNFMap);
        List<Integer> visited = new ArrayList<>();
        for (int i = 0; i < myTrace.size(); i++) {
            Production target = myTrace.get(i);
            if (myCNFMap.keySet().contains(target)) {
                myAnswer.add(target);
                visited.add(1);
            } else {
                for (Production p : myCNFMap.keySet()) {
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

    private List<Integer> searchForRest(ArrayList<Production> list, Production p,
            List<Integer> visited) {
        HashSet<Production> visitedProd = new HashSet<>();
        // System.out.println("Searching through "+list);
        List<Integer> original = new ArrayList<>(visited);
        int count = 0;
        for (int i = 0; i < myTrace.size(); i++) {
            if (list.contains(myTrace.get(i)) && visited.get(i) == 0
                    && !visitedProd.contains(myTrace.get(i))) {
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

    private void backTrackToUnit() {
        if (myUnitStepMap == null) {
            return;
        }
        int index = 0;
        while (index < myAnswer.size()) {
            for (ArrayList<Production> key : myUnitStepMap.keySet()) {
                if (myUnitStepMap.get(key).equals(myAnswer.get(index))) {
                    myAnswer.remove(index);
                    int c = 0;
                    for (Production p : key) {
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

    private void backTrackToLambda() {
        if (myLambdaStepMap == null) {
            return;
        }
        int index = 0;
        while (index < myAnswer.size()) {
            for (ArrayList<Production> key : myLambdaStepMap.keySet()) {
                if (myLambdaStepMap.get(key).equals(myAnswer.get(index))) {
                    // System.out.println("Found it = "+key);
                    // System.out.println("For = "+myAnswer.get(index));
                    myAnswer.remove(index);
                    int c = 0;
                    for (Production p : key) {
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

    public List<Production> getAnswer() {

        /*
         * Collections.sort(myAnswer, new Comparator<Production>(){ public int
         * compare(Production o1, Production o2) { return
         * (o2.getRHS().length()-o1.getRHS().length()); } });
         */
        return new ArrayList<>(myAnswer);
    }

    private void reportError() {
        // System.out.println("ERROR ~ ERROR!");
    }
}
