package lockFreeStack;

public class RangePolicy {
	
	private static int max;
	private static int range;

	public RangePolicy(int upperBound){
		range = 1;
		max = upperBound;
	}
	
	public int getRange() {
		return range;
	}

	public void recordEliminationSuccess() {
		if ( range < max){
			range++;
		}
	}

	public void recordEliminationTimeout() {
		if(range > 1){
			range--;
		}
	}

}
