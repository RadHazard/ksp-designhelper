package com.radhazard.math;

import org.apache.commons.math3.linear.RealMatrix;

/**
 * A basic linear optimization solver
 * 
 * @author Rad Hazard
 *
 */
public class LinearSolver {
	/**
	 * A simplex solver for linear optimization problems.
	 * Takes a canonical matrix in the form
	 *
	 * [ 1 -c 0 ]
	 * [ 0  A B ]
	 * 
	 * Where c is the cost row vector, A is the constraint matrix, and B is the constant column vector 
	 * 
	 * @param canonical
	 * @return solution
	 */
	public static LinearSolution Simplex(RealMatrix canonical) {
		RealMatrix workingMatrix = canonical.copy();
		
		// Set the solution variables as the slack variables initially
		int[] solutionVars = new int[workingMatrix.getRowDimension() - 1]; 
		for (int i = 0; i < solutionVars.length; i++) {
			solutionVars[i] = workingMatrix.getColumnDimension() - workingMatrix.getRowDimension() + i;
		}
		
		// Check all columns except the last (B vector)
		// Pick the leftmost column with a negative value as the pivot column, because it's easy.
		int pivotColNum = -1;
		for (int i = 0; i < workingMatrix.getColumnDimension() - 1; i++) {
			if (Double.compare(workingMatrix.getEntry(0, i), 0.0) < 0) {
				pivotColNum = i;
				break;
			}
		}
		
		// Main loop
		while(pivotColNum >= 0) {
			// Check for negative values in the pivot column.  If they're all negative, bad things happen
			boolean problem = true;
			for (int i = 1; i < workingMatrix.getRowDimension(); i++) {
				if (Double.compare(workingMatrix.getEntry(i, pivotColNum), 0.0) > 0) {
					problem = false;
					break;
				}
			}
			
			if (problem) throw new RuntimeException("All pivots were negative when attempting to use Simplex");
			
			//Chose a pivot value
			int pivotRowNum = -1;
			double bestRank = Double.POSITIVE_INFINITY;
			
			for (int i = 1; i < workingMatrix.getRowDimension(); i++) {
				double newRank = workingMatrix.getEntry(i, workingMatrix.getColumnDimension() - 1) / workingMatrix.getEntry(i, pivotColNum);
				if (Double.compare(newRank, bestRank) < 0 && workingMatrix.getEntry(i, pivotColNum) > 0) pivotRowNum = i; // We chose poorly
			}
			
			// We have a new solution variable
			solutionVars[pivotRowNum - 1] = pivotColNum;
			
			// Zero all the rows except the pivot row			
			RealMatrix pivotRow = workingMatrix.getRowMatrix(pivotRowNum);
			double pivot = workingMatrix.getEntry(pivotRowNum, pivotColNum);
			for (int i = 0; i < workingMatrix.getRowDimension(); i++) {
				if (i == pivotRowNum) {
					// divide the pivot row by the pivot
					workingMatrix.setRowMatrix(i, pivotRow.scalarMultiply(1/pivot));
				} else {
					// add a multiple of the pivot row to the current row
					double mult = (-workingMatrix.getEntry(i, pivotColNum) / pivot);
					RealMatrix curRow = workingMatrix.getRowMatrix(i);
					workingMatrix.setRowMatrix(i, curRow.add(pivotRow.scalarMultiply(mult)));
				}
			}
	
			// Repeat until all top values are positive
			pivotColNum = -1;
			for (int i = 0; i < workingMatrix.getColumnDimension() - 1; i++) {
				if (Double.compare(workingMatrix.getEntry(0, i), 0.0) < 0) {
					pivotColNum = i;
					break;
				}
			}
		}
		
		return new LinearSolution(workingMatrix, solutionVars);
	}
	
	/**
	 * A dual simplex algorithm for linear optimization problems.
	 * Takes a canonical matrix in the form
	 *
	 * [ 1 -c  0 ]
	 * [ 0  A  B ]
	 * 
	 * Where c is the cost row vector, A is the constraint matrix
	 * (including slack variables), and B is the constant column vector
	 * 
	 * @param canonical
	 * @return solution
	 */
	public static LinearSolution DualSimplex(RealMatrix canonical) {
		RealMatrix workingMatrix = canonical.copy();
		
		// Set the solution variables as the slack variables initially
		int[] solutionVars = new int[workingMatrix.getRowDimension() - 1]; 
		for (int i = 0; i < solutionVars.length; i++) {
			solutionVars[i] = workingMatrix.getColumnDimension() - workingMatrix.getRowDimension() + i;
		}
		
		// Check all rows except the first (Cost vector)
		// Pick the topmost row with a negative value as the pivot row, because it's easy.
		int pivotRowNum = -1;
		for (int i = 1; i < workingMatrix.getRowDimension(); i++) {
			if (Double.compare(workingMatrix.getEntry(i, workingMatrix.getColumnDimension() - 1), 0.0) < 0) {
				pivotRowNum = i;
				break;
			}
		}
		

		// Main loop
		while(pivotRowNum >= 0) {
			// TODO == Debug ==
			System.out.println("  Dual Simplex Pivot: Row " + (pivotRowNum + 1)); // TODO Debug
			for(int i = 0; i < workingMatrix.getRowDimension(); i++) {
				System.out.print("{ ");
				for(int j = 0; j < workingMatrix.getColumnDimension(); j++) {
					System.out.print(workingMatrix.getEntry(i, j) + "\t");
				}
				System.out.println("}");
			}
			// TODO == End debug ==
			
			// Check for positive values in the pivot row.  If they're all positive, bad things happen
			boolean problem = true;
			for (int i = 0; i < workingMatrix.getColumnDimension() - 1; i++) {
				if (Double.compare(workingMatrix.getEntry(pivotRowNum, i), 0.0) < 0) {
					problem = false;
					break;
				}
			}
			
			// PANIC
			if (problem) throw new RuntimeException("All pivots were positive when attempting to use Dual Simplex");
			
			//Chose a pivot value
			int pivotColNum = -1;
			double bestRank = Double.POSITIVE_INFINITY;
			
			for (int i = 0; i < workingMatrix.getColumnDimension() - 1; i++) {
				double newRank = -workingMatrix.getEntry(0, i) / workingMatrix.getEntry(pivotRowNum, i);
				if (Double.compare(newRank, bestRank) < 0 && workingMatrix.getEntry(pivotRowNum, i) < 0) pivotColNum = i; // We chose poorly
			}
			
			// We have a new solution variable
			solutionVars[pivotRowNum - 1] = pivotColNum;
			
			// Zero all the rows except the pivot row		
			RealMatrix pivotRow = workingMatrix.getRowMatrix(pivotRowNum);
			double pivot = workingMatrix.getEntry(pivotRowNum, pivotColNum);
			for (int i = 0; i < workingMatrix.getRowDimension(); i++) {
				if (i == pivotRowNum) {
					// divide the pivot row by the pivot
					workingMatrix.setRowMatrix(i, pivotRow.scalarMultiply(1/pivot));
				} else {
					// add a multiple of the pivot row to the current row
					double mult = (-workingMatrix.getEntry(i, pivotColNum) / pivot);
					RealMatrix curRow = workingMatrix.getRowMatrix(i);
					workingMatrix.setRowMatrix(i, curRow.add(pivotRow.scalarMultiply(mult)));
				}
			}
	
			// Repeat until all rightmost values are positive
			pivotRowNum = -1;
			for (int i = 1; i < workingMatrix.getRowDimension(); i++) {
				if (Double.compare(workingMatrix.getEntry(i, workingMatrix.getColumnDimension() - 1), 0.0) < 0) {
					pivotRowNum = i;
					break;
				}
			}
		}
		
		return new LinearSolution(workingMatrix, solutionVars);
	}
}