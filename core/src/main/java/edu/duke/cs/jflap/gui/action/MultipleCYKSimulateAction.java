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
import java.util.List;
import java.util.Scanner;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JToolBar;

import edu.duke.cs.jflap.automata.Automaton;
import edu.duke.cs.jflap.automata.turing.TuringMachine;
import edu.duke.cs.jflap.grammar.Grammar;
import edu.duke.cs.jflap.gui.JTableExtender;
import edu.duke.cs.jflap.gui.SplitPaneFactory;
import edu.duke.cs.jflap.gui.environment.Environment;
import edu.duke.cs.jflap.gui.environment.EnvironmentFrame;
import edu.duke.cs.jflap.gui.environment.GrammarEnvironment;
import edu.duke.cs.jflap.gui.environment.Profile;
import edu.duke.cs.jflap.gui.environment.Universe;
import edu.duke.cs.jflap.gui.environment.tag.CriticalTag;
import edu.duke.cs.jflap.gui.grammar.GrammarInputPane;
import edu.duke.cs.jflap.gui.grammar.parse.CYKParsePane;
import edu.duke.cs.jflap.gui.sim.multiple.InputTableModel;

/**
 * Similiar code to MultipleSimulateAction. Once again, did not want to mess up
 * the original code. So, I created the new MutlipleCYKSimulateAction class just
 * for Multiple CYK Parsing.
 *
 * @author Kyung Min (Jason) Lee
 *
 */
public class MultipleCYKSimulateAction extends MultipleSimulateAction {
	/**
	 * This auxillary class is convenient so that the help system can easily
	 * identify what type of component is active according to its class.
	 */
	public class MultiplePane extends JPanel {
		private static final long serialVersionUID = 61L;

		public JSplitPane mySplit = null;

		public MultiplePane(final JSplitPane split) {
			super(new BorderLayout());
			add(split, BorderLayout.CENTER);
			mySplit = split;
		}
	}

	private static final long serialVersionUID = 41L;
	private final Grammar myOriginalGrammar;

	private final Grammar myCNFGrammar;

	public MultipleCYKSimulateAction(final Grammar original, final Grammar cnf, final Environment environment) {
		super(original, environment);
		myOriginalGrammar = original;
		myCNFGrammar = cnf;
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

	@Override
	public int getMachineIndexByName(final String machineFileName) {
		final List<Object> machines = getEnvironment().myObjects;
		if (machines == null) {
			return -1;
		}
		for (int k = 0; k < machines.size(); k++) {
			final Object current = machines.get(k);
			if (current instanceof Grammar) {
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

	@Override
	public void performAction(final Component source) {

		table = initializeTable(getObject());
		if (((InputTableModel) table.getModel()).isMultiple) {
			getEnvironment().remove(getEnvironment().getActive());
		}

		final JPanel panel = new JPanel(new BorderLayout());
		final JToolBar bar = new JToolBar();
		panel.add(new JScrollPane(table), BorderLayout.CENTER);
		panel.add(bar, BorderLayout.SOUTH);
		// Add the running input thing.
		bar.add(new AbstractAction("Load Inputs") {
			private static final long serialVersionUID = 43L;

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
						while (sc.hasNext()) {
							final String temp = sc.next();
							// System.out.println(temp);
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
		bar.add(new AbstractAction("Run Inputs") {
			private static final long serialVersionUID = 45L;

			@Override
			public void actionPerformed(final ActionEvent e) {
				try {
					// Make sure any recent changes are registered.
					table.getCellEditor().stopCellEditing();
				} catch (final NullPointerException exception) {
					// We weren't editing anything, so we're OK.
				}
				final InputTableModel model = (InputTableModel) table.getModel();

				if (getObject() instanceof Grammar) {
					model.getInputs();
					getObject();
					final CYKParsePane parsePane = new CYKParsePane((GrammarEnvironment) getEnvironment(),
							myOriginalGrammar, myCNFGrammar, model);
					parsePane.inputField.setEditable(false);
					parsePane.row = -1;
					parsePane.parseMultiple();
				}
			}
		});
		if (!((InputTableModel) table.getModel()).isMultiple) {
			// Add the clear button.
			bar.add(new AbstractAction("Clear") {
				private static final long serialVersionUID = 47L;

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
				private static final long serialVersionUID = 49L;

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
		if (((InputTableModel) table.getModel()).isMultiple) {

			bar.add(new AbstractAction("Edit File") {
				private static final long serialVersionUID = 51L;

				@Override
				public void actionPerformed(final ActionEvent arg0) {
					final int k = getMachineIndexBySelectedRow(table);
					if (k >= 0 && k < getEnvironment().myObjects.size()) {
						if (getObject() instanceof Grammar) {
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
				private static final long serialVersionUID = 53L;

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
				private static final long serialVersionUID = 55L;

				@Override
				public void actionPerformed(final ActionEvent arg0) {
					final TestAction test = new TestAction();
					test.chooseFile(getEnvironment().getActive(), false);
					getEnvironment().remove(getEnvironment().getActive());
					performAction(getEnvironment().getActive());
				}
			});

			bar.add(new AbstractAction("Remove file") {
				private static final long serialVersionUID = 57L;

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
				private static final long serialVersionUID = 59L;

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
									if (model.getColumnName(c).startsWith("Output") && !output && turing) {
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

		if (finObject instanceof Grammar) {
			final CYKParsePane bp = new CYKParsePane((GrammarEnvironment) getEnvironment(), (Grammar) finObject,
					myCNFGrammar, (InputTableModel) table.getModel());
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
	@Override
	protected void updateView(final String machineFileName, final String input, final JTableExtender table) {
		final List<Object> machines = getEnvironment().myObjects;
		Object current = null;
		if (machines != null) {
			current = machines.get(0);
		} else {
			current = getEnvironment().getObject();
		}
		if (current instanceof Grammar && (table.getSelectedRow() < (table.getRowCount() - 1))) {
			final int spot = getMachineIndexBySelectedRow(table);
			Grammar cur = null;
			if (spot != -1) {
				cur = (Grammar) machines.get(spot);
			} else {
				cur = (Grammar) getEnvironment().getObject();
			}

			final CYKParsePane bp = new CYKParsePane((GrammarEnvironment) getEnvironment(), cur, myCNFGrammar, null);
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
}
