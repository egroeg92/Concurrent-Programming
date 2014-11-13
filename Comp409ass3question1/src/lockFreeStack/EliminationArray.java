package lockFreeStack;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


public class EliminationArray<T> {
	private int timeoutFactor;
	LockFreeExchanger<T>[] exchanger;
	Random random;
	
	@SuppressWarnings("unchecked")
	public EliminationArray(int capacity, int exchangerTimeoutFactor){
		this.timeoutFactor = exchangerTimeoutFactor;
		exchanger = (LockFreeExchanger<T>[]) new LockFreeExchanger[capacity];
		for(int i = 0; i < capacity; i++){
			exchanger[i] = new LockFreeExchanger<T>();
		}
		random = new Random();
	}
	public T visit(Node<T> node, int range) throws TimeoutException{
		int slot = random.nextInt(range);
//		System.out.println(node.getValue());
		return (exchanger[slot].exchange(node.getValue(), timeoutFactor, TimeUnit.MILLISECONDS));
	}
}
