package com.radhazard.ksp;

import java.util.Vector;

/**
 * Different fuel types
 */
enum Fuel {
	LFO("LFO", 5),
	Liquid("Liquid", 5),
	Xenon("Xenon", 0.1);
	
	final String name;
	final double density;
	Vector<Tank> fuelTanks;  // TODO This is really ugly 
	
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
			while(i < fuelTanks.size() && fuelTanks.get(i).massRatio > tank.massRatio) i++;
			while(i < fuelTanks.size() && fuelTanks.get(i).massRatio == tank.massRatio && fuelTanks.get(i).capacity > tank.capacity) i++;
			fuelTanks.add(i, tank);
		}
	}
	
	public String toString() {
		return name;
	}
};
