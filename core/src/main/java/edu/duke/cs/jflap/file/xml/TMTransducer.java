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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.duke.cs.jflap.automata.Automaton;
import edu.duke.cs.jflap.automata.State;
import edu.duke.cs.jflap.automata.Transition;
import edu.duke.cs.jflap.automata.turing.TMTransition;
import edu.duke.cs.jflap.automata.turing.TuringMachine;
import edu.duke.cs.jflap.file.DataException;

/**
 * This is the transducer for encoding and decoding
 * {@link edu.duke.cs.jflap.automata.turing.TuringMachine} objects.
 *
 * @author Thomas Finley
 */
public class TMTransducer extends AutomatonTransducer {
	/** The tag name for the number of tape element. */
	public static final String TURING_TAPES_NAME = "tapes";

	/** The attribute name for the tape ID attribute. */
	public static final String TRANSITION_TAPE_NAME = "tape";

	/** The tag name for the read string transition elements. */
	public static final String TRANSITION_READ_NAME = "read";

	/** The tag name for the write string transition elements. */
	public static final String TRANSITION_WRITE_NAME = "write";

	/** The tag name for the move string transition elements. */
	public static final String TRANSITION_MOVE_NAME = "move";

	public static final String IS_BLOCK = "block";

	/**
	 * Creates and returns an empty Turing machine.
	 *
	 * @param document
	 *            the DOM document that is being read
	 * @return an empty Turing machine
	 */
	@Override
	protected Automaton createEmptyAutomaton(final Document document) {
		final Map<String, String> e2t = elementsToText(document.getDocumentElement());
		String s = e2t.get(TURING_TAPES_NAME);
		if (s == null) {
			s = "1";
		}
		try {
			final int tapes = Integer.parseInt(s);
			if (tapes < 1 || tapes > 5) {
				throw new DataException(tapes + " invalid # of tapes.  Valid # of tapes 1-5.");
			}
			return new TuringMachine(tapes);
		} catch (final NumberFormatException e) {
			throw new DataException("Error reading " + s + " as number of tapes.");
		}
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
	 * @return the new transition
	 */
	@Override
	protected Transition createTransition(final State from, final State to, final Node node,
			final Map<String, String> e2t, final boolean isBlock) {
		final TuringMachine tm = (TuringMachine) from.getAutomaton();
		final int tapes = tm.tapes();
		final List<String> readStrings = new ArrayList<>(), writeStrings = new ArrayList<>(),
				moveStrings = new ArrayList<>();
		// Set defaults in case the transition for that tape is not specified.
		IntStream.range(0, tapes).forEach(i -> {
			readStrings.add("");
			writeStrings.add("");
			moveStrings.add("R");
		});
		// Avoid undue code duplication.
		final Map<String, List<String>> tag2array = new HashMap<>();
		tag2array.put(TRANSITION_READ_NAME, readStrings);
		tag2array.put(TRANSITION_WRITE_NAME, writeStrings);
		tag2array.put(TRANSITION_MOVE_NAME, moveStrings);
		// Go through the tags.
		final Iterator<String> it = tag2array.keySet().iterator();
		while (it.hasNext()) {
			final String tag = it.next();
			final List<String> array = tag2array.get(tag);
			final NodeList nodes = ((Element) node).getElementsByTagName(tag);
			for (int i = 0; i < nodes.getLength(); i++) {
				final Element elem = (Element) nodes.item(i);
				// Get which tape this is for.
				String tapeString = elem.getAttribute(TRANSITION_TAPE_NAME);
				if (tapeString.length() == 0) {
					tapeString = "1"; // Default single tape.
				}
				int tape = 1;
				try {
					tape = Integer.parseInt(tapeString);
					if (tape < 1 || tape > tapes) {
						throw new DataException(
								"In " + tag + " tag, tape " + tape + " identified but only 1-" + tapes + " are valid.");
					}
				} catch (final NumberFormatException e) {
					throw new DataException("In " + tag + " tag, error reading " + tapeString + " as tape.");
				}
				// Get the contained text.
				String contained = containedText(elem);
				if (contained == null) {
					contained = "";
				}
				// Set the right text.
				array.set(tape - 1, contained);

				if (isBlock) {
					for (int j = 0; j < writeStrings.size(); j++) {
						writeStrings.set(i, "~");
						moveStrings.set(i, "S");
					}
				}
			}
		}
		// Now, try creating the transition.
		try {
			// System.out.println(isBlock);
			final TMTransition t = new TMTransition(from, to, readStrings, writeStrings, moveStrings);
			if (isBlock) {
				t.setBlockTransition(true);
			}
			return t;
		} catch (final IllegalArgumentException e) {
			throw new DataException(e.getMessage());
		}
	}

	/**
	 * Produces a DOM element that encodes a given transition. This adds the
	 * strings to read, write, and move for each tape.
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
		final TMTransition t = (TMTransition) transition;
		final TuringMachine tm = (TuringMachine) t.getFromState().getAutomaton();
		// Add the characterizing strings for this transition.
		final Map<String, String> attr = new HashMap<>();
		for (int i = 0; i < tm.tapes(); i++) {
			if (tm.tapes() > 1) {
				attr.put(TRANSITION_TAPE_NAME, "" + (i + 1));
			}
			String read = t.getRead(i), write = t.getWrite(i);
			if (read.equals(TMTransition.BLANK)) {
				read = "";
			}
			if (write.equals(TMTransition.BLANK)) {
				write = "";
			}
			te.appendChild(createElement(document, TRANSITION_READ_NAME, attr, read));
			if (t.isBlockTransition()) {
				te.setAttribute(IS_BLOCK, "" + "true");
				return te;
			}
			te.appendChild(createElement(document, TRANSITION_WRITE_NAME, attr, write));
			te.appendChild(createElement(document, TRANSITION_MOVE_NAME, attr, t.getDirection(i)));
		}
		return te;
	}

	/**
	 * Returns the type string for this transducer, "pda".
	 *
	 * @return the string "pda"
	 */
	@Override
	public String getType() {
		return "turing";
	}

	/**
	 * Given a Turing machine, this will return the corresponding DOM encoding
	 * of the structure. Tpreferenceshis uses the existing method
	 * {@link edu.duke.cs.jflap.file.xml.AutomatonTransducer#toDOM} to produce
	 * the DOM, and also adds a "tapes" tag containing the number of tapes if
	 * the number of tapes is not 1.
	 *
	 * @param structure
	 *            the structure, which should be a turing machine
	 */
	@Override
	public Document toDOM(final java.io.Serializable structure) {
		final Document dom = super.toDOM(structure);
		final TuringMachine tm = (TuringMachine) structure;
		if (tm.tapes() > 1) {
			final Element tapesElement = createElement(dom, TURING_TAPES_NAME, null, "" + tm.tapes());
			final NodeList list = dom.getDocumentElement().getChildNodes();
			if (list.getLength() == 1) {
				dom.getDocumentElement().appendChild(tapesElement);
			} else {
				dom.getDocumentElement().insertBefore(tapesElement, list.item(1));
			}
		}
		return dom;
	}
}
