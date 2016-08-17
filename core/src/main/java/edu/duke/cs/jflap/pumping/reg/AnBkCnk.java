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

package edu.duke.cs.jflap.pumping.reg;

import com.google.common.collect.Lists;

import edu.duke.cs.jflap.pumping.LemmaMath;
import edu.duke.cs.jflap.pumping.RegularPumpingLemma;

/**
 * The regular pumping lemma for <i>L</i> =
 * {<i>a<sup>n</sup>b<sup>k</sup>c<sup>n+k</sup></i> : <i>n</i> &#8805; 0,
 * <i>k</i> &#8805; 0}.
 *
 * @author Jinghui Lim & Chris Morgan
 *
 */
public class AnBkCnk extends RegularPumpingLemma {
	/**
	 *
	 */
	private static final long serialVersionUID = -2843533645633275259L;

	@Override
	public void chooseI() {
		i = LemmaMath.flipCoin();
	}

	@Override
	protected void chooseW() {
		w = pumpString("a", getM()) + pumpString("b", getM()) + pumpString("c", getM() * 2);
	}

	@Override
	public String getHTMLTitle() {
		return "<i>a<sup>n</sup>b<sup>k</sup>c<sup>n+k</sup></i> : <i>n</i> " + GREATER_OR_EQ + " 0, <i>k</i> "
				+ GREATER_OR_EQ + " 0";
	}

	@Override
	public String getTitle() {
		return "a^n b^k c^(n+k) : n >= 0, k >= 0";
	}

	@Override
	public boolean isInLang(final String s) {
		int a, b, c;
		final char[] list = new char[] { 'a', 'b', 'c' };
		if (LemmaMath.isMixture(s, list)) {
			return false;
		}

		a = LemmaMath.countInstances(s, 'a');
		b = LemmaMath.countInstances(s, 'b');
		c = LemmaMath.countInstances(s, 'c');
		if (a + b == c) {
			return true;
		}
		return false;
	}

	@Override
	public void setDescription() {
		partitionIsValid = false;
		explanation = "For any <i>m</i> value, a possible value for <i>w</i> is \"a<sup><i>m</i></sup>"
				+ "b<sup><i>m</i></sup>c<sup>2<i>m</i></sup>\".  The <i>y</i> value thus would be a multiple of \"a\".  "
				+ "If <i>i</i> = 0, the string becomes at most \"a<sup><i>m</i>-1</sup>b<sup><i>m</i></sup>"
				+ "c<sup>2<i>m</i></sup>\", which is not in the language.  Thus, the language is not regular.";
	}

	@Override
	protected void setRange() {
		myRange = Lists.newArrayList(2, 9);
	}
}