package org.csstudio.utility.rocs;


import java.util.logging.Logger;

import org.eclipse.core.runtime.jobs.Job;

/**
 * ScanServerClient that takes the configuration from the CSS preferences.
 * 
 * 
 */
public class ROCSFromPreferences extends TemplateXMLFactory {

    private static final Logger log = Logger
	    .getLogger(ROCSFromPreferences.class.getName());

    public ROCSFromPreferences() {
	reloadConfiguration();
    }

    public void reloadConfiguration() {
	try {
		Job job = new ROCSJob ("ROCS Templates");
		job.schedule();
	} catch (Exception e) {
	    log.severe(e.getMessage());
	}

    }



   
}
