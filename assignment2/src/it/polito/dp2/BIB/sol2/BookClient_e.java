package it.polito.dp2.BIB.sol2;

import static javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.util.JAXBSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import it.polito.dp2.BIB.sol2.crossref.client.jaxb.CrossrefItems;
import it.polito.dp2.BIB.sol2.crossref.client.jaxb.CrossrefSearchResult;
import it.polito.dp2.rest.gbooks.client.Factory;
import it.polito.dp2.rest.gbooks.client.MyErrorHandler;
import it.polito.dp2.rest.gbooks.client.jaxb.Items;
import it.polito.dp2.rest.gbooks.client.jaxb.SearchResult;
import it.polito.dp2.xml.biblio.PrintableItem;

public class BookClient_e {

	JAXBContext jcGoogle;
	JAXBContext jcCrossref;
	javax.xml.validation.Validator validatorGoogle;
	javax.xml.validation.Validator validatorCrossref;
	
	public static void main(String[] args) {
		if (args.length <= 1) {
			System.err.println("Usage: java BookClient_e N keyword1 keyword2 ...");
	        System.exit(1);
	    }
		
		int n = -1;
		try {
			n = Integer.parseInt(args[0]);
		} catch (NumberFormatException e) {
			System.err.println("Usage: java BookClient_e N keyword1 keyword2 ...");
			System.err.println("Usage: N should be a number");
	        System.exit(1);
		}
		if (n < 0) {
			System.err.println("Usage: java BookClient_e N keyword1 keyword2 ...");
		    System.err.println("Usage: N should be a non negative number");
	        System.exit(1);
		}
		String[] kw = Arrays.copyOfRange(args, 1, args.length);
		try{
			BookClient_e bclient = new BookClient_e();
			bclient.PerformSearch(n, kw);
		}catch(Exception ex ){
			System.err.println("Error during execution of operation");
			ex.printStackTrace(System.out);
		}
	}
	
	public BookClient_e() throws Exception {        
    	// create validator that uses the DataTypes schema
    	SchemaFactory sf = SchemaFactory.newInstance(W3C_XML_SCHEMA_NS_URI);
    	Schema schemaGoogle = sf.newSchema(new File("xsd/gbooks/DataTypes.xsd"));
    	Schema schemaCrossref = sf.newSchema(new File("xsd/crossref/DataTypes.xsd"));
    	validatorGoogle = schemaGoogle.newValidator();
    	validatorCrossref = schemaCrossref.newValidator();
    	validatorGoogle.setErrorHandler(new MyErrorHandler());
    	validatorCrossref.setErrorHandler(new MyErrorHandler());
    	
		// create JAXB context related to the classed generated from the DataTypes schema
        jcGoogle = JAXBContext.newInstance("it.polito.dp2.rest.gbooks.client.jaxb");
        jcCrossref = JAXBContext.newInstance("it.polito.dp2.BIB.sol2.crossref.client.jaxb");
	}
	
	public void PerformSearch(int n, String[] kw){
		
		System.out.println("  ____   ___    ___    ____  _        ___\n"+ 
" /    | /   \\  /   \\  /    || |      /  _]\n"+
"|   __||     ||     ||   __|| |     /  [_ \n"+
"|  |  ||  O  ||  O  ||  |  || |___ |    _]\n"+
"|  |_ ||     ||     ||  |_ ||     ||   [_ \n"+
"|     ||     ||     ||     ||     ||     | \n"+	
"|___,_| \\___/  \\___/ |___,_||_____||_____|\n"+
"                                          ");
		searchGoogleBooks(n, kw);
		
		
		System.out.println("    __  ____   ___   _____ _____ ____     ___  _____ \n"+
"   /  ]|    \\ /   \\ / ___// ___/|    \\   /  _]|     |\n"+
"  /  / |  D  )     (   \\_(   \\_ |  D  ) /  [_ |   __|\n"+
" /  /  |    /|  O  |\\__  |\\__  ||    / |    _]|  |_  \n"+
"/   \\_ |    \\|     |/  \\ |/  \\ ||    \\ |   [_ |   _] \n"+
"\\     ||  .  \\     |\\    |\\    ||  .  \\|     ||  |   \n"+
" \\____||__|\\_|\\___/  \\___| \\___||__|\\_||_____||__|   \n"+
"                                                     ");
	
		searchCrossref(n, kw);
		
	}
	
	private void searchCrossref(int n, String[] kw) {
		// build the JAX-RS client object 
		Client clientCrossref = ClientBuilder.newClient();
		
		// build the web target
		WebTarget targetCrossref = clientCrossref.target(getCrossrefBaseURI());
		
		StringBuffer queryString = new StringBuffer(kw[0]);
		for (int i = 1; i < kw.length; i++) {
			queryString.append(' ');
			queryString.append(kw[i]);
		}
		System.out.println("Searching "+queryString+" on Crossref:");
		Response response = targetCrossref
								.queryParam("query", queryString)
								.queryParam("filter", "type:book")
								//.queryParam("rows", n)
								.request()
								.accept(MediaType.APPLICATION_JSON)
								.get();
		if (response.getStatus() != 200) {
			System.out.println("Error in remote operation: "+response.getStatus()+" "+response.getStatusInfo());
			return;
		}
		response.bufferEntity();
//		System.out.println("Response as string: "+response.readEntity(String.class));
		CrossrefSearchResult result = response.readEntity(CrossrefSearchResult.class);
		
		System.out.println("OK Response received. Items:"+result.getMessage().getTotalResults());
		System.out.println("SIZE : "+result.getMessage().getItems().size());
		System.out.println("Validating items and converting validated items to xml.");
		
		List<PrintableItem> pitems = new ArrayList<PrintableItem>();
		int i=0;
		for (it.polito.dp2.BIB.sol2.crossref.client.jaxb.Items item:result.getMessage().getItems()) {
			if (i == n) break;
			try {
				// validate item
		    	JAXBSource source = new JAXBSource(jcCrossref, item);
		    	System.out.println("Validating "+item.getTitle());
		    	validatorCrossref.validate(source);
		    	System.out.println("Validation OK");
		    	// add item to list
				System.out.println("Adding item to list");
				pitems.add(CrossrefFactory.createPrintableItem(BigInteger.valueOf(i++),item));
			} catch (org.xml.sax.SAXException se) {
			      System.out.println("Validation Failed");
			      // print error messages
			      Throwable t = se;
			      while (t!=null) {
				      String message = t.getMessage();
				      if (message!= null)
				    	  System.out.println(message);
				      t = t.getCause();
			      }
			} catch (IOException e) {
				System.out.println("Unexpected I/O Exception");
			} catch (JAXBException e) {
				System.out.println("Unexpected JAXB Exception");
			}
		}
	    System.out.println("Validated Bibliography items: "+pitems.size());
	    for (PrintableItem item:pitems)
	    	item.print();
	    System.out.println("End of Validated Bibliography items");
	}
	
	private void searchGoogleBooks(int n, String[] kw) {
		// build the JAX-RS client object
		Client clientGoogle = ClientBuilder.newClient();
				
		// build the web target
		WebTarget targetGoogle = clientGoogle.target(getGoogleBaseURI()).path("volumes");
		
		
		// perform a get request using mediaType=APPLICATION_JSON
		// and convert the response into a SearchResult object
		StringBuffer queryString = new StringBuffer(kw[0]);
		for (int i=1; i<kw.length; i++) {
			queryString.append(' ');
			queryString.append(kw[i]);
		}
		System.out.println("Searching "+queryString+" on Google Books:");
		Response response = targetGoogle
							   .queryParam("q", queryString)
							   .queryParam("printType", "books")
							   .request()
							   .accept(MediaType.APPLICATION_JSON)
							   .get();
		if (response.getStatus()!=200) {
			System.out.println("Error in remote operation: "+response.getStatus()+" "+response.getStatusInfo());
			return;
		}
		response.bufferEntity();
	//				System.out.println("Response as string: "+response.readEntity(String.class));
		SearchResult result = response.readEntity(SearchResult.class);
		
		System.out.println("OK Response received. Items:"+result.getTotalItems());
		
		System.out.println("Validating items and converting validated items to xml.");
		// create empty list
		List<PrintableItem> pitems = new ArrayList<PrintableItem>();
		int i=0;
		for (Items item:result.getItems()) {
			if (i == n) break;
			try {
				// validate item
		    	JAXBSource source = new JAXBSource(jcGoogle, item);
		    	System.out.println("Validating "+item.getSelfLink());
		    	validatorGoogle.validate(source);
		    	System.out.println("Validation OK");
		    	// add item to list
				System.out.println("Adding item to list");
				pitems.add(Factory.createPrintableItem(BigInteger.valueOf(i++),item.getVolumeInfo()));
			} catch (org.xml.sax.SAXException se) {
			      System.out.println("Validation Failed");
			      // print error messages
			      Throwable t = se;
			      while (t!=null) {
				      String message = t.getMessage();
				      if (message!= null)
				    	  System.out.println(message);
				      t = t.getCause();
			      }
			} catch (IOException e) {
				System.out.println("Unexpected I/O Exception");
			} catch (JAXBException e) {
				System.out.println("Unexpected JAXB Exception");
			}
		}
	    System.out.println("Validated Bibliography items: "+pitems.size());
	    for (PrintableItem item:pitems)
	    	item.print();
	    System.out.println("End of Validated Bibliography items");
	}
	
	private static URI getGoogleBaseURI() {
	    return UriBuilder.fromUri("https://www.googleapis.com/books/v1").build();
	}
	
	private static URI getCrossrefBaseURI(){
		return UriBuilder.fromUri("https://api.crossref.org/works").build();
	}
}
