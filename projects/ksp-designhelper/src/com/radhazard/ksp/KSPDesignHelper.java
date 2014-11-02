/*
 * KSP Transfer Stage Design Helper v2.0
 * Copyright (c) 2013, Ryan Schneider
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *   - Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *   - Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
 * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 */
package com.radhazard.ksp;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import com.radhazard.ksp.data.Engine;
import com.radhazard.ksp.data.Fuel;

public class KSPDesignHelper extends JPanel implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static final String version = "v2.0";
	private static final double g0 = 9.82;	// Dem kerbals and their silly constants.  This is the exact value so don't "fix" it
	private static final Engine[] engines = Engine.values();
	
	private JLabel TWLabel;
	private JLabel dVLabel;
	private JLabel payloadLabel;
	
	private JTextField TWField;
	private JTextField dVField;
	private JTextField payloadField;
	
	private JButton calcButton;
	
	private ResultTable resultTable;
	
	private JPanel customPanel;
	private JLabel customMassLabel;
	private JLabel customThrustLabel;
	private JLabel customISPLabel;
	private JLabel customFuelLabel;
	
	private JTextField customMassField;
	private JTextField customThrustField;
	private JTextField customISPField;
	private JComboBox<Fuel> customFuelBox;
	
	public KSPDesignHelper() {
		super(new GridBagLayout());

		TWLabel = new JLabel("Minimum TWR");
		TWField = new JTextField(20);
		TWField.addActionListener(this);
		
		dVLabel = new JLabel("Minimum dV");
		dVField = new JTextField(20);
		dVField.addActionListener(this);
		
		payloadLabel = new JLabel("Payload mass");
		payloadField = new JTextField(20);
		payloadField.addActionListener(this);
		
		calcButton = new JButton("Calculate");
		calcButton.addActionListener(this);
		
		customPanel = new JPanel(new GridLayout(2, 4, 5, 5));

		// Initialize the result table
		resultTable = new ResultTable(engines);
		
		// Initialize the custom engine panel.
		customMassLabel = new JLabel("Mass");
		customMassField = new JTextField(10);
		customMassField.addActionListener(this);
		
		customThrustLabel = new JLabel("Thrust");
		customThrustField = new JTextField(10);
		customThrustField.addActionListener(this);
		
		customISPLabel = new JLabel("ISP");
		customISPField = new JTextField(10);
		customISPField.addActionListener(this);
		
		customFuelLabel = new JLabel("Fuel");
		customFuelBox = new JComboBox<Fuel>(Fuel.values());
		customFuelBox.addActionListener(this);
		
		customPanel.add(customMassLabel);
		customPanel.add(customThrustLabel);
		customPanel.add(customISPLabel);
		customPanel.add(customFuelLabel);
		customPanel.add(customMassField);
		customPanel.add(customThrustField);
		customPanel.add(customISPField);
		customPanel.add(customFuelBox);
		
		TitledBorder customBorder;
		customBorder = BorderFactory.createTitledBorder("Custom Engine");
		customPanel.setBorder(customBorder);
		
		//Add Components to this panel.
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(5,5,5,5);
		
		c.gridx = 0; c.gridy = 0;
		add(TWLabel, c);
		c.gridx = 1; c.gridy = 0;
		add(TWField, c);
		
		c.gridx = 0; c.gridy = 1;
		add(dVLabel, c);
		c.gridx = 1; c.gridy = 1;
		add(dVField, c);
		
		c.gridx = 0; c.gridy = 2;
		add(payloadLabel, c);
		c.gridx = 1; c.gridy = 2;
		add(payloadField, c);
		
		c.gridx = 0; c.gridy = 3;
		c.gridwidth = 2;
		add(calcButton, c);
		
		c.gridx = 0; c.gridy = 4;
		c.gridwidth = 2;
		add(new JSeparator(), c);
		
		c.gridx = 0; c.gridy = 5;
		c.gridwidth = 2;
		c.ipady = 0;
		add(resultTable, c);
		
		c.gridx = 0; c.gridy = 6;
		c.gridwidth = 2;
		add(customPanel, c);
	}

	public static void main(String[] args) {	
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				//Create and set up the window.
				JFrame frame = new JFrame("KSP Transfer Stage Design Helper " + version);
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

				//Add contents to the window.
				frame.add(new KSPDesignHelper());

				frame.setResizable(false);
				
				//Display the window.
				frame.pack();
				frame.setVisible(true);
			}
		});
	}
	
	@Override
	public void actionPerformed(ActionEvent event) {
		// Parse the custom engine data
		double custMass;
		double custThrust;
		double custISP;
		Fuel custFuel = (Fuel) customFuelBox.getSelectedItem();
		try {
			custMass = Double.parseDouble(customMassField.getText());
			custThrust = Double.parseDouble(customThrustField.getText());
			custISP = Double.parseDouble(customISPField.getText());
		} catch (NumberFormatException e) {
			custMass = -1;
			custThrust = 0;
			custISP = 0;
		}
		
		// Update the result table
		try {
			double minTMR = Double.parseDouble(TWField.getText()) * g0;
			double min_dV = Double.parseDouble(dVField.getText());
			double payloadMass = Double.parseDouble(payloadField.getText());
			
			// Update all the engines
			for(int i = 0; i < engines.length; i++) {
				if (engines[i] == Engine.CUSTOM) {
					if (custMass >= 0) {
						resultTable.setResult(Engine.CUSTOM, RocketSolver.recalculate(minTMR, min_dV, payloadMass, custMass, custThrust, custISP, custFuel));
					} else {
						resultTable.setResult(Engine.CUSTOM, null);
					}
				} else {
					resultTable.setResult(engines[i], RocketSolver.recalculate(minTMR, min_dV, payloadMass, engines[i]));
				}
			}
		} catch (NumberFormatException e) {
			Object source = event.getSource();
			if (source != customMassField && source != customThrustField && source != customISPField && source != customFuelBox) {
				// Supress this warning if it comes from the custom fuel box
				JOptionPane.showMessageDialog(this, "Please fill in all fields", "Error", JOptionPane.WARNING_MESSAGE);
			}
		}
	}
}