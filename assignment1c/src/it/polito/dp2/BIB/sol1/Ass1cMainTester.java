package it.polito.dp2.BIB.sol1;

import java.util.Set;

import it.polito.dp2.BIB.BibReader;
import it.polito.dp2.BIB.BibReaderException;
import it.polito.dp2.BIB.ItemReader;
import it.polito.dp2.BIB.JournalReader;

public class Ass1cMainTester {
	public static void main (String args[]) throws BibReaderException {
		System.setProperty("it.polito.dp2.BIB.BibReaderFactory", "it.polito.dp2.BIB.sol1.BibReaderFactory");
		System.setProperty("it.polito.dp2.BIB.sol1.BibInfo.file", "xsd/biblio_e.xml");
		BibReader tester = BibReaderFactory.newInstance().newBibReader();
		
		Set<ItemReader> items = tester.getItems(null, 0, 9999);
		Set<JournalReader>  journals = tester.getJournals(null);
		
		System.out.println("ITEMS SIZE " + items.size());
		System.out.println("JOURNALS SIZE " + journals.size());
		
	}

}
