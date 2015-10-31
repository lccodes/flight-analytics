package edu.brown.flights.types;

import java.util.Comparator;

import edu.brown.flights.types.planes.FlightInstance;
import edu.brown.flights.utils.TimeHelpers;

public class InstanceComparator implements Comparator<FlightInstance>{

	@Override
	public int compare(FlightInstance o1, FlightInstance o2) {
		int days = TimeHelpers.compareDatesForSoonness(o1.getSpecs().getDate(), o2.getSpecs().getDate());
		if (days == 0) {
			if (o1.getRoute().getDepartureTime() > o2.getRoute().getDepartureTime()) {
				return -1;
			} else if (o1.getRoute().getDepartureTime() < o2.getRoute().getDepartureTime()){
				return 1;
			}

			return 0;
		}else {
			return -1*days;
		}
	}

}
