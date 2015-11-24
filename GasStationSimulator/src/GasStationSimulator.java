import java.util.*;
import java.io.*;

class GasStationSimulator {

	// input parameters
	private int numGasPumps, carQLimit;
	private int simulationTime, dataSource;
	private int chancesOfArrival, maxDuration;

	// statistical data
	private int numGoAway, numServed, totalWaitingTime;

	// internal data
	private int carIDCounter;
	private GasStation service; // Gas station object
	private Scanner scanner; // get car data from file
	private Random dataRandom; // get car data using random function

	// most recent car arrival info, see getCarData()
	private boolean anyNewArrival;
	private int serviceDuration;
	
	private boolean isFromFile;	// if true, reading numbers from text file was successful

	private GasStationSimulator() {
		scanner = new Scanner(System.in);
		dataRandom = new Random();
		numGoAway = 0;
		numServed = 0;
		totalWaitingTime = 0;
		carIDCounter = 0;
		isFromFile = false;
	}

	private void setupParameters() {
	
		System.out.println("\t*** Get Simulation Parameters ***");
		
		while (true) {
			System.out.print("Enter simulation time (positive integer, max 10000): ");
			simulationTime = scanner.nextInt();
			if (simulationTime <= 10000) {	// if valid input, break from loop
				break;
			}
		}
		
		while (true) {
			System.out.print("Enter maximum service duration of cars (max 500)   : ");
			maxDuration = scanner.nextInt();
			if (maxDuration <= 500) {	// if valid input, break from loop
				break;
			}
		}
		
		while (true) {
			System.out.print("Enter chances (0% < & <= 100%) of new car	   : ");
			chancesOfArrival = scanner.nextInt();
			if (chancesOfArrival <= 100) {	// if valid input, break from loop
				break;
			}
		}
		
		while (true) {
			System.out.print("Enter the number of service pumps (max 10)	   : ");
			numGasPumps = scanner.nextInt();
			if (numGasPumps <= 10) {	// if valid input, break from loop
				break;
			}
		}
		
		while (true) {
			System.out.print("Enter the car waiting queue limit (max 50)	   : ");
			carQLimit = scanner.nextInt();
			if (carQLimit <= 50) {	// if valid input, break from loop
				break;
			}
		}
		
		while (true) {
			System.out.print("Enter 1/0 to get data from file/Random		   : ");
			dataSource = scanner.nextInt();
			if (dataSource == 0 || dataSource == 1) {	// if valid input, break from loop
				break;
			}
		}
				
		if (dataSource == 1) {
			System.out.print("Enter file name (either DataFile or your own)	   : ");
			// DataFile only has numbers for up to simulation length of 500
			try {
				String fileName = scanner.next();
				File file = new File(fileName);
				scanner = new Scanner(file);
				isFromFile = true;	// able to find the file
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	private void getCarData() {
		// get next car data : from file or random number generator
		if (isFromFile) {
			int data1 = scanner.nextInt();
			int data2 = scanner.nextInt();
			anyNewArrival = (((data1%100) + 1) <= chancesOfArrival);
			serviceDuration = (data2%maxDuration) + 1;
		} else {
			anyNewArrival = ((dataRandom.nextInt(100)+1) <= chancesOfArrival);
			serviceDuration = dataRandom.nextInt(maxDuration)+1;
		}
	}

	private void doSimulation() {
		service = new GasStation(numGasPumps, carQLimit, 1);
		
		// Time driver simulation loop
		for (int currentTime = 0; currentTime < simulationTime; currentTime++) {
			System.out.println("===========================");
			System.out.println("Time " + currentTime + "\n");

			// Step 1: any new car enters the gas station?
			getCarData();
			if (anyNewArrival) {

				// Step 1.1: setup car data
				// Step 1.2: check car waiting queue too long?
				carIDCounter++;		// since CarIDCounter is initialized to 0, it is incremented
									// before car object is created
				Car newCar = new Car(carIDCounter, serviceDuration, currentTime);
				
				if (!service.isCarQTooLong()) {	// if line is not too long, add car to queue
					service.insertCarQ(newCar);
					System.out.println("Car " + carIDCounter + " enters line. Transaction "
							+ "time is " + serviceDuration);
				} else {	// else don't add to queue and increase numGoAway
					numGoAway++;
					System.out.println("Car " + carIDCounter + " left because line is too long");
				}
				
			} else {	// no car arrived
				System.out.println("\tNo new car!");
			}

			// Step 2: free busy pumps, add to free pumpQ
			while (!service.emptyBusyGasPumpQ()) {
				GasPump gasPump = service.getFrontBusyGasPumpQ();
				if (gasPump.getEndBusyIntervalTime() == currentTime) {
					gasPump = service.removeBusyGasPumpQ();
					Car car = gasPump.switchBusyToFree();
					service.insertFreeGasPumpQ(gasPump);
					System.out.println("Car " + car.getID() + " finished. Gas pump #" 
							+ gasPump.getGasPumpID() + " is free.");
				} else {
					break;
				}
			}

			// Step 3: get free pumps to serve waiting cars
			while (!service.emptyCarQ() && !service.emptyFreeGasPumpQ()) {
				Car nextCar = service.removeCarQ();
				int timeWaited = currentTime - nextCar.getArrive();
				totalWaitingTime = totalWaitingTime + timeWaited;
				GasPump nextGasPump = service.removeFreeGasPumpQ();
				nextGasPump.switchFreeToBusy(nextCar, currentTime);
				service.insertBusyGasPumpQ(nextGasPump);
				numServed++;
				System.out.println("Car " + nextCar.getID() + " begins service at gas pump #"
						+ nextGasPump.getGasPumpID() + ". Time waited is " + timeWaited);
			}

		} // end simulation loop

		// clean-up
		ArrayList<GasPump> temp = new ArrayList<GasPump>();	// used to hold gaspumps while they are
															// removed from gas station
		int sizeBusyGasPumps = service.numBusyGasPumps();

		if (!service.emptyBusyGasPumpQ()) {
			for (int i = 0; i < sizeBusyGasPumps; i++) {
				GasPump gasPump = service.removeBusyGasPumpQ();
				gasPump.setEndSimulationTime(simulationTime, GasPump.BUSY);
				temp.add(gasPump);
			}
		}
		
		// move gas pumps from temp to busyGasPumpQ
		int tempSize =  temp.size();
		for (int i = 0; i < tempSize; i++) {
			service.insertBusyGasPumpQ(temp.remove(0));	
		}

		if (!service.emptyFreeGasPumpQ()) {
			for (int i = 0; i < service.numFreeGasPumps(); i++) {
				GasPump gasPump = service.removeFreeGasPumpQ();
				gasPump.setEndSimulationTime(simulationTime, GasPump.FREE);
				temp.add(gasPump);
			}
		}
		
		// move gas pumps from temp to freeGasPumpQ
		tempSize =  temp.size();
		for (int i = 0; i < tempSize; i++) {
			service.insertFreeGasPumpQ(temp.remove(0));
		}
	}

	private void printStatistics() {	
		System.out.println("=========================");
		System.out.println("End of simulation report\n");
		
		System.out.println("\t# total arrival cars	: " + (numServed + numGoAway));
		System.out.println("\t# cars gone away	: " + numGoAway);
		System.out.println("\t# cars served		: " + numServed);	
		System.out.println();
		System.out.println("\t*** Current GasPumps Info. ***");
		service.printStatistics();
		System.out.println();
		System.out.println("\tTotal waiting time    : " + totalWaitingTime);
		System.out.println("\tAverage waiting time  : " 
				+ String.format("%.2f", (totalWaitingTime*1.0 / numServed)));	
		System.out.println();
		
		if (!service.emptyBusyGasPumpQ()) {
			System.out.println("\t*** Busy Gas Pumps Info. ***\n");
			int size = service.numBusyGasPumps();
			for (int i = 0; i < size; i++) {
				GasPump gasPump = service.removeBusyGasPumpQ();
				gasPump.printStatistics();
			}
		}
		System.out.println();
		
		if (!service.emptyFreeGasPumpQ()) {
			System.out.println("\t*** Free Gas Pumps Info. ***\n");
			int size = service.numFreeGasPumps();
			for (int i = 0; i < size; i++) {
				GasPump gasPump = service.removeFreeGasPumpQ();
				gasPump.printStatistics();
			}
		}
		
	}

	// *** main method to run simulation ****

	public static void main(String[] args) {
		GasStationSimulator gas_station_simulator = new GasStationSimulator();
		gas_station_simulator.setupParameters();
		gas_station_simulator.doSimulation();
		gas_station_simulator.printStatistics();
	}

}
