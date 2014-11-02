package com.radhazard.ksp.data;

import java.util.Vector;

/**
 * Different fuel types
 */
public enum Fuel {
	LFO("LFO", 5),
	Liquid("Liquid", 5),
	Xenon("Xenon", 0.1);
	
	public final String name;
	public final double density;
	
	Fuel(String name, double density) {
		this.name = name;
		this.density = density;
	}
	
	public String toString() {
		return name;
	}
};
