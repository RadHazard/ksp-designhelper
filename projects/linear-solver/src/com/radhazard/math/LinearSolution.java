package com.radhazard.math;

import org.apache.commons.math3.linear.RealMatrix;

/**
 * A solution to a linear optimization problem of in the form of a matrix and a list of solutions
 * 
 * @author RadHazard
 *
 */
public class LinearSolution {
	private RealMatrix finalMatrix;
	private int[] solutionVarList;
	
	LinearSolution(RealMatrix finalMatrix, int[] solutionVarList) {
		this.finalMatrix = finalMatrix;
		this.solutionVarList = solutionVarList;
	}
	
	public RealMatrix getFinalMatrix() {
		return finalMatrix;
	}
	public int[] getSolutionVarList() {
		return solutionVarList;
	}
}
