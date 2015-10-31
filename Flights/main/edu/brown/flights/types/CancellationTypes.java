package edu.brown.flights.types;

public enum CancellationTypes {

    A,
    B,
    C,
    D;

    public static CancellationTypes fromString(String x) {
        if (x.equalsIgnoreCase("A")) {
            return A;
        } else if (x.equalsIgnoreCase("B")) {
            return B;
        } else if (x.equalsIgnoreCase("C")) {
            return C;
        } else if (x.equalsIgnoreCase("D")) {
            return D;
        } else {
            throw new IllegalArgumentException("Not a valid type ;(");
        }
    }

}
