package it.polito.dp2.BIB.sol2;

import java.math.BigInteger;

import it.polito.dp2.BIB.sol2.crossref.client.jaxb.Author;
import it.polito.dp2.BIB.sol2.crossref.client.jaxb.Items;
import it.polito.dp2.xml.biblio.Factory;
import it.polito.dp2.xml.biblio.PrintableItem;
import it.polito.pad.dp2.biblio.BiblioItemType;
import it.polito.pad.dp2.biblio.BookType;

/*
 * Created by rolud
 */
public class CrossrefFactory extends Factory {
	public static PrintableItem createPrintableItem(BigInteger id, Items crossrefItem) {
		BiblioItemType item = new BiblioItemType();
		item.setId(id);
		if (crossrefItem.getTitle().size() != 0)
			item.setTitle(crossrefItem.getTitle().get(0));
		if (crossrefItem.getSubtitle().size() != 0)
			item.setSubtitle(crossrefItem.getSubtitle().get(0));
		for(Author a : crossrefItem.getAuthor()) {
			item.getAuthor().add(a.getGiven() + " " + a.getFamily());
		}
		
		BookType book = new BookType();
		book.setPublisher(crossrefItem.getPublisher());
		book.setYear(crossrefItem.getCreated().getDateTime());
		if (crossrefItem.getISBN().size() != 0)
			book.setISBN(crossrefItem.getISBN().get(0));
		item.setBook(book);
		return createPrintableItem(item);
	}
}
