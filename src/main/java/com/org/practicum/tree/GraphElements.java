package com.org.practicum.tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.org.practicum.neo4j.ElementBean;
import com.org.practicum.neo4j.SourceTarget;
// The class represents the data structure to reflect the neighbor object of a vertex
class Neighbor {
	public Vertex vertexBean;
	public Neighbor next;

	public Neighbor(Vertex vertexBean, Neighbor nbr) {
		this.vertexBean = vertexBean;
		this.next = nbr;
	}

	public Neighbor() {
		super();
	}

	public Vertex getVertexBean() {
		return vertexBean;
	}

	public void setVertexBean(Vertex vertexBean) {
		this.vertexBean = vertexBean;
	}

	public Neighbor getNext() {
		return next;
	}

	public void setNext(Neighbor next) {
		this.next = next;
	}

}

//The class represents the data structure to reflect the vertex of a graph
class Vertex {
	ElementBean element;
	String id;
	Neighbor adjList = new Neighbor();
	int listIndex;

	Vertex(ElementBean element, String id, int listIndex) {
		this.element = element;
		this.id = id;
		this.listIndex = listIndex;
	}

	public Vertex() {
		// TODO Auto-generated constructor stub
	}

	public ElementBean getElement() {
		return element;
	}

	public void setElement(ElementBean element) {
		this.element = element;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Neighbor getAdjList() {
		return adjList;
	}

	public void setAdjList(Neighbor adjList) {
		this.adjList = adjList;
	}

	public int getListIndex() {
		return listIndex;
	}

	public void setListIndex(int listIndex) {
		this.listIndex = listIndex;
	}

}
//Class for creating and traversing the graph
public class GraphElements {
	List<Vertex> vertices = new ArrayList<Vertex>();
	HashMap<String, Integer> idMapper = new HashMap<String, Integer>();
	public GraphElements(List<ElementBean> elementBeans) {
		for (int i = 0; i < elementBeans.size(); i++) {
			Vertex vertex = new Vertex(elementBeans.get(i), elementBeans.get(i).getId(), i);
			vertices.add(vertex);
			idMapper.put(elementBeans.get(i).getId(), vertices.size() - 1);
		}
	}
/**
 * Adding a vertex to the graph
 * @param newElement
 * @param graphfinal
 * @return
 */
	public GraphElements addVertex(ElementBean newElement, GraphElements graphfinal) {
		if (!graphfinal.getIdMapper().containsKey(newElement.getId())) {
			Vertex newVertex = new Vertex(newElement, newElement.getId(), graphfinal.getVertices().size());
			List<Vertex> newVertices = new ArrayList<>();
			newVertices.addAll(graphfinal.getVertices());
			newVertices.add(newVertex);
			if (!graphfinal.getVertices().isEmpty())
				graphfinal.getIdMapper().put(newElement.getId(), graphfinal.getVertices().size());
			else
				graphfinal.getIdMapper().put(newElement.getId(), 0);
			graphfinal.setVertices(newVertices);
		}

		return graphfinal;
	}

	/**
	 * adds an edge between the vertices provided and the direction given. 
	 * Vertices would be created if they don't exist
	 * @param parent
	 * @param child
	 * @param outgoing
	 * @param graphFinal
	 */
	public void addEdge(ElementBean parent, ElementBean child, boolean outgoing, GraphElements graphFinal) {
		Vertex newVertex = new Vertex();
		String idmapping;
		if (parent.getId().equalsIgnoreCase(child.getId())) {
			return;
		}
		if (outgoing) {
			if (!(graphFinal.getIdMapper().containsKey(child.getId())))

				newVertex = new Vertex(child, child.getId(), graphFinal.getIdMapper().get(child.getId()));
			else
				newVertex = findVertexbyId(child.getId(), graphFinal);
			idmapping = parent.getId();
			Vertex v = findVertexbyId(idmapping, graphFinal);
			v.getAdjList().setNext(new Neighbor(newVertex, null));
			v.getElement().setChild(newVertex.getElement());
		} else {
			if (!(graphFinal.getIdMapper().containsKey(parent.getId()))) {
				newVertex = new Vertex(parent, parent.getId(), graphFinal.getIdMapper().get(parent.getId()));
			} else {
				int locationParent = graphFinal.getIdMapper().get(parent.getId());
				newVertex = graphFinal.getVertices().get(graphFinal.getIdMapper().get(parent.getId()));
			}

			idmapping = child.getId();
			Vertex v = findVertexbyId(idmapping, graphFinal);
			newVertex.getAdjList().setNext(new Neighbor(v, null));
			newVertex.getElement().setChild(v.getElement());
		}
	}

	public GraphElements() {
		// super();
		vertices = new ArrayList<>();
		idMapper = new HashMap<>();
	}

	/**
	 * returns a list of vertices of the graph
	 * @return
	 */
	public List<Vertex> getVertices() {
		return vertices;
	}

	/**
	 * returns a set of element beans which are stored in the graph (in vertices of the graph)
	 * @return
	 */
	public Set<ElementBean> getVerticesElement() {
		Set<ElementBean> elements = new HashSet<>();
		for (Vertex v : vertices)
			elements.add(v.getElement());
		return elements;
	}

	public void setVertices(List<Vertex> vertices) {
		this.vertices = vertices;
	}

	public HashMap<String, Integer> getIdMapper() {
		return idMapper;
	}

	public void setIdMapper(HashMap<String, Integer> idMapper) {
		this.idMapper = idMapper;
	}

	/**
	 * returns the element in the vertex based on the element id and the graph provided
	 * @param id
	 * @param graphFinal
	 * @return
	 */
	public ElementBean findElementbyId(String id, GraphElements graphFinal) {
		return findVertexbyId(id, graphFinal).getElement();
	}
 
	/**
	 *  returns the vertex based on the element id and the graph provided
	 * @param id
	 * @param graphFinal
	 * @return
	 */
	public Vertex findVertexbyId(String id, GraphElements graphFinal) {
			int location = graphFinal.getIdMapper().get(id);
		Vertex vertex = graphFinal.getVertices().get(location);
		return vertex;
	}
/**
 * Graph traversal with limiting the node number based on exposure amount and number parameter from request 
 * @param graph
 * @param visited
 * @param index
 * @param finalSourceTargetMap
 * @param number
 * @param minExposure
 * @param maxExposure
 * @param queryId
 * @return
 */
	private Map<String, List<ElementBean>> traverseGraph(GraphElements graph, boolean[] visited, int index,
			Map<String, List<ElementBean>> finalSourceTargetMap, int number, String minExposure, String maxExposure,
			String queryId) {
		List<ElementBean> targetChildren = new ArrayList<>();
		ExposureAmountComparator comparator = new ExposureAmountComparator();
		Map<String, ElementBean> map = graph.getVertices().get(index).getElement().getAllChildren();
		double minExposureRange = 0;
		double maxExposureRange = 0;
		if (minExposure != null && minExposure.trim().length() > 0 && !minExposure.equalsIgnoreCase("null")) {
			minExposureRange = Double.parseDouble(minExposure);
		}

		if (maxExposure != null && maxExposure.length() > 0 && !maxExposure.equalsIgnoreCase("null")) {
			maxExposureRange = Double.parseDouble(maxExposure);
		}
		if (map != null && map.size() > 0) {
			ElementBean queryElement = new ElementBean();
			if (map.containsKey(queryId)) {
				queryElement = map.get(queryId);

			} else {
				if (queryId != null && graph.getIdMapper().get(queryId) != null)
					queryElement = graph.getVertices().get(graph.getIdMapper().get(queryId)).getElement();
				else
					queryElement = graph.getVertices().get(index).getElement();
			}

			String queryExposureString = queryElement.getExposureAmount();

			if (queryExposureString == null || queryExposureString.trim().length() == 0) {
				if (minExposureRange > 0) {
					visited[index] = true;
					return finalSourceTargetMap;
				}
			} else {
				Double queryExposureDouble = Double.parseDouble(queryExposureString);
				if (Math.abs(queryExposureDouble) < minExposureRange) {
					visited[index] = true;
					return finalSourceTargetMap;
				}
				if (maxExposureRange > 0 && Math.abs(queryExposureDouble) > maxExposureRange) {
					visited[index] = true;
					return finalSourceTargetMap;
				}
			}

			Set<String> idChildren = map.keySet();
			for (String id : idChildren) {
				targetChildren.add(map.get(id));
			}
			Collections.sort(targetChildren, comparator);

			List<ElementBean> target = new ArrayList<>();
			Set<String> keySetFinalSourceTargetMap = finalSourceTargetMap.keySet();

			if (number == 0) {
				number = targetChildren.size();
			}

			for (int i = 0; i < number && i < targetChildren.size(); i++) {

				ElementBean targetChildrenElement = targetChildren.get(i);
				String targetChildrenElementExposureString = targetChildrenElement.getExposureAmount();

				if (targetChildrenElementExposureString == null
						|| targetChildrenElementExposureString.trim().length() == 0) {
					if (minExposureRange > 0)
						continue;
				} else {
					Double targetChildrenExposureDouble = Double.parseDouble(targetChildrenElementExposureString);
					if (Math.abs(targetChildrenExposureDouble) < minExposureRange)
						continue;
					if (maxExposureRange > 0 && Math.abs(targetChildrenExposureDouble) > maxExposureRange)
						continue;
				}

				target.add(targetChildren.get(i));
				for (String key : keySetFinalSourceTargetMap) {
					Map<String, ElementBean> mapKeyChidren = graph.findElementbyId(key, graph).getAllChildren();
					if (mapKeyChidren != null && mapKeyChidren.size() > 0) {
						if (mapKeyChidren.containsKey(targetChildren.get(i).getId())) {
							finalSourceTargetMap.get(key).add(targetChildren.get(i));
						}
					}
				}

			}
			if (targetChildren.size() > number) {
				for (int i = number; i < targetChildren.size(); i++) {

					int location = graph.getIdMapper().get(targetChildren.get(i).getId());
					visited[location] = true;
				}
			}
			visited[index] = true;
			List<ElementBean> oldTargets = finalSourceTargetMap.get(graph.getVertices().get(index).getId());
			if (oldTargets != null) {
				oldTargets.addAll(target);
			} else {
				oldTargets = new ArrayList<>();
				oldTargets.addAll(target);
			}
			finalSourceTargetMap.put(graph.getVertices().get(index).getId(), oldTargets);

		} else {
			List<ElementBean> target = new ArrayList<>();
			ElementBean element = graph.getVertices().get(index).getElement();
			if (element != null && !element.getType().equalsIgnoreCase("Account")) {
				String exposureAmount = element.getExposureAmount();
				Double exposureAmountdouble = 0.0;
				if (exposureAmount != null && exposureAmount.trim().length() > 0) {
					exposureAmountdouble = Double.parseDouble(exposureAmount);
				}

				Double minExposureDouble = 0.0;
				Double maxExposureDouble = 0.0;

				if (minExposure != null && minExposure.trim().length() > 0 && !minExposure.equalsIgnoreCase("null"))
					minExposureDouble = Double.parseDouble(minExposure);
				if (maxExposure != null && maxExposure.trim().length() > 0 && !maxExposure.equalsIgnoreCase("null"))
					maxExposureDouble = Double.parseDouble(maxExposure);
				if (minExposureDouble <= Math.abs(exposureAmountdouble)
						&& maxExposureDouble >= Math.abs(exposureAmountdouble)) {
					target.add(element);
				}

				finalSourceTargetMap.put(graph.getVertices().get(index).getId(), target);
			}
		}
		return finalSourceTargetMap;

	}
/**
 * graph traversal recursive function
 * @param graph
 * @param visited
 * @param initialIndex
 * @param finalSourceTargetMap
 * @param number
 * @param minExposure
 * @param maxExposure
 * @param id
 * @return
 */
	public Map<String, List<ElementBean>> traverseGraphMain(GraphElements graph, boolean[] visited, int initialIndex,
			Map<String, List<ElementBean>> finalSourceTargetMap, int number, String minExposure, String maxExposure,
			String id) {
		for (int v = initialIndex; v < visited.length; v++) { // changed v=0 tto
			if (!visited[v]) {
				finalSourceTargetMap = traverseGraph(graph, visited, v, finalSourceTargetMap, number, minExposure,
						maxExposure, id);
			}
		}

		return finalSourceTargetMap;

	}
}
