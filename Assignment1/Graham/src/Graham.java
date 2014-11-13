import java.awt.Point;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Stack;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


/**
 * @author George Macrae
 *
 
 */
public class Graham {

	private static ArrayList<Integer> min_array = new ArrayList<Integer>();
	private static int remainder;
	private static int coord_index = 0;
	private static int[][] coords;
	private static int prob_size;
	private static int question;
	private static int thread_amt;
	private static int min_y;
	private static int min_x;
	private static int min_index = 0;
	
	

	private static double[][] angular;
	

	private static ArrayList<double[][]> sorted_lists = new ArrayList<double[][]>();
	
	
	
	
	public static void main(String[] args)
	{
		question = Integer.parseInt(args[0]);
		prob_size = Integer.parseInt(args[1]);
		thread_amt = Integer.parseInt(args[2]);
		
		System.out.println("Question : "+ question);
		System.out.println("Problem Size : "+ prob_size);
		System.out.println("Thread Amount : "+ thread_amt);
		
		if( question > 4 || question < 1 || prob_size < 1 || thread_amt > prob_size || thread_amt < 1)
		{
			System.out.println("Invalid Parameters");
			return;
		}
		
		coords = new int[prob_size][2];

		if (question == 1)
		{
			long start = System.currentTimeMillis();
			generateCoords();
			System.out.println("Time = "+ (System.currentTimeMillis() - start) );

		}
		else if( question == 2)
		{
			long start = System.currentTimeMillis();
			generateCoords();
			System.out.println("Time = "+ (System.currentTimeMillis()-start));
			start = System.currentTimeMillis();
			findMinCoord();
			System.out.println("Time = "+ (System.currentTimeMillis()-start));
		}
		else if(question == 3)
		{
			long start = System.currentTimeMillis();
			generateCoords();
			System.out.println("Time 1 = "+ (System.currentTimeMillis()-start));
			
			start = System.currentTimeMillis();
			findMinCoord();
			System.out.println("Time 2 = "+ (System.currentTimeMillis()-start));

			start = System.currentTimeMillis();
			angularSort();
			System.out.println("Time = "+ (System.currentTimeMillis()-start));
			

		}
		else{
			long start = System.currentTimeMillis();
			long tot = start;
			generateCoords();
			System.out.println("Time 1 = "+ (System.currentTimeMillis()-start));
			
			start = System.currentTimeMillis();
			findMinCoord();
			System.out.println("Time 2 = "+ (System.currentTimeMillis()-start));

			start = System.currentTimeMillis();
			angularSort();
			System.out.println("Time = "+ (System.currentTimeMillis()-start));
			
			start = System.currentTimeMillis();
			grahamScan();
			System.out.println("Time = "+ (System.currentTimeMillis()-start));
			System.out.println("Tot = "+ (System.currentTimeMillis()-tot));
			
			
			
		}
		
	}

	
	/**
	 * finds the convex hull of the sorted points in coords
	 * puts the set of convex hull points in sol arraylist
	 */
	private static void grahamScan() {
		// Graham SLAM
		coord_index = 0;

		final Lock lock = new ReentrantLock();
		
		Thread[] thread_list = new Thread[thread_amt];
		
//		for (int i = 0 ; i < prob_size ; i++)
//		{
//				System.out.print(" ("+coords[i][0] +", " + coords[i][1]+")");
//		}

		ArrayList<Integer> sol = new ArrayList<Integer>();
		
		sol.add(0);
		sol.add(1);
		double x1,x2,x3,y1,y2,y3;

		
		for(int i = 2 ; i < coords.length; i++){
//			System.out.println(i);
			int s = sol.size();
			if(sol.size() <= 2){
				sol.add(i);
				continue;
			}
			x1 = coords[sol.get(s-2)][0] / 10000000;
			y1 = coords[sol.get(s-2)][1] / 10000000;
			x2 = coords[sol.get(s-1)][0] / 10000000;
			y2 = coords[sol.get(s-1)][1] / 10000000;
			x3 = coords[i][0]			 / 10000000;
			y3 = coords[i][1]            / 10000000;
			
			//is left
			if ( ((x2 -x1)*(y3-y1)) - (( y2 - y1 )*( x3 - x1 )) > 0){
				sol.add(i);				
			}
			else{
				sol.remove(s-1);
				i--;
			}
			
		}
//		System.out.println("RESULT");
//		
//		for(int i : sol){
//			System.out.print("("+coords[i][0]+", "+coords[i][1]+")");
////			System.out.println(i+ " "+coords[i][0] +", " + coords[i][1]);
//			
//		}
	}
	

	/**
	 *  creates ang array with values corresponding to coords angular values from min point
	 *  sorts ang array using quicksort
	 *  merges all ang arrays
	 *  copies merged ang arrays into coords
	 */
	private static void angularSort() {
		// Find Angular values with respect to minCoord
	
		Thread[] thread_list = new Thread[thread_amt];
		
		coord_index = 0;
		
		Random r = new Random();
//		for(int i = 0 ; i < prob_size ; i++)
//		{
//			coords[i][0] = r.nextInt(prob_size);
//			coords[i][1] = r.nextInt(prob_size);
////			System.out.println( i+" = [ "+coords[i][0]+ " ][ "+coords[i][1]+" ]");
//		}
		
		Thread t;
	
		angular = new double[prob_size][2];
		for(int i = 0 ; i < prob_size ; i++){
			angular[i][1] = -1;
		}
		final Lock partition_lock = new ReentrantLock();
		final Lock lock = new ReentrantLock();
		
		final int part_size = prob_size / thread_amt;
		remainder = prob_size % thread_amt;
		
		
		Runnable run = new Runnable(){
			public void run()
			{
				partition_lock.lock();
				int index_start;
				int index_end;
				try{
					index_start = coord_index;
					index_end = coord_index + part_size - 1;
					if(remainder != 0)
					{
						index_end ++;
						remainder --;
					}
					coord_index = index_end+1;
				}finally{
					partition_lock.unlock();
				}
				int index = 0;
				
				// 0 is the ang_dis (score)
				// 1 is the index 
				double[][] ang = new double[index_end - index_start +1][2];
				
				//calculate angular values
				for(int i = index_start ; i <= index_end ; i++)
				{
					if( i == min_index)
					{
						ang[index][0] = 0;
						ang[index][1] = i;
						index++;
						continue;
					}
					

					double y_dif = (coords[i][1] / 100) - (min_y / 100);
					double x_dif = (coords[i][0] / 100) - (min_x / 100);
					double ratio = y_dif/x_dif;
					double ang_dis = Math.atan(ratio);
					
					ang_dis = Math.toDegrees(ang_dis);
					
					if(ang_dis < 0){
						ang_dis = 180+ ang_dis;
					}
				
					ang[index][0]= ang_dis;
					ang[index][1] = i;
					index++;
				}
				//sort
				quickSort(ang);
				
				//add ang to sorted_lists
				lock.lock();
				try{
					sorted_lists.add(ang);
				}
				finally{
					lock.unlock();
				}
			}
		};
		
		for(int i = 0 ; i < thread_amt ; i++)
		{
			t = new Thread(run);
			t.start();
			thread_list[i] = t;
		}
		for(int i = 0 ; i < thread_amt ; i++)
		{
			try {
				thread_list[i].join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		
		//Merge all ang values using pointers
		int[] pointers = new int[sorted_lists.size()];
		for(int i = 0; i < sorted_lists.size(); i++){
			pointers[i] = 0;
		}

		int[][] coords_t =  new int[prob_size][2];
		
		double score = sorted_lists.get(0)[0][0];
		for(int i = 0 ; i < prob_size ; i++){
			
			score =Integer.MAX_VALUE;
			int sl_in = 0;
			for(int j = 0; j < sorted_lists.size() ; j++){
			
				if(pointers[j] < sorted_lists.get(j).length){
					double score_t = sorted_lists.get(j)[pointers[j]][0];
					if(score_t < score){
						sl_in = j;
						score = score_t;
					}
				}
				
			}
			int x = (int)sorted_lists.get(sl_in)[pointers[sl_in]][1];
			coords_t[i][0] = coords[x][0];
			coords_t[i][1] = coords[x][1];
			pointers[sl_in] = pointers[sl_in]+1; 
		}
		
		//re-write coords to sorted
		coords = coords_t.clone();
		
//		for(int i = 0 ; i < prob_size ; i++)
//		{
//			System.out.println( "  ( "+coords[i][0]+ " , "+coords[i][1]+" )");
//		}
		min_index = 0;
	}



	private static void quickSort(double[][]arg)
	{
		Stack<Integer> stack = new Stack<Integer>();
		stack.push(0);
		stack.push(arg.length);
		while(!stack.isEmpty()){
			int r = stack.pop();
			int l = stack.pop();
			if( r - l < 2)
				continue;
			int p = l+((r-l)/2);
			p = partition(arg,p,l,r);
			
			stack.push(p+1);
			stack.push(r);
			stack.push(l);
			stack.push(p);
		}
	}
	private static int partition(double[][] arg, int pivot, int left, int right)
	{
		int l = left;
		int r = right -2;
		double p = arg[pivot][0];
		swap(arg,pivot,right-1);
		
		while( l < r){
			if(arg[l][0] < p ){
				l++;
			}else if (arg[r][0] >= p ){
				r--;
			}else{
				swap(arg,l,r);
			}
		}
		int idx = r;
		if(arg[r][0] < p)
			idx++;
		swap(arg,right-1,idx);
		return idx;
	}

	private static void swap(double[][] arg, int i, int idx) {
		double t = arg[i][0];
		double t_ = arg[i][1];
		arg[i][0] = arg[idx][0];
		arg[i][1] = arg[idx][1];

		arg[idx][0] = t;
		arg[idx][1] = t_;
		
	}

	/**
	 *  creates n/p partitions and assigns a thread to each partition to find the minimum value within its partition concurrently
	 *  after all thread complete, their minimum values are compared to find absolute minimum
	 */
	private static void findMinCoord() {
		// find minimum y (x breaks tie) in coords

		Thread[] thread_list = new Thread[thread_amt];
		coord_index = 0;
		
		Random r = new Random();
//		for(int i = 0 ; i < prob_size ; i++)
//		{
//			coords[i][0] = r.nextInt(prob_size);
//			coords[i][1] = r.nextInt(prob_size);
////			System.out.println( i+" = [ "+coords[i][0]+ " ][ "+coords[i][1]+" ]");
//		}
//		coords[0][0] = -10;
//		coords[0][1] = -10;
//				
		
		Thread t;
		coord_index = 0;
		min_y = coords[0][1];
		min_x = coords[0][0];
		
		final Lock lock = new ReentrantLock();
		final Lock min_lock = new ReentrantLock();
		final int part_size = prob_size / thread_amt;
		remainder = prob_size % thread_amt;
		min_array.clear();
		Runnable run = new Runnable(){
			public void run()
			{
				lock.lock();
				int index_start;
				int index_end;
				int min_y;
				int min_index;
				try{
					index_start = coord_index;
					index_end = coord_index + part_size - 1;
					if(remainder != 0)
					{
						index_end ++;
						remainder --;
					}
					coord_index = index_end+1;
				}finally{
					lock.unlock();
				}
				min_y = coords[index_start][1];
				min_index = index_start;
				for(int i = index_start ; i <= index_end ; i++)
				{
					if(coords[i][1] < min_y)
					{
						min_y = coords[i][1];
						min_index = i;
					}
					else if(coords[i][1] == min_y)
					{
						if( coords[i][0] < coords[min_index][0]){
							min_index = i;
						}
					}
				}
					
//					System.out.println(Thread.currentThread().getId() +"got min at "+min_index+" = "+ coords[min_index][1]);
				min_lock.lock();
				try{
					min_array.add(min_index);
				}
				finally{
					min_lock.unlock();
				}		
			}
		};
		
		
		for(int i = 0; i < thread_amt ; i ++)
		{
			t = new Thread(run);
			t.start();
			thread_list[i] = t;
		}
		for(int i = 0 ; i < thread_amt ; i++)
		{
//			System.out.println(thread_list[i].getId());
			try {
				thread_list[i].join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		for(int i : min_array)
		{
			if(coords[i][1] < min_y)
			{
				min_y = coords[i][1];
				min_x = coords[i][0];
				min_index = i;
			}
			else if (coords[i][1] == min_y)
			{
				if(coords[i][0] < min_x)
				{
					min_y = coords[i][1];
					min_x = coords[i][0];
					min_index = i;
				}
			}
		}
//		System.out.println( "\nMinimum Index = "+min_index+" = "+coords[min_index][1]);
	}

	
	/**
	 * Generates n / p partitions and assigns each of the p threads a partition to fill concurrently
	 * 
	 */
	private static void generateCoords() {
		// generate a n-length 2D array of random integer-coordinates using Thread_Amt threads concurrently
		
		Thread[] thread_list = new Thread[thread_amt];
		
		coord_index = 0;
		for(int i = 0 ; i < prob_size ; i++)
		{
			coords[i][0] = -1;
			coords[i][1] = -1;	
		}
		 
		Thread t;
		final Random rand = new Random();	
		final Lock lock = new ReentrantLock();
		final int part_size = prob_size / thread_amt;
		remainder = prob_size % thread_amt;
		
		Runnable run = new Runnable(){
			public void run()
			{
				lock.lock();
				int index_start;
				int index_end;
				try{
					index_start = coord_index;
					index_end = coord_index + part_size - 1;
					if(remainder != 0)
					{
						index_end ++;
						remainder --;
					}
					coord_index = index_end+1;
				}finally{
					lock.unlock();
				}
//				
				for(int i = index_start ; i <= index_end ; i++)
				{
					
					int x = rand.nextInt();
					int y = rand.nextInt();
//					if(rand.nextInt()%2 ==0) x*=-1;
//
//					if(rand.nextInt()%2 ==0) y*=-1;
					
					
					coords[i][0] = x;
					coords[i][1] = y;
					
					for(int j = 0 ; j < prob_size ; j++ )
					{
						if(j == i) continue;
						
						else if(coords[j][0] == x && coords[j][1]== y)
						{	
							i--;
							break;
						}
					}
				}
			}
		};
		
		for(int i = 0; i < thread_amt ; i++)
		{
			t = new Thread(run);
			t.start();
			thread_list[i] = t;
		}
		
		for(int i = 0 ; i < thread_amt ; i++)
		{
			try {
				thread_list[i].join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		
//		for(int i = 0 ; i<prob_size ; i++)
//		{
//			System.out.println( i+" [ "+coords[i][0]+" ][ "+coords[i][1]+"]");
//		}
//		
	}
	
	
}
