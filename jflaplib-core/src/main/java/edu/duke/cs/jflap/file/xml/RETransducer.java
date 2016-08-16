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

import edu.duke.cs.jflap.file.ParseException;
import edu.duke.cs.jflap.regular.RegularExpression;

/**
 * This transducer is the codec for
 * {@link edu.duke.cs.jflap.regular.RegularExpression} objects.
 *
 * @author Thomas Finley
 */
public class RETransducer extends AbstractTransducer {
	/** The tag name for the regular expression itself. */
	public static final String EXPRESSION_NAME = "expression";

	/** The comment for the list of productions. */
	private static final String COMMENT_EXPRESSION = "The regular expression.";

	/**
	 * Given a document, this will return the corresponding regular expression
	 * encoded in the DOM document.
	 *
	 * @param document
	 *            the DOM document to convert
	 * @return the {@link edu.duke.cs.jflap.regular.RegularExpression} instance
	 */
	@Override
	public java.io.Serializable fromDOM(final Document document) {
		final Map<?, ?> e2t = elementsToText(document.getDocumentElement());
		String expression = (String) e2t.get(EXPRESSION_NAME);
		if (expression == null) {
			if (e2t.containsKey(EXPRESSION_NAME)) {
				throw new ParseException("Regular expression structure has no " + EXPRESSION_NAME + " tag!");
			} else {
				expression = "";
			}
		}
		return new RegularExpression(expression);
	}

	/**
	 * Returns the type this transducer recognizes, "re".
	 *
	 * @return the string "re"
	 */
	@Override
	public String getType() {
		return "re";
	}

	/**
	 * Given a JFLAP regular expression, this will return the corresponding DOM
	 * encoding of the structure.
	 *
	 * @param structure
	 *            the regular expression to encode
	 * @return a DOM document instance
	 */
	@Override
	public Document toDOM(final java.io.Serializable structure) {
		final RegularExpression re = (RegularExpression) structure;
		final Document doc = newEmptyDocument();
		final Element se = doc.getDocumentElement();
		// Add the regular expression tag.
		se.appendChild(createComment(doc, COMMENT_EXPRESSION));
		se.appendChild(createElement(doc, EXPRESSION_NAME, null, re.asString()));
		// Return the completed document.
		return doc;
	}
}
