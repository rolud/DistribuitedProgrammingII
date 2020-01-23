package it.polito.dp2.BIB.sol3.db;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.NavigableSet;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

import it.polito.dp2.BIB.sol3.service.jaxb.Bookshelf;
import it.polito.dp2.BIB.sol3.service.jaxb.Item;

public class BookshelfDB {

	private static BookshelfDB bsDB = new BookshelfDB();
	private static long lastId = 0;
	
	private ConcurrentHashMap<Long, Bookshelf> bookshelvesById;
	private ConcurrentHashMap<String, ConcurrentHashMap<Long, Bookshelf>> bookshelvesByKeyword;
	private ConcurrentHashMap<Long, ConcurrentHashMap<Long, Item>> bookshelfItems;
	
	
	private BookshelfDB() {
		bookshelvesById = new ConcurrentHashMap<Long, Bookshelf>();
		bookshelvesByKeyword = new ConcurrentHashMap<String, ConcurrentHashMap<Long, Bookshelf>>();
		bookshelfItems = new ConcurrentHashMap<Long, ConcurrentHashMap<Long, Item>>();
	}
	
	public static BookshelfDB getBookshelfDB() {
		return bsDB;
	}
	
	public static synchronized long getNextId() {
		return ++lastId;
	}
	
	public Map<Long, Bookshelf> getBookshelves(String keyword) {
		if (keyword == null)
			keyword = "";
		System.out.println(" --------------------------");
		System.out.println(" --- KEYWORD " + keyword);
		System.out.println(" --------------------------");
		ConcurrentHashMap<Long, Bookshelf> map = bookshelvesByKeyword.get(keyword);
		if (map == null)
			return new HashMap<Long, Bookshelf>(0);
		return map;
	}
	
	public Bookshelf getBookshelf(BigInteger id) {
		return bookshelvesById.get(id.longValue());
	}
	
	public Bookshelf createBookshelf(long id, Bookshelf bookshelf) {
		if (bookshelvesById.putIfAbsent(id, bookshelf) == null) {
			addIndexing(bookshelf, id);
			bookshelfItems.put(id, new ConcurrentHashMap<>(0));
			return bookshelf;
		} else 
			return null;
	}
	
	private void addIndexing(Bookshelf bookshelf, long id) {
		addToIndex("", bookshelf, id);
		StringTokenizer st = new StringTokenizer(bookshelf.getName());
		System.out.println(" --------------------------");
		while (st.hasMoreTokens()) {
			String token = st.nextToken();
			addToIndex(token, bookshelf, id);
			System.out.println(" --- INDEXED " + bookshelf.getName()  + " with " + token );
		}
		System.out.println(" --------------------------");
	}
	
	private void addToIndex(String token, Bookshelf bookshelf, long id) {
		ConcurrentHashMap<Long, Bookshelf> set = bookshelvesByKeyword.get(token);
		if (set == null) { // create set if there was no set for this token
			ConcurrentHashMap<Long, Bookshelf> newMap = new ConcurrentHashMap<>();
			set = bookshelvesByKeyword.putIfAbsent(token, newMap);
			if (set == null)
				set = newMap;
		}
		set.put(id, bookshelf);
	}
	
	private void removeIndexing(Bookshelf bookshelf, long id) {
		removeFromIndex("", id);
		StringTokenizer st = new StringTokenizer(bookshelf.getName());
		while (st.hasMoreTokens()) {
			String token = st.nextToken();
			removeFromIndex(token, id);
		}
	}
	
	private void removeFromIndex(String token, long id) {
		ConcurrentHashMap<Long, Bookshelf> map = bookshelvesByKeyword.get(token);
		if (map != null) {
			map.remove(id);
			bookshelfItems.remove(id);
		}
	}
	
	public Bookshelf updateBookshelf(BigInteger id, Bookshelf bookshelf) {
		Bookshelf oldBookshelf = bookshelvesById.put(id.longValue(), bookshelf);
		removeIndexing(oldBookshelf, id.longValue());
		addIndexing(bookshelf, id.longValue());
		return bookshelf;
	}
	
	public BigInteger deleteBookshelf(BigInteger id) {
		Bookshelf oldBookshelf = bookshelvesById.remove(id.longValue());
		if (oldBookshelf == null) return null;
		removeIndexing(oldBookshelf, id.longValue());
		return id;
	}
	
	public Map<Long, Item> getBookshelfItems(BigInteger id) {
		return bookshelfItems.get(id.longValue());
	}
	
	public synchronized Item addItemToBookshelf(BigInteger bookshelfId, BigInteger itemId, Item item) {
		ConcurrentHashMap<Long, Item> map = bookshelfItems.get(bookshelfId);
		if (map == null) return null;
		map.put(itemId.longValue(), item);
		return item;
	}
	
}
