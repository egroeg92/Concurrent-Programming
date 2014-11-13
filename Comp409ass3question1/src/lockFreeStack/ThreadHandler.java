package lockFreeStack;

public class ThreadHandler 
{
	private static int threadAmt;
	private static int delayUpperBound;
	private static int operationAmt;
	private static int timeoutFactor;
	private static int eliminationArraySize;
	
	private static EliminationStack<Integer> elimStack;
	private static StackThread[] threads;
	
	public static void main(String[] args){
		if (args.length < 5 ){
			System.out.println("not enough args");
			return;
		}
		threadAmt = Integer.parseInt(args[0]);
		delayUpperBound = Integer.parseInt(args[1]);
		operationAmt = Integer.parseInt(args[2]);
		timeoutFactor = Integer.parseInt(args[3]);
		eliminationArraySize = Integer.parseInt(args[4]);
		
		if( threadAmt < 1 ||
				delayUpperBound < 0 ||
				timeoutFactor < 0 ||
				eliminationArraySize < 1 ){
			System.out.println("Invalid parameters");
			return;
		}
		
		initialize();
		System.out.println(run(System.currentTimeMillis()));
	}
	
	private static void initialize(){
		elimStack = new EliminationStack<Integer>(eliminationArraySize, timeoutFactor);
		threads = new StackThread[threadAmt];
		for(int i = 0 ; i < threadAmt ; i++){
			threads[i] = new StackThread(operationAmt, elimStack, delayUpperBound);
		}
	}
	private static String run(long currentTimeMillis) {
		long startTime = currentTimeMillis;
		int totalPops = 0;
		int totalPushes = 0;
		
		for(StackThread t : threads)
			t.start();
		
		for(StackThread t : threads){
			try {
				t.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		for(StackThread t : threads){
			totalPops += t.getPopCount();
			totalPushes += t.getPushCount();
		}
		
		long endTime = System.currentTimeMillis() - startTime;
		return ("Final Time : " +endTime + 
				"\n"+ totalPushes + 
				" "+ totalPops +
				" "+ elimStack.getSize());
	}
	
}
