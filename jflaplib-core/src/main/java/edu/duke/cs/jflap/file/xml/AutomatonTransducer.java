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

import static com.google.common.base.Preconditions.checkArgument;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.duke.cs.jflap.automata.Automaton;
import edu.duke.cs.jflap.automata.Note;
import edu.duke.cs.jflap.automata.State;
import edu.duke.cs.jflap.automata.Transition;
import edu.duke.cs.jflap.automata.graph.AutomatonGraph;
import edu.duke.cs.jflap.automata.graph.LayoutAlgorithm;
import edu.duke.cs.jflap.automata.graph.layout.GEMLayoutAlgorithm;
import edu.duke.cs.jflap.automata.mealy.MooreMachine;
import edu.duke.cs.jflap.automata.turing.TMState;
import edu.duke.cs.jflap.automata.turing.TuringMachine;
import edu.duke.cs.jflap.file.DataException;

/**
 * This is an abstract implementation of a transducer that has methods common to
 * all automaton transducers.
 *
 * @author Thomas Finley
 */
public abstract class AutomatonTransducer extends AbstractTransducer {

	private static final String AUTOMATON_NAME = "automaton";

	/** The comment for the list of Automatons. */
	private static final String COMMENT_AUTOMATA = "The list of automata";

	/** The tag name for individual block elements. */
	private static final String FILE_NAME = "tag";

	/** The tag name for individual block elements. */
	public static final String BLOCK_NAME = "block";

	/** The tag name for individual state elements. */
	public static final String STATE_NAME = "state";

	/** The attribute name for the state ID. */
	public static final String STATE_ID_NAME = "id";

	/** The tag name for the X coordinate. */
	public static final String STATE_X_COORD_NAME = "x";

	/** The tag name for the Y coordinate. */
	public static final String STATE_Y_COORD_NAME = "y";

	/** The tag name for the optional label of the state. */
	public static final String STATE_LABEL_NAME = "label";

	/** The tag name for the optional special name of the state. */
	public static final String STATE_NAME_NAME = "name";

	/** The tag name for the final state. */
	public static final String STATE_FINAL_NAME = "final";

	/** The tag name for the optional special name of the state. */
	public static final String STATE_INITIAL_NAME = "initial";

	/** The tag name for the individual transition elements. */
	public static final String TRANSITION_NAME = "transition";

	/** The tag name for the from state ID. */
	public static final String TRANSITION_FROM_NAME = "from";

	/** The tag name for the to state ID. */
	public static final String TRANSITION_TO_NAME = "to";

	/**
	 * The tag name for the x coordinate of the control point for a transition.
	 */
	public static final String TRANSITION_CONTROL_X = "controlx";

	/**
	 * The tag name for the y coordinate of the control point for a transition.
	 */
	public static final String TRANSITION_CONTROL_Y = "controly";

	/** The comment for the list of states. */
	private static final String COMMENT_STATES = "The list of states.";

	/** The comment for the list of transitions. */
	private static final String COMMENT_TRANSITIONS = "The list of transitions.";

	/** The tag name for the individual note elements. */
	public static final String NOTE_NAME = "note";

	/** The tag name for the text of the note elements. */
	public static final String NOTE_TEXT_NAME = "text";

	/**
	 * Used to map a string means to encode a state ID to some unique identifier
	 * object.
	 *
	 * @param string
	 *            a string that encodes a state ID
	 * @return an object that is unique for this state
	 * @throws DataException
	 *             if the state ID is not in a supported format
	 */
	protected static Integer parseID(final String string) {
		try {
			final int num = Integer.parseInt(string);
			return new Integer(num);
		} catch (final NumberFormatException e) {
			return new Integer(-1);
		}
	}

	private final Map<String, Automaton> automatonMap = new HashMap<>();

	// Add the blocks
	protected void addBlocks(final Node node, final Automaton automaton, final Set<State> locatedStates,
			final Map<Integer, State> i2s, final Document document) {
		// this code should really be in TMTransducer, but I see why it's here
		checkArgument(automaton instanceof TuringMachine);
		if (node == null) {
			return;
		}
		if (!node.hasChildNodes()) {
			return;
		}
		final NodeList allNodes = node.getChildNodes();
		final List<Node> blockNodes = new ArrayList<>();
		for (int k = 0; k < allNodes.getLength(); k++) {
			if (allNodes.item(k).getNodeName().equals(BLOCK_NAME)) {
				blockNodes.add(allNodes.item(k));
			}
		}
		final Map<Integer, Node> i2sn = new TreeMap<>((o1, o2) -> o1.intValue() - o2.intValue());
		createState(blockNodes, i2sn, automaton, locatedStates, i2s, true, document);
		// return i2s;
	}

	/**
	 * @param doc
	 * @param tempAuto
	 * @return
	 */
	protected Element createAutomatonElement(final Document document, final Automaton auto, final String name) {
		final Element se = document.getDocumentElement();
		final Element be = createElement(document, name, null, null);
		se.appendChild(be);
		// System.out.println("auto: " + auto);
		writeFields(document, auto, be);
		return be;
	}

	/**
	 * @param doc
	 * @param tempAuto
	 * @return
	 */
	protected Element createBlockElement(final Document document, final TMState block, final Automaton container) {
		final Element be = createElement(document, BLOCK_NAME, null, null);
		be.setAttribute(STATE_ID_NAME, "" + block.getID());
		if (block.getName() != null) {
			be.setAttribute(STATE_NAME_NAME, "" + block.getName());
		}
		be.appendChild(createElement(document, FILE_NAME, null, "" + block.getInternalName()));
		// Encode position.
		be.appendChild(createElement(document, STATE_X_COORD_NAME, null, "" + block.getPoint().getX()));
		be.appendChild(createElement(document, STATE_Y_COORD_NAME, null, "" + block.getPoint().getY()));
		// Encode whether the block is initial.
		// State parent = block.getParentBlock();
		// Automaton a = null;
		// if (parent != null) {
		// a = (Automaton) container.getBlockMap().get(
		// parent.getInternalName());
		// }
		// if (a == null) {
		// a = container;
		// }

		// MERLIN MERLIN MERLIN MERLIN MERLIN//
		final Automaton a = block.getAutomaton(); // this will fetch the parent
													// - I
		// see no reason to go through the
		// block map
		if (a.getInitialState() == block) {
			be.appendChild(createElement(document, STATE_INITIAL_NAME, null, null));
		}
		// Encode whether the state is final.
		if (a.isFinalState(block)) {
			be.appendChild(createElement(document, STATE_FINAL_NAME, null, null));
		}
		return be;
	}

	/**
	 * Returns an empty automaton of the correct type. This method is used by
	 * {@link #fromDOM}.
	 *
	 * @param document
	 *            the DOM document that is being read
	 * @return an empty automaton
	 */
	protected abstract Automaton createEmptyAutomaton(Document document);

	private Node createNoteElement(final Document doc, final Note note) {
		// Start the creation of the transition.
		final Element ne = createElement(doc, NOTE_NAME, null, null);
		// Encode the from state.
		ne.appendChild(createElement(doc, NOTE_TEXT_NAME, null, "" + note.getText()));
		// Encode the to state.
		ne.appendChild(createElement(doc, STATE_X_COORD_NAME, null, "" + note.getLocation().getX()));
		ne.appendChild(createElement(doc, STATE_Y_COORD_NAME, null, "" + note.getLocation().getY()));
		// Return the completed note encoding element.
		return ne;
	}

	// Creates a state node
	protected void createState(final List<Node> stateNodes, final Map<Integer, Node> i2sn, final Automaton automaton,
			final Set<State> locatedStates, final Map<Integer, State> i2s, final boolean isBlock,
			final Document document) {
		// Create the map of ids to state nodes.
		for (int i = 0; i < stateNodes.size(); i++) {
			final Node stateNode = stateNodes.get(i);
			if (stateNode.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}
			// Extract the ID attribute.
			final String idString = ((Element) stateNode).getAttribute(STATE_ID_NAME);
			// //System.out.println("State created with id " + idString);
			if (idString == null) {
				throw new DataException("State without id attribute encountered!");
			}
			final Integer id = parseID(idString);
			// Check for duplicates.
			if (i2sn.put(id, stateNode) != null) {
				throw new DataException("The state ID " + id + " appears twice!");
			}
		}
		// Go through the map, and turn the state nodes into states.
		final Iterator<Integer> it = i2sn.keySet().iterator();
		while (it.hasNext()) {
			final Integer id = it.next();
			final Element stateNode = (Element) i2sn.get(id);
			// Get the fields of this state.
			final Map<String, String> e2t = elementsToText(stateNode);
			// Create the state.
			final java.awt.Point p = new java.awt.Point();
			boolean hasLocation = true;
			// Try to get the X coord.
			double x = 0, y = 0;
			try {
				x = Double.parseDouble(e2t.get(STATE_X_COORD_NAME).toString());
			} catch (final NullPointerException e) {
				hasLocation = false;
			} catch (final NumberFormatException e) {
				throw new DataException(
						"The x coordinate " + e2t.get(STATE_X_COORD_NAME) + " could not be read for state " + id + ".");
			}
			// Try to get the Y coord.
			try {
				y = Double.parseDouble(e2t.get(STATE_Y_COORD_NAME).toString());
			} catch (final NullPointerException e) {
				hasLocation = false;
			} catch (final NumberFormatException e) {
				throw new DataException(
						"The y coordinate " + e2t.get(STATE_Y_COORD_NAME) + " could not be read for state " + id + ".");
			}
			p.setLocation(x, y);
			// Create the state.
			State state = null;
			// if (!isBlock){
			if (!(automaton instanceof TuringMachine)) {
				state = automaton.createStateWithId(p, id.intValue());
			} else {
				Node tempNode = null;
				if (e2t.containsKey(FILE_NAME)) {
					final String fileName = e2t.get(FILE_NAME).toString();
					tempNode = document.getDocumentElement().getElementsByTagName(fileName).item(0);
					final Automaton temp = (TuringMachine) readAutomaton(tempNode, document);
					// MERLIN MERLIN MERLIN MERLIN MERLIN//
					// EDebug.print("Are we or not creating a block?");
					state = ((TuringMachine) automaton).createInnerTM(p, temp, fileName, id.intValue());
				} else {
					state = ((TuringMachine) automaton).createTMStateWithID(p, id.intValue());
				}
			}
			if (hasLocation && locatedStates != null) {
				locatedStates.add(state);
			}
			i2s.put(id, state);
			final String name = stateNode.getAttribute(STATE_NAME_NAME);
			if (name.equals("")) {
				state.setName("q" + id.intValue());
			} else {
				state.setName(name);
			}

			// Add various attributes.
			if (e2t.containsKey(STATE_NAME_NAME)) {
				state.setName(e2t.get(STATE_NAME_NAME));
			}
			if (e2t.containsKey(STATE_LABEL_NAME)) {
				state.setLabel(e2t.get(STATE_LABEL_NAME));
			}
			if (e2t.containsKey(STATE_FINAL_NAME)) {
				automaton.addFinalState(state);
			}
			if (e2t.containsKey(STATE_INITIAL_NAME)) {
				automaton.setInitialState(state);
			}
			/*
			 * If it is a Moore machine, add state output.
			 */
			if (automaton instanceof MooreMachine && e2t.containsKey(MooreTransducer.STATE_OUTPUT_NAME)) {
				((MooreMachine) automaton).setOutput(state, e2t.get(MooreTransducer.STATE_OUTPUT_NAME));
			}
		}
	}

	/**
	 * Produces a DOM element that encodes a given state.
	 *
	 * @param document
	 *            the document to create the state in
	 * @param state
	 *            the state to encode
	 * @return the newly created element that encodes the state
	 * @see #createTransitionElement
	 * @see #toDOM
	 */
	protected Element createStateElement(final Document document, final State state, final Automaton container) {
		// Start the creation of the state tag.
		final Element se = createElement(document, STATE_NAME, null, null);
		se.setAttribute(STATE_ID_NAME, "" + state.getID());
		// Encode position.
		se.appendChild(createElement(document, STATE_X_COORD_NAME, null, "" + state.getPoint().getX()));
		se.appendChild(createElement(document, STATE_Y_COORD_NAME, null, "" + state.getPoint().getY()));
		// Encode label, if set.
		if (state.getLabel() != null) {
			se.appendChild(createElement(document, STATE_LABEL_NAME, null, state.getLabel()));
		}
		// Encode the name, if set.
		if (state.getName() != null) {
			se.setAttribute(STATE_NAME_NAME, "" + state.getName());
			// Encode whether the state is initial.
			// State parent = state.getParentBlock();
			// Automaton a = null;
			// if (parent != null) {
			// a = (Automaton) container.getBlockMap().get(
			// parent.getInternalName());
			// }
			// if (a == null) {
			// a = container;
			// }
		}

		// MERLIN MERLIN MERLIN MERLIN MERLIN//
		final Automaton a = state.getAutomaton();
		if (a.getInitialState() == state) {
			se.appendChild(createElement(document, STATE_INITIAL_NAME, null, null));
		}
		// Encode whether the state is final.
		if (a.isFinalState(state)) {
			se.appendChild(createElement(document, STATE_FINAL_NAME, null, null));
		}

		// Return the completed state encoding element.
		return se;
	}

	/**
	 * Used by the {@link #readTransitions}method. This should be overridden by
	 * subclasses.
	 *
	 * @param from
	 *            the from state
	 * @param to
	 *            the to state
	 * @param node
	 *            the DOM node corresponding to the transition
	 * @param e2t
	 *            elements to text from {@link #elementsToText}
	 * @return the new transition
	 * @see #readTransitions
	 */
	protected abstract Transition createTransition(State from, State to, Node node, Map<String, String> e2t,
			boolean isBlock);

	/**
	 * Produces a DOM element that encodes a given transition. This
	 * implementation creates a transition element with only the "from" and "to"
	 * elements inserted. The intention is that subclasses will use this to get
	 * the minimal transition element, and fill in whatever else is required
	 * themselves.
	 *
	 * @param document
	 *            the document to create the state in
	 * @param transition
	 *            the transition to encode
	 * @return the newly created element that encodes the state
	 * @see #createStateElement
	 * @see #toDOM
	 */
	protected Element createTransitionElement(final Document document, final Transition transition) {
		// Start the creation of the transition.
		final Element te = createElement(document, TRANSITION_NAME, null, null);
		// Encode the from state.
		te.appendChild(createElement(document, TRANSITION_FROM_NAME, null, "" + transition.getFromState().getID()));
		// Encode the to state.
		te.appendChild(createElement(document, TRANSITION_TO_NAME, null, "" + transition.getToState().getID()));

		// if the transition has a control point defined,then get that too
		if (transition.getControl() != null) {
			final Point p = transition.getControl();
			te.appendChild(createElement(document, TRANSITION_CONTROL_X, null, p.x + ""));
			te.appendChild(createElement(document, TRANSITION_CONTROL_Y, null, p.y + ""));
		}

		// Return the completed transition encoding element.
		return te;
	}

	/**
	 * Given a document, this will return the corresponding automaton encoded in
	 * the DOM document.
	 *
	 * @param document
	 *            the DOM document to convert
	 * @return the
	 *         {@link edu.duke.cs.jflap.automata.fsa.FiniteStateAutomaton}instance
	 */
	@Override
	public java.io.Serializable fromDOM(final Document document) {
		automatonMap.clear();
		Node parent = document.getDocumentElement().getElementsByTagName(AUTOMATON_NAME).item(0);
		if (parent == null) {
			parent = document.getDocumentElement();
		}
		return readAutomaton(parent, document);
	}

	/**
	 * Perform graph layout on the automaton if necessary. This is performed for
	 * those XML files with states that do not have their x and y tags
	 * specified.
	 *
	 * @param automaton
	 *            the automaton to lay out
	 * @param locStates
	 *            the states that have the x and y tags in the DOM
	 *            representation and should be kept as "isonodes" in the layout
	 *            algorithm
	 */
	private void performLayout(final Automaton automaton, final Set<State> locStates) {
		// Apply the graph layout algorithm to those states that
		// appeared without the <x> and <y> tags.
		if (locStates.size() == automaton.getStates().size()) {
			return;
		}
		final AutomatonGraph graph = new AutomatonGraph(automaton);
		final LayoutAlgorithm<State> layout = new GEMLayoutAlgorithm<>();
		for (int i = 0; i < 3; i++) {
			// Do it a few times...
			layout.layout(graph, locStates);
		}
		if (locStates.size() < 2) {
			// Make sure things don't get too large or too small in
			// the event that sufficient reference for scaling is not
			// present.
			graph.moveWithinFrame(new java.awt.Rectangle(20, 20, 425, 260));
		}
		graph.moveAutomatonStates();
	}

	public java.io.Serializable readAutomaton(final Node parent, final Document document) {
		final Set<State> locatedStates = new HashSet<>();
		final Automaton root = createEmptyAutomaton(document);
		if (parent == null) {
			return root;
		}
		readBlocks(parent, root, locatedStates, document);
		// Read the states and transitions.
		readTransitions(parent, root, readStates(parent, root, locatedStates, document));
		// read the notes
		readnotes(parent, root, document);
		// Do the layout if necessary.
		performLayout(root, locatedStates);
		automatonMap.put(parent.getNodeName(), root);
		return root;
	}

	/**
	 * @param document
	 * @param a
	 *            *
	 */
	private void readBlocks(final Node parent, final Automaton root, final Set<State> states, final Document document) {
		final Map<Integer, State> i2b = new HashMap<>();
		addBlocks(parent, root, states, i2b, document);
	}

	private void readnotes(final Node parent, final Automaton root, final Document document) {

		final NodeList allNodes = parent.getChildNodes();
		final List<Node> noteNodes = new ArrayList<>();
		for (int k = 0; k < allNodes.getLength(); k++) {
			if (allNodes.item(k).getNodeName().equals(NOTE_NAME)) {
				noteNodes.add(allNodes.item(k));
			}
		}
		for (int i = 0; i < noteNodes.size(); i++) {
			final Node noteNode = noteNodes.get(i);
			if (noteNode.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}

			final Map<String, String> e2t = elementsToText(noteNode);

			final java.awt.Point p = new java.awt.Point();
			final Object obj = (e2t).get(NOTE_TEXT_NAME);
			if (obj == null) {
				continue;
			}
			final String textString = obj.toString();

			// Try to get the X coord.
			double x = 0, y = 0;
			try {
				x = Double.parseDouble(e2t.get(STATE_X_COORD_NAME).toString());
			} catch (final NullPointerException e) {
			} catch (final NumberFormatException e) {
				throw new DataException("The x coordinate " + e2t.get(STATE_X_COORD_NAME)
						+ " could not be read for the note with text " + textString + ".");
			}
			// Try to get the Y coord.
			try {
				y = Double.parseDouble(e2t.get(STATE_Y_COORD_NAME).toString());
			} catch (final NullPointerException e) {
			} catch (final NumberFormatException e) {
				throw new DataException("The y coordinate " + e2t.get(STATE_Y_COORD_NAME)
						+ " could not be read for the note with text " + textString + ".");
			}
			p.setLocation(x, y);

			root.addNote(new Note(p, textString));
		}
	}

	/**
	 * Reads the states from the document and adds them to the automaton. Note
	 * that in the event of error, the automaton may have been changed up until
	 * the point where the error was encountered.
	 *
	 * @param document
	 *            the DOM document to read states from
	 * @param automaton
	 *            the automaton to add states to
	 * @param locatedStates
	 *            if not <CODE>null</CODE>, this set will be filled with those
	 *            states that have their X and Y coordinates specified in the
	 *            DOM and do not need to be laid out
	 * @return a map from state identifiers to the specific state
	 * @throws DataException
	 *             in the case of non-numeric, negative, or duplicate IDs
	 * @see #readTransitions
	 */
	protected Map<Integer, State> readStates(final Node node, final Automaton automaton, final Set<State> locatedStates,
			final Document document) {
		final Map<Integer, State> i2s = new HashMap<>();
		if (node == null) {
			return i2s;
		}
		final NodeList allNodes = node.getChildNodes();
		final ArrayList<Node> stateNodes = new ArrayList<>();
		for (int k = 0; k < allNodes.getLength(); k++) {
			if (allNodes.item(k).getNodeName().equals(STATE_NAME)) {
				stateNodes.add(allNodes.item(k));
			}
		}
		// Map state IDs to states, in an attempt to add in numeric
		// things first. A specialized Comparator is helpful here.
		final Map<Integer, Node> i2sn = new TreeMap<>((o1, o2) -> o1.intValue() - o2.intValue());
		createState(stateNodes, i2sn, automaton, locatedStates, i2s, false, document);
		// i2s = addBlocks(document, automaton, locatedStates, i2s);
		return i2s;
	}

	/**
	 * Reads the transitions from the document and adds them to the automaton.
	 * Note that in the event of error, the automaton may have been changed up
	 * until the point where the error was encountered.
	 *
	 * @param document
	 *            the DOM document to read transitions from
	 * @param automaton
	 *            the automaton to add transitions to
	 * @param id2state
	 *            the map of ID objects to a state
	 * @throws DataException
	 *             in the case of absent from/to states
	 * @see #createTransition
	 * @see #readStates
	 */
	protected void readTransitions(final Node parent, final Automaton automaton, final Map<Integer, State> id2state) {
		if (parent == null || automaton == null) {
			return;
		}
		final NodeList allNodes = parent.getChildNodes();
		final List<Node> tNodes = new ArrayList<>();
		boolean bool = false;
		for (int k = 0; k < allNodes.getLength(); k++) {
			if (allNodes.item(k).getNodeName().equals(TRANSITION_NAME)) {
				tNodes.add(allNodes.item(k));
			}
		}

		// Create the transitions.
		for (int i = 0; i < tNodes.size(); i++) {
			final Node tNode = tNodes.get(i);
			// Get the subelements of this transition.
			final Map<String, String> e2t = elementsToText(tNode);
			// Get the from state.
			final String isBlock = ((Element) tNode).getAttribute("block");
			if (isBlock.equals("true")) {
				bool = true; // We have a block transition.
			}
			final String fromName = e2t.get(TRANSITION_FROM_NAME);
			if (fromName == null) {
				throw new DataException("A transition has no from state!");
			}
			int id = parseID(fromName).intValue();
			final State from = automaton.getStateWithID(id);
			if (from == null) {
				throw new DataException("A transition is defined from " + "non-existent state " + id + "!");
			}
			// Get the to state.
			final String toName = e2t.get(TRANSITION_TO_NAME);
			if (toName == null) {
				throw new DataException("A transition has no to state!");
			}
			id = parseID(toName).intValue();
			final State to = automaton.getStateWithID(id);
			if (to == null) {
				throw new DataException("A transition is defined to " + "non-existent state " + id + "!");
			}
			// Now, make the transition.
			final Transition transition = createTransition(from, to, tNode, e2t, bool);
			automaton.addTransition(transition);
			bool = false;

			// deal with the shapiness of the transition, if the file specifies
			// it. //add controlX and controlY
			final String controlX = e2t.get(TRANSITION_CONTROL_X);
			final String controlY = e2t.get(TRANSITION_CONTROL_Y);
			if (controlX != null && controlY != null) {
				final Point p = new Point(Integer.parseInt(controlX), Integer.parseInt(controlY));
				transition.setControl(p);
			} else { // explicit is better than implicit
				transition.setControl(null);
			}
		}
	}

	/**
	 * Given a JFLAP automaton, this will return the corresponding DOM encoding
	 * of the structure.
	 *
	 * @param structure
	 *            the JFLAP automaton to encode
	 * @return a DOM document instance
	 */
	@Override
	public Document toDOM(final java.io.Serializable structure) {
		final Automaton automaton = (Automaton) structure;
		final Document doc = newEmptyDocument();
		final Element se = doc.getDocumentElement();
		se.appendChild(createAutomatonElement(doc, automaton, AUTOMATON_NAME));

		// Return the completed document.
		return doc;
	}

	private Element writeFields(final Document doc, final Automaton auto, final Element se) {
		// Add the states as subelements of the structure element.
		final List<State> states = auto.getStates();
		if (states.size() > 0) {
			se.appendChild(createComment(doc, COMMENT_STATES));
		}

		if (auto instanceof TuringMachine) {
			for (int i = 0; i < states.size(); i++) {
				se.appendChild(createBlockElement(doc, (TMState) states.get(i), auto));
			}
		} else {
			for (int i = 0; i < states.size(); i++) {
				se.appendChild(createStateElement(doc, states.get(i), auto));
			}
		}

		// Add the transitions as subelements of the structure element.
		final List<Transition> transitions = auto.getTransitions();
		if (transitions.size() > 0) {
			se.appendChild(createComment(doc, COMMENT_TRANSITIONS));
		}
		for (int i = 0; i < transitions.size(); i++) {
			se.appendChild(createTransitionElement(doc, transitions.get(i)));
		}

		// Add the Automatons the blocks refer to as sub elements of the
		// structure element.

		// only really need an internal name and a full TuringMachine
		// MERLIN MERLIN MERLIN MERLIN MERLIN//
		if (auto instanceof TuringMachine) { // there should not be building
			// blocks in non-Turing Machines
			final Map<String, TuringMachine> references = ((TuringMachine) auto).getBlockMap();
			final Iterator<String> refer = references.keySet().iterator();
			if (refer.hasNext()) {
				se.appendChild(createComment(doc, COMMENT_AUTOMATA));
			}
			while (refer.hasNext()) {
				final String name = refer.next();
				if (!automatonMap.containsKey(references.get(name))) {
					se.appendChild(createAutomatonElement(doc, references.get(name), name));
					automatonMap.put(name, auto);
				}
			}
		}

		// Add the sticky notes at the very end
		final List<Note> notes = auto.getNotes();
		for (int k = 0; k < notes.size(); k++) {
			se.appendChild(createNoteElement(doc, notes.get(k)));
		}
		return se;
	}
}
