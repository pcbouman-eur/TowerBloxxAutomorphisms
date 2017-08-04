package nl.eur.ese.pbouman.automorphism;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Utility class used to generate permutations.
 * A Permutation can be transformed to a list of integers, that indicate the indices
 * of elements in an original sequence.
 * @author Paul Bouman
 *
 */

public class Permutations
{
	
	/**
	 * Generate all permutations of the indices 0...n-1
	 * @param n the number of indices for which permutations will be generated
	 * @return A list of permutation objects, one for each permutation
	 */
	public static List<Permutation> permutations(int n)
	{
		List<Permutation> result = new ArrayList<>();
		boolean [] indices = new boolean[n];
		permute(indices, null, result);
		return result;
	}
	
	private static void permute(boolean [] indices, Permutation p, List<Permutation> res)
	{
		// Recursive definition: if all indices are permuted, we have a 'complete' permutation
		// and can stop generating more.
		if (p != null && p.depth == indices.length)
		{
			res.add(p);
			return;
		}
		// If not, we consider each index
		for (int i=0; i < indices.length; i++)
		{
			// If the index was already permuted, consider the next index
			if (indices[i])
			{
				continue;
			}
			// For now, we state that index i is permuted
			indices[i] = true;
			Permutation newP;
			if (p == null)
			{
				newP = new Permutation(p, i, 1);
			}
			else
			{
				newP = new Permutation(p, i, p.depth+1);
			}
			// Build permutations using the remaining indices
			permute(indices, newP, res);
			indices[i] = false;
		}
	}
	
	/**
	 * Class which represents a (possibly partial) permutation
	 * @author Paul Bouman
	 *
	 */
	public static class Permutation
	{
		private int depth;
		private int index;
		private Permutation prev;
		
		/**
		 * Constructor for a permutation, which defines the position
		 * of the index at position 'depth'
		 * @param prev a permutation containing information about other remappings
		 * @param index the new index of the mapped element
		 * @param depth the depth of this permutation object in the chain of permutation objects
		 */
		public Permutation(Permutation prev, int index, int depth)
		{
			this.depth = depth;
			this.index = index;
			this.prev = prev;
		}
		
		/**
		 * Create a list with permuted indices corresponding to the
		 * permutation modeled by this object
		 * @return a list of permuted indices
		 */
		public List<Integer> getList()
		{
			List<Integer> list = new ArrayList<>(depth);
			makeList(list);
			Collections.reverse(list);
			return list;
		}
		
		private void makeList(List<Integer> list)
		{
			list.add(index);
			if (prev != null)
			{
				prev.makeList(list);
			}
		}
		
		@Override
		public String toString()
		{
			return getList().toString();
		}
		
	}
}
