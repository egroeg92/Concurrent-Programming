/*
 * George Macrae
 * 2014
 * COMP 409
 */

import java.util.Random;


public class passenger extends Thread{
	private final int SLEEP_LOWER_BOUND = 50;
	private final int SLEEP_UPPER_BOUND = 250;
	
	private volatile boolean inElev;
	private volatile boolean doorOpen;
	private int destinationFloor;
	private int currentFloor;
	private volatile int elevAt;
	private callButton[] callButtons;
	private destButton[] destButtons;
	private elevator elev;
	
	private int travelLimit;
	private int travelCount;
	
	private Random r = new Random();
	public passenger(callButton[] c, destButton[] d, elevator e, int travelAmt){
		callButtons = c;
		destButtons = d;
		elev = e;
		
		currentFloor = r.nextInt(3)+1;
		travelCount = 0;
		travelLimit = travelAmt;
		
		doorOpen = false;

		inElev = false;

	}
	private void selectNewDest() {
		do{
			destinationFloor = r.nextInt(3)+1;
		}while(destinationFloor == currentFloor);
	}
	@Override
	public void run(){
		while(travelCount < travelLimit){
			System.out.println("passenger "+getId() + " inElev = "+inElev);
			//not in the elevator
			if(!inElev){
				
				// press call button
				System.out.println("Passenger "+getId()+ " : pressed called button "+ currentFloor);
				callButtons[currentFloor - 1].push();
				sleep();
				
				
				// wait until elev reaches floor
				do{
					//wait until elev calls door open
					while(!doorOpen);
					
					//if elevator is at the current floor
					if(elevAt == currentFloor)
						enter();
					
					//tell elevator 'IM READY' (has either entered the elevator or still waiting)
					elev.ready();
					sleep();
					
					//wait until elev closes door
					while(doorOpen);
				}while(!inElev);
			}
			// in Elev
			else {
				selectNewDest();
				
				// press dest button
				System.out.println("Passenger "+getId()+ " : pressed destination button "+ destinationFloor);
				destButtons[destinationFloor - 1].push();
				sleep();
				
				//loop until leaves the elev
				do{
					//wait until elev calls door open
					while(!doorOpen);
					
					if(elevAt == destinationFloor)
						exit();
					
					elev.ready();
					sleep();
					
					while(doorOpen);

				}while(inElev);
			}
		}
		
		System.out.println("Passenger "+getId()+" : DONE");
		elev.finish();
	}
	private void exit(){
		travelCount++;
		currentFloor = elevAt;
		inElev = false;

		System.out.println("Passenger "+ this.getId() + " : Exited elevator on floor"+ elevAt);
		sleep();
	}
	private void enter(){
		inElev = true;

		System.out.println("Passenger "+ this.getId() + " : Entered elevator on floor "+ elevAt);
		sleep();
	}
	
	public void opened(int floor) {
		elevAt = floor;
		doorOpen = true;
	}
	public void closed() {
		doorOpen = false;
	}
	

	private void sleep() {
		try {
			Thread.sleep(r.nextInt(SLEEP_UPPER_BOUND) + SLEEP_LOWER_BOUND);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}		
	}
	
	
}
