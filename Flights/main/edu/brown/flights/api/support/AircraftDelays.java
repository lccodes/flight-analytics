package edu.brown.flights.api.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Optional;

import edu.brown.flights.transforms.Views;
import edu.brown.flights.types.InstanceComparator;
import edu.brown.flights.types.World;
import edu.brown.flights.types.planes.FlightInstance;
import edu.brown.flights.utils.TimeHelpers;

/**
 * Private api support for the public api
 * @author lcamery
 *
 */
public final class AircraftDelays {
	
	/*
	 * Gets the propogation effect of an aircraft being delayed
	 * @param world : the state of the world
	 * @param minutesLate : the aircraft's delay
	 * @param turnAroundMinutes : how much time the plane needs to get off the ground again
	 * @param code : flight code e.g. AA1
	 * @optional date : when the flight is 
	 * @optional hour : when the flight is
	 */
	public static Map<FlightInstance, Integer> getAirplaneDelayMaster(World world,
			int minutesLate,
			int turnAroundMinutes,
			String code,
			Optional<String> date,
			Optional<Integer> hour) {
		Map<FlightInstance, Integer> delayedFlights = new HashMap<FlightInstance, Integer>();
		if (world.getCodesToFlightsMap().containsKey(code)) {
			Optional<FlightInstance> isTheDelay = 
					world.getCodesToFlightsMap().get(code)
						.getNextFlight(date.or(World.getCurrentDate()),
								hour.or(World.getCurrentTime()));
			if (isTheDelay.isPresent()) {
				FlightInstance theDelay = isTheDelay.get();
				Set<FlightInstance> instances = Views.getByTail(world).get(theDelay.getSpecs().getPlane().getTailNum());
				ArrayList<FlightInstance> sortedSet = new ArrayList<FlightInstance>(instances);
				Collections.sort(sortedSet, new InstanceComparator());
				
				boolean start = false;
				int currentDelay = minutesLate;
				FlightInstance last = theDelay;
				for (FlightInstance f : sortedSet){
					if (start) {
						System.out.println(f.getRoute().getDepartureTime() + " " + last.getRoute().getArrivalTime());
						int diff = TimeHelpers.minutesBetween(
								TimeHelpers.addMinutes(last.getRoute().getArrivalTime(), currentDelay),
								f.getRoute().getDepartureTime());
						System.out.println(diff);
						if ((!f.getSpecs().getDate().equals(last.getSpecs().getDate()))
								|| diff >= turnAroundMinutes) {
							return delayedFlights;
						} else {
							currentDelay = turnAroundMinutes - diff;
							last = f;
							delayedFlights.put(f, currentDelay);
						}
					} else {
						if (f.equals(theDelay)) {
							start = true;
						}
					}
				}
			}
			
		}
		
		return delayedFlights;
	}
	
	/*
	 * Gets the propogation effect of several planes being delayed
	 * @param world : the state of the world
	 * @param minutesLate : the aircraft's delay
	 * @param turnAroundMinutes : how much time the plane needs to get off the ground again
	 * @param code : flight codes e.g. AA1
	 * @optional date : when the flight is 
	 * @optional hour : when the flight is
	 */
	public static Map<FlightInstance, Integer> getMultipleAirplaneDelys(World world,
			int minutesLate,
			int turnAroundMinutes,
			Set<String> codes,
			Optional<String> date,
			Optional<Integer> hour) {
		Map<FlightInstance, Integer> delayedFlights = new HashMap<FlightInstance, Integer>();
		for (String c : codes) {
			if (world.getCodesToFlightsMap().containsKey(c)) {
				delayedFlights.putAll(getAirplaneDelayMaster(world, minutesLate, turnAroundMinutes, c, date, hour));
			}
		}
		
		return delayedFlights;
	}

}
