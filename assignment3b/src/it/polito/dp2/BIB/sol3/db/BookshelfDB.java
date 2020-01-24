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

	private final static int MAX_ITEMS = 20;
	
	private static BookshelfDB bsDB = new BookshelfDB();
	private static long lastId = 0;
	
	private static ConcurrentHashMap<Long, Bookshelf> bookshelvesById;
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
	
	public static Map<Long, Bookshelf> getMap() {
		return bookshelvesById;
	}
	
	public Map<Long, Bookshelf> getBookshelves(String keyword) {
		if (keyword == null)
			keyword = "";
		ConcurrentHashMap<Long, Bookshelf> map = bookshelvesByKeyword.get(keyword.toLowerCase());
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
			return bookshelf;
		} else 
			return null;
	}
	
	private void addIndexing(Bookshelf bookshelf, long id) {
		addToIndex("", bookshelf, id);
		
		
		StringTokenizer st = new StringTokenizer(bookshelf.getName());
		while (st.hasMoreTokens()) {
			String word = st.nextToken();
			StringBuffer sb = new StringBuffer();
			for ( char c : word.toCharArray()) {
				sb.append(c);
				String token = sb.toString();
				addToIndex(token, bookshelf, id);
			}
		}
	}
	
	private void addToIndex(String token, Bookshelf bookshelf, long id) {
		ConcurrentHashMap<Long, Bookshelf> set = bookshelvesByKeyword.get(token);
		if (set == null) { // create set if there was no set for this token
			ConcurrentHashMap<Long, Bookshelf> newMap = new ConcurrentHashMap<>();
			set = bookshelvesByKeyword.putIfAbsent(token.toLowerCase(), newMap);
			if (set == null)
				set = newMap;
		}
		set.put(id, bookshelf);
	}
	
	private void removeIndexing(Bookshelf bookshelf, long id) {
		removeFromIndex("", id);
		StringTokenizer st = new StringTokenizer(bookshelf.getName());
		while (st.hasMoreTokens()) {
			String word = st.nextToken();
			StringBuffer sb = new StringBuffer();
			for ( char c : word.toCharArray()) {
				sb.append(c);
				String token = sb.toString();
				removeFromIndex(token, id);
			}
		}
	}
	
	private void removeFromIndex(String token, long id) {
		ConcurrentHashMap<Long, Bookshelf> map = bookshelvesByKeyword.get(token);
		if (map != null) {
			map.remove(id);
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
		ConcurrentHashMap<Long, Item> map = bookshelfItems.get(id.longValue());
		if (map == null) return new HashMap<Long,Item>(0);
		return map;
	}
	
	public Item getItemFromBookshelf(BigInteger bookshelfId, BigInteger itemId) {
		ConcurrentHashMap<Long, Item> map = bookshelfItems.get(bookshelfId.longValue());
		if (map == null) return null;
		return map.get(itemId.longValue());
	}
	
	public synchronized Item addItemToBookshelf(BigInteger bookshelfId, BigInteger itemId, Item item) {
		ConcurrentHashMap<Long, Item> map = bookshelfItems.get(bookshelfId.longValue());
		if (map == null) {
			map = new ConcurrentHashMap<Long, Item>();
			bookshelfItems.put(bookshelfId.longValue(), map);
		}
		if (map.size() == MAX_ITEMS) return null;
		map.put(itemId.longValue(), item);
		return item;
	}
	
	public synchronized Item deleteItemFromBookshelf(BigInteger bookshelfId, BigInteger itemId) {
		Item item;
		ConcurrentHashMap<Long, Item> map = bookshelfItems.get(bookshelfId.longValue());
		if (map == null) return null;
		item = map.remove(itemId.longValue());
		return item;
	}

	public synchronized void deleteItemFromAllBookshelves(BigInteger id) {
		for (Map<Long,Item> map : bookshelfItems.values()) {
			map.remove(id.longValue());
		}
	}
}
