package it.polito.dp2.BIB.sol3.client;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import it.polito.dp2.BIB.ass3.Bookshelf;
import it.polito.dp2.BIB.ass3.Client;
import it.polito.dp2.BIB.ass3.DestroyedBookshelfException;
import it.polito.dp2.BIB.ass3.ItemReader;
import it.polito.dp2.BIB.ass3.ServiceException;
import it.polito.dp2.BIB.ass3.TooManyItemsException;
import it.polito.dp2.BIB.ass3.UnknownItemException;

public class ClientFactoryImpl implements Client {
	javax.ws.rs.client.Client client;
	WebTarget target;
	
	ObjectFactory objFactory;
	
	static String uri = "http://localhost:8080/BiblioSystem/rest";
	static String urlProperty = "it.polito.dp2.BIB.ass3.URL";
	static String portProperty = "it.polito.dp2.BIB.ass3.PORT";
	
	
	public ClientFactoryImpl(URI uri) {
		this.uri = uri.toString();
		
		client = ClientBuilder.newClient();
		target = client.target(uri).path("biblio");
		objFactory = new ObjectFactory();
	}
	

	@Override
	public Bookshelf createBookshelf(String name) throws ServiceException {
		it.polito.dp2.BIB.sol3.client.Bookshelf bodyBookshelf = objFactory.createBookshelf();
		bodyBookshelf.setName(name);
		it.polito.dp2.BIB.sol3.client.Bookshelves.Bookshelf retBookshelf = target.path("/bookshelves")
				.request(MediaType.APPLICATION_JSON_TYPE)
				.post(Entity.json(bodyBookshelf), it.polito.dp2.BIB.sol3.client.Bookshelves.Bookshelf.class);
		return new BookshelfImpl(retBookshelf, this);
	}

	@Override
	public Set<Bookshelf> getBookshelfs(String name) throws ServiceException {
		Set<Bookshelf> bookshelvesSet = new HashSet<>();
		Bookshelves bookshelves = target.path("/bookshelves")
				.queryParam("keyword", name)
				.request(MediaType.APPLICATION_JSON_TYPE)
				.get(Bookshelves.class);
		
		
		for (it.polito.dp2.BIB.sol3.client.Bookshelves.Bookshelf b : bookshelves.getBookshelf()) {
			bookshelvesSet.add(new BookshelfImpl(b, this));
		}
		return bookshelvesSet;
	}

	@Override
	public Set<ItemReader> getItems(String keyword, int since, int to) throws ServiceException {
		Set<ItemReader> itemSet=new HashSet<>();
		Items items = target.path("/items")
				.queryParam("keyword", keyword)
				.queryParam("beforeInclusive", to)
				.queryParam("afterInclusive", since)
			 	  .request(MediaType.APPLICATION_JSON_TYPE)
			 	  .get( Items.class);
		
		for (it.polito.dp2.BIB.sol3.client.Items.Item i : items.getItem()) {
			itemSet.add(new ItemReaderImpl(i));
		}
		
		return itemSet;
	}
	
	public Bookshelf getBookshelf(Integer id) throws ServiceException {
		it.polito.dp2.BIB.sol3.client.Bookshelves.Bookshelf bookshelf = target.path("/bookshelves/{id}")
				.resolveTemplate("id", id)
				.request(MediaType.APPLICATION_JSON_TYPE)
				.get(it.polito.dp2.BIB.sol3.client.Bookshelves.Bookshelf.class);
		BookshelfImpl ret = new BookshelfImpl(bookshelf, this);
		return ret;
	}
	
//	public Bookshelf updateBookshelf(Integer id) {
//		it.polito.dp2.BIB.sol3.client.Bookshelves.Bookshelf bookshelf = target.path("/bookshelves/{id}")
//				.resolveTemplate("id", id)
//				.request(MediaType.APPLICATION_JSON_TYPE)
//				.put(it.polito.dp2.BIB.sol3.client.Bookshelves.Bookshelf.class);
//		BookshelfImpl ret = new BookshelfImpl(bookshelf);
//		return ret;
//	}
	
	public void removeBookshelf(Integer id) throws ServiceException {
		target.path("/bookshelves/{id}")
			.resolveTemplate("id", id)
			.request(MediaType.APPLICATION_JSON_TYPE)
			.delete();
	}
	
	public Set<ItemReader> getBookshelfItems(Integer id) throws ServiceException {
		Set<ItemReader> itemSet = new HashSet<>();
		Items items = target.path("/bookshelves/{id}/items")
				.resolveTemplate("id", id)
				.request(MediaType.APPLICATION_JSON_TYPE)
				.get(Items.class);
		for (it.polito.dp2.BIB.sol3.client.Items.Item i : items.getItem()) {
			itemSet.add(new ItemReaderImpl(i));
		}
		return itemSet;
	}
	
	public ItemReader getItemFromABookshelf(Integer bookshelfId, ItemReader item) throws ServiceException {
		
		Items.Item retItem = target.path("/bookshelves/{bookshelfId}/items/{itemId}")
				.resolveTemplate("bookshelfId", bookshelfId)
				.resolveTemplate("itemId", ((ItemReaderImpl) item).getId())
				.request(MediaType.APPLICATION_JSON_TYPE)
				.get(Items.Item.class);
		return new ItemReaderImpl(retItem);
	}
	
	public ItemReader addItemToBookshelf(Integer bookshelfId, ItemReader item) throws ServiceException {
		
		Items.Item i = objFactory.createItemsItem();
		Items.Item retItem = target.path("/bookshelves/{bookshelfId}/items/{itemId}")
				.resolveTemplate("bookshelfId", bookshelfId)
				.resolveTemplate("itemId", ((ItemReaderImpl) item).getId())
				.request(MediaType.APPLICATION_JSON_TYPE)
				.put(Entity.json(i), Items.Item.class);
		return new ItemReaderImpl(retItem);
	}
	
	public void removeItemFromBookshelf(Integer bookshelfId, ItemReader item) throws ServiceException {
		
		target.path("/bookshelves/{bookshelfId}/items/{itemId}")
				.resolveTemplate("bookshelfId", bookshelfId)
				.resolveTemplate("itemId", ((ItemReaderImpl) item).getId())
				.request(MediaType.APPLICATION_JSON_TYPE)
				.delete();
	}
	
	public int getNumberOfReads(Integer id) throws ServiceException {
		Integer n = target.path("/bookshelves/{bookshelfId}/numberOfReads")
				.resolveTemplate("bookshelfId", id)
				.request(MediaType.TEXT_PLAIN)
				.get(Integer.class);
		return n;
	}
	
	
	
	
	
	private static void printItems() throws ServiceException {
		Set<ItemReader> set = mainClient.getItems("", 0, 3000);
		System.out.println("Items returned: "+set.size());
		
		// For each Item print related data
		for (ItemReader item: set) {
			System.out.println("Title: "+item.getTitle());
			if (item.getSubtitle()!=null)
				System.out.println("Subtitle: "+item.getSubtitle());
			System.out.print("Authors: ");
			String[] authors = item.getAuthors();
			System.out.print(authors[0]);
			for (int i=1; i<authors.length; i++)
				System.out.print(", "+authors[i]);
			System.out.println(";");
			
			Set<ItemReader> citingItems = item.getCitingItems();
			System.out.println("Cited by "+citingItems.size()+" items:");
			for (ItemReader citing: citingItems) {
				System.out.println("- "+citing.getTitle());
			}	
			printLine('-');

		}
		printBlankLine();
	}
	
	


	private static void printBlankLine() {
		System.out.println(" ");
	}

	
	private static void printLine(char c) {
		System.out.println(makeLine(c));
	}
	
	private static StringBuffer makeLine(char c) {
		StringBuffer line = new StringBuffer(132);
		
		for (int i = 0; i < 132; ++i) {
			line.append(c);
		}
		return line;
	}
	
	
	static ClientFactoryImpl mainClient;
	public static void main(String[] args) throws DestroyedBookshelfException, UnknownItemException, TooManyItemsException {
		System.setProperty("it.polito.dp2.BIB.BibReaderFactory", "it.polito.dp2.BIB.Random.BibReaderFactoryImpl");
		String customUri = System.getProperty(urlProperty);
		String customPort = System.getProperty(portProperty);
		if (customUri != null)
			uri = customUri;
		
		try {
			mainClient = new ClientFactoryImpl(new URI(uri));
//			printItems();
			
			Bookshelf b = mainClient.createBookshelf("Scaffale con troppi libri");
			Set<ItemReader> set = mainClient.getItems("", 0, 3000);
			int n = 0;
			for (ItemReader item : set) {
				System.out.println("Adding item " + item.getTitle() + " .... ");
				b.addItem(item);
				System.out.println("Item added, number " + ++n);
			}
		} catch (URISyntaxException | ServiceException e) {
			e.printStackTrace();
		}
		
	}
		
}
