/**
 * Copyright 2013 deib-polimi
 * Contact: deib-polimi <marco.miglierina@polimi.it>
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package it.polimi.modaclouds.cpimlibrary.entitymng.migration;

import it.polimi.modaclouds.cpimlibrary.blobmng.CloudBlobManager;
import it.polimi.modaclouds.cpimlibrary.entitymng.PersistenceMetadata;
import it.polimi.modaclouds.cpimlibrary.exception.MigrationException;
import it.polimi.modaclouds.cpimlibrary.mffactory.MF;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Manage a {@link it.polimi.modaclouds.cpimlibrary.entitymng.migration.SeqNumberDispenserImpl}
 * foreach table is registered.
 * <p/>
 * A table can be registered at runtime using the {@code addTable(tableName)} method.
 * All the persisted tables stated in persistence.xml are automatically registered in construction.
 * <p/>
 * The class is managed as a singleton instance so to get the next generated sequence number for a table
 * simply call {@code SeqNumberProvider.getInstance().getNextSequenceNumber(tableName)}.
 *
 * @author Fabio Arcidiacono.
 * @see it.polimi.modaclouds.cpimlibrary.entitymng.migration.SeqNumberDispenser
 * @see it.polimi.modaclouds.cpimlibrary.entitymng.migration.SeqNumberDispenserImpl
 */
@Slf4j
public class SeqNumberProvider {

    private static SeqNumberProvider instance = null;
    private Map<String, SeqNumberDispenser> dispenser;
    private final boolean executeBackup;
    private final boolean backupToBlob;
    private String prefix;
    private String backupFile;
    private CloudBlobManager blobManager;

    private SeqNumberProvider() {
        this.executeBackup = MF.getFactory().getCloudMetadata().executeBackup();
        this.backupToBlob = MF.getFactory().getCloudMetadata().isBackupToBlob();
        if (executeBackup) {
            this.prefix = MF.getFactory().getCloudMetadata().getBackupPrefix();
            if (backupToBlob) {
                this.blobManager = MF.getFactory().getBlobManagerFactory().createCloudBlobManager();
            } else {
                this.backupFile = MF.getFactory().getCloudMetadata().getBackupDir();
            }
        }

        this.dispenser = new HashMap<>();
        Set<String> persistedTables = PersistenceMetadata.getInstance().getPersistedTables();
        for (String table : persistedTables) {
            this.addTable(table);
        }
    }

    public static synchronized SeqNumberProvider getInstance() {
        if (instance == null) {
            instance = new SeqNumberProvider();
        }
        return instance;
    }

    private SeqNumberDispenser getDispenser(String tableName) {
        SeqNumberDispenser seqNumberDispenser = this.dispenser.get(tableName);
        if (seqNumberDispenser == null) {
            throw new IllegalArgumentException("Table [" + tableName + "] was not registered");
        }
        return seqNumberDispenser;
    }

    /**
     * Register a table so its ids can be managed through the migration system.
     * <p/>
     * Before registering the table, check if a blob with previous backup exists
     * in this case restore the state of the created table dispenser.
     *
     * @param tableName the table name
     */
    public void addTable(String tableName) {
        SeqNumberDispenserImpl tableDispenser = new SeqNumberDispenserImpl(tableName);
        if (executeBackup) {
            restoreDispenserState(tableDispenser);
        }
        this.dispenser.put(tableName, tableDispenser);
    }

    /**
     * Gives the possibility to modify at runtime the offset for the given table.
     *
     * @param tableName the name of the table on which modify the offset
     * @param offset    the new offset
     */
    public void setOffset(String tableName, int offset) {
        SeqNumberDispenser tableDispenser = getDispenser(tableName);
        tableDispenser.setOffset(offset);
    }

    /**
     * Returns the current offset fot the given table.
     *
     * @param tableName the table name
     *
     * @return the integer value of the current offset
     */
    public int getOffset(String tableName) {
        SeqNumberDispenser tableDispenser = getDispenser(tableName);
        return tableDispenser.geOffset();
    }

    /**
     * Gives the next sequence number assigned by migration system for the given table
     * and backup to a blob the new state of the table dispenser.
     *
     * @param tableName the table name
     *
     * @return the sequence number
     *
     * @throws java.lang.RuntimeException if {@code tableName} was not registered
     */
    public int getNextSequenceNumber(String tableName) {
        SeqNumberDispenser tableDispenser = getDispenser(tableName);
        int next = tableDispenser.nextSequenceNumber();
        if (executeBackup) {
            backupDispenserState(tableDispenser);
        }
        return next;
    }

    private void backupDispenserState(SeqNumberDispenser tableDispenser) {
        byte[] newState = tableDispenser.save();
        if (backupToBlob) {
            String blobFileName = getFileName(tableDispenser);
            blobManager.uploadBlob(newState, blobFileName);
        } else {
            try {
                String fileName = getFileName(tableDispenser);
                FileOutputStream out = new FileOutputStream(fileName);
                out.write(newState);
                out.close();
            } catch (Exception e) {
                throw new MigrationException("Could not backup to file.", e);
            }
        }
    }

    private void restoreDispenserState(SeqNumberDispenser tableDispenser) {
        byte[] savedState = null;
        try {
            if (backupToBlob) {
                String blobFileName = getFileName(tableDispenser);
                if (blobManager.fileExists(blobFileName)) {
                    savedState = blobManager.downloadBlob(blobFileName).getContent();
                }
            } else {
                String fileName = getFileName(tableDispenser);
                File f = new File(fileName);
                if (f.exists() && !f.isDirectory()) {
                    log.info("Restoring state for table [" + tableDispenser.getTable() + "]");
                    FileInputStream in = new FileInputStream(fileName);
                    savedState = IOUtils.toByteArray(in);
                    in.close();
                }
            }
            tableDispenser.restore(savedState);
        } catch (Exception e) {
            throw new MigrationException("Some problem occurred while restoring the previous state for table [" + tableDispenser.getTable() + "]", e);
        }
    }

    private String getFileName(SeqNumberDispenser tableDispenser) {
        if (backupToBlob) {
            return prefix + tableDispenser.getTable();
        } else {
            return backupFile + prefix + tableDispenser.getTable();
        }
    }
}
