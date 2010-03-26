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
package org.csstudio.utility.ldapUpdater;

import static org.csstudio.utility.ldap.LdapUtils.ATTR_FIELD_OBJECT_CLASS;
import static org.csstudio.utility.ldap.LdapUtils.ATTR_VAL_OBJECT_CLASS;
import static org.csstudio.utility.ldap.LdapUtils.ECOM_FIELD_NAME;
import static org.csstudio.utility.ldap.LdapUtils.ECOM_FIELD_VALUE;
import static org.csstudio.utility.ldap.LdapUtils.ECON_FIELD_NAME;
import static org.csstudio.utility.ldap.LdapUtils.EFAN_FIELD_NAME;
import static org.csstudio.utility.ldap.LdapUtils.EPICS_CTRL_FIELD_VALUE;
import static org.csstudio.utility.ldap.LdapUtils.EREN_FIELD_NAME;
import static org.csstudio.utility.ldap.LdapUtils.OU_FIELD_NAME;
import static org.csstudio.utility.ldap.LdapUtils.attributesForLdapEntry;
import static org.csstudio.utility.ldap.LdapUtils.createLdapQuery;
import static org.csstudio.utility.ldapUpdater.preferences.LdapUpdaterPreferenceKey.IOC_DBL_DUMP_PATH;
import static org.csstudio.utility.ldapUpdater.preferences.LdapUpdaterPreferences.getValueFromPreferences;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;

import org.apache.log4j.Logger;
import org.csstudio.platform.logging.CentralLogger;
import org.csstudio.utility.ldap.LdapUtils;
import org.csstudio.utility.ldap.engine.Engine;
import org.csstudio.utility.ldap.reader.LdapService;
import org.csstudio.utility.ldap.reader.LdapServiceImpl;
import org.csstudio.utility.ldapUpdater.files.HistoryFileAccess;
import org.csstudio.utility.ldapUpdater.files.HistoryFileContentModel;
import org.csstudio.utility.ldapUpdater.mail.NotificationMail;
import org.csstudio.utility.ldapUpdater.mail.NotificationType;
import org.csstudio.utility.ldapUpdater.model.IOC;
import org.csstudio.utility.ldapUpdater.model.LDAPContentModel;
import org.csstudio.utility.ldapUpdater.model.Record;
import org.csstudio.utility.ldapUpdater.preferences.LdapUpdaterPreferenceKey;


public final class LdapAccess {


    private static final LdapService _service = new LdapServiceImpl();


    private static class UpdateIOCResult {
        private final int _numOfRecsWritten;
        private final boolean _noError;
        private final int _numOfRecsInFile;
        private final int _numOfRecsInLDAP;


        public UpdateIOCResult(final int numOfRecsInFile,
                               final int numOfRecsWritten,
                               final int numOfRecordsInLDAP,
                               final boolean noError) {
            _numOfRecsInFile = numOfRecsInFile;
            _numOfRecsWritten = numOfRecsWritten;
            _numOfRecsInLDAP = numOfRecordsInLDAP;
            _noError = noError;
        }

        public int getNumOfRecsInFile() {
            return _numOfRecsInFile;
        }

        public int getNumOfRecsWritten() {
            return _numOfRecsWritten;
        }

        public boolean hasNoError() {
            return _noError;
        }

        public int getNumOfRecsInLDAP() {
            return _numOfRecsInLDAP;
        }
    }

    private final static Logger LOGGER = CentralLogger.getInstance().getLogger(LdapAccess.class);


    /**
     * Don't instantiate.
     */
    private LdapAccess() {
        // Empty
    }

    /**
     * The method triggers an LDAP search with the given parameters.
     * The ldapDataObserver is triggered to fill its internal LDAPContentModel as soon as the LDAP search has been completed.
     *
     * @param ldapDataObserver
     * @param searchRoot
     * @param filter
     * @return the filled LDAP content model from the current search request.
     * @throws InterruptedException
     */
    public static LDAPContentModel fillModelFromLdap(final ReadLdapObserver ldapDataObserver, final String searchRoot, final String filter)
    throws InterruptedException {

        _service.readLdapEntries(searchRoot, filter, ldapDataObserver);

        while(!ldapDataObserver.isReady()){ Thread.sleep(100);} // observer finished update of the model
        LOGGER.info("LDAP Read Done");
        return ldapDataObserver.getLdapModel();
    }


    private static LDAPContentModel fillModelWithLdapRecordsForIOC(final ReadLdapObserver ldapDataObserver,
                                                                   final IOC iocFromLDAP) throws InterruptedException {
        return fillModelWithLdapRecordsForIOC(ldapDataObserver, iocFromLDAP.getName(), iocFromLDAP.getGroup());
    }


    private static LDAPContentModel fillModelWithLdapRecordsForIOC(final ReadLdapObserver ldapDataObserver,
                                                                   final String iocName,
                                                                   final String facilityName) throws InterruptedException {

        ldapDataObserver.setReady(false);
        final String query = createLdapQuery(ECON_FIELD_NAME, iocName,
                                             ECOM_FIELD_NAME, ECOM_FIELD_VALUE,
                                             EFAN_FIELD_NAME, facilityName,
                                             OU_FIELD_NAME, EPICS_CTRL_FIELD_VALUE);

        _service.readLdapEntries(query, LdapUtils.any(EREN_FIELD_NAME), ldapDataObserver);

        while (!ldapDataObserver.isReady()) {
            Thread.sleep(100);
        }
        return ldapDataObserver.getLdapModel();
    }


    /**
     * TODO (bknerr) : should be encapsulated in a file access class - does not belong here.
     */
    private static Set<Record> getRecordsFromFile(final String pathToFile) {
        final Set<Record> records = new HashSet<Record>();
        try {
            final BufferedReader br = new BufferedReader(new FileReader(pathToFile));
            String strLine;
            while ((strLine = br.readLine()) != null)   {
                records.add(new Record(strLine));
            }
            return records;
        } catch (final FileNotFoundException e) {
            LOGGER.error("Could not find file: " + pathToFile + "\n" + e.getMessage());
        } catch (final IOException e) {
            LOGGER.error("Error while reading from file: " + e.getMessage());
        }
        return Collections.emptySet();
    }

    private static boolean isIOCFileNewerThanHistoryEntry(final IOC ioc, final HistoryFileContentModel historyFileModel) {
        final long timeFromFile = ioc.getDateTime().getTimeInMillis() / 1000;
        final long timeFromHistoryFile = historyFileModel.getTimeForRecord(ioc.getName());
        return timeFromFile > timeFromHistoryFile;
    }


    public static void removeIocEntryFromLdap(final String iocName, final String facilityName) {

        // retrieve all info from
        final LDAPContentModel ldapContentModel = new LDAPContentModel();
        final ReadLdapObserver ldapDataObserver = new ReadLdapObserver(ldapContentModel);
        String query = "";
        try {
            final LDAPContentModel model =
                fillModelWithLdapRecordsForIOC(ldapDataObserver, iocName, facilityName);

            final IOC ioc = model.getIOC(facilityName, iocName);

            final Map<String, Record> records = ioc.getRecords();
            for (final Record record : records.values()) {
                removeRecordEntryFromLdap(ioc , record);
            }

            final DirContext context = Engine.getInstance().getLdapDirContext();
            query = createLdapQuery(ECON_FIELD_NAME, iocName,
                                    ECOM_FIELD_NAME, ECOM_FIELD_VALUE,
                                    EFAN_FIELD_NAME, facilityName,
                                    OU_FIELD_NAME, EPICS_CTRL_FIELD_VALUE);
            context.unbind(query);
            // FIXME (bknerr) : test missing
            LOGGER.info("IOC removed from LDAP: " + query);
        } catch (final NamingException e) {
            LOGGER.warn("Naming Exception while trying to unbind: " + query);
            LOGGER.warn(e.getExplanation());
        } catch (final InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }


    public static void removeIocEntryFromLdap(final IOC iocFromLdap) {
        removeIocEntryFromLdap(iocFromLdap.getName(), iocFromLdap.getGroup());
    }


    private static void removeRecordEntryFromLdap(final IOC ioc, final Record record) {
        final DirContext context = Engine.getInstance().getLdapDirContext();

        final String query = createLdapQuery(EREN_FIELD_NAME, record.getName(),
                                             ECON_FIELD_NAME, ioc.getName(),
                                             ECOM_FIELD_NAME, ECOM_FIELD_VALUE,
                                             EFAN_FIELD_NAME, ioc.getGroup(),
                                             OU_FIELD_NAME, EPICS_CTRL_FIELD_VALUE);
        try {
            context.unbind(query);
            LOGGER.info("Record removed from LDAP: " + query);
        } catch (final NamingException e) {
            LOGGER.warn("Naming Exception while trying to unbind: " + query);
            LOGGER.warn(e.getExplanation());
        }
    }


    /**
     * @param service
     * @param ldapDataObserver
     * @param ldapContentModel
     * @param iocFilePath
     * @param iocList
     * @throws InterruptedException
     */
    public static void tidyUpLDAPFromIOCList(final ReadLdapObserver ldapDataObserver,
                                             final LDAPContentModel ldapContentModel,
                                             final Map<String, IOC> iocMapFromFS) throws InterruptedException {

        for (final String iocNameFromLdap : ldapContentModel.getIOCNames()) {

            final IOC iocFromLdap = ldapContentModel.getIOC(iocNameFromLdap);
            if (iocMapFromFS.containsKey(iocNameFromLdap)) {

                tidyUpIocEntryInLdap(iocFromLdap);
            } else { // LDAP entry is not contained in current IOC directory - is considered obsolete!
                removeIocEntryFromLdap(iocFromLdap);
            }
        }
    }

    /**
     * Compares the records found in the file on directoryServer with the records specified in LDAP and
     * removes any record from LDAP that is not found in the file.
     *
     * @param ioc the IOC
     */
    public static void tidyUpIocEntryInLdap(final IOC ioc) {
        tidyUpIocEntryInLdap(ioc.getName(), ioc.getGroup());
    }

    /**
     * Compares the records found in the file on directoryServer with the records specified in LDAP and
     * removes any record from LDAP that is not found in the file.
     *
     * @param iocName IOC name
     * @param facName Facility name
     */
    public static void tidyUpIocEntryInLdap(final String iocName, final String facName) {
        final String iocFilePath = getValueFromPreferences(IOC_DBL_DUMP_PATH);
        final Set<Record> fileRecords = getRecordsFromFile(iocFilePath + iocName);

        final LDAPContentModel ldapContentModel = new LDAPContentModel();
        final ReadLdapObserver ldapDataObserver = new ReadLdapObserver(ldapContentModel);
        try {
            LdapAccess.fillModelWithLdapRecordsForIOC(ldapDataObserver, iocName, facName);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
        }

        final IOC ioc = ldapContentModel.getIOC(facName, iocName);
        final Set<Record> ldapRecords = ioc.getRecordValues();

        ldapRecords.removeAll(fileRecords); // removes all that are contained in the file

        for (final Record record : ldapRecords) {
            removeRecordEntryFromLdap(ioc, record);
            LOGGER.info("Tidying: Record " + record.getName() + " removed.");
        }
    }


    private static UpdateIOCResult updateIOC(final LDAPContentModel ldapContentModel,
                                             final IOC ioc) {

        final String iocName = ioc.getName();
        int numOfRecsWritten = 0;

        final String iocFilePath = getValueFromPreferences(IOC_DBL_DUMP_PATH);
        final Set<Record> recordsFromFile = getRecordsFromFile(iocFilePath + iocName);

        final StringBuilder forbiddenRecords = new StringBuilder();

        LOGGER.info( "Process IOC " + iocName + "\t\t #records " +  recordsFromFile.size());
        for (final Record record : recordsFromFile) {
            final String recordName = record.getName();

            if (ldapContentModel.getRecord(ioc.getGroup(), iocName, recordName) == null) { // does not yet exist
                LOGGER.info("New Record: " + iocName + " " + recordName);

                if (!LdapUtils.filterLDAPNames(recordName)) {
                    // TODO (bknerr) : Stopping or proceeding? Transaction rollback? Hist file update ?
                    if (!createLDAPRecord(ioc, recordName)) {
                        LOGGER.error("Error while updating LDAP record for " + recordName +
                        "\nProceed with next record.");
                    } else {
                        numOfRecsWritten++;
                    }
                } else {
                    LOGGER.warn("Record " + recordName + " could not be written. Unallowed characters!");
                    forbiddenRecords.append(recordName + "\n");
                }
            }
        }
        if (forbiddenRecords.length() > 0) {
            NotificationMail.sendMail(NotificationType.UNALLOWED_CHARS,
                                      ioc.getResponsible(),
                                      "\nIn IOC " + iocName + ":\n\n" + forbiddenRecords.toString());
        }

        // TODO (bknerr) : what to do with success variable ?
        return new UpdateIOCResult(recordsFromFile.size(),
                                   numOfRecsWritten,
                                   ldapContentModel.getRecords(iocName).size(), true);
    }


    /**
     * This method compares the contents of the current LDAP hierarchy with the contents found in
     * the directory, where the IOC files reside. The contents of the ioc list are firstly checked
     * whether they are more recent than those stored in the history file, if not so the ioc file
     * has already been processed. If so, the LDAP is updated with the newer content of the ioc
     * files conservatively, i.e. by adding references to existing files, but not removing entries
     * from the LDAP in case the corresponding file does not exist in the ioc directory.
     *
     * @param service
     * @param ldapDataObserver
     * @param ldapContentModel
     * @param iocMap
     *            the list of ioc filenames as found in the ioc directory
     * @param historyFileModel
     *            the contents of the history file
     * @throws InterruptedException
     */
    public static void updateLDAPFromIOCList(final ReadLdapObserver ldapDataObserver,
                                             final LDAPContentModel ldapContentModel,
                                             final Map<String, IOC> iocMap,
                                             final HistoryFileContentModel historyFileModel) throws InterruptedException {

        for (final Entry<String, IOC> iocFromFS : iocMap.entrySet()) {

            final String iocName = iocFromFS.getKey();

            if (historyFileModel.contains(iocName)) {
                if (!isIOCFileNewerThanHistoryEntry(iocFromFS.getValue(), historyFileModel)) {
                    LOGGER.debug("IOC file for " + iocName
                                 + " is not newer than history file time stamp.");
                    continue;
                }
            } // else means 'new IOC file in directory'

            final IOC iocFromLDAP = ldapContentModel.getIOC(iocName);
            if (iocFromLDAP == null) {
                LOGGER.warn("IOC "
                            + iocName
                            + " (from file system) does not exist in LDAP - no facility/group association possible.\n"
                            + "No LDAP Update! Generate an LDAP entry for this IOC manually!");
                continue;
            }

            LdapAccess.fillModelWithLdapRecordsForIOC(ldapDataObserver, iocFromLDAP);

            final UpdateIOCResult updateResult = updateIOC(ldapContentModel, iocFromLDAP);

            // TODO (bknerr) : does only make sense when the update process has been stopped
            if (updateResult.hasNoError()) {
                try {
                    HistoryFileAccess.appendLineToHistfile(iocName,
                                                           updateResult.getNumOfRecsWritten(),
                                                           updateResult.getNumOfRecsInFile(),
                                                           updateResult.getNumOfRecsInLDAP() );
                } catch (final IOException e) {
                    LOGGER.error("I/O-Exception while trying to append a line to "
                                 + getValueFromPreferences(LdapUpdaterPreferenceKey.LDAP_HIST_PATH) + "history.dat");
                }
            }
        }
    }

    private static boolean createLDAPRecord(final IOC ioc,
                                            final String recordName) {

        final DirContext context = Engine.getInstance().getLdapDirContext();

        final String query = createLdapQuery(EREN_FIELD_NAME, recordName,
                                             ECON_FIELD_NAME, ioc.getName(),
                                             ECOM_FIELD_NAME, ECOM_FIELD_VALUE,
                                             EFAN_FIELD_NAME, ioc.getGroup(),
                                             OU_FIELD_NAME, EPICS_CTRL_FIELD_VALUE);

        final Attributes afe =
            attributesForLdapEntry(ATTR_FIELD_OBJECT_CLASS, ATTR_VAL_OBJECT_CLASS,
                                   EREN_FIELD_NAME, recordName);
        try {
            context.bind(query, null, afe);
            LOGGER.info( "Record written: " + query);
        } catch (final NamingException e) {
            LOGGER.warn( "Naming Exception while trying to bind: " + query);
            LOGGER.warn(e.getExplanation());
            return false;
        }
        return true;
    }
}
