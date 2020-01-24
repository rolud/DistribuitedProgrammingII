package it.polito.dp2.BIB.sol3.service;

import java.math.BigInteger;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.ws.rs.core.UriInfo;

import it.polito.dp2.BIB.ass3.TooManyItemsException;
import it.polito.dp2.BIB.sol3.db.BadRequestInOperationException;
import it.polito.dp2.BIB.sol3.db.BookshelfDB;
import it.polito.dp2.BIB.sol3.db.ConflictInOperationException;
import it.polito.dp2.BIB.sol3.db.DB;
import it.polito.dp2.BIB.sol3.db.ItemPage;
import it.polito.dp2.BIB.sol3.db.Neo4jDB;
import it.polito.dp2.BIB.sol3.resources.CounterImpl;
import it.polito.dp2.BIB.sol3.service.jaxb.Bookshelf;
import it.polito.dp2.BIB.sol3.service.jaxb.Bookshelves;
import it.polito.dp2.BIB.sol3.service.jaxb.Citation;
import it.polito.dp2.BIB.sol3.service.jaxb.Item;
import it.polito.dp2.BIB.sol3.service.jaxb.Items;
import it.polito.dp2.BIB.sol3.service.util.ResourseUtils;

public class BiblioService {
	private DB n4jDb = Neo4jDB.getNeo4jDB();
	private BookshelfDB bsDB = BookshelfDB.getBookshelfDB();
	ResourseUtils rutil;

	private CounterImpl counter = CounterImpl.getCounter();

	public BiblioService(UriInfo uriInfo) {
		rutil = new ResourseUtils((uriInfo.getBaseUriBuilder()));
		try {
			ItemPage itemPage = n4jDb.getItems(SearchScope.ALL, "", 10000, 0, BigInteger.valueOf(1));
			counter.initCounter(itemPage.getMap().keySet());	
		} catch (Exception e) {}
	}
	
	public Items getItems(SearchScope scope, String keyword, int beforeInclusive, int afterInclusive, BigInteger page) throws Exception {
		ItemPage itemPage = n4jDb.getItems(scope,keyword,beforeInclusive,afterInclusive,page);

		Items items = new Items();
		List<Item> list = items.getItem();
		
		Set<Entry<BigInteger,Item>> set = itemPage.getMap().entrySet();
		for(Entry<BigInteger,Item> entry:set) {
			Item item = entry.getValue();
			rutil.completeItem(item, entry.getKey());
			list.add(item);
		}
		items.setTotalPages(itemPage.getTotalPages());
		items.setPage(page);
		return items;
	}

	public Item getItem(BigInteger id) throws Exception {
			Item item = n4jDb.getItem(id);
			if (item!=null)
				rutil.completeItem(item, id);
			return item;
	}

	public Item updateItem(BigInteger id, Item item) throws Exception {
		Item ret = n4jDb.updateItem(id, item);
		if (ret!=null) {
			rutil.completeItem(item, id);
			return item;
		} else
			return null;
	}

	public Item createItem(Item item) throws Exception {
		BigInteger id = n4jDb.createItem(item);
		if (id==null)
			throw new Exception("Null id");
		rutil.completeItem(item, id);
		counter.initCounter(id);
		return item;
	}

	public synchronized BigInteger deleteItem(BigInteger id) throws ConflictServiceException, Exception {
		try {
			BigInteger deletedId = n4jDb.deleteItem(id);
			System.out.println("DELETED " + deletedId);
			if (deletedId != null) {
				counter.deleteCounter(id);
				bsDB.deleteItemFromAllBookshelves(id);
			}
			return deletedId;
		} catch (ConflictInOperationException e) {
			throw new ConflictServiceException();
		}
	}

	public Citation createItemCitation(BigInteger id, BigInteger tid, Citation citation) throws Exception {
		try {
			return n4jDb.createItemCitation(id, tid, citation);
		} catch (BadRequestInOperationException e) {
			throw new BadRequestServiceException();
		}
	}

	public Citation getItemCitation(BigInteger id, BigInteger tid) throws Exception {
		Citation citation = n4jDb.getItemCitation(id,tid);
		if (citation!=null)
			rutil.completeCitation(citation, id, tid);
		return citation;
	}

	public boolean deleteItemCitation(BigInteger id, BigInteger tid) throws Exception {
		return n4jDb.deleteItemCitation(id, tid);
	}

	public Items getItemCitations(BigInteger id) throws Exception {
		ItemPage itemPage = n4jDb.getItemCitations(id, BigInteger.ONE);
		if (itemPage==null)
			return null;

		Items items = new Items();
		List<Item> list = items.getItem();
		
		Set<Entry<BigInteger,Item>> set = itemPage.getMap().entrySet();
		for(Entry<BigInteger,Item> entry:set) {
			Item item = entry.getValue();
			rutil.completeItem(item, entry.getKey());
			list.add(item);
		}
		items.setTotalPages(itemPage.getTotalPages());
		items.setPage(BigInteger.ONE);
		return items;
	}

	public Items getItemCitedBy(BigInteger id) throws Exception {
		ItemPage itemPage = n4jDb.getItemCitedBy(id, BigInteger.ONE);
		if (itemPage==null)
			return null;

		Items items = new Items();
		List<Item> list = items.getItem();
		
		Set<Entry<BigInteger,Item>> set = itemPage.getMap().entrySet();
		for(Entry<BigInteger,Item> entry:set) {
			Item item = entry.getValue();
			rutil.completeItem(item, entry.getKey());
			list.add(item);
		}
		items.setTotalPages(itemPage.getTotalPages());
		items.setPage(BigInteger.ONE);
		return items;
	}
	
	public Bookshelf createBookshelf(Bookshelf bookshelf) throws Exception {
		long id = BookshelfDB.getNextId();
		Bookshelf b = bsDB.createBookshelf(id, bookshelf);
		if (b == null)
			throw new Exception("null id");
		rutil.completeBookshelf(bookshelf, BigInteger.valueOf(id));
		return bookshelf;
	}
	
	public Bookshelves getBookshelves(String keyword) throws Exception {
		Bookshelves bookshelves = new Bookshelves();
		List<Bookshelf> list = bookshelves.getBookshelf();
		for (Entry<Long, Bookshelf> entry : bsDB.getBookshelves(keyword).entrySet()) {
			Bookshelf bookshelf = entry.getValue();
			rutil.completeBookshelf(bookshelf, BigInteger.valueOf(entry.getKey()));
			list.add(bookshelf);
		}
		return bookshelves;
	}
	
	public Bookshelf getBookshelf(BigInteger id) {
		Bookshelf bookshelf = bsDB.getBookshelf(id);
		if (bookshelf != null)
			rutil.completeBookshelf(bookshelf, id);
		return bookshelf;
	}

	public Bookshelf updateBookshelf(BigInteger id, Bookshelf bookshelf) {
		Bookshelf updateBookshelf = bsDB.updateBookshelf(id, bookshelf);
		if (updateBookshelf != null) {
			rutil.completeBookshelf(updateBookshelf, id);
			return updateBookshelf;
		} else
			return null;
	}
	
	public BigInteger deleteBookshelf(BigInteger id) throws Exception {
		return bsDB.deleteBookshelf(id);	
	}
	
	public Items getBookshelfItems(BigInteger id) throws Exception {
		Items items = new Items();
		List<Item> list = items.getItem();
		if (bsDB.getBookshelfItems(id) == null) return null;
		Set<Entry<Long, Item>> set = bsDB.getBookshelfItems(id).entrySet();
		if (set == null) return null;
		for (Entry<Long, Item> entry : set) {
			Item item = entry.getValue();
			rutil.completeItem(item, BigInteger.valueOf(entry.getKey()));
			list.add(item);
		}
		return items;
	}
	
	public Item getItemFromBookshelf(BigInteger bookshelfId, BigInteger itemId) throws Exception {
		
		Item item = bsDB.getItemFromBookshelf(bookshelfId, itemId);
		if (item != null)
			rutil.completeItem(item, itemId);
		return item;
	}
	
	public Item addItemToBookshelf(BigInteger bookshelfId, BigInteger itemId) throws Exception {
		Item item = n4jDb.getItem(itemId);
		Bookshelf bookshelf = bsDB.getBookshelf(bookshelfId);
		System.out.println("--- FROM SERVICE --- BOOKSHELF " + bookshelfId + " ITEM " + itemId);
		System.out.println("     ITEM STATUS " + item);
		System.out.println("BOOKSHELF STATUS " + bookshelf);
		if (item == null || bookshelf == null) return null;
		Item ret = bsDB.addItemToBookshelf(bookshelfId, itemId, item);
		if (ret == null) 
			throw new TooManyItemsException();
		rutil.completeItem(ret, itemId);
		return ret;
	}
	
	public Item deleteItemFromBookshelf(BigInteger bookshelfId, BigInteger itemId) throws Exception {
		return bsDB.deleteItemFromBookshelf(bookshelfId, itemId);
	}
	

	public int getCounterTotValue() {
		return counter.getCounterTotValue();
	}
	
	public int getCounterValue(BigInteger id) {
		return counter.getCounterValue(id);
	}
	
	public Object getSyncObject() {
		return BookshelfDB.getMap();
	}
}
