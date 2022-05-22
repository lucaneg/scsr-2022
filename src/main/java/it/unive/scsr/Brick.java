package it.unive.scsr;

import java.util.HashSet;
import java.util.Set;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.analysis.representation.DomainRepresentation;
import java.util.stream.Collectors;

public class Brick extends BaseNonRelationalValueDomain<Brick>{

	private Index min;
	private Index max; 

	private Set<String> strings; 

	public Brick() {
		this.min = new Index(0); 
		this.max = new Index(0); 
		this.strings = new HashSet<String>();
	}


	public Brick(Set<String> strings, Index min, Index max) {
		this.min = min; 
		this.max = max; 
		this.strings = strings; 
	}


	@Override
	public DomainRepresentation representation() {
		return null;
	}

	@Override
	public Brick top() {
		return new Brick(strings, new Index(0), Index.INFINITY);
	}

	
	@Override
	public Brick bottom() {
		return new Brick(new HashSet<String>(), new Index(0), new Index(0));
	}
	
	
	@Override 
	public boolean isTop() {
		return strings==null && min.equals(0) && max==Index.INFINITY; 
	}
	
	@Override
	public boolean isBottom() {
		
		boolean bottom = false; 
		
		if(this.strings!=null && !strings.isEmpty() && (min.getValue() == 0 && max.getValue()==0)) {
			bottom = true;
		}else if(this.strings!=null && strings.isEmpty() && !(min.getValue() == 0 && max.getValue()==0)) {
			bottom = true;
		}else if (max.less(min)){
			bottom = true; 
		}
		return bottom; 
	}
	

	@Override
	protected Brick lubAux(Brick other) throws SemanticException {
		
		Set<String> tempStringList = null; 
		
		if(this.strings!=null && other.strings!=null) {
			tempStringList = new HashSet<>(this.strings); 
			tempStringList.addAll(tempStringList);
		}

		Index min = Index.min(this.min, other.min); 
		Index max = Index.max(this.max,other.max); 

		return new Brick(tempStringList, min, max);
	}

	@Override
	protected Brick glbAux(Brick other) throws SemanticException {
		Set<String> tempStringList = null; 
	
		if(this.strings!=null && other.strings!=null) {
			
			tempStringList = this.strings.stream()
					.filter(other.strings::contains)
					.collect(Collectors.toSet()); 
			
		}else if(this.strings==null) {
			
			tempStringList = other.strings;
			
		}else if(other.strings==null) {
			
			tempStringList = this.strings; 
		}
		
		Index min = Index.max(this.min, other.min); 
		Index max = Index.min(this.max,other.max); 
		

		return new Brick(tempStringList,min,max);
	}
	
	@Override
	protected Brick wideningAux(Brick other) throws SemanticException {

		return null;
	}

	@Override
	protected boolean lessOrEqualAux(Brick other) throws SemanticException {
		
		if(other.isTop()) {
			return true;
		}
		
		if(other.isBottom()) {
			return false; 
		}
		
		if(other.strings!=null && this.strings!=null) {
			if(!other.strings.containsAll(this.strings)) {
				if(Index.min(this.min,other.min).equals(other.min) && Index.max(this.max, other.max).equals(other.max)){
					return true; 
				}
			}
		}
		return false; 
	}
	
	 

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((max == null) ? 0 : max.hashCode());
		result = prime * result + ((min == null) ? 0 : min.hashCode());
		result = prime * result + ((strings == null) ? 0 : strings.hashCode());
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
		Brick other = (Brick) obj;
		if (max == null) {
			if (other.max != null)
				return false;
		} else if (!max.equals(other.max))
			return false;
		if (min == null) {
			if (other.min != null)
				return false;
		} else if (!min.equals(other.min))
			return false;
		if (strings == null) {
			if (other.strings != null)
				return false;
		} else if (!strings.equals(other.strings))
			return false;
		return true;
	}

	


}
