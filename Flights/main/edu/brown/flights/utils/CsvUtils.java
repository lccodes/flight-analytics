package edu.brown.flights.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import edu.brown.flights.types.Airport;
import edu.brown.flights.types.Passenger;
import edu.brown.flights.types.World;
import edu.brown.flights.types.World.WorldBuilder;
import edu.brown.flights.types.planes.FlightInstance;
import edu.brown.flights.types.planes.PlaneFlight;
import edu.brown.flights.types.planes.PlaneFlight.PlaneFlightBuilder;

public class CsvUtils {

    private static final String[] shitThatMatters =
            new String[]{"FlightDate","Carrier","TailNum","FlightNum","Origin","OriginCityName","OriginState",
        "Dest","DestCityName","DestState","CRSDepTime","DepTime","CRSArrTime","ArrTime","Cancelled",
        "CancellationCode","Diverted","CRSElapsedTime"};

    public static World getStateFromCsvFile(String path) throws IOException {
    	System.out.println(path);

        WorldBuilder worldBuilder = new WorldBuilder(new HashMap<String, PlaneFlight>(), new HashSet<Passenger>(),
                new HashSet<Airport>());
        BufferedReader r = new BufferedReader(new FileReader(path));
        String firstLine = r.readLine();
        List<Integer> columnsToStore = new ArrayList<Integer>(shitThatMatters.length);
        String[] potentialFields = firstLine.split(",");
        for (int i = 0 ; i < potentialFields.length; i++) {
            for (String s : shitThatMatters) {
                if (potentialFields[i].equals(s)) {
                    columnsToStore.add(i);
                    break;
                }
            }
        }

        String line = r.readLine();
        while (line != null) {
            StringBuilder sb = new StringBuilder();
            potentialFields = line.split(",");
            for (int i = 0; i < potentialFields.length; i++) {
                if (columnsToStore.contains(i)) {
                    sb.append(potentialFields[i] + ",");
                }
            }
            String lineInfo = sb.toString();
            //System.out.println(lineInfo);
            try {
                PlaneFlightBuilder flightBuilder = new PlaneFlightBuilder(lineInfo);
                PlaneFlight thisFlight = flightBuilder.build();
                worldBuilder.addFlight(thisFlight);
                line = r.readLine();
            } catch (Exception e) {
                line = r.readLine();
            }
        }
        r.close();
        
        System.out.println("[SUCCESS] World loaded \n");

        return worldBuilder.build();
    }
    
    
    public static void writeWorldToCsv(World world, String flightsPath, String peoplePath) throws Exception {
    	BufferedWriter flightsTable = new BufferedWriter(new FileWriter(flightsPath));
    	BufferedWriter peopleTable = new BufferedWriter(new FileWriter(peoplePath));
    	
    	flightsTable.write("Flight Code,Date,Origin,Destination,Departing Time,Arrival Time,Carrier,Plane Type,Capacity,Passenger Count,Passenger UTIs-> \n");
    	for (Entry<String, PlaneFlight> e : world.getCodesToFlightsMap().entrySet()) {
    		String flightCode = e.getKey();
    		PlaneFlight theFlight = e.getValue();
    		for (FlightInstance flight : theFlight.getInstances()) {
    			String toWrite = flightCode + "," + flight.getSpecs().getDate() + "," + flight.getRoute().getOrigin().getCode() + "," 
    					+ flight.getRoute().getDestination().getCode() + "," + flight.getRoute().getDepartureTime() + "," + flight.getRoute().getArrivalTime()
    					+ "," + flight.getCarrier() + "," + flight.getSpecs().getPlane().getType().toString() + "," + flight.getSpecs().getPlane().getCapacity() 
    					+ "," + flight.getSpecs().getPassengers().size();
    			for (Long p : flight.getSpecs().getPassengers()) {
    				toWrite += "," + p;
    			}
    			flightsTable.write(toWrite + "\n");
    		}
    	}
    	
    	peopleTable.write("Universal Traveller Identifier (UTI),Last Name,First Name,Flight 1 Date,Flight 1 Code..-> \n");
    	for (Passenger p : world.getPassengers()) {
    		String toWrite = p.getUTI() + "," + p.getLastName() + "," + p.getFirstName();
    		for (Entry<String, Set<String>> e : p.getDateToFlights().entrySet()) {
    			for (String f : e.getValue()) {
    				toWrite += "," + e.getKey() + "," + f; //e,g. "2011-09-29,UA932"
    			}
    		}
    		peopleTable.write(toWrite + "\n");
    		
    	}
    	
    	flightsTable.close();
    	peopleTable.close();
    	
    }

}
