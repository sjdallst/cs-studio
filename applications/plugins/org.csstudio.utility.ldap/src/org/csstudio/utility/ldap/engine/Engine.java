/*
 * Copyright (c) 2006 Stiftung Deutsches Elektronen-Synchrotron,
 * Member of the Helmholtz Association, (DESY), HAMBURG, GERMANY.
 *
 * THIS SOFTWARE IS PROVIDED UNDER THIS LICENSE ON AN "../AS IS" BASIS.
 * WITHOUT WARRANTY OF ANY KIND, EXPRESSED OR IMPLIED, INCLUDING BUT NOT LIMITED
 * TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR PARTICULAR PURPOSE AND
 * NON-INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR
 * THE USE OR OTHER DEALINGS IN THE SOFTWARE. SHOULD THE SOFTWARE PROVE DEFECTIVE
 * IN ANY RESPECT, THE USER ASSUMES THE COST OF ANY NECESSARY SERVICING, REPAIR OR
 * CORRECTION. THIS DISCLAIMER OF WARRANTY CONSTITUTES AN ESSENTIAL PART OF THIS LICENSE.
 * NO USE OF ANY SOFTWARE IS AUTHORIZED HEREUNDER EXCEPT UNDER THIS DISCLAIMER.
 * DESY HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS,
 * OR MODIFICATIONS.
 * THE FULL LICENSE SPECIFYING FOR THE SOFTWARE THE REDISTRIBUTION, MODIFICATION,
 * USAGE AND OTHER RIGHTS AND OBLIGATIONS IS INCLUDED WITH THE DISTRIBUTION OF THIS
 * PROJECT IN THE FILE LICENSE.HTML. IF THE LICENSE IS NOT INCLUDED YOU MAY FIND A COPY
 * AT HTTP://WWW.DESY.DE/LEGAL/LICENSE.HTM
 */
package org.csstudio.utility.ldap.engine;

import java.text.SimpleDateFormat;
import java.util.Vector;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.csstudio.utility.ldap.Activator;
import org.csstudio.utility.ldap.connection.LDAPConnector;
import org.csstudio.utility.ldap.preference.PreferenceConstants;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;

public class Engine extends Job {

	public Engine(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}
	private static 		Engine thisEngine = null;
	private boolean 	doWrite = false;
	private DirContext 	ctx;
	private Vector<WriteRequest>	writeVector = new Vector<WriteRequest>();
	/**
	 * @param args
	 */

	protected IStatus run(IProgressMonitor monitor) {
		Integer intSleepTimer = null;

		//
		// initialize LDAP connection (dir context
		//
//		ctx = LDAPReader.initial();
		// TODO:
		/*
		 *  create message ONCE
		 *  retry forever if ctx == null
		 *  BUT do NOT block caller (calling sigleton)
		 *  submit ctx = new LDAPConnector().getDirContext(); to 'background process'
		 *
		 */
		System.out.println("$$$$$$$$$$$$$$$$$$$$$ Engine.run - start");
		ctx = new LDAPConnector().getDirContext();
		System.out.println("##################### Engine.run - ctx: " + ctx.toString());
		if ( ctx  != null) {
			System.out.println("Engine.run - successfully connected to LDAP server");
		} else {
			System.out.println("Engine.run - connection to LDAP server failed");
		}

		while (true) {
			//
			// do the work actually prepared
			//
			if (doWrite) {
				System.out.println("Engine.run - performLdapWrite");
				performLdapWrite();
			}
			/*
        	 * sleep before we check for work again
        	 */
//			System.out.println("Engine.run - waiting...");
        	try {
        		if(Activator.getDefault().getPluginPreferences().getString(PreferenceConstants.SECURITY_PROTOCOL).trim().length()>0) {
        			intSleepTimer = new Integer(Activator.getDefault().getPluginPreferences().getString(PreferenceConstants.SECURITY_PROTOCOL));
        		} else {
        			intSleepTimer = 100; //default
        		}
        		Thread.sleep( (long)intSleepTimer );
        	}
        	catch (InterruptedException  e) {
        		return null;
        	}
		}
	}


    public static Engine getInstance() {
		//
		// get an instance of our sigleton
		//
		if ( thisEngine == null) {
			synchronized (Engine.class) {
				if (thisEngine == null) {
					thisEngine = new Engine("LdapEngine");
					thisEngine.schedule();
					System.out.println("Engine.getInstance - exit");
				}
			}
		}
		return thisEngine;
	}

    synchronized public void addLdapWriteRequest(String attribute, String channel, String value) {
    	boolean addVectorOK = true;
		WriteRequest writeRequest = new WriteRequest( attribute, channel, value);
		//
		// add request to vector
		//
		int bufferSize = writeVector.size();
		System.out.println("Engine.addLdapWriteRequest actual buffer size: " + bufferSize);
		if ( bufferSize > 10000) {
			if (addVectorOK) {
				System.out.println("Engine.addLdapWriteRequest writeVector > 500 - cannot store more!");
				addVectorOK = false;
			}
		} else {
			if ( ! addVectorOK) {
				System.out.println("Engine.addLdapWriteRequest writeVector - continue writing");
				addVectorOK = true;
			}
			writeVector.add(writeRequest);
			doWrite = true;
		}
	}

    private void performLdapWrite() {
//    	int size= writeVector.size();
    	ModificationItem[] modItem = new ModificationItem[100];
    	int i = 0;
    	String channel;
    	channel = null;
    	i = 0;

    	while(writeVector.size()>0){
    			WriteRequest writeReq = writeVector.firstElement();
    		//
    		// prepare LDAP request for all entries matching the same channel
    		//
    		if ( channel == null) {
    			// first time setting
    			channel = writeReq.getChannel();
    		}
    		if ( !channel.equals(writeReq.getChannel())){
    			System.out.print("write: ");
    			changeValue("eren", channel, modItem);
    			System.out.println(" finisch!!!");
    			modItem = new ModificationItem[100];
    			i = 0;
    			//
    			// define next channel name
    			//
    			channel = writeReq.getChannel();
    		}
			//
			// combine all items that are related to the same channel
			//
			BasicAttribute ba = new BasicAttribute(	writeReq.getAttribute(), writeReq.getValue());
			modItem[i++] = new ModificationItem( DirContext.REPLACE_ATTRIBUTE,ba);
			writeVector.remove(0);
			if(writeVector.size()<20||(writeVector.size()%10)==0)
				System.out.println("Engine.performLdapWrite buffer size: "+writeVector.size());
		}
    	//
    	// still something left to do?
    	//
    	System.out.println("Vector leer!!!!");
    	if (i != 0 ) {
    		//
			try {
				System.out.println("Engine.performLdapWrite write last modItem (: "+modItem+")" );
				changeValue("eren", channel, modItem);
				System.out.println("+++++++++++++++++++++++++++++++++++++++ d o n e +++++++++++++++++++++++++++++++++++++++");
			}
			 catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					//
					// too bad it did not work
					doWrite = false;	// wait for next time
					return;
				}
    	}

    	doWrite = false;
    	System.out.println("Engine.performLdapWrite\r\n  -------------------------------------- E N D --------------------------------------");
    }

    /**
	 * @param string
	 * @param channel
	 * @param modItemTemp
	 */
	private void changeValue(String string, String channel, ModificationItem[] modItem) {
		int j=0;
		// Delete null values and make rigth size
		for(;j<modItem.length;j++){
			if(modItem[j]==null)
				break;
		}
//		System.out.println("Enter Engine.changeValue with: " + channel);
		ModificationItem modItemTemp[] = new ModificationItem[j];
		for(int n = 0;n<j;n++){
			modItemTemp[n] = modItem[n];
		}
		//
		// channel name changed
		// first write all values
		//
        SearchControls ctrl = new SearchControls();
        ctrl.setSearchScope(SearchControls.SUBTREE_SCOPE);
		try {
			NamingEnumeration<SearchResult> results = ctx.search("",string+"=" + channel,ctrl);
//			System.out.println("Enter Engine.changeValue results for channnel: " + channel );
			while(results.hasMore()) {
//				System.out.println("Enter Engine.changeValue in while channnel: " + channel );
				String ldapChannelName = results.next().getNameInNamespace();
				if(ldapChannelName.endsWith(",o=DESY,c=DE")){
					ldapChannelName=ldapChannelName.substring(0,ldapChannelName.length()-12);
				}
    			try {
    				ctx.modifyAttributes(ldapChannelName, modItemTemp);
    			} catch (NamingException e) {
    				e.printStackTrace();
    				//
    				// too bad it did not work
    				doWrite = false;	// wait for next time
    				return;
    			}catch (Exception e) {
    				e.printStackTrace();
    				//
    				// too bad it did not work
    				doWrite = false;	// wait for next time
    				return;
				}

//    			//
//    			// reset for to get ready for values of next channel
//    			//
//    			modItemTemp = new ModificationItem[writeVector.size()];
			}

		} catch (NamingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}


	}


	public void setLdapValue ( String channel, String severity, String status, String timeStamp) {
		ModificationItem epicsStatus, epicsSeverity, epicsTimeStamp, epicsAcknowledgeTimeStamp ;
		ModificationItem[] modItem = null;
		int i = 0;

		String channelName = "eren=" + channel;

		//
		// change severity if value is entered
		//
		if ((severity != null)&& (severity.length() > 0)) {
			epicsSeverity = new ModificationItem( DirContext.REPLACE_ATTRIBUTE,	new BasicAttribute(	"epicsAlarmSeverity", severity));
			modItem[i++] = epicsSeverity;
		}

		//
		// change status if value is entered
		//
		if ((status != null) && (status.length() > 0)) {
			epicsStatus = new ModificationItem( DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("epicsAlarmStatus", status));
		}

		//
		// change alarm time stamp
		//
		if ((timeStamp != null) && (timeStamp.length() > 0)) {
			epicsTimeStamp = new ModificationItem( DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("epicsAlarmTimeStamp", timeStamp));
		}

		//
		// change time stamp acknowledged time
		//
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:S");
        java.util.Date currentDate = new java.util.Date();
        String eventTime = sdf.format(currentDate);

        epicsAcknowledgeTimeStamp = new ModificationItem( DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("epicsAlarmAcknTimeStamp", eventTime));

        try {
			ctx.modifyAttributes(channelName, modItem);
		} catch (NamingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


    }
    private class WriteRequest {
    	private String 	attribute = null;
    	private String 	channel	= null;
    	private String	value = null;

    	public WriteRequest ( String attribute, String channel, String value) {

    		this.attribute = attribute;
    		this.channel = channel;
    		this.value = value;
    	}

    	public String getAttribute () {
    		return this.attribute;
    	}

    	public String getChannel () {
    		return this.channel;
    	}

    	public String getValue () {
    		return this.value;
    	}

    }


}
