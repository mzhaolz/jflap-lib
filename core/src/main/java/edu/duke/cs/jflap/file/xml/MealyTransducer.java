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

package edu.duke.cs.jflap.file.xml;

import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import edu.duke.cs.jflap.automata.Automaton;
import edu.duke.cs.jflap.automata.State;
import edu.duke.cs.jflap.automata.Transition;
import edu.duke.cs.jflap.automata.mealy.MealyMachine;
import edu.duke.cs.jflap.automata.mealy.MealyTransition;

/**
 * This is the transducer for encoding and decoding
 * {@link edu.duke.cs.jflap.automata.mealy.MealyMachine} objects.
 *
 * @author Jinghui Lim
 *
 */
public class MealyTransducer extends AutomatonTransducer {
	/**
	 * The tag name for the read string transition elements.
	 */
	public static final String TRANSITION_READ_NAME = "read";
	/**
	 * The tag name for the output string transition elements.
	 */
	public static final String TRANSITION_OUTPUT_NAME = "transout";

	/**
	 * Creates and returns an empty Mealy machine.
	 *
	 * @param document
	 *            the DOM document that is being red
	 * @return an empty Mealy machine
	 */
	@Override
	protected Automaton createEmptyAutomaton(final Document document) {
		return new MealyMachine();
	}

	/**
	 * Creates and returns a transition consistent with this node.
	 *
	 * @param from
	 *            the from state
	 * @param to
	 *            the to state
	 * @param node
	 *            the DOM node corresponding to the transition, which should
	 *            contain a "read" element, a "pop" element, and a "push"
	 *            elements
	 * @param e2t
	 *            elements to text from {@link #elementsToText}
	 * @param isBlock
	 * @return the new transition
	 */
	@Override
	protected Transition createTransition(final State from, final State to, final Node node,
			final Map<String, String> e2t, final boolean isBlock) {
		/*
		 * The boolean isBlock seems to be ignored in FSATransducer.java, so I'm
		 * ignoring it here too.
		 */
		String label = e2t.get(TRANSITION_READ_NAME);
		String output = e2t.get(TRANSITION_OUTPUT_NAME);
		if (label == null) {
			label = "";
		}
		if (output == null) {
			output = "";
		}
		return new MealyTransition(from, to, label, output);
	}

	/**
	 * Produces a DOM element that encodes a given transition. This adds the
	 * strings to read and the output.
	 *
	 * @param document
	 *            the document to create the state in
	 * @param transition
	 *            the transition to encode
	 * @return the newly created element that encodes the transition
	 * @see edu.duke.cs.jflap.file.xml.AutomatonTransducer#createTransitionElement
	 */
	@Override
	protected Element createTransitionElement(final Document document, final Transition transition) {
		final Element te = super.createTransitionElement(document, transition);
		final MealyTransition t = (MealyTransition) transition;
		te.appendChild(createElement(document, TRANSITION_READ_NAME, null, t.getLabel()));
		te.appendChild(createElement(document, TRANSITION_OUTPUT_NAME, null, t.getOutput()));
		return te;
	}

	/**
	 * Returns the type string for this transducer, "mealy".
	 *
	 * @return the string "mealy"
	 */
	@Override
	public String getType() {
		return "mealy";
	}
}
