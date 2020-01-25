package it.polito.dp2.BIB.sol3.client;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotFoundException;

import it.polito.dp2.BIB.ass3.DestroyedBookshelfException;
import it.polito.dp2.BIB.ass3.ItemReader;
import it.polito.dp2.BIB.ass3.ServiceException;
import it.polito.dp2.BIB.ass3.TooManyItemsException;
import it.polito.dp2.BIB.ass3.UnknownItemException;
import it.polito.dp2.BIB.sol3.client.Bookshelves.Bookshelf;

public class BookshelfImpl implements it.polito.dp2.BIB.ass3.Bookshelf {

	private Integer id;
	private String name;
	private boolean destroyed;
	
	private ClientFactoryImpl client;
	
	public BookshelfImpl(Bookshelf b, ClientFactoryImpl client) {
		this.id = resolveId(b.getSelf());
		this.name = b.getName();
		this.destroyed = false;
		this.client = client;
	}
	
	@Override
	public String getName() throws DestroyedBookshelfException {
		if (destroyed) throw new DestroyedBookshelfException();
		return this.name;
	}

	@Override
	public void addItem(ItemReader item)
			throws DestroyedBookshelfException, UnknownItemException, TooManyItemsException, ServiceException {
		if (destroyed) throw new DestroyedBookshelfException();
		try {
			ItemReader ir = client.addItemToBookshelf(this.id, item);
		} catch (NotFoundException e) {
			throw new UnknownItemException(e);
		} catch (ForbiddenException e) {
			throw new TooManyItemsException(e);
		}
	}

	@Override
	public void removeItem(ItemReader item) throws DestroyedBookshelfException, UnknownItemException, ServiceException {
		if (destroyed) throw new DestroyedBookshelfException();
		try {
			client.removeItemFromBookshelf(this.id, item);
		} catch (NotFoundException e) {
			throw new UnknownItemException(e);
		}
	}

	@Override
	public Set<ItemReader> getItems() throws DestroyedBookshelfException, ServiceException {
		if (destroyed) throw new DestroyedBookshelfException();
		return client.getBookshelfItems(this.id);
	}

	@Override
	public void destroyBookshelf() throws DestroyedBookshelfException, ServiceException {
		if (destroyed) throw new DestroyedBookshelfException();
		client.removeBookshelf(this.id);
		this.destroyed = true;
	}

	@Override
	public int getNumberOfReads() throws DestroyedBookshelfException, ServiceException {
		if (destroyed) throw new DestroyedBookshelfException();
		return client.getNumberOfReads(this.id);
	}

	private static int resolveId(String self) {
		String[] tokens = self.split("bookshelves/");
		if (tokens.length != 2) return -1;
		int id;
		try {
			id = Integer.parseInt(tokens[1]);
		} catch (NumberFormatException e) {
			return -1;
		}
		return id;
	}
}
