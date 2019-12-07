package it.polito.dp2.BIB.sol2;

import java.math.BigInteger;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;

import it.polito.dp2.BIB.BibReader;
import it.polito.dp2.BIB.BibReaderException;
import it.polito.dp2.BIB.BibReaderFactory;
import it.polito.dp2.BIB.BookReader;
import it.polito.dp2.BIB.ItemReader;
import it.polito.dp2.BIB.JournalReader;
import it.polito.dp2.BIB.ass2.CitationFinder;
import it.polito.dp2.BIB.ass2.CitationFinderException;
import it.polito.dp2.BIB.ass2.ServiceException;
import it.polito.dp2.BIB.ass2.UnknownItemException;
import it.polito.dp2.BIB.sol2.neo4j.client.jaxb.Data;
import it.polito.dp2.BIB.sol2.neo4j.client.jaxb.Node;
import it.polito.dp2.BIB.sol2.neo4j.client.jaxb.Relationship;
import it.polito.dp2.BIB.sol2.neo4j.client.jaxb.RelationshipPayload;
import it.polito.dp2.BIB.sol2.neo4j.client.jaxb.TraversalPayload;
import it.polito.dp2.BIB.sol2.neo4j.client.jaxb.ObjectFactory;

/*
 * Created by rolud
 */
public class CitationFinderImpl implements CitationFinder {

	private String url;
	private String port;
	private Client client;
	private WebTarget target;
	private BibReader monitor;
	private ObjectFactory objFactory;
	
	private Map<ItemReader, Node> itemToNode;
	private Map<String, ItemReader> urlToItem;
	
	public CitationFinderImpl() throws CitationFinderException {      
		try {
			this.monitor = BibReaderFactory.newInstance().newBibReader();
			this.url = System.getProperty("it.polito.dp2.BIB.ass2.URL");
			this.port = System.getProperty("it.polito.dp2.BIB.ass2.PORT");
			this.client = ClientBuilder.newClient();
			this.target = this.client.target(this.url);
			this.objFactory = new ObjectFactory();
			
			this.itemToNode = new HashMap<>();
			this.urlToItem = new HashMap<>();
			
			loadGraph();
		} catch (BibReaderException | MalformedURLException e) {
			throw new CitationFinderException(e);
		}
		
		
		
		
		
	}
	
	private void loadGraph() throws MalformedURLException {
		Set<ItemReader> items = monitor.getItems(null,  0,  9999);
		for (ItemReader item : items) {
			Node node = createNode(item);
			itemToNode.put(item, node);
			urlToItem.put(node.getSelf(),  item);
			
		}
		
		for (ItemReader item : items) {
			for (ItemReader citing : item.getCitingItems()) {
				Node nodeStart = itemToNode.get(item);
				Node nodeEnd = itemToNode.get(citing);
				createRelationship(nodeStart, nodeEnd);
			}
		}
	}
	
	private Node createNode(ItemReader item) {
		
		Data data = objFactory.createData();
		data.setTitle(item.getTitle());

		Node node = target.path("data/node")
					      .request()
				    	  .accept(MediaType.APPLICATION_JSON_TYPE)
				          .post(Entity.json(data), Node.class);
		System.out.println("Created node " + node.getSelf());
		return node;
	}
	
	private void createRelationship(Node nodeStart, Node nodeEnd) {
		RelationshipPayload data = objFactory.createRelationshipPayload();
		data.setTo(nodeEnd.getSelf());
		data.setType("CitedBy");
		
		
		Relationship relationship = target.path("data/node/{nodeID}/relationships")
						                  .resolveTemplate("nodeID", nodeStart.getMetadata().getId().intValue())
										  .request()
								          .accept(MediaType.APPLICATION_JSON_TYPE)
								          .post(Entity.json(data), Relationship.class);


		System.out.println("Created relationship " + relationship.getSelf());
	}
	
	private List<Node> getCitingElements(Node nodeStart, int maxDepth) {
		
		TraversalPayload.Relationships rel = objFactory.createTraversalPayloadRelationships();
		rel.setDirection("out");
		rel.setType("CitedBy");
		
		TraversalPayload payload = objFactory.createTraversalPayload();
		payload.setOrder("breadth_first");
		payload.getRelationships().add(rel);
		payload.setMaxDepth(BigInteger.valueOf(maxDepth));
			
		List<Node> nodes = target.path("data/node/{nodeID}/traverse/node")
								 .resolveTemplate("nodeID", nodeStart.getMetadata().getId().intValue())
								 .request()
								 .accept(MediaType.APPLICATION_JSON_TYPE).post(Entity.json(payload), new GenericType<List<Node>>() {});
		return nodes;
	}
	
	@Override
	public BookReader getBook(String arg0) {
		return monitor.getBook(arg0);
	}

	@Override
	public Set<ItemReader> getItems(String arg0, int arg1, int arg2) {
		return monitor.getItems(arg0, arg1, arg2);
	}

	@Override
	public JournalReader getJournal(String arg0) {
		return monitor.getJournal(arg0);
	}

	@Override
	public Set<JournalReader> getJournals(String arg0) {
		return getJournals(arg0);
	}

	@Override
	public Set<ItemReader> findAllCitingItems(ItemReader item, int maxDepth)
			throws UnknownItemException, ServiceException {
		
		if (!itemToNode.containsKey(item))
			throw new UnknownItemException("Item " + item.getTitle() + " unknown");
		Node nodeStart = itemToNode.get(item);
		
		List<Node> nodes = getCitingElements(nodeStart, maxDepth);
	
		Set<ItemReader> items = new HashSet<>();
		
		for (Node n : nodes) {
			items.add(urlToItem.get(n.getSelf()));
		}
		return items;
	}

}
