package it.polito.dp2.BIB.sol1;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import it.polito.dp2.BIB.ArticleReader;
import it.polito.dp2.BIB.IssueReader;
import it.polito.dp2.BIB.ItemReader;
import it.polito.dp2.BIB.JournalReader;
import it.polito.dp2.BIB.sol1.jaxb.ArticleType;
import it.polito.dp2.BIB.sol1.jaxb.JournalType;
import it.polito.dp2.BIB.sol1.jaxb.JournalType.Issue;

public class ArticleReaderImpl extends ItemReaderImpl implements ArticleReader {

	private JournalReaderImpl journal;
	private IssueReaderImpl issue;
	
	protected ArticleReaderImpl(ArticleType article, List<JournalType> journals, Map<String, JournalReaderImpl> jrnImpl) {
		super.title = article.getTitle();
		super.subtitle = article.getSubtitle();
		super.authors = article.getAuthor().toArray(new String[article.getAuthor().size()]);
		super.citingElements = new HashSet<ItemReader>();
		JournalType journal = journals.stream().filter(j -> j.getIssn().equals(article.getJournal())).collect(Collectors.toList()).get(0);
		this.journal = jrnImpl.get(journal.getIssn());
		Issue issue = journal.getIssue().stream().filter(i -> i.getId().intValue() == article.getIssue().intValue()).collect(Collectors.toList()).get(0);
		this.issue = (IssueReaderImpl) this.journal.getIssue(issue.getYear().getYear(), issue.getNumber().intValue());
		
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
