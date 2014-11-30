/*
 * George Macrae
 * 2014
 * COMP 409
 * 
 */

import java.util.Random;


public class door extends Thread{
	private final int SLEEP_LOWER_BOUND = 50;
	private final int SLEEP_UPPER_BOUND = 250;
	
	private int floor;
	private volatile boolean isOpened, running;
	private elevator elev;
	private Random r = new Random();
	
	public door(int floor, elevator elev){
		this.floor = floor;
		isOpened = false;
		this.elev = elev;
		running = true;
	}
	
	@Override
	public void run(){
		while(running){
		//if opened, wait until told to close
		//tell elev it has closed, once closed
			if(isOpened){
				while(isOpened && running);
				if(!running)
					break;
				elev.setClosed();
			}
		// if closed, wait until told to open
		// tell elev it has opened once opened
			else{
				while(!isOpened && running);
				if(!running)
					break;
				elev.setOpened();
			}
		}
		System.out.println("door done");
	}
	public void open() {
		System.out.println("Door "+ floor+ " : opened");
		isOpened = true;
		sleep();
	}
	public void close()
	{		
		System.out.println("Door "+ floor + " : closed");
		isOpened = false;
		sleep();
	}
	public void finish(){
		
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
