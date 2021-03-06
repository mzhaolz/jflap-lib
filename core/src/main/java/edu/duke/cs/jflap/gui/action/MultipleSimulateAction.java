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

package edu.duke.cs.jflap.gui.action;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.table.TableColumnModel;

import com.google.common.collect.Lists;

import edu.duke.cs.jflap.automata.Automaton;
import edu.duke.cs.jflap.automata.AutomatonSimulator;
import edu.duke.cs.jflap.automata.Configuration;
import edu.duke.cs.jflap.automata.NondeterminismDetector;
import edu.duke.cs.jflap.automata.NondeterminismDetectorFactory;
import edu.duke.cs.jflap.automata.SimulatorFactory;
import edu.duke.cs.jflap.automata.State;
import edu.duke.cs.jflap.automata.mealy.MealyConfiguration;
import edu.duke.cs.jflap.automata.mealy.MealyMachine;
import edu.duke.cs.jflap.automata.turing.TMSimulator;
import edu.duke.cs.jflap.automata.turing.TuringMachine;
import edu.duke.cs.jflap.grammar.Grammar;
import edu.duke.cs.jflap.gui.JTableExtender;
import edu.duke.cs.jflap.gui.SplitPaneFactory;
import edu.duke.cs.jflap.gui.TableTextSizeSlider;
import edu.duke.cs.jflap.gui.editor.ArrowDisplayOnlyTool;
import edu.duke.cs.jflap.gui.editor.EditorPane;
import edu.duke.cs.jflap.gui.environment.Environment;
import edu.duke.cs.jflap.gui.environment.EnvironmentFrame;
import edu.duke.cs.jflap.gui.environment.GrammarEnvironment;
import edu.duke.cs.jflap.gui.environment.Profile;
import edu.duke.cs.jflap.gui.environment.Universe;
import edu.duke.cs.jflap.gui.environment.tag.CriticalTag;
import edu.duke.cs.jflap.gui.grammar.GrammarInputPane;
import edu.duke.cs.jflap.gui.grammar.parse.BruteParsePane;
import edu.duke.cs.jflap.gui.sim.TraceWindow;
import edu.duke.cs.jflap.gui.sim.multiple.InputTableModel;
import edu.duke.cs.jflap.gui.viewer.AutomatonPane;

/**
 * This is the action used for the simulation of multiple inputs on an automaton
 * with no interaction. This method can operate on any automaton.
 *
 * @author Thomas Finley
 * @modified by Kyung Min (Jason) Lee
 */
public class MultipleSimulateAction extends NoInteractionSimulateAction {
	/**
	 * This auxillary class is convenient so that the help system can easily
	 * identify what type of component is active according to its class.
	 */
	public class MultiplePane extends JPanel {
		private static final long serialVersionUID = 99L;

		public JSplitPane mySplit = null;

		public MultiplePane(final JSplitPane split) {
			super(new BorderLayout());
			add(split, BorderLayout.CENTER);
			mySplit = split;
		}
	}

	private static final long serialVersionUID = 23L;

	private static List<String> RESULT = Lists.newArrayList("Accept", "Reject", "Cancelled");

	protected JTable table = null;

	protected JPanel myPanel = null;

	/**
	 * Instantiates a new <CODE>MultipleSimulateAction</CODE>.
	 *
	 * @param automaton
	 *            the automaton that input will be simulated on
	 * @param environment
	 *            the environment object that we shall add our simulator pane to
	 */
	public MultipleSimulateAction(final Automaton automaton, final Environment environment) {
		super(automaton, environment);
		putValue(NAME, "Multiple Run");
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_M, MAIN_MENU_MASK));
	}

	public MultipleSimulateAction(final Grammar gram, final Environment environment) {
		super(gram, environment);
		putValue(NAME, "Multiple Run");
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_M, MAIN_MENU_MASK));
	}

	/**
	 * Handles the creation of the multiple input pane.
	 *
	 * @param e
	 *            the action event
	 */
	@Override
	public void actionPerformed(final ActionEvent e) {
		performAction((Component) e.getSource());
	}

	/**
	 * Returns the title for the type of compontent we will add to the
	 * environment.
	 *
	 * @return in this base class, returns "Multiple Inputs"
	 */
	public String getComponentTitle() {
		return "Multiple Run";
	}

	public int getMachineIndexByName(final String machineFileName) {
		final List<Object> machines = getEnvironment().myObjects;
		if (machines == null) {
			return -1;
		}
		for (int k = 0; k < machines.size(); k++) {
			final Object current = machines.get(k);
			if (current instanceof Automaton) {
				final Automaton cur = (Automaton) current;
				if (cur.getFileName().equals(machineFileName)) {
					return k;
				}
			} else if (current instanceof Grammar) {
				final Grammar cur = (Grammar) current;
				if (cur.getFileName().equals(machineFileName)) {
					return k;
				}
			}
		}
		return -1;
	}

	private int getMachineIndexBySelectedRow(final JTable table) {
		final InputTableModel model = (InputTableModel) table.getModel();
		final int row = table.getSelectedRow();
		if (row < 0) {
			return -1;
		}
		final String machineFileName = model.getValueAt(row, 0);
		return getMachineIndexByName(machineFileName);
	}

	/**
	 * This will search configurations for an accepting configuration.
	 *
	 * @param automaton
	 *            the automaton input is simulated on
	 * @param simulator
	 *            the automaton simulator for this automaton
	 * @param configs
	 *            the initial configurations generated from a single input
	 * @param initialInput
	 *            the object that represents the initial input; this is a String
	 *            object in most cases, but for Turing Machines is an array of
	 *            String objects
	 * @param associatedConfigurations
	 *            the first accepting configuration encountered will be added to
	 *            this list, or the last configuration considered if there was
	 *            no accepted configuration
	 * @return <CODE>0</CODE> if this was an accept, <CODE>1</CODE> if reject,
	 *         and <CODE>2</CODE> if the user cancelled the run
	 */
	protected int handleInput(final Automaton automaton, final AutomatonSimulator simulator,
			List<Configuration> configs, final Object initialInput,
			final List<Configuration> associatedConfigurations) {
		final JFrame frame = Universe.frameForEnvironment(getEnvironment());
		// How many configurations have we had?
		int numberGenerated = 0;
		// When should the next warning be?
		int warningGenerated = WARNING_STEP;
		Configuration lastConsidered = configs.get(configs.size() - 1);
		while (configs.size() > 0) {
			numberGenerated += configs.size();
			// Make sure we should continue.
			if (numberGenerated >= warningGenerated) {
				if (!confirmContinue(numberGenerated, frame)) {
					associatedConfigurations.add(lastConsidered);
					return 2;
				}
				while (numberGenerated >= warningGenerated) {
					warningGenerated *= 2;
				}
			}
			// Get the next batch of configurations.
			final List<Configuration> next = new ArrayList<>();
			for (int i = 0; i < configs.size(); i++) {
				lastConsidered = configs.get(i);
				if (configs.get(i).isAccept()) {
					associatedConfigurations.add(configs.get(i));
					return 0;
				} else {
					next.addAll(simulator.stepConfiguration(configs.get(i)));
				}
			}
			configs = next;
		}
		associatedConfigurations.add(lastConsidered);
		return 1;
	}

	/**
	 * Provides an initialized multiple input table object.
	 *
	 * @param obj
	 *            the automaton to provide the multiple input table for
	 * @return a table object for this automaton
	 * @see edu.duke.cs.jflap.gui.sim.multiple.InputTableModel
	 */
	protected JTableExtender initializeTable(final Object obj) {
		// System.out.println("In regular multiple initialize");
		boolean multiple = false;
		int inputCount = 0;
		if (getEnvironment().myObjects != null) {
			multiple = true;
			inputCount = 1;
		}
		// System.out.println("In initialize:" + multiple);
		InputTableModel model = null;
		if (getObject() instanceof Automaton) {
			model = InputTableModel.getModel((Automaton) getObject(), multiple);
		} else if (getObject() instanceof Grammar) {
			model = InputTableModel.getModel((Grammar) getObject(), multiple);
		}
		final JTableExtender table = new JTableExtender(model, this);
		// In this regular multiple simulate pane, we don't care about
		// the outputs, so get rid of them.
		final TableColumnModel tcmodel = table.getColumnModel();

		inputCount += model.getInputCount();
		for (int i = model.getInputCount(); i > 0; i--) {
			tcmodel.removeColumn(tcmodel.getColumn(inputCount));
		}
		if (multiple) {
			final List<Object> autos = getEnvironment().myObjects;
			// System.out.println("In initialize: " + autos.size());
			final List<String> strings = getEnvironment().myTestStrings;
			final int offset = strings.size();
			int row = 0;
			for (int m = 0; m < autos.size(); m++) {
				for (int k = 0; k < strings.size(); k++) {
					row = k + offset * m;
					final Object currentObj = autos.get(m);
					if (currentObj instanceof Automaton) {
						model.setValueAt(((Automaton) currentObj).getFileName(), row, 0);
						model.setValueAt(strings.get(k), row, 1);
					} else if (currentObj instanceof Grammar) {
						model.setValueAt(((Grammar) currentObj).getFileName(), row, 0);
						model.setValueAt(strings.get(k), row, 1);
					}
				}
			}
			while ((model.getRowCount() - 1) > (autos.size() * strings.size())) {
				model.deleteRow(model.getRowCount() - 2);
			}
		}
		// Set up the last graphical parameters.
		table.setShowGrid(true);
		table.setGridColor(Color.lightGray);
		return table;
	}

	public void performAction(final Component source) {
		if (getObject() instanceof Automaton) {
			if (((Automaton) getObject()).getInitialState() == null) {
				JOptionPane.showMessageDialog(source, "Simulation requires an automaton\n" + "with an initial state!",
						"No Initial State", JOptionPane.ERROR_MESSAGE);
				return;
			}
		}
		/*
		 * If it is a Mealy or Moore machine, it cannot have nondeterminism.
		 */
		if (getObject() instanceof MealyMachine) {
			final Automaton a = (Automaton) getObject();
			final NondeterminismDetector d = NondeterminismDetectorFactory.getDetector(a);
			final List<State> nd = d.getNondeterministicStates(a);
			if (nd.size() > 0) {
				JOptionPane.showMessageDialog(source, "Please remove nondeterminism for simulation.\n"
						+ "Select menu item Test : Highlight Nondeterminism\n" + "to see nondeterministic states.",
						"Nondeterministic states detected", JOptionPane.ERROR_MESSAGE);
				return;
			}
		}

		// System.out.println("In performAction");
		table = initializeTable(getObject());
		// if(table==null){
		// System.out.println("table null");
		// table = newTable;
		// }
		// System.out.println((((InputTableModel)newTable.getModel()).isMultiple));
		// if(((InputTableModel)table.getModel()).isMultiple !=
		// (((InputTableModel)newTable.getModel()).isMultiple)){
		// System.out.println("got here");
		// table = newTable;
		// }

		if (((InputTableModel) table.getModel()).isMultiple) {
			getEnvironment().remove(getEnvironment().getActive());
		}

		final JPanel panel = new JPanel(new BorderLayout());
		final JToolBar bar = new JToolBar();
		panel.add(new JScrollPane(table), BorderLayout.CENTER);
		panel.add(bar, BorderLayout.SOUTH);
		panel.add(new TableTextSizeSlider(table), BorderLayout.NORTH);

		// Load inputs
		bar.add(new AbstractAction("Load Inputs") {
			private static final long serialVersionUID = 71L;

			@Override
			public void actionPerformed(final ActionEvent e) {
				// TODO Auto-generated method stub
				try {
					// Make sure any recent changes are registered.
					table.getCellEditor().stopCellEditing();
				} catch (final NullPointerException exception) {
					// We weren't editing anything, so we're OK.
				}
				final InputTableModel model = (InputTableModel) table.getModel();
				final JFileChooser ourChooser = new JFileChooser(System.getProperties().getProperty("user.dir"));
				final int retval = ourChooser.showOpenDialog(null);
				File f = null;
				if (retval == JFileChooser.APPROVE_OPTION) {
					f = ourChooser.getSelectedFile();
					try (Scanner sc = new Scanner(f)) {
						int last = model.getRowCount() - 1;
						/*
						 * int tapes = 0; if(getObject() instanceof Automaton) {
						 * Automaton currentAuto = (Automaton)getObject(); if
						 * (currentAuto instanceof TuringMachine) { tapes =
						 * ((TuringMachine)currentAuto).tapes; } } if (tapes==0)
						 * tapes++;
						 */
						while (sc.hasNext()) {
							// System.out.println(temp);
							final String temp = sc.next();
							model.setValueAt(temp, last, 0);
							last++;
						}
					} catch (final FileNotFoundException e1) {
						// TODO Auto-generate catch block
						e1.printStackTrace();
					}
				}
			}
		});
		// Add the running input thing.
		bar.add(new AbstractAction("Run Inputs") {
			private static final long serialVersionUID = 72L;

			@Override
			public void actionPerformed(final ActionEvent e) {
				try {
					// Make sure any recent changes are registered.
					table.getCellEditor().stopCellEditing();
				} catch (final NullPointerException exception) {
					// We weren't editing anything, so we're OK.
				}
				final InputTableModel model = (InputTableModel) table.getModel();

				if (getObject() instanceof Automaton) {
					Automaton currentAuto = (Automaton) getObject();
					AutomatonSimulator simulator = SimulatorFactory.getSimulator(currentAuto);
					final String[][] inputs = model.getInputs();
					int uniqueInputs = inputs.length;
					int tapes = 1;
					if (model.isMultiple) {
						if (currentAuto instanceof TuringMachine) {
							tapes = ((TuringMachine) currentAuto).tapes;
						}
						uniqueInputs = getEnvironment().myTestStrings.size() / tapes;
					}
					for (int r = 0; r < inputs.length; r++) {
						if (r > 0) {
							if (r % uniqueInputs == 0) {
								currentAuto = (Automaton) getEnvironment().myObjects.get(r / uniqueInputs);

								simulator = SimulatorFactory.getSimulator(currentAuto);
							}
						}
						List<Configuration> configs = null;
						Object input = null;
						// Is this a Turing machine?
						if (currentAuto instanceof TuringMachine) {

							configs = ((TMSimulator) simulator).getInitialConfigurations(Arrays.asList(inputs[r]));
							input = inputs[r];
						} else { // If it's not a Turing machine.
							configs = simulator.getInitialConfigurations(inputs[r][0]);
							input = inputs[r][0];
						}
						final List<Configuration> associated = new ArrayList<>();
						final int result = handleInput(currentAuto, simulator, configs, input, associated);
						Configuration c = null;
						if (associated.size() != 0) {
							c = associated.get(0);
						}

						/*
						 * If it's a Moore or Mealy machine, the output should
						 * be the string not accept/reject.
						 */
						// MERLIN MERLIN MERLIN MERLIN MERLIN//
						if (getObject() instanceof MealyMachine) {
							final MealyConfiguration con = (MealyConfiguration) c;
							model.setResult(r, con.getOutput(), con, getEnvironment().myTransducerStrings,
									(r % (uniqueInputs)) * (tapes + 1));
						} else {
							model.setResult(r, RESULT.get(result), c, getEnvironment().myTransducerStrings,
									(r % (uniqueInputs)) * (tapes + 1));
						}
					}
				} else if (getObject() instanceof Grammar) {
					model.getInputs();
					final Grammar currentGram = (Grammar) getObject();
					final BruteParsePane parsePane = new BruteParsePane((GrammarEnvironment) getEnvironment(),
							currentGram, model);
					parsePane.inputField.setEditable(false);
					parsePane.row = -1;
					parsePane.parseMultiple();
					// while(parsePane.pauseResumeAction.isEnabled()){
					// timer.start();
					// while(timer.isRunning()){
					// //wait
					// }

					// }
					// if(parsePane.stepAction.isEnabled()) model.setResult(r,
					// "Accept", null, getEnvironment().myTransducerStrings,
					// (r%uniqueInputs));

				}
			}
		});
		if (!((InputTableModel) table.getModel()).isMultiple) {
			// Add the clear button.
			bar.add(new AbstractAction("Clear") {
				private static final long serialVersionUID = 73L;

				@Override
				public void actionPerformed(final ActionEvent e) {
					try {
						// Make sure any recent changes are registered.
						table.getCellEditor().stopCellEditing();
					} catch (final NullPointerException exception) {
						// We weren't editing anything, so we're OK.
					}
					final InputTableModel model = (InputTableModel) table.getModel();
					model.clear();
				}
			});

			/*
			 * So that it will show up as Lambday or Epsilon, depending on the
			 * profile. Sorry about the cheap hack.
			 *
			 * Jinghui Lim
			 */
			String empty = "Lambda";
			if (Universe.curProfile.getEmptyString().equals(Profile.LAMBDA)) {
				empty = "Lambda";
			} else if (Universe.curProfile.getEmptyString().equals(Profile.EPSILON)) {
				empty = "Epsilon";
			}
			bar.add(new AbstractAction("Enter " + empty /* "Enter Lambda" */) {
				private static final long serialVersionUID = 74L;

				@Override
				public void actionPerformed(final ActionEvent e) {
					final int row = table.getSelectedRow();
					if (row == -1) {
						return;
					}
					for (int column = 0; column < table.getColumnCount() - 1; column++) {
						table.getModel().setValueAt("", row, column);
					}
				}
			});
		}
		if (getObject() instanceof Automaton) {
			bar.add(new AbstractAction("View Trace") {
				private static final long serialVersionUID = 75L;

				@Override
				public void actionPerformed(final ActionEvent e) {
					final int[] rows = table.getSelectedRows();
					final InputTableModel tm = (InputTableModel) table.getModel();
					final List<Integer> nonassociatedRows = new ArrayList<>();
					for (final int row : rows) {
						if (row == tm.getRowCount() - 1) {
							continue;
						}
						final Configuration c = tm.getAssociatedConfigurationForRow(row);
						if (c == null) {
							nonassociatedRows.add(new Integer(row + 1));
							continue;
						}
						final TraceWindow window = new TraceWindow(c);
						window.setVisible(true);
						window.toFront();
					}
					// Print the warning message about rows without
					// configurations we could display.setValueAt
					if (nonassociatedRows.size() > 0) {
						final StringBuffer sb = new StringBuffer("Row");
						if (nonassociatedRows.size() > 1) {
							sb.append("s");
						}
						sb.append(" ");
						sb.append(nonassociatedRows.get(0));
						for (int i = 1; i < nonassociatedRows.size(); i++) {
							if (i == nonassociatedRows.size() - 1) {
								// Last one!
								sb.append(" and ");
							} else {
								sb.append(", ");
							}
							sb.append(nonassociatedRows.get(i));
						}
						sb.append("\ndo");
						if (nonassociatedRows.size() == 1) {
							sb.append("es");
						}
						sb.append(" not have end configurations.");
						JOptionPane.showMessageDialog((Component) e.getSource(), sb.toString(), "Bad Rows Selected",
								JOptionPane.ERROR_MESSAGE);
					}
				}
			});
		}
		if (((InputTableModel) table.getModel()).isMultiple) {

			bar.add(new AbstractAction("Edit File") {
				private static final long serialVersionUID = 76L;

				@Override
				public void actionPerformed(final ActionEvent arg0) {
					final int k = getMachineIndexBySelectedRow(table);
					if (k >= 0 && k < getEnvironment().myObjects.size()) {
						if (getObject() instanceof Automaton) {
							final Automaton cur = (Automaton) getEnvironment().myObjects.get(k);
							final EditorPane ep = new EditorPane(cur);
							ep.setName(cur.getFileName());
							getEnvironment().add(ep, "Edit", new CriticalTag() {
							});
							getEnvironment().setActive(ep);
						} else if (getObject() instanceof Grammar) {
							final Grammar cur = (Grammar) getEnvironment().myObjects.get(k);
							final GrammarInputPane ep = new GrammarInputPane(cur);
							ep.setName(cur.getFileName());
							getEnvironment().add(ep, "Edit", new CriticalTag() {
							});
							getEnvironment().setActive(ep);
						}
					}
				}
			});

			bar.add(new AbstractAction("Add input string") {
				private static final long serialVersionUID = 77L;

				@Override
				public void actionPerformed(final ActionEvent arg0) {
					boolean turing = false;
					if (getEnvironment().myObjects.get(0) instanceof TuringMachine) {
						turing = true;
					}
					final Object input = initialInput(getEnvironment().getActive(), "Input");

					if (input instanceof String) {
						final String s = (String) input;
						getEnvironment().myTestStrings.add(s);
					} else if (input instanceof String[]) {
						final String[] s = (String[]) input;
						for (final String element : s) {
							getEnvironment().myTestStrings.add(element);
						}
					} else {
						return;
					}

					// add expected output
					if (turing) {
						final Object output = initialInput(getEnvironment().getActive(), "Expected Output?");

						if (output instanceof String) {
							final String s = (String) output;
							getEnvironment().myTransducerStrings.add(s);
						} else if (output instanceof String[]) {
							final String[] s = (String[]) output;
							for (final String element : s) {
								getEnvironment().myTransducerStrings.add(element);
							}
						} else {
							getEnvironment().myTestStrings.remove(getEnvironment().myTestStrings.size() - 1);
							return;
						}
					}
					// add expected result
					final Object result = initialInput(getEnvironment().getActive(),
							"Expected Result? (Accept or Reject)");

					if (result instanceof String) {
						final String s = (String) result;
						getEnvironment().myTransducerStrings.add(s);
					} else if (result instanceof String[]) {
						final String[] s = (String[]) result;
						getEnvironment().myTransducerStrings.add(s[0]);
					} else {
						getEnvironment().myTestStrings.remove(getEnvironment().myTestStrings.size() - 1);
						getEnvironment().myTransducerStrings.remove(getEnvironment().myTestStrings.size() - 1);
						return;
					}

					getEnvironment().remove(getEnvironment().getActive());
					performAction(getEnvironment().getActive());
				}
			});

			bar.add(new AbstractAction("Add file") {
				private static final long serialVersionUID = 79L;

				@Override
				public void actionPerformed(final ActionEvent arg0) {
					final TestAction test = new TestAction();
					test.chooseFile(getEnvironment().getActive(), false);
					getEnvironment().remove(getEnvironment().getActive());
					performAction(getEnvironment().getActive());
				}
			});

			bar.add(new AbstractAction("Remove file") {
				private static final long serialVersionUID = 80L;

				@Override
				public void actionPerformed(final ActionEvent arg0) {
					final int k = getMachineIndexBySelectedRow(table);
					if (k >= 0 && k < getEnvironment().myObjects.size()) {
						getEnvironment().myObjects.remove(k);
						final int row = table.getSelectedRow();

						getEnvironment().myObjects.size();
						final int stringSize = getEnvironment().myTestStrings.size();

						final int beginOffset = row % stringSize;
						final int begin = (row - beginOffset);

						for (int i = 0; i < (stringSize); i++) {
							((InputTableModel) table.getModel()).deleteRow(begin);
						}
						table.changeSelection(0, 0, false, false);
					}
				}
			});

			bar.add(new AbstractAction("Save Results") {
				private static final long serialVersionUID = 81L;

				@Override
				public void actionPerformed(final ActionEvent arg0) {
					final JFrame frame = new JFrame("Save Location");

					final JRadioButton defaultLocation = new JRadioButton("Save Results with Original File");
					defaultLocation.setMnemonic(KeyEvent.VK_B);
					defaultLocation.setActionCommand("Save Results with Original File");
					defaultLocation.addActionListener(event -> {
					});
					final JRadioButton specifyLocation = new JRadioButton("Specify New Location");
					specifyLocation.addActionListener(event -> {
					});
					specifyLocation.setMnemonic(KeyEvent.VK_C);
					specifyLocation.setActionCommand("Specify New Location");
					defaultLocation.setSelected(true);
					final ButtonGroup group = new ButtonGroup();
					group.add(defaultLocation);
					group.add(specifyLocation);

					final JPanel panel = new JPanel();
					panel.add(defaultLocation);
					panel.add(specifyLocation);
					frame.getContentPane().add(panel, BorderLayout.CENTER);

					final JButton accept = new JButton("Accept");
					accept.addActionListener(event -> {
						frame.setVisible(false);
						String filepath = "";
						boolean failedSave = false;
						if (specifyLocation.isSelected()) {
							// The save as loop.
							File file = null;
							final boolean badname = false;
							while (badname || file == null) {
								if (!badname) {
									Universe.CHOOSER.setFileFilter(null);
									Universe.CHOOSER.setDialogTitle("Choose directory to save files in");
									Universe.CHOOSER.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
									final int result = Universe.CHOOSER.showSaveDialog(frame);
									if (result != JFileChooser.APPROVE_OPTION) {
										break;
									}
									file = Universe.CHOOSER.getSelectedFile();

									try {
										// Get the suggested file name.
										filepath = file.getCanonicalPath();
										final int last = filepath.lastIndexOf("\\");
										if (last == -1) {
											filepath = filepath + "/";
										} else {
											filepath = filepath + "\\";
										}

									} catch (final IOException e1) {
										// TODO Auto-generated catch block
										e1.printStackTrace();
									}
								}
							}
						}
						if (filepath.equals("")) {
							failedSave = true;
						}
						final InputTableModel model = (InputTableModel) table.getModel();
						String oldfileName = model.getValueAt(0, 0);
						String fileName = model.getValueAt(0, 0);
						boolean turing = false;
						Object machine = getEnvironment().myObjects.get(0);
						String base = filepath;
						if (machine instanceof Automaton) {
							if (machine instanceof TuringMachine) {
								turing = true;
							}
							if (failedSave) {
								base = ((Automaton) machine).getFilePath();
							}
						} else if (machine instanceof Grammar) {
							if (failedSave) {
								base = ((Grammar) machine).getFilePath();
							}
						}

						try {
							final FileWriter writer = new FileWriter(base + "results" + fileName + ".txt");
							BufferedWriter bwriter = new BufferedWriter(writer);
							PrintWriter out = new PrintWriter(bwriter);
							;
							for (int r = 0; r < model.getRowCount(); r++) {
								fileName = model.getValueAt(r, 0);
								if (!fileName.equals(oldfileName)) {
									oldfileName = fileName;
									out.flush();
									out.close();
									if (fileName.equals("")) {
										break;
									}
									final int index = getMachineIndexByName(fileName);
									machine = getEnvironment().myObjects.get(index);
									if (machine instanceof Automaton) {
										if (!specifyLocation.isSelected() || failedSave) {
											base = ((Automaton) machine).getFilePath();
										}
									} else if (machine instanceof Grammar) {
										if (!specifyLocation.isSelected() || failedSave) {
											base = ((Grammar) machine).getFilePath();
										}
									}
									bwriter = new BufferedWriter(new FileWriter(base + "results" + fileName + ".txt"));
									out = new PrintWriter(bwriter);
								}
								boolean input = false;
								boolean end = false;
								boolean output = false;

								for (int c = 1; c < model.getColumnCount(); c++) {
									if ((model.getColumnName(c).startsWith("Input")) && !input) {
										out.write("Input: ");
										input = true;
									}
									if ((model.getColumnName(c).startsWith("Output")) && !output && turing) {
										out.write("Output: ");
										output = true;
									}
									if (model.getColumnName(c).startsWith("Result")) {
										end = true;
										out.write("Result: ");
									}
									final String value = model.getValueAt(r, c);

									out.write(value + " ");
									try {
										if (end) {
											bwriter.newLine();
										}
									} catch (final IOException e2) {

									}
								}
							}
							out.close();
						} catch (final IOException e3) {

						}
					});

					frame.getContentPane().add(accept, BorderLayout.SOUTH);
					frame.pack();
					final Point point = new Point(100, 50);
					frame.setLocation(point);
					frame.setVisible(true);
				}
			});
		}

		myPanel = panel;
		// Set up the final view.
		final Object finObject = getObject();
		if (finObject instanceof Automaton) {
			final AutomatonPane ap = new AutomatonPane((Automaton) finObject);
			ap.addMouseListener(new ArrowDisplayOnlyTool(ap, ap.getDrawer()));
			final JSplitPane split = SplitPaneFactory.createSplit(getEnvironment(), true, 0.5, ap, panel);
			final MultiplePane mp = new MultiplePane(split);
			getEnvironment().add(mp, getComponentTitle(), new CriticalTag() {
			});
			getEnvironment().setActive(mp);
		} else if (finObject instanceof Grammar) {
			final BruteParsePane bp = new BruteParsePane((GrammarEnvironment) getEnvironment(), (Grammar) finObject,
					(InputTableModel) table.getModel());
			bp.inputField.setEditable(false);
			if (getEnvironment().myTestStrings != null && getEnvironment().myTestStrings.size() > 0) {
				bp.inputField.setText(getEnvironment().myTestStrings.get(0));
			}
			final JSplitPane split = SplitPaneFactory.createSplit(getEnvironment(), true, 0.5, bp, panel);

			final MultiplePane mp = new MultiplePane(split);
			getEnvironment().add(mp, getComponentTitle(), new CriticalTag() {
			});
			getEnvironment().setActive(mp);
		}
	}

	/**
	 * @param machineFileName
	 *
	 */
	protected void updateView(final String machineFileName, final String input, final JTableExtender table) {
		final List<Object> machines = getEnvironment().myObjects;
		Object current = null;
		if (machines != null) {
			current = machines.get(0);
		} else {
			current = getEnvironment().getObject();
		}
		if (current instanceof Automaton && ((InputTableModel) table.getModel()).isMultiple) {
			final int spot = getMachineIndexBySelectedRow(table);
			Automaton cur = null;
			if (spot != -1) {
				cur = (Automaton) machines.get(spot);
			} else {
				cur = (Automaton) getEnvironment().getObject();
			}

			final AutomatonPane newAP = new AutomatonPane(cur);
			newAP.addMouseListener(new ArrowDisplayOnlyTool(newAP, newAP.getDrawer()));
			final JSplitPane split = SplitPaneFactory.createSplit(getEnvironment(), true, 0.5, newAP, myPanel);
			final MultiplePane mp = new MultiplePane(split);

			final EnvironmentFrame frame = Universe.frameForEnvironment(getEnvironment());
			final String newTitle = cur.getFileName();
			if (newTitle != "") {
				frame.setTitle(newTitle);
			}
			getEnvironment().remove(getEnvironment().getActive());

			getEnvironment().add(mp, getComponentTitle(), new CriticalTag() {
			});
			getEnvironment().setActive(mp);

		} else if (current instanceof Grammar && (table.getSelectedRow() < (table.getRowCount() - 1))) {
			final int spot = getMachineIndexBySelectedRow(table);
			Grammar cur = null;
			if (spot != -1) {
				cur = (Grammar) machines.get(spot);
			} else {
				cur = (Grammar) getEnvironment().getObject();
			}

			final BruteParsePane bp = new BruteParsePane((GrammarEnvironment) getEnvironment(), cur, null);
			int column = 1;
			if (spot == -1) {
				column = 0;
			}
			bp.inputField.setText((String) table.getModel().getValueAt(table.getSelectedRow(), column));
			// bp.inputField.setEnabled(false);
			bp.inputField.setEditable(false);
			final JSplitPane split = SplitPaneFactory.createSplit(getEnvironment(), true, 0.5, bp, myPanel);
			final MultiplePane mp = new MultiplePane(split);
			getEnvironment().add(mp, getComponentTitle(), new CriticalTag() {
			});

			final EnvironmentFrame frame = Universe.frameForEnvironment(getEnvironment());
			final String newTitle = cur.getFileName();
			if (newTitle != "") {
				frame.setTitle(newTitle);
			}
			getEnvironment().remove(getEnvironment().getActive());

			getEnvironment().add(mp, getComponentTitle(), new CriticalTag() {
			});
			getEnvironment().setActive(mp);
		}
	}

	public void viewAutomaton(final JTableExtender table) {
		final InputTableModel model = (InputTableModel) table.getModel();
		if (model.isMultiple) {
			final int row = table.getSelectedRow();
			if (row < 0) {
				return;
			}
			final String machineFileName = model.getValueAt(row, 0);
			updateView(machineFileName, model.getValueAt(row, 1), table);
		} else if (getEnvironment().getObject() instanceof Grammar) {
			updateView(((Grammar) getEnvironment().getObject()).getFileName(),
					model.getValueAt(table.getSelectedRow(), 1), table);
		} else if (getEnvironment().getObject() instanceof Automaton) {
			updateView(((Automaton) getEnvironment().getObject()).getFileName(),
					model.getValueAt(table.getSelectedRow(), 1), table);
		}
	}
}
