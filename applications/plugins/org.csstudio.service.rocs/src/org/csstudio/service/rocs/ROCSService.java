package org.csstudio.service.rocs;

import org.epics.pvmanager.service.Service;
import org.epics.pvmanager.service.ServiceDescription;

public class ROCSService extends Service {
    /**
     * @param serviceDescription
     */
    public ROCSService() {
	super(new ServiceDescription("rocs", "ROCS template service")
	.addServiceMethod(new TableMethod()));	
    }
}
