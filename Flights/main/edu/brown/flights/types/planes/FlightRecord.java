package edu.brown.flights.types.planes;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.google.common.base.Optional;

import edu.brown.flights.types.CancellationTypes;
import edu.brown.flights.types.Passenger;
/**
 * All of the information that would be relevant to the specific run of a flight
 * @author crobotham
 *
 */
public class FlightRecord {

    private Plane plane;
    private String flightID; //e.g. UA093
    private String date;
    private Set<Long> passengers = new HashSet<Long>();
    private boolean isCancelled;
    private Optional<CancellationTypes> cancellationInfo = Optional.absent();
    private boolean isDiverted;
    private int actualDepTime;
    private int actualArrTime;

    public String getFlightID() {
        return flightID;
    }

    public void setFlightID(String flightID) {
        this.flightID = flightID;
    }

    public int getActualDepTime() {
        return actualDepTime;
    }

    public void setActualDepTime(int actualDepTime) {
        this.actualDepTime = actualDepTime;
    }

    public int getActualArrTime() {
        return actualArrTime;
    }

    public void setActualArrTime(int actualArrTime) {
        this.actualArrTime = actualArrTime;
    }

    public void setCancellationInfo(Optional<CancellationTypes> cancellationInfo) {
        this.cancellationInfo = cancellationInfo;
    }

    private FlightRecord(String tailNum, String flightID, String date, boolean cancelled, boolean diverted, int actDepTime, int actArrTime) {
        this.setPlane(new Plane(tailNum));
        this.flightID = flightID;
        this.setDate(date);
        this.setCancelled(cancelled);
        this.setDiverted(diverted);
        this.actualDepTime = actDepTime;
        this.actualArrTime = actArrTime;
    }

    public FlightRecordBuilder getBuilder() {
        return new FlightRecordBuilder(this.flightID);
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Set<Long> getPassengers() {
        return passengers;
    }

    public void setPassengers(Set<Long> passengers) {
        this.passengers = passengers;
    }

    public boolean isCancelled() {
        return isCancelled;
    }

    public void setCancelled(boolean isCancelled) {
        this.isCancelled = isCancelled;
    }

    public Optional<CancellationTypes> getCancellationInfo() {
        return cancellationInfo;
    }

    public void addCancellationInfo(CancellationTypes type) {
        this.cancellationInfo = Optional.of(type);
    }

    public boolean isDiverted() {
        return isDiverted;
    }

    public void addPassenger(Passenger p) {
        this.passengers.add(p.getUTI());
    }

    public void addPassengers(Collection<Passenger> passengers) {
        for (Passenger p : passengers) {
            this.passengers.add(p.getUTI());
        }
    }
    
    public void addPassengerByUTI(Long UTI) {
    	this.passengers.add(UTI);
    }
    
    public void addPassengersByUTI(Collection<Long> UTIs) {
    	for (Long UTI : UTIs) {
    		this.passengers.add(UTI);
    	}
    }
    
    public void removePassenger(Passenger p) {
    	this.passengers.remove(p.getUTI());
    }
    
    public void removePassengers(Collection<Passenger> passengers) {
    	for (Passenger p : passengers) {
    		this.removePassenger(p);
    	}
    }
    
    public void removePassengerByUTI(Long UTI) {
    	this.passengers.remove(UTI);
    }

    public void removePassengersByUTI(Collection<Long> UTIs) {
    	for (Long UTI : UTIs) {
    		this.removePassengerByUTI(UTI);
    	}
    }
    
    public void setDiverted(boolean isDiverted) {
        this.isDiverted = isDiverted;
    }

    public Plane getPlane() {
        return plane;
    }

    public void setPlane(Plane plane) {
        this.plane = plane;
    }

    public static class FlightRecordBuilder {

        private String tailNum;
        private String flightID; //e.g. UA093
        private String date;
        private Set<Long> passengers = new HashSet<Long>();
        private boolean isCancelled = false;
        private String cancellationInfo;
        private boolean isDiverted = false;
        private int actualDepTime;
        private int actualArrTime;

        public FlightRecordBuilder(String flightID) {
            this.flightID = flightID;
        }

        public FlightRecordBuilder withTailNum(String x) {
            this.tailNum = x; return this;
        }

        public FlightRecordBuilder onDate(String x) {
            this.date = x; return this;
        }

        public FlightRecordBuilder isCancelled() {
            this.isCancelled = true; return this;
        }

        public FlightRecordBuilder isDiverted() {
            this.isDiverted = true; return this;
        }

        public FlightRecordBuilder withCancellationType(String type) {
            this.cancellationInfo = type; return this;
        }

        public FlightRecordBuilder withPassenger(Passenger p) {
            this.passengers.add(p.getUTI()); return this;
        }

        public FlightRecordBuilder withPassengers(Collection<Passenger> passengers) {
            for (Passenger p : passengers) {
                this.passengers.add(p.getUTI());
            }
            return this;
        }

        public FlightRecordBuilder actuallyDepartedAt(int x) {
            this.actualDepTime = x; return this;
        }

        public FlightRecordBuilder actuallyArrivedAt(int x) {
            this.actualArrTime = x; return this;
        }

        public FlightRecord build() {
            FlightRecord toReturn = new FlightRecord(this.tailNum, this.flightID, this.date, this.isCancelled, this.isDiverted, this.actualDepTime, this.actualArrTime);
            if (this.isCancelled) {
                toReturn.addCancellationInfo(CancellationTypes.fromString(cancellationInfo));
            }
            return toReturn;
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((cancellationInfo == null) ? 0 : cancellationInfo.hashCode());
        result = prime * result + ((date == null) ? 0 : date.hashCode());
        result = prime * result + ((flightID == null) ? 0 : flightID.hashCode());
        result = prime * result + (isCancelled ? 1231 : 1237);
        result = prime * result + (isDiverted ? 1231 : 1237);
        result = prime * result + ((passengers == null) ? 0 : passengers.hashCode());
        result = prime * result + ((plane == null) ? 0 : plane.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        FlightRecord other = (FlightRecord) obj;
        if (cancellationInfo == null) {
            if (other.cancellationInfo != null)
                return false;
        } else if (!cancellationInfo.equals(other.cancellationInfo))
            return false;
        if (date == null) {
            if (other.date != null)
                return false;
        } else if (!date.equals(other.date))
            return false;
        if (flightID == null) {
            if (other.flightID != null)
                return false;
        } else if (!flightID.equals(other.flightID))
            return false;
        if (isCancelled != other.isCancelled)
            return false;
        if (isDiverted != other.isDiverted)
            return false;
        if (passengers == null) {
            if (other.passengers != null)
                return false;
        } else if (!passengers.equals(other.passengers))
            return false;
        if (plane == null) {
            if (other.plane != null)
                return false;
        } else if (!plane.equals(other.plane))
            return false;
        return true;
    }

    /*
     * Did the flight already occur?
     */
	public boolean ran() {
		return (this.actualArrTime != -1) && (this.actualDepTime != -1);
	}
	
	@Override
	public String toString() {
		return this.getFlightID() + "," + this.getDate();
	}

}
