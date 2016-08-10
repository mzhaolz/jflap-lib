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

package edu.duke.cs.jflap.gui.sim;

import static com.google.common.base.Preconditions.checkArgument;

import edu.duke.cs.jflap.automata.AutomatonSimulator;
import edu.duke.cs.jflap.automata.Configuration;
import edu.duke.cs.jflap.automata.turing.TMConfiguration;
import edu.duke.cs.jflap.automata.turing.TMSimulator;
import edu.duke.cs.jflap.automata.turing.TMState;
import edu.duke.cs.jflap.automata.turing.TuringMachine;
import edu.duke.cs.jflap.gui.viewer.SelectionDrawer;

import com.google.common.collect.Lists;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.JOptionPane;
import javax.swing.JSplitPane;

/**
 * This is an intermediary object between the simulator GUI and the automaton
 * simulators.
 *
 * @author Thomas Finley
 */
public class ConfigurationController implements ConfigurationSelectionListener {
    /**
     * Instantiates a new configuration controller.
     *
     * @param pane
     *            the pane from which we retrieve configurations
     * @param simulator
     *            the automaton simulator
     * @param drawer
     *            the drawer of the automaton
     * @param component
     *            the component in which the automaton is displayed
     */
    public ConfigurationController(ConfigurationPane pane,
            AutomatonSimulator simulator,
            SelectionDrawer drawer,
            Component component) {
        configurations = pane;
        this.simulator = simulator;
        this.drawer = drawer;
        this.component = component;
        changeSelection();
        configurations.addSelectionListener(this);
        originalConfigurations = Lists.newArrayList(configurations.getConfigurations());
        // for(int k = 0; k < originalConfigurations.length; k++){
        // Configuration current = originalConfigurations[k];
        // if(current instanceof TMConfiguration){
        // TMConfiguration currentTM = (TMConfiguration)current;
        // originalConfigurations[k] = (Configuration)currentTM.clone();
        // }
        // }
    }

    /**
     * This sets the configuration pane to have the initial configuration for
     * this input.
     */
    public void reset() {
        configurations.clear();
        if (simulator instanceof TMSimulator) {
            TMSimulator tmSim = (TMSimulator) simulator;
            List<Configuration> configs = tmSim.getInitialConfigurations(tmSim.getInputStrings());
            for (Configuration config : configs) {
                configurations.add(config);
            }
        } else {
            for (int i = 0; i < originalConfigurations.size(); i++) {
                originalConfigurations.get(i).reset();
                configurations.add(originalConfigurations.get(i));
            }
        }
        // What the devil do I have to do to get it to repaint?
        // configurations.invalidate();
        configurations.validate();
        configurations.repaint();

        // Change them darned selections.
        changeSelection();
    }

    /**
     * This method should be called when the simulator pane that this
     * configuration controller belongs to is removed from the environment. This
     * will remove all of the open configuration trace windows.
     */
    public void cleanup() {
        Collection<TraceWindow> windows = configurationToTraceWindow.values();
        Iterator<TraceWindow> it = windows.iterator();
        while (it.hasNext()) {
            it.next().dispose();
        }
        configurationToTraceWindow.clear();
    }

    /**
     * The step method takes all configurations from the configuration pane, and
     * replaces them with "successor" transitions.
     *
     * @param blockStep
     */
    public void step(boolean blockStep) {
        List<Configuration> configs = configurations.getValidConfigurations();
        List<Configuration> list = new ArrayList<>();
        HashSet<Configuration> reject = new HashSet<>();

        // Clear out old states.
        configurations.clearThawed();

        if (!blockStep) { // for ordinary automaton
            for (Configuration config : configs) {
                // System.out.println("HERE!");
                List<? extends Configuration> next = simulator.stepConfiguration(config);
                // MERLIN MERLIN MERLIN MERLIN MERLIN//
                if (next.size() == 0) { // crucial check for rejection
                    // System.out.println("Rejected");
                    reject.add(config);
                    list.add(config);
                } else {
                    list.addAll(next);
                }
            }
        } else {
            do {
                checkArgument(configs.size() == 1);
                checkArgument(configs.get(0) instanceof TMConfiguration);
                checkArgument(simulator instanceof TMSimulator);

                if (configs.size() == 0) {
                    break; // bit of a hack, but not much time to debug right
                    // now.
                }

                List<Configuration> next = ((TMSimulator) simulator)
                        .stepBlock((TMConfiguration) configs.get(0));
                // MERLIN MERLIN MERLIN MERLIN MERLIN//
                if (next.size() == 0) { // crucial check for rejection
                    // System.out.println("Rejected");
                    reject.add(configs.get(0));
                    list.add(configs.get(0));
                } else {
                    list.addAll(next);
                }

            } while (false);
        }

        // Replace them with the successors.
        Iterator<Configuration> it = list.iterator();
        while (it.hasNext()) {
            Configuration config = it.next();
            configurations.add(config);
            if (reject.contains(config)) {
                configurations.setReject(config);
            }
        }
        // What the devil do I have to do to get it to repaint?
        configurations.validate();
        configurations.repaint();

        // Change them darned selections.
        changeSelection();
        // Ready for the ugliest code in the whole world, ever?
        try {
            // I take this action without the knowledge or sanction of
            // my government...
            JSplitPane split = (JSplitPane) configurations.getParent().getParent().getParent()
                    .getParent();
            int loc = split.getDividerLocation();
            split.setDividerLocation(loc - 1);
            split.setDividerLocation(loc);
            // Yes! GridLayout doesn't display properly in a scroll
            // pane, but if the user "wiggled" the size a little it
            // displays correctly -- now the size is wiggled in code!
        } catch (Throwable e) {

        }

        // State current = null;
        // Iterator iter = list.iterator();
        // int count = 0;

        // MERLIN MERLIN MERLIN MERLIN MERLIN// //forgetting BlockStep for now
        //// if (blockStep) { //should ONLY apply to Turing Machines // while
        // (iter.hasNext()) {
        //// Configuration configure = (Configuration) iter.next();
        //// current = configure.getCurrentState();
        //// if (configure.getBlockStack().size() > 0) {
        //// if(((Automaton)configure.getAutoStack().peek()).getInitialState()
        // != current || configure.getBlockStack().size()>1){
        //// if(!configure.isAccept()){
        //// count++;
        //// if(count > 10000){
        //// int result = JOptionPane.showConfirmDialog(null, "JFLAP has
        // generated 10000 configurations. Continue?");
        //// switch (result) {
        //// case JOptionPane.CANCEL_OPTION:
        //// case JOptionPane.NO_OPTION:
        //// return;
        //// default:
        //// }
        //// }
        // step(blockStep);
        //// }
        //// break;
        //// }
        //// }
        //// }
        // }
    }

    /**
     * Freezes selected configurations.
     */
    public void freeze() {
        Set<Configuration> configs = configurations.getSelected();
        if (configs.size() == 0) {
            JOptionPane.showMessageDialog(configurations, NO_CONFIGURATION_ERROR,
                    NO_CONFIGURATION_ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
            return;
        }
        for (Configuration config : configs) {
            configurations.setFrozen(config);
        }
        configurations.deselectAll();
        configurations.repaint();
    }

    /**
     * Removes the selected configurations.
     */
    public void remove() {
        Set<Configuration> configs = configurations.getSelected();
        if (configs.size() == 0) {
            JOptionPane.showMessageDialog(configurations, NO_CONFIGURATION_ERROR,
                    NO_CONFIGURATION_ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
            return;
        }
        for (Configuration config : configs) {
            configurations.remove(config);
        }
        configurations.validate();
        configurations.repaint();
    }

    /**
     * Zooms in on selected configuration
     */
    public void focus() {
        List<Configuration> configs = Lists.newArrayList(configurations.getSelected());
        if (configs.size() == 0) {
            JOptionPane.showMessageDialog(configurations, NO_CONFIGURATION_ERROR,
                    NO_CONFIGURATION_ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
            return;
        } else if (configs.size() > 1) {
            JOptionPane.showMessageDialog(configurations, FOCUS_CONFIGURATION_ERROR,
                    FOCUS_CONFIGURATION_ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
            return;
        }

        Configuration toFocus = configs.get(0);
        configurations.setFocused(toFocus);
        toFocus.setFocused(true);

        // State block = toFocus.getCurrentState().getParentBlock();
        // State parent = block;
        // Automaton checkTop = null;
        // Stack heirarchy = new Stack();
        // if (parent != null) {
        // checkTop = findAutomaton(checkTop, heirarchy, parent, false);
        // //System.out.println("Call from focus");
        // if (checkTop != null)
        // drawer.setAutomaton(checkTop);
        // }
        // drawer.invalidate();
        component.repaint();
    }

    /*
     * public Automaton findAutomaton(Automaton checkTop, Stack heirarchy, State
     * parent, boolean select) { Configuration[] configs; configs =
     * configurations.getConfigurations(); for (int k = 0; k < configs.length;
     * k++) { if (configs[k].getFocused()) { Automaton temp = (Automaton)
     * configs[k].getAutoStack().peek(); return temp; } } while (parent != null)
     * { if (select) drawer.addSelected(parent); checkTop = (Automaton)
     * simulator.getAutomaton().getBlockMap().get( parent.getInternalName()); if
     * (checkTop != null) { while (!heirarchy.isEmpty()) { State popped =
     * (State) heirarchy.pop(); checkTop = (Automaton)
     * checkTop.getBlockMap().get( popped.getInternalName()); } break; } else
     * heirarchy.push(parent); parent = parent.getParentBlock(); } return
     * checkTop; }
     */

    /**
     * Sets the drawer to draw the selected configurations' states as selected,
     * or to draw all configurations' states as selected in the event that there
     * are no selected configurations. In this case, the selection refers to the
     * selection of states within the automata, though
     */
    public void changeSelection() {
        drawer.clearSelected();
        Set<Configuration> configs;
        configs = configurations.getConfigurations();
        boolean foundFocused = false;
        for (Configuration current : configs) {
            foundFocused = setFocusIfNeeded(current, foundFocused);

            if (current instanceof TMConfiguration) {
                // then blocks become relevant
                TMState cur = (TMState) current.getCurrentState();
                while (((TuringMachine) cur.getAutomaton()).getParent() != null) {
                    cur = ((TuringMachine) cur.getAutomaton()).getParent();
                }
                drawer.addSelected(cur);
            }

            // Stack blocks = (Stack) configs[i].getBlockStack().clone();
            // if (!blocks.empty()) {
            // State parent = (State) configs[i].getBlockStack().peek();
            // int start = blocks.lastIndexOf(parent);
            // while (start >= 0) {
            // parent = (State) blocks.get(start);
            // drawer.addSelected(parent);
            // start--;
            // }
            // }
            drawer.addSelected(current.getCurrentState());
        }
        component.repaint();
    }

    private boolean setFocusIfNeeded(Configuration current, boolean foundFocused) {
        Configuration parentConfig = current.getParent();
        if (parentConfig == null) {
            return foundFocused;
        }

        if (parentConfig.getFocused()) {
            current.setFocused(true);
            if (!foundFocused) {
                configurations.setFocused(current);
                current.setFocused(true);

                // MERLIN MERLIN MERLIN MERLIN MERLIN// //not sure what might
                // have been broken here
                // Automaton setWith = (Automaton)
                // current.getAutoStack().peek();
                // System.out.println("Stack size "
                // + current.getAutoStack().size());
                // drawer.setAutomaton(setWith);
                foundFocused = true;
            }
        }
        return foundFocused;
    }

    public void defocus() {
        Set<Configuration> configs = configurations.getConfigurations();
        for (Configuration config : configs) {
            if (config.getFocused()) {
                configurations.defocus(config);
            }
        }
        drawer.setAutomaton(simulator.getAutomaton());
        drawer.invalidate();
        component.repaint();
    }

    /**
     * Thaws the selected configurations.
     */
    public void thaw() {
        Set<Configuration> configs = configurations.getSelected();
        if (configs.size() == 0) {
            JOptionPane.showMessageDialog(configurations, NO_CONFIGURATION_ERROR,
                    NO_CONFIGURATION_ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
            return;
        }
        for (Configuration config : configs) {
            configurations.setNormal(config);
        }
        configurations.deselectAll();
        configurations.repaint();
    }

    /**
     * Given the selected configurations, shows their "trace."
     */
    public void trace() {
        Set<Configuration> configs = configurations.getSelected();
        if (configs.size() == 0) {
            JOptionPane.showMessageDialog(configurations, NO_CONFIGURATION_ERROR,
                    NO_CONFIGURATION_ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
            return;
        }
        for (Configuration config : configs) {
            TraceWindow window = configurationToTraceWindow.get(config);
            if (window == null) {
                configurationToTraceWindow.put(config, new TraceWindow(config));
            } else {
                window.setVisible(true);
                window.toFront();
            }
        }
    }

    /**
     * This method is used to find out if we need the <b>Focus</b> and
     * <b>Defocus</b> buttons in the simulator.
     *
     * @return <code>true</code> if the automaton is a turing machine,
     *         <code>false</code> otherwise
     * @author Jinghui Lim
     */
    public boolean isTuringMachine() {
        /*
         * Sorry about this pretty cheap method.
         */
        return simulator instanceof TMSimulator;
    }

    /**
     * Listens for configuration selection events.
     *
     * @param event
     *            the selection event
     */
    @Override
    public void configurationSelectionChange(ConfigurationSelectionEvent event) {
        // changeSelection();
    }

    /** This is the pane holding the configurations. */
    private ConfigurationPane configurations;

    /** This is the simulator that we step through configurations with. */
    private AutomatonSimulator simulator;

    /** This is the selection drawer that draws the automaton. */
    private SelectionDrawer drawer;

    /** This is the pane in which the automaton is displayed. */
    private Component component;

    /**
     * The mapping of a particular configuration to a trace window. If there is
     * no trace window for that configuration, then that trace window no longer
     * exists.
     */
    private HashMap<Configuration, TraceWindow> configurationToTraceWindow = new HashMap<>();

    /**
     * This is the set of original configurations when the configuration pane
     * started.
     */
    private List<Configuration> originalConfigurations = new ArrayList<>();

    /** The error message displayed when there is no config selected. */
    private static final String NO_CONFIGURATION_ERROR = "Select at least one configuration!";

    /** The error message displayed when there is no config selected. */
    private static final String NO_CONFIGURATION_ERROR_TITLE = "No Configuration Selected";

    /** The error message displayed when there is no config selected. */
    private static final String FOCUS_CONFIGURATION_ERROR = "JFLAP can only focus on one configuration at a time!";

    /** The error message displayed when there is no config selected. */
    private static final String FOCUS_CONFIGURATION_ERROR_TITLE = "Too many configurations selected";
}
