package edu.brown.flights.api.versions;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.spark.mllib.regression.LinearRegressionModel;

import com.google.common.base.Optional;

import edu.brown.flights.api.support.AircraftDelays;
import edu.brown.flights.transforms.estimation.Estimators;
import edu.brown.flights.transforms.estimation.types.Lateness;
import edu.brown.flights.types.World;
import edu.brown.flights.types.planes.FlightInstance;
import edu.brown.flights.types.planes.FlightRecord;

public class V1 {

    /** Gets downstream effect of a late plane by aircraft only and
	 * returns this as a map of plane impacted to lateness.
	 * @param World : the Balrog world view
	 * @param minuteslate : int with number of minutes late
	 * @param turnAroundMinutes : int for how much time every plane is given to get out again
	 * @param code : String with flight code
	 * @param date : String with date
	 * @param hour : the hour e.g. 1824
	 *
	 * @return HashMap<PlaneFlight, Lateness>
	 */
	public static Map<FlightInstance, Integer> getAirplaneDelayByCode(World world,
			int minutesLate,
			int turnAroundMinutes,
			String code,
			String date,
			int hour) {
		return AircraftDelays.getAirplaneDelayMaster(world, minutesLate, 
				turnAroundMinutes, code, Optional.of(date), Optional.of(hour));
	}


	/** Gets downstream effect of a late plane TODAY by aircraft only and
    * returns this as a map of plane impacted to lateness.
    * @param World : the Balrog world view
    * @param minuteslate : int with number of minutes late
    * @param turnAroundMinutes : int for how much time every plane is given to get out again
    * @param code : String with flight code
    *
    * @return HashMap<PlaneFlight, Lateness>
    */
   public static Map<FlightInstance, Integer> getNextAirplaneDelayByCode(World world,
           int minutesLate,
           int turnAroundMinutes,
           String code) {
	   Optional<String> empty = Optional.absent();
	   Optional<Integer> other = Optional.absent();
       return AircraftDelays.getAirplaneDelayMaster(world,
               minutesLate,
               turnAroundMinutes,
               code,
               empty,
               other);
   }
   
   /** Gets downstream effect of a several late planes by aircraft only and
	 * returns this as a map of plane impacted to lateness.
	 * @param World : the Balrog world view
	 * @param minuteslate : int with number of minutes late
	 * @param turnAroundMinutes : int for how much time every plane is given to get out again
	 * @param code : String with flight code
	 * @param date : String with date
	 * @param hour : the hour e.g. 1824
	 *
	 * @return HashMap<PlaneFlight, Lateness>
	 */
	public static Map<FlightInstance, Integer> getSeveralAirplaneByCode(World world,
			int minutesLate,
			int turnAroundMinutes,
			Set<String> code,
			String date,
			int hour) {
		return AircraftDelays.getMultipleAirplaneDelys(world, minutesLate, 
				turnAroundMinutes, code, Optional.of(date), Optional.of(hour));
	}
	
	/**
	 * Gets the estimated DEPARTURE lateness for every flightrecord in the world
	 * @param World: the balrog world view
	 * @optional model : linear regression to use
	 * @return Map<PlaneRecord, Lateness> map of planerecords to their *predicted* lateness type
	 */
	public static Map<FlightRecord, Lateness> estimateDepartureLateness(World w,
			Optional<LinearRegressionModel> model) {
		return Estimators.estimateDepartureAccuracy(w, model);
	}
	
	/**
	 * Gets the estimated ARRIVAL lateness for every flightrecord in the world
	 * @param World: the balrog world view
	 * @optional model : linear regression to use
	 * @return string : csv file of flight to lateness
	 */
	public static String estimateArrivalLateness(World w,
			Optional<LinearRegressionModel> model) {
		Map<FlightRecord, Lateness> x = Estimators.estimateArrivalAccuracy(w, model);
		StringBuilder toCsv = new StringBuilder();
		for (Entry<FlightRecord, Lateness> one : x.entrySet()) {
			toCsv.append(one.getKey());
			toCsv.append(",");
			toCsv.append(one.getValue());
			toCsv.append("\n");
		}
		
		return toCsv.toString();
	}

}
