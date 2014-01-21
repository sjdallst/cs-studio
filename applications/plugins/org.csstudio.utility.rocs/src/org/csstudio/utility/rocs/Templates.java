package org.csstudio.utility.rocs;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;
import org.w3c.dom.Document;


public class Templates {

	private Templates(){
		
	}
	
	private static ExecutorService defaultExecutor = Executors.newSingleThreadExecutor(Templates.namedPool("ROCS Template"));
	static Pattern namePattern = Pattern.compile("(.*)_header");
	
	
	public static Template createFromXml(String name, InputStream input){
		try {

			TransformerFactory tf = TransformerFactory.newInstance();
			Bundle bundle = Platform.getBundle("org.csstudio.utility.rocs");
			Path path = new Path("src/org/csstudio/utility/rocs/MacroReplace.xsl");
			URL fileURL = FileLocator.find(bundle, path, null);
			InputStream in = fileURL.openStream();
			StreamSource xslt = new StreamSource(in);
			Transformer transformer = tf.newTransformer(xslt);

			ByteArrayOutputStream os = new ByteArrayOutputStream();
			StreamResult result = new StreamResult(os);
			//Matcher matcher = namePattern.matcher(name);

			transformer.setParameter("templateName", name);
			transformer.transform(new StreamSource(input), result);

			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			DocumentBuilder db = factory.newDocumentBuilder();
			Document cachedTemplate = db.parse(new ByteArrayInputStream(os.toByteArray()));

			TemplateDescription templateDescription = new TemplateDescription(name);
			
			templateDescription.executorService = defaultExecutor;

			templateDescription.setTemplate(cachedTemplate);

			

			return new Template(templateDescription);
    	} catch (Exception ex) {
    		Logger.getLogger(Templates.class.getName()).log(Level.FINEST, "Couldn't create template", ex);
    		throw new IllegalArgumentException("Couldn't create template", ex);
    	}
	}
	
	public static ThreadFactory namedPool(String poolName) {
        return new DefaultThreadFactory(poolName);
    }
    
    /**
     * Taken from {@link Executors#defaultThreadFactory() }.
     */
    static class DefaultThreadFactory implements ThreadFactory {
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        DefaultThreadFactory(String poolName) {
            SecurityManager s = System.getSecurityManager();
            group = (s != null)? s.getThreadGroup() :
                                 Thread.currentThread().getThreadGroup();
            namePrefix = poolName;
        }

        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r,
                                  namePrefix + threadNumber.getAndIncrement(),
                                  0);
            if (!t.isDaemon())
                t.setDaemon(true);
            if (t.getPriority() != Thread.NORM_PRIORITY)
                t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    }
}
