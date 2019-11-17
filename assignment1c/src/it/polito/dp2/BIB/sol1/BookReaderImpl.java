package it.polito.dp2.BIB.sol1;

import java.util.Set;

import it.polito.dp2.BIB.BookReader;
import it.polito.dp2.BIB.ItemReader;
import it.polito.dp2.BIB.sol1.jaxb.BookType;

public class BookReaderImpl extends ItemReaderImpl implements BookReader {

	private String isbn;
	private String publisher;
	private int year;
	
	protected BookReaderImpl(BookType book) {
		super.title = book.getTitle();
		super.subtitle = book.getSubtitle();
		super.authors = book.getAuthor().toArray(new String[book.getAuthor().size()]);
		this.isbn = book.getTitle();
		this.publisher = book.getPublisher();
		this.year = book.getYear().getYear();
	}
	
	@Override
	public String[] getAuthors() {
		return this.authors;
	}

	@Override
	public Set<ItemReader> getCitingItems() {
		return this.citingElements;
	}

	@Override
	public String getSubtitle() {
		return this.subtitle;
	}

	@Override
	public String getTitle() {
		return this.title;
	}

	@Override
	public String getISBN() {
		return this.isbn;
	}

	@Override
	public String getPublisher() {
		return this.publisher;
	}

	@Override
	public int getYear() {
		return this.year;
	}

}
