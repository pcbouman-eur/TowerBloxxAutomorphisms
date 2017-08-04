package nl.eur.ese.pbouman.automorphism;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nl.eur.ese.pbouman.automorphism.Permutations.Permutation;

/**
 * Class that can be used to compute automorphisms of a Graph.
 * The algorithm starts with partitioning vertices of the graph according to their degree.
 * It then keeps partitioning these partitions based on the number of neighbours
 * in each of the partitions, until no further refinement is possible.
 * It then considers all combinations of the permutations of the partitions.
 * 
 * This algorithm will probably fail on toruses of moderate size, as all vertices
 * end up in the same partition on a torus, so all permutations of the vertices
 * need to be considered in those cases.
 * 
 * @author Paul
 *
 */
public class Automorphisms
{
	private Graph graph;
	
	private Map<Integer,Set<Integer>> partitionMap;
	private Set<Set<Integer>> currentPartitions;
	
	/**
	 * Constructor, which accepts the graph for which automorphisms will be computed
	 * @param g the graph for which we will compute the automorphisms
	 */
	public Automorphisms(Graph g)
	{
		graph = g;
		currentPartitions = new LinkedHashSet<>();
		partitionMap = new LinkedHashMap<>();
		init();
		run();
	}
	
	/**
	 * Initializes the partition map, based on the degrees of the vertices in the graph
	 */
	private void init()
	{
		int size = graph.size();
		Map<Integer,Set<Integer>> groups = new LinkedHashMap<>();
		for (int v=0; v < size; v++)
		{
			int key = graph.getInDegree(v)*size + graph.getOutDegree(v);
			if (!groups.containsKey(key))
			{
				groups.put(key, new LinkedHashSet<>());
			}
			groups.get(key).add(v);
		}
		submit(groups.values());
	}
	
	/**
	 * Keep refining the partition while something changes
	 */
	private void run()
	{
		while (step()) {}
	}
	
	/**
	 * Refine the partitions and return whether something changes
	 * @return A boolean indicating whether the partitions were refined or not
	 */
	private boolean step()
	{
		int size = graph.size();
		Map<Set<Integer>,Integer> setIDs = new LinkedHashMap<>();
		for (Set<Integer> s : partitionMap.values())
		{
			setIDs.put(s, setIDs.size());
		}
		
		Map<Map<Integer,Integer>, Set<Integer>> groups = new LinkedHashMap<>();
		for (int v=0; v < size; v++)
		{
			Map<Integer,Integer> key = new LinkedHashMap<>();
			for (Integer w : graph.getNeighbours(v))
			{
				key.merge(setIDs.get(partitionMap.get(w)), 1, Integer::sum);
			}
			if (!groups.containsKey(key))
			{
				groups.put(key, new LinkedHashSet<>());
			}
			groups.get(key).add(v);
		}
		
		Collection<Set<Integer>> res = new LinkedHashSet<>(groups.values());
		return submit(res);
	}
	
	/**
	 * Update the current partitions to a new set of partitions
	 * @param sets the new collection of partitions
	 * @return whether the new partitions are different from the current partitions
	 */
	private boolean submit(Collection<Set<Integer>> sets)
	{
		// Check whether something is different about the new partitions
		boolean change = !( sets.containsAll(currentPartitions) 
		                  && currentPartitions.containsAll(sets));
		
		// Update the partitions according to the new partititions
		partitionMap.clear();
		currentPartitions.clear();
		
		for (Set<Integer> s : sets)
		{
			currentPartitions.add(s);
			for (Integer i : s)
			{
				partitionMap.put(i, s);
			}
		}
		
		return change;
	}
	
	/**
	 * Generates a list with proper automorphisms of the graph, represented by their mappings.
	 * @return A list of automorphisms of the graph passed to the constructor
	 */
	public List<Mapping> computeAutomorphisms()
	{
		List<Mapping> result = new ArrayList<>();
		result.add(new Mapping(graph.size()));
		for (Set<Integer> set : currentPartitions)
		{
			List<Integer> indices = new ArrayList<>(set);
			List<Mapping> newList = new ArrayList<>();
			for (Permutation p : Permutations.permutations(set.size()))
			{
				for (Mapping m : result)
				{
					Mapping newMap = m.cloneAndPermute(indices, p.getList());
					if (graph.checkMapping(newMap))
					{
						newList.add(newMap);
					}
				}
			}
			result = newList;
		}
		return result;
	}
	
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("Potential Automorphism Partitioning\n");
		sb.append("--START OF CANDIDATE AUTOMORPHISM PARTITIONS--\n");
		int index = 1;
		for (Set<Integer> s : currentPartitions)
		{
			sb.append("Group "+index+" : "+s+"\n");
			index++;
		}
		sb.append("--END OF CANDIDATE AUTOMORPHISM PARTITIONS--\n");
		return sb.toString();
	}
	
}
