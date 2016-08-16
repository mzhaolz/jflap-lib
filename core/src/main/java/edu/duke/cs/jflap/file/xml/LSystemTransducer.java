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
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.duke.cs.jflap.file.ParseException;
import edu.duke.cs.jflap.grammar.Grammar;
import edu.duke.cs.jflap.grammar.Production;
import edu.duke.cs.jflap.grammar.UnboundGrammar;
import edu.duke.cs.jflap.grammar.lsystem.LSystem;

/**
 * This transducer is the codec for
 * {@link edu.duke.cs.jflap.grammar.lsystem.LSystem} objects.
 *
 * @author Thomas Finley
 */
public class LSystemTransducer extends AbstractTransducer {
	/** The tag name for the axiom. */
	public static final String AXIOM_NAME = "axiom";

	/** The tag name for a rewriting rule. */
	public static final String RULE_NAME = "production";

	/** The tag name for the left of a rewriting rule. */
	public static final String RULE_LEFT_NAME = "left";

	/** The tag name for a right of a rewriting rule. */
	public static final String RULE_RIGHT_NAME = "right";

	/** The tag name for one of the drawing parameters. */
	public static final String PARAMETER_NAME = "parameter";

	/** The tag name for the parameter name. */
	public static final String PARAMETER_NAME_NAME = "name";

	/** The tag name for the parameter value. */
	public static final String PARAMETER_VALUE_NAME = "value";

	/** The comment for the axiom. */
	private static final String COMMENT_AXIOM = "The L-system axiom.";

	/** The comment for the list of rewriting rules. */
	private static final String COMMENT_RULE = "The rewriting rules.";

	/** The comment for the list of parameters. */
	private static final String COMMENT_PARAMETER = "The drawing parameters.";

	/**
	 * Returns an element that encodes a given rewriting rule.
	 *
	 * @param document
	 *            the document to create the element in
	 * @param lsystem
	 *            the L-system we are encoding
	 * @param left
	 *            the replacement predicate we are replacing
	 * @return an element that encodes a production
	 */
	public static Element createRuleElement(final Document document, final LSystem lsystem, final String left) {
		final Element re = createElement(document, RULE_NAME, null, null);
		re.appendChild(createElement(document, RULE_LEFT_NAME, null, left));
		final List<List<String>> replacements = lsystem.getReplacements(left);
		for (int i = 0; i < replacements.size(); i++) {
			re.appendChild(createElement(document, RULE_RIGHT_NAME, null, listAsString(replacements.get(i))));
		}
		return re;
	}

	/**
	 * Returns the productions representing the rewriting rules for a given
	 * node.
	 *
	 * @param node
	 *            the node the encapsulates a production
	 */
	private static List<Production> createRules(final Node node) {
		final Map<?, ?> e2t = elementsToText(node);
		String left = (String) e2t.get(RULE_LEFT_NAME);
		if (left == null) {
			left = "";
		}
		final NodeList list = ((Element) node).getElementsByTagName(RULE_RIGHT_NAME);
		final List<Production> p = new ArrayList<>();
		for (int i = 0; i < list.getLength(); i++) {
			final String right = containedText(list.item(i));
			p.add(new Production(left, right == null ? "" : right));
		}
		return p;
	}

	/**
	 * Given a list of objects, this converts it to a space delimited string.
	 *
	 * @param list
	 *            the list to convert to a string
	 * @return a string containing the elements of the list
	 */
	private static String listAsString(final List<?> list) {
		final Iterator<?> it = list.iterator();
		if (!it.hasNext()) {
			return "";
		}
		final StringBuffer sb = new StringBuffer();
		sb.append(it.next());
		while (it.hasNext()) {
			sb.append(' ');
			sb.append(it.next());
		}
		return sb.toString();
	}

	/**
	 * Given a document, this will return the corresponding L-system encoded in
	 * the DOM document.
	 *
	 * @param document
	 *            the DOM document to convert
	 * @return the {@link edu.duke.cs.jflap.grammar.lsystem.LSystem} instance
	 */
	@Override
	public java.io.Serializable fromDOM(final Document document) {
		final String axiom = readAxiom(document);
		final Grammar rules = readGrammar(document);
		final Map<String, String> parameters = readParameters(document);
		return new LSystem(axiom, rules, parameters);
	}

	/**
	 * Returns the type this transducer recognizes, "lsystem".
	 *
	 * @return the string "lsystem"
	 */
	@Override
	public String getType() {
		return "lsystem";
	}

	/**
	 * Reads the axiom from the DOM and returns it as a string.
	 *
	 * @param document
	 *            the DOM document to read the axiom from
	 * @return the string that contains the axiom
	 */
	private String readAxiom(final Document document) {
		final NodeList list = document.getDocumentElement().getElementsByTagName(AXIOM_NAME);
		if (list.getLength() < 1) {
			throw new ParseException("No axiom specified in the document!");
		}
		String axiom = containedText(list.item(list.getLength() - 1));
		if (axiom == null) {
			axiom = "";
		}
		return axiom;
	}

	/**
	 * Reads the rewriting rules from the DOM and returns it as a grammar.
	 *
	 * @param document
	 *            the DOM document to read rewriting rules from
	 * @return the grammar that holds these results
	 */
	private Grammar readGrammar(final Document document) {
		final Grammar g = new UnboundGrammar();
		final NodeList list = document.getDocumentElement().getElementsByTagName(RULE_NAME);
		for (int i = 0; i < list.getLength(); i++) {
			g.addProductions(createRules(list.item(i)));
		}
		return g;
	}

	/**
	 * Reads the parameters from the DOM document and returns it as a map from
	 * parameter names to values.
	 *
	 * @param document
	 *            the DOM document to read parameters from
	 * @return the mapping of parameter names to values
	 */
	private Map<String, String> readParameters(final Document document) {
		final Map<String, String> p = new HashMap<>();
		final NodeList list = document.getDocumentElement().getElementsByTagName(PARAMETER_NAME);
		for (int i = 0; i < list.getLength(); i++) {
			final Map<?, ?> e2t = elementsToText(list.item(i));
			final String name = (String) e2t.get(PARAMETER_NAME_NAME);
			String value = (String) e2t.get(PARAMETER_VALUE_NAME);
			if (name == null) {
				continue;
			}
			if (value == null) {
				value = "";
			}
			p.put(name, value);
		}
		return p;
	}

	/**
	 * Given a JFLAP L-system, this will return the corresponding DOM encoding
	 * of the structure.
	 *
	 * @param structure
	 *            the JFLAP L-system to encode
	 * @return a DOM document instance
	 */
	@Override
	public Document toDOM(final java.io.Serializable structure) {
		final LSystem lsystem = (LSystem) structure;
		final Document doc = newEmptyDocument();
		final Element se = doc.getDocumentElement();
		// Add the axiom.
		se.appendChild(createComment(doc, COMMENT_AXIOM));
		se.appendChild(createElement(doc, AXIOM_NAME, null, listAsString(lsystem.getAxiom())));
		// Add the rewriting rules.
		final Set<?> symbols = lsystem.getSymbolsWithReplacements();
		Iterator<?> it = symbols.iterator();
		if (it.hasNext()) {
			se.appendChild(createComment(doc, COMMENT_RULE));
		}
		while (it.hasNext()) {
			se.appendChild(createRuleElement(doc, lsystem, (String) it.next()));
		}
		// Add the parameters.
		final Map<?, ?> parameters = lsystem.getValues();
		it = parameters.keySet().iterator();
		if (it.hasNext()) {
			se.appendChild(createComment(doc, COMMENT_PARAMETER));
		}
		while (it.hasNext()) {
			final String name = (String) it.next();
			final String value = (String) parameters.get(name);
			final Element pe = createElement(doc, PARAMETER_NAME, null, null);
			pe.appendChild(createElement(doc, PARAMETER_NAME_NAME, null, name));
			pe.appendChild(createElement(doc, PARAMETER_VALUE_NAME, null, value));
			se.appendChild(pe);
		}
		// Return the completed document.
		return doc;
	}
}
