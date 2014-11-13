package lockFreeStack;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicStampedReference;


public class LockFreeExchanger<T> {
	private final int EMPTY = 0 , WAITING = 1 , BUSY = 2;
	
	AtomicStampedReference<T> slot = new AtomicStampedReference<T>(null,EMPTY);
	
	public T exchange(T node, long timeoutFactor, TimeUnit unit)
		throws TimeoutException{
			long nanos = unit.toNanos(timeoutFactor);
			long timeBound = System.nanoTime() + nanos;
			int[] stampHolder = {EMPTY};
			
			while(true){
				if(System.nanoTime() > timeBound)
					throw new TimeoutException();
				
				T yrItem = slot.get(stampHolder);
				int stamp = stampHolder[0];
				
				switch(stamp){
				
				case EMPTY:
					if (slot.compareAndSet(yrItem, node, EMPTY, WAITING)){
						while (System.nanoTime() < timeBound){
							yrItem = slot.get(stampHolder);
							if(stampHolder[0] == BUSY){
								slot.set(null, EMPTY);
								return yrItem;
							}
						}
					
					
						if (slot.compareAndSet(node,  null,  WAITING,  EMPTY)){
							throw new TimeoutException();
						} else {
							yrItem = slot.get(stampHolder);
							slot.set(null, EMPTY);
							return yrItem;
						}
					}
					break;
				case WAITING:
					if (slot.compareAndSet(yrItem, node, WAITING, BUSY))
						return yrItem;
					break;
				
				case BUSY:
					break;
				default:
					break; // impossibru
				}
		}
	}
}
