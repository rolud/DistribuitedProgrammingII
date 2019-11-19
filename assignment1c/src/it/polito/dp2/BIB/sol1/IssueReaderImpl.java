package it.polito.dp2.BIB.sol1;

import java.util.HashSet;
import java.util.Set;

import it.polito.dp2.BIB.ArticleReader;
import it.polito.dp2.BIB.IssueReader;
import it.polito.dp2.BIB.JournalReader;

import it.polito.dp2.BIB.sol1.jaxb.JournalType;

public class IssueReaderImpl implements IssueReader {

	private JournalReaderImpl journal;
	private Set<ArticleReader> articles;
	private int year;
	private int number;
	
	protected IssueReaderImpl(JournalType.Issue issue, JournalReaderImpl journal) {
		this.journal = journal;
		this.year = issue.getYear().getYear();
		this.number = issue.getNumber().intValue();
		this.articles = new HashSet<ArticleReader>();
	}
	
	protected void addArticle(ArticleReader article) {
		this.articles.add(article);
	}
	
	@Override
	public Set<ArticleReader> getArticles() {
		return articles;
	}

	@Override
	public JournalReader getJournal() {
		return this.journal;
	}

	@Override
	public int getNumber() {
		return this.number;
	}

	@Override
	public int getYear() {
		return this.year;
	}

}
