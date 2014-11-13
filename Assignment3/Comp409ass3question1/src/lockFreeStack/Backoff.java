package lockFreeStack;
import java.util.Random;


public class Backoff {
	private final int minDelay, maxDelay;
	private int limit;
	private final Random random;
	
	public Backoff(int min, int max){
		minDelay = min;
		maxDelay = max;
		limit = minDelay;
		random = new Random();
	}
	public void backoff() throws InterruptedException{
		
		if(limit == 0){
			Thread.sleep(0);
		} else {
			int delay = random.nextInt(limit);
			limit = Math.min(maxDelay, 2*limit);
			Thread.sleep(delay);
		}
	}
}
