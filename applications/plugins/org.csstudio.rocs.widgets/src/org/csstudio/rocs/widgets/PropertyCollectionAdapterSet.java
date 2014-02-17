package org.csstudio.rocs.widgets;

import gov.bnl.channelfinder.api.Property;

import java.util.Collection;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class PropertyCollectionAdapterSet extends XmlAdapter<Object,Collection<XMLPropertyCollection>> {

	@Override
	public Object marshal(Collection<XMLPropertyCollection> arg0)
			throws Exception {
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.newDocument();
			Element customXml = doc.createElement("properties");
			for (XMLPropertyCollection property : arg0) {
				if (property.getId() != null) {
					Element propElement = doc.createElement("property");
					propElement.setAttribute("id", property.getId());
					for(Property prop :property.getProperties()){
						Element keyValuePair = doc.createElement(prop.getName());
					if (prop.getValue() == null) {
						keyValuePair.appendChild(doc.createTextNode(""));
					} else {
						keyValuePair.appendChild(doc.createTextNode(prop.getValue()));
					}
						propElement.appendChild(keyValuePair);
					}
					customXml.appendChild(propElement);
					
				}
			}
			return customXml;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public Collection<XMLPropertyCollection> unmarshal(Object arg0)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
