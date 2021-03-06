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

import static com.google.common.base.Preconditions.checkArgument;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

import edu.duke.cs.jflap.automata.Automaton;
import edu.duke.cs.jflap.automata.AutomatonSimulator;
import edu.duke.cs.jflap.automata.Configuration;
import edu.duke.cs.jflap.automata.Transition;
import edu.duke.cs.jflap.debug.EDebug;
import edu.duke.cs.jflap.gui.environment.Universe;

/**
 * The TM simulator progresses TM configurations on a possibly multitape Turing
 * machine.
 *
 * @author Thomas Finley
 *
 */
public class TMSimulator extends AutomatonSimulator {
	private List<String> inputStrings;

	private final Map<String, String> varToChar = new HashMap<>();

	private final List<AcceptanceFilter> myFilters;

	/**
	 * Creates a TM simulator for the given automaton.
	 *
	 * @param automaton
	 *            the machine to simulate
	 * @throws IllegalArgumentException
	 *             if this automaton is not a Turing machine
	 */
	public TMSimulator(final Automaton automaton) {
		super(automaton);
		if (!(automaton instanceof TuringMachine)) {
			throw new IllegalArgumentException("Automaton is not a Turing machine, but a " + automaton.getClass());
		}

		// //MERLIN MERLIN MERLIN MERLIN MERLIN// //this code is only for show,
		// it should be moved into a setting with a better UI before release//
		// AcceptanceFilter[] choices = new AcceptanceFilter[] {new
		// AcceptByHaltingFilter(), new AcceptByFinalStateFilter()};
		//
		// List<AcceptanceFilter> tlist = new ArrayList<AcceptanceFilter>();
		// for (int i = 0; i < choices.length; i++){
		// int res = JOptionPane.showConfirmDialog(null, "Would you like to " +
		// choices[i].getName()+ "?", "Confirm use of this acceptance criteria",
		// JOptionPane.YES_NO_OPTION);
		// if (res == JOptionPane.YES_OPTION) tlist.add(choices[i]);
		// }
		//
		// if (tlist.size() == 0)
		// JOptionPane.showMessageDialog(null, "Will reject all inputs
		// eventually, if you choose to persist with this.", "Warning",
		// JOptionPane.WARNING_MESSAGE);
		//
		// //END MERLIN MERLIN MERLIN MERLIN MERLIN// //this code is only for
		// show, it should be moved into a setting with a better UI before
		// release//

		final List<AcceptanceFilter> tlist = new ArrayList<>();

		if (Universe.curProfile.getAcceptByFinalState()) {
			tlist.add(new AcceptByFinalStateFilter());
		}
		if (Universe.curProfile.getAcceptByHalting()) {
			tlist.add(new AcceptByHaltingFilter());
		}

		myFilters = tlist;
	}

	/**
	 * Returns a TMConfiguration object that represents the initial
	 * configuration of the TM, before any input has been processed. This
	 * returns an array of length one.
	 *
	 * @param inputs
	 *            the input strings
	 */
	public List<Configuration> getInitialConfigurations(final List<String> inputs) {
		inputStrings = new ArrayList<>(inputs);
		final List<Tape> tapes = new ArrayList<>();
		for (int i = 0; i < tapes.size(); i++) {
			tapes.add(new Tape(inputs.get(i)));
		}
		final List<Configuration> configs = new ArrayList<>();
		final TMState initialState = (TMState) myAutomaton.getInitialState();
		configs.add(new TMConfiguration(initialState, null, tapes, myFilters));

		return configs;
	}

	/**
	 * Returns a TMConfiguration object that represents the initial
	 * configuration of the TM, before any input has been processed. This
	 * returns an array of length one. This method exists only to provide
	 * compatibility with the general definition of
	 * <CODE>AutomatonSimulator</CODE>. One should use the version of this
	 * function that accepts an array of inputs instead.
	 *
	 * @param input
	 *            the input string
	 */
	@Override
	public List<Configuration> getInitialConfigurations(final String input) {
		final int tapes = ((TuringMachine) myAutomaton).tapes();
		final List<String> inputs = new ArrayList<>();
		for (int i = 0; i < tapes; i++) {
			inputs.add(input);
		}
		return getInitialConfigurations(inputs);
	}

	public List<String> getInputStrings() {
		return inputStrings;
	}

	/**
	 * Returns true if the simulation of the input string on the automaton left
	 * the machine in a final state. This method does not appear to be used. It
	 * is only left here because the class from which it inherited requires it.
	 *
	 * @return true if the simulation of the input string on the automaton left
	 *         the machine in a final state
	 */
	@Override
	public boolean isAccepted() {
		return false;
	}

	/**
	 * A vision and a dream, that another take this responsibility.
	 *
	 * This method figures out whether a particular transition is matched by a
	 * particular array of tapes.
	 *
	 */
	private boolean matches(final List<Tape> list, final TMTransition tmt) {
		checkArgument(list.size() == tmt.tapes()); // make sure they address
		// the same number of tapes

		// for MULTITAPE turing Machine
		if (list.size() > 1) {
			for (int i = 0; i < list.size(); i++) {
				final char underHead = list.get(i).readChar();
				final char toMatch = tmt.getRead(i).charAt(0);

				// if (toMatch == '!'){
				// toMatch = tmt.getRead(i).charAt(1);
				// if (underHead == toMatch)
				// return false;
				// }
				if (underHead != toMatch && toMatch != '~') {
					return false;
					// would like to get priorities right for each tape, because
					// the
					// ! parameter shoudl not introduce arbitrary nondeterminism
					// unless we want it to
				}
			}
			return true;
		}

		checkArgument(list.size() == 1);

		// fancy features only work on single-tape machines
		final char underHead = list.get(0).readChar();
		final String strtoMatch = tmt.getRead(0);

		final int assignIndex = strtoMatch.indexOf('}');
		final int bangIndex = strtoMatch.indexOf('!');

		// MERLIN MERLIN MERLIN MERLIN MERLIN//
		// this should have been taken care of during the editing phase -
		// probably put in a re-check on load from save, for legacy issues
		checkArgument(assignIndex == -1 || bangIndex == -1); // both cannot
																// coexist

		if (assignIndex == -1 && bangIndex == -1) { // ordinary case
			return underHead == strtoMatch.charAt(0) || strtoMatch.charAt(0) == '~';

			// watch out for recognizing a variable
		} else if (assignIndex != -1) {
			final String[] characters = strtoMatch.substring(0, assignIndex).split(",");
			boolean flag = false; // this would not be needed, but we want to go
			// through and do error checking on the
			// transition as well
			for (final String character : characters) {
				checkArgument(character.length() == 1);
				if (varToChar.containsKey(character)) {
					// warn the user that they are attempting something
					// erroneous
					// MERLIN MERLIN MERLIN MERLIN MERLIN//
					JOptionPane.showMessageDialog(null,
							"You cannot use a variable on the left side of the assignment operator!\n Please fix this and restart the simulation.",
							"Illegal Variable Location!\n", JOptionPane.ERROR_MESSAGE);
				}
				if (character.charAt(0) == underHead) {
					flag = true;
				}
				; // take care of assignment somewhere else //here, it's only
					// alphabet letters
			}
			if (flag) {
				return flag;
			}
		} else {
			assert bangIndex == 0;
			return underHead != strtoMatch.charAt(1);
		}

		assert false; // should never get down here
		return true;
	}

	/**
	 * Runs the automaton on the input string.
	 *
	 * @param input
	 *            the input string to be run on the automaton
	 * @return true if the automaton accepts the input
	 */
	@Override
	public boolean simulateInput(final String input) {
		/** clear the configurations to begin new simulation. */
		// System.out.println("In Simulate Input");
		myConfigurations.clear();
		final List<Configuration> initialConfigs = getInitialConfigurations(input);
		for (int k = 0; k < initialConfigs.size(); k++) {
			final TMConfiguration initialConfiguration = (TMConfiguration) initialConfigs.get(k);
			myConfigurations.add(initialConfiguration);
		}
		while (!myConfigurations.isEmpty()) {
			// System.out.println("HERE!!!!!");
			if (isAccepted()) {
				return true;
			}
			final List<Configuration> configurationsToAdd = new ArrayList<>();
			final Iterator<Configuration> it = myConfigurations.iterator();
			while (it.hasNext()) {
				final TMConfiguration configuration = (TMConfiguration) it.next();
				final List<Configuration> configsToAdd = stepConfiguration(configuration);
				configurationsToAdd.addAll(configsToAdd);
				it.remove();
			}
			myConfigurations.addAll(configurationsToAdd);
		}
		return false;
	}

	/**
	 * Simulates stepping by building blocks (top-level building blocks). Again,
	 * note that this is deterministic.
	 *
	 * @param config
	 *            the configuration to simulate the one step on
	 *
	 * @return List containing the single configuration, or null if there are no
	 *         valid transitions.
	 */
	public List<Configuration> stepBlock(TMConfiguration config) {
		EDebug.print("Inside StepBlock");
		while (((TuringMachine) (config = (TMConfiguration) stepConfiguration(config).get(0)).getCurrentState()
				.getAutomaton()).getParent() != null) {
			;
		}
		return Arrays.asList(config);
	}

	/**
	 * Simulates one step for a particular configuration, adding the next
	 * reachable configuration. In other words, this code is for a DETERMINISTIC
	 * Turing machine. The ArrayList returned will have list 1.
	 *
	 * @param config
	 *            the configuration to simulate the one step on
	 *
	 * @return ArrayList containing the single configuration, or null if there
	 *         are no valid transitions.
	 */
	@Override
	public List<Configuration> stepConfiguration(final Configuration config) { // one
		// step,
		// and
		// will
		// dig
		// into
		// building
		// blocks
		// if
		// necessary

		// MERLIN MERLIN MERLIN MERLIN MERLIN//

		final List<Configuration> list = new ArrayList<>();
		final TMConfiguration configuration = (TMConfiguration) config;

		TMState currentState = (TMState) configuration.getCurrentState(); // innerTM
		// should
		// never
		// be
		// null;
		// because
		// of
		// the
		// way
		// we
		// set
		// it
		// up
		// in
		// the
		// constructor
		// and
		// in
		// the
		// restoration
		// phase.
		TuringMachine tmp = null; // just a literally tmp, like /tmp

		int times = 0;
		while ((tmp = currentState.getInnerTM()).getStates().size() != 0) {
			EDebug.print(times++);
			currentState = (TMState) tmp.getInitialState();

			// check that the initial state exists
			if (currentState == null) {
				JOptionPane.showMessageDialog(null,
						"It appears that one of your building blocks, possibly nested, lacks an initial state.\n "
								+ "Please resolve this problem and restart the simulation.",
						"Missing Initial State", JOptionPane.ERROR_MESSAGE);

				return list;
			}
		}

		assert (tmp == currentState.getInnerTM());
		assert (tmp.getParent() == currentState);

		List<Transition> trans = currentState.getAutomaton().getTransitionsFromState(currentState);
		TMTransition tmt = null;
		boolean success = false;
		outer: while (true) {

			// sort the ones with the ! symbol to be the later ones. If there
			// are multiple !, then the choice is arbitrary.
			trans.sort((a, b) -> { // variables
				// are only
				// allowed with
				// SINGLE TAPE,
				// and same
				// with NOT

				final TMTransition tma = (TMTransition) a;
				final TMTransition tmb = (TMTransition) b;

				final char fa = tma.getRead(0).charAt(0);
				final char fb = tmb.getRead(0).charAt(0);
				return (fa == '!') ? (fb == '!' ? 0 : 1) : (fb == '!' ? 1 : 0);
			});

			// go through transitions at current level
			for (int i = 0; i < trans.size(); i++) {
				tmt = (TMTransition) trans.get(i);

				if (matches(configuration.getTapes(), tmt)) {
					success = true;
					break outer;
				}
				// tilda means to read nothing or write nothing, and it seems to
				// be explicitly written for turing machines, rather differently
				// from other automata
			}

			// rise a level above.
			if (tmp.getParent() != null) // if this fails, that means that you
			// forgot to set the parent in the XML
			// encoder
			{
				currentState = tmp.getParent();
				tmp = (TuringMachine) currentState.getAutomaton();
				trans = tmp.getTransitionsFromState(currentState); // this line
				// does not
				// matter
				// right now
			} else {
				break; // halting condition
			}
		}

		if (success) { // if variables are used then they will be common to all
			// tapes...
			if (configuration.getTapes().size() > 1) {
				for (int k = 0; k < configuration.getTapes().size(); k++) {
					configuration.getTapes().get(k).writeChar(tmt.getWrite(k).charAt(0) == '~'
							? configuration.getTapes().get(k).readChar() : tmt.getWrite(k).charAt(0));
					configuration.getTapes().get(k).moveHead(tmt.getDirection(k));
					list.add(new TMConfiguration(tmt.getToState(), null, configuration.getTapes(), myFilters));
				}
			} else { // only do variable assignments for the one-tape Turing
				// machine...

				// do necessary variable assignments
				final String st = tmt.getRead(0);
				final int assignIndex = st.indexOf('}');

				if (assignIndex != -1) {
					final String s = "" + st.charAt(assignIndex + 1);
					varToChar.put(s, configuration.getTapes().get(0).readChar() + "");
				}

				// perform the operations on the tape, and return a new
				// TMConfiguration that represents the new position
				configuration.getTapes().get(0)
						.writeChar(tmt.getWrite(0).charAt(0) == '~' ? configuration.getTapes().get(0).readChar()
								: (varToChar.containsKey(tmt.getWrite(0).charAt(0) + "")
										? varToChar.get(tmt.getWrite(0).charAt(0) + "").charAt(0)
										: tmt.getWrite(0).charAt(0)));

				configuration.getTapes().get(0).moveHead(tmt.getDirection(0));
				list.add(new TMConfiguration(tmt.getToState(), null, configuration.getTapes(), myFilters)); // no
																											// going
																											// back
				// - we are in a
				// deterministic
				// world. If you
				// freeze, then
				// you will not
				// go forward
				// either.
			}
		} else {

			// halt - set a flag in a place so that the filter can pick it up
			// later - why not just return a list with a configuration that has
			// a flag which the halt will recognize? But then, when should we
			// return an empty list?
			// well, if we get here again, and the halt flag is set already,
			// then we know that we should return an empty list to know that we
			// rejected.
			// MERLIN MERLIN MERLIN MERLIN MERLIN//
			if (!configuration.isHalted()) { // set the halt flag and then add
				// to the list
				configuration.setHalted(true);
				list.add(configuration); // MIGHT need to use clone instead, but
				// if this works, we'll just go with
				// this.
			}
		}
		return list;
	}
}
