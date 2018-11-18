package graphTAD;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;

public class Graph<K extends Comparable<K>, V> implements TheGraph<K, V>{

	//-------------------------//
	//-------Parameters--------//
	//-------------------------//

	
	private Hashtable<K, Hashtable<K, Integer>> adjacencyArray;
	// Nodes
	private Hashtable<K, Node<K, V>> nodes;
	// Edges
	private List<Edge<K>> edges;
	// type of graph
	private boolean directed;
	// representation
	private int numberOfEdges;
	
	private Hashtable<K, Hashtable<K, List<K>>> paths;
	private boolean changedPaths;


	//---------------------------//
	//--------Constructor--------//
	//---------------------------//

	public Graph(boolean directed) {
		// initialize attributes
		adjacencyArray = new Hashtable<>();
		nodes = new Hashtable<>();
		edges = new ArrayList<>();
		this.directed = directed;
		numberOfEdges = 0;
	}

	//----------------------------//
	//---------Methods------------//
	//----------------------------//

	@Override
	public void addNode(K key,V value) {
		if (!nodes.contains(key)) {
			nodes.put(key, new Node<K, V>(key, value));
		}
		changedPaths = true;
	}

	@Override
	public void addEdge(K key1, V value1, K key2, V value2) {
		addEdge(key1, value1, key2, value2, 1);
	}

	@Override
	public void addEdge(K key1, V value1, K key2, V value2, int weight) {
		changedPaths = true;
		addNode(key1, value1);
		addNode(key2, value2);
		edges.add(new Edge<K>(key1, key2, weight));
		if (!directed) {	
			edges.add(new Edge<K>(key2, key1, weight));
		}

		// check if the row in the key2 exist
		if (adjacencyArray.get(key1) == null) {
			adjacencyArray.put(key1, new Hashtable<K,Integer>());
		}
		Integer adjacents = adjacencyArray.get(key1).get(key2) != null? adjacencyArray.get(key1).get(key2) + 1 : 0;
		adjacencyArray.get(key1).put(key2,  adjacents);
		if (!directed) {
			if (adjacencyArray.get(key2) == null) {
				adjacencyArray.put(key2, new Hashtable<K,Integer>());
			}
			adjacents = adjacencyArray.get(key2).get(key1) != null? adjacencyArray.get(key2).get(key1) + 1 : 0;
			adjacencyArray.get(key2).put(key1,  adjacents);
		}
		
		numberOfEdges++;
	}

	@Override
	public boolean isConnected() {
		
		boolean connected = true;
		Hashtable<K, Integer> levels = BFS(nodes.keys().nextElement());

		for(K key : Collections.list(nodes.keys())) {
			connected &= levels.containsKey(key);
		}

		return connected;
	}

	@Override
	public List<K> shortesPath(K key1, K key2) {
		if (changedPaths) {
			paths = Floyd_Warshall();
			changedPaths = false;
		}
		return paths.get(key1).get(key2);
	}

	@Override
	public int shortesPathWeight(K key1, K key2) {
		return BellmanFord(key1, key2);
	}

	@Override
	public void removeEdge(K from, K to) {
		for (int i = 0; i < edges.size(); i++) {
			if (edges.get(i).getFrom().equals(from) && edges.get(i).getTo().equals(to)) {
				edges.remove(i);
				i--;
			}
			if (!directed && edges.get(i).getTo().equals(from) && edges.get(i).getFrom().equals(to)) {
				edges.remove(i);
				i--;
			}
		}
	}
	
	@Override
	public void removeNode(K key) {
		nodes.remove(key);
	}
	
	@Override
	public TheGraph<K, V> MST() {
		TheGraph<K, V> minimun = new Graph<>(false);
		Collections.sort(edges);
		DisjointSet<K> dis = new DisjointSet<>(); 
		for (int i = 0; i < edges.size(); i++) {
			K key1 = edges.get(i).getFrom();
			K key2 = edges.get(i).getTo();
			dis.makeGroup(key1);
			dis.makeGroup(key2);
			if (!dis.representant(key1).equals(dis.representant(key2))) {
				dis.union(key1, key2);
				minimun.addEdge(key1, nodes.get(key1).getValue(), key2, nodes.get(key2).getValue(), edges.get(i).getWeight());
			}
		}
		return minimun;
	}

	//---------------------------------------------------------//
	//------------------Private Methods------------------------//
	//---------------------------------------------------------//
	private Hashtable<K, Integer> BFS(K root) {

		Hashtable<K, Integer> levels = new Hashtable<>();

		Queue<K> queue = new LinkedList<>();
		queue.add(root);
		levels.put(root, 1);
		while(queue.peek() != null){
			K actual = queue.poll();
			ArrayList<K> list = Collections.list(adjacencyArray.get(actual).keys());
			for(int i = 0; i < list.size(); i++){
				if(adjacencyArray.get(actual).get(list.get(i)) != 0 && !levels.contains(list.get(i))){
					queue.add(list.get(i));
					levels.put(list.get(i),levels.get(list.get(i)) + 1);
				}
			}
		}
		return levels;
	}

	private Hashtable<K, Integer> DFS(K root) {
		Hashtable<K, Integer> levels = new Hashtable<>();

		Stack<K> queue = new Stack<>();
		queue.add(root);
		levels.put(root, 1);
		while(queue.peek() != null){
			K actual = queue.pop();
			ArrayList<K> list = Collections.list(adjacencyArray.get(actual).keys());
			for(int i = 0; i < list.size(); i++){
				if(adjacencyArray.get(actual).get(list.get(i)) != 0 && !levels.contains(list.get(i))){
					queue.add(list.get(i));
					levels.put(list.get(i),levels.get(list.get(i)) + 1);
				}
			}
		}
		return levels;
	}
	
	private int BellmanFord(K root, K objetive) {
		
		List<K> keys = Collections.list(adjacencyArray.keys());
		Hashtable<K, Integer> distance = new Hashtable<>(keys.size());
		for(K key : keys) {
			distance.put(key, Integer.MAX_VALUE);
		}
		distance.put(root, 0);
		
		for (int i = 0; i < keys.size()-1; i++) {
			for (int j = 0; j < edges.size(); j++) {
				if (distance.get(edges.get(j).getFrom()) + edges.get(j).getWeight() < distance.get(edges.get(j).getTo()) ) {
					distance.put(edges.get(j).getTo(), distance.get(edges.get(j).getFrom()) + edges.get(j).getWeight());
				}
			}
		}
		
		return distance.get(objetive);
	}
	
	private Hashtable<K, Hashtable<K, List<K>>> Floyd_Warshall(){
		
		Hashtable<K, Hashtable<K, Integer>> weigthArray = new Hashtable<>(nodes.size());
		Hashtable<K, Hashtable<K, List<K>>> pathArray = new Hashtable<>(nodes.size());
		List<K> keys = Collections.list(adjacencyArray.keys());
		for(K key1 : keys) {
			weigthArray.put(key1, new Hashtable<>(nodes.size()));
			for(K key2 : keys) {
				weigthArray.get(key1).put(key2, key1.equals(key2)? 0 : Integer.MAX_VALUE);
			}
		}
		
		for (int i = 0; i < edges.size(); i++) {
			Edge<K> edge = edges.get(i);
			weigthArray.get(edge.getFrom()).put(edge.getTo(), edge.getWeight());
			List<K> list = new ArrayList<>();
			list.add(edge.getFrom());
			list.add(edge.getTo());
			pathArray.get(edge.getFrom()).put(edge.getTo(), list);
		}
		
		for(K key1 : keys) {
			for(K key2 : keys) {
				for(K key3 : keys) {
					int num1 = weigthArray.get(key2).get(key3);
					int num2 = weigthArray.get(key2).get(key1) + weigthArray.get(key1).get(key3);
					weigthArray.get(key2).put(key3, num1 < num2? num1:num2);
					if (num1 < num2) {
						List<K> l = pathArray.get(key2).get(key1);
						l.remove(l.size()-1);
						l.addAll(pathArray.get(key1).get(key3));
						pathArray.get(key2).put(key3, l);
					}
				}
			}
		}
		
		return pathArray;
	}


}
