package edu.brown.flights.types.planes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.base.Optional;

import edu.brown.flights.types.Airlines;
import edu.brown.flights.types.Route;
import edu.brown.flights.types.World;
import edu.brown.flights.types.Route.RouteBuilder;
import edu.brown.flights.types.planes.FlightInstance.FlightInstanceBuilder;
import edu.brown.flights.types.planes.FlightRecord.FlightRecordBuilder;
import edu.brown.flights.utils.TimeHelpers;

/**
 * @author lcamery
 *
 */
public class PlaneFlight implements Comparable<PlaneFlight> {

    private int flightNumber;
    private Airlines carrier;
    private Map<Route, Set<FlightRecord>> routesAndRuns = new HashMap<Route, Set<FlightRecord>>();

    public void addRouteAndRuns(Route r, Set<FlightRecord> flights) {
        this.routesAndRuns.put(r, flights);
    }

    public String getCode() { //e.g. UA093
        return this.carrier.getAbbreviation() + this.flightNumber;
    }

    public boolean hasRoute(Route r) {
        return this.routesAndRuns.containsKey(r);
    }

    public Map<Route, Set<FlightRecord>> getRoutesAndRuns() {
        return this.routesAndRuns;
    }

    public void addRunForRoute(Route r, FlightRecord i) {
        if (!this.routesAndRuns.containsKey(r)) {
            this.routesAndRuns.put(r, new HashSet<FlightRecord>());
        }
        this.routesAndRuns.get(r).add(i);
    }

    public Set<FlightInstance> getInstances() {
        Set<FlightInstance> toReturn = new HashSet<FlightInstance>();
        for (Entry<Route, Set<FlightRecord>> e : this.routesAndRuns.entrySet()) {
            Route theRoute = e.getKey();
            Set<FlightRecord> records = e.getValue();
            for (FlightRecord record : records) {
                toReturn.add((new FlightInstanceBuilder()).withCarrier(this.carrier)
                        .withCode(this.getCode())
                        .withRoute(theRoute)
                        .withSpecs(record)
                        .build());
            }
        }
        return toReturn;
    }

    public Optional<FlightInstance> getFlightOnDate(String date) {
        Set<FlightInstance> instances = this.getInstances();
        for (FlightInstance instance : instances) {
            if (instance.getSpecs().getDate() != null &&
            		instance.getSpecs().getDate().equals(date)) {
                return Optional.of(instance);
            }
        }
        return Optional.absent();
    }

    public Optional<FlightInstance> getNextFlight(final String date, int time) {
        Optional<FlightInstance> maybeTodaysFlight = this.getFlightOnDate(date);
        if (maybeTodaysFlight.isPresent()) {
            FlightInstance todaysFlight = maybeTodaysFlight.get();
            int itsTime = todaysFlight.getRoute().getDepartureTime();
            if (itsTime > time) {
                return Optional.of(todaysFlight);
            }
        }

        //this means the next flight is neigh today
        Set<FlightInstance> instances = this.getInstances();
        List<FlightInstance> instancesList = new ArrayList<FlightInstance>(instances);
        Collections.sort(instancesList, new Comparator<FlightInstance>() {
            @Override
            public int compare(FlightInstance instanceOne, FlightInstance instanceTwo) {
                return TimeHelpers.compareDatesAndTimes(instanceOne.getSpecs().getDate(), instanceOne.getRoute().getDepartureTime(),
                        instanceTwo.getSpecs().getDate(), instanceTwo.getRoute().getDepartureTime());
            }
        });

        for (FlightInstance instance : instancesList) {
            //since it's sorted, the first one to be after the current one is by definition the "next" flight
            if (TimeHelpers.compareDatesAndTimes(date, time, instance.getSpecs().getDate(), instance.getRoute().getDepartureTime()) < 0) {
                return Optional.of(instance);
            }
        }
        return Optional.absent();
    }

    public Optional<Plane> getTodaysPlane() {
        return Optional.absent();
    }

    public PlaneFlightBuilder getBuilder() {
        return new PlaneFlightBuilder(this);
    }

    public static class PlaneFlightBuilder {

        private String[] infoPieces;
        private String date;
        private Airlines carrier;
        private String tailNum;
        private int flightNum;
        private String originAirportCode;
        private String originCityName;
        private String originStateAbbrev;
        private String destAirportCode;
        private String destCityName;
        private String destStateAbbrev;
        private int schedDepTime;
        private int actualDepTime;
        private int schedArrTime;
        private int actualArrTime;
        private boolean cancelled;
        private String cancellationCode = "";
        private boolean diverted;
        private int schedElapsedTime;


        public PlaneFlightBuilder(PlaneFlight flight) {
            this.carrier = flight.carrier;
            this.flightNum = flight.getFlightNumber();
        }

        public PlaneFlightBuilder(String infoLine) {
            this.infoPieces = infoLine.split(","); //assumes they were parsed in the order above ^. If neigh, things are FOOKED
            int i = 0;
            if (infoPieces[i].equals("")) {
                throw new IllegalArgumentException();
            }
            //date = infoPieces[i]; i++;
            String rawDate = infoPieces[i];
            String[] dateParts = rawDate.split("-");
            if (dateParts.length != 3) {
                throw new IllegalArgumentException();
            }
            date = dateParts[0] + "-" + TimeHelpers.toValidString(dateParts[1]) + "-" + TimeHelpers.toValidString(dateParts[2]);
            i++;
            if (infoPieces[i].equals("")) {
                throw new IllegalArgumentException();
            }
            carrier = Airlines.getAirlineFromAbbreviation(infoPieces[i]); i++;
            if (infoPieces[i].equals("")) {
                throw new IllegalArgumentException();
            }
            tailNum = infoPieces[i]; i++;
            if (infoPieces[i].equals("")) {
                throw new IllegalArgumentException();
            }
            flightNum = Integer.parseInt(infoPieces[i]); i++;
            if (infoPieces[i].equals("")) {
                throw new IllegalArgumentException();
            }
            originAirportCode = infoPieces[i]; i++;
            if (infoPieces[i].equals("")) {
                throw new IllegalArgumentException();
            }
            originCityName = infoPieces[i]; i++;
            if (infoPieces[i].equals("")) {
                throw new IllegalArgumentException();
            }
            originStateAbbrev = infoPieces[i]; i++;
            if (infoPieces[i].equals("")) {
                throw new IllegalArgumentException();
            }
            destAirportCode = infoPieces[i]; i++;
            if (infoPieces[i].equals("")) {
                throw new IllegalArgumentException();
            }
            destCityName = infoPieces[i]; i++;
            if (infoPieces[i].equals("")) {
                throw new IllegalArgumentException();
            }
            destStateAbbrev = infoPieces[i]; i++;
            if (infoPieces[i].equals("")) {
                throw new IllegalArgumentException();
            }
            schedDepTime = Integer.parseInt(infoPieces[i]); i++;
            if (infoPieces[i].equals("")) {
                throw new IllegalArgumentException();
            }
            actualDepTime = Integer.parseInt(infoPieces[i]); i++;
            if (infoPieces[i].equals("")) {
                throw new IllegalArgumentException();
            }
            schedArrTime = Integer.parseInt(infoPieces[i]); i++;
            if (infoPieces[i].equals("")) {
                throw new IllegalArgumentException();
            }
            actualArrTime = Integer.parseInt(infoPieces[i]); i++;
            if (infoPieces[i].equals("")) {
                throw new IllegalArgumentException();
            }
            cancelled = (int) Double.parseDouble(infoPieces[i]) == 1; i++;
            cancellationCode = infoPieces[i]; i++;
            if (infoPieces[i].equals("")) {
                throw new IllegalArgumentException();
            }
            diverted = (int) Double.parseDouble(infoPieces[i]) == 1; i++;
            if (infoPieces[i].equals("")) {
                throw new IllegalArgumentException();
            }
            schedElapsedTime = (int) Double.parseDouble(infoPieces[i]);
        }

        public PlaneFlight build() {
            PlaneFlight toReturn = new PlaneFlight();
            Route itsRoute = (new RouteBuilder()).withOriginAirport(originAirportCode)
                    .withOriginCity(originCityName)
                    .withOriginState(originStateAbbrev)
                    .withDestAirport(destAirportCode)
                    .withDestCity(destCityName)
                    .withDestState(destStateAbbrev)
                    .departingAt(schedDepTime)
                    .arrivingAt(schedArrTime)
                    .withFlightTime(schedElapsedTime)
                    .build();
            FlightRecord record = (new FlightRecordBuilder(this.carrier.getAbbreviation() + this.flightNum))
                    .withTailNum(this.tailNum)
                    .onDate(this.date)
                    .actuallyDepartedAt(this.actualDepTime)
                    .actuallyArrivedAt(this.actualArrTime)
                    .build();
            if (this.cancelled) {
               record = record.getBuilder().isCancelled().build();
                if (!this.cancellationCode.equals("")) {
                    record = record.getBuilder().withCancellationType(this.cancellationCode).build();
                }
            }
            if (this.diverted) {
                record = record.getBuilder().isDiverted().build();
            }
            HashSet<FlightRecord> instances = new HashSet<FlightRecord>(1); instances.add(record);

            toReturn.setFlightNumber(flightNum);
            toReturn.addRouteAndRuns(itsRoute, instances);
            toReturn.setCarrier(carrier);
            return toReturn;
        }

        public int getActualDepTime() {
            return actualDepTime;
        }

        public void setActualDepTime(int actualDepTime) {
            this.actualDepTime = actualDepTime;
        }

        public int getActualArrTime() {
            return actualArrTime;
        }

        public void setActualArrTime(int actualArrTime) {
            this.actualArrTime = actualArrTime;
        }

    }

    public Airlines getCarrier() {
        return this.carrier;
    }

    public void setCarrier(Airlines carrier) {
        this.carrier = carrier;
    }

    public int getFlightNumber() {
        return this.flightNumber;
    }

    public void setFlightNumber(int x) {
        this.flightNumber = x;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((carrier == null) ? 0 : carrier.hashCode());
        result = prime * result + flightNumber;
        result = prime * result + ((routesAndRuns == null) ? 0 : routesAndRuns.hashCode());
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
        PlaneFlight other = (PlaneFlight) obj;
        if (carrier != other.carrier)
            return false;
        if (flightNumber != other.flightNumber)
            return false;
        if (routesAndRuns == null) {
            if (other.routesAndRuns != null)
                return false;
        } else if (!routesAndRuns.equals(other.routesAndRuns))
            return false;
        return true;
    }

    @Override
    public int compareTo(PlaneFlight o) {

        boolean thisHasFlightToday = false;
        boolean thatHasFlightToday = false;
        int bestThisTime = 2401; //i.e. neigh been found
        int bestThatTime = 2401;

        for (Entry<Route, Set<FlightRecord>> e : this.routesAndRuns.entrySet()) {
            Set<FlightRecord> runs = e.getValue();
            boolean runsToday = false;
            for (FlightRecord run : runs) {
                if (run.getDate().equals(World.getCurrentDate())) {
                    runsToday = true; thisHasFlightToday = true; break;
                }
            }
            if (runsToday) {
                bestThisTime = Math.min(bestThisTime, e.getKey().getDepartureTime());
            }
        }
        for (Entry<Route, Set<FlightRecord>> e : o.getRoutesAndRuns().entrySet()) {
            Set<FlightRecord> runs = e.getValue();
            boolean runsToday = false;
            for (FlightRecord run : runs) {
                if (run.getDate().equals(World.getCurrentDate())) {
                    runsToday = true; thatHasFlightToday = true; break;
                }
            }
            if (runsToday) {
                bestThatTime = Math.min(bestThatTime, e.getKey().getDepartureTime());
            }
        }
        if (thisHasFlightToday) {
            if (thatHasFlightToday) {
                if (bestThisTime < bestThatTime) {
                    return -1;
                } else if (bestThatTime < bestThisTime) {
                    return 1;
                }
            } else {
                return -1;
            }
        } else if (thatHasFlightToday) {
            return 1;
        }

        //neigh implementing beyond today
        return 0;
    }

    @Override
    public String toString() {
    	String toReturn = this.getCode() + ",";
    	for(Route r : this.getRoutesAndRuns().keySet()) {
    		toReturn += r.toString() +",";
    	}
    	return toReturn;
    }

}
