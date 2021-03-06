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

import java.io.Serializable;
import java.util.ArrayList;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.google.common.collect.Lists;

import edu.duke.cs.jflap.pumping.RegularPumpingLemma;

/**
 * This is the transducer for encoding and decoding
 * {@link edu.duke.cs.jflap.pumping.RegularPumpingLemma} objects.
 *
 * @author Jinghui Lim
 * @see edu.duke.cs.jflap.gui.pumping.PumpingLemmaChooser
 *
 */
public class RegPumpingLemmaTransducer extends PumpingLemmaTransducer {
	/**
	 * The type of pumping lemma.
	 */
	public static String TYPE = "regular pumping lemma";
	/**
	 * The tag for the length of <i>x</i>.
	 */
	public static String X_NAME = "xLength";
	/**
	 * The tag for the length of <i>y</i>.
	 */
	public static String Y_NAME = "yLength";

	@Override
	public Serializable fromDOM(final Document document) {
		final RegularPumpingLemma pl = (RegularPumpingLemma) PumpingLemmaFactory.createPumpingLemma(TYPE,
				document.getElementsByTagName(LEMMA_NAME).item(0).getTextContent());

		// Decode m, w, & i.
		pl.setM(Integer.parseInt(document.getElementsByTagName(M_NAME).item(0).getTextContent()));
		pl.setW(document.getElementsByTagName(W_NAME).item(0).getTextContent());
		pl.setI(Integer.parseInt(document.getElementsByTagName(I_NAME).item(0).getTextContent()));

		// Decode the attempts
		final NodeList attempts = document.getDocumentElement().getElementsByTagName(ATTEMPT);
		for (int i = 0; i < attempts.getLength(); i++) {
			pl.addAttempt(attempts.item(i).getTextContent());
		}

		// Decode the first player.
		pl.setFirstPlayer(document.getElementsByTagName(FIRST_PLAYER).item(0).getTextContent());

		// Decode the decomposition.
		final int xLength = Integer.parseInt(document.getElementsByTagName(X_NAME).item(0).getTextContent());
		final int yLength = Integer.parseInt(document.getElementsByTagName(Y_NAME).item(0).getTextContent());
		pl.setDecomposition(Lists.newArrayList(xLength, yLength));

		return pl;
	}

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public Document toDOM(final Serializable structure) {
		final RegularPumpingLemma pl = (RegularPumpingLemma) structure;
		final Document doc = newEmptyDocument();
		final Element elem = doc.getDocumentElement();
		elem.appendChild(createElement(doc, LEMMA_NAME, null, pl.getTitle()));
		elem.appendChild(createElement(doc, FIRST_PLAYER, null, pl.getFirstPlayer()));
		elem.appendChild(createElement(doc, M_NAME, null, "" + pl.getM()));
		elem.appendChild(createElement(doc, W_NAME, null, "" + pl.getW()));
		elem.appendChild(createElement(doc, I_NAME, null, "" + pl.getI()));
		elem.appendChild(createElement(doc, X_NAME, null, "" + pl.getX().length()));
		elem.appendChild(createElement(doc, Y_NAME, null, "" + pl.getY().length()));

		// Encode the list of attempts.
		final ArrayList<?> attempts = pl.getAttempts();
		if (attempts != null && attempts.size() > 0) {
			for (int i = 0; i < attempts.size(); i++) {
				elem.appendChild(createElement(doc, ATTEMPT, null, (String) attempts.get(i)));
			}
		}

		return doc;
	}
}
