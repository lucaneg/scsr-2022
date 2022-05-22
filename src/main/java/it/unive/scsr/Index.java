package it.unive.scsr;

public class Index {

	public static final Index INFINITY = null; 

	private Integer value;


	public Index() {
		this.value=null;
	}

	public Index(Integer value) {
		if(value<0) {
			value=0;
		}
		this.value = value; 
	}

	public Integer getValue() {
		return value;
	}

	public void setValue(Integer value) {
		this.value = value;
	} 

	public Index plus(Index other) {
		Index result=Index.INFINITY;
		if(this.value!=null && other.value!=null) {
			result = new Index(other.value + this.value);
		}
		return result; 
	}

	public Index minus(Index other) {
		Index result=Index.INFINITY;
		if(this.value!=null && other.value!=null) {
			result = new Index(other.value - this.value);
		}
		return result;
	}

	public boolean less(Index other) {
		boolean result=true;
		if(this.value!=null && other.value!=null) {
			result=this.value < other.getValue();
		}
		if(this.value==null && other.value!=null) {
			result = false; 
		}
		return result;
	}

	public boolean greater(Index other) {
		boolean result=false;
		if(this.value!=null && other.value!=null) {
			result=this.value > other.getValue();
		}
		if(this.value==null && other.value!=null) {
			result = true; 
		}
		return result;
	}

	public boolean lessOrEqual(Index other) {
		boolean result=true;
		if(this.value!=null && other.value!=null) {
			result=this.value <= other.getValue();
		}
		if(this.value==null && other.value!=null) {
			result = false; 
		}
		return result;
	}

	public boolean greaterOrEqual(Index other) {
		boolean result=false;
		if(this.value!=null && other.value!=null) {
			result=this.value >= other.getValue();
		}
		if(this.value==null && other.value!=null) {
			result = true; 
		}
		return result;
	}

	public static Index min(Index fistIndex, Index secondIndex) {
		return fistIndex.less(secondIndex)?fistIndex:secondIndex; 
	}

	public static Index max(Index fistIndex, Index secondIndex) {
		return fistIndex.greater(secondIndex)?fistIndex:secondIndex; 
	}



	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Index other = (Index) obj;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}





}
