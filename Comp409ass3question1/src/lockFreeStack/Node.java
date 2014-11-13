package lockFreeStack;

public class Node<T> {
	
	private int id;
	private T value;
	private Node<T> next;
	
	
	public Node(T value ){
		this.next = null;
		this.value = value;
	}
	
	
	public void setNext(Node<T> n){
		next = n;
	}
	
	public Node<T> getNext(){
		return next;
	}
	
	public int getId(){
		return id;
	}
	public T getValue(){
		return value;
	}
}
