
public class Controller {
	private static final int FLOOR_AMT = 3;
	private static int passengerAmt;
	private static int travelAmt;
	
	
	private static elevator elev;
	
	public static void main(String[] args){
		passengerAmt = Integer.parseInt(args[0]);
		travelAmt = Integer.parseInt(args[1]);
		
		initializeProcesses();
		startProcesses();
		
	}
	
	//elevator create other processes 
	private static void startProcesses() {
		elev.start();
	}

	private static void initializeProcesses() {
		elev = new elevator(FLOOR_AMT, passengerAmt, travelAmt);
	}
}
