package nl.eur.ese.pbouman.automorphism;
import java.util.ArrayList;
import java.util.List;

/**
 * Ugly graph class that has both principles of an adjacency matrix and an adjacency list
 * The idea is that it should be especially efficient to compute automorphisms for relatively
 * small graphs. For other applications it is probably better to use a nicer Graph class.
 * @author Paul Bouman
 *
 */
public class Graph {
	
	private int vertices;
	
	private boolean [] arcs;
	private int [] inDegrees;
	private int [] outDegrees;
	public List<Arc> arcList;
	
	/**
	 * Constructor for a graph with a fixed number of vertices
	 * @param n the number of vertices in the graph
	 */
	public Graph(int n)
	{
		this.vertices = n;
		this.arcs = new boolean[n*n];
		this.inDegrees = new int[n];
		this.outDegrees = new int[n];
		this.arcList = new ArrayList<>();
	}
	
	/**
	 * Checks whether to vertices are adjacent in an undirected sense
	 * @param i one endpoint of the edge of which we check existence
	 * @param j antoher endpoint of the edge of which we check existence 
	 * @return whether the two vertices are adjacent in an undirected sense
	 */
	public boolean isAdjacent(int i, int j)
	{
		return isArcPresent(i,j) || isArcPresent(j,i);
	}
	
	/**
	 * Check whether a directed arc occurs in the graph
	 * @param i The source of the arc
	 * @param j The destination of the arc
	 * @return Whether there exists and arc from i to j
	 */
	public boolean isArcPresent(int i, int j)
	{
		checkVertex(i,"i");
		checkVertex(j,"j");
		return arcs[i*vertices+j];	
	}
	
	/**
	 * Adds an undirected edge to the graph
	 * @param i the first endpoint of the edge
	 * @param j the second endpoint of the edge
	 */
	public void addEdge(int i, int j)
	{
		addArc(i,j);
		addArc(j,i);
	}
	
	/**
	 * Add a directed arc to this graph
	 * @param i the label of the source vertex
	 * @param j the label of the destination vertex
	 */
	public void addArc(int i, int j)
	{
		checkVertex(i,"i");
		checkVertex(j,"j");
		int index = i*vertices+j;
		if (!arcs[index])
		{
			outDegrees[i]++;
			inDegrees[j]++;
			arcs[index] = true;
			arcList.add(new Arc(i,j));
		}
	}
	
	/**
	 * Checks whether a candidate mapping is a valid automorphism
	 * @param m The mapping to be checked
	 * @return Whether m is a valid automorphism for this graph or not
	 */
	public boolean checkMapping(Mapping m)
	{
		if (vertices != m.getSize())
		{
			throw new IllegalArgumentException("The mapping is for a different number of vertices");
		}

		for (Arc a : arcList)
		{
			int mapI = m.lookup(a.v);
			int mapJ = m.lookup(a.w);
			
			if (mapI > 0 && mapJ > 0)
			{
				if (!isAdjacent(mapI,mapJ))
				{
					// If two adjacent vertices are mapped in the mapping, and they are
					// not adjacent in the mapped variant, this is not a valid automorphism.
					return false;
				}
			}
		}
		
		return true;
	}

	/**
	 * Computes the neighbours of a vertex with label v
	 * @param v the vertex for which we want to find it's nei
	 * @return A list of neighbour vertices
	 */
	public List<Integer> getNeighbours(int v)
	{
		checkVertex(v,"v");
		List<Integer> result = new ArrayList<>(vertices);
		for (int j=0; j < vertices; j++)
		{
			if (isArcPresent(v,j))
			{
				result.add(j);
			}
		}
		return result;
	}
	
	private void checkVertex(int i, String name)
	{
		if (i < 0 || vertices <= i)
		{
			throw new IllegalArgumentException("Vertex "+name+" is out of bounds");
		}
	}
	
	/**
	 * Static helper function for the generation of a gridgraph
	 * @param n
	 * @return
	 */
	public static Graph gridGraph(int n)
	{
		Graph result = new Graph(n*n);
		
		for (int i=0; i < n; i++)
		{
			for (int j=0; j < n; j++)
			{
				int v = i*n+j;
				
				// Left edge if possible
				if (i>=1)
				{
					int w = (i-1)*n+j;
					result.addEdge(v,w);
				}
				// Right edge if possible 
				if (i<n-1)
				{
					int w = (i+1)*n+j;
					result.addEdge(v, w);
				}
				// Top edge if possible
				if (j>=1)
				{
					int w = i*n+(j-1);
					result.addEdge(v, w);
				}
				// Bottom edge if possible
				if (j<n-1)
				{
					int w = i*n+(j+1);
					result.addEdge(v, w);
				}
			}
		}
		
		return result;
	}
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("Graph with "+vertices+" vertices\n");
		sb.append("--START OF GRAPH--\n");
		for (int i=0; i < vertices; i++)
		{
			for (int j=0; j < vertices; j++)
			{
				if (isArcPresent(i,j) && isArcPresent(j,i))
				{
					if (i < j)
					{
						sb.append(""+i+" -- "+j+"\n");
					}
				}
				else if (isArcPresent(i,j))
				{
					sb.append(""+i+" -> "+j+"\n");
				}
				else if (isArcPresent(j,i))
				{
					sb.append(""+j+" -> "+i+"\n");
				}
			}
		}
		sb.append("--END OF GRAPH--\n");
		return sb.toString();
	}

	/**
	 * Get the outdegree of a vertex
	 * Note: in undirected graph this will be equal to the indegree
	 * @param i
	 * @return
	 */
	public int getInDegree(int i) {
		checkVertex(i, "i");
		return inDegrees[i];
	}
	
	/**
	 * Get the outdegree of a vertex
	 * Note: in undirected graphs, this will be equal to the outdegree
	 * @param i the vertex to get the outdegree of
	 * @return the indegree
	 */
	public int getOutDegree(int i) {
		checkVertex(i, "i");
		return outDegrees[i];
	}
	
	/**
	 * Retrieve the number of vertices.
	 * @return
	 */
	public int size() {
		return vertices;
	}

	/**
	 * Just a pair of int's, modelling an arc in the graph
	 * @author Paul
	 *
	 */
	public final class Arc
	{
		public final int v;
		public final int w;
		
		public Arc(int v, int w)
		{
			this.v = v;
			this.w = w;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + v;
			result = prime * result + w;
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
			Arc other = (Arc) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (v != other.v)
				return false;
			if (w != other.w)
				return false;
			return true;
		}

		private Graph getOuterType() {
			return Graph.this;
		}
	}
	
}
