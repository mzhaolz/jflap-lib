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

package edu.duke.cs.jflap.gui.editor;

import java.util.Deque;
import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.duke.cs.jflap.automata.Automaton;
import edu.duke.cs.jflap.automata.turing.TuringMachine;
import edu.duke.cs.jflap.gui.environment.Universe;

/**
 * This class will store the states between actions, that we may undo them.
 * Since there should be one set of states per active window, this class should
 * be instantiated within the AutomatonFrame class.
 *
 * Let's use it statically for now (testing / prototype), so we don't have to
 * change environment frame to instantiate it.
 *
 * @author Henry Qin
 *
 */
public class UndoKeeper {
	private final Logger logger = LoggerFactory.getLogger(UndoKeeper.class);

	private final Automaton myMaster;

	private final Deque<Automaton> myDeck;
	private final Deque<Automaton> myBackDeck;

	// private final int DEFAULT_NUM = 50;

	private int numUndo;

	public boolean sensitive = false;

	private boolean wait = false;

	public UndoKeeper(final Automaton master) {
		myMaster = master;
		myDeck = new LinkedList<>();
		myBackDeck = new LinkedList<>();
		numUndo = Universe.curProfile.undo_num;
	}

	public void redo() {
		// EDebug.print("I am mucking with that data structure.");
		if (myBackDeck.size() == 0) {
			return;
		}
		// EDebug.print("Back deck is not empty.");
		//
		myDeck.push(myMaster.clone()); // push on head

		if (myMaster instanceof TuringMachine) {
			TuringMachine.become((TuringMachine) myMaster, (TuringMachine) myBackDeck.pop()); // casting
		} else {
			Automaton.become(myMaster, myBackDeck.pop());
		}

		myMaster.getEnvironmentFrame().repaint();
	}

	/* Undo */
	public void restoreStatus() {
		// EDebug.print("I am mucking with that data structure.");
		if (myDeck.size() == 0) {
			return;
		}

		Automaton p = null;
		while (myDeck.size() > 0 && (p = myDeck.pop()).hashCode() == myMaster.hashCode()) {
			;
		}

		// EDebug.print("Master's hash is " + myMaster.hashCode());
		// EDebug.print("Top hash is " + p.hashCode());

		if (myDeck.size() == 0 && p.hashCode() == myMaster.hashCode()) {
			return;
		}

		sensitive = true;
		myBackDeck.push(myMaster.clone());

		if (myMaster instanceof TuringMachine) {
			TuringMachine.become((TuringMachine) myMaster, (TuringMachine) p);
		} else {
			Automaton.become(myMaster, p); // pop off head
		}

		sensitive = false;
		myMaster.getEnvironmentFrame().repaint();
	}

	public void saveStatus() {
		// EDebug.print("I have been called upon");
		if (wait) {
			wait = false;
			return;
		}

		// EDebug.print("\nFirst place");
		// for (int i = 0; i < myDeck.size(); i++)
		// EDebug.print(((LinkedList)myDeck).get(i).hashCode());

		// if (myDeck.size() > 0)
		// EDebug.print("The top of deck hash is " + myDeck.peek().hashCode());

		myDeck.push(myMaster.clone()); // push on head

		// EDebug.print("The master that is getting pushed on has hascode = " +
		// myMaster.hashCode());

		logger.debug("saveStatus()");

		// EDebug.print("Second place");
		// for (int i = 0; i < myDeck.size(); i++)
		// EDebug.print(((LinkedList)myDeck).get(i).hashCode());
		//
		// EDebug.print("\n");

		if (myDeck.size() >= 2) {
			final Automaton first = myDeck.pop();
			final Automaton second = myDeck.pop();
			// EDebug.print("The first is " + first.hashCode() + "While the
			// second is " + second.hashCode());
			if (first.hashCode() == second.hashCode()) {
				myDeck.push(first);
			} else {
				myDeck.push(second);
				myDeck.push(first);
				myBackDeck.clear();
			}
		}

		// EDebug.print("Third place");
		// for (int i = 0; i < myDeck.size(); i++)
		// EDebug.print(((LinkedList)myDeck).get(i).hashCode());
		// EDebug.print(myDeck.size());

		while (myDeck.size() > numUndo) {
			myDeck.removeLast();
		}
	}

	public void setNumUndo(final int nn) {
		numUndo = nn;
	}

	public void setWait() {
		wait = true;
	}
}
