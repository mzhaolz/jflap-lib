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
 * The context-free pumping lemma for <i>L</i> =
 * {<i>a<sup>k</sup>b<sup>n</sup>c<sup>n</sup>d<sup>j</sup></i> : <i>j</i>
 * &#8800; k}.
 *
 * @author Chris Morgan & Chris Morgan
 */
public class AkBnCnDj extends ContextFreePumpingLemma {

	/**
	 *
	 */
	private static final long serialVersionUID = -7823242644145426440L;

	@Override
	protected void addCases() {
		/*
		 * v is a string of a's and y is a string of a's
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
				if (v.indexOf("a") > -1 && v.indexOf("b") == -1 && v.indexOf("c") == -1 && v.indexOf("d") == -1
						&& y.indexOf("a") > -1 && y.indexOf("b") == -1 && y.indexOf("c") == -1
						&& y.indexOf("d") == -1) {
					return true;
				}
				return false;
			}
		});
		/*
		 * v is a string of a's and y is a string of a's followed by b's
		 */
		myAllCases.add(new Case() {

			@Override
			public String description() {
				return "v is a string of \"a\"s and y is a string of \"a\"s followed by \"b\"s";
			}

			@Override
			public List<Integer> getPreset() {
				return Lists.newArrayList(m - 2, 1, 0, 2);
			}

			@Override
			public boolean isCase(final String v, final String y) {
				if (v.indexOf("a") > -1 && v.indexOf("b") == -1 && v.indexOf("c") == -1 && v.indexOf("d") == -1
						&& y.indexOf("a") > -1 && y.indexOf("b") > -1 && y.indexOf("c") == -1 && y.indexOf("d") == -1) {
					return true;
				}
				return false;
			}
		});
		/*
		 * v is a string of a's and y is a string of b's
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
				if (v.indexOf("a") > -1 && v.indexOf("b") == -1 && v.indexOf("c") == -1 && v.indexOf("d") == -1
						&& y.indexOf("a") == -1 && y.indexOf("b") > -1 && y.indexOf("c") == -1
						&& y.indexOf("d") == -1) {
					return true;
				}
				return false;
			}
		});
		/*
		 * v is a string of a's followed by b's and y is a string of b's
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
				if (v.indexOf("a") > -1 && v.indexOf("b") > -1 && v.indexOf("c") == -1 && v.indexOf("d") == -1
						&& y.indexOf("a") == -1 && y.indexOf("b") > -1 && y.indexOf("c") == -1
						&& y.indexOf("d") == -1) {
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
				if (v.indexOf("a") == -1 && v.indexOf("b") > -1 && v.indexOf("c") == -1 && v.indexOf("d") == -1
						&& y.indexOf("a") == -1 && y.indexOf("b") > -1 && y.indexOf("c") == -1
						&& y.indexOf("d") == -1) {
					return true;
				}
				return false;
			}
		});
		/*
		 * v is a string of b's and y is a string of b's followed by c's
		 */
		myAllCases.add(new Case() {

			@Override
			public String description() {
				return "v is a string of \"b\"s and y is a string of \"b\"s followed by \"c\"s";
			}

			@Override
			public List<Integer> getPreset() {
				return Lists.newArrayList(2 * m - 2, 1, 0, 2);
			}

			@Override
			public boolean isCase(final String v, final String y) {
				if (v.indexOf("a") == -1 && v.indexOf("b") > -1 && v.indexOf("c") == -1 && v.indexOf("d") == -1
						&& y.indexOf("a") == -1 && y.indexOf("b") > -1 && y.indexOf("c") > -1 && y.indexOf("d") == -1) {
					return true;
				}
				return false;
			}
		});
		/*
		 * v is a string of b's and y is a string of c's
		 */
		myAllCases.add(new Case() {
			@Override
			public String description() {
				return "v is a string of \"b\"s and y is a string of \"c\"s";
			}

			@Override
			public List<Integer> getPreset() {
				return Lists.newArrayList(2 * m - 1, 1, 0, 1);
			}

			@Override
			public boolean isCase(final String v, final String y) {
				if (v.indexOf("a") == -1 && v.indexOf("b") > -1 && v.indexOf("c") == -1 && v.indexOf("d") == -1
						&& y.indexOf("a") == -1 && y.indexOf("b") == -1 && y.indexOf("c") > -1
						&& y.indexOf("d") == -1) {
					return true;
				}
				return false;
			}
		});
		/*
		 * v is a string of b's followed by c's and y is a string of c's
		 */
		myAllCases.add(new Case() {

			@Override
			public String description() {
				return "v is a string of \"b\"s followed by \"c\"s and y is a string of \"c\"s";
			}

			@Override
			public List<Integer> getPreset() {
				return Lists.newArrayList(2 * m - 1, 2, 0, 1);
			}

			@Override
			public boolean isCase(final String v, final String y) {
				if (v.indexOf("a") == -1 && v.indexOf("b") > -1 && v.indexOf("c") > -1 && v.indexOf("d") == -1
						&& y.indexOf("a") == -1 && y.indexOf("b") == -1 && y.indexOf("c") > -1
						&& y.indexOf("d") == -1) {
					return true;
				}
				return false;
			}
		});
		/*
		 * v is a string of c's and y is a string of c's
		 */
		myAllCases.add(new Case() {
			@Override
			public String description() {
				return "v is a string of \"c\"s and y is a string of \"c\"s";
			}

			@Override
			public List<Integer> getPreset() {
				return Lists.newArrayList(2 * m, 1, 0, 1);
			}

			@Override
			public boolean isCase(final String v, final String y) {
				if (v.indexOf("a") == -1 && v.indexOf("b") == -1 && v.indexOf("c") > -1 && v.indexOf("d") == -1
						&& y.indexOf("a") == -1 && y.indexOf("b") == -1 && y.indexOf("c") > -1
						&& y.indexOf("d") == -1) {
					return true;
				}
				return false;
			}
		});
		/*
		 * v is a string of c's and y is a string of c's followed by d's
		 */
		myAllCases.add(new Case() {

			@Override
			public String description() {
				return "v is a string of \"c\"s and y is a string of \"c\"s followed by \"d\"s";
			}

			@Override
			public List<Integer> getPreset() {
				return Lists.newArrayList(3 * m - 2, 1, 0, 2);
			}

			@Override
			public boolean isCase(final String v, final String y) {
				if (v.indexOf("a") == -1 && v.indexOf("b") == -1 && v.indexOf("c") > -1 && v.indexOf("d") == -1
						&& y.indexOf("a") == -1 && y.indexOf("b") == -1 && y.indexOf("c") > -1 && y.indexOf("d") > -1) {
					return true;
				}
				return false;
			}
		});
		/*
		 * v is a string of c's and y is a string of d's
		 */
		myAllCases.add(new Case() {
			@Override
			public String description() {
				return "v is a string of \"c\"s and y is a string of \"d\"s";
			}

			@Override
			public List<Integer> getPreset() {
				return Lists.newArrayList(3 * m - 1, 1, 0, 1);
			}

			@Override
			public boolean isCase(final String v, final String y) {
				if (v.indexOf("a") == -1 && v.indexOf("b") == -1 && v.indexOf("c") > -1 && v.indexOf("d") == -1
						&& y.indexOf("a") == -1 && y.indexOf("b") == -1 && y.indexOf("c") == -1
						&& y.indexOf("d") > -1) {
					return true;
				}
				return false;
			}
		});
		/*
		 * v is a string of c's followed by d's and y is a string of d's
		 */
		myAllCases.add(new Case() {

			@Override
			public String description() {
				return "v is a string of \"c\"s followed by \"d\"s and y is a string of \"d\"s";
			}

			@Override
			public List<Integer> getPreset() {
				return Lists.newArrayList(3 * m - 1, 2, 0, 1);
			}

			@Override
			public boolean isCase(final String v, final String y) {
				if (v.indexOf("a") == -1 && v.indexOf("b") == -1 && v.indexOf("c") > -1 && v.indexOf("d") > -1
						&& y.indexOf("a") == -1 && y.indexOf("b") == -1 && y.indexOf("c") == -1
						&& y.indexOf("d") > -1) {
					return true;
				}
				return false;
			}
		});
		/*
		 * v is a string of d's and y is a string of d's
		 */
		myAllCases.add(new Case() {
			@Override
			public String description() {
				return "v is a string of \"d\"s and y is a string of \"d\"s";
			}

			@Override
			public List<Integer> getPreset() {
				return Lists.newArrayList(3 * m, 1, 0, 1);
			}

			@Override
			public boolean isCase(final String v, final String y) {
				if (v.indexOf("a") == -1 && v.indexOf("b") == -1 && v.indexOf("c") == -1 && v.indexOf("d") > -1
						&& y.indexOf("a") == -1 && y.indexOf("b") == -1 && y.indexOf("c") == -1
						&& y.indexOf("d") > -1) {
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
		int a, b, c, d;
		a = w.indexOf('a');
		b = w.indexOf('b');
		c = w.indexOf('c');
		d = w.indexOf('d');

		if (b > -1 && c > -1) {
			setDecomposition(Lists.newArrayList(c - 1, 1, 0, 1));
		} else if (a > -1 && d > -1) {
			setDecomposition(Lists.newArrayList(d - 1, 1, 0, 1));
		} else {
			super.chooseDecomposition();
		}

	}

	@Override
	public void chooseI() {
		final int da = LemmaMath.countInstances(getV(), 'a') + LemmaMath.countInstances(getY(), 'a');
		if (da == 1) {
			i = 2;
		} else {
			i = 0;
		}
	}

	@Override
	protected void chooseW() {
		w = pumpString("a", m) + pumpString("b", m) + pumpString("c", m) + pumpString("d", m + 1);
	}

	@Override
	public String getHTMLTitle() {
		return "<i>a<sup>k</sup>b<sup>n</sup>c<sup>n</sup>d<sup>j</sup></i> : <i>j</i> " + NOT_EQUAL + " k";
	}

	@Override
	public String getTitle() {
		return "a^k b^n c^n d^j : j != k";
	}

	@Override
	public boolean isInLang(final String s) {
		final char[] list = new char[] { 'a', 'b', 'c', 'd' };
		if (LemmaMath.isMixture(s, list)) {
			return false;
		}

		final List<Integer> sections = Lists.newArrayList(0, 0, 0, 0);

		int i, j;

		i = 0;
		j = 0;
		while (i < s.length()) {
			if (s.charAt(i) != list[j]) {
				j++;
			} else {
				sections.set(j, sections.get(j) + 1);
				i++;
			}
		}

		if (sections.get(1) == sections.get(2) && sections.get(0) != sections.get(3)) {
			return true;
		}
		return false;
	}

	@Override
	public void setDescription() {
		partitionIsValid = true;
		explanation = "Because this is a context-free language, a valid decomposition exists.  For all m "
				+ GREATER_OR_EQ + " 2, " + "if <i>n</i> " + GREATER_OR_EQ
				+ " 1, <i>v</i> could equal \"b\" and <i>y</i> could equal \"c\".  If <i>n</i> "
				+ "= 0 and <i>k</i> & <i>j</i> " + GREATER_OR_EQ
				+ " 1, <i>v</i> could equal \"a\" and <i>y</i> could equal "
				+ "\"d\".  If <i>n</i> = 0 and only one of <i>k</i> or <i>j</i> " + GREATER_OR_EQ
				+ " 1, <i>v</i> could equal \"a\" "
				+ "or \"d\" (whichever one is in the string).  and <i>y</i> could be empty.  This covers all possible combinations.";
	}

	@Override
	protected void setRange() {
		myRange = Lists.newArrayList(3, 5);

	}

}
