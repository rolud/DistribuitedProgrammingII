package it.polito.dp2.BIB.sol1;

import java.util.Set;

import it.polito.dp2.BIB.ArticleReader;
import it.polito.dp2.BIB.BibReader;
import it.polito.dp2.BIB.BibReaderException;
import it.polito.dp2.BIB.BookReader;
import it.polito.dp2.BIB.FactoryConfigurationError;
import it.polito.dp2.BIB.ItemReader;
import it.polito.dp2.BIB.JournalReader;

public class Ass1cMainTester {
	public static void main (String args[]) throws BibReaderException {
		System.setProperty("it.polito.dp2.BIB.BibReaderFactory", "it.polito.dp2.BIB.sol1.BibReaderFactory");
		System.setProperty("it.polito.dp2.BIB.sol1.BibInfo.file", "xsd/biblio_e_<Error.xml");
		BibReader tester;
		
		tester = BibReaderFactory.newInstance().newBibReader();
				
		Set<ItemReader> items = tester.getItems(null, 0, 9999);
		Set<JournalReader>  journals = tester.getJournals(null);
		
		System.out.println(" ==== ITEMS SIZE " + items.size());
		items.forEach(item -> {
			if (item instanceof BookReader) {
				System.out.println(" - BOOK");
				System.out.println(" --- ISBN      : " + ((BookReader) item).getISBN());
				System.out.println(" --- TITLE     : " + item.getTitle());
				if (item.getSubtitle() != null && item.getSubtitle().length() != 0)
					System.out.println(" --- SUBTITLE  : " + item.getSubtitle());
				System.out.println(" --- YEAR      : " + ((BookReader) item).getYear());
				System.out.print(" --- AUTHORS   : " + item.getAuthors()[0]);
				for(int i = 1; i < item.getAuthors().length; i++) 
					System.out.print(", " + item.getAuthors()[i]);
				System.out.println();
				System.out.println(" --- PUBLISHER : " + ((BookReader) item).getPublisher());
				System.out.println();
			} else {
				System.out.println(" - ARTICLE ");
				System.out.println(" --- TITLE     : " + item.getTitle());
				if (item.getSubtitle() != null && item.getSubtitle().length() != 0)
					System.out.println(" --- SUBTITLE  : " + item.getSubtitle());
				System.out.println(" --- JOURNAL   : " + ((ArticleReader) item).getJournal().getTitle());
				System.out.println(" --- ISSUE     : " + ((ArticleReader) item).getIssue().getYear() + " " + ((ArticleReader) item).getIssue().getNumber());
				System.out.print(" --- AUTHORS   : " + item.getAuthors()[0]);
				for(int i = 1; i < item.getAuthors().length; i++) 
					System.out.print(", " + item.getAuthors()[i]);
				System.out.println("\n");
			}
		});
		System.out.println("\n");
		System.out.println(" ==== JOURNALS SIZE " + journals.size());
		journals.forEach(journal -> {
			System.out.println(" - JOURNAL " + journal.getTitle() + " (" + journal.getIssues(0, 9999).size() + ")");
			journal.getIssues(0, 9999).forEach(issue -> {
				System.out.println(" --- ISSUE : " +issue.getYear() + " " + issue.getNumber() + " (" + issue.getArticles().size() + " articles)");
			});
			System.out.println();
		});
	}

}
