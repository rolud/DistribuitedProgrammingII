package it.polito.dp2.BIB.sol1;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import it.polito.dp2.BIB.IssueReader;
import it.polito.dp2.BIB.JournalReader;
import it.polito.dp2.BIB.sol1.jaxb.JournalType;

public class JournalReaderImpl implements JournalReader {

	private String issn;
	private String title;
	private String publisher;
	private Set<IssueReaderImpl> issues;
	
	protected JournalReaderImpl (JournalType journal) {
		this.issn = journal.getIssn();
		this.title = journal.getTitle();
		this.publisher = journal.getPublisher();
		this.issues = new HashSet<IssueReaderImpl>();
		journal.getIssue().forEach(i -> this.issues.add(new IssueReaderImpl(i, this)));
		
	}
	
	protected Set<IssueReaderImpl> getIssues() {
		return this.issues;
	}
	
	@Override
	public String getISSN() {
		return this.issn;
	}

	@Override
	public IssueReader getIssue(int year, int number) {
		for (IssueReaderImpl issue : issues) 
			if (issue.getYear() == year && issue.getNumber() == number)
				return issue;
		return null;
	}

	@Override
	public Set<IssueReader> getIssues(int since, int to) {
		return issues.stream()
				.filter(issue -> issue.getYear() >= since && issue.getYear() <= to)
				.collect(Collectors.toSet());
	}

	@Override
	public String getPublisher() {
		return this.publisher;
	}

	@Override
	public String getTitle() {
		return this.title;
	}

}
