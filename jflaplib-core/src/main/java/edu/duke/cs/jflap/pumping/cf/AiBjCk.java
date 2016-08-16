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
 * {<i>a<sup>i</sup>b<sup>j</sup>c<sup>k</sup></i> : <i>i</i> &#62; <i>j</i>,
 * <i>i</i> &#62; <i>k</i>}.
 *
 * @author Jinghui Lim & Chris Morgan
 *
 */
public class AiBjCk extends ContextFreePumpingLemma {
	/**
	 *
	 */
	private static final long serialVersionUID = 4161793826486881891L;

	@Override
	public int addCase(final List<Integer> decomposition, final int num) {
		// TODO Auto-generated method stub
		return 0;
	}

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
				if (v.indexOf("a") > -1 && v.indexOf("b") == -1 && v.indexOf("c") == -1 && y.indexOf("a") > -1
						&& y.indexOf("b") == -1 && y.indexOf("c") == -1) {
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
				return Lists.newArrayList(m - 1, 1, 0, 2);

			}

			@Override
			public boolean isCase(final String v, final String y) {
				if (v.indexOf("a") > -1 && v.indexOf("b") == -1 && v.indexOf("c") == -1 && y.indexOf("a") > -1
						&& y.indexOf("b") > -1 && y.indexOf("c") == -1) {
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
				return Lists.newArrayList(m, 1, 0, 1);
			}

			@Override
			public boolean isCase(final String v, final String y) {
				if (v.indexOf("a") > -1 && v.indexOf("b") == -1 && v.indexOf("c") == -1 && y.indexOf("a") == -1
						&& y.indexOf("b") > -1 && y.indexOf("c") == -1) {
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
				return Lists.newArrayList(m, 2, 0, 1);
			}

			@Override
			public boolean isCase(final String v, final String y) {
				if (v.indexOf("a") > -1 && v.indexOf("b") > -1 && v.indexOf("c") == -1 && y.indexOf("a") == -1
						&& y.indexOf("b") > -1 && y.indexOf("c") == -1) {
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
				return Lists.newArrayList(m + 1, 1, 0, 1);
			}

			@Override
			public boolean isCase(final String v, final String y) {
				if (v.indexOf("a") == -1 && v.indexOf("b") > -1 && v.indexOf("c") == -1 && y.indexOf("a") == -1
						&& y.indexOf("b") > -1 && y.indexOf("c") == -1) {
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
				return Lists.newArrayList(2 * m - 1, 1, 0, 2);
			}

			@Override
			public boolean isCase(final String v, final String y) {
				if (v.indexOf("a") == -1 && v.indexOf("b") > -1 && v.indexOf("c") == -1 && y.indexOf("a") == -1
						&& y.indexOf("b") > -1 && y.indexOf("c") > -1) {
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
				return Lists.newArrayList(2 * m, 1, 0, 1);
			}

			@Override
			public boolean isCase(final String v, final String y) {
				if (v.indexOf("a") == -1 && v.indexOf("b") > -1 && v.indexOf("c") == -1 && y.indexOf("a") == -1
						&& y.indexOf("b") == -1 && y.indexOf("c") > -1) {
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
				return Lists.newArrayList(2 * m, 2, 0, 1);
			}

			@Override
			public boolean isCase(final String v, final String y) {
				if (v.indexOf("a") == -1 && v.indexOf("b") > -1 && v.indexOf("c") > -1 && y.indexOf("a") == -1
						&& y.indexOf("b") == -1 && y.indexOf("c") > -1) {
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
				return Lists.newArrayList(2 * m + 1, 1, 0, 1);
			}

			@Override
			public boolean isCase(final String v, final String y) {
				if (v.indexOf("a") == -1 && v.indexOf("b") == -1 && v.indexOf("c") > -1 && y.indexOf("a") == -1
						&& y.indexOf("b") == -1 && y.indexOf("c") > -1) {
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
		int a, b, c;
		a = LemmaMath.countInstances(w, 'a');
		b = LemmaMath.countInstances(w, 'b');
		c = LemmaMath.countInstances(w, 'c');
		if (a > b + 1 && a > c + 1) {
			setDecomposition(Lists.newArrayList(0, 1, 0, 0));
		} else {
			super.chooseDecomposition();
		}

	}

	@Override
	public void chooseI() {
		if (getV().indexOf("a") == -1 && getY().indexOf("a") == -1) {
			i = 2;
		} else {
			i = 0;
		}
	}

	@Override
	protected void chooseW() {
		w = pumpString("a", getM() + 1) + pumpString("b", getM()) + pumpString("c", getM());
	}

	@Override
	public String getHTMLTitle() {
		return "<i>a<sup>i</sup>b<sup>j</sup>c<sup>k</sup></i> : <i>i</i> " + GREATER_THAN + " <i>j</i>, <i>i</i> "
				+ GREATER_THAN + " <i>k</i>";
	}

	@Override
	public String getTitle() {
		return "a^i b^j c^k : i > j, i > k";
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

		if (a > b && a > c) {
			return true;
		}
		return false;
	}

	@Override
	public boolean replaceCase(final List<Integer> decomposition, final int num, final int index) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean setDecomposition(final List<Integer> decomposition) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setDescription() {
		partitionIsValid = false;
		explanation = "For any <i>m</i> value, a possible value for <i>w</i> is \"a<sup><i>m</i>+1</sup>"
				+ "b<sup><i>m</i></sup>c<sup><i>m</i></sup>\".  The <i>v</i> and <i>y</i> values together "
				+ "thus would have a maximum of two unique letters.  Any possible <i>v</i> or <i>y</i> values "
				+ "would then be problematic if <i>i</i> = 0, <i>i</i> = 2, or perhaps both.  Thus, this language "
				+ "is not context-free.";
	}

	@Override
	protected void setRange() {
		myRange = Lists.newArrayList(3, 7);

	}

}
