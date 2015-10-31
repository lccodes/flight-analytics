package edu.brown.flights.types;

public class Route {

    private Airport origin;
    private Airport destination;
    private int departureTime;
    private int arrivalTime;
    private int flightTime; //allows us to circumvent time zone diffs, for more complicated math

    public Route() {}

    public static class RouteBuilder {

        private String originAirportCode;
        private String originAirportCity;
        private String originAirportState;
        private String destAirportCode;
        private String destAirportCity;
        private String destAirportState;
        private int depTime;
        private int arrTime;
        private int flTime;

        public RouteBuilder withOriginAirport(String x) {
            originAirportCode = x; return this;
        }

        public RouteBuilder withOriginCity(String x) {
            originAirportCity = x; return this;
        }

        public RouteBuilder withOriginState(String x) {
            originAirportState = x; return this;
        }

        public RouteBuilder withDestAirport(String x) {
            destAirportCode = x; return this;
        }

        public RouteBuilder withDestCity(String x) {
            destAirportCity = x; return this;
        }

        public RouteBuilder withDestState(String x) {
            destAirportState = x; return this;
        }

        public RouteBuilder departingAt(int x) {
            depTime = x; return this;
        }

        public RouteBuilder arrivingAt(int x) {
            arrTime = x; return this;
        }

        public RouteBuilder withFlightTime(int x) {
            flTime = x; return this;
        }

        public Route build() {
            Route toReturn = new Route();
            Airport origin = new Airport(originAirportCode, originAirportCity + ", " + originAirportState);
            Airport dest = new Airport(destAirportCode, destAirportCity + ", " + destAirportState);
            toReturn.setOrigin(origin);
            toReturn.setDestination(dest);
            toReturn.setDepartureTime(depTime);
            toReturn.setArrivalTime(arrTime);
            toReturn.setFlightTime(flTime);
            return toReturn;
        }

    }

    public Airport getOrigin() {
        return origin;
    }
    public void setOrigin(Airport origin) {
        this.origin = origin;
    }
    public Airport getDestination() {
        return destination;
    }
    public void setDestination(Airport destination) {
        this.destination = destination;
    }
    public int getDepartureTime() {
        return departureTime;
    }
    public void setDepartureTime(int departureTime) {
        this.departureTime = departureTime;
    }
    public int getArrivalTime() {
        return arrivalTime;
    }
    public void setArrivalTime(int arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + arrivalTime;
        result = prime * result + departureTime;
        result = prime * result + ((destination == null) ? 0 : destination.hashCode());
        result = prime * result + ((origin == null) ? 0 : origin.hashCode());
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
        Route other = (Route) obj;
        if (arrivalTime != other.arrivalTime)
            return false;
        if (departureTime != other.departureTime)
            return false;
        if (destination == null) {
            if (other.destination != null)
                return false;
        } else if (!destination.equals(other.destination))
            return false;
        if (origin == null) {
            if (other.origin != null)
                return false;
        } else if (!origin.equals(other.origin))
            return false;
        return true;
    }
    public int getFlightTime() {
        return flightTime;
    }
    public void setFlightTime(int flightTime) {
        this.flightTime = flightTime;
    }
    
    @Override
    public String toString() {
    	return "{"+ this.origin + "->" + this.destination + "@" +  this.departureTime + "}"; 
    }

}
