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

import edu.duke.cs.jflap.pumping.LemmaMath;
import edu.duke.cs.jflap.pumping.RegularPumpingLemma;

import com.google.common.collect.Lists;

/**
 * The regular pumping lemma for <i>L</i> = {<i>a<sup>n</sup></i> : <i>n</i> is
 * even}.
 *
 * @author Jinghui Lim & Chris Morgan
 */
public class AnEven extends RegularPumpingLemma {
    /**
     *
     */
    private static final long serialVersionUID = 6813155572111784226L;

    @Override
    public String getTitle() {
        return "a^n : n is even";
    }

    @Override
    public String getHTMLTitle() {
        return "<i>a<sup>n</sup></i> : <i>n</i> is even";
    }

    @Override
    public void setDescription() {
        partitionIsValid = true;
        explanation = "Because this is a regular language, a valid decomposition exists.  If <i>m</i> "
                + GREATER_OR_EQ + " 2, " + "the <i>y</i> value \"aa\" will always pump the string.";
    }

    @Override
    protected void chooseW() {
        if (getM() % 2 == 0) {
            w = pumpString("a", getM());
        } else {
            w = pumpString("a", getM() + 1);
        }
    }

    @Override
    public void chooseI() {
        i = LemmaMath.flipCoin();
    }

    @Override
    protected void setRange() {
        myRange = Lists.newArrayList(2, 18);
    }

    @Override
    public void chooseDecomposition() {
        setDecomposition(Lists.newArrayList(0, 2));
    }

    /**
     * Checks if the pumped string is in the language.
     *
     * @return <code>true</code> if it is, <code>false</code> otherwise
     */
    @Override
    public boolean isInLang(String s) {
        String temp;
        char[] list = new char[] { 'a' };
        if (LemmaMath.otherCharactersFound(s, list)) {
            return false;
        }

        if (s.length() == 0) {
            temp = createPumpedString();
        } else {
            temp = s;
        }
        return temp.length() % 2 == 0;
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
