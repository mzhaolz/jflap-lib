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

package edu.duke.cs.jflap.gui.pumping;

import edu.duke.cs.jflap.pumping.PumpingLemma;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * This class represents the implementation of
 * <code>PumpingLemmaInputPane</code> wherein the computer makes the first move.
 * The computer will choose the <i>m</i> and decomposition values, while the
 * user will generate the <i>w</i> and <i>i</i> values based on the computer's
 * output.
 *
 * @author Chris Morgan & Jinghui Lim
 */
public abstract class ComputerFirstPane extends PumpingLemmaInputPane {
  /**
   *
   */
  private static final long serialVersionUID = 1L;
  /**
   * The goal of the user, which is to try to force a contradiction.
   */
  private static String OBJECTIVE = "Prevent the computer from finding a valid partition.";
  /**
   * The description that describes the computer's chosen value of <i>m</i>.
   */
  private static String DESCRIBE_M = "I have selected a value for m, displayed below.";
  /**
   * The instruction that prompts the user for the selection of <i>w</i>.
   */
  private static String PROMPT_W = "Please enter a possible value for w and press \"Enter\".";
  /**
   * The description outlining the nature of the decomposition <i>w</i>.
   */
  private static String DESCRIBE_DECOMPOSITION = "I have decomposed w into the following...";
  /**
   * The instruction that prompts the user for the selection of <i>i</i>.
   */
  private static String PROMPT_I = "Please enter a possible value for i and press \"Enter\".";
  /**
   * The message that states not every case can be generated with the current
   * <i>w</i> value.
   */
  // Uncomment this and the corresponding areas in initWPanel() if the
  // computer will process
  // cases.
  // private static String ALL_CASES_NOT_APPLICABLE = "WARNING: Every case
  // cannot be generated with this w value.";
  /**
   * This label is placed in the decomp stage panel, and describes the
   * decomposition of <i>w</i> that is generated by the computer.
   */
  protected JLabel decompLabel;

  public ComputerFirstPane(PumpingLemma l, String title) {
    super(l, title);
    l.setFirstPlayer(PumpingLemma.COMPUTER);
  }

  @Override
  protected JPanel initDecompPanel() {
    JPanel p = new JPanel();
    decompLabel = new JLabel();
    p.add(decompLabel);
    p.setBorder(BorderFactory.createTitledBorder("3. " + DESCRIBE_DECOMPOSITION));
    p.setMaximumSize(new Dimension(MAX_SIZE.width, MAX_SIZE.height / 5));
    p.setPreferredSize(new Dimension(MAX_SIZE.width, MAX_SIZE.height / 5));
    return p;
  }

  @Override
  public void resetDecompPanel() {
    decompLabel.setText("");
  }

  @Override
  public void setDecomposition(int[] decomposition) {
    myLemma.setDecomposition(decomposition);
    decompLabel.setText(myLemma.getDecompositionAsString());
  }

  @Override
  protected String addTopGameFeatures(JButton b) {
    b.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            myWDisplay.setText("");
            updateTopPane(false);
            reset();
            myLemma.chooseM();
            myMDisplay.setText(Integer.toString(myLemma.getM()));
          }
        });
    return OBJECTIVE;
  }

  @Override
  protected String addMGameFeatures() {
    myMDisplay = new JTextArea(1, 10);
    myMDisplay.setEditable(false);
    if (myLemma.getM() == -1) // only not -1 if loading a file
    myLemma.chooseM();
    myMDisplay.setText(Integer.toString(myLemma.getM()));
    return DESCRIBE_M;
  }

  @Override
  protected String addWGameFeatures() {
    myWDisplay = new JTextField(20);
    ((JTextField) myWDisplay)
        .addActionListener(
            new ActionListener() {
              @Override
              public void actionPerformed(ActionEvent ev) {
                for (int i = 3; i < stages.length; i++) stages[i].setVisible(false);
                String w = myWDisplay.getText();

                if (myLemma.isInLang(w) && w.length() >= myLemma.getM()) {
                  // String oldW, oldMessage;
                  // oldW = myLemma.getW();
                  // oldMessage = stageMessages[2].getText();

                  myLemma.setW(w);
                  stages[3].setVisible(true);
                  stages[4].setVisible(true);
                  myIDisplay.setText("");
                  myPumpedStringDisplay.setText("");
                  myLemma.chooseDecomposition();
                  decompLabel.setText("\n\n" + myLemma.getDecompositionAsString());
                  resetMessages();
                  leftPanel.revalidate();
                  /*
                   * If this is has only one case, myCases will not be
                   * initialized so we check for null.
                   */
                  /*
                   * if(myCases != null) { if (!oldW.equals(w)) {
                   * myCases.clearAll(); if
                   * (!oldMessage.equals(ALL_CASES_NOT_APPLICABLE) &&
                   * !myLemma.checkAllCasesPossible()) {
                   * stageMessages[2].setText(ALL_CASES_NOT_APPLICABLE); } }
                   * myCases.setDecomposition(myLemma.getDecomposition());
                   * myCases.setAddReplaceButtonsEnabled(false); }
                   */
                } else {
                  String error;
                  if (w.length() >= myLemma.getM())
                    error = "That string was not in the language.  Please enter another.";
                  else error = "Remember |w| must be >= m";
                  // Something other than an acceptable w value was entered
                  myWDisplay.selectAll();
                  stageMessages[2].setText(error);
                }
              }
            });
    return PROMPT_W;
  }

  @Override
  protected String addIGameFeatures() {
    myIDisplay = new JTextField(20);
    ((JTextField) myIDisplay)
        .addActionListener(
            new ActionListener() {
              @Override
              public void actionPerformed(ActionEvent ev) {
                try {
                  int i = Integer.parseInt(myIDisplay.getText());
                  if (!(i >= 0 && i != 1 && i <= 12)) throw new NumberFormatException();
                  stages[5].setVisible(true);
                  myLemma.setI(i);
                  if (myLemma.isInLang(myLemma.createPumpedString()))
                    myLemma.addAttempt(
                        myLemma.getDecompositionAsString()
                            + "; I = "
                            + myLemma.getI()
                            + "; <i>Failed</i>");
                  else
                    myLemma.addAttempt(
                        myLemma.getDecompositionAsString()
                            + "; I = "
                            + myLemma.getI()
                            + "; <i>Won</i>");
                  resetMessages();
                  displayEnd();
                  updateTopPane(false);
                  leftPanel.revalidate();
                  setCanvas();
                  myCanvas.stop();
                  if (myCases != null) {
                    myCases.setI(i);
                    myCases.setAddReplaceButtonsEnabled(true);
                  }
                } catch (NumberFormatException e) {
                  // Something other than a positive integer or something in
                  // the wrong range was entered.
                  String error =
                      "Please enter a positive integer in range [0, 2...12] for best results.";
                  myIDisplay.selectAll();
                  stageMessages[4].setText(error);
                }
              }
            });
    return PROMPT_I;
  }

  @Override
  public void displayEnd() {
    String s = myLemma.createPumpedString();
    myPumpedStringDisplay.setText(s);
    if (myLemma.isInLang(s))
      myLastWord.setText(
          createXYZ()
              + " = "
              + PumpingLemmaInputPane.toHTMLString(s)
              + " = "
              + s
              + " is in the language.  Please try again.");
    else
      myLastWord.setText(
          createXYZ()
              + " = "
              + PumpingLemmaInputPane.toHTMLString(s)
              + " = "
              + s
              + " is NOT in the language.  YOU WIN!");
  }
}
