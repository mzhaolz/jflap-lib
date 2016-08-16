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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import edu.duke.cs.jflap.gui.environment.EnvironmentFrame;

/**
 * The grammar object is the root class for the representation of all forms of
 * grammars, including regular and context-free grammars. This object simply
 * maintains a structure that holds and maintains the data pertinent to the
 * definition of a grammar.
 *
 * @author Ryan Cavalcante
 */
public abstract class Grammar implements Serializable, Cloneable {
	private static final long serialVersionUID = 100L;
	private final Logger logger = LoggerFactory.getLogger(Grammar.class);

	private EnvironmentFrame myEnvFrame = null;

	private String fileName = "";

	/** Set of Variables. */
	protected Set<String> myVariables;

	/** Set of Terminals. */
	protected Set<String> myTerminals;

	/** Start variable. */
	protected String myStartVariable;

	/** Set of Production rules. */
	protected List<Production> myProductions = new ArrayList<>();

	/**
	 * Creates an instance of <CODE>Grammar</CODE>. The created instance has no
	 * productions, no terminals, no variables, and specifically no start
	 * variable.
	 */
	public Grammar() {
		myVariables = new HashSet<>();
		myTerminals = new HashSet<>();
		myStartVariable = null;
	}

	/**
	 * Adds <CODE>production</CODE> to the set of productions in the grammar.
	 *
	 * @param production
	 *            the production to be added.
	 * @throws IllegalArgumentException
	 *             if the production is unsuitable somehow
	 */
	public void addProduction(final Production production) {
		checkProduction(production);
		new GrammarChecker();
		/** if production already in grammar. */
		if (GrammarChecker.isProductionInGrammar(production, this)) {
			return;
		}
		myProductions.add(production);

		/**
		 * add all new variables introduced by production to set of variables.
		 */
		final List<String> variablesInProduction = production.getVariables();
		for (final String variableInProduction : variablesInProduction) {
			if (!myVariables.contains(variableInProduction)) {
				addVariable(variableInProduction);
			}
		}

		/**
		 * add all new terminals introduced by production to set of terminals.
		 */
		final List<String> terminalsInProduction = production.getTerminals();
		for (final String terminalInProduction : terminalsInProduction) {
			if (!myTerminals.contains(terminalInProduction)) {
				addTerminal(terminalInProduction);
			}
		}
	}

	/**
	 * Adds <CODE>productions</CODE> to grammar by calling addProduction for
	 * each production in array.
	 *
	 * @param productions
	 *            the set of productions to add to grammar
	 */
	public void addProductions(final List<Production> productions) {
		for (final Production production : productions) {
			addProduction(production);
		}
	}

	/**
	 * Adds <CODE>terminal</CODE> to the set of terminals in the grammar.
	 *
	 * @param terminal
	 *            the terminal to add.
	 */
	private void addTerminal(final String terminal) {
		myTerminals.add(terminal);
	}

	/**
	 * Adds <CODE>variable</CODE> to the set of variables in the grammar.
	 *
	 * @param variable
	 *            the variable to add.
	 */
	private void addVariable(final String variable) {
		myVariables.add(variable);
	}

	/**
	 * If a production is invalid for the grammar, this method should throw
	 * exceptions indicating why the production is invalid. Otherwise it should
	 * do nothing. This method will be called when a production is added, and
	 * may be called by outsiders wishing to check a production without adding
	 * it to a grammar.
	 *
	 * @param production
	 *            the production
	 * @throws IllegalArgumentException
	 *             if the production is in some way faulty
	 */
	public abstract void checkProduction(Production production);

	/**
	 * Returns a copy of the Grammar object.
	 *
	 * @return a copy of the Grammar object.
	 */
	@Override
	public Object clone() {
		Grammar g;
		try {
			g = getClass().newInstance();
		} catch (final Throwable e) {
			logger.warn("Warning: clone of grammar failed: {}", e.getCause(), e);
			return null;
		}

		final HashMap<String, String> map = new HashMap<>(); // old variables to
																// new
		// variables

		final List<String> variables = getVariables();
		for (int v = 0; v < variables.size(); v++) {
			final String variable = variables.get(v);
			final String nvariable = new String(variables.get(v));
			map.put(variable, nvariable);
			g.addVariable(nvariable);
		}

		/** set start variable. */
		g.setStartVariable(map.get(getStartVariable()));

		final List<String> terminals = getTerminals();
		for (int t = 0; t < terminals.size(); t++) {
			g.addTerminal(new String(terminals.get(t)));
		}

		final List<Production> productions = getProductions();
		for (int p = 0; p < productions.size(); p++) {
			final String rhs = productions.get(p).getRHS();
			final String lhs = productions.get(p).getLHS();
			g.addProduction(new Production(rhs, lhs));
		}

		return g;
	}

	/**
	 * Gets the Environment Frame the automaton is in.
	 *
	 * @return the environment frame.
	 */
	public EnvironmentFrame getEnvironmentFrame() {
		return myEnvFrame;
	}

	public String getFileName() {
		int last = fileName.lastIndexOf("\\");
		if (last == -1) {
			last = fileName.lastIndexOf("/");
		}

		return fileName.substring(last + 1);
	}

	public String getFilePath() {
		int last = fileName.lastIndexOf("\\");
		if (last == -1) {
			last = fileName.lastIndexOf("/");
		}

		return fileName.substring(0, last + 1);
	}

	/**
	 * Returns all productions in the grammar.
	 *
	 * @return all productions in the grammar.
	 */
	public List<Production> getProductions() {
		return myProductions;
	}

	/**
	 * Returns the start variable.
	 *
	 * @return the start variable.
	 */
	public String getStartVariable() {
		return myStartVariable;
	}

	/**
	 * Returns all terminals in the grammar.
	 *
	 * @return all terminals in the grammar.
	 */
	public List<String> getTerminals() {
		return Lists.newArrayList(myTerminals);
	}

	/**
	 * Returns all variables in the grammar.
	 *
	 * @return all variables in the grammar.
	 */
	public List<String> getVariables() {
		return Lists.newArrayList(myVariables);
	}

	public abstract boolean isConverted();

	/**
	 * Returns true if <CODE>production</CODE> is in the set of productions of
	 * the grammar.
	 *
	 * @param production
	 *            the production.
	 * @return true if <CODE>production</CODE> is in the set of productions of
	 *         the grammar.
	 */
	public boolean isProduction(final Production production) {
		return myProductions.contains(production);
	}

	/**
	 * Returns true if <CODE>terminal</CODE> is in the set of terminals in the
	 * grammar.
	 *
	 * @param terminal
	 *            the terminal.
	 * @return true if <CODE>terminal</CODE> is in the set of terminals in the
	 *         grammar.
	 */
	public boolean isTerminal(final String terminal) {
		return myTerminals.contains(terminal);
	}

	/**
	 * Returns true if <CODE>production</CODE> is a valid production for the
	 * grammar. This method by default calls <CODE>checkProduction</CODE> and
	 * returns true if and only if the method did not throw an exception.
	 *
	 * @param production
	 *            the production.
	 * @return <CODE>true</CODE> if the production is fine, <CODE>false</CODE>
	 *         if it is not
	 */
	public boolean isValidProduction(final Production production) {
		try {
			checkProduction(production);
			return true;
		} catch (final IllegalArgumentException e) {
			return false;
		}
	}

	/**
	 * Returns true if <CODE>variable</CODE> is in the set of variables in the
	 * grammar.
	 *
	 * @param variable
	 *            the variable.
	 * @return true if <CODE>variable</CODE> is in the set of variables in the
	 *         grammar.
	 */
	public boolean isVariable(final String variable) {
		return myVariables.contains(variable);
	}

	/**
	 * Removes <CODE>production</CODE> from the set of productions in the
	 * grammar.
	 *
	 * @param production
	 *            the production to remove.
	 */
	public void removeProduction(final Production production) {
		myProductions.remove(production);
		new GrammarChecker();
		/**
		 * Remove any variables that existed only in the production being
		 * removed.
		 */
		final List<String> variablesInProduction = production.getVariables();
		for (final String variableInProduction : variablesInProduction) {
			if (!GrammarChecker.isVariableInProductions(this, variableInProduction)) {
				removeVariable(variableInProduction);
			}
		}

		/**
		 * Remove any terminals that existed only in the production being
		 * removed.
		 */
		final List<String> terminalsInProduction = production.getTerminals();
		for (final String terminalInProduction : terminalsInProduction) {
			if (!GrammarChecker.isTerminalInProductions(this, terminalInProduction)) {
				removeTerminal(terminalInProduction);
			}
		}
	}

	/**
	 * Removes <CODE>terminal</CODE> from the set of terminals in the grammar.
	 *
	 * @param terminal
	 *            the terminal to remove.
	 */
	private void removeTerminal(final String terminal) {
		myTerminals.remove(terminal);
	}

	/**
	 * Removes <CODE>variable</CODE> from the set of variables of the grammar.
	 *
	 * @param variable
	 *            the variable to remove.
	 */
	private void removeVariable(final String variable) {
		myVariables.remove(variable);
	}

	/**
	 * Changes the environment frame this automaton is in.
	 *
	 * @param frame
	 *            the environment frame
	 */
	public void setEnvironmentFrame(final EnvironmentFrame frame) {
		myEnvFrame = frame;
	}

	public void setFilePath(final String name) {
		fileName = name;
	}

	/**
	 * Changes the start variable to <CODE>variable</CODE>.
	 *
	 * @param variable
	 *            the new start variable.
	 */
	public void setStartVariable(final String variable) {
		myStartVariable = variable;
	}

	/**
	 * Returns a string representation of the grammar object, listing the four
	 * parts of the definition of a grammar: the set of variables, the set of
	 * terminals, the start variable, and the set of production rules.
	 *
	 * @return a string representation of the grammar object.
	 */
	@Override
	public String toString() {
		final StringBuffer buffer = new StringBuffer();
		buffer.append(super.toString());
		buffer.append('\n');
		/** print variables. */
		buffer.append("V: ");
		final List<String> variables = getVariables();
		for (int v = 0; v < variables.size(); v++) {
			buffer.append(variables.get(v));
			buffer.append(" ");
		}
		buffer.append('\n');

		/** print terminals. */
		buffer.append("T: ");
		final List<String> terminals = getTerminals();
		for (int t = 0; t < terminals.size(); t++) {
			buffer.append(terminals.get(t));
			buffer.append(" ");
		}
		buffer.append('\n');

		/** print start variable. */
		buffer.append("S: ");
		buffer.append(getStartVariable());
		buffer.append('\n');

		/** print production rules. */
		buffer.append("P: ");
		buffer.append('\n');
		final List<Production> productions = getProductions();
		for (int p = 0; p < productions.size(); p++) {
			buffer.append(productions.get(p).toString());
			buffer.append('\n');
		}

		return buffer.toString();
	}
}
