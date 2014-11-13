package lockFreeStack;
import java.util.EmptyStackException;
import java.util.concurrent.atomic.AtomicReference;


public class LockFreeStack<T> {
	AtomicReference<Node<T>> top = new AtomicReference<Node<T>>(null);
	static int min_delay = 10;
	static int max_delay = 100;
	Backoff backoff;
	
	public LockFreeStack(int min, int max){
		min_delay = min;
		max_delay = max;
		backoff = new Backoff(min_delay, max_delay);
	}
	
	/**
	 * @param node
	 * @return false if node was not added, true if added
	 */
	protected boolean tryPush(Node<T> node){
		Node<T> oldTop = top.get();
		node.setNext(oldTop);
		return(top.compareAndSet(oldTop, node));
	}

	public void push(Node<T> n) {
		while (true){
			if (tryPush(n)){
				break;
			}else{
				try {
					backoff.backoff();
				}catch (InterruptedException e){
					e.printStackTrace();
				}
			}
		}
	}
	
	protected Node<T> tryPop() throws EmptyStackException{
		Node<T> oldTop = top.get();
		if (oldTop == null){
			throw new EmptyStackException();
		}
		
		Node<T> newTop = oldTop.getNext();
		if (top.compareAndSet(oldTop, newTop)){
			return oldTop;
		}else{
			return null;
		}
	}

	public T pop() throws EmptyStackException {
		while (true){
			Node<T> returnNode = tryPop();
			if (returnNode != null){
				return returnNode.getValue();
			} else {
				try {
					backoff.backoff();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public int getSize(){
		int size = 0;
		
		Node<T> node = top.get();
		
		while(node != null){
			size++;
			node = node.getNext();
		}
		
		
		return size;
	}
	
}
