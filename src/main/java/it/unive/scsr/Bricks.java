package it.unive.scsr;

import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.SemanticDomain.Satisfiability;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.analysis.representation.DomainRepresentation;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.symbolic.value.operator.binary.BinaryOperator;
import it.unive.lisa.symbolic.value.operator.binary.StringConcat;
import it.unive.lisa.symbolic.value.operator.binary.StringContains;
import it.unive.lisa.symbolic.value.operator.ternary.StringSubstring;
import it.unive.lisa.symbolic.value.operator.ternary.TernaryOperator;
import it.unive.scsr.utils.BrickUtils;


public class Bricks extends BaseNonRelationalValueDomain<Bricks>{

	static final int kL = 10;

	private List<Brick> bricks;

	public Bricks() {
		bricks = new ArrayList<>();
	}

	Bricks(List<Brick> bricks) {
		this.bricks = bricks;
	}

	Bricks(Brick brick) {
		this.bricks = new ArrayList<>();
		bricks.add(brick);
	}

	Bricks(String string) {
		this(new Brick(string));
	}

	@Override
	public Bricks top() {
		return new Bricks(new Brick().top());
	}

	@Override
	public Bricks bottom() {
		return new Bricks();
	}

	/**
	 * The top element of BR is a list containing only one brick,
	 * */
	@Override
	public boolean isTop() {
		return this.bricks.size() == 1 && this.bricks.get(0).isTop();
	}

	/**
	 * 
	 * The bottom element is an empty list (it does not represent any string at all, not even the empty string) 
	 * or any list that contains at least one invalid element. 
	 * 
	 * */
	@Override
	public boolean isBottom() {
		if (this.bricks.isEmpty())
			return true;

		for (Brick brick: this.bricks) {
			if (brick.isBottom()) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected Bricks lubAux(Bricks other) throws SemanticException {

		List<Brick> bricksToReturn = new ArrayList<>();
		List<Brick> l1 = BrickUtils.pad(this, other);

		List<Brick> l2 = BrickUtils.pad(other, this);
		for (int i = 0; i < l1.size(); i++) {
			bricksToReturn.add(l1.get(i).lub(l2.get(i)));
		}

		return new Bricks(BrickUtils.normalize(bricksToReturn));
	}


	@Override
	protected Bricks glbAux(Bricks other) throws SemanticException {

		List<Brick> bricksToReturn = new ArrayList<>();
		List<Brick> l1 = BrickUtils.pad(this, other);
		List<Brick> l2 = BrickUtils.pad(other, this);

		for (int i = 0; i < l1.size(); i++) {
			bricksToReturn.add(l1.get(i).glb(l2.get(i)));
		}
		return new Bricks(BrickUtils.normalize(bricksToReturn));
	}


	@Override
	protected Bricks wideningAux(Bricks other) throws SemanticException {

		if ((!this.lessOrEqual(other) && !other.lessOrEqual(this)) || (this.bricks.size() > kL || other.bricks.size() > kL)) {
			return top();
		}else {
			List<Brick> bricksToReturn = new ArrayList<>();
			List<Brick> l1 = BrickUtils.pad(this,other);
			List<Brick> l2 = BrickUtils.pad(other,this);

			int n = l1.size();

			for(int i = 0; i < n; i++) {
				Brick b1 = l1.get(i);
				Brick b2 = l2.get(i);
				bricksToReturn.add(b1.widening(b2));
			}
			return new Bricks(BrickUtils.normalize(bricksToReturn));
		}
	}


	@Override
	protected boolean lessOrEqualAux(Bricks other) throws SemanticException {
		List<Brick> l1 = BrickUtils.pad(this,other);
		List<Brick> l2 = BrickUtils.pad(other,this);

		for (int i = 0; i < l1.size(); i++) {
			if (!(l1.get(i).lessOrEqual(l2.get(i))))
				return false;
		}

		return true;
	}


	/**
	 * @return a new bricks object containing the normalized union of the bricks
	 * present in this and other object. 
	 * */
	public Bricks add(Bricks other) {
		List<Brick> newList = new ArrayList<>();
		newList.addAll(this.bricks);
		newList.addAll(other.bricks);
		return new Bricks(BrickUtils.normalize(newList));
	}


	/**
	 * Check if the bricks list contain only one brick with a single string that have only one char
	 * */
	private boolean isChar() {
		return bricks.size() == 1 && bricks.get(0).getStrings().size() == 1
				&& ((String) bricks.get(0).getStrings().toArray()[0]).length() == 1;
	}

	/**
	 * @return the first char present in the string or null if the string contain more then one char 
	 * */
	private Character getChar() {
		return isChar() ? ((String) bricks.get(0).getStrings().toArray()[0]).charAt(0) : null;
	}

	/**
	 * @return if the constant is null will return TOP 
	 * */
	@Override
	protected Bricks evalNullConstant(ProgramPoint pp) {
		return new Bricks().top();
	}


	/**
	 * @return a new Bricks object that will contain a single brick in the list with a single string
	 * that will represent the integer or the string given in input. If the costant is not
	 * an integer or a string the function will return top. 
	 * */
	@Override
	protected Bricks evalNonNullConstant(Constant constant, ProgramPoint pp) {

		if (constant.getValue() instanceof Integer) {
			return new Bricks((constant.getValue()).toString());
		}

		if (constant.getValue() instanceof String) {
			String s  = (String) constant.getValue();
			/*
			 * Since the string s is in the form "string" 
			 * we have to remove the quotes before saving it 
			 * */
			s = s.substring(1, s.length() - 1);
			return new Bricks(s);
		}

		return top();
	}

	/**
	 * If the operator is a StringConcat will return a new bricks object that will  
	 * contain a normalized brick list create by joining the brick list of the left object
	 * with the one on the right. In any other case will return the top. 
	 * 
	 * */
	@Override
	protected Bricks evalBinaryExpression(BinaryOperator operator, Bricks left, Bricks right, ProgramPoint pp) {
		if (operator instanceof StringConcat) {
			return left.add(right);
		}
		return new Bricks().top();
	}


	@Override
	protected Bricks evalTernaryExpression(TernaryOperator operator, Bricks left, Bricks middle, Bricks right, ProgramPoint pp) {

		if (operator instanceof StringSubstring) {

			List<Brick> brickList = BrickUtils.normalize(left.bricks);
			List<Brick> newList = new ArrayList<>(brickList);
			Set<String> substrings = new HashSet<>();

			Brick brick = brickList.get(0);

			int start = Integer.parseInt((String) middle.bricks.get(0).getStrings().toArray()[0]);
			int end = Integer.parseInt((String) right.bricks.get(0).getStrings().toArray()[0]);

			//check if the first brick and all string inside respect the condition  min = max = 1 and len(s)>=e 
			if (brick.getMin().equals(1) && brick.getMax().equals(1)) {

				for (String s: brick.getStrings()) {
					if (s.length() < end) {
						return new Bricks().top();
					}						
				}

				for (String string : brick.getStrings()) {
					substrings.add(string.substring(start, end));
				}

				newList.set(0, new Brick(substrings, new Index(1), new Index(1)));
				return new Bricks(newList);
			}
		}

		return new Bricks().top();
	}


	private boolean appearInAllStringsOfBrick(Brick brick, String c) {
		boolean appear = true;
		if(brick.getMin().greaterOrEqual(1) && brick.getMin().lessOrEqual(brick.getMax())) {
			for(String s:brick.getStrings()) {
				if(!s.contains(c)) {
					appear = false;
				}
			}
		}else {
			appear=false; 
		}
		return appear;
	}

	/**
	 * 
	 * The semantics of stringContains returns true if the character c appears in all the strings of a certain
	 * brick with minimal index min >= 1. It returns false if we are sure that c does not appear in any string
	 * of any brick of the abstract value. Otherwise, we have to return TOP.
	 * */
	@Override
	protected Satisfiability satisfiesBinaryExpression(BinaryOperator operator, Bricks left, Bricks right, ProgramPoint pp) throws SemanticException {

		if (operator instanceof StringContains) {
			if (right.isChar() && right.getChar() != null) {
				String c = right.getChar().toString();
				for(Brick brick:left.bricks) {
					if(appearInAllStringsOfBrick(brick,c)){
						return Satisfiability.SATISFIED;
					}
				}

				boolean contains = true; 
				for(Brick brick:left.bricks) {
					for(String s:brick.getStrings()) {
						if(s.contains(c)) {
							contains=false; 
						}
					}
				}

				if(contains==true) {
					return Satisfiability.NOT_SATISFIED;
				}
			}
		}
		return  Satisfiability.UNKNOWN;
	}


	@Override
	public DomainRepresentation representation() {

		StringBuilder representation = new StringBuilder("{");

		if(isTop()) {
			representation.append(Lattice.TOP_STRING);
			representation.append("}");
		} else if(isBottom()) {
			representation.append(Lattice.BOTTOM_STRING);
			representation.append("}");
		}else {
			if(this.bricks!=null) {
				for (int i = 0; i < bricks.size(); i++) {
					representation.append(bricks.get(i).toString());
					if (i < bricks.size() - 1) {
						representation.append(",");
					}

				}
			}
			representation.append("}");
		}

		final String toPrint = representation.toString();
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
		if (obj == null || !(obj instanceof Brick)) {
			return false; 
		}
		Bricks objBricks = (Bricks) obj;
		return Objects.equals(this.bricks, objBricks.bricks);
	}

	@Override
	public int hashCode() {
		return Objects.hash(bricks);
	}

	public List<Brick> getBricks() {
		return bricks;
	}

	public void setBricks(List<Brick> bricks) {
		this.bricks = bricks;
	}
}
