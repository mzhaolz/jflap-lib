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

import edu.duke.cs.jflap.pumping.Case;
import edu.duke.cs.jflap.pumping.ContextFreePumpingLemma;
import edu.duke.cs.jflap.pumping.LemmaMath;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * The context-free pumping lemam for <i>L</i> =
 * {<i>a<sup>n</sup>b<sup>n</sup></i> : <i>n</i> &#8805; 0}.
 *
 * @author Jinghui Lim & Chris Morgan
 *
 */
public class AnBn extends ContextFreePumpingLemma {
    /**
     *
     */
    private static final long serialVersionUID = -2621561938235244043L;

    @Override
    public String getTitle() {
        return "a^n b^n : n >= 0";
    }

    @Override
    public String getHTMLTitle() {
        return "<i>a<sup>n</sup>b<sup>n</sup></i> : <i>n</i> " + GREATER_OR_EQ + " 0";
    }

    @Override
    protected void chooseW() {
        w = pumpString("a", m) + pumpString("b", m);
    }

    @Override
    public void setDescription() {
        partitionIsValid = true;
        explanation = "Because this is a context-free language, a valid decomposition exists.  If <i>m</i> "
                + GREATER_OR_EQ
                + " 2, one could choose <i>v</i> to be \"a\" and <i>y</i> to be \"b\", which will work for all values of "
                + "<i>i</i>.";
    }

    @Override
    public void chooseI() {
        i = 2;
    }

    @Override
    protected void addCases() {
        /*
         * v is string of a's and y is string of a's
         */
        myAllCases.add(new Case() {
            @Override
            public boolean isCase(String v, String y) {
                if (v.indexOf("a") > -1 && v.indexOf("b") == -1 && y.indexOf("a") > -1
                        && y.indexOf("b") == -1) {
                    return true;
                }
                return false;
            }

            @Override
            public String description() {
                return "v is a string of \"a\"s and y is a string of \"a\"s";
            }

            @Override
            public List<Integer> getPreset() {
                return Lists.newArrayList(0, 1, 0, 1);
            }
        });
        /*
         * v is string of a's and y is string of a's followed by b's
         */
        myAllCases.add(new Case() {

            @Override
            public boolean isCase(String v, String y) {
                if (v.indexOf("a") > -1 && v.indexOf("b") == -1 && y.indexOf("a") > -1
                        && y.indexOf("b") > -1) {
                    return true;
                }
                return false;
            }

            @Override
            public String description() {
                return "v is a string of \"a\"s and y is a string of \"a\"s followed by \"b\"s";
            }

            @Override
            public List<Integer> getPreset() {
                return Lists.newArrayList(m - 2, 1, 0, 2);
            }
        });
        /*
         * v is string of a's and y is string of b's
         */
        myAllCases.add(new Case() {
            @Override
            public boolean isCase(String v, String y) {
                if (v.indexOf("a") > -1 && v.indexOf("b") == -1 && y.indexOf("a") == -1
                        && y.indexOf("b") > -1) {
                    return true;
                }
                return false;
            }

            @Override
            public String description() {
                return "v is a string of \"a\"s and y is a string of \"b\"s";
            }

            @Override
            public List<Integer> getPreset() {
                return Lists.newArrayList(1, m - 1, 0, 1);
            }
        });
        /*
         * v is string of a's followed by b's and y is string of b's
         */
        myAllCases.add(new Case() {

            @Override
            public boolean isCase(String v, String y) {
                if (v.indexOf("a") > -1 && v.indexOf("b") > -1 && y.indexOf("a") == -1
                        && y.indexOf("b") > -1) {
                    return true;
                }
                return false;
            }

            @Override
            public String description() {
                return "v is a string of \"a\"s followed by \"b\"s and y is a string of \"b\"s";
            }

            @Override
            public List<Integer> getPreset() {
                return Lists.newArrayList(m - 1, 2, 0, 1);
            }
        });
        /*
         * v is a string of b's and y is a string of b's
         */
        myAllCases.add(new Case() {
            @Override
            public boolean isCase(String v, String y) {
                if (v.indexOf("a") == -1 && v.indexOf("b") > -1 && y.indexOf("a") == -1
                        && y.indexOf("b") > -1) {
                    return true;
                }
                return false;
            }

            @Override
            public String description() {
                return "v is a string of \"b\"s and y is a string of \"b\"s";
            }

            @Override
            public List<Integer> getPreset() {
                return Lists.newArrayList(m, 1, 0, 1);
            }
        });
        /*
         * v is an empty string and y is a non-empty string
         */
        myAllCases.add(new Case() {

            @Override
            public boolean isCase(String v, String y) {
                if (v.length() == 0 && y.length() > 0) {
                    return true;
                }
                return false;
            }

            @Override
            public String description() {
                return "v is an empty string and y is a non-empty string";
            }

            @Override
            public List<Integer> getPreset() {
                return Lists.newArrayList(m, 0, 1, 1);
            }
        });
        /*
         * v is a non-empty string and y is an empty string
         */
        myAllCases.add(new Case() {
            @Override
            public boolean isCase(String v, String y) {
                if (v.length() > 0 && y.length() == 0) {
                    return true;
                }
                return false;
            }

            @Override
            public String description() {
                return "v is a non-empty string and y is an empty string";
            }

            @Override
            public List<Integer> getPreset() {
                return Lists.newArrayList(m, 1, 0, 0);
            }

        });
    }

    @Override
    protected void setRange() {
        myRange = Lists.newArrayList(4, 11);

    }

    @Override
    public void chooseDecomposition() {
        // The last character in a^n and the first in b^n
        setDecomposition(Lists.newArrayList(w.length() / 2 - 1, 1, 0, 1));

    }

    /**
     * Checks if the pumped string is in the language.
     *
     * @return <code>true</code> if it is, <code>false</code> otherwise
     */
    @Override
    public boolean isInLang(String s) {
        int a, b;
        char[] list = new char[] { 'a', 'b' };
        if (LemmaMath.isMixture(s, list)) {
            return false;
        }

        a = LemmaMath.countInstances(s, 'a');
        b = LemmaMath.countInstances(s, 'b');
        if (a == b) {
            return true;
        }
        return false;
    }

}
