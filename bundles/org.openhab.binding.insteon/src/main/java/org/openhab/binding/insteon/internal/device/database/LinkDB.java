/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.insteon.internal.device.InsteonAddress;
import org.openhab.binding.insteon.internal.device.InsteonDevice;
import org.openhab.binding.insteon.internal.utils.ByteUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The LinkDB class holds all-link database records for a device
 *
 * @author Jeremy Setton - Initial contribution
 */
@NonNullByDefault
public class LinkDB {
    public static final int RECORD_BYTE_SIZE = 8;

    private final Logger logger = LoggerFactory.getLogger(LinkDB.class);

    private enum LinkDBStatus {
        EMPTY,
        COMPLETE,
        PARTIAL,
        LOADING
    }

    private InsteonDevice device;
    private LinkDBStatus status = LinkDBStatus.EMPTY;
    private TreeMap<Integer, LinkDBRecord> records = new TreeMap<>(Collections.reverseOrder());
    private int delta = -1;
    private int firstRecord = 0x0FFF;
    private boolean refresh = false;

    public LinkDB(InsteonDevice device) {
        this.device = device;
    }

    public int getDatabaseDelta() {
        return delta;
    }

    public int getFirstRecordOffset() {
        return firstRecord;
    }

    public List<LinkDBRecord> getRecords() {
        return records.values().stream().collect(Collectors.toList());
    }

    public boolean isComplete() {
        return status == LinkDBStatus.COMPLETE;
    }

    public boolean shouldRefresh() {
        return refresh;
    }

    public synchronized void setDatabaseDelta(int delta) {
        if (logger.isTraceEnabled()) {
            logger.trace("setting link db delta to {} for {}", delta, device.getAddress());
        }
        this.delta = delta;
    }

    public synchronized void setFirstRecordOffset(int firstRecord) {
        if (logger.isTraceEnabled()) {
            logger.trace("setting link db first record offset to {} for {}", ByteUtils.getHexString(firstRecord),
                    device.getAddress());
        }
        this.firstRecord = firstRecord;
    }

    public synchronized void setRefresh(boolean refresh) {
        if (logger.isTraceEnabled()) {
            logger.trace("setting link db refresh to {} for {}", refresh, device.getAddress());
        }
        this.refresh = refresh;
    }

    private synchronized void setStatus(LinkDBStatus status) {
        if (logger.isTraceEnabled()) {
            logger.trace("setting link db status to {} for {}", status, device.getAddress());
        }
        this.status = status;
    }

    /**
     * Adds record to database
     *
     * @param record the record to add
     * @return true if record was added
     */
    public synchronized boolean addRecord(LinkDBRecord record) {
        if (status == LinkDBStatus.EMPTY) {
            setStatus(LinkDBStatus.LOADING);
        }
        if (status != LinkDBStatus.LOADING) {
            if (logger.isDebugEnabled()) {
                logger.debug("incorrect link db status for {}, ignoring record", device.getAddress());
            }
        } else if (records.containsKey(record.getOffset())) {
            if (logger.isDebugEnabled()) {
                logger.debug("duplicate link db record for {}, ignoring record", device.getAddress());
            }
        } else {
            records.put(record.getOffset(), record);
            return true;
        }
        return false;
    }

    /**
     * Clears all database records
     */
    public synchronized void clearRecords() {
        if (logger.isTraceEnabled()) {
            logger.trace("clearing link records for {}", device.getAddress());
        }
        records.clear();
        setStatus(LinkDBStatus.EMPTY);
    }

    /**
     * Loads database records
     *
     * @param records list of records to load
     */
    public void loadRecords(List<LinkDBRecord> records) {
        if (status != LinkDBStatus.EMPTY) {
            clearRecords();
        }
        if (logger.isTraceEnabled()) {
            logger.trace("loading link records for {}", device.getAddress());
        }
        records.forEach(record -> addRecord(record));
    }

    /**
     * Logs all database records
     */
    public void logRecords() {
        if (logger.isDebugEnabled()) {
            if (status == LinkDBStatus.EMPTY) {
                logger.debug("no link records found for {}", device.getAddress());
            } else {
                logger.debug("---------------- start of link records for {} ----------------", device.getAddress());
                getRecords().stream().map(String::valueOf).forEach(logger::debug);
                logger.debug("----------------- end of link records for {} -----------------", device.getAddress());
            }
        }
    }

    /**
     * Updates link database delta
     *
     * @param newDelta the database delta to update to
     */
    public void updateDatabaseDelta(int newDelta) {
        int oldDelta = this.delta;
        // ignore delta if not defined or equal to old one
        if (newDelta == -1 || oldDelta == newDelta) {
            return;
        }
        // set database delta
        setDatabaseDelta(newDelta);
        // set db to refresh if old delta defined and less than new one
        if (oldDelta != -1 && oldDelta < newDelta) {
            setRefresh(true);
        }
    }

    /**
     * Updates link database status
     */
    public void updateStatus() {
        if (records.isEmpty()) {
            setStatus(LinkDBStatus.EMPTY);
        } else {
            int firstOffset = records.firstKey();
            int lastOffset = records.lastKey();
            int expected = (firstOffset - lastOffset) / RECORD_BYTE_SIZE + 1;
            if (firstOffset != getFirstRecordOffset()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("got unexpected first record offset for {}", device.getAddress());
                }
                setStatus(LinkDBStatus.PARTIAL);
            } else if (!records.lastEntry().getValue().isLast()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("got unexpected last record type for {}", device.getAddress());
                }
                setStatus(LinkDBStatus.PARTIAL);
            } else if (records.size() != expected) {
                if (logger.isDebugEnabled()) {
                    logger.debug("got {} records for {} expected {}", records.size(), device.getAddress(), expected);
                }
                setStatus(LinkDBStatus.PARTIAL);
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("got complete link db records ({}) for {} ", records.size(), device.getAddress());
                }
                setStatus(LinkDBStatus.COMPLETE);
            }
        }
    }

    /**
     * Returns a list of broadcast groups for a given component id
     *
     * @param componentId the record data3 field
     * @param modemAddress the modem address
     * @return list of the broadcast groups
     */
    public List<Integer> getBroadcastGroups(int componentId, InsteonAddress modemAddress) {
        // unique groups from modem responder records matching component id and on level > 0
        return getRecords().stream()
                .filter(record -> record.isResponder() && record.getAddress().equals(modemAddress)
                        && record.getComponentId() == componentId && record.getOnLevel() > 0)
                .map(LinkDBRecord::getGroup).map(Integer::valueOf).distinct().collect(Collectors.toList());
    }

    /**
     * Returns a list of related devices for a given group
     *
     * @param group the record group
     * @param modemDB the modem database
     * @return list of related device addresses
     */
    public List<InsteonAddress> getRelatedDevices(int group, ModemDB modemDB) {
        // unique addresses from controller records matching group and is in modem database
        return getRecords().stream()
                .filter(record -> record.isController() && record.getGroup() == group
                        && modemDB.hasEntry(record.getAddress()))
                .map(LinkDBRecord::getAddress).distinct().collect(Collectors.toList());
    }

    /**
     * Returns a list of responder records for a given controller address and group
     *
     * @param address the controller address
     * @param group the controller group
     * @return list of responder records
     */
    public List<LinkDBRecord> getResponderRecords(InsteonAddress address, int group) {
        // responder records matching address and group
        return getRecords().stream().filter(
                record -> record.isResponder() && record.getAddress().equals(address) && record.getGroup() == group)
                .collect(Collectors.toList());
    }
}
