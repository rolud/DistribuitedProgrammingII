package it.polito.dp2.BIB.sol1;

import java.io.File;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import it.polito.dp2.BIB.IssueReader;
import it.polito.dp2.BIB.ItemReader;
import it.polito.dp2.BIB.JournalReader;

import it.polito.dp2.BIB.sol1.jaxb.ArticleType;
import it.polito.dp2.BIB.sol1.jaxb.Biblio;
import it.polito.dp2.BIB.sol1.jaxb.BookType;
import it.polito.dp2.BIB.sol1.jaxb.JournalType;
import it.polito.dp2.BIB.sol1.jaxb.JournalType.Issue;
import it.polito.dp2.BIB.sol1.jaxb.ObjectFactory;


public class BibInfoSerializer {
	private BibReader monitor;
	private ObjectFactory of;
	private Map<String, BookType> books;
	private Map<BigInteger, ArticleType> articles;
	private Map<String, JournalType> journals;
	private int itemID;
	private int issueID;
	
	public BibInfoSerializer() throws BibReaderException {
		BibReaderFactory factory = BibReaderFactory.newInstance();
		monitor = factory.newBibReader();
		of = new ObjectFactory();
		books = new HashMap<String, BookType>();
		articles = new HashMap<BigInteger, ArticleType>();
		journals = new HashMap<String, JournalType>();
		itemID = 0;
		issueID = 0;
	}
	
	public BibInfoSerializer(BibReader monitor) {
		this.monitor = monitor;
		of = new ObjectFactory();
		books = new HashMap<String, BookType>();
		articles = new HashMap<BigInteger, ArticleType>();
		journals = new HashMap<String, JournalType>();
		itemID = 0;
		issueID = 0;
	}
	
	public static void main(String[] args) {
		BibInfoSerializer bis;
		String filename = null;
		if (args.length != 0) {
			filename = args[0];
		}
		for (int i = 0; i < args.length; i++) System.out.print(args[i] + " ");
		System.out.println();
		try {
			bis = new BibInfoSerializer();
			bis.deserialize();
			bis.serialize(filename);
		} catch (BibReaderException e) {
			System.err.println("Could not instantiate data generator.");
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	private void deserialize() {
		deserializeJournals();
		deserializeItems();
	}
	
	
	private void serialize(String filename) {
		Biblio biblio = of.createBiblio();
		Biblio.Items items = of.createBiblioItems();
		items.getBook().addAll(books.values());
		items.getArticle().addAll(articles.values());
		biblio.setItems(items);
		
		Biblio.Journals journalsElement = of.createBiblioJournals();
		journalsElement.getJournal().addAll(journals.values());
		biblio.setJournals(journalsElement);
		
		if (filename != null)
			JAXB.marshal(biblio, new File(filename));
		
	}
	
	private void deserializeItems() {
		Set<ItemReader> set = monitor.getItems(null,  0,  3000);
		
		// read and save book and article items
		for (ItemReader item : set) {
			if (item instanceof BookReader) {
				BookType b = of.createBookType();
				b.setId(BigInteger.valueOf(itemID++));
				for (int i = 0; i < item.getAuthors().length; i++)
					b.getAuthor().add(item.getAuthors()[i]);
				b.setTitle(item.getTitle());
				if (item.getSubtitle() != null) b.setSubtitle(item.getSubtitle());
				b.setYear(getDate(((BookReader) item).getYear()));
				b.setIsbn(((BookReader) item).getISBN());
				b.setPublisher(((BookReader) item).getPublisher());
				books.put(b.getIsbn(), b);
			} else if (item instanceof ArticleReader) {
				ArticleType a = of.createArticleType();
				a.setId(BigInteger.valueOf(itemID++));
				for (int i = 0; i < item.getAuthors().length; i++)
					a.getAuthor().add(item.getAuthors()[i]);
				a.setTitle(item.getTitle());
				if (item.getSubtitle() != null) a.setSubtitle(item.getSubtitle());
				a.setJournal(((ArticleReader) item).getJournal().getISSN());
				IssueReader issue = ((ArticleReader) item).getIssue();
				for (Issue i : journals.get(a.getJournal()).getIssue()) {
					if (i.getYear().getYear() == issue.getYear() && i.getNumber().intValue() == issue.getNumber()) {
						a.setIssue(i.getId());
						break;
					}
				}
				articles.put(a.getId(), a);
			}
		}
		
		// read items again to set citedBy elements
		for (ItemReader item : set) {
			if (item instanceof BookReader) {
				BookType book = books.get(((BookReader) item).getISBN());
				List<BigInteger> citedByElements = book.getCitedBy();
				for (ItemReader cit : item.getCitingItems()) {
					if (cit instanceof BookReader) {
						citedByElements.add(books.get(((BookReader) cit).getISBN()).getId());
					} else if (cit instanceof ArticleReader) {
						for (ArticleType at : articles.values()) {
							if (at.getTitle().equals(cit.getTitle())) {
								for (Issue iss : journals.get(((ArticleReader) cit).getJournal().getISSN()).getIssue()) {
									if (iss.getYear().getYear() == ((ArticleReader) cit).getIssue().getYear() 
											&& iss.getNumber().intValue() == ((ArticleReader) cit).getIssue().getNumber() ) {
										citedByElements.add(at.getId());
										break;
									}
								}
							}
						}
					}
				}
			} else if (item instanceof ArticleReader) {
				ArticleType article = null;
				for (ArticleType at : articles.values()) {
					if (at.getTitle().equals(item.getTitle())) {
						for (Issue iss : journals.get(((ArticleReader) item).getJournal().getISSN()).getIssue()) {
							if (iss.getYear().getYear() == ((ArticleReader) item).getIssue().getYear() 
									&& iss.getNumber().intValue() == ((ArticleReader) item).getIssue().getNumber() ) {
								article = at;
								break;
							}
						}
					}
				}
				if (article != null) {
					List<BigInteger> citedByElements = article.getCitedBy();
					for (ItemReader cit : item.getCitingItems()) {
						if (cit instanceof BookReader) {
							citedByElements.add(books.get(((BookReader) cit).getISBN()).getId());
						} else if (cit instanceof ArticleReader) {
							for (ArticleType at : articles.values()) {
								if (at.getTitle().equals(cit.getTitle())) {
									for (Issue iss : journals.get(((ArticleReader) cit).getJournal().getISSN()).getIssue()) {
										if (iss.getYear().getYear() == ((ArticleReader) cit).getIssue().getYear() 
												&& iss.getNumber().intValue() == ((ArticleReader) cit).getIssue().getNumber() ) {
											citedByElements.add(at.getId());
											break;
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}
	
	private void deserializeJournals() {
		Set<JournalReader> set = monitor.getJournals(null);
		
		for (JournalReader journal : set) {
			JournalType j = of.createJournalType();
			j.setIssn(journal.getISSN());
			j.setPublisher(journal.getPublisher());
			j.setTitle(journal.getTitle());
			List<Issue> issues = j.getIssue();
			for (IssueReader ir : journal.getIssues(0, 3000)) {
				Issue i = of.createJournalTypeIssue();
				i.setNumber(BigInteger.valueOf(ir.getNumber()));
				i.setYear(getDate(ir.getYear()));
				i.setId(BigInteger.valueOf(issueID++));
				issues.add(i);
			}
			journals.put(j.getIssn(), j);
				
		}
	}
	

	private XMLGregorianCalendar getDate(int year) {
		try {
			return DatatypeFactory.newInstance().newXMLGregorianCalendar(year, 1, 1, 0, 0, 0, 0, 0);
		} catch (DatatypeConfigurationException e) {
			throw new Error(e);
		}
	}
	
}
