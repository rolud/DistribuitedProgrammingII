package it.polito.dp2.BIB.sol3.model;

import java.util.List;

import javax.ws.rs.core.UriBuilder;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import it.polito.dp2.BIB.sol3.service.jaxb.Bookshelf;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "self",
    "name",
    "items"
})
@XmlRootElement(name = "bookshelf")
public class EBookshelf extends Bookshelf {

	transient private UriBuilder root;
	transient private UriBuilder items;
	
	public EBookshelf() {
		super();
	}
	
	public EBookshelf(UriBuilder root) {
		this.root = root;
		items = root.clone().path("items");
	}
	
	@Override
    @XmlElement(required = true)
    @XmlSchemaType(name = "anyURI")
	public String getItems() {
		return items.toTemplate();
	}	
	
	@Override
    @XmlElement(required = true)
    @XmlSchemaType(name = "anyURI")
	public String getSelf() {
		return root.toTemplate();
	}
}
