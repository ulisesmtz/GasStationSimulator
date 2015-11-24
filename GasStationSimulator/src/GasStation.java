import java.util.*;

//--------------------------------------------------------------------------
//
// Define simulation queues in a gas station. Queues hold references to Car &
// GasPump objects
//
// Car (FIFO) queue is used to hold waiting cars. If the queue is too long
// (i.e. >  carQLimit), car goes away without entering car queue
//
// There are several gas pumps in a gas station. Use PriorityQueue to 
// hold BUSY gas pumps and FIFO queue to hold FREE gas pumps, 
// i.e. a pump that is FREE for the longest time should start be used first.
//
// To handle gasPump in PriorityQueue, we need to define comparator 
// for comparing 2 gasPump objects. Here is a constructor from Java API:
//
// 	PriorityQueue(int initialCapacity, Comparator<? super E> comparator) 
//
// For priority queue, the default compare function is "natural ordering"
// i.e. for numbers, minimum value is returned first
//
// User can define own comparator class for PriorityQueue.
// For gaspump objects, we like to have smallest end busy interval time first.
//
// The following class define compare() for two gas pumps :

class CompareGasPump implements Comparator<GasPump> {
	// overide compare() method
	public int compare(GasPump o1, GasPump o2) {
		return o1.getEndBusyIntervalTime() - o2.getEndBusyIntervalTime();
	}
}


class GasStation {

	private PriorityQueue<GasPump> busyGasPumpQ;

	// define two FIFO queues
	private Queue<Car> carQ;
	private Queue<GasPump> freeGasPumpQ;

	private int carQLimit;

	// Constructor
	public GasStation() {
		this(10,10,1);
	}

	// Constructor
	public GasStation(int numGasPumps, int carQlimit, int startGasPumpID) {
		busyGasPumpQ = new PriorityQueue<GasPump>(numGasPumps,
				new CompareGasPump());

		carQLimit = carQlimit;		
		freeGasPumpQ = new ArrayDeque<GasPump>(numGasPumps);
		
		for (int i = startGasPumpID; i < startGasPumpID + numGasPumps; i++) {
			freeGasPumpQ.add(new GasPump(i));
		}
		
		carQ = new ArrayDeque<Car>(carQLimit);
	}

	public GasPump removeFreeGasPumpQ() {
		return freeGasPumpQ.poll();
	}

	public GasPump removeBusyGasPumpQ() {
		return busyGasPumpQ.poll();
	}

	public Car removeCarQ() {
		return carQ.poll();
	}

	public void insertFreeGasPumpQ(GasPump gasPump) {
		freeGasPumpQ.add(gasPump);
	}

	public void insertBusyGasPumpQ(GasPump gasPump) {
		busyGasPumpQ.add(gasPump);
	}

	public void insertCarQ(Car car) {
		carQ.add(car);
	}

	public boolean emptyFreeGasPumpQ() {
		return freeGasPumpQ.isEmpty();
	}

	public boolean emptyBusyGasPumpQ() {
		return busyGasPumpQ.isEmpty();
	}

	public boolean emptyCarQ() {
		return carQ.isEmpty();
	}

	public int numFreeGasPumps() {
		return freeGasPumpQ.size();
	}

	public int numBusyGasPumps() {
		return busyGasPumpQ.size();
	}

	public int numWaitingCars() {
		return carQ.size();
	}

	public GasPump getFrontBusyGasPumpQ() {
		return busyGasPumpQ.peek();
	}

	public boolean isCarQTooLong() {
		return carQ.size() >= carQLimit;
	}

	public void printStatistics() {
		System.out.println("\t# waiting cars        : " + numWaitingCars());
		System.out.println("\t# busy gas pumps      : " + numBusyGasPumps());
		System.out.println("\t# free gas pumps      : " + numFreeGasPumps());
	}

}
