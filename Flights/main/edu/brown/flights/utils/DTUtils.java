package edu.brown.flights.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.spark.api.java.JavaRDD;

import com.google.common.base.Optional;

import edu.brown.flights.types.Airport;
import edu.brown.flights.types.Passenger;
import edu.brown.flights.types.World;
import edu.brown.flights.types.Passenger.PassengerBuilder;
import edu.brown.flights.types.World.WorldBuilder;
import edu.brown.flights.types.planes.PlaneFlight;
import edu.brown.flights.types.planes.PlaneFlight.PlaneFlightBuilder;

public final class DTUtils {

	/*
	 * Cannot instantiate; use utility methods
	 */
	public DTUtils() {
		throw new UnsupportedOperationException();
	}

	/*
	 * Creates a world from RDDs
	 * @param flights RDD [matrix]
	 * @param Optional -> passengers RDD
	 */
	public static World rddToWorld(JavaRDD<JavaRDD<Object>> flights,
			Optional<JavaRDD<JavaRDD<Object>>> passengers) {

		//Components of World
		Map<String, PlaneFlight> flightMap = new HashMap<String, PlaneFlight>();
		Set<Passenger> passengerManifest = new HashSet<Passenger>();
		Set<Airport> airportList = new HashSet<Airport>();
		WorldBuilder worldBuilder = new WorldBuilder(flightMap, passengerManifest, airportList);

		//Walk through the columns in flights
		for(JavaRDD<Object> row : flights.collect()) {
			List<Object> collect = row.collect();

			StringBuilder flightInfoSB = new StringBuilder();

			int[] indicesThatMatter = new int[]{5,8,9,10,14,15,23,24,29,30,40,41,47,48,49,50,51};
			for (int index : indicesThatMatter) {
			    if (index == 15) { //e.g. New York, NY -> [New York, NY] -> "New York,NY"
			        String[] temp = collect.get(index).toString().split(", ");
			        if (temp.length != 2) {
			            throw new IllegalArgumentException("Something is configured wrong w/the import options");
			        }
			        flightInfoSB.append(temp[0] + "," + temp[1]);
			    } else {
			        flightInfoSB.append(collect.get(index).toString() + ",");
			    }

            }
			String flightInfo = flightInfoSB.toString();
			PlaneFlight thisFlight = (new PlaneFlightBuilder(flightInfo)).build();

			worldBuilder.addFlight(thisFlight);

		}

		//Passengers resolution
		if (passengers.isPresent()) {
			worldBuilder.addPassengers(rddToPassengers(passengers.get()));
		} else {
			worldBuilder.addPassengers(FakeData.dummyPassengers(worldBuilder.build().getCodesToFlightsMap()));
		}

		return worldBuilder.build();
	}

	/*
	 * Converts DT data to passenger list
	 */
	@SuppressWarnings("unchecked")
	private static Set<Passenger> rddToPassengers(
			JavaRDD<JavaRDD<Object>> javaRDD) {
		Set<Passenger> pass = new HashSet<Passenger>();
		for (JavaRDD<Object> row : javaRDD.collect()) {
			List<Object> theRow = row.collect();
			PassengerBuilder build = new PassengerBuilder((String) theRow.get(0), (String) theRow.get(1));
			List<String> flights = (List<String>) theRow.get(2);
			List<String> dates = (List<String>) theRow.get(3);
			if (flights.size() != dates.size()) {
				throw new IllegalArgumentException();
			}

			//Ensured that they're the same length
			for (int i = 0; i < dates.size(); i++) {
				build.addFlightOnDate(flights.get(i), dates.get(i));
			}
			
			//Put the completed passenger obj in
			pass.add(build.build());
		}

		return pass;
	}

}
