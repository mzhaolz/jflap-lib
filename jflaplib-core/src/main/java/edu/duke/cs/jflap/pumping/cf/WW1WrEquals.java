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

import edu.duke.cs.jflap.pumping.ContextFreePumpingLemma;
import edu.duke.cs.jflap.pumping.LemmaMath;

import com.google.common.collect.Lists;

/**
 * The context-free pumping lemma for <i>L</i> =
 * {<i>ww<sub>1</sub>w<sup>R</sup></i> : |<i>w</i>| = |<i>w<sub>1</sub></i>|,
 * <i>w</i> & <i>w<sub>1</sub></i> &#8712; {<i>a</i>, <i>b</i>}*}.
 *
 * @author Chris Morgan
 */
public class WW1WrEquals extends ContextFreePumpingLemma {

    /**
     *
     */
    private static final long serialVersionUID = 9179931471363918962L;

    @Override
    public String getTitle() {
        return "w w1 w^R : |w| = |w1|, w & w1 element_of {ab}*";
    }

    @Override
    public String getHTMLTitle() {
        return "<i>ww<sub>1</sub>w<sup>R</sup></i> : |<i>w</i>| = |<i>w<sub>1</sub></i>| "
                + ", <i>w</i> & <i>w<sub>1</sub></i> " + ELEMENT_OF + " " + AB_STAR;
    }

    @Override
    public void setDescription() {
        partitionIsValid = false;
        explanation = "For any <i>m</i> value, a possible value for <i>w</i> is \"a<sup><i>m</i></sup>"
                + "b<sup><i>m</i></sup>a<sup><i>m</i></sup>\".  To be in the language with "
                + "this example, <i>v</i> & <i>y</i> together cannot possess substrings that are from both 'w' "
                + "and 'w<sup>R</sup>'.  Thus, pumping a substring from either 'w', 'w<sup>1</sup>', "
                + " or 'w<sup>R</sup>' will violate the |'w'| = |'w<sup>R</sup>'| equality or cause |'w'| "
                + NOT_EQUAL + "|'w<sup>1</sup>'|.    Thus, this language is not context-free.";
    }

    @Override
    protected void addCases() {
        // TODO Auto-generated method stub

    }

    @Override
    public void chooseI() {
        i = LemmaMath.flipCoin();
    }

    @Override
    public void chooseDecomposition() {
        setDecomposition(Lists.newArrayList(0, 1, 0, 0));

    }

    @Override
    protected void chooseW() {
        w = pumpString("a", m) + pumpString("b", m) + pumpString("a", m);
    }

    @Override
    protected void setRange() {
        myRange = Lists.newArrayList(3, 10);

    }

    @Override
    public boolean isInLang(String s) {
        char[] list = new char[] { 'a', 'b' };
        if (LemmaMath.otherCharactersFound(s, list) || s.length() == 0) {
            return false;
        }

        int front, end;
        front = 0;
        end = s.length() - 1;
        while (s.charAt(front) == s.charAt(end) && front < end) {
            front++;
            end--;
            if (front == end - front + 1) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean setDecomposition(int[] decomposition) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public int addCase(int[] decomposition, int num) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean replaceCase(int[] decomposition, int num, int index) {
        // TODO Auto-generated method stub
        return false;
    }
}
