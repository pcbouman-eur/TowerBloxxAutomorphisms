package nl.eur.ese.pbouman.towerbloxx;

/**
 * Main class that can be used to perform TowerBloxx computations
 * @author Paul Bouman
 */
public class Main {
	public static void main(String [] args)
	{
		int k = 3;
		if (args.length == 1)
		{
			k = Integer.parseInt(args[0]);
		}
		
		// Create a new TowerBloxx object
		TowerBloxx tb = new TowerBloxx(k);
				
		
		// We use 0 as the starting state
		long startState = 0;
		startState = tb.reduce(startState);

		// We create a search object
		TBSearch search = new TBSearch(tb);
		
		// Run the Breadth-First-Search for three times the number of states
		search.runBFS(3*k*k);
		//search.doBestFirst(1500000, 10000);
		for (String state : search.getBestPath())
		{
			System.out.println(state);
		}
		
		System.out.println("----");
		System.out.println("Number of states with equal score: "+search.getBestCount());
		for (String state : search.getBestStates())
		{
			System.out.println(state);
		}
	}
}
