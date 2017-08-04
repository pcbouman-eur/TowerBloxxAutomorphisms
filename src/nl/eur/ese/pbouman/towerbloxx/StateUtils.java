package nl.eur.ese.pbouman.towerbloxx;

import nl.eur.ese.pbouman.automorphism.Graph;

/**
 * Utility class that allows us to encode and decode the
 * state of n vertices as longs
 * @author Paul Bouman
 */
public class StateUtils
{
	private int stateSize;
	
	private long [] basicMasks;
	private long [] neighbourMasks;
	
	public StateUtils(Graph g, int stateSize)
	{
		if (stateSize * g.size() > 63)
		{
			throw new IllegalArgumentException("Cannot encode this using long's");
		}
		
		this.stateSize = stateSize;
		this.neighbourMasks = new long[g.size()];
		this.basicMasks = new long[g.size()];
		
		long base = (1 << stateSize)-1;
		for (int v=0; v < g.size(); v++)
		{
			long mask = 0;
			for (Integer w : g.getNeighbours(v))
			{
				mask = mask | (base << (stateSize*w));
			}
			neighbourMasks[v] = mask;
			basicMasks[v] = (base << (stateSize*v));
		}		
	}
	
	/**
	 * Provides a long that can be used to mask the neighbouring states
	 * of a given vertex.
	 * @param v the vertex for which we want to consider the neighbours
	 * @return A mask that contains 1's at the state bits of the neighbours of v, and 0's elsewhere
	 */
	public long getNeighbourMask(int v)
	{
		return neighbourMasks[v];
	}
	
	/**
	 * Returns a long with the states of the neighbours of a vertex v
	 * @param states the current states of all vertices 
	 * @param v the vertex for which we want to consider the neighbours
	 * @return the long states, with all bits belonging to other vetrices than v's neighbours set to 0
	 */
	public long getNeighbours(long states, int v)
	{
		return states & neighbourMasks[v];
	}
	
	/**
	 * Returns a mask that can be used to only obtain the bits belonging to the state of a vertex v.
	 * Note that these bits are not yet shifted
	 * @param v the vertex for which we want to obtain the state bits
	 * @return a long all state bits except those of vertex v zeroed
	 */
	public long getStateMask(int v)
	{
		return basicMasks[v];
	}
	
	/**
	 * Obtain the state of a single vertex v from a long containing the states of multiple vertices
	 * @param states the long containing the states of multiple vertices
	 * @param v the vertex for which we want to obtain the state
	 * @return the state of vertex v in the states long
	 */
	public long getState(long states, int v)
	{
		return (states & basicMasks[v]) >> (v*stateSize);
	}
	
	/**
	 * Set the state of a certain vertex to a given value in a long encoding multiple states
	 * @param states a long encoding the states of multiple vertices
	 * @param v the vertex for which we want to modify the state
	 * @param val the new state for vertex v
	 * @return a long containing updated states
	 */
	public long changeState(long states, int v, long val)
	{
		if (val >= 1<<stateSize || val < 0)
		{
			throw new IllegalArgumentException("The provided value for a vertex does not fit in the number of bits reserved per vertex");
		}
		return (states & ~basicMasks[v]) | (val << (v*stateSize));
	}
	
	/**
	 * Converts a long with states per vertex to a human-readable string represented as a grid.
	 * This assumes the vertices can be correctly layed out as a grid.
	 * @param grid a long containing the states of vertices in the grid graph
	 * @param dim the (square) dimension of the grid, e.g. 3 for a 3x3 grid graph
	 * @return A string with the states of the vertices layed out as a grid
	 */
	public String toGrid(long grid, int dim)
	{
		StringBuilder sb = new StringBuilder();
		for (int i=0; i < dim; i++)
		{
			for (int j=0; j < dim; j++)
			{
				int index = i+j*dim;
				long val = (grid & basicMasks[index]) >> (index*stateSize);
				sb.append(val);
			}
			sb.append("\n");
		}
		return sb.toString();
	}
	
	/**
	 * Returns the number of bits used to represent states by this StateUtils object
	 * @return the number of bits used for a state
	 */
	public int getStateSize()
	{
		return stateSize;
	}
}
