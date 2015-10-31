package edu.brown.flights.types;

import static org.junit.Assert.assertTrue;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.sun.istack.Nullable;

import edu.brown.flights.types.planes.FlightInstance;
import edu.brown.flights.types.planes.FlightRecord;
import edu.brown.flights.types.planes.PlaneFlight;
import edu.brown.flights.utils.TimeHelpers;

public class World {

    private final Map<String, PlaneFlight> codesToFlightsMap;
    private final Set<Passenger> passengers;
    private final Set<Airport> airports;
    private final static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private static Date currentDate = new Date();
    private static final int BUFFER = 30;
    private static final int MIN_LAYOVER_TIME = 45;
    private static final int MINS_PER_HOUR = 60;
    private static final int HOURS_PER_DAY = 24;

    /*
     * Use builder class
     */
    public World() {
    	throw new UnsupportedOperationException();
    }

    /*
     * Constructor for builder
     */
    private World(Map<String, PlaneFlight> codesToFlightsMap, Set<Passenger> passengers,
    		Set<Airport> airports) {
    	this.codesToFlightsMap = codesToFlightsMap;
    	this.passengers = passengers;
    	this.airports = airports;
    }

    public Optional<Passenger> getPassengerByUTI(long UTI) {
        for (Passenger p : this.passengers) {
            if (p.getUTI() == UTI) {
                return Optional.of(p);
            }
        }
        return Optional.absent();
    }

    /**
     * Gets the flights occurring on date at or after time. Make the assumption
     * that a given plane flight (e.g. UA093) neigh occurs more than once per day
     * @param date
     * @param time
     * @return
     */
    public Set<FlightInstance> getNextFlights(String date, int time) {
        Set<FlightInstance> nextFlights = new HashSet<FlightInstance>();
        for (Entry<String, PlaneFlight> e : this.getCodesToFlightsMap().entrySet()) {
            Optional<FlightInstance> possibility = e.getValue().getNextFlight(date, time);
            if (possibility.isPresent()) {
                nextFlights.add(possibility.get());
            }
        }
        return nextFlights;
    }

    public Map<Passenger, Set<FlightInstance>> getPassengersWhoWillMissNextFlight(FlightInstance delayedFlight, int minsDelayed) {
        Map<Passenger, Set<FlightInstance>> toReturn = new HashMap<Passenger, Set<FlightInstance>>();
        String canNextTakeoffDate = delayedFlight.getRoute().getArrivalTime() < delayedFlight.getRoute().getDepartureTime() ? TimeHelpers.getNextDate(delayedFlight.getSpecs().getDate()) : delayedFlight.getSpecs().getDate();
        StringBuilder sb = new StringBuilder();
        int canNextTakeoffTime = TimeHelpers.addMinutes(delayedFlight.getRoute().getArrivalTime(), minsDelayed + MIN_LAYOVER_TIME, canNextTakeoffDate, sb);
        canNextTakeoffDate = sb.toString();
        for (long UTI : delayedFlight.getSpecs().getPassengers()) {
            Optional<Passenger> p = this.getPassengerByUTI(UTI);
            if (p.isPresent()) {
                Passenger passenger = p.get();
                String currDate = delayedFlight.getSpecs().getDate();
                int numDates = 2 + (minsDelayed / (MINS_PER_HOUR * HOURS_PER_DAY)); //probably could do 1, but best to be safe
                while (numDates > 0) { //i.e. how many dates in advance we should
                    Set<String> flightCodesToday = passenger.getDateToFlights().get(currDate);
                    for (String flightCode : flightCodesToday) {
                        PlaneFlight thePlaneFlight = this.getCodesToFlightsMap().get(flightCode);
                        assertTrue(thePlaneFlight != null);
                        Optional<FlightInstance> possibility = thePlaneFlight.getNextFlight(currDate, 0);
                        assertTrue(possibility.isPresent());
                        FlightInstance theFlight = possibility.get();
                        if (TimeHelpers.compareFlightDepartureTimes(delayedFlight, theFlight) < 0 //this flight is AFTER the delayed flight
                                && TimeHelpers.compareDatesAndTimes(theFlight.getSpecs().getDate(), theFlight.getRoute().getDepartureTime(), canNextTakeoffDate, canNextTakeoffTime) < 0) { //but it's gunna take off before they will be able to, given the delay
                            //in other words, it's a flight AFTER the delayed one but BEFORE they can next take off, given the delay, ergo, they is miss this shits
                            if (!toReturn.containsKey(passenger)) {
                                toReturn.put(passenger, new HashSet<FlightInstance>());
                            }
                            toReturn.get(passenger).add(theFlight);
                        }
                    }
                    numDates--;
                }
            }
        }
        return toReturn;
    }

    /**
     * Given a set of flights and a current flight, returns a set of itineraries (from the
     * inputed set of flights) that do the same route that you can catch (in terms of
     * going somewhere else in your current airport and having a sufficiently long layover).
     * @param current
     * @param possibleOthers
     * @return
     */
    public static Set<FlightInstance[]> getViableRoutes(FlightInstance current, Set<FlightInstance> possibleOthers) {
        Set<FlightInstance[]> toReturn = new HashSet<FlightInstance[]>();
        assertTrue(toReturn.isEmpty());
        StringBuilder sb = new StringBuilder();
        int earliestTakeoff1Time = TimeHelpers.addMinutes(current.getRoute().getDepartureTime(), BUFFER, current.getSpecs().getDate(), sb);
        String earliestTakeoff1Date = sb.toString();
        Set<FlightInstance> flights = possibleOthers;
        sb = new StringBuilder();

        for (FlightInstance flight1 : flights) {
            if (!flight1.getCode().equals(current.getCode())
                    && flight1.getRoute().getOrigin().equals(current.getRoute().getOrigin())
                    && TimeHelpers.compareDatesAndTimes(flight1.getSpecs().getDate(), flight1.getRoute().getDepartureTime(), earliestTakeoff1Date, earliestTakeoff1Time) > 0) {
                if (flight1.getRoute().getDestination().equals(current.getRoute().getDestination())) {
                    toReturn.add(new FlightInstance[]{flight1});
                } else {
                    sb = new StringBuilder();
                    int earliestTakeoff2Time = TimeHelpers.addMinutes(flight1.getRoute().getArrivalTime(), MIN_LAYOVER_TIME, flight1.getSpecs().getDate(), sb);
                    String earliestTakeoff2Date = sb.toString();
                    for (FlightInstance flight2 : flights) {
                        if (!flight2.getCode().equals(current.getCode())
                                && !flight2.getCode().equals(flight1.getCode())
                                && flight2.getRoute().getOrigin().equals(flight1.getRoute().getDestination()) //aka actually is a connecting flight
                                && flight2.getRoute().getDestination().equals(current.getRoute().getDestination())
                                && TimeHelpers.compareDatesAndTimes(flight2.getSpecs().getDate(), flight2.getRoute().getDepartureTime(), earliestTakeoff2Date, earliestTakeoff2Time) > 0) {
                            toReturn.add(new FlightInstance[]{flight1, flight2});
                        }
                    }
                }
            }
        }
        return toReturn;
    }

    /**
     * Given a set of flights, and a currently scheduled flight that is delayed, return the
     * set (can be empty) of itineraries that will get someone to their destination faster
     * than waiting out the delay. An itinerary is an array of flights, whose order represents
     * the successive legs of a total flight path.
     * 
     * this is the big boy
     */
    public static Set<FlightInstance[]> getFasterRoutesFromFlights(FlightInstance current, int minsDelayed, Set<FlightInstance> flights) {
        Set<FlightInstance[]> toReturn = new HashSet<FlightInstance[]>();
        assertTrue(toReturn.isEmpty());
        int currentArrivalTime = TimeHelpers.addMinutes(current.getRoute().getArrivalTime(), minsDelayed);
        StringBuilder sb = new StringBuilder();
        int currentDepartureTime = TimeHelpers.addMinutes(current.getRoute().getDepartureTime(), minsDelayed, current.getSpecs().getDate(), sb);
        String currentDepartureDate = sb.toString();
        String currentArrivalDate = currentArrivalTime < currentDepartureTime ? TimeHelpers.getNextDate(currentDepartureDate) : currentDepartureDate;

        for (FlightInstance flight1 : flights) {
            String firstDepDate = flight1.getSpecs().getDate();
            String firstArrDate = flight1.getRoute().getArrivalTime() < flight1.getRoute().getDepartureTime() ? TimeHelpers.getNextDate(firstDepDate) : firstDepDate;
            //pick up here
            sb = new StringBuilder();
            int earliestTakeoff1 = TimeHelpers.addMinutes(current.getRoute().getDepartureTime(), BUFFER, current.getSpecs().getDate(), sb);
            String earliestTakeoff1Date = sb.toString();
            
            if (!flight1.getCode().equals(current.getCode())
            		&& flight1.getRoute().getOrigin().equals(current.getRoute().getOrigin())
            		&& TimeHelpers.compareDatesAndTimes(firstDepDate, flight1.getRoute().getDepartureTime(), earliestTakeoff1Date, earliestTakeoff1) >= 0
            		&& TimeHelpers.compareDatesAndTimes(firstArrDate, flight1.getRoute().getArrivalTime(), currentArrivalDate, currentArrivalTime) < 0
            		&& flight1.getSeatsLeft() > 0) {
            	if (flight1.getRoute().getDestination().equals(current.getRoute().getDestination())) {
                    toReturn.add(new FlightInstance[]{flight1}); //shits is direct
                } else { //look for a connecting flight
                	for (FlightInstance flight2 : flights) {
                		
                		String thisDepDate = flight2.getSpecs().getDate();
                        String thisArrDate = flight2.getRoute().getArrivalTime() < flight2.getRoute().getDepartureTime() ? TimeHelpers.getNextDate(thisDepDate) : thisDepDate;
                        sb = new StringBuilder();
                        int earliestTakeoff2 = TimeHelpers.addMinutes(flight1.getRoute().getArrivalTime(), MIN_LAYOVER_TIME, flight1.getSpecs().getDate(), sb);
                        String earliestTakeoff2Date = sb.toString();
                        
                        if (!flight2.getCode().equals(current.getCode())
                        		&& !flight2.getCode().equals(flight1.getCode())
                        		&& flight2.getRoute().getOrigin().equals(flight1.getRoute().getDestination())
                        		&& flight2.getRoute().getDestination().equals(current.getRoute().getDestination())
                        		&& TimeHelpers.compareDatesAndTimes(thisDepDate, flight2.getRoute().getDepartureTime(), earliestTakeoff2Date, earliestTakeoff2) >= 0
                        		&& TimeHelpers.compareDatesAndTimes(thisArrDate, flight2.getRoute().getArrivalTime(), currentArrivalDate, currentArrivalTime) < 0
                        		&& flight2.getSeatsLeft() > 0) {
                        	toReturn.add(new FlightInstance[]{flight1, flight2});
                        }
                	}
                }
            }
        }
        return toReturn;
    }

    /**
     * Given a FlightInstance current and a delay minsDelayed, books as many people
     * as possible, sorted by status, on flights that will get to their destination
     * sooner than the delayed flight.
     */
    public Set<FlightInstance[]> getFlightsForPeopleWithDelayedFlight(FlightInstance currentFlight, int minsDelayed) {
        Set<FlightInstance[]> possibleItineraries = new HashSet<FlightInstance[]>();
        //String currentArrivalDate = currentFlight.getSpecs().getDate();
        //int currrentArrivalTimeTime = TimeHelpers.addMinutes(currentFlight.getRoute().getDepartureTime(), minsDelayed, currentArrivalDate, sb);
        LinkedList<Long> passengerUTIs = new LinkedList<Long>(currentFlight.getSpecs().getPassengers());
        Collections.sort(passengerUTIs, new Comparator<Long>() {
        	@Override
			public int compare(Long o1, Long o2) {
				return Long.compare(o1, o2);
			}
        });
        System.out.println("We have now sorted the passengers. There are " + passengerUTIs.size() + " of them");
        
        String currDate = currentFlight.getSpecs().getDate();
        int currTime = currentFlight.getRoute().getDepartureTime();
        while (!passengerUTIs.isEmpty()) {
            Set<FlightInstance[]> itinerariesThatDay = World.getFasterRoutesFromFlights(currentFlight, minsDelayed, this.getNextFlights(currDate, currTime));
            if (itinerariesThatDay.isEmpty()) { //which will inevitably occur for any finite delay
                break;
            }
            ArrayList<FlightInstance[]> itinerariesList = new ArrayList<FlightInstance[]>(itinerariesThatDay);
            Collections.sort(itinerariesList, new Comparator<FlightInstance[]>() {
				@Override
				public int compare(FlightInstance[] itin1, FlightInstance[] itin2) { //sorts the flights, w/the better ones on top
					//assumes flights were entered in the array in order
					FlightInstance lastLeg1 = itin1[itin1.length - 1];
					FlightInstance lastLeg2 = itin2[itin2.length - 1];
					return TimeHelpers.compareFlightArrivalTimes(lastLeg1, lastLeg2);
				}
            });
            System.out.println("we now have sorted the potential itineraries ");
            for (FlightInstance[] itinerary : itinerariesList) {
            	System.out.println("\tHere's an itinerary");
            	for (FlightInstance leg : itinerary) {
            		System.out.println("\t\t" + leg.toString());
            	}
            }
            
            
            for (FlightInstance[] itinerary : itinerariesList) {
            	if (passengerUTIs.size() > 0) {

                    int canFit = Integer.MAX_VALUE;
                    for (FlightInstance leg : itinerary) {
                        canFit = Math.min(canFit, leg.getSeatsLeft());
                    }
                    List<Long> passengersToAdd = new LinkedList<Long>();
                    while (canFit > 0 && !passengerUTIs.isEmpty()) {
                    	passengersToAdd.add(passengerUTIs.removeFirst());
                    }
                    for (FlightInstance leg : itinerary) {
                    	leg.getSpecs().addPassengersByUTI(passengersToAdd);
                    	currentFlight.getSpecs().removePassengersByUTI(passengersToAdd);
                    	for (Long UTI : passengersToAdd) {
                    		Optional<Passenger> maybeP = this.getPassengerByUTI(UTI);
                    		assertTrue(maybeP.isPresent());
                    		Passenger p = maybeP.get();
                    		p = p.getBuilder().addFlightOnDate(leg.getSpecs().getDate(), leg.getCode()).removeFlightOnDate(currentFlight.getSpecs().getDate(), currentFlight.getCode()).build();
                    	}
                    }
                    possibleItineraries.add(itinerary);
            	}
            }
            currDate = TimeHelpers.getNextDate(currDate);
            currTime = 0;
            
            
        }
        return possibleItineraries;
    }
    
    /**
     * Does the exact same thing as getFasterRoutesFromFlights(), but with a shit ton of
     * println's.
     */
    public static Set<FlightInstance[]> getFasterRoutesFromFlightsWithLogging(FlightInstance current, int minsDelayed, Set<FlightInstance> possibleOthers) {
    	Set<FlightInstance[]> toReturn = new HashSet<FlightInstance[]>();
        assertTrue(toReturn.isEmpty());
        int currentArrivalTime = TimeHelpers.addMinutes(current.getRoute().getArrivalTime(), minsDelayed);
        StringBuilder sb = new StringBuilder();
        int currentDepartureTime = TimeHelpers.addMinutes(current.getRoute().getDepartureTime(), minsDelayed, current.getSpecs().getDate(), sb);
        String currentDepartureDate = sb.toString();
        String currentArrivalDate = currentArrivalTime < currentDepartureTime ? TimeHelpers.getNextDate(currentDepartureDate) : currentDepartureDate;

        for (FlightInstance flight1 : possibleOthers) {
            String firstDepDate = flight1.getSpecs().getDate();
            String firstArrDate = flight1.getRoute().getArrivalTime() < flight1.getRoute().getDepartureTime() ? TimeHelpers.getNextDate(firstDepDate) : firstDepDate;
            //pick up here
            sb = new StringBuilder();
            int earliestTakeoff1 = TimeHelpers.addMinutes(current.getRoute().getDepartureTime(), BUFFER, current.getSpecs().getDate(), sb);
            String earliestTakeoff1Date = sb.toString();
            
            System.out.println("Is neigh the original? " + !flight1.getCode().equals(current.getCode()));
            System.out.println("Same origins? " + flight1.getRoute().getOrigin().equals(current.getRoute().getOrigin()));
            System.out.println("Doesn't take off too early, datewise? " + (TimeHelpers.compareDatesForSoonness(earliestTakeoff1Date, firstDepDate) >= 0));
            System.out.println("Doesn't get in too late, datewise? " + (TimeHelpers.compareDatesForSoonness(currentArrivalDate, firstArrDate) <= 0));
            System.out.println("Doesn't take off too early, timewise? " + (flight1.getRoute().getDepartureTime() > earliestTakeoff1));
            System.out.println("Doesn't get in too late, timewise? " + (flight1.getRoute().getArrivalTime() < currentArrivalTime));
            System.out.println("Has a seat? " + (flight1.getSeatsLeft() > 0));
            
            if (!flight1.getCode().equals(current.getCode())
            		&& flight1.getRoute().getOrigin().equals(current.getRoute().getOrigin())
            		&& TimeHelpers.compareDatesAndTimes(firstDepDate, flight1.getRoute().getDepartureTime(), earliestTakeoff1Date, earliestTakeoff1) >= 0
            		&& TimeHelpers.compareDatesAndTimes(firstArrDate, flight1.getRoute().getArrivalTime(), currentArrivalDate, currentArrivalTime) < 0
            		&& flight1.getSeatsLeft() > 0) {
            	if (flight1.getRoute().getDestination().equals(current.getRoute().getDestination())) {
                    toReturn.add(new FlightInstance[]{flight1}); //shits is direct
                } else { //look for a connecting flight
                	for (FlightInstance flight2 : possibleOthers) {
                		
                		String thisDepDate = flight2.getSpecs().getDate();
                        String thisArrDate = flight2.getRoute().getArrivalTime() < flight2.getRoute().getDepartureTime() ? TimeHelpers.getNextDate(thisDepDate) : thisDepDate;
                        sb = new StringBuilder();
                        int earliestTakeoff2 = TimeHelpers.addMinutes(flight1.getRoute().getArrivalTime(), MIN_LAYOVER_TIME, flight1.getSpecs().getDate(), sb);
                        String earliestTakeoff2Date = sb.toString();
                        
                        System.out.println("\tFound a possible second leg: " + flight2.toString());
                        System.out.println("\tCompletes the trip? " + flight2.getRoute().getOrigin().equals(flight1.getRoute().getDestination()));
                        System.out.println("\tSame origins? " + flight1.getRoute().getOrigin().equals(current.getRoute().getOrigin()));
                        System.out.println("\tDoesn't take off too early, datewise? " + (TimeHelpers.compareDatesForSoonness(earliestTakeoff1Date, firstDepDate) >= 0));
                        System.out.println("\tDoesn't get in too late, datewise? " + (TimeHelpers.compareDatesForSoonness(currentArrivalDate, firstArrDate) <= 0));
                        System.out.println("\tDoesn't take off too early, timewise? " + (flight1.getRoute().getDepartureTime() > earliestTakeoff1));
                        System.out.println("\tDoesn't get in too late, timewise? " + (flight1.getRoute().getArrivalTime() < currentArrivalTime));
                        System.out.println("\tHas a seat? " + (flight1.getSeatsLeft() > 0));
                        
                        if (!flight2.getCode().equals(current.getCode())
                        		&& !flight2.getCode().equals(flight1.getCode())
                        		&& TimeHelpers.compareDatesAndTimes(thisDepDate, flight2.getRoute().getDepartureTime(), earliestTakeoff2Date, earliestTakeoff2) >= 0
                        		&& TimeHelpers.compareDatesAndTimes(thisArrDate, flight2.getRoute().getArrivalTime(), currentArrivalDate, currentArrivalTime) < 0
                        		&& flight2.getSeatsLeft() > 0) {
                        	toReturn.add(new FlightInstance[]{flight1, flight2});
                        }
                	}
                }
            }
        }
        return toReturn;
    }

    /*
     * Gets a new builder for modification
     */
    public WorldBuilder getBuilder() {
    	return new WorldBuilder(this.codesToFlightsMap, this.passengers, this.airports);
    }

    public static String getCurrentDate() {
        return dateFormat.format(currentDate);
    }

    public Map<String, PlaneFlight> getCodesToFlightsMap() {
		return codesToFlightsMap;
	}

	public Set<Airport> getAirports() {
		return airports;
	}

	public Set<Passenger> getPassengers() {
		return passengers;
	}

    public static class WorldBuilder {
        private Map<String, PlaneFlight> codesToFlightsMap; //ex. key: UA093
        private Set<Passenger> passengers;
        private Set<Airport> airports;

    	/*
    	 * Required types: codesToFlightsMap, List<Passenger>, and List<Airport>
    	 */
    	public WorldBuilder (Map<String, PlaneFlight> codesToFlightsMap, Set<Passenger> passengers,
    			Set<Airport> airports) {
    		this.codesToFlightsMap = codesToFlightsMap;
    		this.passengers = passengers;
    		this.airports = airports;
    	}

    	/*
    	 * Constructs World
    	 */
    	public World build() {
    		return new World(codesToFlightsMap, passengers, airports);
    	}

    	public WorldBuilder setCodes(Map<String, PlaneFlight> codes) {
    		this.codesToFlightsMap = codes;

    		return this;
    	}

    	public WorldBuilder setPassengers(Set<Passenger> pass) {
    		this.passengers = pass;

    		return this;
    	}

    	public WorldBuilder setAirports(Set<Airport> air) {
    		this.airports = air;

    		return this;
    	}

    	/**
    	 * Predicated on the passenger having a pre-loaded itinerary,
    	 * where the flights themselves have been loaded into the world.
    	 *
    	 * Also assumes that for a given flight ID (e.g. UA093), that it
    	 * neigh runs more than once a day!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    	 */
    	public WorldBuilder addPassenger(final Passenger p) {
    	    this.passengers.add(p);
            for (Entry<String, Set<String>> e : p.getDateToFlights().entrySet()) {
                String date = e.getKey();
                Set<String> flightsThatDay = e.getValue();
                for (String flightCode : flightsThatDay) {
                    if (this.codesToFlightsMap.containsKey(flightCode)) {
                        for (Entry<Route, Set<FlightRecord>> routeAndRuns : this.codesToFlightsMap.get(flightCode).getRoutesAndRuns().entrySet()) {
                            Set<FlightRecord> runs = routeAndRuns.getValue();
                            for (FlightRecord run : runs) {
                                if (run.getDate().equals(date)) {
                                    run.addPassenger(p);
                                }
                            }
                        }
                    }
                }


            }
            return this;
    	}

    	public WorldBuilder addPassengers(final Collection<Passenger> passengers) {
    	    for (Passenger p : passengers) {
    	        this.addPassenger(p);
    	    }
    	    return this;
    	}

    	public WorldBuilder addAirport(Airport a) {
    	    this.airports.add(a);
    		return this;
    	}

    	public WorldBuilder addAirports(final Collection<Airport> airports) {
            for (Airport a : airports) {
                this.addAirport(a);
            }
            return this;
        }

    	public WorldBuilder addFlight(final PlaneFlight flight) {
    	    boolean alreadyThere = Iterables.any(this.codesToFlightsMap.entrySet(),
                    new Predicate<Entry<String, PlaneFlight>>() {
                        @Override
                        public boolean apply(@Nullable Entry<String, PlaneFlight> input) {
                            return input != null && input.getKey().equalsIgnoreCase(flight.getCode());
                        }
            });
            if (!alreadyThere) {
                this.codesToFlightsMap.put(flight.getCode(), flight);
                for (Entry<Route, Set<FlightRecord>> e : flight.getRoutesAndRuns().entrySet()) {
                    Route r = e.getKey();
                    this.addAirport(r.getOrigin());
                    this.addAirport(r.getDestination());
                }
            } else {
                PlaneFlight theFlight = this.codesToFlightsMap.get(flight.getCode());
                for (Entry<Route, Set<FlightRecord>> e : flight.getRoutesAndRuns().entrySet()) {
                    Route r = e.getKey();
                    if (theFlight.hasRoute(r)) {
                        for (FlightRecord i : flight.getRoutesAndRuns().get(r)) {
                            theFlight.addRunForRoute(r, i);
                        }
                    } else {
                        theFlight.addRouteAndRuns(r, e.getValue());
                    }
                }
            }
            return this;
    	}

    }

    public static int getCurrentTime() {
    	String h = (new SimpleDateFormat("HHmm")).format(currentDate);
        return Integer.parseInt(h);
    }

	public static String getDate() {
		return dateFormat.format(currentDate);
	}
}
