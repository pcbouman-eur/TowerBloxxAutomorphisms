package nl.eur.ese.pbouman.towerbloxx;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import nl.eur.ese.pbouman.automorphism.Graph;
import nl.eur.ese.pbouman.automorphism.Automorphisms;
import nl.eur.ese.pbouman.automorphism.Mapping;

/**
 * Class that models a game of TowerBloxx on grids,
 * encoding states as long values
 * @author Paul Bouman
 *
 */
public class TowerBloxx {
	
	private static final int [] scores = {205, 966, 2677, 5738};
	private static final boolean DEBUG = false;
	
	private Graph g;
	private StateUtils su;
	private List<Mapping> mappings;
	private int dim;
	private int [][] neighbours;
	
	/**
	 * Produces a TowerBlox game on a kxk grid
	 * @param k the dimension of the grid of the TowerBloxx game we want to model
	 */
	public TowerBloxx(int k)
	{
		g = Graph.gridGraph(k);
		mappings = (new Automorphisms(g)).computeAutomorphisms();
		su = new StateUtils(g,2);
		dim = k;
		
		// We cache the neighbours of each state in an array, in the hope that this is faster
		// than obtaining the neighbours in each step
		neighbours = new int[g.size()][];
		for (int v=0; v < g.size(); v++)
		{
			List<Integer> nbsList = g.getNeighbours(v);
			int [] nbs = new int[nbsList.size()];
			for (int i=0; i < nbsList.size(); i++)
			{
				nbs[i] = nbsList.get(i);
			}
			neighbours[v] = nbs;
		}
	}
	
	/**
	 * Maps a long to its canonical representation. This way, multiple rotations and mirrorings
	 * of a TowerBloxx state can be mapped onto a single canonical version of that state,
	 * removing the need for search algorithms to consider symmetrics states 
	 * @param in The state we want to obtain the canonical variant of
	 * @return the canonical variant of the state
	 */
	public long reduce(long in)
	{
		long lowest = in;
		for (Mapping m : mappings)
		{
			lowest = Math.min(lowest, m.mapLong(in, su.getStateSize()));
		}
		return lowest;
	}
	
	/**
	 * Generate possible follow up states for a given state in a game of Tower Bloxx
	 * @param state the current state of a game of towerbloxx
	 * @return a unique set of subsequent states that can be obtained by doing a single move on state
	 */
	public Set<Long> expand(long state)
	{
		Set<Long> result = new LinkedHashSet<>();
		for (int v=0; v < g.size(); v++)
		{
			int val = (int) su.getState(state, v);
			int [] hist = getHist(state, v);
			
			//System.out.println("v="+v+", val="+val+", hist="+Arrays.toString(hist));
			
			if (val == 0 && hist[0] > 0)
			{
				// Currently it is a type-0 building, and it has type-0 neighbours
				// So: change it to a type-1 building
				result.add(reduce(su.changeState(state, v, 1)));
				if (DEBUG)
				{
					System.out.println("0 -> 1 ("+v+")");
				}
			}
			if (val < 2 && hist[0] > 0 && hist[1] > 0)
			{
				// Currently it is less than a type-2 building and it has type-0
				// and type-1 neighbours.
				// So: change it to a type-2 building
				result.add(reduce(su.changeState(state, v, 2)));
				if (DEBUG)
				{
					System.out.println("1 -> 2 ("+v+")");
				}
			}
			if (val < 3 && hist[0] > 0 && hist[1] > 0 && hist[2] > 0)
			{
				// Currently it is less than a type-3 building, but we can upgrade it
				// So: do so
				result.add(reduce(su.changeState(state, v, 3)));
				if (DEBUG)
				{
					System.out.println("2 -> 3 ("+v+")");
				}
			}
			if (val < 3 && hist[0] == 0)
			{
				// It is not a type-3 building, but has no type-0 neighbours.
				// So: We consider demolishing one of the neighbour to type-0
				// TODO: refine -> if you have only two neighbours, you can never become type-3
				//     , so if you are type-2 demolishing something is not necessary
				for (int w : neighbours[v])
				{
					if (su.getState(state, w) > 0)
					{
						result.add(reduce(su.changeState(state, w, 0)));
						if (DEBUG)
						{
							System.out.println("? -> 0 ("+w+")");
						}
					}
				}
			}
		}
		return result;
	}
	
	/**
	 * Computes a histogram of how many neighbours of a given state are in a certain states 
	 * @param states a long encoding the states of all vertices in the graph
	 * @param v the state of which we want to analyze the neighbours
	 * @return a histogram with the number of neighbours in a certain states,
	 *         e.g. hist[1] is the number of neighbours that have currently 1 as a state
	 */
	public int [] getHist(long states, int v)
	{
		int [] result = new int[4];
		for (int w : neighbours[v])
		{
			result[(int)su.getState(states, w)]++;
		}
		return result;
	}
	
	/**
	 * Computes the score based on a TowerBloxx configuration encoded as a long
	 * @param state the state for which we want to compute a score
	 * @return the score associated with the states
	 */
	public long computeScore(long states)
	{
		long score = 0;
		for (int t=0; t < g.size(); t++)
		{
			int val = (int) su.getState(states, t);
			score += scores[val];
		}
		return score;
	}
	
	/**
	 * Convert a long encoding a TowerBloxx configuration to a human-readable String
	 * @param states the states of the various vertices in a Tower Bloxx configuration encoded as long
	 * @return a String with a human readable representation of the Tower Blox game
	 */
	public String toGrid(long states)
	{
		return su.toGrid(states, dim);
	}
}
