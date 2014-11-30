/*
 * George Macrae
 * 2014
 * COMP 409
 */
import java.util.Random;


public class callButton extends Thread{
	private final int SLEEP_LOWER_BOUND = 50;
	private final int SLEEP_UPPER_BOUND = 250;
	private int floor;
	private elevator elev;
	private volatile boolean running, pushed;
	private Random r = new Random();

	public callButton( int floor , elevator elev) {
		this.floor = floor;
		this.elev = elev;
		running = true;
		pushed = false;
	}
	@Override
	public void run(){
		while(running){
			//wait until pushed, then notify elev of request
			while(!pushed && running);
			if(!running)
				break;
			elev.request(floor);
			pushed = false;
		}
	}

	public void push(){
		pushed = true;
		System.out.println("Call button "+floor+" : pushed ");
		sleep();
	}

	public void finish() {
		running = false;
	}

	private void sleep() {
		try {
			Thread.sleep(r.nextInt(SLEEP_UPPER_BOUND) + SLEEP_LOWER_BOUND);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}		
	}

}
