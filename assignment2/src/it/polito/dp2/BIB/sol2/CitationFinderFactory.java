package it.polito.dp2.BIB.sol2;

import java.util.Set;

import it.polito.dp2.BIB.BibReader;
import it.polito.dp2.BIB.BibReaderException;
import it.polito.dp2.BIB.BibReaderFactory;
import it.polito.dp2.BIB.FactoryConfigurationError;
import it.polito.dp2.BIB.ItemReader;
import it.polito.dp2.BIB.ass2.CitationFinder;
import it.polito.dp2.BIB.ass2.CitationFinderException;

/*
 * Created by rolud
 */
public class CitationFinderFactory extends it.polito.dp2.BIB.ass2.CitationFinderFactory {

	@Override
	public CitationFinder newCitationFinder() throws CitationFinderException {
		
		BibReader reader;
		CitationFinderImpl cfi = new CitationFinderImpl();
			
		
		// TODO Auto-generated method stub
		return cfi;
	}

}
