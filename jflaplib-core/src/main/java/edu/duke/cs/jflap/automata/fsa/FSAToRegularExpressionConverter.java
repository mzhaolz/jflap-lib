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

package edu.duke.cs.jflap.automata.fsa;

import java.util.ArrayList;
import java.util.List;

import edu.duke.cs.jflap.automata.Automaton;
import edu.duke.cs.jflap.automata.State;
import edu.duke.cs.jflap.automata.StatePlacer;
import edu.duke.cs.jflap.automata.Transition;
import edu.duke.cs.jflap.gui.environment.Universe;
import edu.duke.cs.jflap.regular.Discretizer;

/**
 * The fsa to regular expression converter can be used to convert a finite state
 * automaton to its equivalent regular expression. In order to perform this
 * conversion, you need to convert the finite state automaton into a "simple"
 * automaton (i.e. an automaton with a single final state and a different single
 * initial state and exactly one transition between all combinations of states)
 * by calling convertToSimpleAutomaton. Or you can do this conversion step by
 * step, by first calling getSingleFinalState to give your automaton a unique,
 * single final state. Then, you would need to check all combinations of pairs
 * of states in your automaton to see if they have a single transition between
 * them (remember, the simple automaton needs exactly one transition between all
 * pairs of states). If there are 0 transitions between any two states, you can
 * call addTransitionOnEmptySet to create a transition on the empty set between
 * the two states. Or if there are more than one transitions between any two
 * states, you can call combineToSingleTransition to combine all those
 * transitions to a single transition between the two states. Then you can
 * convert this automaton immediately to its equivalent regular expression by
 * calling convertToRegularExpression, or you can perform this conversion step
 * by step. First you need to remove all states other than the final and initial
 * states. You do this by calling getTransitionsForRemoveState and then
 * removeState for each state. Once this is completed, you will have a
 * generalized transition graph (with only two states--an initial and final
 * state). At this point you can get the regular expression by calling
 * getExpressionFromGTG, or you can do that work yourself by calling getII,
 * getIJ, getJJ, and getJI to get the expressions on the four arcs in your
 * two-state generalized transition graph, and then calling getFinalExpression.
 *
 * @author Ryan Cavalcante
 *
 */
public class FSAToRegularExpressionConverter {
	/* the string for the empty set. */
	public static final String EMPTY = "\u00F8";

	/* the string for lambda. */
	public static final String LAMBDA_DISPLAY = Universe.curProfile.getEmptyString();

	public static final String LAMBDA = "";

	/* the string for the kleene star. */
	public static final String KLEENE_STAR = "*";

	/* the string for the or symbol. */
	public static final String OR = "+";

	/** right paren. */
	public static final String RIGHT_PAREN = ")";

	/** left paren. */
	public static final String LEFT_PAREN = "(";

	/**
	 * Returns a string of <CODE>word</CODE> surrounded by parentheses. i.e.
	 * (<word>), unless it is unnecessary.
	 *
	 * @param word
	 *            the word.
	 * @return a string of <CODE>word</CODE> surrounded by parentheses.
	 */
	public static String addParen(final String word) {
		return LEFT_PAREN + word + RIGHT_PAREN;
	}

	/**
	 * Adds a new transition to <CODE>automaton</CODE> between
	 * <CODE>fromState</CODE> and </CODE>toState</CODE> on the symbol for the
	 * empty set.
	 *
	 * @param fromState
	 *            the from state for the transition
	 * @param toState
	 *            the to state for the transition
	 * @param automaton
	 *            the automaton.
	 * @return the <CODE>FSATransition</CODE> that was created
	 */
	public static FSATransition addTransitionOnEmptySet(final State fromState, final State toState,
			final Automaton automaton) {
		final FSATransition t = new FSATransition(fromState, toState, EMPTY);
		automaton.addTransition(t);
		return t;
	}

	/**
	 * Returns true if there are more removable states in
	 * <CODE>automaton</CODE>.
	 *
	 * @param automaton
	 *            the automaton
	 * @return true if there are more removable states in
	 *         <CODE>automaton</CODE>.
	 */
	public static boolean areRemovableStates(final Automaton automaton) {
		final List<State> states = automaton.getStates();
		for (final State state : states) {
			if (isRemovable(state, automaton)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Removes all transitions in <CODE>transitions</CODE> from
	 * <CODE>automaton</CODE>, replacing them with a single transition in
	 * <CODE>automaton</CODE> between <CODE>fromState</CODE> and
	 * <CODE>toState</CODE> labeled with a regular expression that represents
	 * the labels of all the removed transitions Or'ed together (e.g. a + (b*c)
	 * + (d+e)).
	 *
	 * @param fromState
	 *            the from state for <CODE>transitions</CODE> and for the newly
	 *            created transition.
	 * @param toState
	 *            the to state for <CODE>transitions</CODE> and for the newly
	 *            created transition.
	 * @param transitions
	 *            the transitions being removed and combined into a single
	 *            transition
	 * @param automaton
	 *            the automaton
	 * @return the transition that replaced all of these
	 */
	public static FSATransition combineToSingleTransition(final State fromState, final State toState,
			final List<Transition> transitions, final Automaton automaton) {
		String label = ((FSATransition) transitions.get(0)).getDescription();
		automaton.removeTransition(transitions.get(0));
		for (int i = 1; i < transitions.size(); i++) {
			label = or(label, ((FSATransition) transitions.get(i)).getDescription());
			automaton.removeTransition(transitions.get(i));
		}
		final FSATransition t = new FSATransition(fromState, toState, label);
		automaton.addTransition(t);
		return t;
	}

	/**
	 * Returns the expression that represents <CODE>r1</CODE> concatenated with
	 * <CODE>r2</CODE>. (essentialy just the two strings concatenated).
	 *
	 * @param r1
	 *            the first part of the expression.
	 * @param r2
	 *            the second part of the expression.
	 * @return the expression that represents <CODE>r1</CODE> concatenated with
	 *         <CODE>r2</CODE>. (essentialy just the two strings concatenated).
	 */
	public static String concatenate(String r1, String r2) {
		if (r1.equals(EMPTY) || r2.equals(EMPTY)) {
			return EMPTY;
		} else if (r1.equals(LAMBDA)) {
			return r2;
		} else if (r2.equals(LAMBDA)) {
			return r1;
		}
		if (Discretizer.or(r1).size() > 1) {
			r1 = addParen(r1);
		}
		if (Discretizer.or(r2).size() > 1) {
			r2 = addParen(r2);
		}
		return r1 + r2;
	}

	/**
	 * Converts <CODE>automaton</CODE> into a generalized transition graph with
	 * only two states, a unique initial state, and a unique final state.
	 *
	 * @param automaton
	 *            the automaton.
	 */
	public static void convertToGTG(final Automaton automaton) {
		final List<State> finalStates = automaton.getFinalStates();
		final State finalState = finalStates.get(0);
		final State initialState = automaton.getInitialState();
		final List<State> states = automaton.getStates();
		for (int k = 0; k < states.size(); k++) {
			final State state = states.get(k);
			if (state != finalState && state != initialState) {
				final List<Transition> transitions = getTransitionsForRemoveState(state, automaton);
				removeState(state, transitions, automaton);
			}
		}
	}

	/**
	 * Returns the regular expression that represents <CODE>automaton</CODE>.
	 *
	 * @param automaton
	 *            the automaton
	 * @return the regular expression that represents <CODE>automaton</CODE>.
	 */
	public static String convertToRegularExpression(final Automaton automaton) {
		if (!isConvertable(automaton)) {
			return null;
		}
		convertToGTG(automaton);
		return getExpressionFromGTG(automaton);
	}

	/**
	 * Converts <CODE>automaton</CODE> to an equivalent automaton with a single
	 * transition between all combinations of states. (if there are currently
	 * more than one transition between two states, it combines them into a
	 * single transition by or'ing the labels of all the transitions. If there
	 * is no transition between two states, it creates a transition and labels
	 * it with the empty set character (EMPTY).
	 *
	 * @param automaton
	 *            the automaton.
	 */
	public static void convertToSimpleAutomaton(final Automaton automaton) {
		if (!isConvertable(automaton)) {
			getSingleFinalState(automaton);
		}
		final List<State> states = automaton.getStates();
		for (int k = 0; k < states.size(); k++) {
			for (int j = 0; j < states.size(); j++) {
				final List<Transition> transitions = automaton.getTransitionsFromStateToState(states.get(k),
						states.get(j));
				if (transitions.size() == 0) {
					addTransitionOnEmptySet(states.get(k), states.get(j), automaton);
				} else if (transitions.size() > 1) {
					combineToSingleTransition(states.get(k), states.get(j), transitions, automaton);
				}
			}
		}
	}

	/**
	 * Returns a non-unicoded version of <CODE>word</CODE> for debug purposes.
	 *
	 * @param word
	 *            the expression to output
	 * @return a non-unicoded version of <CODE>word</CODE> for debug purposes.
	 */
	public static String getExp(final String word) {
		if (word.equals(LAMBDA)) {
			return "lambda";
		} else if (word.equals(EMPTY)) {
			return "empty";
		}
		return word;
	}

	/**
	 * Returns the expression obtained from evaluating the following equation:
	 * r(pq) = r(pq) + r(pk)r(kk)*r(kq), where p, q, and k represent the IDs of
	 * states in <CODE>automaton</CODE>.
	 *
	 * @param p
	 *            the from state
	 * @param q
	 *            the to state
	 * @param k
	 *            the state being removed.
	 * @param automaton
	 *            the automaton.
	 * @return the expression obtained from evaluating the following equation:
	 *         r(pq) = r(pq) + r(pk)r(kk)*r(kq), where p, q, and k represent the
	 *         IDs of states in <CODE>automaton</CODE>.
	 */
	public static String getExpression(final int p, final int q, final int k, final Automaton automaton) {
		final State fromState = automaton.getStateWithID(p);
		final State toState = automaton.getStateWithID(q);
		final State removeState = automaton.getStateWithID(k);

		final String pq = getExpressionBetweenStates(fromState, toState, automaton);
		final String pk = getExpressionBetweenStates(fromState, removeState, automaton);
		final String kk = getExpressionBetweenStates(removeState, removeState, automaton);
		final String kq = getExpressionBetweenStates(removeState, toState, automaton);

		final String temp1 = star(kk);
		final String temp2 = concatenate(pk, temp1);
		final String temp3 = concatenate(temp2, kq);
		final String label = or(pq, temp3);
		return label;
	}

	/**
	 * Returns the expression on the transition between <CODE>fromState</CODE>
	 * and <CODE>toState</CODE> in <CODE>automaton</CODE>.
	 *
	 * @param fromState
	 *            the from state
	 * @param toState
	 *            the to state
	 * @param automaton
	 *            the automaton
	 * @return the expression on the transition between <CODE>fromState</CODE>
	 *         and <CODE>toState</CODE> in <CODE>automaton</CODE>.
	 */
	public static String getExpressionBetweenStates(final State fromState, final State toState,
			final Automaton automaton) {
		final List<Transition> transitions = automaton.getTransitionsFromStateToState(fromState, toState);
		final FSATransition trans = (FSATransition) transitions.get(0);
		return trans.getLabel();
	}

	/**
	 * Returns the expression for the generalized transition graph
	 * <CODE>automaton</CODE> with two states, a unique initial and unique final
	 * state. Evaluates to the expression r =
	 * (r(ii)*r(ij)r(jj)*r(ji))*r(ii)*r(ij)r(jj)*. where r(ij) represents the
	 * expression on the transition between state i (the initial state) and
	 * state j (the final state)
	 *
	 * @param automaton
	 *            the generalized transition graph with two states (a unique
	 *            initial and final state).
	 * @return the expression for the generalized transition graph
	 *         <CODE>automaton</CODE> with two states, a unique initial and
	 *         unique final state
	 */
	public static String getExpressionFromGTG(final Automaton automaton) {
		final String ii = getII(automaton);
		final String ij = getIJ(automaton);
		final String jj = getJJ(automaton);
		final String ji = getJI(automaton);

		return getFinalExpression(ii, ij, jj, ji);
	}

	/**
	 * Returns the expression for the values of ii, ij, jj, and ji determined
	 * from the GTG with a unique initial and final state.
	 *
	 * @param ii
	 *            the expression on the loop off the initial state
	 * @param ij
	 *            the expression on the arc from the initial state to the final
	 *            state.
	 * @param jj
	 *            the expression on the loop off the final state.
	 * @param ji
	 *            the expression on the arc from the final state to the initial
	 *            state.
	 * @return the expression for the values of ii, ij, jj, and ji determined
	 *         from the GTG with a unique initial and final state.
	 */
	public static String getFinalExpression(final String ii, final String ij, final String jj, final String ji) {
		final String temp = concatenate(star(ii), concatenate(ij, concatenate(star(jj), ji)));
		final String temp2 = concatenate(star(ii), concatenate(ij, star(jj)));
		// String expression =
		// concatenate(star(concatenate(LEFT_PAREN,concatenate(temp,RIGHT_PAREN))),
		// temp2);
		final String expression = concatenate(star(temp), temp2);
		return expression;
	}

	/**
	 * Returns the expression on the loop off the initial state of
	 * <CODE>automaton</CODE>.
	 *
	 * @param automaton
	 *            a generalized transition graph with only two states, a unique
	 *            initial and final state.
	 * @return the expression on the loop off the initial state of
	 *         <CODE>automaton</CODE>.
	 */
	public static String getII(final Automaton automaton) {
		final State initialState = automaton.getInitialState();
		return getExpressionBetweenStates(initialState, initialState, automaton);
	}

	/**
	 * Returns the expression on the arc from the initial state to the final
	 * state of <CODE>automaton</CODE>.
	 *
	 * @param automaton
	 *            a generalized transition graph with only two states, a unique
	 *            initial and final state.
	 * @return the expression on the arc from the initial state to the final
	 *         state of <CODE>automaton</CODE>.
	 */
	public static String getIJ(final Automaton automaton) {
		final State initialState = automaton.getInitialState();
		final List<State> finalStates = automaton.getFinalStates();
		final State finalState = finalStates.get(0);
		return getExpressionBetweenStates(initialState, finalState, automaton);
	}

	/**
	 * Returns the expression on the arc from the final state to the initial
	 * state of <CODE>automaton</CODE>
	 *
	 * @param automaton
	 *            a generalized transition graph with only two states, a unique
	 *            initial and final state.
	 * @return the expression on the arc from the final state to the initial
	 *         state of <CODE>automaton</CODE>
	 */
	public static String getJI(final Automaton automaton) {
		final State initialState = automaton.getInitialState();
		final List<State> finalStates = automaton.getFinalStates();
		final State finalState = finalStates.get(0);
		return getExpressionBetweenStates(finalState, initialState, automaton);
	}

	/**
	 * Returns the expression on the loop off the final state of
	 * <CODE>automaton</CODE>
	 *
	 * @param automaton
	 *            a generalized transition graph with only two states, a unique
	 *            initial and final state.
	 * @return the expression on the loop off the final state of
	 *         <CODE>automaton</CODE>
	 */
	public static String getJJ(final Automaton automaton) {
		final List<State> finalStates = automaton.getFinalStates();
		final State finalState = finalStates.get(0);
		return getExpressionBetweenStates(finalState, finalState, automaton);
	}

	/**
	 * Makes all final states in <CODE>automaton</CODE> non-final, adding
	 * transitions from these states to a newly created final state on lambda.
	 *
	 * @param automaton
	 *            the automaton
	 */
	public static void getSingleFinalState(final Automaton automaton) {
		final StatePlacer sp = new StatePlacer();
		final State finalState = automaton.createState(sp.getPointForState(automaton));

		final List<State> finalStates = automaton.getFinalStates();
		for (int k = 0; k < finalStates.size(); k++) {
			final State state = finalStates.get(k);
			automaton.addTransition(new FSATransition(state, finalState, LAMBDA));
			automaton.removeFinalState(state);
		}
		automaton.addFinalState(finalState);
	}

	/**
	 * Returns a Transition object that represents the transition between the
	 * states with ID's <CODE>p</CODE> and <CODE>q</CODE>, with
	 * <CODE>expression</CODE> as the transition label.
	 *
	 * @param p
	 *            the ID of the from state.
	 * @param q
	 *            the ID of the to state.
	 * @param expression
	 *            the expression
	 * @param automaton
	 *            the automaton
	 * @return a Transition object that represents the transition between the
	 *         states with ID's <CODE>p</CODE> and <CODE>q</CODE>, with
	 *         <CODE>expression</CODE> as the transition label.
	 */
	public static Transition getTransitionForExpression(final int p, final int q, final String expression,
			final Automaton automaton) {
		final State fromState = automaton.getStateWithID(p);
		final State toState = automaton.getStateWithID(q);
		final Transition transition = new FSATransition(fromState, toState, expression);
		return transition;
	}

	/**
	 * Returns a list of all transitions for <CODE>automaton</CODE> created by
	 * removing <CODE>state</CODE>.
	 *
	 * @param state
	 *            the state to remove.
	 * @param automaton
	 *            the automaton.
	 * @return a list of all transitions for <CODE>automaton</CODE> created by
	 *         removing <CODE>state</CODE>.
	 */
	public static List<Transition> getTransitionsForRemoveState(final State state, final Automaton automaton) {
		if (!isRemovable(state, automaton)) {
			return null;
		}
		final List<Transition> list = new ArrayList<>();
		final int k = state.getID();
		final List<State> states = automaton.getStates();
		for (final State stati : states) {
			final int p = stati.getID();
			if (p != k) {
				for (final State statj : states) {
					final int q = statj.getID();
					if (q != k) {
						final String exp = getExpression(p, q, k, automaton);
						list.add(getTransitionForExpression(p, q, exp, automaton));
					}
				}
			}
		}
		return list;
	}

	/**
	 * Returns true if <CODE>automaton</CODE> can be converted to a regular
	 * expression (i.e. it has a unique initial and final state and it is a
	 * finite state automaton, and the initial state is not the final state).
	 *
	 * @param automaton
	 *            the automaton to convert
	 * @return true if <CODE>automaton</CODE> can be converted to a regular
	 *         expression.
	 */
	public static boolean isConvertable(final Automaton automaton) {
		if (!(automaton instanceof FiniteStateAutomaton)) {
			return false;
		}
		final List<State> finalStates = automaton.getFinalStates();
		if (finalStates.size() != 1) {
			return false;
		}

		final State initialState = automaton.getInitialState();
		if (finalStates.get(0) == initialState) {
			return false;
		}
		return true;
	}

	/**
	 * Returns true if <CODE>state</CODE> is a removable state (i.e. it is not
	 * the unique initial or final state).
	 *
	 * @param state
	 *            the state to remove.
	 * @param automaton
	 *            the automaton.
	 * @return true if <CODE>state</CODE> is a removable state
	 */
	public static boolean isRemovable(final State state, final Automaton automaton) {
		final List<State> finalStates = automaton.getFinalStates();
		final State finalState = finalStates.get(0);
		final State initialState = automaton.getInitialState();
		if (state == finalState || state == initialState) {
			return false;
		}
		return true;
	}

	/**
	 * Returns true if <CODE>word</CODE> is one character long and it is a
	 * letter.
	 *
	 * @param word
	 *            the word
	 * @return true if <CODE>word</CODE> is one character long and it is a
	 *         letter.
	 */
	public static boolean isSingleCharacter(final String word) {
		if (word.length() != 1) {
			return false;
		}
		final char ch = word.charAt(0);
		return Character.isLetter(ch);
	}

	/**
	 * Returns true if <CODE>word</CODE> needs parens. (i.e. it is an '+' (OR)
	 * expression)
	 *
	 * @param word
	 *            the word.
	 * @return true if <CODE>word</CODE> needs parens. (i.e. it is an '+' (OR)
	 *         expression)
	 */
	public static boolean needsParens(final String word) {
		for (int k = 0; k < word.length(); k++) {
			final char ch = word.charAt(k);
			if (ch == '+') {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns the string that represents <CODE>r1</CODE> or'ed with
	 * <CODE>r2</CODE>.
	 *
	 * @param r1
	 *            the first expression
	 * @param r2
	 *            the second expression
	 * @return the string that represents <CODE>r1</CODE> or'ed with
	 *         <CODE>r2</CODE>.
	 */
	public static String or(String r1, String r2) {
		if (r1.equals(EMPTY)) {
			return r2;
		}
		if (r2.equals(EMPTY)) {
			return r1;
		}
		if (r1.equals(LAMBDA) && r2.equals(LAMBDA)) {
			return LAMBDA;
		}
		if (r1.equals(LAMBDA)) {
			r1 = LAMBDA_DISPLAY;
		}
		if (r2.equals(LAMBDA)) {
			r2 = LAMBDA_DISPLAY;
		}
		// if(needsParens(r1)) r1 = addParen(r1);
		// if(needsParens(r2)) r2 = addParen(r2);
		return r1 + OR + r2;
	}

	/**
	 * Completely reconstructs <CODE>automaton</CODE>, removing all transitions
	 * and <CODE>state</CODE> and adding all transitions in
	 * <CODE>transitions</CODE>.
	 *
	 * @param state
	 *            the state to remove.
	 * @param transitions
	 *            the transitions returned for removing <CODE>state</CODE>.
	 * @param automaton
	 *            the automaton.
	 */
	public static void removeState(final State state, final List<Transition> transitions, final Automaton automaton) {
		final List<Transition> oldTransitions = automaton.getTransitions();
		for (final Transition trans : oldTransitions) {
			automaton.removeTransition(trans);
		}

		automaton.removeState(state);

		for (int i = 0; i < transitions.size(); i++) {
			automaton.addTransition(transitions.get(i));
		}
	}

	/**
	 * Returns the expression that represents <CODE>r1</CODE> kleene-starred.
	 *
	 * @param r1
	 *            the expression being kleene-starred.
	 * @return the expression that represents <CODE>r1</CODE> kleene-starred.
	 */
	public static String star(String r1) {
		if (r1.equals(EMPTY) || r1.equals(LAMBDA)) {
			return LAMBDA;
		}
		if (Discretizer.or(r1).size() > 1 || Discretizer.cat(r1).size() > 1) {
			r1 = addParen(r1);
		} else {
			if (r1.endsWith(KLEENE_STAR)) {
				return r1;
			}
		}
		return r1 + KLEENE_STAR;
	}

	/**
	 * Creates an instance of <CODE>FSAToRegularExpressionConverter</CODE>.
	 */
	private FSAToRegularExpressionConverter() {
	}
}
