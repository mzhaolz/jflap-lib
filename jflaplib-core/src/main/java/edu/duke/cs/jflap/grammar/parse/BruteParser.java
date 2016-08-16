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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.duke.cs.jflap.grammar.Grammar;
import edu.duke.cs.jflap.grammar.Production;

/**
 * The <CODE>BruteParser</CODE> is an abstract class that will perform a brute
 * force parse of a grammar.
 *
 * @author Thomas Finley
 */
public abstract class BruteParser {
	// Stuff for the possibilities.
	private static final List<Production> P = new ArrayList<>();

	private static final List<Integer> S = new ArrayList<>();

	private static final ParseNode E = new ParseNode("", P, S);

	/**
	 * This factory method will return a brute force parser appropriate for the
	 * grammar.
	 *
	 * @param grammar
	 *            the grammar to get a brute force parser for
	 * @param target
	 *            the target string
	 */
	public static BruteParser get(final Grammar grammar, final String target) {
		if (Unrestricted.isUnrestricted(grammar)) {
			return new UnrestrictedBruteParser(grammar, target);
		}
		return new RestrictedBruteParser(grammar, target);
	}

	/** The set of listeners. */
	protected Set<BruteParserListener> listeners = new HashSet<>();

	/** This is the grammar. */
	protected Grammar grammar;

	/** The array of productions. */
	protected List<Production> productions;

	/** This is the target string. */
	protected String target;

	/** This should be set to done when the operation has completed. */
	private boolean isDone = false;

	/**
	 * This is the thread that does the parsing; if the value is
	 * <CODE>null</CODE> that indicates that no parsing thread exists yet.
	 */
	private Thread parseThread = null;

	/** This set holds those strings already added to the tree. */
	private final Set<String> alreadyAdded = new HashSet<>();

	/**
	 * This holds those strings that have already been derived, with a map to
	 * those nodes for each string.
	 */
	private final Map<String, List<ParseNode>> prederived = new HashMap<>();

	/** This holds the list of nodes for the BFS. */
	protected LinkedList<ParseNode> queue = new LinkedList<>();

	/** The number of explored nodes. */
	private int consideredNodes = 0;

	/** The number of unexplored but perhaps soon to be explored nodes. */
	private int beingConsideredNodes = 0;

	/** The number of deleted nodes. */
	private int deletedNodes = 0;

	/** The "answer" to the parse question. */
	private ParseNode answer = null;

	/**
	 * The "smaller" set, those symbols that may possibly reduce to nothing.
	 */
	protected Set<String> smaller;

	/**
	 * This does nothing. One can use this constructor if one wants to call init
	 * later.
	 */
	protected BruteParser() {
	}

	/**
	 * This will instantiate a new brute parser. All this does is call
	 * {@link #init} with the grammar and target string.
	 */
	public BruteParser(final Grammar grammar, final String target) {
		init(grammar, target);
	}

	/**
	 * Adds a brute parser listener to this parser.
	 *
	 * @param listener
	 *            the listener to add
	 */
	public void addBruteParserListener(final BruteParserListener listener) {
		listeners.add(listener);
	}

	/**
	 * Distributes a brute parser event to all listeners.
	 *
	 * @param event
	 *            the brute parser event to distribute
	 */
	protected void distributeEvent(final BruteParserEvent event) {
		final Iterator<BruteParserListener> it = listeners.iterator();
		while (it.hasNext()) {
			it.next().bruteParserStateChange(event);
		}
	}

	/**
	 * This returns the answer node for the parser.
	 *
	 * @return the answer node for the parse, or <CODE>null</CODE> if there was
	 *         no answer, or one has not been discovered yet
	 */
	public synchronized ParseNode getAnswer() {
		return answer;
	}

	/**
	 * Returns the number of nodes on the current "consideration" queue. These
	 * nodes have not yet been added.
	 */
	public int getConsiderationNodeCount() {
		return beingConsideredNodes;
	}

	/**
	 * Returns the number of nodes currently in the tree.
	 *
	 * @return number of nodes in the tree whose paths have not been ruled out
	 */
	public int getCurrentNodeCount() {
		return consideredNodes - deletedNodes;
	}

	/**
	 * Returns a list of possible one step parses for a given string. The first
	 * entry is always the identity.
	 */
	private List<ParseNode> getPossibilities(final String c) {
		final List<ParseNode> possibilities = new ArrayList<>();
		if (prederived.containsKey(c)) {
			return prederived.get(c);
		}
		final HashSet<String> alreadyEncountered = new HashSet<>();
		if (c.length() == 0) {
			possibilities.add(E);
			return possibilities;
		}
		for (int i = -1; i < productions.size(); i++) {
			final Production prod = i == -1 ? new Production(c.substring(0, 1), c.substring(0, 1)) : productions.get(i);
			// Find the start of the production.
			final int start = c.indexOf(prod.getLHS());
			final int lengthSubs = prod.getLHS().length();
			if (start == -1) {
				continue;
			}
			final List<ParseNode> list = getPossibilities(c.substring(start + lengthSubs));
			final Iterator<ParseNode> it = list.iterator();
			final String prepend = c.substring(0, start) + prod.getRHS();
			final int lengthReplace = start + prod.getLHS().length();
			// Make adjustments for each entry.
			while (it.hasNext()) {
				ParseNode node = it.next();
				final String d = node.getDerivation();
				final List<Production> p = node.getProductions();
				final String a = prepend + d;
				final List<Integer> s = node.getSubstitutions();
				if (i == -1) {
					final List<Integer> newS = new ArrayList<>();
					for (int j = 0; j < p.size(); j++) {
						newS.add(s.get(j) + lengthReplace);
					}
					// Make the node with the substitution.
					if (alreadyEncountered.add(a)) {
						node = new ParseNode(a, p, newS);
						possibilities.add(node);
						beingConsideredNodes++;
					}
				} else {
					final List<Production> newP = new ArrayList<>(Collections.nCopies(p.size() + 1, null));
					final List<Integer> newS = new ArrayList<>(Collections.nCopies(s.size() + 1, null));
					newS.set(0, start);
					newP.set(0, prod);
					for (int j = 0; j < p.size(); j++) {
						newP.set(j + 1, p.get(j));
						newS.set(j + 1, s.get(j) + lengthReplace);
					}
					// Make the node with the substitution.
					if (alreadyEncountered.add(a)) {
						node = new ParseNode(a, newP, newS);
						possibilities.add(node);
						beingConsideredNodes++;
					}
				}
			}
		}
		// prederived.put(c, possibilities);
		return possibilities;
	}

	/**
	 * Returns the number of nodes generated.
	 *
	 * @return number of nodes in the tree, even those that have been ruled out
	 *         by now
	 */
	public int getTotalNodeCount() {
		return consideredNodes;
	}

	/**
	 * This will initialize data structures.
	 */
	protected void init(Grammar grammar, final String target) {
		for (int i = 0; i < target.length(); i++) {
			if (!grammar.isTerminal(target.substring(i, i + 1))) {
				throw new IllegalArgumentException(
						"String to parse has nonterminal " + target.substring(i, i + 1) + ".");
			}
		}

		queue.clear();

		grammar = Unrestricted.optimize(grammar);
		if (grammar == null) {
			return;
		}
		queue.add(new ParseNode(grammar.getStartVariable(), P, S));
		consideredNodes = 0;
		deletedNodes = 0;

		smaller = Collections.unmodifiableSet(Unrestricted.smallerSymbols(grammar));

		this.grammar = grammar;
		productions = grammar.getProductions();
		this.target = target;
	}

	/**
	 * Returns if the parser is currently in the process of parsing.
	 *
	 * @return <CODE>true</CODE> if the parser thread is currently active, or
	 *         <CODE>false</CODE> if the parser thread is inactive
	 */
	public synchronized boolean isActive() {
		return parseThread != null;
	}

	/**
	 * Returns if the parser has finished, with success or otherwise.
	 *
	 * @return <CODE>true</CODE> if the
	 */
	public synchronized boolean isFinished() {
		return isDone;
	}

	/**
	 * Given a string, this method should indicate whether or not this
	 * derivation could eventually result in our target. Note that returning
	 * <CODE>true</CODE> does not mean that this derivation can actually result
	 * in the target, just that it has not been firmly ruled out. This method is
	 * used for optimization purposes by the parser.
	 *
	 * @param derivation
	 */
	public boolean isPossibleDerivation(final String derivation) {
		return Unrestricted.minimumLength(derivation, smaller) <= target.length();
	}

	/**
	 * The parsing method.
	 */
	private synchronized void parse() {
		if (queue.isEmpty()) {
			isDone = true;
			parseThread = null;
			distributeEvent(new BruteParserEvent(this, BruteParserEvent.REJECT));
			return;
		}
		// Get one element.
		final ParseNode node = queue.removeFirst();
		beingConsideredNodes = 0;
		final List<ParseNode> pos = getPossibilities(node.getDerivation());
		beingConsideredNodes = 0;
		final Iterator<ParseNode> it = pos.iterator();
		while (it.hasNext()) {
			ParseNode pNode = it.next();
			if (!alreadyAdded.add(pNode.getDerivation())) {
				continue;
			}
			if (!isPossibleDerivation(pNode.getDerivation())) {
				continue;
			}
			pNode = new ParseNode(pNode);
			node.add(pNode);
			queue.add(pNode);
			consideredNodes++;

			if (pNode.getDerivation().equals(target)) {
				answer = pNode;
				isDone = true;
				parseThread = null;
				queue.clear();
				distributeEvent(new BruteParserEvent(this, BruteParserEvent.ACCEPT));
				return;
			}
		}
		// Was anything added?
		if (node.isLeaf()) {
			removeFutility(node);
		}
	}

	/**
	 * This will pause the parsing. At the end of this method the parsing thread
	 * will probably not halt.
	 */
	public synchronized void pause() {
		parseThread = null;
		distributeEvent(new BruteParserEvent(this, BruteParserEvent.PAUSE));
	}

	/**
	 * Removes a brute parser listener from this parser.
	 *
	 * @param listener
	 *            the listener to remove
	 */
	public void removeBruteParserListener(final BruteParserListener listener) {
		listeners.remove(listener);
	}

	/**
	 * Any node that is not accepted and can have no children is futile. Since
	 * the tree is doubly linked, the entire BFS tree will be preserved. By
	 * removing futile nodes, this allows recursion to proceed much deeper. If
	 * the node passed in has no children, it is removed from the parent, and we
	 * recurse on the parent.
	 *
	 * @param node
	 *            a possibly futile node
	 */
	private void removeFutility(ParseNode node) {
		try {
			while (node.isLeaf()) {
				((ParseNode) node.getParent()).remove(node);
				deletedNodes++;
				node = (ParseNode) node.getParent();
			}
		} catch (final NullPointerException e) {
			// The parent didn't exist, so we're done.
		}
	}

	/**
	 * This will start the parsing. This method will return immediately. The
	 * parsing is done in a separate thread since the potential for the parsing
	 * to take forever on some brute force parses exists.
	 *
	 * @return if the starting of the parsing was successful, which will not be
	 *         successful if the parsing is already underway, or if the parser
	 *         is finished
	 */
	public synchronized boolean start() {
		if (isActive() || isFinished()) {
			return false;
		}
		parseThread = new Thread() {
			@Override
			public void run() {
				while (parseThread != null) {
					parse();
				}
			}
		};
		parseThread.start();
		distributeEvent(new BruteParserEvent(this, BruteParserEvent.START));
		return true;
	}
}
