package edu.brown.flights.types;

/**
 * We is need to add geospatial data to this shit (maybe at build time, take the
 * code and use it as an argument to look up in a table w/the geospatial/other data)
 */
public class Airport {

    private String code; //e.g. "LAX"
    private String city; //e.g. "Los Angeles"
    private String state; //e.g. "CA"
    private String country = "USA";
    private boolean hasDeltaLounge;

    public Airport(String code, String city, String state, String country) {
        this.setCode(code);
        this.setCity(city);
        this.setState(state);
        this.setCountry(country);
    }

    /**
     * @param cityAndState - matches the pattern <city>, <state>
     * assumes country is the US
     */
    public Airport(String code, String cityAndState) {
        String[] components = cityAndState.split(", ");
        if (components.length != 2) {
            throw new IllegalArgumentException("Please provide an input in the form of Palo Alto, CA");
        }
        this.setCode(code);
        this.setCity(components[0]); this.setState(components[1]);
        this.setCode(code);
    }

    public Airport withDeltaLounge() {
        this.hasDeltaLounge = true; return this;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public boolean isThereDeltaLoungeOrNah() {
        return hasDeltaLounge;
    }

    public void setHasDeltaLounge(boolean hasDeltaLounge) {
        this.hasDeltaLounge = hasDeltaLounge;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((city == null) ? 0 : city.hashCode());
        result = prime * result + ((code == null) ? 0 : code.hashCode());
        result = prime * result + ((country == null) ? 0 : country.hashCode());
        result = prime * result + (hasDeltaLounge ? 1231 : 1237);
        result = prime * result + ((state == null) ? 0 : state.hashCode());
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
        Airport other = (Airport) obj;
        return this.code.equals(other.getCode());
    }
    
    @Override
    public String toString() {
    	return code;
    }

}
