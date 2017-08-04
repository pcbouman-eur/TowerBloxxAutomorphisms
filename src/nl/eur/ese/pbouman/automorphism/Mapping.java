package nl.eur.ese.pbouman.automorphism;
import java.util.Arrays;
import java.util.List;

/**
 * Models a mapping of vertices in a graph of n vertices, with labels 0...n-1
 * onto these vertices. A proper mapping is a bijection of the set of vertices
 * on the set of vertices. An automorphism is always a proper mapping, but not
 * every proper mapping is an automorphism, as this depends on the structure
 * of the graph.
 * @author Paul Bouman
 *
 */
public class Mapping {

	private int [] mapping;
	
	private int size;
	
	private Mapping() {}
	
	/**
	 * Initiliazes a partial mapping for a graph with n vertices
	 * In the default case, no vertices are mapped at all
	 * @param n the number of vertices of the graph we consider
	 */
	public Mapping(int n)
	{
		this.size = n;
		this.mapping = new int[n];
		for (int i=0; i < n; i++)
		{
			this.mapping[i] = -1;
		}
	}
	
	/**
	 * Lookup the current mapping of the vertex with label v in the graph
	 * @param v the label of the vertex for which we want to find the mapped vertex
	 * @return the label of the vertex onto which v was mapped
	 */
	public int lookup(int v)
	{
		if (v <0 || v >= size)
		{
			throw new IllegalArgumentException("Index "+v+" is not valid for a mapping of size "+size);
		}
		return mapping[v];
	}
	
	/**
	 * Get the number of vertices for which this Automorphism is defined
	 * @return the number of vertices
	 */
	public int getSize()
	{
		return size;
	}
	
	/**
	 * Check whether this mapping is incomplete, i.e. some vertices are not mapped
	 * onto some other vertices
	 * @return whether all vertices are mapped onto some vertex
	 */
	public boolean isIncomplete()
	{
		for (int i=0; i < size; i++)
		{
			if (mapping[i] < 0)
			{
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Check whether this is a proper mapping or just a partial one.
	 * A proper mapping is a bijection of the set of vertices on itself
	 * @return
	 */
	public boolean isValid()
	{
		boolean [] check = new boolean[size];
		for (int i=0; i < size; i++)
		{
			if (mapping[i] < 0)
			{
				return false;
			}
			check[i] = true;
		}
		for (int i=0; i < size; i++)
		{
			if (!check[i])
			{
				return false;
			}
		}
		return true;
	}

	/**
	 * Takes a list of vertex indices, and maps them according to the provided permutation.
	 * For example, if the 'org' list contains [3, 5, 7] and the 'perm' list contains
	 * [2,1,3], vertex 3 will be mapped onto vertex 5, vertex 5 onto vertex 3, and vertex 7 on itself.
	 * @param org indices of vertices of the graph; these should be in the range 0 ... size-1
	 * @param perm permutation indices, this should be a permutation of the indices 0,1,...,org.size()-1
	 */
	public void applyPermutation(List<Integer> org, List<Integer> perm)
	{
		if (org.size() != perm.size())
		{
			throw new IllegalArgumentException("Original indices and permutation must have same length");
		}
		for (int i=0; i < org.size(); i++)
		{
			int from = org.get(i);
			int permIndex = perm.get(i);
			if (permIndex < 0 || org.size() <= permIndex)
			{
				throw new IllegalArgumentException("Index "+permIndex+" in permutation is out of bounds");
			}
			int to = org.get(permIndex);
			
			if (from < 0 || size <= from || to < 0 || size <= to)
			{
				throw new IllegalArgumentException("The original list of indices contains an invalid index");
			}
			mapping[from] = to;
		}
	}
	
	/**
	 * Expands this mapping using a list of vertex labels and a permutation, but returns a
	 * new object, without modifying the current object. See applyPermutation as well.
	 * @param org a list of vertex labels, a subset of integes in the range 0, ..., size-1
	 * @param perm a permutation of 
	 * @return
	 */
	public Mapping cloneAndPermute(List<Integer> org, List<Integer> perm)
	{
		Mapping m = new Mapping();
		m.size = size;
		m.mapping = Arrays.copyOf(mapping, size);
		m.applyPermutation(org, perm);
		return m;
	}
	
	/**
	 * This method reshuffles the bits in a long, assuming that the state of every vertex
	 * in the graph corresponds to a number of bits in the long. 
	 * @param in the long containing the states of the vertices
	 * @param stateSize the number of bits per vertex
	 * @return a reshuffled long with the states mapped to different vertices according to this mapping
	 */
	public long mapLong(long in, int stateSize)
	{
		long mask = (1<<stateSize)-1;
		long res = 0;
		for (int from=0; from < size; from++)
		{
			int to = mapping[from];
			long masked = in & mask;
			long shifted;
			if (from >= to)
			{
				shifted = masked >> (stateSize*(from - to));
			}
			else
			{
				shifted = masked << (stateSize*(to - from));
			}
			res = res | shifted;
			mask = mask << stateSize;
		}
		return res;
	}
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(mapping);
		result = prime * result + size;
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
		Mapping other = (Mapping) obj;
		if (!Arrays.equals(mapping, other.mapping))
			return false;
		if (size != other.size)
			return false;
		return true;
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("Mapping defined on "+size+" vertices\n");
		sb.append("--START OF MAPPING--\n");
		for (int i=0; i < size; i++)
		{
			if (mapping[i] >= 0)
			{
				sb.append(""+i+" to "+mapping[i]+"\n");
			}
		}
		sb.append("--END OF MAPPING--\n");
		return sb.toString();
	}
	

	/**
	 * Convenience function to create a mapping based on a graph, a set of vertex labels and a
	 * permutation of indices
	 * @param g The graph, of which the size will be used to generate a mapping
	 * @param org A list of vertex labels in the graph
	 * @param perm A permutation of indices of elements in the 'org' list
	 * @return A mapping which corresponds to the permutation of the vertices
	 */
	public static Mapping construct(Graph g, List<Integer> org, List<Integer> perm)
	{
		Mapping m = new Mapping(g.size());
		m.applyPermutation(org, perm);
		return m;
	}
	
}
