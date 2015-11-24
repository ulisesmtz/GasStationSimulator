class Car {
	private int id;
	private int duration;
	private int arrive;

	Car() {
		this(0, 0, 0);
	}

	Car(int carid, int serviceDuration, int arrivalTime) {
		id = carid;
		duration = serviceDuration;
		arrive = arrivalTime;
	}

	int getDuration() {
		return duration;
	}

	int getArrive() {
		return arrive;
	}

	int getID() {
		return id;
	}

	public String toString() {
		return "" + id + ":" + duration + ":" + arrive;
	}

}
