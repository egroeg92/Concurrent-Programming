package lockFreeStack;
import java.util.EmptyStackException;
import java.util.concurrent.TimeoutException;


public class EliminationStack <T> extends LockFreeStack<T> {
	
	private static int exchangerCapacity, exchangerWaitDuration;
	EliminationArray<T> eliminationArray;
	
	private static ThreadLocal<RangePolicy> policy;
	
	@SuppressWarnings("static-access")
	public EliminationStack( final int exchangerCapacity, int exchangerTimeOutFactor){
		super(0, 0);
		
		this.exchangerCapacity = exchangerCapacity;
		this.exchangerWaitDuration = exchangerTimeOutFactor;
		this.eliminationArray = new EliminationArray<T>(exchangerCapacity, exchangerTimeOutFactor);
	
		policy = new ThreadLocal<RangePolicy>(){
			protected synchronized RangePolicy initialValue(){
				return new RangePolicy(exchangerCapacity);
			}
		};
	}
	
	
	public void push(T value){
		Node<T> n = new Node<T>(value);
		RangePolicy rangePolicy = policy.get();
		while(true){
			if (tryPush(n)){
				return;
			} else try {
				T otherValue = eliminationArray.visit(n, rangePolicy.getRange());
				
				if(otherValue == null){ // exchanged with a pop
					rangePolicy.recordEliminationSuccess();
					return;
				}
				
			} catch (TimeoutException ex){
				rangePolicy.recordEliminationTimeout();
			}
		}
	}
	
	public T pop() throws EmptyStackException{
		RangePolicy rangePolicy = policy.get();
		while(true){
			Node<T> returnNode = tryPop();
			if (returnNode != null){
				return returnNode.getValue();
			} else try {
				T otherValue = eliminationArray.visit(null,  rangePolicy.getRange());
				if (otherValue != null){ // exchanged with a push
					// increase range so that thread is less likely to wait on BUSY exchanger
					rangePolicy.recordEliminationSuccess();
					return otherValue;
				}
			} catch(TimeoutException ex){ // shrink range so collision is more likely
				rangePolicy.recordEliminationTimeout();
			}
		}
	}
	
	
}


