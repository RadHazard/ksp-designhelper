package com.radhazard.ksp;

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
	
	final Fuel fuel;
	final double capacity;
	final double dryMass;
	final double wetMass;
	final double massRatio;
	
	Tank(Fuel fuel, double capacity, double dryMass) {
		this.fuel = fuel;
		this.capacity = capacity;
		this.dryMass = dryMass;
		this.wetMass = dryMass + (capacity * fuel.density);
		this.massRatio = (capacity * fuel.density) / dryMass; 
	}
}