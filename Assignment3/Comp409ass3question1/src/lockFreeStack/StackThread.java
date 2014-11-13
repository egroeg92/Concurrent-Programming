package lockFreeStack;

import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.Random;

public class StackThread extends Thread{
	private static final int HISTORY_SIZE = 10;
	
	private int pushCount;
	private int popCount;
	private int operationAmt;
	private ArrayList<Integer> history;
	
	private Random r;
	private int delay;
	
	private static EliminationStack<Integer> stack;
	
	public StackThread(int opAmt, EliminationStack<Integer> stack, int delay){
		this.operationAmt = opAmt;
		this.stack = stack;
		this.delay = delay;
		
		pushCount = 0;
		popCount = 0;
		history = new ArrayList<Integer>();
		r = new Random();
	}
	
	@Override
	public void run(){
		for(int i = 0; i < operationAmt ; i++ ){
			executeOperation();
			try{
				Thread.sleep(r.nextInt(delay));
			} catch (InterruptedException e){
				e.printStackTrace();
			}
		}
		
	}
	
	private void executeOperation(){
		while(true){
			if(r.nextBoolean()){
				if (history.isEmpty() || r.nextBoolean()){
					stack.push(new Node<Integer>(r.nextInt()));
				} else {
					stack.push(getFromList());
				}
				pushCount++;
				break;
			} else {
				try{
					history.add(0, stack.pop());
					if(history.size() > HISTORY_SIZE){
						history.remove(HISTORY_SIZE);
					}
					popCount++;
					break;
				}catch (EmptyStackException e){
					continue;
				}catch (NullPointerException e){
					continue;
				}
			}
		}
	}
	private Integer getFromList(){
		Integer n = null;
		while(n == null){
			n = history.get(r.nextInt(history.size()));
		}
		return n;
	}
	public int getPushCount(){
		return pushCount;
	}
	public int getPopCount(){
		return popCount;
	}
	
}
