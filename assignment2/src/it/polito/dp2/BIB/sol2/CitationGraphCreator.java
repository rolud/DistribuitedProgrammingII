package it.polito.dp2.BIB.sol2;

import java.util.Set;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;

import javax.ws.rs.core.MediaType;

import it.polito.dp2.BIB.BibReader;
import it.polito.dp2.BIB.BibReaderException;
import it.polito.dp2.BIB.BibReaderFactory;
import it.polito.dp2.BIB.FactoryConfigurationError;
import it.polito.dp2.BIB.ItemReader;
import it.polito.dp2.rest.neo4j.client.jaxb.Data;
import it.polito.dp2.rest.neo4j.client.jaxb.Node;
import it.polito.dp2.rest.neo4j.client.jaxb.ObjectFactory;

/*
 * Created by rolud
 */
public class CitationGraphCreator {

	private String url;
	private String port;
	private Client client;
	private WebTarget target;
	private BibReader monitor;
	private ObjectFactory objFactory;
	
	public CitationGraphCreator() {    
		
		try {
			this.monitor = BibReaderFactory.newInstance().newBibReader();
		} catch (BibReaderException | FactoryConfigurationError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		this.url = System.getProperty("it.polito.dp2.BIB.ass2.URL");
		this.port = System.getProperty("it.polito.dp2.BIB.ass2.PORT");
		this.client = ClientBuilder.newClient();
		this.target = this.client.target(this.url);
		this.objFactory = new ObjectFactory();
		
		
		 
		
	}
	
	public void loadGraph() {
		Set<ItemReader> items = monitor.getItems(null,  0,  9999);
		for (ItemReader item : items) {
			Node node = addNode(item);
			
		}
	}
	
	public void getServiceRoot() {
		String response = target.path("data")
	                 			.request()
	                 			.accept(MediaType.APPLICATION_JSON_TYPE)
	                 			.get(String.class);
		System.out.println("GET SERVICE RESPONSE : " + response);
	}
	
	
	public Node addNode(ItemReader item) {
	
		Data data = objFactory.createData();
		data.setTitle(item.getTitle());

		Node node = target.path("data/node")
								.request()
				     			.accept(MediaType.APPLICATION_JSON_TYPE)
				                .post(Entity.json(data), Node.class);
		System.out.println("POST NODE RESPONSE : " + node.toString());
		return node;
	}
}
