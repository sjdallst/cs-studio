package org.csstudio.utility.rocs;


import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.IPreferencesService;

public class ROCSJob extends Job {
	public static final String JOB_FAMILY_NAME = "ROCS Templates";
	private final IPreferencesService prefs = Platform.getPreferencesService();
	
	public ROCSJob(String name) {
		super(name);
	}

	public boolean belongsTo(Object family) {
		return JOB_FAMILY_NAME.equals(family);
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {

		try {
			
			String directories = prefs.getString(Activator.PLUGIN_ID,
					PreferenceConstants.Template_Directories,
					Platform.getInstallLocation().getURL().getFile() + "/configuration/rocs/templates", null);

			for (String directoryString : directories.split(";")) {
				Path path = new Path(directoryString);
			    IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
			    // retrieve the full system path:
			    String osfile = file.getRawLocation().toOSString();
				File directory = new File(osfile);
				if (directory.exists() && !directory.isDirectory()) {
		            throw new IllegalArgumentException("Path provided is not a directory (" + directory + ")");
		        }
				TemplateXMLFactory templateXMLFactory = new TemplateXMLFactory(directory);
				monitor.beginTask("Registering templates", 100);
				for (Template template : templateXMLFactory.createTemplates()) {
					monitor.subTask("Loading ...");
					TemplateRegistry.getDefault().registerTemplate(template);
					monitor.worked(1 / templateXMLFactory.getFiles().size() * 100);
					if (monitor.isCanceled())
						return Status.CANCEL_STATUS;
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			return Status.CANCEL_STATUS;
		} finally {
			monitor.done();
		}
		return Status.OK_STATUS;
	}

}
