package org.csstudio.rocs.widgets;

import java.util.ArrayList;
import java.util.Collection;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name="properties") 
public class XMLPropertyCollectionSet {
	
	@XmlAttribute
	private String id;

	@XmlElement(name="properties")
	@XmlJavaTypeAdapter(PropertyCollectionAdapterSet.class) 
	private Collection<XMLPropertyCollection> property = new ArrayList<XMLPropertyCollection>();

	
	public XMLPropertyCollectionSet() {
	}

	public XMLPropertyCollectionSet(String id, Collection<XMLPropertyCollection> property) {
		this.id = id;
		this.property = property;
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Collection<XMLPropertyCollection> getProperty() {
		return property;
	}

	public void setProperty(Collection<XMLPropertyCollection> property) {
		this.property = property;
	}

	
}
