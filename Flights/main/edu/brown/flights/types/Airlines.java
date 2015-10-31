package edu.brown.flights.types;

public enum Airlines {

    DELTA,
    AMERICAN,
    US_AIRWAYS,
    UNITED,
    SOUTHWEST;

    public String getAbbreviation() {
        switch (this) {
        case DELTA :
            return "DL";
        case AMERICAN :
            return "AA";
        case US_AIRWAYS :
            return "US";
        case UNITED :
            return "UA";
        case SOUTHWEST :
            return "WN";
        }
        return "Not supported at the moment";
    }

    public static Airlines getAirlineFromAbbreviation(String abbreviation) {
        if (abbreviation.equals("DL")) {
            return DELTA;
        } else if (abbreviation.equals("AA")) {
            return AMERICAN;
        } else if (abbreviation.equals("US")) {
            return US_AIRWAYS;
        } else if (abbreviation.equals("UA")) {
            return UNITED;
        } else if (abbreviation.equals("WN")) {
            return SOUTHWEST;
        } else {
            throw new IllegalArgumentException("Give a valid code");
        }
    }
    
    public static Airlines getRandom() {
    	switch((int) (4 * Math.random())) {
    	case 0: return Airlines.AMERICAN;
    	case 1: return Airlines.DELTA;
    	case 2: return Airlines.SOUTHWEST;
    	case 3: return Airlines.UNITED;
    	case 4: return Airlines.US_AIRWAYS;
    	default: return Airlines.DELTA;
    	}
    }

}
