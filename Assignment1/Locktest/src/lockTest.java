import java.util.HashMap;

/**
 * @author George Macrae
 *
 */
public class lockTest {

	private static int question;
	private static int n_amt;
	private static int thread_amt;
	private static int counter;
	private static int exp_delay;
	
	private static volatile int lock;

	private static HashMap<Integer, Integer> delays = new HashMap<Integer,Integer>(); 
	private static HashMap<Integer, Integer> enters = new HashMap<Integer,Integer>();
	public static void main(String[] args)
	{
		
		question = Integer.parseInt(args[0]);
		n_amt = Integer.parseInt(args[1]);
		thread_amt = Integer.parseInt(args[2]);
		counter = 0;
		exp_delay = 1;
		lock = 0;
		
		if( question > 4 || question < 1 || n_amt < 1 || thread_amt > n_amt || thread_amt < 1)
		{
			System.out.println("Invalid Parameters");
			return;
		}
		if(question == 1){

			long start = System.currentTimeMillis();
			syncro();
			System.out.println(System.currentTimeMillis() - start);
		}
		else if(question == 2){
			long start = System.currentTimeMillis();
			simpleTTAS();

			System.out.println(System.currentTimeMillis() - start);
		}
		else if (question == 3){
			long start = System.currentTimeMillis();
			backoffTTAS();

			System.out.println(System.currentTimeMillis() - start);
		}
		else{
			System.out.println("ClH");
			long start = System.currentTimeMillis();
			CLH();

			System.out.println(System.currentTimeMillis() - start);
		}
		
		for (Integer k : enters.keySet()) {
			System.out.println(k + " max delay = " + delays.get(k));
		}
		System.out.println(counter);
	}

	private static void CLH() {
		Thread t;
		Thread[] thread_list = new Thread[thread_amt];

		
		for(int i = 0 ; i < thread_amt ; i++)
		{
			t = new Thread(){

				
				node tail = new node();
				node me = new node();
				node pred = null;
				
				private node CLH_ts(node tail, node me){
					node t = tail;
					tail = me;
					return t;
				}
				private void enter(node me, node pred){
					Thread.yield();
					me.setLock(true);
					pred = CLH_ts(tail, me);
					while(pred.isLocked());
				}
				private void exit(node me, node pred){
					me.setLock(false);
					me = pred;
					Thread.yield();
				}
				public void run(){
					delays.put((int)getId(), 0);
					enters.put((int)getId(), 0);
				
					while(counter < n_amt){
						enter(me,pred);
						criticalSection((int)getId());
						exit(me,pred);
					}
					
				}
			};
			t.start();
			thread_list[i] = t;
		}		
		for(int i = 0 ; i < thread_amt ; i++){
			try {
				thread_list[i].join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private static void backoffTTAS() {
		Thread t;
		Thread[] thread_list = new Thread[thread_amt];
		
		for(int i = 0 ; i < thread_amt ; i++)
		{
			t = new Thread(){
				
				public void run(){
					delays.put((int)getId(), 0);
					enters.put((int)getId(), 0);
					
					while(counter < n_amt-thread_amt+1){
						while(lock == 1);
				
						if(ts(lock,1) == 0)
						{
							Thread.yield();
							criticalSection((int)getId());
							lock = 0;
							
						}else{
							try {
								sleep(exp_delay);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							exp_delay *= 2;
						}
					}
				}
			};
			t.start();
			thread_list[i] = t;
		}
		for(int i = 0 ; i < thread_amt ; i++){
			try {
				thread_list[i].join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	}

	private static void simpleTTAS() {
		Thread t;
		Thread[] thread_list = new Thread[thread_amt];
		
		lock = 0;
		for(int i = 0 ; i < thread_amt ; i++)
		{
			t = new Thread(){
				public void run(){
					delays.put((int)getId(), 0);
					enters.put((int)getId(), 0);
				
					while( counter < n_amt ){
						while(lock == 1);
						
						if(ts(lock,1) == 0)
						{
							Thread.yield();
							if(counter >= n_amt)
								break;
						
							criticalSection((int)getId());
							lock = 0;
						}
						
					}
				}
			};
			t.start();
			thread_list[i] = t;
		}
		
		for(int i = 0 ; i < thread_amt ; i++){
			try {
				thread_list[i].join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	private synchronized static int ts(int x, int y){
		int tmp = x;
		x = y;
		return tmp;
	}

	private static void syncro() {
		Thread[] thread_list = new Thread[thread_amt];
		Thread t;
		for(int i = 0 ; i < thread_amt ; i++){
			t = new Thread(){
				
				public void run(){
					delays.put((int)getId(), 0);
					enters.put((int)getId(), 0);
				
					while(counter < n_amt - thread_amt + 1)
					{
						synchro_lock((int)getId());
						Thread.yield();
					}
				}
			};
			thread_list[i] = t;
			t.start();
		}
		for(int i = 0 ; i < thread_amt ; i++){
			try {
				thread_list[i].join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	private synchronized static void synchro_lock(int id){
		Thread.yield();
		criticalSection(id);
	}
	
	
	// CS, keeps track of how many times a thread enters the CS and what its maximum delay is
	private static void criticalSection(int id){
		Integer old = enters.get(id);
		
		if (old == null){
			old = 0;
		}
		counter++;
		int delay = counter - old - 1;
		if(counter <= n_amt){
			try{
				if(delay > delays.get(id)){
					delays.put(id, delay);
				}
			}
			catch(NullPointerException e){}
			enters.put(id, counter);
		}
	}
}
class node{
	private boolean locked = false;
	public void setLock(boolean b){
		locked = b;
	}
	public boolean isLocked(){
		return locked;
	}
}
