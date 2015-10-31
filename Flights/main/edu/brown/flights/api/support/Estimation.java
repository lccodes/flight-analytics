package edu.brown.flights.api.support;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.apache.spark.mllib.regression.LinearRegressionModel;

import com.google.common.base.Optional;

import edu.brown.flights.transforms.estimation.EstimatorUtil;

/**
 * @PublicApi
 * Estimation api functions
 */
public class Estimation {
	/**
	 * Trains an estimator for a given JavaRDD<LabeledPoint>
	 * @param JavaRDD<LabeledPoint>
	 * @return LinearRegressionModel
	 */
	public static LinearRegressionModel trainEstimator(JavaRDD<LabeledPoint> points) {
		Optional<JavaSparkContext> none = Optional.absent();
		return EstimatorUtil.trainRegression(points, none);
	}
}
