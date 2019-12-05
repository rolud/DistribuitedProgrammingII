package it.polito.dp2.BIB.sol2;

import java.util.HashSet;
import java.util.Set;

import it.polito.dp2.BIB.FactoryConfigurationError;
import it.polito.dp2.BIB.IssueReader;
import it.polito.dp2.BIB.ItemReader;
import it.polito.dp2.BIB.ass2.CitationFinder;
import it.polito.dp2.BIB.ass2.CitationFinderException;
import it.polito.dp2.BIB.ass2.ServiceException;
import it.polito.dp2.BIB.ass2.UnknownItemException;

/*
 * Created by rolud
 */
public class Ass2TestMain {
	public static void main(String args[]) {
		try {
			System.setProperty("it.polito.dp2.BIB.BibReaderFactory", "it.polito.dp2.BIB.Random.BibReaderFactoryImpl");
			System.setProperty("it.polito.dp2.BIB.ass2.CitationFinderFactory", "it.polito.dp2.BIB.sol2.CitationFinderFactory");
			System.setProperty("it.polito.dp2.BIB.ass2.PORT", "7474");
			System.setProperty("it.polito.dp2.BIB.ass2.URL", "http://localhost:7474/db");
			
			CitationFinder citationFinder = CitationFinderFactory.newInstance().newCitationFinder();
			Set<ItemReader> items = citationFinder.getItems(null, 0, 3000);
			for (ItemReader item : items) {
				Set<ItemReader> citingElements = citationFinder.findAllCitingItems(item, 1);
				System.out.println("ITEM " + item.getTitle() + " CITED BY : " + item.getCitingItems().size());
				System.out.println("ITEM " + item.getTitle() + " CITING TREE SIZE : " + citingElements.size());
				for (ItemReader ce : citingElements) {
					System.out.println("  -->  " + ce.getTitle());
				}
				System.out.println();
			}
			
			citationFinder.findAllCitingItems(
					new ItemReader() {
						@Override
						public String[] getAuthors() {
							String[] retval = new String[1];
							retval[0]="Author";
							return retval;
						}

						@Override
						public Set<ItemReader> getCitingItems() {
							return new HashSet<ItemReader>();
						}

						@Override
						public String getSubtitle() {
							return null;
						}

						@Override
						public String getTitle() {
							return null;
					}}, 
					1
				);
			
		} catch (CitationFinderException | FactoryConfigurationError | UnknownItemException | ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
