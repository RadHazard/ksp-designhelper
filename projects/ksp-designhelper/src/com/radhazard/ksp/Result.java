package com.radhazard.ksp;

/**
 * A class that holds result data
 */
public class Result {
	private int enginesRequired;
	private double fuelTankMass;
	private double shipMass;
	private double payloadFraction;

	public Result(int enginesRequired, double fuelTankMass, double shipMass, double payloadFraction) {
		this.enginesRequired = enginesRequired;
		this.fuelTankMass = fuelTankMass;
		this.shipMass = shipMass;
		this.payloadFraction = payloadFraction;
	}
	
	public int getEnginesRequired() {
		return enginesRequired;
	}
	public double getFuelTankMass() {
		return fuelTankMass;
	}
	public double getShipMass() {
		return shipMass;
	}
	public double getPayloadFraction() {
		return payloadFraction;
	}
}
