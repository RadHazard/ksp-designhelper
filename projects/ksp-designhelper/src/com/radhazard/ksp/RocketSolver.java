package com.radhazard.ksp;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

import com.radhazard.ksp.data.Data;
import com.radhazard.ksp.data.Engine;
import com.radhazard.ksp.data.Fuel;
import com.radhazard.ksp.data.Tank;
import com.radhazard.math.LinearSolution;
import com.radhazard.math.LinearSolver;

public class RocketSolver {
	private static final double g0 = 9.82;	// Dem kerbals and their silly constants.  This is the exact value so don't "fix" it
	
	/**
	 * Recalculate ship parameters given the new TMR, dV, payload mass, and engine
	 * @param engine
	 * @param minTMR
	 * @param min_dV
	 * @param payloadMass
	 * @return
	 */
	public static Result recalculate(double minTMR, double min_dV, double payloadMass, Engine engine) {
		return recalculate(minTMR, min_dV, payloadMass, engine.mass, engine.thrust, engine.isp, engine.fuel);
	}
	
	/**
	 * Recalculate ship parameters given the new TMR, dV, payload mass, and engine stats
	 * @param minTMR
	 * @param min_dV
	 * @param payloadMass
	 * @param engineMass
	 * @param engineThrust
	 * @param engineIsp
	 * @param engineFuel
	 * @return
	 */
	public static Result recalculate(double minTMR, double min_dV, double payloadMass, double engineMass, double engineThrust, double engineIsp, Fuel engineFuel) {
		double minMassRatio = Math.exp(min_dV / (g0 * engineIsp));
		
		double maxLoad = (engineThrust / minTMR) - engineMass;		// Maximum load each engine can carry while still retaining the TWR.
		double dryMass = (maxLoad + engineMass) / minMassRatio;		// Calculate dry mass with the inverted rocket equation
		double fuelMass = maxLoad + engineMass - dryMass;			// Mass of the fuel is equal to total mass minus dry mass
		
		// TODO Modify this for MILPSolver
		double tankMassRatio = Data.getTanksByFuel(engineFuel)[0].massRatio;	// Just pick the first tank for now
		double tankMass = fuelMass / tankMassRatio; 
		double maxPayload = dryMass - (engineMass + tankMass);		// Maximum payload is the dry mass minus engines and empty fuel tanks.
		
		int enginesRequired = (int)Math.ceil(payloadMass / maxPayload);
		
		if (enginesRequired > 0) { // sanity check
			int[] numTanks = MILPSolver(payloadMass, engineMass, engineThrust, minMassRatio, minTMR, Data.getTanksByFuel(engineFuel));
			if (numTanks == null) {
				// TODO handle this more gracefully
				throw new RuntimeException("MILPSolver died (RIP MILPSolver)");
			}
		}
		
		// Correct total fuel mass
		// Rocket equation, solved for fuel mass.
		double fuelTankMass = (payloadMass + enginesRequired * engineMass) * (minMassRatio - 1) / (1 - minMassRatio / tankMassRatio);
		double totalShipMass = (enginesRequired * engineMass + fuelTankMass + payloadMass);
		double payloadFraction = payloadMass / totalShipMass;
		
		if (enginesRequired <= 0) {
			return null;
		} else {
			return new Result(enginesRequired, fuelTankMass, totalShipMass, payloadFraction);
		}
	}
	
	/**
	 * A simple cutting-plane algorithm for solving Mixed-Integer Linear Programming.
	 * Implements Simplex followed by Gomory's Cut, repeated until an integral solution is found.
	 * 
	 * Unrelated to either dimunitive adolescent equines in the possessive or matriarchs of suitable mating quality
	 * @return 
	 */
	public static int[] MILPSolver(double payloadMass, double engineMass, double engineThrust, double dmr, double twr, Tank[] tanks) {
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
		
		RealMatrix canonical = new Array2DRowRealMatrix(3, tanks.length + 5);
		
		// Set first row and engine var column
		double sm1[][] = {{1, -engineMass},
						  {0, (1 - dmr) * engineMass},
						  {0, engineThrust - twr * engineMass}};
		canonical.setSubMatrix(sm1, 0, 0);
		
		// Set each fuel tank column
		for (int i = 0; i < tanks.length; i++) {
			double col[] = {-tanks[i].wetMass, tanks[i].wetMass - dmr * tanks[i].dryMass, -twr * tanks[i].wetMass};
			canonical.setColumn(2 + i, col);
		}
		
		// Set slack and B columns
		double sm2[][] = {{0, 0, 0},
						  {1, 0, (dmr - 1) * payloadMass},
						  {0, 1, twr * payloadMass}};
		canonical.setSubMatrix(sm2, 0, 2 + tanks.length);
		
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
		canonical = simplexSol.getFinalMatrix();
		solutionVars = simplexSol.getSolutionVarList();
		
		// Check for integrality of the variables (ignore the slack variables)
		boolean integral = true; 
		for(int i = 1; i < canonical.getRowDimension(); i++) {
			double result = canonical.getEntry(i, canonical.getColumnDimension() - 1);
			if (Double.compare(Math.floor(result), result) != 0 && solutionVars[i - 1] <= tanks.length) {
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
			// TODO == Debug == 
			System.out.print("  Result: \n{ ");
			for(int i = 1; i < canonical.getRowDimension(); i++) {
				System.out.print(solutionVars[i - 1] + ":" + canonical.getEntry(i, canonical.getColumnDimension() - 1) + "\t");
			}
			System.out.println("}");
			System.out.print(" Not Integral! New Cut: \n{ ");
			// TODO == End Debug ==
			
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
			
			// TODO == Debug ==
			for (int i = 0; i < cut.length; i++) {
				System.out.print(cut[i] + "\t");
			}
			System.out.println("}");
			// TODO == End Debug ==
			
			canonical = newCanon;
			
			System.out.println(" Dual Simplex: " + (canonical.getRowDimension() - 1) + " constraints"); // TODO Debug
			
			simplexSol = LinearSolver.DualSimplex(canonical);
			canonical = simplexSol.getFinalMatrix();
			solutionVars = simplexSol.getSolutionVarList();
			
			// Check for integrality of the variables (ignore the slack variables)
			integral = true; 
			for(int i = 1; i < canonical.getRowDimension(); i++) {
				double result = canonical.getEntry(i, canonical.getColumnDimension() - 1);
				if (Double.compare(Math.floor(result), result) != 0 && solutionVars[i - 1] <= tanks.length) {
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
		
		
		int[] result = new int[tanks.length];
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
}
