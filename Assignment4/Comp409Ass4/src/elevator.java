/*
 * George Macrae
 * 2014
 * COMP 409
 */

import java.util.HashSet;
import java.util.Random;


public class elevator extends Thread{
	private final int SLEEP_LOWER_BOUND = 50;
	private final int SLEEP_UPPER_BOUND = 250;
	
	private volatile int donePassengerCount , count; 
	private int curretFloor, floorAmt, passengerAmt;
	private volatile boolean isOpen;
	
	private volatile HashSet<Integer> requests;
	
	private passenger[] passengers;
	private callButton[] callButtons;
	private destButton[] destButtons;
	private door[] doors;
	
	private Random r = new Random();
	
	public elevator(int floorAmt, int passengerAmt, int travelLimit){
		count = 0;
		this.curretFloor = r.nextInt(3)+1;
		requests = new HashSet<Integer>();
		isOpen = false;
		donePassengerCount = 0;
		this.floorAmt = floorAmt;
		this.passengerAmt = passengerAmt;
		
		initializeProccesses(travelLimit);
	}
	
	private void initializeProccesses(int travelLimit) {
		passengers = new passenger[passengerAmt];
		callButtons = new callButton[floorAmt];
		destButtons = new destButton[floorAmt];
		doors = new door[floorAmt];
		
		for(int i = 0 ; i < floorAmt ; i++){
			callButton cb = new callButton(i+1, this);
			destButton db = new destButton(i+1, this);
			door d = new door(i+1, this);
			
			callButtons[i] = cb;
			destButtons[i] = db;
			doors[i] = d;
		}
		
		for(int i = 0 ; i < passengerAmt ; i++){
			passengers[i] = new passenger(callButtons, destButtons , this ,travelLimit );
		}
		
	}

	public synchronized void request(int request){
		requests.add(request);
		System.out.println("Elevator : Request to floor "+ request+" added");
		sleep();
	}
	private synchronized void removeRequest(int request){;
		requests.remove(request);
		System.out.println("Elevator : Request to floor "+ request+" serviced");
		sleep();
	}
	private synchronized int getFloorRequest() {
		int size = requests.size();
		int item = r.nextInt(size);
		int index = 0;
		for(Integer i: requests){
			if (index == item){
				return i; 
			}
			index++;
		}
		return -1;
	}
	public synchronized void ready(){
		count ++;
	}	
	@Override 
	public void run(){
		startProcesses();
		
		while (donePassengerCount < passengers.length){
			
			//wait for request to be made
			while(requests.isEmpty() && donePassengerCount < passengers.length);
			
			if (donePassengerCount >= passengers.length)
				break;
					
			
			curretFloor = getFloorRequest();
			moveTo(curretFloor);
			System.out.println("Elevator : moved to floor "+curretFloor);
			sleep();
			
			//tell door at currentFloor to open
			doors[curretFloor - 1].open();
			
			// wait for doors to respond
			while (!isOpen);
			
			// tell passengers door has opened at floor
			for(passenger p : passengers){
				p.opened(curretFloor);
			}
			
			// wait for all passengers to be ready
			while(count != (passengers.length - donePassengerCount));
			count = 0;
			
			System.out.println("Elevator : all passengers ready");
			//remove serviced request
			removeRequest(curretFloor);

			// close door
			doors[curretFloor - 1].close();
			
			// tell passengers door has closed
			for (passenger p : passengers){
				p.closed();
			}
			
			// wait until doors are closed to move onto next request
			while(isOpen);
		
		}
		System.out.println("Elevator : DONE");
		for (int i = 0 ; i < floorAmt ; i++){
			doors[i].finish();
			callButtons[i].finish();
			destButtons[i].finish();
		}
	}
	
	

	private void startProcesses() {
		for(int i = 0 ; i < floorAmt ; i++){
			callButtons[i].start();
			destButtons[i].start();
			doors[i].start();
		}
		for(passenger p : passengers)
			p.start();
	}
	
	private void moveTo(int floor){
		this.curretFloor = floor;
	}
	
	public void setOpened() {
		System.out.println("Elevator : DOORS OPEN");
		isOpen = true;
		sleep();
	}
	
	public void setClosed(){
		System.out.println("Elevator : DOORS CLOSED");
		isOpen = false;
		sleep();
	}

	public void finish() {
		donePassengerCount++;
	}
	

	private void sleep() {
		try {
			Thread.sleep(r.nextInt(SLEEP_UPPER_BOUND) + SLEEP_LOWER_BOUND);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}		
	}
	
}
