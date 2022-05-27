package it.unive.scsr;

public class Pair<T, K> {
	private T first;
	private K second;
	
	public Pair(T first, K second) {
		this.first = first;
		this.second = second;
	}
	
	public T getFisrt() {
		return this.first;
	}
	
	public K getSecond() {
		return this.second;	
	}
	
	public String toString() {
		return first + " " + second;
	}
	

}
