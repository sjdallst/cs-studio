package org.csstudio.rocs.widgets;

import gov.bnl.channelfinder.api.Property;

import java.util.ArrayList;
import java.util.Collection;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

public class PropertyCollectionAdapter extends XmlAdapter<Object,Collection<Property>> {

	@Override
	public Object marshal(Collection<Property> v) throws Exception {
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.newDocument();
			Element customXml = doc.createElement("property2");
			for (Property property : v) {
				if (property.getName() != null) {
					Element keyValuePair = doc.createElement(property.getName());
					if (property.getValue() == null) {
						keyValuePair.appendChild(doc.createTextNode(""));
					} else {
						keyValuePair.appendChild(doc.createTextNode(property.getValue()));
					}
					customXml.appendChild(keyValuePair);
				}
			}
			return customXml;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;

	}

	@Override
	public Collection<Property> unmarshal(Object v) throws Exception {
		Collection<Property> properties = new ArrayList<Property>();
		Element content = (Element) v;
		NodeList childNodes = content.getChildNodes();
		if (childNodes.getLength() > 0) {
		   for (int i = 0; i < childNodes.getLength(); i++)
		     {                              
		         Node child =childNodes.item(i);                              
		         String key = child.getNodeName();                              
		         // Skip text nodes                              
		         if (key.startsWith("#"))continue;                              
		         String value=((Text)child.getChildNodes().item(0))
		             .getWholeText();
		         if(value.contentEquals("")){
		                    value=null;
		         }                     
		        // properties.add(Property.Builder.property(key, value));                       
		      }              
		   }              
		   return properties;
	}



}
