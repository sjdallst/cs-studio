package org.csstudio.utility.rocs.ui;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URL;
import java.security.SecureRandom;
import java.util.concurrent.Callable;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.util.JAXBSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.csstudio.openfile.DisplayUtil;
import org.csstudio.utility.file.IFileUtil;
import org.csstudio.utility.rocs.Template;
import org.csstudio.utility.rocs.TemplateRegistry;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.osgi.framework.Bundle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ROCSFactory {

	private static IJobManager jobMan = Job.getJobManager();
	
	public static void create(String TemplateName, JAXBSource jaxbSource, RunOPI option) {

				try{
					Job[] build = jobMan.find("ROCS Templates"); 
					if (build.length == 1)
						build[0].join();
					Template template = TemplateRegistry.getDefault().findTemplate(TemplateName);
					if(template==null)
						throw new IllegalArgumentException("Couldn't find template "+TemplateName);
					
					//IFile resource = "get resource somehow";
					//  if (resource != null) resource.refreshLocal(IResource.DEPTH_INFINITE, monitor);
					  
					InputStream inputStream = template.getExecutorService().submit(new DisplayTemplate(jaxbSource,template)).get();
					IFileUtil fileUtil = IFileUtil.getInstance();
					IWorkbench workbench = PlatformUI.getWorkbench();
					SecureRandom random = new SecureRandom();
					String fileName = TemplateName+"_"+new BigInteger(32, random).toString(32)+".opi";
					
					IFile ifile = fileUtil.createFileResource(fileName, inputStream);
					IEditorDescriptor desc = workbench.getEditorRegistry().getDefaultEditor(ifile.getName());
					IWorkbenchPage page = workbench.getActiveWorkbenchWindow().getActivePage();
					
					// default: desc.getId() org.csstudio.opibuilder.OPIRunner
					// org.csstudio.opibuilder.OPIEditor
					// org.csstudio.opibuilder.opiView
					IEditorPart part;
					switch (option) {
					case ATTACHED:
						part = page.openEditor(new FileEditorInput(ifile), "org.csstudio.opibuilder.OPIRunner");
						IFileUtil.getInstance().registerPart(part, ifile);
						break;
					case DETACHED:
						DisplayUtil.getInstance().openDisplay(ifile.getFullPath().toOSString(),"Position=Detached");
//						RunModeService.getInstance().runOPIInView(ifile.getFullPath(), 
//								new DisplayOpenManager(null), null, Position.DETACHED);
						break;
					case EDIT:
						part = page.openEditor(new FileEditorInput(ifile), "org.csstudio.opibuilder.OPIEditor");
						IFileUtil.getInstance().registerPart(part, ifile);
						break;
					default:
						part = page.openEditor(new FileEditorInput(ifile), desc.getId());
						IFileUtil.getInstance().registerPart(part, ifile);
						break;	
					}

					
				} catch (Exception e){
					// clean up opi file if it didn't get registered
					//errorBar.setException(e);
				}	
	}
	
	private static class DisplayTemplate implements Callable<InputStream> {
		private JAXBSource jaxbSource;
		private Template template;

		DisplayTemplate(JAXBSource jaxbSource, Template template){
			this.jaxbSource = jaxbSource;
			this.template = template;
		}

		@Override
		public InputStream call() throws Exception {

			//JAXBContext jc = JAXBContext.newInstance(XMLPropertyCollectionSet.class, XMLPropertyCollection.class);
			
			Bundle bundle = Platform.getBundle("org.csstudio.utility.rocs.ui");
			Path path = new Path(
					"src/org/csstudio/utility/rocs/ui/stylesheet.xslt");
			URL fileURL = FileLocator.find(bundle, path, null);
			InputStream stylesheetFile = fileURL.openStream();
			
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			DocumentBuilder db = factory.newDocumentBuilder();
		    Document cachedTemplate = template.getTemplate();
		    Document cachedHeaderTemplate = TemplateRegistry.getDefault().findTemplate(template.getName()+"_header").getTemplate();
		    Document cachedTemplateStylesheet = db.parse(stylesheetFile);
			
			Document stylesheet = (Document) cachedTemplateStylesheet.cloneNode(true);
			Element template = (Element) stylesheet.getElementsByTagName("xsl:stylesheet").item(0);
			Node opiHeaderTemplate = cachedHeaderTemplate.getElementsByTagName("opitemplate").item(0);
			for(int i=0; i<opiHeaderTemplate.getChildNodes().getLength();i++){
				template.appendChild(stylesheet.importNode(opiHeaderTemplate.getChildNodes().item(i),true));
			}
			Node opiTemplate = cachedTemplate.getElementsByTagName("opitemplate").item(0);
			for(int i=0; i<opiTemplate.getChildNodes().getLength();i++){
				template.appendChild(stylesheet.importNode(opiTemplate.getChildNodes().item(i),true));
			}

			
			TransformerFactory tf = TransformerFactory.newInstance();
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			StreamResult result = new StreamResult(os);
			
			Transformer transformer = tf.newTransformer(new DOMSource(stylesheet));
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
	        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
	        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
	        // new JAXBSource(jc,entry)
	        transformer.transform(jaxbSource,result);
			
			return new ByteArrayInputStream(os.toByteArray());
		}
	}
}
