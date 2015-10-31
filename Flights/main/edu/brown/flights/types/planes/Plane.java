package edu.brown.flights.types.planes;

import java.util.Map;
import java.util.Set;

import com.google.common.base.Optional;

import edu.brown.flights.transforms.Views;
import edu.brown.flights.types.Route;
import edu.brown.flights.types.World;

public class Plane {

    private final int capacity;
    private final String tailNum;
    private final PlaneType type;
    private final int defaultCapacity = 200;
    private final PlaneType defaultPlaneType = PlaneType.BOEING737;

    public Plane(String tailNum) {
        this.tailNum = tailNum;
        this.capacity = defaultCapacity;
        this.type = defaultPlaneType;
    }

    public Plane(int capacity, String tailNum, PlaneType type) {
        this.capacity = capacity;
        this.tailNum = tailNum;
        this.type = type;
    }

    public int getCapacity() {
        return capacity;
    }

    public String getTailNum() {
        return tailNum;
    }

    public PlaneType getType() {
    	return type;
    }

    public Optional<FlightInstance> getNextFlight(final String date, final int time, World w) {
        FlightInstance bestInstance = null;

        Map<Plane, Set<PlaneFlight>> viewsByPlane = Views.getFlightsByTail(w);
        Set<PlaneFlight> associatedFlights = viewsByPlane.get(this);
        if (associatedFlights != null && !associatedFlights.isEmpty()) {
            for (PlaneFlight f : associatedFlights) {
                Optional<FlightInstance> mapPoss = f.getNextFlight(date, time);
                if (mapPoss.isPresent()) {
                    FlightInstance instance = mapPoss.get();
                    Route theRoute = instance.getRoute();
                    FlightRecord theSpecs = instance.getSpecs();
                    if (theRoute != null && theSpecs != null) {
                        if (!theSpecs.isCancelled() && !theSpecs.isDiverted()
                                && theRoute.getDepartureTime() < bestInstance.getRoute().getDepartureTime()) {
                            bestInstance = instance;
                        }
                    }
                }
            }
        }

        if (bestInstance != null) {
            return Optional.of(bestInstance);
        }

        return Optional.absent();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + capacity;
        result = prime * result + ((tailNum == null) ? 0 : tailNum.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
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
        Plane other = (Plane) obj;
        if (capacity != other.capacity)
            return false;
        if (tailNum == null) {
            if (other.tailNum != null)
                return false;
        } else if (!tailNum.equals(other.tailNum))
            return false;
        if (type != other.type)
            return false;
        return true;
    }

}
