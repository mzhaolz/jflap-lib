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
 * The regular pumping lemma for <i>L</i> = {<i>w</i> &#8712; {<i>a</i>,
 * <i>b</i>}* : <i>n<sub>a</sub></i> (<i>w</i>) &#60; <i>n<sub>b</sub></i>
 * (<i>w</i>)}.
 *
 * @author Jinghui Lim & Chris Morgan
 */
public class NaNb extends RegularPumpingLemma {
    /**
     *
     */
    private static final long serialVersionUID = 1706121984502776649L;

    @Override
    public String getHTMLTitle() {
        return "<i>w</i> " + ELEMENT_OF + " " + AB_STAR + " : <i>n<sub>a</sub></i> (<i>w</i>) "
                + LESS_THAN + " <i>n<sub>b</sub></i> (<i>w</i>)";
    }

    @Override
    public String getTitle() {
        return "w element_of {ab}* : na(w) < nb(w)";
    }

    @Override
    public void setDescription() {
        partitionIsValid = false;
        explanation = "For any <i>m</i> value, a possible value for <i>w</i> is \"a<sup><i>m</i></sup>"
                + "b<sup><i>m</i>+1</sup>\".  The <i>y</i> value thus would be a multiple of \"a\".  "
                + "For any <i>i</i> " + GREATER_THAN + " 1, n<sub>a</sub> " + GREATER_OR_EQ
                + " n<sub>b</sub>, "
                + "giving a string which is not in the language.  Thus, the language is not regular.";
    }

    @Override
    protected void chooseW() {
        w = pumpString("a", getM()) + pumpString("b", getM() + 1);
    }

  @Override
  public void chooseDecomposition() {
    setDecomposition(Lists.newArrayList(Math.min(m - 1, w.indexOf('b')), 1));

  }

    @Override
    public void chooseI() {
        i = 2;
    }

  @Override
  protected void setRange() {
    myRange = Lists.newArrayList(2, 17);

  }

    @Override
    public boolean isInLang(String s) {
        int a, b;
        char[] list = new char[] { 'a', 'b' };
        if (LemmaMath.otherCharactersFound(s, list)) {
            return false;
        }

        a = LemmaMath.countInstances(s, 'a');
        b = LemmaMath.countInstances(s, 'b');
        if (a < b) {
            return true;
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
