package it.unive.scsr.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import it.unive.scsr.Brick;
import it.unive.scsr.Bricks;
import it.unive.scsr.Index;

public class BrickUtils {


	/**
	 * Remove unnecessary bricks, that is, bricks of the form [""](0,0), because they represent
	 * only the empty string, which is the neutral element of the concatenation operation.
	 * */
	private static List<Brick> ruleOne(List<Brick> bricks) {
		return bricks.stream()
				.filter(brick-> brick!=null && brick.getStrings()!=null && brick.getMax()!=null && !brick.getStrings().isEmpty() && brick.getMin().getValue()!=0 && brick.getMax().getValue()!=0)
				.collect(Collectors.toList());
	}


	/**
	 * Merge successive bricks with the same indices, min=1 and max=1, in a new single
	 * brick where the indices remain the same (min=max=1), and the strings set is the
	 * concatenation of the two original strings sets. 
	 * 
	 * @param  a list of bricks 
	 * @return a list of bricks after applying the second rule to the input list 
	 * */
	private static List<Brick> ruleTwo(List<Brick> bricks) {

		List<Brick> bricksToReturn = new ArrayList<>(); 
		
		if(bricks!=null && bricks.size()>1) {
			for(int i = 0;i<bricks.size()-1;i++) {
				Brick b1 = bricks.get(i); 
				Brick b2 = bricks.get(i+1); 

				if(b1.getMin().equals(1) && b1.getMax().equals(1) && b2.getMin().equals(1) && b2.getMax().equals(1)) {
					Set<String> tempStrings = new HashSet<>(); 
					if(b1.getStrings() == null || b2.getStrings()==null) {
						bricksToReturn.add(b1);
						bricksToReturn.add(b2);
					}else {
						for(String s1:b1.getStrings()) {
							for(String s2:b2.getStrings()) {
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
	public static  List<Brick> ruleThree(List<Brick> bricks) {
		List<Brick> bricksToReturn = new ArrayList<>(); 
		for(Brick brick:bricks) {

			if (brick.getStrings() == null) {
				bricksToReturn.add(new Brick(null, new Index(1), new Index(1)));
			}

			Set<String> newStrings = new HashSet<>(brick.getStrings());

			if (brick.getMin().equals(brick.getMax())) {

				for (int i = 0; i < brick.getMin().getValue() - 1; i++) {
					Set<String> temp = new HashSet<>();
					for (String s1: newStrings) {
						for (String s2: brick.getStrings()) {
							temp.add(s1.concat(s2));
						}
					}
					newStrings.clear();
					newStrings.addAll(temp);
				}
				bricksToReturn.add( new Brick(newStrings, new Index(1), new Index(1)));
			}
			else {
				bricksToReturn.add(brick);
			}
		}
		return bricksToReturn; 


	}

	/**
	 * Merge two successive bricks in which the set of strings is the same
	 * into a single one modifying the indices. 
	 * **/
	private static List<Brick> ruleFour(List<Brick> bricks) {
		List<Brick> bricksToReturn = new ArrayList<>();
		if (bricks.size() == 1) {
			return bricks;
		}

		for(int i = 0; i < bricks.size() - 1; i++) {
			Brick b1 = bricks.get(i);
			Brick b2 = bricks.get(i + 1);

			if ((b1.getStrings() == null && b2.getStrings() == null) || (b1.getStrings() != null && b2.getStrings() != null && b1.getStrings().equals(b2.getStrings()))) {
				Index newMin = b1.getMin().plus(b2.getMin());
				Index newMax = b1.getMax().plus(b2.getMax());

				bricksToReturn.add(new Brick(b1.getStrings(), newMin, newMax));
				i++;
			}
			else {
				bricksToReturn.add(b1);
				if (i == bricksToReturn.size() - 2) {
					bricksToReturn.add(b2);
				}
			}
		}

		return bricksToReturn;
	}

	/**
	 * Break a single brick with min > 1  && max != min into two simpler bricks.
	 * */
	public static List<Brick> ruleFive(List<Brick> bricks) {
		List<Brick> bricksToReturn = new ArrayList<>(); 
		for(Brick brick:bricks) {
			List<Brick> newList = new ArrayList<>();
			if (!brick.isTop() && !brick.isBottom() && brick.getMin().greaterOrEqual(new Index(1)) && brick.getMax().greater(brick.getMin())) {
				Brick brickToAdd = new Brick(brick.getStrings(), brick.getMin(), brick.getMin()); 
				List<Brick> tempBricks = new ArrayList<>(); 
				bricks.add(brickToAdd); 
				newList.add(ruleThree(tempBricks).get(0));
				newList.add(new Brick(brick.getStrings(), new Index(0), brick.getMax().minus(brick.getMin())));
			}
			else {
				newList.add(brick);
			}
			bricksToReturn.addAll(newList);
		}
		return bricksToReturn;
	}

	/**
	 * Normalizes a list of bricks
	 *
	 * @param bricks an ordered list of bricks
	 * @return the normalized list, following the 5 rules stated in the paper
	 *
	 */

	public static List<Brick> normalize(List<Brick> bricks) {

		int startSize=0;
		List<Brick> bricksToReturn = bricks;

		bricksToReturn = ruleOne(bricksToReturn); 
		do{
			startSize = bricksToReturn.size();
			bricksToReturn = ruleOne(bricksToReturn);
			bricksToReturn = ruleTwo(bricksToReturn); 
			bricksToReturn = ruleThree(bricksToReturn);
			bricksToReturn = ruleFour(bricksToReturn);
			bricksToReturn = ruleFive(bricksToReturn);		

		}while (bricksToReturn.size() < startSize);

		return new ArrayList<>(bricksToReturn);
	}


	/**
	 *Pad Algorithm 
	 */
	public static  List<Brick> pad(Bricks b1, Bricks b2) {
		ArrayList<Brick> l1 = new ArrayList<>(b1.getBricks());
		ArrayList<Brick> l2 = new ArrayList<>(b2.getBricks());
		int n1 = l1.size();
		int n2 = l2.size();
		int n = n2 - n1;

		if (n <= 0) {
			return b1.getBricks();
		}   

		List<Brick> newList = new ArrayList<>();
		int emptyBricksAdded = 0;

		for (int i = 0; i < n2; i++) {
			if (emptyBricksAdded >= n) {
				newList.add(l1.remove(0));
			}
			else if (l1.isEmpty() || l1.get(0) != l2.get(i)) {
				newList.add(new Brick());
				emptyBricksAdded++;
			}
			else {
				newList.add(l1.remove(0));
			}
		}

		return newList;
	}
}
