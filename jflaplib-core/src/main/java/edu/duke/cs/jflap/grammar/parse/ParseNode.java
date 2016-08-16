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

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Arrays;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import edu.duke.cs.jflap.grammar.Production;

/**
 * A parse node is used as an aide for brute force parsing. It contains a
 * derivation field showing a current derivation of a string, the grammar rules
 * that were substitute in to achieve this, and the position of those
 * substitutions.
 * <P>
 *
 * For example, if the string "aACBb" were to derive "axxCyyb" based on the
 * rules "A->xx" and "B->yy", then the rules used array would hold those two
 * rules, and the substitution array would be {1,3} for the positions in the
 * original string that the substitutions happened in.
 *
 * @author Thomas Finley
 */
public class ParseNode extends DefaultMutableTreeNode {
	/**
	 *
	 */
	private static final long serialVersionUID = 2188813856397032225L;

	/** The current string derivation. */
	private final String derivation;

	/** The grammar rules used to achieve this derivation. */
	private final List<Production> productions;

	/** The positions at which substitutions were attempted. */
	private final List<Integer> subs;

	/**
	 * Instantiates a parse node based on an existing node.
	 *
	 * @param node
	 *            the parse node to copy
	 */
	public ParseNode(final ParseNode node) {
		this(node.derivation, node.productions, node.subs);
	}

	/**
	 * Instantiates a new parse node.
	 *
	 * @param derivation
	 *            the derivation of this rule
	 * @param productions2
	 *            the productions that led to this derivation
	 * @param subs2
	 *            the positions in the parent string derivation that the
	 *            productions were substituted in to achieve this derivation
	 */
	public ParseNode(final String derivation, final List<Production> productions2, final List<Integer> subs2) {
		this.derivation = derivation;
		checkArgument(productions2.size() == subs2.size(), "Production and subsitutions sizes must match.");

		productions = productions2;
		subs = subs2;
	}

	/**
	 * Returns the derivation string.
	 *
	 * @return the derivation string
	 */
	public String getDerivation() {
		return derivation;
	}

	/**
	 * Returns the productions array for this node. For performance reasons this
	 * array could not be copied, and so must not be modified.
	 *
	 * @return the productions that were substituted in to achieve the
	 *         derivation
	 */
	public List<Production> getProductions() {
		return productions;
	}

	/**
	 * Returns the substitution positions. For performance reasons this array
	 * could not be copied, and so must not be modified.
	 *
	 * @return the positions for the substitutions of the productions in the
	 *         parent derivation that led to this current derivation
	 */
	public List<Integer> getSubstitutions() {
		return subs;
	}

	/**
	 * Returns a string representation of those object.
	 *
	 * @return a string representation of those object
	 */
	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer(derivation);
		sb.append(", ");
		sb.append(Arrays.asList(productions) + ", ");
		sb.append('[');
		for (int j = 0; j < subs.size(); j++) {
			if (j != 0) {
				sb.append(", ");
			}
			sb.append(subs.get(j));
		}
		sb.append(']');
		return sb.toString();
	}
}
