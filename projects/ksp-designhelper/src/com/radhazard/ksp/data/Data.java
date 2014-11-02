package com.radhazard.ksp.data;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Miscellaneous data about the fuel tanks 
 */
public class Data {
	private static HashMap<Fuel, Tank[]> fuelMap = new HashMap<>();
	
	static {
		// Initialized the fuel tank map
		for(Fuel fuel : Fuel.values()) {
			ArrayList<Tank> tanks = new ArrayList<>();
			
			for (Tank tank : Tank.values()) {
				if (tank.fuel == fuel) {
					if (tanks.size() == 0) {
						tanks.add(tank);
					} else {
						// Insertion sort by mass ratio, and then capacity if mass ratio is equal
						int i = 0;
						while(i < tanks.size() && tanks.get(i).massRatio > tank.massRatio) i++;
						while(i < tanks.size() && tanks.get(i).massRatio == tank.massRatio && tanks.get(i).capacity > tank.capacity) i++;
						tanks.add(i, tank);
					}
				}
			}
			
			fuelMap.put(fuel, (Tank[])tanks.toArray());
		}
	}
	
	public static Tank[] getTanksByFuel(Fuel fuel) {
		return fuelMap.get(fuel);
	}
}