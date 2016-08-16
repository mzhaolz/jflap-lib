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

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

import edu.duke.cs.jflap.pumping.ContextFreePumpingLemma;
import edu.duke.cs.jflap.pumping.LemmaMath;

/**
 * The context-free pumping lemma for <i>L</i> =
 * {<i>w<sub>1</sub>cw<sub>2</sub>cw<sub>3</sub>cw<sub>4</sub></i>, :
 * <i>w<sub>1</sub></i> = <i>w<sub>2</sub></i> or <i>w<sub>3</sub></i> =
 * <i>w<sub>4</sub></i>, <i>w<sub>i</sub></i> &#8712; {<i>a</i>, <i>b</i>}*,
 * |<i>w<sub>i</sub></i>| > 0}.
 *
 * @author Chris Morgan
 */
public class W1CW2CW3CW4 extends ContextFreePumpingLemma {

	/**
	 *
	 */
	private static final long serialVersionUID = 56169474941404352L;

	@Override
	protected void addCases() {
		// TODO Auto-generated method stub

	}

	/**
	 * Checks to see if it is possible to find a decomposition with the two
	 * <i>w</i> strings given.
	 *
	 * @param s0
	 *            - the first string
	 * @param s1
	 *            - the second string
	 * @param shift
	 *            - the amount to shift over the decomposition, based on the
	 *            position of the first string.
	 * @return null if no decomposition is adequate, the decomposition if it is.
	 */
	private List<Integer> checkIfPossibility(final String s0, final String s1, final int shift) {
		int x;
		String first, last;
		if (s0.length() == 1 && s1.length() == 1) {
			return null;
		} else if (s0.length() == 1) {
			return Lists.newArrayList(shift + s0.length() + 1, 1, 0, 0);
		} else if (s1.length() == 1 || !s0.equals(s1)) {
			return Lists.newArrayList(shift, 1, 0, 0);
		} else if (m >= s1.length() + 2) {
			return Lists.newArrayList(shift, 1, s0.length(), 1);
		}
		// Otherwise, check to see if there is a pattern.
		for (

				int i = s1.length() - m + 2; i < s1.length(); i++) {
			for (int j = 0; j < i; j++) {
				if (s1.charAt(i) == s1.charAt(j)) {
					first = s1.substring(0, i) + pumpString("" + s1.charAt(i), 2) + s1.substring(i + 1, s1.length());
					last = s1.substring(0, j) + pumpString("" + s1.charAt(j), 2) + s1.substring(j + 1, s1.length());
					x = s1.length() - i + j;
					if (first.equals(last) && x <= m - 2) {
						return Lists.newArrayList(shift + i, 1, x, 1);
					}
				}
			}
		}
		return null;
	}

	@Override
	public void chooseDecomposition() {
		final List<String> wArray = getWs(w);
		List<Integer> decomp = checkIfPossibility(wArray.get(0), wArray.get(1), 0);
		if (decomp != null) {
			setDecomposition(decomp);
			return;
		}
		decomp = checkIfPossibility(wArray.get(2), wArray.get(3), wArray.get(0).length() + wArray.get(1).length() + 2);
		if (decomp != null) {
			setDecomposition(decomp);
			return;
		}
		super.chooseDecomposition();
	}

	@Override
	public void chooseI() {
		i = 0;
	}

	@Override
	protected void chooseW() {
		w = pumpString("a", m) + pumpString("b", m) + 'c' + pumpString("a", m) + pumpString("b", m) + "cacb";
	}

	@Override
	public String getHTMLTitle() {
		return "<i>w<sub>1</sub>cw<sub>2</sub>cw<sub>3</sub>cw<sub>4</sub></i>, : "
				+ "<i>w<sub>1</sub></i> = <i>w<sub>2</sub></i> or " + "<i>w<sub>3</sub></i> = <i>w<sub>4</sub></i>, "
				+ "<i>w<sub>i</sub></i> " + ELEMENT_OF + " " + AB_STAR + ", |<i>w<sub>i</sub></i>| > 0";
	}

	@Override
	public String getTitle() {
		return "w1cw2cw3cw4 : w1 = w2 or w3 = w4, wi element_of {ab}*, |wi| >= 5";
	}

	/**
	 * Will return a String[] containing all the w[k] values from w[0]...w[3] in
	 * the given string.
	 *
	 * @param s
	 *            the string to check.
	 * @return the w[k] values.
	 */
	private List<String> getWs(final String s) {
		final List<String> w = new ArrayList<>();
		String temp = s;
		int c;

		for (int i = 0; i < 3; i++) {
			c = temp.indexOf('c');
			if (c == -1) {
				return null;
			}
			w.add(temp.substring(0, c));
			temp = temp.substring(c + 1);
		}
		w.set(3, temp);
		return w;
	}

	@Override
	public boolean isInLang(final String s) {
		final List<String> w = getWs(s);
		if (w == null) {
			return false;
		}

		final char[] list = new char[] { 'a', 'b' };
		for (int i = 0; i < 4; i++) {
			if (LemmaMath.otherCharactersFound(w.get(i), list) || w.get(i).length() == 0) {
				return false;
			}
		}

		if (w.get(0).equals(w.get(1)) || w.get(2).equals(w.get(3))) {
			return true;
		}
		return false;
	}

	@Override
	public void setDescription() {
		partitionIsValid = false;
		explanation = "For any <i>m</i> value, a possible value for <i>w</i> is \"a<sup><i>m</i></sup>"
				+ "b<sup><i>m</i></sup>ca<sup><i>m</i></sup>b<sup><i>m</i></sup>cacb\".  If either <i>v</i> or <i>y</i> "
				+ "together span two 'w<sub>n</sub>'s or span less but possess a \"c\", then pumping that value could result "
				+ "in more or less than three \"c\"s, which is not permissible.  If either <i>v</i> or <i>y</i> span "
				+ "'w<sub>3</sub>' or 'w<sub>4</sub>', then if <i>i</i> = 0, |'w<sub>3</sub>'| = 0 or |'w<sub>4</sub>'| = 0.  "
				+ "If either <i>v</i> or <i>y</i> span 'w<sub>1</sub>' or 'w<sub>2</sub>', then for any <i>i</i> "
				+ NOT_EQUAL + " 1, 'w<sub>1</sub>' " + NOT_EQUAL
				+ " 'w<sub>2</sub>'.  Thus, this language is not context-free.";
	}

	@Override
	protected void setRange() {
		myRange = Lists.newArrayList(2, 7);

	}

}
