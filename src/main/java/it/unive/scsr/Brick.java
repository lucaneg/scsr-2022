package it.unive.scsr;


import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.analysis.representation.DomainRepresentation;


public class Brick extends BaseNonRelationalValueDomain<Brick>{

	private static final Brick EMPTY = new Brick();
	
	private static final int KI = 10;
	private static final int KS = 20;

	private Index min;
	private Index max; 

	private Set<String> strings;

	public Brick() {
		this.min = new Index(0);
		this.max = new Index(0);
		this.strings = new HashSet<>();
	}

	public Brick(String string) {
		this.min = new Index(1);
		this.max = new Index(1);
		this.strings = new HashSet<>();
		this.strings.add(string);
	}

	public Brick(Set<String> strings, Index min, Index max) {
		this.min = min;
		this.max = max;
		this.strings = strings;
	}

	/**
	 * The top element is defined as the brick [K](0,INFINITY)
	 * */
	@Override
	public Brick top() {
		return new Brick(null, new Index(0), Index.INFINITY);
	}

	/***
	 * The bottom element is defined as follow:  [S] (m,M) with M<m
	 * */
	@Override
	public Brick bottom() {
		return new Brick(new HashSet<>(), new Index(1), new Index(0));
	}

	/**
	 * The top element is defined as the brick [K](0,INFINITY)
	 * */
	@Override
	public boolean isTop() {
		return strings == null && min.equals(0) && max == Index.INFINITY;
	}

	/***
	 * The bottom element is defined as follow: 
	 *  1) [EMPTY](m,M) with (m,M)!=(0,0)
	 *  2) [S] (m,M) with M<m 
	 *  3) [S] (0,0) with S != EMPTY 
	 * 
	 * The three possible definitions are all bricks that do not represent any string. 
	 * They are invalid bricks, and they correspond to EMPTY
	 * */
	@Override
	public boolean isBottom() {
		boolean bottom = false; 
		if((this.strings != null) && (this.strings.isEmpty()) && !(this.min.equals(0) && this.max.equals(0))) {
			bottom = true; 
		}else if(this.max.less(this.min)) {
			bottom = true; 
		}else if(strings != null && !strings.isEmpty() && min.equals(0) && max.equals(0)) {
			bottom = true;
		}
		return bottom; 
	}

	/**
	 * The lub operator on single bricks is defiend as follow:
	 *  U ([S1](m1,M1), [S2](m2,M2)) = [S1 U S2](m,M)
	 *  where m = min(m1,m2) and M = max(M1,M2)  
	 * */

	@Override
	protected Brick lubAux(Brick other) throws SemanticException {
		Set<String> newStrings = null;

		if (this.strings != null && other.strings != null) {
			newStrings = new HashSet<>(this.strings);
			newStrings.addAll(other.strings);
		}

		Index newMin = Index.min(this.min, other.min);
		Index newMax = Index.max(this.max, other.max);

		return new Brick(newStrings, newMin, newMax);
	}

	/**
	 * The glb operator on single bricks is defined as follows:
	 *  Intersection ([S1](m1,M1), [S2](m2,M2)) = [S1 Itersection S2](m,M)
	 *  here m = min(m1,m2) and M = max(M1,M2)  
	 * */
	@Override
	public Brick glbAux(Brick other) {
		Set<String> newStrings=new HashSet<>();

		if (this.strings == null)
			newStrings = other.strings;
		else if (other.strings == null)
			newStrings = this.strings;
		else {
			for(String s:this.strings) {
				if(other.getStrings().contains(s)) {
					newStrings.add(s);
				}
			}
		}

		Index newMin = Index.max(this.min, other.min);
		Index newMax = Index.min(this.max, other.max);

		return new Brick(newStrings, newMin, newMax);
	}

	/**
	 * The widening operator returns: 
	 * 
	 * 1) Top if |[S1 U S2]| > KS
	 * 2) [S1 U S2](0,INFINITY) if (M-m) >KI 
	 * 3) [S1 U S2](m,M) otherwise 
	 * 
	 * */

	@Override
	protected Brick wideningAux(Brick other) throws SemanticException {

		Set<String> newStrings = new HashSet<>();

		if (this.strings != null && other.strings != null) {
			newStrings.addAll(this.strings);
			newStrings.addAll(other.strings);
		}

		if (newStrings.size() > KS || this.isTop() || other.isTop()) {
			return top();
		}

		Index min = Index.min(this.min, other.min);
		Index max = Index.max(this.max, other.max);

		if (max == Index.INFINITY || max.minus(min).greater(new Index(KI))) {
			return new Brick(newStrings, new Index(0), Index.INFINITY);
		} else {
			return new Brick(newStrings, min, max);
		}
	}


	/**
	 * The partial order <= is defined as follow: 
	 * Given two bricks [C1](m1,M1) and [C2](m2,M2), [C1](m1,M1) <= [C2](m2,M2) if: 
	 * [C1](m1,M1) is bottom or [C2](m2,M2) is top or if: C1 contains C2 and m1>=m2 and M1<=M2 
	 * */
	@Override
	protected boolean lessOrEqualAux(Brick other) throws SemanticException {
		if (this.strings == null && other.strings != null) {
			return false;
		}

		if(this.isBottom() || other.isTop()) {
			return true; 
		}

		if (this.strings != null && other.strings != null) {
			for (String string: this.strings) {
				if (!other.strings.contains(string)) {
					return false;
				}
			}
		}
		return this.min.greater(other.min) && this.max.less(other.max); 
	}


	@Override
	public DomainRepresentation representation() {
		String representation=""; 
		if(isTop()) {
			representation=Lattice.TOP_STRING;
		} else if(isBottom()) {
			representation=Lattice.BOTTOM_STRING;
		}else {
			if(this.strings!=null) {
				representation = String.format("%s(%s,%s)",this.strings.toString(),this.min.toString(),this.max.toString());
			}
		}

		final String toPrint = representation;
		return new DomainRepresentation() {

			@Override
			public String toString() {
				return toPrint;
			}

			@Override
			public int hashCode() {
				return 0;
			}

			@Override
			public boolean equals(Object obj) {
				return false;
			}
		};
	}

	@Override
	public boolean equals(Object obj) {
		if(obj==null || !(obj instanceof Brick)){
			return false;
		}	

		Brick brick = (Brick) obj; 
		return 	Objects.equals(this.strings, brick.strings)
				&& Objects.equals(this.min, brick.min) 
				&& Objects.equals(this.max, brick.max);

	}


	@Override
	public int hashCode() {
		return Objects.hash(strings, min, max);
	}

	public Set<String> getStrings() {
		return strings;
	}

	public void setStrings(Set<String> strings) {
		this.strings = strings;
	}

	public Index getMin() {
		return min;
	}

	public void setMin(Index min) {
		this.min = min;
	}

	public Index getMax() {
		return max;
	}

	public void setMax(Index max) {
		this.max = max;
	}
}
