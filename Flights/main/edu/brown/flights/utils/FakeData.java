package edu.brown.flights.utils;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.brown.flights.types.Airlines;
import edu.brown.flights.types.Passenger;
import edu.brown.flights.types.planes.FlightInstance;
import edu.brown.flights.types.planes.PlaneFlight;
import edu.brown.flights.types.planes.PlaneFlight.PlaneFlightBuilder;

public final class FakeData {

	public static final int UPPER_BOUND = 10000;
	public static final int PER_PLANE = 100;

	public FakeData() { throw new IllegalArgumentException("Cannot instantiate");}

	/*
	 * Dummies up flights
	 */
	public static Map<String, PlaneFlight> dummyFlights(int num){
		Map<String, PlaneFlight> flights = new HashMap<String, PlaneFlight>();
		
		SecureRandom sr = new SecureRandom();
		for (int i = 0; i < num; i++) {
			PlaneFlight pf = fakeFlight(sr);
			flights.put(pf.getCode(), pf);
		}
		
		return flights;
	}

	/*
	 * Dummies up passenger data if DT has none
	 */
	public static Set<Passenger> dummyPassengers(Map<String, PlaneFlight> map) {
	    List<Passenger> fakePeople = new ArrayList<Passenger>();
    	int howmany = (int) (Math.random() * UPPER_BOUND);
    	SecureRandom random = new SecureRandom();
    	for(int i = 0; i < howmany; i++) {
    		fakePeople.add(fakePerson(random));
    	}

	    for(String code : map.keySet()) {
	    	int num = (int) (Math.random() * PER_PLANE);
	    	for(int i = 0; i < num; i++) {
	    		String date = "";
	    		int count = (int) (map.get(code).getInstances().size() * Math.random());
	    		for (FlightInstance inst : map.get(code).getInstances()) {
	    			if (--count == 0) {
	    				date = inst.getSpecs().getDate();
	    				break;
	    			}
	    		}
	    		fakePeople.get((int)((howmany-1)*Math.random())).getBuilder().addFlightOnDate(date, code).build();
	    	}
	    }

		return new HashSet<Passenger>(fakePeople);
	}

	/*
	 * Generates a single fake passenger w/o flights
	 */
	private static Passenger fakePerson(SecureRandom sr) {
		return new Passenger.PassengerBuilder((new BigInteger(25, sr).toString(32)),
				new BigInteger(50, sr).toString(32)).build();
	}

	/*
	 * Generates a single fake flight w/o passengers
	 */
	public static PlaneFlight fakeFlight(SecureRandom sr) {
		String theLine = (int) (2015 + (2 * Math.random())) + "-" + 
				(int) (12 * Math.random()) + 
				"-" + (31 * Math.random()) + ",";
		theLine += Airlines.getRandom().getAbbreviation() + ",";
		theLine += "N" + (int) (100000 * Math.random()) + ",";
		theLine += (int) (999 * Math.random()) + ","; //flightnum
		String origin = fakeCode();
		theLine += origin + ",";
		theLine += getCity(origin) + ",";
		theLine += getState(origin) + ",";
		String dest = fakeCode();
		theLine += dest + ",";
		theLine += getCity(dest) + ",";
		theLine += getState(dest) + ",";
		theLine += (int) (Math.random() * 2400) + ",";
		theLine += (int) (Math.random() * 2400) + ",";
		theLine += "0,0," + (int) (Math.random() * 2400);
		
		PlaneFlightBuilder pfb = new PlaneFlightBuilder(theLine);

		return pfb.build();
	}

	private static String getState(String origin) {
		switch(origin) {
		case "JFK": return "NY";
		case "LGA": return "NY";
		case "EWR": return "NJ";
		case "ATL": return "GA";
		case "BWI": return "MD";
		case "CDG": return "FR";
		case "SFO": return "CA";
		case "LAX": return "CA";
		case "BOS": return "MA";
		case "GBI": return "TX";
		default:
			return "BC";
		}
	}

	private static String getCity(String origin) {
		switch(origin) {
		case "JFK": return "New York";
		case "LGA": return "New York";
		case "EWR": return "Newark";
		case "ATL": return "Atlanta";
		case "BWI": return "Baltimore";
		case "CDG": return "Paris";
		case "SFO": return "San Fransisco";
		case "LAX": return "Los Angeles";
		case "BOS": return "Boston";
		case "GBI": return "Houston";
		default:
			return "Vancouver";
		}
	}

	private static String fakeCode() {
		switch((int)(9 * Math.random())) {
		case 0: return "JFK";
		case 1: return "LGA";
		case 2: return "EWR";
		case 3: return "ATL";
		case 4: return "BWI";
		case 5: return "CDG";
		case 6: return "SFO";
		case 7: return "LAX";
		case 8: return "BOS";
		case 9: return "GBI";
		default:
			return "YVR";
		}
	}

}
