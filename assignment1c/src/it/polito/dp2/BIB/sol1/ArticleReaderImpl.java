package it.polito.dp2.BIB.sol1;

import java.util.Set;

import it.polito.dp2.BIB.ArticleReader;
import it.polito.dp2.BIB.IssueReader;
import it.polito.dp2.BIB.ItemReader;
import it.polito.dp2.BIB.JournalReader;
import it.polito.dp2.BIB.sol1.jaxb.ArticleType;

public class ArticleReaderImpl extends ItemReaderImpl implements ArticleReader {

	private JournalReaderImpl journal;
	private IssueReaderImpl issue;
	
	protected ArticleReaderImpl(ArticleType article) {
		super.title = article.getTitle();
		super.subtitle = article.getSubtitle();
		super.authors = article.getAuthor().toArray(new String[article.getAuthor().size()]);
		
	}
	
	@Override
	public String[] getAuthors() {
		return authors;
	}

	@Override
	public Set<ItemReader> getCitingItems() {
		return citingElements;
	}

	@Override
	public String getSubtitle() {
		return subtitle;
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public IssueReader getIssue() {
		return issue;
	}

	@Override
	public JournalReader getJournal() {
		return journal;
	}

}
