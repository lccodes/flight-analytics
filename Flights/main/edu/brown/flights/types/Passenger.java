package edu.brown.flights.types;

import static org.junit.Assert.assertTrue;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Optional;

import edu.brown.flights.types.planes.FlightInstance;
import edu.brown.flights.utils.TimeHelpers;

public class Passenger implements Comparable<Passenger> {

    private final String firstName;
	private final String lastName;
	private Map<String, Set<String>> dateToFlights = new HashMap<String, Set<String>>();
	private final long UTI;

	/*
	 * Do not instantiate. Use builder
	 */
	public Passenger() {
		throw new UnsupportedOperationException();
	}

	private Passenger(String firstName, String lastName, Map<String, Set<String>> dateToFlights, long UTI) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.dateToFlights = dateToFlights;
		this.UTI = UTI;
	}
	
	

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

	/*
	 * Gets the next leg in their journey if any
	 */
	public Optional<String> getNextFlight(String date, String flightID) {
	    if (this.dateToFlights.containsKey(date)) {
	        //get the exact next flight
	    	//TODO: This
	    }
	    return Optional.absent();
	}

	public long getUTI() {
		return UTI;
	}

	public PassengerBuilder getBuilder() {
	    return new PassengerBuilder(this);
	}

	public static class PassengerBuilder {

		private final String firstName;
		private final String lastName;
		private Map<String, Set<String>> legs = new HashMap<String, Set<String>>(); //set of keys, like DL0875, UA093, etc.
		private long UTI;

		/*
		 * Builder requires first name and last name
		 */
		public PassengerBuilder(String firstName, String lastName) {
			this.firstName = firstName;
			this.lastName = lastName;
		}

		public PassengerBuilder(Passenger p) {
			this.firstName = p.getFirstName();
			this.lastName = p.getLastName();
			this.legs = p.getDateToFlights();
			this.UTI = p.getUTI();
		}
		
		public PassengerBuilder addFlightOnDate(String date, String flightCode) {
		    if (!date.matches("[0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9]")) {
		        throw new IllegalArgumentException("Please provide a date in the form of 2001-09-11 as the first argument");
		    }
		    if (!this.legs.containsKey(date)) {
		        this.legs.put(date, new HashSet<String>());
		    }
		    this.legs.get(date).add(flightCode);
		    return this;
		}
		
		public PassengerBuilder removeFlightOnDate(String date, String flightCode) {
			if (!date.matches("[0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9]")) {
		        throw new IllegalArgumentException("Please provide a date in the form of 2001-09-11 as the first argument");
		    }
		    assertTrue(this.legs.containsKey(date));
		    System.out.println("Trying to remove flight " + flightCode + " on " + date + " for passenger " + this.firstName + " " + this.lastName);
		    
		    
		    assertTrue(this.legs.get(date).contains(flightCode));
		    assertTrue(this.legs.get(date).contains(flightCode));
		    assertTrue(this.legs.get(date).remove(flightCode));
		    return this;
		}

		public Passenger build() {
			byte[] seed = new byte[]{(byte) firstName.hashCode(),
					(byte) lastName.hashCode(),
					(byte) legs.hashCode()};
			SecureRandom sr = new SecureRandom(seed);
			UTI = sr.nextLong();
			return new Passenger(this.firstName, this.lastName, this.legs, UTI);
		}
	}

    public Map<String, Set<String>> getDateToFlights() {
        return dateToFlights;
    }

    public void setDateToFlights(Map<String, Set<String>> dateToFlights) {
        this.dateToFlights = dateToFlights;
    }

    /**
     * Get faster routes for this passenger
     * @param current
     * @param minsDelayed
     * @param w
     * @return set of faster route instances
     */
    public Set<FlightInstance[]> getFasterRoutes(FlightInstance current, int minsDelayed, World w) {
        Set<FlightInstance> flights = w.getNextFlights(current.getSpecs().getDate(), current.getRoute().getDepartureTime());
        Set<FlightInstance[]> toReturn = new HashSet<FlightInstance[]>(World.getFasterRoutesFromFlights(current, minsDelayed, flights));

        String currDate = TimeHelpers.getNextDate(current.getSpecs().getDate());
        while (true) {
            Set<FlightInstance> newFlights = w.getNextFlights(currDate, 0);
            Set<FlightInstance[]> maybeMore = World.getFasterRoutesFromFlights(current, minsDelayed, newFlights);
            if (maybeMore.isEmpty()) {
                break;
            }
            toReturn.addAll(maybeMore);
            currDate = TimeHelpers.getNextDate(currDate);
        }

        return toReturn;
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (UTI ^ (UTI >>> 32));
		result = prime * result + ((dateToFlights == null) ? 0 : dateToFlights.hashCode());
		result = prime * result + ((firstName == null) ? 0 : firstName.hashCode());
		result = prime * result + ((lastName == null) ? 0 : lastName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Passenger other = (Passenger) obj;
		return this.UTI == other.getUTI();
	}

	@Override
	public int compareTo(Passenger o) {
		// TODO this is where some sort of status algorithm would go
		return 0;
	}

}
