package it.polito.dp2.BIB.sol1;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import it.polito.dp2.BIB.BibReader;
import it.polito.dp2.BIB.BookReader;
import it.polito.dp2.BIB.ItemReader;
import it.polito.dp2.BIB.JournalReader;

import it.polito.dp2.BIB.sol1.jaxb.Biblio;

public class BibReaderImpl implements BibReader {

	private Map<Integer, BookReaderImpl> books;
	private Map<Integer, ArticleReaderImpl> articles;
	private Map<String, JournalReaderImpl> journals;
	
	protected BibReaderImpl(Biblio biblio) {
		books    = new HashMap<Integer, BookReaderImpl>();
		articles = new HashMap<Integer, ArticleReaderImpl>();
		journals = new HashMap<String, JournalReaderImpl>();
		
		biblio.getJournals().getJournal().forEach(journal -> {
			journals.put(journal.getIssn(), new JournalReaderImpl(journal));
		});
		biblio.getItems().getBook().forEach(book -> {
			books.put(book.getId().intValue(), new BookReaderImpl(book));
		});
		biblio.getItems().getArticle().forEach(article -> {
			articles.put(article.getId().intValue(), new ArticleReaderImpl(article, biblio.getJournals().getJournal(), journals));
		});
		
		// set article refs in issue
				articles.values().forEach(a -> {
			JournalReaderImpl j = journals.get(a.getJournal().getISSN());
			j.getIssues().forEach(i -> {
				if (a.getJournal().getISSN().equals(j.getISSN()) && a.getIssue().getYear() == i.getYear() && a.getIssue().getNumber() == i.getNumber())
					i.addArticle(a);
			});
		});
		
		// set citing items refs
		biblio.getItems().getBook().forEach(b -> {
			BookReaderImpl book = books.get(b.getId().intValue());
			b.getCitedBy().forEach(id -> {
				
				if (books.containsKey(id.intValue())) 
					book.addCitingElements(books.get(id.intValue()));
				
				if (articles.containsKey(id.intValue())) 
					book.addCitingElements(articles.get(id.intValue()));
			
			});
			
		});
		
		biblio.getItems().getArticle().forEach(a -> {
			ArticleReaderImpl article = articles.get(a.getId().intValue());
			a.getCitedBy().forEach(id -> {
				
				if (books.containsKey(id.intValue())) 
					article.addCitingElements(books.get(id.intValue()));
				
				if (articles.containsKey(id.intValue())) 
					article.addCitingElements(articles.get(id.intValue()));
			
			});
			
		});
		
	}
	
	@Override
	public BookReader getBook(String isbn) {
		for (BookReaderImpl book : books.values())
			if (book.getISBN() == isbn)
				return book;
		return null;
	}

	@Override
	public Set<ItemReader> getItems(String keyword, int since, int to) {
		// TODO Auto-generated method stub
		Set<ItemReader> items = new HashSet<>();
		if (keyword == null) {
			items.addAll(
					books.values().stream()
						.filter(b -> b.getYear() >= since && b.getYear() <= to)
						.collect(Collectors.toSet())
			);
			items.addAll(
					articles.values().stream()
						.filter(a -> a.getIssue().getYear() >= since && a.getIssue().getYear() <= to)
						.collect(Collectors.toSet())
			);
		} else if (keyword == "book") {
			items.addAll(
					books.values().stream()
					.filter(b -> b.getYear() >= since && b.getYear() <= to)
					.collect(Collectors.toSet())
			);
		} else if (keyword == "article") {
			items.addAll(
					articles.values().stream()
					.filter(a -> a.getIssue().getYear() >= since && a.getIssue().getYear() <= to)
					.collect(Collectors.toSet())
			);
		}
		return items;
	}

	@Override
	public JournalReader getJournal(String issn) {
		for (JournalReaderImpl journal : journals.values())
			if (journal.getISSN() == issn)
				return journal;
		return null;
	}

	@Override
	public Set<JournalReader> getJournals(String keyword) {
		if (keyword == null)
			return journals.values().stream().collect(Collectors.toSet());
		else
			return journals.values().stream()
					.filter(j -> j.getTitle().contains(keyword) || j.getPublisher().contains(keyword))
					.collect(Collectors.toSet());
	}

}
