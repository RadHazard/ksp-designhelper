package com.radhazard.ksp.data;

public enum Engine {
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
	
	public final String name;
	public final double mass;
	public final double thrust;
	public final double isp;
	public final Fuel fuel;
	public final double tmr;
	
	private Engine(String name, double mass, double thrust, double isp, Fuel fuel) {
		this.name = name;
		this.mass = mass;
		this.thrust = thrust;
		this.isp = isp;
		this.fuel = fuel;
		this.tmr = thrust/mass;
	}
}
