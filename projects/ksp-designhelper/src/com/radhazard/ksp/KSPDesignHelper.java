package com.radhazard.ksp;
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
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowSorter;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.apache.commons.math3.linear.*;

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
	
	private JPanel results;
	private JTable resultTable;
	
	private JPanel customPanel;
	private JLabel customMassLabel;
	private JLabel customThrustLabel;
	private JLabel customISPLabel;
	private JLabel customFuelLabel;
	
	private JTextField customMassField;
	private JTextField customThrustField;
	private JTextField customISPField;
	private JComboBox customFuelBox;
	
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
		
		results = new JPanel(new BorderLayout());
		customPanel = new JPanel(new GridLayout(2, 4, 5, 5));

		// Initialize the result table
		resultTable = new JTable(new ResultModel());
		
		int [] prefWidths = {120, 130, 110, 110, 120};	// preferred table widths 
		for (int i = 0; i < resultTable.getColumnCount(); i++) {
			TableColumn column = resultTable.getColumnModel().getColumn(i);
			column.setPreferredWidth(prefWidths[i]);
			column.setResizable(false);
		}
		
		for (int i = 0; i < resultTable.getRowCount(); i++) {
			resultTable.getModel().setValueAt(engines[i].name, i, 0);
		}
		RowSorter<TableModel> sorter = new TableRowSorter<TableModel>(resultTable.getModel());
		resultTable.setRowSorter(sorter);
		
		ResultRenderer resultRenderer = new ResultRenderer(); 
		
		resultTable.setDefaultRenderer(Integer.class, resultRenderer);
		resultTable.setDefaultRenderer(Double.class, resultRenderer);
		
		results.add(resultTable.getTableHeader(), BorderLayout.PAGE_START);
		results.add(resultTable);
		
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
		customFuelBox = new JComboBox(Fuel.values());
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
		add(results, c);
		
		c.gridx = 0; c.gridy = 6;
		c.gridwidth = 2;
		add(customPanel, c);
		
		// Initialize Fuel tanks
		Tank[] tanks = Tank.values();
		
		for(int i = 0; i < tanks.length; i++) {
			tanks[i].fuel.addTank(tanks[i]);
		}
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

	/**
	 * A custom table model for holding our design data
	 */
	class ResultModel extends AbstractTableModel {

		private static final long serialVersionUID = 1L;
		private String[] columnNames = {"Name", "Engines Required", "Total Fuel Mass", "Total Ship Mass", "Payload Fraction"};
		private Object[][] data = new Object[engines.length][columnNames.length];
		
		ResultModel() {
			super();
		}

		public int getColumnCount() {
			return columnNames.length;
		}

		public int getRowCount() {
			return data.length;
		}
		
		public String getColumnName(int col) {
			return columnNames[col];
		}

		public Object getValueAt(int row, int col) {
			return data[row][col];
		}

		public Class<?> getColumnClass(int col) {
			switch(col) {
			case 0:
				return String.class;
			case 4:
				return Integer.class;
			default:
				return Double.class;
			}
		}

		public void setValueAt(Object value, int row, int col) {
			data[row][col] = value;
			fireTableCellUpdated(row, col);
		}
	}
	
	/**
	 * A custom table cell renderer that replaces the empty string
	 * with a center-aligned bar.
	 */
	class ResultRenderer extends DefaultTableCellRenderer {
		private static final long serialVersionUID = 1L;

		public ResultRenderer() {
			super();
		}

		public void setValue(Object value) {
			if (value == null) {
				setHorizontalAlignment(JLabel.CENTER);
				setText("-");
			} else {
				setHorizontalAlignment(JLabel.RIGHT);
				setText(value.toString());
			}
		}
	}
	
	enum Engine {
		LV_1("LV-1/LV-1R", 0.03, 1.5, 290, Fuel.LFO),
		R24_77("24-77", 0.09, 20, 300, Fuel.LFO),
		R48_7S("48-7S", 0.1, 20, 350, Fuel.LFO),
		RMK_55("Mark 55", 0.9, 120, 320, Fuel.LFO),
		LV_909("LV-909", 0.5, 50, 390, Fuel.LFO),
		LV_T30("LV-T30", 1.25, 215, 370, Fuel.LFO),
		LV_T45("LV-T45", 1.5, 200, 370, Fuel.LFO),
		Aerospike("Aerospike", 1.5, 175, 390, Fuel.LFO),
		Poodle("Poodle", 2.5, 220, 390, Fuel.LFO),
		Skipper("Skipper", 4, 650, 350, Fuel.LFO),
		Mainsail("Mainsail", 6, 1500, 330, Fuel.LFO),
		LV_N("LV-N", 2.25, 60, 800, Fuel.LFO),
		PB_ION_GIG("PB-ION /W Gigantor", 0.6, 0.5, 4200, Fuel.Xenon),
		PB_ION_OX4("PB-ION /W 9 OX-4", 0.4075, 0.5, 4200, Fuel.Xenon),
		PB_ION("PB-ION", 0.25, 0.5, 4200, Fuel.Xenon),
		CUSTOM("Custom", 0, 0, 0, Fuel.LFO);
		
		String name;
		double mass;
		double thrust;
		double isp;
		Fuel fuel;
		
		int enginesRequired;
		double totalFuelTankMass;
		double totalShipMass;
		double payloadFraction;
		
		Engine(String name, double mass, double thrust, double isp, Fuel fuel) {
			this.name = name;
			this.mass = mass;
			this.thrust = thrust;
			this.isp = isp;
			this.fuel = fuel;
		}
		
		double getTM() {
			return (thrust/mass);
		}
		
		/**
		 * Recalculate ship parameters given the new TMR, dV, and payload mass
		 * 
		 * @param minTMR
		 * @param min_dV
		 * @param payloadMass
		 */
		void recalculate(double minTMR, double min_dV, double payloadMass) {
			double minMassRatio = Math.exp(min_dV / (g0 * isp));
			
			double maxLoad = (thrust / minTMR) - mass;				// Maximum load each engine can carry while still retaining the TWR.
			double dryMass = (maxLoad + mass) / minMassRatio;		// Calculate dry mass with the inverted rocket equation
			double fuelMass = maxLoad + mass - dryMass;				// Mass of the fuel is equal to total mass minus dry mass
			
			// TODO Modify this for MILPSolver
			double tankMassRatio = fuel.fuelTanks.get(0).getMassRatio();
			double tankMass = fuelMass / tankMassRatio; 
			double maxPayload = dryMass - (mass + tankMass);		// Maximum payload is the dry mass minus engines and empty fuel tanks.
			
			enginesRequired = (int)Math.ceil(payloadMass / maxPayload);
			
			if (enginesRequired > 0) { // sanity check
				int[] numTanks = MILPSolver(payloadMass, mass, thrust, minMassRatio, minTMR, fuel.fuelTanks);	// That's Mixed Integer Linear Programming, not... anything else.
				if (numTanks == null) {
					// TODO: MILPSolver died (RIP MILPSolver)
				}
			}
			
			// Correct total fuel mass
			// Rocket equation, solved for fuel mass.
			totalFuelTankMass = (payloadMass + enginesRequired * mass) * (minMassRatio - 1) / (1 - minMassRatio / tankMassRatio);
			totalShipMass = (enginesRequired * mass + totalFuelTankMass + payloadMass);
			payloadFraction = payloadMass / totalShipMass;
		}
		
		void recalcCustom(double minTMR, double min_dV, double payloadMass, double mass, double thrust, double isp, Fuel fuel) {
			this.mass = mass;
			this.thrust = thrust;
			this.isp = isp;
			this.fuel = fuel;
			recalculate(minTMR, min_dV, payloadMass);
		}
	};
	
	/**
	 * Different fuel types
	 */
	enum Fuel {
		LFO("LFO", 5),
		Liquid("Liquid", 5),
		Xenon("Xenon", 0.1);
		
		String name;
		double density;
		Vector<Tank> fuelTanks; 
		
		Fuel(String name, double density) {
			this.name = name;
			this.density = density;
			fuelTanks = new Vector<Tank>();
		}
		
		void addTank(Tank tank) {
			if (fuelTanks.size() <= 0) {
				fuelTanks.add(tank);
			} else {
				// Insertion sort by mass ratio, and then capacity if mass ratio is equal
				int i = 0;
				while(i < fuelTanks.size() && fuelTanks.get(i).getMassRatio() > tank.getMassRatio()) i++;
				while(i < fuelTanks.size() && fuelTanks.get(i).getMassRatio() == tank.getMassRatio() && fuelTanks.get(i).capacity > tank.capacity) i++;
				fuelTanks.add(i, tank);
			}
		}
		
		public String toString() {
			return name;
		}
	};

	/**
	 * Various fuel tanks
	 */
	enum Tank {
		OscarB(Fuel.LFO, 12.735, 0.015),
		Round8(Fuel.LFO, 22.2, 0.025),
		FL_T100(Fuel.LFO, 100, 0.0625),
		MK1(Fuel.Liquid, 150, 0.35),
		MK2(Fuel.Liquid, 160, 0.2),
		Xenon(Fuel.Xenon, 700, 0.05),
		XenonRadial(Fuel.Xenon, 400, 0.03);
		
		Fuel fuel;
		double capacity;
		double dryMass;
		double wetMass;
		
		Tank(Fuel fuel, double capacity, double dryMass) {
			this.fuel = fuel;
			this.capacity = capacity;
			this.dryMass = dryMass;
			this.wetMass = dryMass + (capacity * fuel.density);
		}
		
		double getMassRatio() {
			return (capacity * fuel.density) / dryMass;
		}
	}
	
	/**
	 * A simple cutting-plane algorithm for solving Mixed-Integer Linear Programming.
	 * Implements Simplex followed by Gomory's Cut, repeated until an integral solution is found.
	 * @return 
	 */
	public static int[] MILPSolver(double payloadMass, double engineMass, double engineThrust, double dmr, double twr, Vector<Tank> fuelTanks) {
		/*
		 * Constants:
		 * mp - payload mass	me - engine mass	wm1..n - fuel tank wet mass		dm1..n - fuel tank dry mass		DMR - Desired Mass Ratio	TWR - Thrust/Weight ratio
		 * 
		 * Variables:
		 * xe - num engines		x1..n - num fuel tanks	slack1..2 - slack variables
		 * 
		 * Goal:
		 * Minimize me * xe + wm1 * x1 + ... + wmn * xn
		 * 
		 * Constraints:
		 * (me - DMR * me)xe + (wm1 - DMR * dm1)x1 + ... + (wmn - DMR * dmn)xn + slack1 == -(mp - DMR * mp)
		 * (Te - TWR * me)xe - (TWR * wm1)x1 - ... - (TWR * wmn)xn + slack2 == TWR * mp
		 */
		
		/* Canonical Matrix:
		 * [ 1 -me -wm1 ... -wmn 0 0 0  ]
		 * [ 0  Ce  C1  ...  Cn  1 0 Cp ]
		 * [ 0  Ce -C2  ... -Cn  0 1 Cp ]
		 */
		
		System.out.println("= SOLVING MILP ="); // TODO Debug
		
		RealMatrix canonical = new Array2DRowRealMatrix(3, fuelTanks.size() + 5);
		
		// Set first row and engine var column
		double sm1[][] = {{1, -engineMass},
						  {0, (1 - dmr) * engineMass},
						  {0, engineThrust - twr * engineMass}};
		canonical.setSubMatrix(sm1, 0, 0);
		
		// Set each fuel tank column
		for (int i = 0; i < fuelTanks.size(); i++) {
			double col[] = {-fuelTanks.get(i).wetMass, fuelTanks.get(i).wetMass - dmr * fuelTanks.get(i).dryMass, -twr * fuelTanks.get(i).wetMass};
			canonical.setColumn(2 + i, col);
		}
		
		// Set slack and B columns
		double sm2[][] = {{0, 0, 0},
						  {1, 0, (dmr - 1) * payloadMass},
						  {0, 1, twr * payloadMass}};
		canonical.setSubMatrix(sm2, 0, 2 + fuelTanks.size());
		
		System.out.println(" Simplex: " + (canonical.getRowDimension() - 1) + " constraints"); // TODO Debug
		
		// TODO DEBUG
		/*
		for(int i = 0; i < canonical.getRowDimension(); i++) {
			System.out.print("{ ");
			for(int j = 0; j < canonical.getColumnDimension(); j++) {
				System.out.print(canonical.getEntry(i, j) + "\t");
			}
			System.out.println("}");
		}*/
		
		// Simplex solution of relaxed LP;
		int[] solutionVars;
		LinearSolution simplexSol = LinearSolver.Simplex(canonical);
		canonical = simplexSol.finalMatrix;
		solutionVars = simplexSol.solutionVarList;
		
		// Check for integrality of the variables (ignore the slack variables)
		boolean integral = true; 
		for(int i = 1; i < canonical.getRowDimension(); i++) {
			double result = canonical.getEntry(i, canonical.getColumnDimension() - 1);
			if (Double.compare(Math.floor(result), result) != 0 && solutionVars[i - 1] <= fuelTanks.size()) {
				integral = false;
				break;
			}
		}
		
		// We now have a simplex solution to our relaxed version of the problem.  However, if this solution isn't integral,
		// we're no closer to an actual solution, since the simplex solution is equal to the previously hard-calculated situation.
		// (damn, all that work for nothing!) The solution?  Gomory's Cutting Plane.  We add a cutting plane, then do the whole
		// thing over again (and again... and again...) until integers pop out. (Wow, that sounded dirty)
		
		int row = 0;	// Which row to build the cut out of
		while (!integral){
			// TODO Debug 
			System.out.print("  Result: \n{ ");
			for(int i = 1; i < canonical.getRowDimension(); i++) {
				System.out.print(solutionVars[i - 1] + ":" + canonical.getEntry(i, canonical.getColumnDimension() - 1) + "\t");
			}
			System.out.println("}");
			System.out.print(" Not Integral! New Cut: \n{ "); // TODO Debug
			
			RealMatrix newCanon = new Array2DRowRealMatrix(canonical.getRowDimension() + 1, canonical.getColumnDimension() + 1);
			newCanon.setSubMatrix(canonical.getData(), 0, 0);
			
			newCanon.setColumn(newCanon.getColumnDimension() - 1, newCanon.getColumn(newCanon.getColumnDimension() - 2));	// Slide constant column over one to make room for another slack variable
			
			// Zero out slack variable column
			for (int i = 0; i < newCanon.getRowDimension(); i++) {
				newCanon.setEntry(i, newCanon.getColumnDimension() - 2, 0.0);
			}
			
			// TODO make this better, somehow
			row++;  // Build the cuts out constraints in linear order
			
			// Build Gomory's cut
			double[] cut = new double[newCanon.getColumnDimension()];
			for (int i = 0; i < cut.length - 2; i++) {
				double oldEntry = canonical.getEntry(row, i); 
				cut[i] = Math.floor(oldEntry) - oldEntry;	// Remove the non-fractional part of the constraint
			}
			cut[cut.length - 2] = 1.0;	// New slack variable
			cut[cut.length - 1] = Math.floor(canonical.getEntry(row, canonical.getColumnDimension() - 1)) - canonical.getEntry(row, canonical.getColumnDimension() - 1);	// B vector constant
			
			newCanon.setRow(newCanon.getRowDimension() - 1, cut);
			
			// TODO Debug
			for (int i = 0; i < cut.length; i++) {
				System.out.print(cut[i] + "\t");
			}
			System.out.println("}");
			
			canonical = newCanon;
			
			System.out.println(" Dual Simplex: " + (canonical.getRowDimension() - 1) + " constraints"); // TODO Debug
			
			simplexSol = LinearSolver.DualSimplex(canonical);
			canonical = simplexSol.finalMatrix;
			solutionVars = simplexSol.solutionVarList;
			
			// Check for integrality of the variables (ignore the slack variables)
			integral = true; 
			for(int i = 1; i < canonical.getRowDimension(); i++) {
				double result = canonical.getEntry(i, canonical.getColumnDimension() - 1);
				if (Double.compare(Math.floor(result), result) != 0 && solutionVars[i - 1] <= fuelTanks.size()) {
					integral = false;
					break;
				}
			}
		}
		
		// TODO Debug
		System.out.println(" Done! "); 
		for(int i = 0; i < canonical.getRowDimension(); i++) {
			System.out.print("{ ");
			for(int j = 0; j < canonical.getColumnDimension(); j++) {
				System.out.print(canonical.getEntry(i, j) + "\t");
			}
			System.out.println("}");
		}
		System.out.print(" Solultion Vars: \n{ ");
		for(int i = 0; i < solutionVars.length; i++) {
			System.out.print(solutionVars[i] + "\t");
		}
		System.out.println("}");
		System.out.print("  Result: \n{ ");
		for(int i = 1; i < canonical.getRowDimension(); i++) {
			System.out.print(solutionVars[i - 1] + ":" + canonical.getEntry(i, canonical.getColumnDimension() - 1) + "\t");
		}
		System.out.println("}");
		
		
		int[] result = new int[fuelTanks.size()];
		for(int i = 0; i < solutionVars.length; i++) {
			// Discard slack variables
			if(solutionVars[i] < result.length) {
				result[solutionVars[i]] = (int) canonical.getEntry(i + 1, canonical.getColumnDimension() - 1);
			}
		}
		
		// TODO Debug
		/*
		System.out.print(" Result: \n{ ");
		for(int i = 0; i < result.length; i++) {
			System.out.print(result[i] + "\t");
		}
		System.out.println("}");
		*/
				
		
		return result;
	}
	
	@Override
	public void actionPerformed(ActionEvent event) {
		// Calculate
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
		
		try {
			double minTMR = Double.parseDouble(TWField.getText()) * g0;
			double min_dV = Double.parseDouble(dVField.getText());
			double payloadMass = Double.parseDouble(payloadField.getText());
			
			for(int i = 0; i < engines.length - 1; i++) {
				Engine en = engines[i];
				en.recalculate(minTMR, min_dV, payloadMass);
				
				if (en.enginesRequired > 0) {
					resultTable.getModel().setValueAt(en.enginesRequired, i, 1);
					resultTable.getModel().setValueAt(Math.floor(en.totalFuelTankMass * 100) / 100, i, 2);
					resultTable.getModel().setValueAt(Math.floor(en.totalShipMass * 100) / 100, i, 3);
					resultTable.getModel().setValueAt(Math.floor(en.payloadFraction * 10000) / 10000, i, 4);
				} else {
					for (int j = 1; j < 5; j++) {
						resultTable.getModel().setValueAt(null, i, j);
					}
				}
			}

			if (custMass >= 0) {
				Engine.CUSTOM.recalcCustom(minTMR, min_dV, payloadMass, custMass, custThrust, custISP, custFuel);
			}
			
			if (custMass > 0 && Engine.CUSTOM.enginesRequired > 0) {
				resultTable.getModel().setValueAt(Engine.CUSTOM.enginesRequired, engines.length - 1, 1);
				resultTable.getModel().setValueAt(Math.floor(Engine.CUSTOM.totalFuelTankMass * 100) / 100, engines.length - 1, 2);
				resultTable.getModel().setValueAt(Math.floor(Engine.CUSTOM.totalShipMass * 100) / 100, engines.length - 1, 3);
				resultTable.getModel().setValueAt(Math.floor(Engine.CUSTOM.payloadFraction * 10000) / 10000, engines.length - 1, 4);
			} else {
				for (int j = 1; j < 5; j++) {
					resultTable.getModel().setValueAt(null, engines.length - 1, j);
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