package org.csstudio.service.rocs;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

public class PropertyCollectionAdapter extends XmlAdapter<Object,Map<String,String>> {

	@Override
	public Object marshal(Map<String,String> v) throws Exception {
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.newDocument();
			Element customXml = doc.createElement("property2");
			for (Map.Entry<String, String> property : v.entrySet()) {
				if (property.getKey() != null) {
					Element keyValuePair = doc.createElement(property.getKey());
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
	public Map<String,String> unmarshal(Object v) throws Exception {
		Map<String,String> properties = new HashMap<String,String>();
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
