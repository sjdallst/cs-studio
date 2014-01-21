package org.csstudio.utility.rocs;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class TemplateRegistry {
    private final static TemplateRegistry registry = new TemplateRegistry();
    
    private Map<String, Template> templates = new ConcurrentHashMap<>();
    

    public static TemplateRegistry getDefault() {
        return registry;
    }
    
    public void registerTemplate(Template template) {
        templates.put(template.getName(), template);
    }
    
    
    public Set<String> listTemplates() {
        return Collections.unmodifiableSet(new HashSet<>(templates.keySet()));
    }
    
    public Template findTemplate(String name) {
        return templates.get(name);
    }
}
