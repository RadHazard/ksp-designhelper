package com.radhazard.ksp.data;

/**
 * Various fuel tanks
 */
public enum Tank {
	OscarB(Fuel.LFO, 12.735, 0.015),
	Round8(Fuel.LFO, 22.2, 0.025),
	FL_T100(Fuel.LFO, 100, 0.0625),
	MK1(Fuel.Liquid, 150, 0.35),
	MK2(Fuel.Liquid, 160, 0.2),
	Xenon(Fuel.Xenon, 700, 0.05),
	XenonRadial(Fuel.Xenon, 400, 0.03);
	
	public final Fuel fuel;
	public final double capacity;
	public final double dryMass;
	public final double wetMass;
	public final double massRatio;
	
	private Tank(Fuel fuel, double capacity, double dryMass) {
		this.fuel = fuel;
		this.capacity = capacity;
		this.dryMass = dryMass;
		this.wetMass = dryMass + (capacity * fuel.density);
		this.massRatio = (capacity * fuel.density) / dryMass; 
	}
}