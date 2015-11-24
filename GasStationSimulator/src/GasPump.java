class GasPump {

	// define constants for representing intervals
	static int BUSY = 1;
	static int FREE = 0;

	// start time and end time of current interval
	private int startTime;
	private int endTime;

	// pump id and current car which is served by this gas pump
	private int pumpID;
	private Car currentCar;

	// for keeping statistical data
	private int totalFreeTime;
	private int totalBusyTime;
	private int totalCars;

	// Constructor
	GasPump() {
		this(0);
	}

	// Constructor with gas pump id
	GasPump(int gasPumpId) {
		pumpID = gasPumpId;
	}

	int getGasPumpID() {
		return pumpID;
	}

	Car getCurrentCar() {
		return currentCar;
	}

	int getEndBusyIntervalTime() {
		return endTime;
	}

	// FREE -> BUSY :
	void switchFreeToBusy(Car currentCar, int currentTime) {
		totalFreeTime = totalFreeTime + (endTime - startTime);
		startTime = currentTime;
		this.currentCar = currentCar;
		endTime = currentTime + currentCar.getDuration();

		totalCars++;
	}

	// BUSY -> FREE :
	Car switchBusyToFree() {
		totalBusyTime = totalBusyTime + (endTime - startTime);
		startTime = endTime;
		return currentCar;
	}

	// use this method at the end of simulation to update gas pump data in free
	// and busy queues
	void setEndSimulationTime(int endsimulationtime, int intervalType) {
		endTime = endsimulationtime;
		if (intervalType == FREE) {
			totalFreeTime = totalFreeTime + (endTime - startTime);
		} else {
			totalBusyTime = totalBusyTime + (endTime - startTime);
		}
	}

	void printStatistics() {
		System.out.println("\t\tGasPump ID           : " + pumpID);
		System.out.println("\t\tTotal free time      : " + totalFreeTime);
		System.out.println("\t\tTotal service time   : " + totalBusyTime);
		System.out.println("\t\tTotal # of cars      : " + totalCars);
		if (totalCars > 0) {
			System.out.println(("\t\tAverage service time : "
					+ String.format("%.2f", (totalBusyTime * 1.0) / totalCars)) +"\n");
		}
	}

	public String toString() {
		return "GasPump:" + pumpID + ":" + startTime + "-" + endTime + ":Car:"
				+ currentCar;
	}
}
