package org.csstudio.utility.rocs;

import java.util.concurrent.ExecutorService;

import org.w3c.dom.Document;




public class Template {
    
    private final String name;
    private final ExecutorService executorService;
    private final Document template;
    private final Document header;

    public Template(TemplateDescription templateDescription) {
        this.name = templateDescription.name;
        this.executorService = templateDescription.executorService;
        this.template = templateDescription.getTemplate();
        this.header = templateDescription.getHeader();
    }
    
    public Template(Template template){
    	this.name = template.name;
    	this.executorService = template.executorService;
    	this.template = template.template;
    	this.header = template.header;
    }

	public final String getName() {
        return name;
    }

    public ExecutorService getExecutorService() {
		return executorService;
	}

	public final Document getTemplate() {
        return template;
    }
	
	public final Document getHeader() {
        return header;
    }
}
