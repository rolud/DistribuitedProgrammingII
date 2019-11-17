package it.polito.dp2.BIB.sol1;

import it.polito.dp2.BIB.BibReader;
import it.polito.dp2.BIB.BibReaderException;

public class Ass1cMainTester {
	public static void main (String args[]) throws BibReaderException {
		System.setProperty("it.polito.dp2.BIB.BibReaderFactory", "it.polito.dp2.BIB.sol1.BibReaderFactory");
		System.setProperty("it.polito.dp2.BIB.sol1.BibInfo.file", "xsd/biblio_e.xml");
		BibReader tester = BibReaderFactory.newInstance().newBibReader();
		
		
	}

}
