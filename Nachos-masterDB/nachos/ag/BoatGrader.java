package nachos.ag;

public class BoatGrader {

	/**
	 * BoatGrader consists of functions to be called to show that your solution
	 * is properly synchronized. This version simply prints messages to standard
	 * out, so that you can watch it. You cannot submit this file, as we will be
	 * using our own version of it during grading.
	 * 
	 * Note that this file includes all possible variants of how someone can get
	 * from one island to another. Inclusion in this class does not imply that
	 * any of the indicated actions are a good idea or even allowed.
	 */

	/*
	 * ChildRowToMolokai should be called when a child pilots the boat from Oahu
	 * to Molokai
	 */
	public void ChildRowToMolokai() {
		System.out.println("**Child rowing to Molokai.");
	}

	/*
	 * ChildRowToOahu should be called when a child pilots the boat from Molokai
	 * to Oahu
	 */
	public void ChildRowToOahu() {
		System.out.println("**Child rowing to Oahu.");
	}

	/*
	 * ChildRideToMolokai should be called when a child not piloting the boat
	 * disembarks on Molokai
	 */
	public void ChildRideToMolokai() {
		System.out.println("**Child arrived on Molokai as a passenger.");
	}

	/*
	 * ChildRideToOahu should be called when a child not piloting the boat
	 * disembarks on Oahu
	 */
	public void ChildRideToOahu() {
		System.out.println("**Child arrived on Oahu as a passenger.");
	}

	/*
	 * AdultRowToMolokai should be called when a adult pilots the boat from Oahu
	 * to Molokai
	 */
	public void AdultRowToMolokai() {
		System.out.println("**Adult rowing to Molokai.");
	}

	/*
	 * AdultRowToOahu should be called when a adult pilots the boat from Molokai
	 * to Oahu
	 */
	public void AdultRowToOahu() {
		System.out.println("**Adult rowing to Oahu.");
	}

	/*
	 * AdultRideToMolokai should be called when an adult not piloting the boat
	 * disembarks on Molokai
	 */
	public void AdultRideToMolokai() {
		System.out.println("**Adult arrived on Molokai as a passenger.");
	}

	/*
	 * AdultRideToOahu should be called when an adult not piloting the boat
	 * disembarks on Oahu
	 */
	public void AdultRideToOahu() {
		System.out.println("**Adult arrived on Oahu as a passenger.");
	}
}
