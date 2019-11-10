package it.polito.dp2.BIB.sol1;

import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXB;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import it.polito.dp2.BIB.ArticleReader;
import it.polito.dp2.BIB.BibReader;
import it.polito.dp2.BIB.BibReaderException;
import it.polito.dp2.BIB.BibReaderFactory;
import it.polito.dp2.BIB.BookReader;
import it.polito.dp2.BIB.ItemReader;

import it.polito.dp2.BIB.sol1.jaxb.ArticleType;
import it.polito.dp2.BIB.sol1.jaxb.Biblio;
import it.polito.dp2.BIB.sol1.jaxb.BookType;
import it.polito.dp2.BIB.sol1.jaxb.JournalType;
import it.polito.dp2.BIB.sol1.jaxb.ObjectFactory;






public class BibInfoSerializer {
	private BibReader monitor;
	private ObjectFactory of;
	private List<BookType> books;
	private List<ArticleType> articles;
	private List<JournalType> journals;
	private int itemID;
	
	public BibInfoSerializer() throws BibReaderException {
		BibReaderFactory factory = BibReaderFactory.newInstance();
		monitor = factory.newBibReader();
		of = new ObjectFactory();
		books = new ArrayList<BookType>();
		articles = new ArrayList<ArticleType>();
		journals = new ArrayList<JournalType>();
		itemID = 0;
	}
	
	public BibInfoSerializer(BibReader monitor) {
		this.monitor = monitor;
		of = new ObjectFactory();
		books = new ArrayList<BookType>();
		articles = new ArrayList<ArticleType>();
		journals = new ArrayList<JournalType>();
		itemID = 0;
	}
	
	public static void main(String[] args) {
		BibInfoSerializer bis;
		try {
			bis = new BibInfoSerializer();
			bis.deserialize();
			bis.serialize();
		} catch (BibReaderException e) {
			System.err.println("Could not instantiate data generator.");
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	private void deserialize() {
		deserializeItems();
		deserializeJournals();
	}
	
	
	private void serialize() {
		Biblio biblio = of.createBiblio();
		Biblio.Items items = of.createBiblioItems();
		items.getBook().addAll(books);
		biblio.setItems(items);
		
		JAXB.marshal(biblio, System.out);
		
	}
	
	private void deserializeItems() {
		Set<ItemReader> set = monitor.getItems(null,  0,  3000);
		
		for (ItemReader item : set) {
			if (item instanceof BookReader) {
				BookType b = of.createBookType();
				b.setId(BigInteger.valueOf(itemID++));
				b.setTitle(item.getTitle());
				if (item.getSubtitle() != null) b.setSubtitle(item.getSubtitle());
				b.setYear(getDate(((BookReader) item).getYear()));
				b.setIsbn(((BookReader) item).getISBN());
				b.setPublisher(((BookReader) item).getPublisher());
				books.add(b);
			} else if (item instanceof ArticleReader) {
				ArticleType a = of.createArticleType();
				a.setId(BigInteger.valueOf(itemID++));
				a.setTitle(item.getTitle());
				if (item.getSubtitle() != null) a.setSubtitle(item.getSubtitle());
				a.setJournal(((ArticleReader) item).getJournal().getISSN());
//				a.setIssue(((ArticleReader) item).getIssue().get);
			}
		}
	}
	
	private void deserializeJournals() {}
	

	private XMLGregorianCalendar getDate(int year) {
		try {
			return DatatypeFactory.newInstance().newXMLGregorianCalendar(year, 1, 1, 0, 0, 0, 0, 0);
		} catch (DatatypeConfigurationException e) {
			throw new Error(e);
		}
	}
	
	private void printBlankLine() {
		System.out.println(" ");
	}

	
	private void printLine(char c) {
		System.out.println(makeLine(c));
	}

	private void printHeader(String header) {
		System.out.println(header);
	}

	private void printHeader(String header, char c) {		
		System.out.println(header);
		printLine(c);	
	}
	
	private void printHeader(char c, String header) {		
		printLine(c);	
		System.out.println(header);
	}
	
	private StringBuffer makeLine(char c) {
		StringBuffer line = new StringBuffer(132);
		
		for (int i = 0; i < 132; ++i) {
			line.append(c);
		}
		return line;
	}
	
}
