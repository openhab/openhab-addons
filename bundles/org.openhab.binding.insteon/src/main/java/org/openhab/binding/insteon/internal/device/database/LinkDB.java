/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.insteon.internal.device.database;

import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.insteon.internal.device.InsteonAddress;
import org.openhab.binding.insteon.internal.device.InsteonDevice;
import org.openhab.binding.insteon.internal.device.InsteonModem;
import org.openhab.binding.insteon.internal.utils.HexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link LinkDB} holds all-link database records for a device
 *
 * @author Jeremy Setton - Initial contribution
 */
@NonNullByDefault
public class LinkDB {
    private static enum DatabaseStatus {
        EMPTY,
        COMPLETE,
        PARTIAL,
        LOADING
    }

    public static enum ReadWriteMode {
        STANDARD,
        PEEK_POKE,
        UNKNOWN
    }

    private final Logger logger = LoggerFactory.getLogger(LinkDB.class);

    private final InsteonDevice device;
    private final TreeMap<Integer, LinkDBRecord> records = new TreeMap<>(Collections.reverseOrder());
    private final TreeMap<Integer, LinkDBChange> changes = new TreeMap<>(Collections.reverseOrder());
    private DatabaseStatus status = DatabaseStatus.EMPTY;
    private int delta = -1;
    private int firstLocation = 0x0FFF;
    private boolean reload = false;
    private boolean update = false;

    public LinkDB(InsteonDevice device) {
        this.device = device;
    }

    private @Nullable InsteonModem getModem() {
        return device.getModem();
    }

    public @Nullable DatabaseManager getDatabaseManager() {
        return Optional.ofNullable(getModem()).map(InsteonModem::getDBM).orElse(null);
    }

    public int getDatabaseDelta() {
        return delta;
    }

    public int getFirstRecordLocation() {
        return firstLocation;
    }

    public int getLastRecordLocation() {
        synchronized (records) {
            return records.isEmpty() ? getFirstRecordLocation() : records.lastKey();
        }
    }

    public @Nullable LinkDBRecord getFirstRecord() {
        synchronized (records) {
            return records.isEmpty() ? null : records.firstEntry().getValue();
        }
    }

    public @Nullable LinkDBRecord getRecord(int location) {
        synchronized (records) {
            return records.get(location);
        }
    }

    public List<LinkDBRecord> getRecords() {
        synchronized (records) {
            return records.values().stream().toList();
        }
    }

    private Stream<LinkDBRecord> getRecords(@Nullable InsteonAddress address, @Nullable Integer group,
            @Nullable Boolean isController, @Nullable Boolean isActive, @Nullable Integer componentId) {
        return getRecords().stream()
                .filter(record -> (address == null || record.getAddress().equals(address))
                        && (group == null || record.getGroup() == group)
                        && (isController == null || record.isController() == isController)
                        && (isActive == null || record.isActive() == isActive)
                        && (componentId == null || record.getComponentId() == componentId));
    }

    public List<LinkDBRecord> getControllerRecords() {
        return getRecords(null, null, true, true, null).toList();
    }

    public List<LinkDBRecord> getControllerRecords(InsteonAddress address) {
        return getRecords(address, null, true, true, null).toList();
    }

    public List<LinkDBRecord> getControllerRecords(InsteonAddress address, int group) {
        return getRecords(address, group, true, true, null).toList();
    }

    public List<LinkDBRecord> getResponderRecords() {
        return getRecords(null, null, false, true, null).toList();
    }

    public List<LinkDBRecord> getResponderRecords(InsteonAddress address) {
        return getRecords(address, null, false, true, null).toList();
    }

    public List<LinkDBRecord> getResponderRecords(InsteonAddress address, int group) {
        return getRecords(address, group, false, true, null).toList();
    }

    public @Nullable LinkDBRecord getActiveRecord(InsteonAddress address, int group, boolean isController,
            int componentId) {
        return getRecords(address, group, isController, true, componentId).findFirst().orElse(null);
    }

    public boolean hasRecord(@Nullable InsteonAddress address, @Nullable Integer group, @Nullable Boolean isController,
            @Nullable Boolean isActive, @Nullable Integer componentId) {
        return getRecords(address, group, isController, isActive, componentId).findAny().isPresent();
    }

    public boolean hasComponentIdRecord(int componentId, boolean isController) {
        return getRecords(null, null, isController, true, componentId).findAny().isPresent();
    }

    public boolean hasGroupRecord(int group, boolean isController) {
        return getRecords(null, group, isController, true, null).findAny().isPresent();
    }

    public int size() {
        return getRecords().size();
    }

    public int getLastChangeLocation() {
        synchronized (changes) {
            return changes.isEmpty() ? getFirstRecordLocation() : changes.lastKey();
        }
    }

    public List<LinkDBChange> getChanges() {
        synchronized (changes) {
            return changes.values().stream().toList();
        }
    }

    private Stream<LinkDBChange> getChanges(@Nullable InsteonAddress address, @Nullable Integer group,
            @Nullable Boolean isController, @Nullable Integer componentId) {
        return getChanges().stream()
                .filter(changes -> (address == null || changes.getRecord().getAddress().equals(address))
                        && (group == null || changes.getRecord().getGroup() == group)
                        && (isController == null || changes.getRecord().isController() == isController)
                        && (componentId == null || changes.getRecord().getComponentId() == componentId));
    }

    public @Nullable LinkDBChange getChange(InsteonAddress address, int group, boolean isController, int componentId) {
        return getChanges(address, group, isController, componentId).findFirst().orElse(null);
    }

    public @Nullable LinkDBChange pollNextChange() {
        synchronized (changes) {
            return Optional.ofNullable(changes.pollFirstEntry()).map(Entry::getValue).orElse(null);
        }
    }

    public boolean isComplete() {
        return status == DatabaseStatus.COMPLETE;
    }

    public boolean shouldReload() {
        return reload;
    }

    public boolean shouldUpdate() {
        return update;
    }

    public synchronized void setDatabaseDelta(int delta) {
        logger.trace("setting link db delta to {} for {}", delta, device.getAddress());
        this.delta = delta;
    }

    public synchronized void setFirstRecordLocation(int firstLocation) {
        if (logger.isTraceEnabled()) {
            logger.trace("setting link db first record location to {} for {}", HexUtils.getHexString(firstLocation),
                    device.getAddress());
        }
        this.firstLocation = firstLocation;
    }

    public synchronized void setReload(boolean reload) {
        logger.trace("setting link db reload to {} for {}", reload, device.getAddress());
        this.reload = reload;
    }

    private synchronized void setUpdate(boolean update) {
        logger.trace("setting link db update to {} for {}", update, device.getAddress());
        this.update = update;
    }

    private synchronized void setStatus(DatabaseStatus status) {
        logger.trace("setting link db status to {} for {}", status, device.getAddress());
        this.status = status;
    }

    /**
     * Returns a change location for a given address, group, controller flag and component id
     *
     * @param address the record address
     * @param group the record group
     * @param isController if is controller record
     * @param componentId the record componentId
     * @return change location if found, otherwise next available location
     */
    public int getChangeLocation(InsteonAddress address, int group, boolean isController, int componentId) {
        LinkDBChange change = getChange(address, group, isController, componentId);
        return change != null ? change.getLocation() : getNextAvailableLocation();
    }

    /**
     * Returns next available record location
     *
     * @return first available record location if found, otherwise the next lowest record or change location
     */
    public int getNextAvailableLocation() {
        return getRecords().stream().filter(LinkDBRecord::isAvailable).mapToInt(LinkDBRecord::getLocation).findFirst()
                .orElse(Math.min(getLastRecordLocation(), getLastChangeLocation() - LinkDBRecord.SIZE));
    }

    /**
     * Returns database read/write mode
     *
     * @return read/write mode based on device insteon engine
     */
    public ReadWriteMode getReadWriteMode() {
        switch (device.getInsteonEngine()) {
            case I1:
                return ReadWriteMode.PEEK_POKE;
            case I2:
            case I2CS:
                return ReadWriteMode.STANDARD;
            default:
                return ReadWriteMode.UNKNOWN;
        }
    }

    /**
     * Clears this link db
     */
    public synchronized void clear() {
        logger.debug("clearing link db for {}", device.getAddress());
        records.clear();
        changes.clear();
        status = DatabaseStatus.EMPTY;
        delta = -1;
        reload = false;
        update = false;
    }

    /**
     * Loads this link db
     */
    public void load() {
        load(0L);
    }

    /**
     * Loads this link db with a delay
     *
     * @param delay reading delay (in milliseconds)
     */
    public void load(long delay) {
        DatabaseManager dbm = getDatabaseManager();
        if (!device.isAwake() || !device.isOnline()) {
            logger.debug("deferring load link db for {}, device is not awake or online", device.getAddress());
            setReload(true);
        } else if (dbm == null) {
            logger.debug("unable to load link db for {}, database manager not available", device.getAddress());
        } else {
            setStatus(DatabaseStatus.LOADING);
            dbm.read(device, delay);
        }
    }

    /**
     * Updates this link db with changes
     */
    public void update() {
        update(0L);
    }

    /**
     * Updates this link db with changes and a delay
     *
     * @param delay writing delay (in milliseconds)
     */
    public void update(long delay) {
        DatabaseManager dbm = getDatabaseManager();
        if (getChanges().isEmpty()) {
            logger.debug("no changes to update link db for {}", device.getAddress());
            setUpdate(false);
        } else if (!device.isAwake() || !device.isOnline()) {
            logger.debug("deferring update link db for {}, device is not awake or online", device.getAddress());
            setUpdate(true);
        } else if (dbm == null) {
            logger.debug("unable to update link db for {}, database manager not available", device.getAddress());
        } else {
            dbm.write(device, delay);
        }
    }

    /**
     * Adds a link db record
     *
     * @param record the record to add
     * @return the previous record if overwritten
     */
    public @Nullable LinkDBRecord addRecord(LinkDBRecord record) {
        synchronized (records) {
            LinkDBRecord prevRecord = records.put(record.getLocation(), record);
            // move last record if overwritten by a different record
            if (prevRecord != null && prevRecord.isLast() && !prevRecord.equals(record)) {
                int location = prevRecord.getLocation() - LinkDBRecord.SIZE;
                records.put(location, prevRecord.withNewLocation(location));
                if (logger.isTraceEnabled()) {
                    logger.trace("moved last record for {} to location {}", device.getAddress(),
                            HexUtils.getHexString(location));
                }
            }
            return prevRecord;
        }
    }

    /**
     * Loads a list of link db records
     *
     * @param records list of records to load
     */
    public void loadRecords(List<LinkDBRecord> records) {
        logger.trace("loading link db records for {}", device.getAddress());
        records.forEach(this::addRecord);
        recordsLoaded();
    }

    /**
     * Logs the link db records
     */
    private void logRecords() {
        if (logger.isDebugEnabled()) {
            if (getRecords().isEmpty()) {
                logger.debug("no link records found for {}", device.getAddress());
            } else {
                logger.debug("---------------- start of link records for {} ----------------", device.getAddress());
                getRecords().stream().map(String::valueOf).forEach(logger::debug);
                logger.debug("----------------- end of link records for {} -----------------", device.getAddress());
            }
        }
    }

    /**
     * Notifies that the link db records have been loaded
     */
    public void recordsLoaded() {
        logRecords();
        updateStatus();
        device.linkDBUpdated();
    }

    /**
     * Clears the link db changes
     */
    public void clearChanges() {
        logger.debug("clearing link db changes for {}", device.getAddress());
        synchronized (changes) {
            changes.clear();
        }
    }

    /**
     * Adds a link db change
     *
     * @param change the change to add
     */
    private void addChange(LinkDBChange change) {
        synchronized (changes) {
            LinkDBChange prevChange = changes.put(change.getLocation(), change);
            if (prevChange == null) {
                logger.trace("added change: {}", change);
            } else {
                logger.trace("modified change from: {} to: {}", prevChange, change);
            }
        }
    }

    /**
     * Marks a link db record to be added
     *
     * @param address the record address to use
     * @param group the record group to use
     * @param isController if is controller record
     * @param data the record data to use
     */
    public void markRecordForAdd(InsteonAddress address, int group, boolean isController, byte[] data) {
        int location = getChangeLocation(address, group, isController, data[2]);
        addChange(LinkDBChange.forAdd(location, address, group, isController, data));
    }

    /**
     * Marks a link db record to be modified
     *
     * @param record the record to modify
     * @param data the record data to use
     */
    public void markRecordForModify(LinkDBRecord record, byte[] data) {
        addChange(LinkDBChange.forModify(record, data));
    }

    /**
     * Marks a link db record to be added or modified
     *
     * @param address the record address to use
     * @param group the record group to use
     * @param isController if is controller record
     * @param data the record data to use
     */
    public void markRecordForAddOrModify(InsteonAddress address, int group, boolean isController, byte[] data) {
        LinkDBRecord record = getActiveRecord(address, group, isController, data[2]);
        if (record == null) {
            markRecordForAdd(address, group, isController, data);
        } else {
            markRecordForModify(record, data);
        }
    }

    /**
     * Marks a link db record to be deleted
     *
     * @param record the record to delete
     */
    public void markRecordForDelete(LinkDBRecord record) {
        if (record.isAvailable()) {
            logger.debug("ignoring already deleted record: {}", record);
            return;
        }
        addChange(LinkDBChange.forDelete(record));
    }

    /**
     * Marks a link db record to be deleted
     *
     * @param address the record address to use
     * @param group the record group to use
     * @param isController if is controller record
     * @param componentId the record component id to use
     */
    public void markRecordForDelete(InsteonAddress address, int group, boolean isController, int componentId) {
        LinkDBRecord record = getActiveRecord(address, group, isController, componentId);
        if (record == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("no active record found for {} group:{} isController:{} componentId:{}", address, group,
                        isController, HexUtils.getHexString(componentId));
            }
            return;
        }
        markRecordForDelete(record);
    }

    /**
     * Updates link database delta
     *
     * @param newDelta the database delta to update to
     */
    public void updateDatabaseDelta(int newDelta) {
        int oldDelta = getDatabaseDelta();
        // ignore delta if not defined or equal to old one
        if (newDelta == -1 || oldDelta == newDelta) {
            return;
        }
        // set database delta
        setDatabaseDelta(newDelta);
        // set db to reload if old delta defined and less than new one
        if (oldDelta != -1 && oldDelta < newDelta) {
            setReload(true);
        }
    }

    /**
     * Updates link database status
     */
    public synchronized void updateStatus() {
        if (records.isEmpty()) {
            logger.debug("no link db records for {}", device.getAddress());
            setStatus(DatabaseStatus.EMPTY);
            return;
        }

        int firstLocation = records.firstKey();
        int lastLocation = records.lastKey();
        int expected = (firstLocation - lastLocation) / LinkDBRecord.SIZE + 1;
        if (firstLocation != getFirstRecordLocation()) {
            logger.debug("got unexpected first record location for {}", device.getAddress());
            setStatus(DatabaseStatus.PARTIAL);
        } else if (!records.lastEntry().getValue().isLast()) {
            logger.debug("got unexpected last record type for {}", device.getAddress());
            setStatus(DatabaseStatus.PARTIAL);
        } else if (records.size() != expected) {
            logger.debug("got {} records for {} expected {}", records.size(), device.getAddress(), expected);
            setStatus(DatabaseStatus.PARTIAL);
        } else {
            logger.debug("got complete link db records ({}) for {} ", records.size(), device.getAddress());
            setStatus(DatabaseStatus.COMPLETE);
        }
    }

    /**
     * Returns broadcast group for a given component id
     *
     * @param componentId the record data3 field
     * @return list of the broadcast groups
     */
    public List<Integer> getBroadcastGroups(int componentId) {
        List<Integer> groups = List.of();
        InsteonModem modem = getModem();
        if (modem != null) {
            // unique groups from modem responder records matching component id and on level > 0
            groups = getRecords().stream()
                    .filter(record -> record.isActive() && record.isResponder()
                            && record.getAddress().equals(modem.getAddress()) && record.getComponentId() == componentId
                            && record.getOnLevel() > 0)
                    .map(LinkDBRecord::getGroup).filter(modem.getDB()::isValidBroadcastGroup).map(Integer::valueOf)
                    .distinct().toList();
        }
        return groups;
    }

    /**
     * Returns a list of related devices for a given group
     *
     * @param group the record group
     * @return list of related device addresses
     */
    public List<InsteonAddress> getRelatedDevices(int group) {
        List<InsteonAddress> devices = List.of();
        InsteonModem modem = getModem();
        if (modem != null) {
            // unique addresses from controller records matching group and is in modem database
            devices = getRecords().stream()
                    .filter(record -> record.isActive() && record.isController() && record.getGroup() == group
                            && modem.getDB().hasEntry(record.getAddress()))
                    .map(LinkDBRecord::getAddress).distinct().toList();
        }
        return devices;
    }
}
