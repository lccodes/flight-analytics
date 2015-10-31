package edu.brown.flights.transforms.estimation;

import java.util.List;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.apache.spark.mllib.regression.LinearRegressionModel;
import org.apache.spark.mllib.regression.LinearRegressionWithSGD;

import com.google.common.base.Optional;

public class EstimatorUtil {
	/*
	 * Returns a trained regression model for lateness
	 * @param List<LabeledPoint>
	 */
	public static LinearRegressionModel trainRegression(JavaRDD<LabeledPoint> labeledData,
			Optional<JavaSparkContext> jsc) {
		labeledData.cache();
		final LinearRegressionModel model = 
			      LinearRegressionWithSGD.train(JavaRDD.toRDD(labeledData), 1);
		
		return model;
	}
	
	/*
	 * Returns a trained regression model for lateness
	 * @param List<LabeledPoint>
	 */
	public static LinearRegressionModel trainRegression(List<LabeledPoint> labeledData,
			Optional<JavaSparkContext> jsc) {
		
		JavaRDD<LabeledPoint> training;
		if (jsc.isPresent()) {
			training = jsc.get().parallelize(labeledData);
			
			training.cache();
			final LinearRegressionModel model = 
				      LinearRegressionWithSGD.train(JavaRDD.toRDD(training), 1);
			return model;
		}else {
			JavaSparkContext theJsc = new JavaSparkContext(new SparkConf().setAppName("Anon Estimator").setMaster("local[1]"));
			training = theJsc.parallelize(labeledData);
			
			training.cache();
			final LinearRegressionModel model = 
				      LinearRegressionWithSGD.train(JavaRDD.toRDD(training), 1);
			
			theJsc.close();
			return model;
		}
	}
}
