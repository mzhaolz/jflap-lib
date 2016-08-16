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

package edu.duke.cs.jflap.automata.turing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import edu.duke.cs.jflap.automata.State;
import edu.duke.cs.jflap.automata.Transition;
import edu.duke.cs.jflap.grammar.Production;

/**
 * Converter for turing to unrestricted grammar NEEDS to make abstraction (super
 * class for both TuringToGrammar and PDAtoCFGconverter)
 *
 * @author Kyung Min (Jason) Lee
 *
 */
public class TuringToGrammarConverter {

	// Some String constants.
	private static final String SQUARE_SYMBOL = "" + '\u25A1';
	private static final String SQUARE = "=";
	private static final String VAR_START = "V(";
	private static final String VAR_END = ")";

	// Z
	private final HashSet<String> myAllReadableString;

	// |- finite set of symbols in the tape alphabet
	private final HashSet<String> myAllWritableString;

	/**
	 * Constructor for converting TM to Unrestrcited grammar
	 */
	public TuringToGrammarConverter() {
		myAllReadableString = new HashSet<>();
		myAllWritableString = new HashSet<>();
	}

	public List<Production> createProductionsForInit(final State state, final List<Transition> tm) {
		// TODO Auto-generated method stub
		final int id = state.getID();
		final ArrayList<Production> init = new ArrayList<>();

		// for now
		init.add(new Production("S", VAR_START + SQUARE + SQUARE + VAR_END + "S"));
		init.add(new Production("S", "S" + VAR_START + SQUARE + SQUARE + VAR_END));
		init.add(new Production("S", "T"));
		myAllReadableString.add(SQUARE);

		for (int i = 0; i < tm.size(); i++) {
			final TMTransition trans = (TMTransition) tm.get(i);
			final int tape = trans.getTapeLength();
			for (int j = 0; j < tape; j++) {
				String str = trans.getRead(j);
				if (str.equals(SQUARE_SYMBOL)) {
					str = SQUARE;
				}
				String write = trans.getWrite(j);
				if (write.equals(SQUARE_SYMBOL)) {
					write = SQUARE;
				}
				myAllWritableString.add(write);
				if (!myAllReadableString.contains(str)) {
					myAllReadableString.add(str);
					final String var1 = VAR_START + str + str + VAR_END;
					final String var2 = VAR_START + str + id + str + VAR_END;
					init.add(new Production("T", "T" + var1));
					init.add(new Production("T", var2));
				}
			}
		}
		init.add(new Production(SQUARE, null));

		return init;
	}

	/**
	 * For each transition apply Peter Linz's algorithm to generate new
	 * productions
	 *
	 * @param transition
	 * @param list2
	 * @return
	 */
	public List<Production> createProductionsForTransition(final Transition transition, final List<State> list2) {
		// TODO Auto-generated method stub
		final ArrayList<Production> list = new ArrayList<>();
		final TMTransition trans = (TMTransition) transition;
		final HashMap<Integer, Boolean> finalStateMap = new HashMap<>();
		for (int i = 0; i < list2.size(); i++) {
			finalStateMap.put(list2.get(i).getID(), true);
		}
		// what is exactly tape??
		final int fromState = trans.getFromState().getID();
		final int toState = trans.getToState().getID();
		final int tape = trans.getTapeLength();
		for (int i = 0; i < tape; i++) {
			final String direction = trans.getDirection(i);
			String read = trans.getRead(i);
			String write = trans.getWrite(i);
			if (read.equals(SQUARE_SYMBOL)) {
				read = SQUARE;
			}
			if (write.equals(SQUARE_SYMBOL)) {
				write = SQUARE;
			}

			for (final String p : myAllReadableString) {
				for (final String a : myAllReadableString) {

					for (final String q : myAllWritableString) {
						if (direction.equals("R")) {
							final String lhs_var1 = VAR_START + a + fromState + read + VAR_END;
							final String lhs_var2 = VAR_START + p + q + VAR_END;
							final String rhs_var1 = VAR_START + a + write + VAR_END;
							final String rhs_var2 = VAR_START + p + toState + q + VAR_END;

							final Production prod = new Production(lhs_var1 + lhs_var2, rhs_var1 + rhs_var2);
							list.add(prod);

							if (finalStateMap.containsKey(toState)) {
								final String lhs = VAR_START + p + toState + q + VAR_END;
								final String rhs = p;
								list.add(new Production(lhs, rhs));
								final String lhs2 = VAR_START + a + q + VAR_END + p;
								list.add(new Production(lhs2, a + rhs));

								final String lhs3 = p + VAR_START + a + q + VAR_END;
								list.add(new Production(lhs3, p + a));
							}
						}
						if (direction.equals("L")) {
							final String lhs_var1 = VAR_START + p + q + VAR_END;
							final String lhs_var2 = VAR_START + a + fromState + read + VAR_END;

							final String rhs_var1 = VAR_START + p + toState + q + VAR_END;
							final String rhs_var2 = VAR_START + a + write + VAR_END;

							final Production prod = new Production(lhs_var1 + lhs_var2, rhs_var1 + rhs_var2);

							list.add(prod);

							if (finalStateMap.containsKey(toState)) {
								final String lhs = VAR_START + p + toState + q + VAR_END;
								final String rhs = p;
								final String lhs2 = p + VAR_START + a + q + VAR_END;
								list.add(new Production(lhs, rhs));
								list.add(new Production(lhs2, p + a));

								// TODO: Change this later

								final String lhs3 = VAR_START + a + q + VAR_END + p;
								list.add(new Production(lhs3, a + rhs));
							}
						}
						if (direction.equals("S")) {
							// what to do? : DO nothing standard TM only has
							// left and right
						}
					}
				}
			}
		}

		return list;
	}
}
