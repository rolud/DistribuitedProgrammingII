package it.polito.dp2.BIB.sol1;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.SAXException;

import static javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI;

import java.io.File;

import it.polito.dp2.BIB.BibReader;
import it.polito.dp2.BIB.BibReaderException;
import it.polito.dp2.BIB.sol1.jaxb.Biblio;

public class BibReaderFactory extends it.polito.dp2.BIB.BibReaderFactory {

	private BibReaderImpl monitor;
	
	@Override
	public BibReader newBibReader() throws BibReaderException {
		// TODO Auto-generated method stub
		
				
		String filename = System.getProperty("it.polito.dp2.BIB.sol1.BibInfo.file");
		System.out.println("FILENAME " + filename);
		if (filename != null) {
			try {
				JAXBContext jc = JAXBContext.newInstance("it.polito.dp2.BIB.sol1.jaxb");
				Unmarshaller u = jc.createUnmarshaller();
				
				SchemaFactory sf = SchemaFactory.newInstance(W3C_XML_SCHEMA_NS_URI);
				Schema schema = sf.newSchema(new File("xsd/biblio_e.xsd"));
				u.setSchema(schema);
				
				Biblio biblio = (Biblio) u.unmarshal(new File(filename));
				
				monitor = new BibReaderImpl(biblio);
				
				
			} catch (JAXBException | SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return monitor;
	}

}
