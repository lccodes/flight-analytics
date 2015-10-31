package edu.brown.flights.types.planes;

import edu.brown.flights.types.Airlines;
import edu.brown.flights.types.Route;

public class FlightInstance {

    private Airlines carrier;
    private String code; //e.g. UA093
    private Route theRoute;
    private FlightRecord theSpecs;

    private FlightInstance(Airlines carrier, String code, Route route, FlightRecord specs) {
        this.carrier = carrier;
        this.code = code;
        this.theRoute = route;
        this.theSpecs = specs;
    }

    public FlightInstanceBuilder getBuilder() {
        return new FlightInstanceBuilder(this);
    }

    public int getSeatsLeft() {
        return this.theSpecs.getPlane().getCapacity() - this.theSpecs.getPassengers().size();
    }

    public static class FlightInstanceBuilder {

        private Airlines carrier;
        private String code; //e.g. UA093
        private Route theRoute;
        private FlightRecord theSpecs;

        public FlightInstanceBuilder() {}

        public FlightInstanceBuilder(FlightInstance x) {
            this.carrier = x.getCarrier();
            this.code = x.getCode();
            this.theRoute = x.getRoute();
            this.theSpecs = x.getSpecs();
        }

        public FlightInstanceBuilder withRoute(Route x) {
            this.theRoute = x; return this;
        }

        public FlightInstanceBuilder withSpecs(FlightRecord x) {
            this.theSpecs = x; return this;
        }

        public FlightInstanceBuilder withCarrier(Airlines x) {
            this.carrier = x; return this;
        }

        public FlightInstanceBuilder withCode(String x) {
            this.code = x; return this;
        }

        public FlightInstance build() {
            return new FlightInstance(carrier, code, theRoute, theSpecs);
        }


    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((carrier == null) ? 0 : carrier.hashCode());
        result = prime * result + ((code == null) ? 0 : code.hashCode());
        result = prime * result + ((theRoute == null) ? 0 : theRoute.hashCode());
        result = prime * result + ((theSpecs == null) ? 0 : theSpecs.hashCode());
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
        FlightInstance other = (FlightInstance) obj;
        if (carrier != other.carrier)
            return false;
        if (code == null) {
            if (other.code != null)
                return false;
        } else if (!code.equals(other.code))
            return false;
        if (theRoute == null) {
            if (other.theRoute != null)
                return false;
        } else if (!theRoute.equals(other.theRoute))
            return false;
        if (theSpecs == null) {
            if (other.theSpecs != null)
                return false;
        } else if (!theSpecs.equals(other.theSpecs))
            return false;
        return true;
    }
    public Airlines getCarrier() {
        return carrier;
    }
    public Route getRoute() {
        return theRoute;
    }
    public void setTheRoute(Route theRoute) {
        this.theRoute = theRoute;
    }
    public FlightRecord getSpecs() {
        return theSpecs;
    }
    public void setTheSpecs(FlightRecord theSpecs) {
        this.theSpecs = theSpecs;
    }
    public void setCarrier(Airlines carrier) {
        this.carrier = carrier;
    }
    public String getCode() {
        return code;
    }
    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return this.getCode() + ": " + this.getRoute().getOrigin().getCode() + "->" + this.getRoute().getDestination().getCode()
                + ", " + this.getRoute().getDepartureTime() + "-" + this.getRoute().getArrivalTime() + ", " + this.getSpecs().getDate();
    }

}
