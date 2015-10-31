package edu.brown.flights.transforms.estimation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.mllib.linalg.Vectors;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.apache.spark.mllib.regression.LinearRegressionModel;
import org.apache.spark.mllib.tree.DecisionTree;
import org.apache.spark.mllib.tree.model.DecisionTreeModel;

import com.google.common.base.Optional;

import edu.brown.flights.transforms.Views;
import edu.brown.flights.transforms.estimation.types.Lateness;
import edu.brown.flights.types.Route;
import edu.brown.flights.types.World;
import edu.brown.flights.types.planes.FlightRecord;
import edu.brown.flights.types.planes.PlaneFlight;

public class Estimators {
	
	static final private int MAXDEPTH = 5;
	static final private int MAXBINS = 32;
	
	/*
	 * Estimates the departure lateness by flightrecord
	 * @return map of flights to lateness prediction
	 */
	public static Map<FlightRecord, Lateness> estimateDepartureAccuracy(World w, 
			Optional<LinearRegressionModel> model) {
		return latenessConfigure(w, true, model);
	}
	
	/*
	 * Estimates the arrival lateness by flightrecord
	 * @return map of flights to lateness prediction
	 */
	public static Map<FlightRecord, Lateness> estimateArrivalAccuracy(World w,
			Optional<LinearRegressionModel> model) {
		return latenessConfigure(w, false, model);
	}
	
	/*
	 * Lets you configure where you are looking for lateness
	 */
	private static Map<FlightRecord, Lateness> latenessConfigure(World w, 
			boolean dept, Optional<LinearRegressionModel> potentialModel) {
		Map<FlightRecord, Lateness> estimations = new HashMap<FlightRecord, Lateness>();
		//Setup spark
		SparkConf conf = new SparkConf().setAppName("Estimate Turnaround").setMaster("local[1]");
		JavaSparkContext jsc = new JavaSparkContext(conf);
		
		Map<FlightRecord, Route> data = Views.getAllFlights(w);
		List<LabeledPoint> labeledData = new ArrayList<LabeledPoint>(data.size());
		Map<LabeledPoint, FlightRecord> matched = new HashMap<LabeledPoint, FlightRecord>(data.size());
		//Convert to labeled points
		for (Entry<FlightRecord, Route> pair : data.entrySet()) {
			if (pair.getKey().getDate() != null) {
				double[] v = new double[]{
						//pair.getKey().getPlane().getType().getValue(), add when this is useful
						pair.getKey().getActualArrTime(),
						pair.getKey().getDate().hashCode(),
						pair.getValue().getDepartureTime(),
						pair.getValue().getOrigin().hashCode(),
						pair.getValue().getDestination().hashCode()
						};
				
				LabeledPoint lp = null;
				if (pair.getKey().ran()) {
					int diff = pair.getKey().getActualDepTime() - pair.getValue().getDepartureTime();
					if (!dept) {
						diff = pair.getKey().getActualArrTime() - pair.getValue().getArrivalTime();
					}
				
					lp = new LabeledPoint(
						diff,
						Vectors.dense(v));
					labeledData.add(lp);
				}
				matched.put(lp, pair.getKey());
			}
		}
		
		//Abstracts out the model for easy improvement
		LinearRegressionModel model = 
				potentialModel.or(EstimatorUtil.trainRegression(labeledData, Optional.of(jsc)));
		
		for (Entry<LabeledPoint, FlightRecord> match : matched.entrySet()) {
			int amt = (int) Math.round(model.predict(match.getKey().features()));
			Lateness howlate = null;
			if (amt < -1){
				howlate = Lateness.VERYEARLY;
			} else if (amt == -1) {
				howlate = Lateness.EARLY;
			} else if (amt > 1) {
				howlate = Lateness.VERYLATE;
			} else if (amt == 1) {
				howlate = Lateness.LATE;
			} else {
				howlate = Lateness.ONTIME;
			}
			
			estimations.put(match.getValue(), howlate);
		}
		
		//Close
		jsc.close();
		return estimations;
	}
	
	/*
	 * Estimate lateness by route
	 */
	public static Map<Route, Lateness> estimateDelayByRoute(World w) {
		Map<Route, Double> routes = new HashMap<Route, Double>();
		List<LabeledPoint> points = new ArrayList<LabeledPoint>();
		Map<LabeledPoint, Route> eval = new HashMap<LabeledPoint, Route>();
		//Spark init
		SparkConf sparkConf = new SparkConf().setAppName("Route Partition").setMaster("local[1]");
		JavaSparkContext sc = new JavaSparkContext(sparkConf);
		
		for (PlaneFlight pf : w.getCodesToFlightsMap().values()) {
			for (Entry<Route, Set<FlightRecord>> r : pf.getRoutesAndRuns().entrySet()) {
				for (FlightRecord indiv : r.getValue()) {
					int label = 0;
					int delay = indiv.getActualArrTime() - r.getKey().getArrivalTime();
					if (delay > 15) {
						label = 4;
					} else if (delay < 15 && delay > 0) {
						label = 3;
					} else if (delay == 0) {
						label = 2;
					} else if (delay > -15) {
						label = 1;
					} else {
						label = 0;
					}
					
					LabeledPoint lp = new LabeledPoint(
							label,
							Vectors.dense(new double[]{
									indiv.getActualDepTime(),
									r.getKey().getFlightTime(),
									r.getKey().getOrigin().hashCode(),
									r.getKey().getDestination().hashCode()}));
					points.add(lp);
					eval.put(lp, r.getKey());
				}
			}
		}
		
		//Forest parameters
		Map<Integer, Integer> categoricalFeaturesInfo = new HashMap<Integer, Integer>();
		
		//Forest
		final DecisionTreeModel model = DecisionTree.trainClassifier(sc.parallelize(points), 5,
				  categoricalFeaturesInfo, "entropy", MAXDEPTH, MAXBINS);
		
		for (Entry<LabeledPoint, Route> toEval : eval.entrySet()) {
			double add = model.predict(toEval.getKey().features());
			if (routes.containsKey(toEval.getValue())) {
				routes.put(toEval.getValue(), (routes.get(toEval.getValue())+add)/2);
			} else {
				routes.put(toEval.getValue(), add);
			}
		}
		
		//Conversion
		Map<Route, Lateness> lateRoutes = new HashMap<Route, Lateness>(routes.size());
		for (Entry<Route, Double> one : routes.entrySet()) {
			Lateness l = null;
			if (one.getValue() > 3.5) {
				l = Lateness.VERYLATE;
			} else if (one.getValue() >= 2.5) {
				l = Lateness.LATE;
			} else if (one.getValue() >= 1.5) {
				l = Lateness.ONTIME;
			} else if (one.getValue() >= .5) {
				l = Lateness.EARLY;
			} else {
				l = Lateness.VERYLATE;
			}
			
			lateRoutes.put(one.getKey(), l);
		}
		
		//Close out
		sc.close();
		
		return lateRoutes;
	}

}
