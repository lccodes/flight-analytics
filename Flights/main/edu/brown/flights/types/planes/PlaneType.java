package edu.brown.flights.types.planes;

public enum PlaneType {
	BOEING737("BOEING", 737),
	BOEING747("BOEING", 747),
	BOEING767("BOEING", 767),
	BOEING777("BOEING", 777),
	DREAMLINER("BOEING", 787),
	AIRBUS380("AIRBUS", 380);
	
	private final String make;
	private final int model;
	PlaneType(String make, int model) {
		this.make = make;
		this.model = model;
	}
	
	public String getMake() {
		return make;
	}
	
	public int getModel() {
		return model;
	}
	
	public double getValue() {
		switch(this) {
		case BOEING737: return 0;
		case BOEING747: return 1;
		case BOEING767: return 2;
		case BOEING777: return 3;
		case DREAMLINER: return 4;
		case AIRBUS380: return 5;
		default:
			return 6;
		}
	}
}
