package edu.brown.flights.tests;

import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.Test;

import edu.brown.flights.types.Airlines;
import edu.brown.flights.types.Airport;
import edu.brown.flights.types.Passenger;
import edu.brown.flights.types.World;
import edu.brown.flights.types.Passenger.PassengerBuilder;
import edu.brown.flights.types.planes.FlightInstance;
import edu.brown.flights.types.planes.PlaneFlight;
import edu.brown.flights.utils.CsvUtils;

public class PassengerTests {

    @Test
    public void passengerFlightLookupSmokeTest() throws Exception {
    	System.out.println("Passenger stuff (and kinda unrelated stuff) smoke test is now running");

        World world = null;
        try {
            world = CsvUtils.getStateFromCsvFile("/Users/crobotham/Downloads/3_flight_test.csv");
        } catch (Exception e) {
            return;
        }
        assertTrue(world != null);
        Passenger tWinky = (new PassengerBuilder("Tyler", "Winklevoss")).addFlightOnDate("2001-09-11", "AA1")
        		.addFlightOnDate("2001-09-11", "AA5").build();
        world.getBuilder().addPassenger(tWinky);

        PlaneFlight jfkToLax = world.getCodesToFlightsMap().get("AA1");
        assertTrue(jfkToLax.getInstances().size() == 1);
        
        FlightInstance current = null;
        for (edu.brown.flights.types.planes.FlightInstance f : jfkToLax.getInstances()) {
            current = f;
        }
        assertTrue(current != null);
        assertTrue(current.getCarrier() == Airlines.AMERICAN);
        assertTrue(current.getCode().equals("AA1"));
        assertTrue(current.getRoute().getDepartureTime() == 900);
        assertTrue(current.getRoute().getArrivalTime() == 1230);
        assertTrue(current.getRoute().getOrigin().getCode().equals("JFK")
                && current.getRoute().getDestination().getCode().equals("LAX")
                && current.getSpecs().getDate().equals("2001-09-11")
                && current.getSpecs().getFlightID().equals("AA1")
                && current.getSeatsLeft() == 199);
        
        
        Set<FlightInstance[]> betterFlights = tWinky.getFasterRoutes(current, 181, world);
        assertTrue(betterFlights.size() == 2);
        for (FlightInstance[] itinerary : betterFlights) {
            assertTrue(itinerary.length == 1 || itinerary.length == 2);
        	if (itinerary.length == 1) {
            	FlightInstance f = itinerary[0];
            	assertTrue(f.getCarrier() == Airlines.AMERICAN);
            	assertTrue(f.getCode().equals("AA4"));
            	Airport origin = f.getRoute().getOrigin();
            	assertTrue(origin.getCode().equals("JFK"));
            	assertTrue(origin.getCity().equals("New York"));
            	assertTrue(origin.getState().equals("NY"));
            	assertTrue(origin.getCountry().equals("USA"));
            	Airport destination = f.getRoute().getDestination();
            	assertTrue(destination.getCode().equals("LAX"));
            	assertTrue(destination.getCity().equals("Los Angeles"));
            	assertTrue(destination.getState().equals("CA"));
            	assertTrue(destination.getCountry().equals("USA"));
            	assertTrue(f.getSeatsLeft() == 200);
            	assertTrue(f.getRoute().getDepartureTime() == 1000);
            	assertTrue(f.getRoute().getArrivalTime() == 1330);
            	assertTrue(f.getSpecs().getDate().equals("2001-09-11"));
            	assertTrue(f.getSpecs().getPassengers().size() == 0);
            	assertTrue(f.getRoute().getFlightTime() == 390);
            } else {
            	FlightInstance f = itinerary[0];
            	assertTrue(f.getCarrier() == Airlines.AMERICAN);
            	assertTrue(f.getCode().equals("AA2"));
            	Airport origin = f.getRoute().getOrigin();
            	assertTrue(origin.getCode().equals("JFK"));
            	assertTrue(origin.getCity().equals("New York"));
            	assertTrue(origin.getState().equals("NY"));
            	assertTrue(origin.getCountry().equals("USA"));
            	Airport destination = f.getRoute().getDestination();
            	assertTrue(destination.getCode().equals("ORD"));
            	assertTrue(destination.getCity().equals("Chicago"));
            	assertTrue(destination.getState().equals("IL"));
            	assertTrue(destination.getCountry().equals("USA"));
            	assertTrue(f.getSeatsLeft() == 200);
            	assertTrue(f.getRoute().getDepartureTime() == 1000);
            	assertTrue(f.getRoute().getArrivalTime() == 1140);
            	assertTrue(f.getSpecs().getDate().equals("2001-09-11"));
            	assertTrue(f.getSpecs().getPassengers().size() == 0);
            	assertTrue(f.getRoute().getFlightTime() == 160);
            	
            	f = itinerary[1];
            	assertTrue(f.getCarrier() == Airlines.AMERICAN);
            	assertTrue(f.getCode().equals("AA3"));
            	origin = f.getRoute().getOrigin();
            	assertTrue(origin.getCode().equals("ORD"));
            	assertTrue(origin.getCity().equals("Chicago"));
            	assertTrue(origin.getState().equals("IL"));
            	assertTrue(origin.getCountry().equals("USA"));
            	destination = f.getRoute().getDestination();
            	assertTrue(destination.getCode().equals("LAX"));
            	assertTrue(destination.getCity().equals("Los Angeles"));
            	assertTrue(destination.getState().equals("CA"));
            	assertTrue(destination.getCountry().equals("USA"));
            	assertTrue(f.getSeatsLeft() == 200);
            	assertTrue(f.getRoute().getDepartureTime() == 1300);
            	assertTrue(f.getRoute().getArrivalTime() == 1530);
            	assertTrue(f.getSpecs().getDate().equals("2001-09-11"));
            	assertTrue(f.getSpecs().getPassengers().size() == 0);
            	assertTrue(f.getRoute().getFlightTime() == 270);
            }
        }
        
       for (FlightInstance[] itinerary : betterFlights) {
    	   System.out.println("Possible itinerary:");
    	   for (FlightInstance f : itinerary) {
    		   System.out.println(f.toString());
    	   }
       }
        
       System.out.println("\n\n\n");

       Map<Passenger, Set<FlightInstance>> results = world.getPassengersWhoWillMissNextFlight(current, 105);
       assertTrue(results.isEmpty());
       results = world.getPassengersWhoWillMissNextFlight(current, 106);
       for (Entry<Passenger, Set<FlightInstance>> e : results.entrySet()) {
    	   Passenger p = e.getKey();
    	   System.out.println("A passenger by the name of " + p.getFirstName() + " " + p.getLastName() + " will miss this next flight b/c of a delay:");
    	   for (FlightInstance f : e.getValue()) {
    		   System.out.println("\t" + f.toString());
    	   }
       }
       assertTrue(!results.isEmpty() && results.size() == 1);
       for (Entry<Passenger, Set<FlightInstance>> e : results.entrySet()) {
    	   Passenger p = e.getKey();
    	   assertTrue(p.getFirstName().equals("Tyler"));
    	   assertTrue(p.getLastName().equals("Winklevoss"));
    	   assertTrue(p.getUTI() == tWinky.getUTI()); 
    	   Set<FlightInstance> hisFlights = e.getValue();
    	   assertTrue(hisFlights.size() == 1);
    	   for (FlightInstance f : hisFlights) {
    		   assertTrue(f.getCarrier() == Airlines.AMERICAN);
			   assertTrue(f.getCode().equals("AA5"));
			   Airport origin = f.getRoute().getOrigin();
			   assertTrue(origin.getCode().equals("LAX"));
			   assertTrue(origin.getCity().equals("Los Angeles"));
			   assertTrue(origin.getState().equals("CA"));
			   assertTrue(origin.getCountry().equals("USA"));
			   Airport destination = f.getRoute().getDestination();
			   assertTrue(destination.getCode().equals("HNL"));
			   assertTrue(destination.getCity().equals("Honolulu"));
			   assertTrue(destination.getState().equals("HI"));
			   assertTrue(destination.getCountry().equals("USA"));
			   assertTrue(f.getSeatsLeft() == 199);
			   assertTrue(f.getRoute().getDepartureTime() == 1500);
			   assertTrue(f.getRoute().getArrivalTime() == 1900);
			   assertTrue(f.getSpecs().getDate().equals("2001-09-11"));
			   assertTrue(f.getSpecs().getPassengers().size() == 1);
			   assertTrue(f.getRoute().getFlightTime() == 360);
    	   }
       }
       
       assertTrue(jfkToLax.getInstances().size() == 1);
       for (FlightInstance f : jfkToLax.getInstances()) {
    	   current = f;
       }
       
       Set<FlightInstance[]> newFlights = world.getFlightsForPeopleWithDelayedFlight(current, 1500);
       assertTrue(newFlights.size() == 1);
       for (FlightInstance[] itinerary : newFlights) {
    	   assertTrue(itinerary.length == 1);
    	   FlightInstance nextFlight = itinerary[0];
    	   assertTrue(nextFlight.getCode().equals("AA4"));
    	   assertTrue(nextFlight.getSeatsLeft() == 199);
    	   Airport origin = nextFlight.getRoute().getOrigin();
    	   assertTrue(origin.getCode().equals("JFK"));
    	   assertTrue(origin.getCity().equals("New York"));
    	   assertTrue(origin.getState().equals("NY"));
    	   assertTrue(origin.getCountry().equals("USA"));
    	   Airport destination = nextFlight.getRoute().getDestination();
    	   assertTrue(destination.getCode().equals("LAX"));
    	   assertTrue(destination.getCity().equals("Los Angeles"));
    	   assertTrue(destination.getState().equals("CA"));
    	   assertTrue(destination.getCountry().equals("USA"));
    	   assertTrue(nextFlight.getRoute().getDepartureTime() == 1000);
    	   assertTrue(nextFlight.getRoute().getArrivalTime() == 1330);
    	   assertTrue(nextFlight.getRoute().getFlightTime() == 390);
    	   assertTrue(nextFlight.getSpecs().getDate().equals("2001-09-11"));
    	   assertTrue(nextFlight.getSpecs().getFlightID().equals("AA4"));
    	   assertTrue(nextFlight.getSpecs().getPassengers().size() == 1);
    	   assertTrue(nextFlight.getSpecs().getPassengers().contains(tWinky.getUTI()));
       }
       
        
        
       CsvUtils.writeWorldToCsv(world, "/Users/crobotham/Downloads/flightsOutput.csv", "/Users/crobotham/Downloads/peopleOutput.csv");

    }

}
