package org.csstudio.utility.rocs;


import java.util.concurrent.ExecutorService;
import java.util.regex.Pattern;

import org.w3c.dom.Document;



public class TemplateDescription {
	
    final String name;
    ExecutorService executorService;
    private Document template;
    private Document header;
    static Pattern namePattern = Pattern.compile("[a-zA-Z_]\\w*");

    public TemplateDescription(String name) {
        this.name = name;
        if (!namePattern.matcher(name).matches()) {
            throw new IllegalArgumentException("Name must start by a letter and only consist of letters and numbers");
        }
    }
    
    public TemplateDescription setTemplate(Document template) {
    	this.template = template;
        return this;
    }
    
    public Document getTemplate(){
    	return template;
    }
    
    public TemplateDescription setHeader(Document header) {
    	this.header = header;
        return this;
    }
    
    public Document getHeader(){
    	return header;
    }
    
}
