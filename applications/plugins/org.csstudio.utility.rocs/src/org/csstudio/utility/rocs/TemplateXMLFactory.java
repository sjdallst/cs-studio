package org.csstudio.utility.rocs;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.Platform;

public class TemplateXMLFactory {

		private final static ExecutorService defaultExecutor = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors(), Templates.namedPool("ROCS Template Init"));
	    private final File directory;
	    private final List<File> files = new ArrayList<>();

	    public TemplateXMLFactory(){
	    	this.directory = getTemplateDirectory();
	        if (directory.exists() && !directory.isDirectory()) {
	            throw new IllegalArgumentException("Path provided is not a directory (" + directory + ")");
	        }
	        files.addAll(getTemplateFiles());
	    }
	    /**
	     * Creates a new factory that reads from the given directory.
	     * <p>
	     * If the directory does not exist, it simply returns an empty set.
	     * 
	     * @param directory a directory
	     */
	    public TemplateXMLFactory(File directory) {
	        this.directory = directory;
	        if (directory.exists() && !directory.isDirectory()) {
	            throw new IllegalArgumentException("Path provided is not a directory (" + directory + ")");
	        }
	        files.addAll(getTemplateFiles());
	    }
	    
	    public static ExecutorService getExecutorService(){
	    	return defaultExecutor;
	    }
	    /**
	     * Crawls the directory and creates templates.
	     * <p>
	     * XML files that do not parse correctly are skipped.
	     * 
	     * @return the created Templates
	     */
	    public Collection<Template> createTemplates() {
	    	List<Template> templates = new ArrayList<>(); 

	    	CompletionService<Template> completionService = new ExecutorCompletionService<Template>(defaultExecutor);
	    	for (File file : files) {
	    		completionService.submit(new CreateTemplate(file));
	    	}

	    	try{
	    		for(int t=0, n=files.size(); t<n; t++){ 
	    			Future<Template> f = completionService.take();
	    			templates.add(f.get());
	    		}
	    	} catch (InterruptedException e){
	    		Thread.currentThread().interrupt();
	    	} catch (ExecutionException e) {
	    		Logger.getLogger(Templates.class.getName()).log(Level.INFO, "Failed creating template from ", e);
	    	}
	    	

	    	return templates;
	    }
	    
		public static File getTemplateDirectory() {
			File productDirectory = new File(Platform.getInstallLocation().getURL().getFile() + "/configuration/rocs/templates");
			Logger.getLogger(TemplateXMLFactory.class.getName()).log(Level.CONFIG, "Reading Template configuration directory " + productDirectory);
			return productDirectory;
		}
		
		public List<File> getFiles() {
			return files;
		}
		
		private List<File> getTemplateFiles(){
			List<File> files = new ArrayList<>();
			if (directory.exists()) {
				for (File file : directory.listFiles()) {
					if (file.getName().endsWith(".opi")) {
						files.add(file);
					}
				}
			} else {
				Logger.getLogger(Templates.class.getName()).log(Level.WARNING, "Directory " + directory + " does not exist");
			}
			return files;
		}

		private class CreateTemplate implements Callable<Template>{
			private File file;
			
			CreateTemplate(File file){
				this.file = file;
			}
			@Override
			public Template call() throws Exception {
				return Templates.createFromXml(file.getName().replace(".opi", ""), new FileInputStream(file));
			}
			
		}
	    
	
}
