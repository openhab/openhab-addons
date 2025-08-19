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

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.insteon.internal.device.InsteonAddress;
import org.openhab.binding.insteon.internal.device.InsteonModem;
import org.openhab.binding.insteon.internal.device.InsteonScene;
import org.openhab.binding.insteon.internal.device.ProductData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ModemDB} holds all-link database entries for a modem
 *
 * @author Jeremy Setton - Initial contribution
 */
@NonNullByDefault
public class ModemDB {
    /**
     * List of known default controller groups with 0 being the standard one,
     * while older third party software have used 254 or 255
     */
    private static final List<Integer> DEFAULT_CONTROLLER_GROUPS = List.of(0, 254, 255);

    private final Logger logger = LoggerFactory.getLogger(ModemDB.class);

    private final InsteonModem modem;
    private final Map<InsteonAddress, ModemDBEntry> dbes = new HashMap<>();
    private final List<ModemDBRecord> records = new ArrayList<>();
    private final List<ModemDBChange> changes = new ArrayList<>();
    private volatile int defaultControllerGroup = 0;
    private volatile boolean complete = false;

    public ModemDB(InsteonModem modem) {
        this.modem = modem;
    }

    public DatabaseManager getDatabaseManager() {
        return modem.getDBM();
    }

    public int getDefaultControllerGroup() {
        return defaultControllerGroup;
    }

    public List<InsteonAddress> getDevices() {
        synchronized (dbes) {
            return dbes.keySet().stream().toList();
        }
    }

    public List<ModemDBEntry> getEntries() {
        synchronized (dbes) {
            return dbes.values().stream().toList();
        }
    }

    public @Nullable ModemDBEntry getEntry(InsteonAddress address) {
        synchronized (dbes) {
            return dbes.get(address);
        }
    }

    private ModemDBEntry getOrAddEntry(InsteonAddress address) {
        synchronized (dbes) {
            return Objects.requireNonNull(dbes.computeIfAbsent(address, a -> new ModemDBEntry(a, this)));
        }
    }

    public boolean hasEntry(InsteonAddress address) {
        synchronized (dbes) {
            return dbes.containsKey(address);
        }
    }

    private void deleteEntry(InsteonAddress address) {
        synchronized (dbes) {
            dbes.remove(address);
        }
    }

    public List<ModemDBRecord> getRecords() {
        synchronized (records) {
            return records.stream().toList();
        }
    }

    public byte[] getRecordDump() {
        return getRecords().stream().distinct().map(ModemDBRecord::getBytes)
                .flatMapToInt(bytes -> IntStream.range(0, bytes.length).map(i -> bytes[i]))
                .collect(ByteArrayOutputStream::new, ByteArrayOutputStream::write,
                        (out1, out2) -> out1.write(out2.toByteArray(), 0, out2.size()))
                .toByteArray();
    }

    private Stream<ModemDBRecord> getRecords(@Nullable InsteonAddress address, @Nullable Integer group,
            @Nullable Boolean isController) {
        return getRecords().stream()
                .filter(record -> (address == null || record.getAddress().equals(address))
                        && (group == null || record.getGroup() == group)
                        && (isController == null || record.isController() == isController));
    }

    public List<ModemDBRecord> getRecords(InsteonAddress address) {
        return getRecords(address, null, null).toList();
    }

    public @Nullable ModemDBRecord getRecord(InsteonAddress address, int group, boolean isController) {
        return getRecords(address, group, isController).findFirst().orElse(null);
    }

    public @Nullable ModemDBRecord getRecord(InsteonAddress address, int group) {
        return getRecords(address, group, null).findFirst().orElse(null);
    }

    private int getRecordIndex(ModemDBRecord record) {
        synchronized (records) {
            return records.indexOf(record);
        }
    }

    private int getRecordIndex(InsteonAddress address, int group, boolean isController) {
        return getRecords(address, group, isController).mapToInt(this::getRecordIndex).findFirst().orElse(-1);
    }

    private int getRecordIndex(InsteonAddress address, int group) {
        return getRecords(address, group, null).mapToInt(this::getRecordIndex).findFirst().orElse(-1);
    }

    public boolean hasRecord(@Nullable InsteonAddress address, @Nullable Integer group,
            @Nullable Boolean isController) {
        return getRecords(address, group, isController).findAny().isPresent();
    }

    public List<ModemDBChange> getChanges() {
        synchronized (changes) {
            return changes.stream().toList();
        }
    }

    private Stream<ModemDBChange> getChanges(@Nullable InsteonAddress address, @Nullable Integer group,
            @Nullable Boolean isController) {
        return getChanges().stream()
                .filter(change -> (address == null || change.getRecord().getAddress().equals(address))
                        && (group == null || change.getRecord().getGroup() == group)
                        && (isController == null || change.getRecord().isController() == isController));
    }

    private int getChangeIndex(ModemDBChange change) {
        synchronized (changes) {
            return changes.indexOf(change);
        }
    }

    private int getChangeIndex(InsteonAddress address, int group, boolean isController) {
        return getChanges(address, group, isController).mapToInt(this::getChangeIndex).findFirst().orElse(-1);
    }

    public @Nullable ModemDBChange pollNextChange() {
        synchronized (changes) {
            return changes.isEmpty() ? null : changes.remove(0);
        }
    }

    public Map<InsteonAddress, ProductData> getProducts() {
        return getEntries().stream().filter(dbe -> dbe.getProductData() != null).collect(
                Collectors.toMap(ModemDBEntry::getAddress, dbe -> Objects.requireNonNull(dbe.getProductData())));
    }

    public @Nullable ProductData getProductData(InsteonAddress address) {
        return getProducts().get(address);
    }

    public boolean hasProductData(InsteonAddress address) {
        return getProducts().containsKey(address);
    }

    public boolean isComplete() {
        return complete;
    }

    public void setIsComplete(boolean complete) {
        this.complete = complete;

        if (complete) {
            modem.databaseCompleted();
        }
    }

    /**
     * Clears the modem db
     */
    public synchronized void clear() {
        logger.debug("clearing modem db");
        dbes.clear();
        records.clear();
        changes.clear();
        complete = false;
    }

    /**
     * Loads the modem db
     */
    public void load() {
        getDatabaseManager().read(modem, 0L);
    }

    /**
     * Updates the modem db with changes
     */
    public void update() {
        if (getChanges().isEmpty()) {
            logger.debug("no changes to update modem db");
        } else {
            getDatabaseManager().write(modem, 0L);
        }
    }

    /**
     * Adds a modem db record
     *
     * @param record the record to add
     */
    public void addRecord(ModemDBRecord record) {
        InsteonAddress address = record.getAddress();
        ModemDBEntry dbe = getOrAddEntry(address);

        int index = getRecordIndex(record);
        if (index != -1) {
            logger.trace("duplicate record: {}", record);
            return;
        }

        synchronized (records) {
            records.add(record);
        }

        if (record.isController()) {
            dbe.addControllerGroup(record.getGroup());
        } else if (record.isResponder()) {
            dbe.addResponderGroup(record.getGroup());
        }

        logger.trace("added record: {}", record);
    }

    /**
     * Deletes modem db record
     *
     * @param record the record to delete
     */
    public void deleteRecord(ModemDBRecord record) {
        InsteonAddress address = record.getAddress();
        ModemDBEntry dbe = getEntry(address);
        if (dbe == null) {
            return;
        }

        synchronized (records) {
            records.remove(record);
        }

        if (!dbe.hasRecords()) {
            deleteEntry(address);
        } else if (record.isController()) {
            dbe.removeControllerGroup(record.getGroup());
        } else if (record.isResponder()) {
            dbe.removeResponderGroup(record.getGroup());
        }

        logger.trace("deleted record: {}", record);
    }

    /**
     * Deletes modem db record for a given address and group
     *
     * @param address the record address
     * @param group the record group to delete
     */
    public void deleteRecord(InsteonAddress address, int group) {
        ModemDBRecord record = getRecord(address, group);
        if (record == null) {
            logger.trace("no record found to delete for {} group:{}", address, group);
        } else {
            deleteRecord(record);
        }
    }

    /**
     * Loads a list of modem db records
     *
     * @param records list of records to load
     */
    public void loadRecords(List<ModemDBRecord> records) {
        logger.debug("loading modem db records");
        records.forEach(this::addRecord);
        recordsLoaded();
    }

    /**
     * Modifies a modem db record
     *
     * @param index the record index to modify
     * @param record the record to use
     */
    public void modifyRecord(int index, ModemDBRecord record) {
        InsteonAddress address = record.getAddress();
        ModemDBEntry dbe = getEntry(address);
        if (dbe == null || index < 0 || index >= records.size()) {
            return;
        }

        ModemDBRecord prevRecord;
        synchronized (records) {
            if (records.get(index).equals(record)) {
                logger.trace("no change needed for record: {}", record);
                return;
            }
            prevRecord = records.set(index, record);
        }

        if (prevRecord.isController()) {
            dbe.removeControllerGroup(prevRecord.getGroup());
        } else if (prevRecord.isResponder()) {
            dbe.removeResponderGroup(prevRecord.getGroup());
        }

        if (record.isController()) {
            dbe.addControllerGroup(record.getGroup());
        } else if (record.isResponder()) {
            dbe.addResponderGroup(record.getGroup());
        }

        logger.trace("modified record from: {} to: {}", prevRecord, record);
    }

    /**
     * Modifies first controller or responder modem db record if found or adds it
     *
     * @param record the record to modify or add
     */
    public void modifyOrAddRecord(ModemDBRecord record) {
        int index = getRecordIndex(record.getAddress(), record.getGroup());
        if (index != -1) {
            modifyRecord(index, record);
        } else {
            addRecord(record);
        }
    }

    /**
     * Modifies first controller modem db record if found or adds it
     *
     * @param record the record to modify or add
     */
    public void modifyOrAddControllerRecord(ModemDBRecord record) {
        int index = getRecordIndex(record.getAddress(), record.getGroup(), true);
        if (index != -1) {
            modifyRecord(index, record);
        } else {
            addRecord(record);
        }
    }

    /**
     * Modifies first responder modem db record if found or adds it
     *
     * @param record the record to modify or add
     */

    public void modifyOrAddResponderRecord(ModemDBRecord record) {
        int index = getRecordIndex(record.getAddress(), record.getGroup(), false);
        if (index != -1) {
            modifyRecord(index, record);
        } else {
            addRecord(record);
        }
    }

    /**
     * Clears the modem db changes
     */
    public void clearChanges() {
        logger.debug("clearing modem db changes");

        synchronized (changes) {
            changes.clear();
        }
    }

    /**
     * Adds a modem db change
     *
     * @param change the change to add
     */
    private void addChange(ModemDBChange change) {
        ModemDBRecord record = change.getRecord();
        int index = getChangeIndex(record.getAddress(), record.getGroup(), record.isController());
        if (index == -1) {
            synchronized (changes) {
                changes.add(change);
            }
            logger.trace("added change: {}", change);
        } else {
            ModemDBChange prevChange;
            synchronized (changes) {
                prevChange = changes.set(index, change);
            }
            logger.trace("modified change from: {} to: {}", prevChange, change);
        }
    }

    /**
     * Marks a modem db record to be added
     *
     * @param address the record address to use
     * @param group the record group to use
     * @param isController if is controller record
     * @param data the record data to use
     */
    public void markRecordForAdd(InsteonAddress address, int group, boolean isController, byte[] data) {
        addChange(ModemDBChange.forAdd(address, group, isController, data));
    }

    /**
     * Marks a modem db record to be modified
     *
     * @param record the record to modify
     * @param data the record data to use
     */
    public void markRecordForModify(ModemDBRecord record, byte[] data) {
        addChange(ModemDBChange.forModify(record, data));
    }

    /**
     * Marks a modem db record to be added or modified
     *
     * @param address the record address to use
     * @param group the record group to use
     * @param isController if is controller record
     * @param data the record data to use
     */
    public void markRecordForAddOrModify(InsteonAddress address, int group, boolean isController, byte[] data) {
        ModemDBRecord record = getRecord(address, group, isController);
        if (record == null) {
            markRecordForAdd(address, group, isController, data);
        } else {
            markRecordForModify(record, data);
        }
    }

    /**
     * Marks a modem db record to be added or modified
     *
     * @param address the record address to use
     * @param group the record group to use
     * @param isController if is controller record
     */
    public void markRecordForAddOrModify(InsteonAddress address, int group, boolean isController) {
        ProductData productData = getProductData(address);
        if (productData == null) {
            logger.debug("no product data for device {}", address);
            return;
        }
        byte[] data = isController ? productData.getRecordData() : new byte[3];
        markRecordForAddOrModify(address, group, isController, data);
    }

    /**
     * Marks a modem db record to be deleted
     *
     * @param record the record to delete
     */
    public void markRecordForDelete(ModemDBRecord record) {
        if (record.isAvailable()) {
            logger.debug("ignoring already deleted record: {}", record);
            return;
        }
        addChange(ModemDBChange.forDelete(record));
    }

    /**
     * Marks a modem db record to be deleted
     *
     * @param address the record address to use
     * @param group the record group to use
     */
    public void markRecordForDelete(InsteonAddress address, int group) {
        ModemDBRecord record = getRecord(address, group);
        if (record == null) {
            logger.debug("no record found to delete for {} group:{}", address, group);
            return;
        }
        markRecordForDelete(record);
    }

    /**
     * Logs all modem db entries
     */
    private void logEntries() {
        if (logger.isDebugEnabled()) {
            List<ModemDBEntry> dbes = getEntries();
            if (dbes.isEmpty()) {
                logger.debug("modem database is empty");
            } else {
                logger.debug("modem database has {} entries:", dbes.size());
                dbes.stream().map(String::valueOf).forEach(logger::debug);
                if (logger.isTraceEnabled()) {
                    logger.trace("---------------- start of modem link records ----------------");
                    getRecords().stream().map(String::valueOf).forEach(logger::trace);
                    logger.trace("----------------- end of modem link records -----------------");
                }
            }
        }
    }

    /**
     * Logs a modem db entry for a given address
     *
     * @param address the address for the modem db entry to log
     */
    private void logEntry(InsteonAddress address) {
        if (logger.isDebugEnabled()) {
            ModemDBEntry dbe = getEntry(address);
            if (dbe == null) {
                logger.debug("no modem database entry for {}", address);
            } else {
                logger.debug("{}", dbe);
                if (logger.isTraceEnabled()) {
                    logger.trace("--------- start of modem link records for {} ---------", address);
                    dbe.getRecords().stream().map(String::valueOf).forEach(logger::trace);
                    logger.trace("---------- end of modem link records for {} ----------", address);
                }
            }
        }
    }

    /**
     * Notifies that a modem db link has been updated
     *
     * @param address the link address
     * @param group the link group
     * @param is2Way if two way update
     */
    public void linkUpdated(InsteonAddress address, int group, boolean is2Way) {
        logEntry(address);
        modem.databaseLinkUpdated(address, group, is2Way);
    }

    /**
     * Notifies that the modem db records have been loaded
     */
    public void recordsLoaded() {
        logEntries();
        setDefaultControllerGroup();
    }

    /**
     * Sets the default controller group
     */
    private void setDefaultControllerGroup() {
        int maxSize = 0;
        int defaultControllerGroup = 0;
        // Set the default controller group to the one with the most related devices
        for (int group : DEFAULT_CONTROLLER_GROUPS) {
            int size = getRelatedDevices(group).size();
            if (size > maxSize) {
                maxSize = size;
                defaultControllerGroup = group;
            }
        }

        logger.debug("set default controller group to {}", defaultControllerGroup);
        this.defaultControllerGroup = defaultControllerGroup;
    }

    /**
     * Loads a map of products
     *
     * @param products map of products to load
     */
    public void loadProducts(Map<InsteonAddress, ProductData> products) {
        logger.debug("loading modem db products");
        products.forEach(this::setProductData);
    }

    /**
     * Sets product data for a modem db entry
     *
     * @param address the address for the modem db entry
     * @param productData the product data to set
     */
    public void setProductData(InsteonAddress address, ProductData productData) {
        ModemDBEntry dbe = getOrAddEntry(address);
        dbe.setProductData(productData);
        modem.databaseProductDataUpdated(address, productData);
        logger.trace("set product data for {} as {}", address, productData);
    }

    /**
     * Returns a list of related devices for a given group
     *
     * @param group the group
     * @return list of related device addresses
     */
    public List<InsteonAddress> getRelatedDevices(int group) {
        return getEntries().stream().filter(dbe -> dbe.getControllerGroups().contains(group))
                .map(ModemDBEntry::getAddress).toList();
    }

    /**
     * Returns a list of all broadcast groups
     *
     * @return list of all broadcast groups
     */
    public List<Integer> getBroadcastGroups() {
        return getEntries().stream().map(ModemDBEntry::getControllerGroups).flatMap(List::stream).distinct()
                .filter(this::isValidBroadcastGroup).toList();
    }

    /**
     * Returns if a broadcast group is in modem database
     *
     * @param group the broadcast group
     * @return true if the broadcast group number is in modem database
     */
    public boolean hasBroadcastGroup(int group) {
        return getBroadcastGroups().contains(group);
    }

    /**
     * Returns if a broadcast group is valid
     *
     * @param group the broadcast group
     * @return true if the broadcast group number is valid and not the default controller group
     */
    public boolean isValidBroadcastGroup(int group) {
        return InsteonScene.isValidGroup(group) && group != defaultControllerGroup;
    }

    /**
     * Returns the next available broadcast group
     */
    public int getNextAvailableBroadcastGroup() {
        return IntStream.range(InsteonScene.GROUP_NEW_MIN, InsteonScene.GROUP_NEW_MAX)
                .filter(group -> isValidBroadcastGroup(group) && !hasBroadcastGroup(group)).min().orElse(-1);
    }
}
