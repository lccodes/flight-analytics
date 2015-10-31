package edu.brown.flights.transforms.estimation.types;

public enum Lateness {
	VERYEARLY,
	EARLY,
	ONTIME,
	LATE,
	VERYLATE;
	
	@Override
	public String toString() {
		switch(this){
		case VERYEARLY: return "0";
		case EARLY: return "1";
		case ONTIME: return "2";
		case LATE: return "3";
		case VERYLATE: return "4";
		default:
			throw new IllegalArgumentException("Not valid Lateness");
		}
	}

}
