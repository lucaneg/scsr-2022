package it.unive.scsr;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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

	private void ruleOne() {

	}

	
	/**
	 * Merge successive bricks with the same indices, min=1 and max=1, in a new single
	 * brick where the indices remain the same (min=max=1), and the strings set is the
	 * concatenation of the two original strings sets. 
	 * 
	 * @param  a list of bricks 
	 * @return a list of bricks after applying the second rule to the input list 
	 * */
	private List<Brick> ruleTwo(List<Brick> bricks) {
		List<Brick> bricksToReturn = new ArrayList<>(); 
		if(bricks!=null && bricks.size()>1) {
			for(int i = 0;i<bricks.size()-1;i++) {
				Brick b1 = bricks.get(i); 
				Brick b2 = bricks.get(i+1); 

				if(b1.min.equals(1) && b2.max.equals(1) && b2.min.equals(1) && b2.max.equals(1)) {

					Set<String> tempStrings = new HashSet<>(); 
					if(b1.strings == null || b2.strings==null) {
						bricksToReturn.add(b1);
						bricksToReturn.add(b2);
					}else {
						for(String s1:b1.strings) {
							for(String s2:b2.strings) {
								tempStrings.add(s1.concat(s2));
							}
						}
						bricksToReturn.add(new Brick(tempStrings,new Index(1),new Index(1)));
					}
				}
			}
		}else {
			return bricks; 
		}

		return bricksToReturn;
	}

	
	/**
	 * Transform a brick in which the number of applications is constant (min = max)
	 * into one in which the indices are 1 (min = max = 1).
	 * Given the Brick(Strings,min,max) as input, the after applying the third rule
	 * new string set will represents the concatenation of S with itself for m times.
	 * 
	 * @param  a list of bricks 
	 * @return a list of bricks after applying the third rule to the input list 
	 * */
	private List<Brick> ruleThree(List<Brick> bricks) {
		List<Brick> bricksToReturn = new ArrayList<>(); 
		if(bricks==null) {
			return bricks;
		}else {
			for(Brick brick:bricks) {
				if(brick.max.equals(brick.min)) {
					Set<String> newBrickStrings = new HashSet<String>(); 
					for(int i=0; i<brick.min.getValue(); i++) {
						Set<String> tempStrings = new HashSet<String>(brick.strings); 
						for(String s1:brick.strings) {
							for(String s2:tempStrings) {
								tempStrings.add(s1.concat(s1)); 
							}
						}
						newBrickStrings.clear();
						newBrickStrings.addAll(tempStrings); 
					}
					bricksToReturn.add(new Brick(newBrickStrings, new Index(1), new Index(1))); 
				}
			}
		}
		return bricksToReturn; 
	}

	/**
	 * Merge two successive bricks in which the set of strings is the same
	 * into a single one modifying the indices. 
	 * **/
	private List<Brick> ruleFour(List<Brick> bricks) {
		List<Brick> bricksToReturn = new ArrayList<>(); 
		if(bricks!=null && bricks.size()>=2) {
			for(int i = 0; i<bricks.size()-1;i++) {
				Brick b1 = bricks.get(i);
				Brick b2 = bricks.get(i+1); 
				if((b1.strings != null && b2.strings!=null) || (b1.strings != null && b2.strings!=null && b1.strings.equals(b2.strings))){
					bricksToReturn.add(new Brick(b1.strings,b1.min.plus(b2.min), b1.max.plus(b2.max))); 
				}else {
					bricksToReturn.add(b1); 
				}
			}
		}else {
			return bricks; 
		}
		return bricksToReturn; 
	}

	/**
	 * Break a single brick with min > 1  && max != min into two simpler bricks.
	 * */
	private List<Brick> ruleFive() {
		List<Brick> bricksToReturn = new ArrayList<>(); 
		if(this.min.getValue()>=1 && this.max.getValue() != this.min.getValue()) {
		
		}
		return bricksToReturn; 
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
