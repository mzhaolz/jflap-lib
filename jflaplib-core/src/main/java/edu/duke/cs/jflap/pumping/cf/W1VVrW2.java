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

package edu.duke.cs.jflap.pumping.cf;

import java.util.List;

import com.google.common.collect.Lists;

import edu.duke.cs.jflap.pumping.ContextFreePumpingLemma;
import edu.duke.cs.jflap.pumping.LemmaMath;

/**
 * The context-free pumping lemma for <i>L</i> =
 * {<i>w<sub>1</sub>vv<sup>R</sup>w<sub>2</sub></i>, :
 * <i>n<sub>a</sub></i>(<i>w<sub>1</sub></i>) =
 * <i>n<sub>a</sub></i>(<i>w<sub>2</sub></i>), |<i>v</i>| &#8805; 3, <i>v</i>,
 * <i>w<sub>1</sub></i>, & <i>w<sub>2</sub></i> &#8712; {<i>a</i>, <i>b</i>}*}.
 *
 * @author Chris Morgan
 */
public class W1VVrW2 extends ContextFreePumpingLemma {

	/**
	 *
	 */
	private static final long serialVersionUID = 255488807130945086L;

	@Override
	protected void addCases() {
		// TODO Auto-generated method stub
	}

	@Override
	public void chooseDecomposition() {
		final List<Integer> v = getVVr(w);
		String w1, w2;
		w1 = w.substring(0, v.get(0));
		w2 = w.substring(v.get(1) + 1);
		// If possible, the last character in v and the first in v^R
		if (v.get(1) - v.get(0) > 5
				|| LemmaMath.countInstances(w1, 'b') == 0 && LemmaMath.countInstances(w2, 'b') == 0) {
			setDecomposition(Lists.newArrayList(v.get(0) + (v.get(1) - v.get(0)) / 2, 1, 0, 1));
		} else if (w1.indexOf('b') > -1) {
			setDecomposition(Lists.newArrayList(w1.indexOf('b'), 1, 0, 0));
		} else {
			setDecomposition(Lists.newArrayList(w2.indexOf('b') + v.get(1) + 1, 1, 0, 0));
		}

	}

	@Override
	public void chooseI() {
		i = 3;
	}

	@Override
	protected void chooseW() {
		final int power = m / 2;
		w = pumpString("ab", power) + "abbbba" + pumpString("ab", power);
	}

	@Override
	public String getHTMLTitle() {
		return "<i>w<sub>1</sub>vv<sup>R</sup>w<sub>2</sub></i>, : " + "<i>n<sub>a</sub></i>(<i>w<sub>1</sub></i>) = "
				+ "<i>n<sub>a</sub></i>(<i>w<sub>2</sub></i>),  " + "|<i>v</i>| " + GREATER_THAN
				+ " 3,  <i>v</i>, <i>w<sub>1</sub>, " + "w<sub>2</sub> " + ELEMENT_OF + " " + AB_STAR;
	}

	@Override
	public String getTitle() {
		return "w1 v v^R w2 : na(w1) = na(w2), |v|>=3, w1 & w2 element_of {ab}*";
	}

	/**
	 * This method returns the first acceptable vv<sup>R</sup> segment that it
	 * can find.
	 *
	 * @param s
	 *            the string in which to find the vv<sup>R</sup> segment.
	 * @return the segment in an int[], with the first index of the segment of
	 *         the given string in the first array item, and the last index in
	 *         the second array item.
	 */
	private List<Integer>

			getVVr(final String s) {
		if (s.length() < 6) {
			return null;
		}

		boolean match;
		for (int end = s.length() - 1; end >= 5; end--) {
			for (int start = 0; start <= end - 5; start++) {
				if ((end - start) % 2 == 1 && s.charAt(start) == s.charAt(end)) {
					match = true;
					for (int i = 0; i <= (end - start) / 2; i++) {
						if (s.charAt(start + i) != s.charAt(end - i)) {
							match = false;
						}
					}
					if (match && LemmaMath.countInstances(s.substring(0, start), 'a') == LemmaMath
							.countInstances(s.substring(end + 1), 'a')) {
						return Lists.newArrayList(start, end);
					}
				}
			}
			;
		}

		return null;

	}

	@Override
	public boolean isInLang(final String s) {
		final char[] list = new char[] { 'a', 'b' };
		if (LemmaMath.otherCharactersFound(s, list)) {
			return false;
		}

		if (getVVr(s) == null) {
			return false;
		} else {
			return true;
		}
	}

	@Override
	public void setDescription() {
		partitionIsValid = true;
		explanation = "Because this is a context-free language, a valid decomposition exists.  If |'v'| " + GREATER_THAN
				+ " 3, " + "or if <i>m</i> " + GREATER_OR_EQ
				+ " 8 and there are no \"b\"s in w<sub>1</sub> and w<sub>2</sub>, one could "
				+ "just pump single opposite characters in 'v' and 'v<sup>R</sup>' repeatedly to find a valid decomposition.  "
				+ "For example, if |'v'| = 4, then <i>v</i> could equal the fourth character of 'v' and <i>y</i> the first "
				+ "character of 'v<sup>R</sup>'.  Otherwise, if <i>m</i> " + GREATER_OR_EQ
				+ " 8 and |v| = 3, one could just " + "pump the first \"b\" value in w<sub>1</sub> or w<sub>2</sub>.";
	}

	@Override
	protected void setRange() {
		myRange = Lists.newArrayList(2, 15);

	}

}
