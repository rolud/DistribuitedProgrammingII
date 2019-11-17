package it.polito.dp2.BIB.sol1;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import it.polito.dp2.BIB.BibReader;
import it.polito.dp2.BIB.BookReader;
import it.polito.dp2.BIB.ItemReader;
import it.polito.dp2.BIB.JournalReader;
import it.polito.dp2.BIB.sol1.jaxb.Biblio;

public class BibReaderImpl implements BibReader {

	private Set<BookReaderImpl> books;
	private Set<ArticleReaderImpl> articles;
	private Set<JournalReaderImpl> journals;
	
	protected BibReaderImpl(Biblio biblio) {
		books = new HashSet<BookReaderImpl>();
		articles = new HashSet<ArticleReaderImpl>();
		journals = new HashSet<JournalReaderImpl>();
		biblio.getItems().getBook().forEach(book -> {
			books.add(new BookReaderImpl(book));
		});
		biblio.getItems().getArticle().forEach(article -> {
			articles.add(new ArticleReaderImpl(article));
		});
		biblio.getJournals().getJournal().forEach(journal -> {
			journals.add(new JournalReaderImpl(journal));
		});
		
	}
	
	@Override
	public BookReader getBook(String isbn) {
		for (BookReaderImpl book : books)
			if (book.getISBN() == isbn)
				return book;
		return null;
	}

	@Override
	public Set<ItemReader> getItems(String keyword, int to, int since) {
		// TODO Auto-generated method stub
		Set<ItemReader> items = new HashSet<>();
		if (keyword == null) {
			items.addAll(
					books.stream()
						.filter(b -> b.getYear() >= since && b.getYear() <= to)
						.collect(Collectors.toSet())
			);
			items.addAll(
					articles.stream()
						.filter(a -> a.getIssue().getYear() >= since && a.getIssue().getYear() <= to)
						.collect(Collectors.toSet())
			);
		} else if (keyword == "book") {
			items.addAll(
					books.stream()
					.filter(b -> b.getYear() >= since && b.getYear() <= to)
					.collect(Collectors.toSet())
			);
		} else if (keyword == "article") {
			items.addAll(
					articles.stream()
					.filter(a -> a.getIssue().getYear() >= since && a.getIssue().getYear() <= to)
					.collect(Collectors.toSet())
			);
		}
		return items;
	}

	@Override
	public JournalReader getJournal(String issn) {
		for (JournalReaderImpl journal : journals)
			if (journal.getISSN() == issn)
				return journal;
		return null;
	}

	@Override
	public Set<JournalReader> getJournals(String keyword) {
		return journals.stream()
				.filter(j -> j.getTitle().contains(keyword) || j.getPublisher().contains(keyword))
				.collect(Collectors.toSet());
	}

}
