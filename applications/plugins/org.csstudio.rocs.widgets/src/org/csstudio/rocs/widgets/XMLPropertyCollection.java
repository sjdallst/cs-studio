package org.csstudio.rocs.widgets;

import gov.bnl.channelfinder.api.Property;

import java.util.ArrayList;
import java.util.Collection;

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.bind.annotation.XmlAccessType;

@XmlAccessorType(XmlAccessType.FIELD)
public class XMLPropertyCollection implements Comparable<XMLPropertyCollection>{
	
	@XmlAttribute
	private String id;
	
	@XmlElement(name="property")
	@XmlJavaTypeAdapter(PropertyCollectionAdapter.class) 
	private Collection<Property> properties = new ArrayList<Property>();

	
	public XMLPropertyCollection() {
	}

	public XMLPropertyCollection(String id, Collection<Property> properties) {

		this.id = id;
		this.properties = properties;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Collection<Property> getProperties() {
		return properties;
	}

	public void setProperties(Collection<Property> properties) {
		this.properties = properties;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		XMLPropertyCollection other = (XMLPropertyCollection) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}


	@Override
	public int compareTo(XMLPropertyCollection o) {
		int x = id.compareTo(o.id);
		return x;
	}

	
	
	
	

}
