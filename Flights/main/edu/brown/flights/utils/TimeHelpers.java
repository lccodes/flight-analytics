package edu.brown.flights.utils;

import edu.brown.flights.types.planes.FlightInstance;

public class TimeHelpers {

    private TimeHelpers() {
        throw new UnsupportedOperationException("Utility class, do not instantiate");
    }

    /**
     * also assumes that times are neigh more than 24 hrs apart
     */
    public static int getDifferenceAsTimeRepresentation(int timeFinal, int timeInitial) {

        int finalHour = timeFinal / 100;
        int initialHour = timeInitial / 100;
        int finalMins = timeFinal % 100;
        int initialMins = timeInitial % 100;
        int resultHours = 0;
        int resultMins = 0;

        if (finalHour < initialHour) {
            finalHour += 24;
        }
        resultHours = finalHour - initialHour;
        resultMins = finalMins - initialMins;
        if (resultMins < 0) {
            resultHours--;
            resultMins += 60;
        }
        return timeFinal > timeInitial ? (resultHours * 100) + resultMins : -1 * (resultHours * 100) + resultMins;
    }

    /**
     * Returns 1 if the first date is sooner, -1 if the latter date is sooner,
     * and 0 if they're the same day
     * @param date1
     * @param date2
     * @return
     */
    public static int compareDatesForSoonness(String date1, String date2) {
        if (!date1.matches("[0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9]") || !date2.matches("[0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9]")) {
        	throw new IllegalArgumentException("Please provide a date in the form of 2001-09-11 as the first argument:" + date1 + "||" + date2);
        }
        String[] date1Components = date1.split("-");
        String[] date2Components = date2.split("-");
        if (Integer.parseInt(date1Components[0]) > Integer.parseInt(date2Components[0])) {
            return -1;
        } else if (Integer.parseInt(date1Components[0]) < Integer.parseInt(date2Components[0])) {
            return 1;
        }

        if (Integer.parseInt(date1Components[1]) > Integer.parseInt(date2Components[1])) {
            return -1;
        } else if (Integer.parseInt(date1Components[1]) < Integer.parseInt(date2Components[1])) {
            return 1;
        }

        if (Integer.parseInt(date1Components[2]) > Integer.parseInt(date2Components[2])) {
            return -1;
        } else if (Integer.parseInt(date1Components[2]) < Integer.parseInt(date2Components[2])) {
            return 1;
        }

        return 0;
    }

    /*
     * Changes date single int to two digits to string
     */
    public static String toValidString(int x) {
        if (x < 1 || x > 99) {
            throw new IllegalArgumentException("come on");
        }
        if (x >= 10) {
            return Integer.toString(x);
        } else {
            return "0" + x;
        }
    }

    /*
     * Changes date single int to two digits to string
     */
    public static String toValidString(String x) {
        if (x.length() == 1) {
            return "0" + x;
        } else if (x.length() == 2) {
            return x;
        } else {
            throw new IllegalArgumentException();
        }
    }

    /*
     * Get's the next date
     */
    public static String getNextDate(String date) {
        if (!date.matches("[0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9]")) {
            throw new IllegalArgumentException("Please provide a date in the form of 2001-09-11 as the first argument");
        }
        String[] parts = date.split("-");
        int year = Integer.parseInt(parts[0]);
        int month = Integer.parseInt(parts[1]);
        int day = Integer.parseInt(parts[2]);

        if (!(month >= 1 && month <= 12) || !(day >= 1 && day <= 31)) {
            throw new IllegalArgumentException("Please provide a valid date");
        }

        if (day < 28) {
            day++;
            return year + "-" + toValidString(month) + "-" + toValidString(day);
        } else if (day == 29 && month == 2) {
            if (year % 4 == 0) {
                return year + "-03-01";
            } else {
                throw new IllegalArgumentException("only legit every 4 years");
            }
        } else if (day < 30 && month != 2) {
            day++;
            return year + "-" + toValidString(month) + "-" +  toValidString(day);
        } else if (day == 30) {
            if (month == 4 || month == 6 || month == 9 || month == 11) {
                month++;
                day = 1;
                return year + "-" + toValidString(month) + "-" +  toValidString(day);
            } else {
                day++;
                return year + "-" + toValidString(month) + "-" +  toValidString(day);
            }
        } else if (day == 31) {
            if (month == 12) {
                year++; month = 1; day = 1;
                return year + "-" + toValidString(month) + "-" +  toValidString(day);
            } else {
                month++;
                day = 1;
                return year + "-" + toValidString(month) + "-" +  toValidString(day);
            }
        } else {
            throw new IllegalArgumentException("neigh a valid date");
        }
    }

    /*
     * Adds some number of minutes and returns a valid hour representation
     */
    public static int addMinutes(int hours, int minutes) {
    	int originalHours = (hours / 100) * 100;
    	int originalMins = (hours % 100);
    	int addHours = minutes/60;
    	int leftover = minutes % 60;

    	int hrsToAdd = (originalMins + leftover) / 60;
    	int minsToAdd = (originalMins + leftover) % 60;

    	originalHours += (100 * addHours) + (hrsToAdd * 100) + minsToAdd;

    	if (originalHours >= 2400) {
    		originalHours -= 2400;
    	}

    	return originalHours;
    }

    public static int addMinutes(int hours, int minutes, String date, StringBuilder sb) {
        int originalHours = (hours / 100) * 100;
        int originalMins = (hours % 100);
        int addHours = minutes / 60;
        int leftover = minutes % 60;
        int hrsToAdd = (originalMins + leftover) / 60;
        int minsToAdd = (originalMins + leftover) % 60;

        originalHours += (100 * addHours) + (hrsToAdd * 100) + minsToAdd;

        if (originalHours >= 2400) {
            sb.append(TimeHelpers.getNextDate(date));
            originalHours -= 2400;
        } else {
            sb.append(date);
        }

        return originalHours;
    }

    /*
     * Calculates timezone difference based on deptTime, arrvTime, and duration
     * +X means foreign time is ahead by X
     */
    public static int timezoneCalculator(int departure, int arrival, int duration) {
    	int localArrival = addMinutes(departure, duration);

    	return arrival - localArrival;
    }

    public static int minutesBetween(int one, int two) {
        if (one > two) {
            two += 2400;
        }
        return ((two / 100) * 60) - ((one / 100) * 60) + (two - ((two / 100) * 100)) - (one - ((one / 100) * 100));
    }

    public static int compareDatesAndTimes(String date1, int time1, String date2, int time2) {

        if (TimeHelpers.compareDatesForSoonness(date1, date2) > 0) {
            return -1;
        } else if (TimeHelpers.compareDatesForSoonness(date1, date2) < 0) {
            return 1;
        } else if (time1 > time2) {
            return 1;
        } else if (time1 < time2) {
            return -1;
        }
        return 0;
    }
    
    public static int compareFlightDepartureTimes(FlightInstance flight1, FlightInstance flight2) {
    	return compareDatesAndTimes(flight1.getSpecs().getDate(), flight1.getRoute().getDepartureTime(),
    			flight2.getSpecs().getDate(), flight2.getRoute().getDepartureTime());
    }
    
    public static int compareFlightArrivalTimes(FlightInstance flight1, FlightInstance flight2) {
    	return compareDatesAndTimes(flight1.getSpecs().getDate(), flight1.getRoute().getArrivalTime(),
    			flight2.getSpecs().getDate(), flight2.getRoute().getArrivalTime());
    }

}

