package it.polito.dp2.BIB.sol1;

import java.util.Set;

import it.polito.dp2.BIB.ItemReader;

public class ItemReaderImpl implements ItemReader {

	protected String title;
	protected String[] authors;
	protected String subtitle;
	protected Set<ItemReader> citingElements;
	

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

}
