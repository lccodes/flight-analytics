package edu.brown.flights.transforms;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import edu.brown.flights.types.Route;
import edu.brown.flights.types.World;
import edu.brown.flights.types.planes.FlightInstance;
import edu.brown.flights.types.planes.FlightRecord;
import edu.brown.flights.types.planes.Plane;
import edu.brown.flights.types.planes.PlaneFlight;

public class Views {

	/*
	 * Constructs a tailNum centric view of the world
	 */
	public static Map<String, Set<FlightInstance>> getByTail(World w) {
	    Map<String, PlaneFlight> allFlights = w.getCodesToFlightsMap();
	    Map<String, Set<FlightInstance>> toReturn = new HashMap<String, Set<FlightInstance>>();

	    for (Entry<String, PlaneFlight> e : allFlights.entrySet()) {
	        Set<FlightInstance> instances = e.getValue().getInstances();
	        for (FlightInstance instance : instances) {
	            Plane thePlane = instance.getSpecs().getPlane();
	            if (!toReturn.containsKey(thePlane.getTailNum())) {
	                toReturn.put(thePlane.getTailNum(), new HashSet<FlightInstance>());
	            }
	            toReturn.get(thePlane.getTailNum()).add(instance);
	        }
	    }

	    return toReturn;
	}

	/*
	 * Constructs a plane centric view with planeflights
	 */
	public static Map<Plane, Set<PlaneFlight>> getFlightsByTail(World w) {
		Map<Plane, Set<PlaneFlight>> theMap = new HashMap<Plane, Set<PlaneFlight>>();
		for (PlaneFlight f : w.getCodesToFlightsMap().values()) {
			for (Set<FlightRecord> records : f.getRoutesAndRuns().values()) {
				for (FlightRecord r : records) {
					if (theMap.containsKey(r.getPlane())) {
						theMap.get(r.getPlane()).add(f);
					} else {
						Set<PlaneFlight> toAdd = new HashSet<PlaneFlight>();
						toAdd.add(f);
						theMap.put(r.getPlane(), toAdd);
					}
				}
			}
		}

		return theMap;
	}
	
	/*
	 * Gets all FlightRecords in a flattened view with their routes
	 */
	public static Map<FlightRecord, Route> getAllFlights(World w) {
		Map<FlightRecord, Route> flights = new HashMap<FlightRecord, Route>(w.getCodesToFlightsMap().size()*2);
		
		for (PlaneFlight flight : w.getCodesToFlightsMap().values()) {
			for (Entry<Route, Set<FlightRecord>> combo : flight.getRoutesAndRuns().entrySet()) {
				for (FlightRecord r : combo.getValue()) {
					flights.put(r, combo.getKey());
				}
			}
		}
		
		return flights;
	}
	
	/*
	 * Constructs a route centric view
	 */
	public static Map<Route, Set<FlightRecord>> viewByRoutes(World w) {
		Map<Route, Set<FlightRecord>> theMap = new HashMap<Route, Set<FlightRecord>>();
		
		for (PlaneFlight flight : w.getCodesToFlightsMap().values()) {
			for (Entry<Route, Set<FlightRecord>> combo : flight.getRoutesAndRuns().entrySet()) {
				if (theMap.containsKey(combo.getKey())) {
					Set<FlightRecord> records = theMap.get(combo.getKey());
					records.addAll(combo.getValue());
					theMap.put(combo.getKey(), records);
				} else {
					theMap.put(combo.getKey(), combo.getValue());
				}
			}
		}
		
		return theMap;
	}
}
