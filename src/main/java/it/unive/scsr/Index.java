package it.unive.scsr;


public class Index {

	public static final Index INFINITY = new Index();

	private final Integer value;

	public Index() {
		value = null;
	}

	public Index(int value) {
		this.value = value;
	}

	public Integer getValue() {
		return value;
	}

	public Index plus(Index other) { 
		if(this.value==null || other==null) {
			return INFINITY;
		}else {
			return new Index(this.value + other.getValue());
		} 
	}


	public Index minus(Index other) {
		if(this.value ==null) {
			return INFINITY;
		}else {
			return new Index(this.value - other.getValue());
		}
	}


	public boolean less(Integer other) { 
		if((other == null) || (this.value != null && value < other)) {
			return true; 
		}else {
			return false; 
		}
	}

	public boolean less(Index other) { 
		return this.less(other.value); 
	}


	public boolean lessOrEqual(Integer other) { 
		if((other == null) || (this.value != null && value <= other)) {
			return true; 
		}else {
			return false; 
		}
	}

	public boolean lessOrEqual(Index other) { 
		return this.lessOrEqual(other.value);
	}


	public boolean greater(Integer other) { 
		if((other != null) && (this.value == null || value > other)) {
			return true;
		}else {
			return false; 
		}
	}

	public boolean greater(Index other) { 
		return this.greater(other.value); 
	}

	public boolean greaterOrEqual(Integer other) { 	
		if((other != null) && (this.value == null || value >= other)) {
			return true;
		}else {
			return false;
		}
	}

	public boolean greaterOrEqual(Index other) { 
		return this.greaterOrEqual(other.value); 

	}

	public static Index max(Index i1, Index i2) {
		return i1.greater(i2) ? i1 : i2;
	}


	public static Index min(Index i1, Index i2) {
		return i1.less(i2) ? i1 : i2;
	}

	
	public boolean equals(Integer other) { 
		if((this.value == null && other == null)|| this.value != null && value.equals(other)){
			return true; 
		}else {
			return false; 
		}
	}

	public boolean equals(Index other) {
		return this.equals(other.value);
	}
	
	public String toString() {
		if(this.value==null) {
			return "INFINTIY";
		}else {
			return value.toString();
		}
	}
}
