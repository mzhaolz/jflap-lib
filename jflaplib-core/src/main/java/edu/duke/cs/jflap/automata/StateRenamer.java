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

package edu.duke.cs.jflap.automata;

import com.google.common.collect.Lists;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * This class contains operations to contain that all states have numeric IDs
 * from 0 up. This will change the ID numbers of the states.
 *
 * @author Thomas Finley
 */
public class StateRenamer {
    /**
     * Renames the states for an automaton, by changing all the ID numbers so
     * that all the ID numbers go from 0 up without interruption in the numeric
     * sequence. This will modify the automaton passed in.
     *
     * @param a
     *            the automaton to change the IDs of the states
     */
    public static void rename(Automaton a) {
        List<State> s = a.getStates();
        int maxId = s.size() - 1;
        Set<Integer> untaken = new HashSet<>();
        Set<State> reassign = new HashSet<>(s);
        for (int i = 0; i <= maxId; i++) {
            untaken.add(new Integer(i));
        }
        for (int i = 0; i < s.size(); i++) {
            if (untaken.remove(new Integer(s.get(i).getID()))) {
                reassign.remove(s.get(i));
            }
        }
        // Now untaken has the untaken IDs, and reassign has the
        // states that need reassigning.
        s = Lists.newArrayList(reassign);
        Iterator<Integer> it = untaken.iterator();
        for (int i = 0; i < s.size(); i++) {
            s.get(i).setID(it.next().intValue());
        }
    }
}
