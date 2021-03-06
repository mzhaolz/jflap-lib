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

import edu.duke.cs.jflap.pumping.Case;
import edu.duke.cs.jflap.pumping.ContextFreePumpingLemma;
import edu.duke.cs.jflap.pumping.LemmaMath;

/**
 * The context-free pumping lemma for <i>L</i> = {<i>ww</i> : <i>w</i> &#8712;
 * {<i>a</i>, <i>b</i>}*}.
 *
 * @author Jinghui Lim & Chris Morgan
 *
 */
public class WW extends ContextFreePumpingLemma {
	/**
	 *
	 */
	private static final long serialVersionUID = -6607037930496485667L;

	@Override
	protected void addCases() {
		/*
		 * v is string of a's and y is string of a's
		 */
		myAllCases.add(new Case() {
			@Override
			public String description() {
				return "v is a string of \"a\"s and y is a string of \"a\"s";
			}

			@Override
			public List<Integer> getPreset() {
				return Lists.newArrayList(0, 1, 0, 1);
			}

			@Override
			public boolean isCase(final String v, final String y) {
				if (v.indexOf("a") > -1 && v.indexOf("b") == -1 && y.indexOf("a") > -1 && y.indexOf("b") == -1) {
					return true;
				}
				return false;
			}
		});
		/*
		 * v is string of a's and y is string of a's followed by b's
		 */
		myAllCases.add(new Case() {

			@Override
			public String description() {
				return "v is a string of \"a\"s and y is a string of \"a\"s followed by \"b\"s";
			}

			@Override
			public List<Integer> getPreset() {
				return Lists.newArrayList(1, 1, 0, m - 1);
			}

			@Override
			public boolean isCase(final String v, final String y) {
				if (v.indexOf("a") > -1 && v.indexOf("b") == -1 && y.indexOf("a") > -1 && y.indexOf("b") > -1
						&& y.indexOf("a") < y.indexOf("b")) {
					return true;
				}
				return false;
			}
		});
		/*
		 * v is string of a's and y is string of b's
		 */
		myAllCases.add(new Case() {
			@Override
			public String description() {
				return "v is a string of \"a\"s and y is a string of \"b\"s";
			}

			@Override
			public List<Integer> getPreset() {
				return Lists.newArrayList(m - 1, 1, 0, 1);
			}

			@Override
			public boolean isCase(final String v, final String y) {
				if (v.indexOf("a") > -1 && v.indexOf("b") == -1 && y.indexOf("a") == -1 && y.indexOf("b") > -1) {
					return true;
				}
				return false;
			}
		});
		/*
		 * v is string of a's followed by b's and y is string of b's
		 */
		myAllCases.add(new Case() {

			@Override
			public String description() {
				return "v is a string of \"a\"s followed by \"b\"s and y is a string of \"b\"s";
			}

			@Override
			public List<Integer> getPreset() {
				return Lists.newArrayList(m - 1, 2, 0, 1);
			}

			@Override
			public boolean isCase(final String v, final String y) {
				if (v.indexOf("a") > -1 && v.indexOf("b") > -1 && v.indexOf("a") < v.indexOf("b")
						&& y.indexOf("a") == -1 && y.indexOf("b") > -1) {
					return true;
				}
				return false;
			}
		});
		/*
		 * v is a string of b's and y is a string of b's
		 */
		myAllCases.add(new Case() {
			@Override
			public String description() {
				return "v is a string of \"b\"s and y is a string of \"b\"s";
			}

			@Override
			public List<Integer> getPreset() {
				return Lists.newArrayList(m, 1, 0, 1);
			}

			@Override
			public boolean isCase(final String v, final String y) {
				if (v.indexOf("a") == -1 && v.indexOf("b") > -1 && y.indexOf("a") == -1 && y.indexOf("b") > -1) {
					return true;
				}
				return false;
			}
		});
		/*
		 * v is a string of b's and y is a string of b's followed by a's
		 */
		myAllCases.add(new Case() {

			@Override
			public String description() {
				return "v is a string of \"b\"s and y is a string of \"b\"s followed by \"a\"s";
			}

			@Override
			public List<Integer> getPreset() {
				return Lists.newArrayList(2 * m - 2, 1, 0, 2);
			}

			@Override
			public boolean isCase(final String v, final String y) {
				if (v.indexOf("a") == -1 && v.indexOf("b") > -1 && y.indexOf("a") > -1 && y.indexOf("b") > -1
						&& y.indexOf("a") > y.indexOf("b")) {
					return true;
				}
				return false;
			}
		});
		/*
		 * v is a string of b's and y is a string of a's
		 */
		myAllCases.add(new Case() {
			@Override
			public String description() {
				return "v is a string of \"b\"s and y is a string of \"a\"s";
			}

			@Override
			public List<Integer> getPreset() {
				return Lists.newArrayList(2 * m - 1, 1, 0, 1);
			}

			@Override
			public boolean isCase(final String v, final String y) {
				if (v.indexOf("a") == -1 && v.indexOf("b") > -1 && y.indexOf("a") > -1 && y.indexOf("b") == -1) {
					return true;
				}
				return false;
			}
		});
		/*
		 * v is a string of b'a followed by a's and y is a string of a's
		 */
		myAllCases.add(new Case() {

			@Override
			public String description() {
				return "v is a string of \"b\"s followed by \"a\"s and y is a string of \"a\"s";
			}

			@Override
			public List<Integer> getPreset() {
				return Lists.newArrayList(2 * m - 1, 2, 0, 1);
			}

			@Override
			public boolean isCase(final String v, final String y) {
				if (v.indexOf("a") > -1 && v.indexOf("b") > -1 && v.indexOf("a") > v.indexOf("b") && y.indexOf("a") > -1
						&& y.indexOf("b") > -1) {
					return true;
				}
				return false;
			}
		});
		/*
		 * v is an empty string and y is a non-empty string
		 */
		myAllCases.add(new Case() {
			@Override
			public String description() {
				return "v is an empty string and y is a non-empty string";
			}

			@Override
			public List<Integer> getPreset() {
				return Lists.newArrayList(2 * m, 0, 1, 1);
			}

			@Override
			public boolean isCase(final String v, final String y) {
				if (v.length() == 0 && y.length() > 0) {
					return true;
				}
				return false;
			}
		});
		/*
		 * v is a non-empty string and y is an empty string
		 */
		myAllCases.add(new Case() {

			@Override
			public String description() {
				return "v is a non-empty string and y is an empty string";
			}

			@Override
			public List<Integer> getPreset() {
				return Lists.newArrayList(2 * m, 1, 0, 0);

			}

			@Override
			public boolean isCase(final String v, final String y) {
				if (v.length() > 0 && y.length() == 0) {
					return true;
				}
				return false;
			}
		});
	}

	@Override
	public void chooseDecomposition() {
		int x, half;
		half = w.length() / 2;
		String first, last;
		// If possible, just set the first characters in the w strings.
		if (m > half) {
			setDecomposition(Lists.newArrayList(0, 1, half - 1, 1));
			return;
		}
		// Otherwise, check to see if there is a pattern.
		for (

				int i = half - m + 1; i < half; i++) {
			for (int j = 0; j < i; j++) {
				if (w.charAt(i) == w.charAt(j)) {
					first = w.substring(0, i) + pumpString("" + w.charAt(i), 2) + w.substring(i + 1, half);
					last = w.substring(0, j) + pumpString("" + w.charAt(j), 2) + w.substring(j + 1, half);
					x = half - i + j - 1;
					if (first.equals(last) && x <= m - 2) {
						setDecomposition(Lists.newArrayList(i, 1, x, 1));
						return;
					}
				}
			}
		}
		// If none will always win, choose a random one.
		super.chooseDecomposition();
	}

	@Override
	public void chooseI() {
		i = 0;
	}

	@Override
	protected void chooseW() {
		w = pumpString("a", getM()) + pumpString("b", getM()) + pumpString("a", getM()) + pumpString("b", getM());
	}

	@Override
	public String getHTMLTitle() {
		return "<i>ww</i> : <i>w</i> " + ELEMENT_OF + " " + AB_STAR;
	}

	@Override
	public String getTitle() {
		return "ww : w element_of {ab}*";
	}

	@Override
	public boolean isInLang(final String s) {
		final char[] list = new char[] { 'a', 'b' };
		if (LemmaMath.otherCharactersFound(s, list)) {
			return false;
		}

		String first, last;
		final int halfSize = s.length() / 2;
		first = s.substring(0, halfSize);
		last = s.substring(halfSize);
		if (first.equals(last)) {
			return true;
		}

		return false;
	}

	@Override
	public void setDescription() {
		partitionIsValid = false;
		explanation = "For any <i>m</i> value, a possible value for <i>w</i> is \"a<sup><i>m</i></sup>"
				+ "b<sup><i>m</i></sup>a<sup><i>m</i></sup>b<sup><i>m</i></sup>\".  To be in the language with "
				+ "this example, <i>v</i> & <i>y</i> together cannot possess identical letters that are from separate "
				+ "blocks of alike letters (ex: <i>v</i> has \"b\"s from the first set of \"b\"s, "
				+ "while <i>y</i> has \"b\"s from the second set of \"b\"s.  Because of this, any increase or decrease in "
				+ "\"a\"s or \"b\"s will not be matched by any corresponding change in the other blocks of similar letters, "
				+ "resulting in an inequality that prevents the decomposition from working.  Thus, this language is "
				+ "not context-free.";
	}

	@Override
	protected void setRange() {
		myRange = Lists.newArrayList(3, 6);

	}

}
