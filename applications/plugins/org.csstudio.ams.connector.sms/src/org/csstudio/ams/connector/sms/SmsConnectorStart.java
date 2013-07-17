
/* 
 * Copyright (c) 2008 Stiftung Deutsches Elektronen-Synchrotron, 
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
 
package org.csstudio.ams.connector.sms;

import java.net.InetAddress;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import org.csstudio.ams.AmsActivator;
import org.csstudio.ams.AmsConstants;
import org.csstudio.ams.Log;
import org.csstudio.ams.SynchObject;
import org.csstudio.ams.Utils;
import org.csstudio.ams.connector.sms.internal.SmsConnectorPreferenceKey;
import org.csstudio.ams.internal.AmsPreferenceKey;
import org.csstudio.platform.utility.jms.JmsSimpleProducer;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.jface.preference.IPreferenceStore;
import org.remotercp.common.tracker.IGenericServiceListener;
import org.remotercp.service.connection.session.ISessionService;

public class SmsConnectorStart implements IApplication,
                                          IGenericServiceListener<ISessionService> {
    
    public final static int STAT_INIT = 0;
    public final static int STAT_OK = 1;
    public final static int STAT_ERR_MODEM = 2;
    public final static int STAT_ERR_MODEM_SEND = 3;
    public final static int STAT_ERR_JMSCON = 4; // jms communication to ams internal jms partners
    public final static int STAT_ERR_UNDEFINED = 5;
    public final static int STAT_SENDING = 6;
    public final static int STAT_READING = 7;

    public static String STATE_TEXT[] = { "Init", "OK", "Modem Error: General",
                                          "Modem Error: Sending", "JMS Error",
                                          "Unknown", "Sending", "Reading" };

    public final static long WAITFORTHREAD = 10000;
    public final static boolean CREATE_DURABLE = true;

    private static SmsConnectorStart _me;

    private ISessionService xmppService;
    
    private JmsSimpleProducer extPublisherStatusChange;
    
    private SynchObject sObj;
    private String managementPassword; 
    private int lastStatus = 0;
    
    private boolean bStop;
    private boolean restart;
    
    public SmsConnectorStart() {
        
        _me = this;
        sObj = new SynchObject(STAT_INIT, System.currentTimeMillis());
        bStop = false;
        restart = false;
        xmppService = null;
        
        IPreferencesService pref = Platform.getPreferencesService();
        managementPassword = pref.getString(AmsActivator.PLUGIN_ID, AmsPreferenceKey.P_AMS_MANAGEMENT_PASSWORD, "", null);
        if(managementPassword == null) {
            managementPassword = "";
        }
    }

    @Override
    public Object start(IApplicationContext context) throws Exception {
        
        SmsConnectorWork scw = null;
        boolean bInitedJms = false;

        Log.log(this, Log.INFO, "start");
        Log.log(this, Log.INFO, "Location: " + Platform.getInstanceLocation().toString());
        
        SmsConnectorPreferenceKey.showPreferences();

        SmsConnectorPlugin.getDefault().addSessionServiceListener(this);
        
        // use synchronized method
        lastStatus = getStatus();

        while(bStop == false) {
            
            try {
                
                if (scw == null) {
                    scw = new SmsConnectorWork(this);
                    scw.start();
                }
                
                if (!bInitedJms) {
                    bInitedJms = initJms();
                }
        
                // Log.log(this, Log.DEBUG, "is running");
                Thread.sleep(1000);
                
                SynchObject actSynch = new SynchObject(0, 0);
                // If the status have not been changed during the last 3 minutes
                // OR
                // If the current status signals a modem error
                // THEN
                // Do a automatic restart
                if(!sObj.hasStatusSet(actSynch, 180, STAT_ERR_UNDEFINED)
                        || sObj.getSynchStatus() == STAT_ERR_MODEM) {
                    
                    // every 2 minutes if blocked
                    Log.log(this, Log.FATAL, "TIMEOUT: status has not changed the last 3 minutes.");
                    
                    scw.stopWorking();
                    
                    try {
                        scw.join(WAITFORTHREAD);
                    } catch(InterruptedException ie) {
                        // Can be ignored
                    }

                    if(scw.stoppedClean()) {
                        Log.log(this, Log.FATAL, "ERROR: Thread stopped clean.");
                        
                        scw = null;
                    } else {
                        Log.log(this, Log.FATAL, "ERROR: Thread did NOT stop clean.");
                        scw.closeJms();
                        scw = null;
                    }
                    
                    this.setStatus(STAT_ERR_MODEM);
                    
                    Log.log(Log.FATAL, "Modem error. Restart SmsConnector");
                    
                    this.setRestart();
                }
                
                String statustext = "unknown";
                // if status value changed
                if(actSynch.getStatus() != lastStatus) {
                    
                    switch(actSynch.getStatus()) {
                        
                        case STAT_INIT:
                            statustext = "init";
                            break;
                            
                        case STAT_OK:
                            statustext = "ok";
                            break;
                        
                        case STAT_READING:
                            statustext = "reading";
                            break;
                            
                        case STAT_SENDING:
                            statustext = "sending";
                            break;
                            
                        case STAT_ERR_MODEM:
                        case STAT_ERR_MODEM_SEND:
                            statustext = "err_modem";
                            break;
                            
                        case STAT_ERR_JMSCON:
                            statustext = "err_jms";
                            break;
                    }
                    
                    Log.log(this, Log.DEBUG, "set status to " + statustext + "(" + actSynch.getStatus() + ") - Last status: " + lastStatus);
                    
                    lastStatus = actSynch.getStatus();
                    
                    if(bInitedJms) {
                        if(!sendStatusChange(actSynch.getStatus(), statustext, actSynch.getTime())) {
                            closeJms();
                            bInitedJms = false;
                        }
                    }
                }
            } catch(Exception e) {
                Log.log(this, Log.FATAL, e);
                closeJms();
                bInitedJms = false;
            }
        }

        if(scw != null) {
            
            // Clean stop of the working thread
            scw.stopWorking();
            
            try {
                scw.join(WAITFORTHREAD);
            } catch(InterruptedException ie) {
                // Can be ignored
            }
    
            if(scw.stoppedClean()) {
                Log.log(this, Log.FATAL, "Restart/Exit: Thread stopped clean.");
                scw = null;
            } else {
                Log.log(this, Log.FATAL, "Restart/Exit: Thread did NOT stop clean.");
                scw.closeModem();
                scw.closeJms();
                scw.storeRemainingMessages();
                scw = null;
            }
        }
        
        if (xmppService != null) {
            xmppService.disconnect();
        }
        
        Log.log(this, Log.INFO, "Leaving start()");
        
        Integer exitCode = IApplication.EXIT_OK;
        if(restart) {
            exitCode = IApplication.EXIT_RESTART;
        }
        
        return exitCode;
    }

    
    @Override
    public void stop() {
        Log.log(Log.INFO, "method: stop()");
        return;
    }

    public static SmsConnectorStart getInstance() {
        return _me;
    }
    
    /**
     * Sets the restart flag to force a restart.
     */
    public synchronized void setRestart() {
        restart = true;
        bStop = true;
    }
    
    /**
     * Sets the shutdown flag to force a shutdown.
     */
    public synchronized void setShutdown() {
        restart = false;
        bStop = true;
    }

    /**
     * 
     * @return Password for remote management
     */
    public synchronized String getPassword() {
        return managementPassword;
    }

    public int getStatus() {
        return sObj.getSynchStatus();
    }
    
    public void setStatus(int status) {
        sObj.setSynchStatus(status);                                            // set always, to update time
    }
    
    private boolean initJms() {
        
        try {
            
            IPreferenceStore storeAct = AmsActivator.getDefault().getPreferenceStore();
            String factoryClass = storeAct.getString(AmsPreferenceKey.P_JMS_EXTERN_CONNECTION_FACTORY_CLASS);
            String url = storeAct.getString(AmsPreferenceKey.P_JMS_EXTERN_SENDER_PROVIDER_URL);
            String topic = storeAct.getString(AmsPreferenceKey.P_JMS_EXT_TOPIC_STATUSCHANGE);
            
            extPublisherStatusChange = new JmsSimpleProducer("SmsConnectorStartSenderExternal", url, factoryClass, topic);
            if (extPublisherStatusChange.isConnected() == false) {
                Log.log(this, Log.FATAL, "could not create extPublisherStatusChange");
                return false;
            }
        } catch(Exception e) {
            Log.log(this, Log.FATAL, "Could not init external Jms: " + e.getMessage());
        }
        
        return extPublisherStatusChange.isConnected();
    }

    private void closeJms() {
        
        Log.log(this, Log.INFO, "Exiting external JMS communication");
        
        if (extPublisherStatusChange != null){
            extPublisherStatusChange.closeAll();
        }
        
        Log.log(this, Log.INFO, "JMS external communication closed");
    }
    
    private boolean sendStatusChange(int status, String strStat, long lSetTime) throws Exception {
        
        boolean success = false;
        
        try {
            
            MapMessage mapMsg = extPublisherStatusChange.createMapMessage();

            mapMsg.setString(AmsConstants.MSGPROP_CHECK_TYPE, "PStatus");
            mapMsg.setString(AmsConstants.MSGPROP_CHECK_PURL, InetAddress.getLocalHost().getHostAddress());
            mapMsg.setString(AmsConstants.MSGPROP_CHECK_PLUGINID, SmsConnectorPlugin.PLUGIN_ID);
            mapMsg.setString(AmsConstants.MSGPROP_CHECK_STATUSTIME, Utils.longTimeToUTCString(lSetTime));
            mapMsg.setString(AmsConstants.MSGPROP_CHECK_STATUS, String.valueOf(status));
            mapMsg.setString(AmsConstants.MSGPROP_CHECK_TEXT, strStat);

            Log.log(this, Log.DEBUG, "StatusChange - start external jms send. MessageProperties= " + Utils.getMessageString(mapMsg));

            success = extPublisherStatusChange.sendMessage(mapMsg);
            Log.log(this, Log.DEBUG, "Send external jms message done");
            
        } catch (JMSException jmse) {
            Log.log(this, Log.FATAL, "Could not create or send MapMessage: " + jmse.getMessage());
        }

        return success;
    }
    
    @Override
    public void bindService(ISessionService sessionService) {
    	
        IPreferencesService pref = Platform.getPreferencesService();
    	String xmppServer = pref.getString(SmsConnectorPlugin.PLUGIN_ID, SmsConnectorPreferenceKey.P_XMPP_SERVER, "krynfs.desy.de", null);
        String xmppUser = pref.getString(SmsConnectorPlugin.PLUGIN_ID, SmsConnectorPreferenceKey.P_XMPP_USER, "anonymous", null);
        String xmppPassword = pref.getString(SmsConnectorPlugin.PLUGIN_ID, SmsConnectorPreferenceKey.P_XMPP_PASSWORD, "anonymous", null);
    	
    	try {
			sessionService.connect(xmppUser, xmppPassword, xmppServer);
			xmppService = sessionService;
		} catch (Exception e) {
			Log.log(this, Log.WARN, "XXMPP connection is not available: " + e.getMessage());
			xmppService = null;
		}
    }
    
    @Override
    public void unbindService(ISessionService service) {
    	service.disconnect();
    }
}