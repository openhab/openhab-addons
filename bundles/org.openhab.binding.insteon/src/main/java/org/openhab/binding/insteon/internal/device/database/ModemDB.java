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

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.insteon.internal.device.InsteonAddress;
import org.openhab.binding.insteon.internal.device.ProductData;
import org.openhab.binding.insteon.internal.device.ProductDataLoader;
import org.openhab.binding.insteon.internal.message.FieldException;
import org.openhab.binding.insteon.internal.message.Msg;
import org.openhab.binding.insteon.internal.utils.ByteUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The ModemDB class holds all-link database entries for the modem
 *
 * @author Jeremy Setton - Initial contribution
 */
@NonNullByDefault
public class ModemDB {
    private final Logger logger = LoggerFactory.getLogger(ModemDB.class);

    private volatile boolean complete = false;
    private Map<InsteonAddress, ModemDBEntry> dbes = new ConcurrentHashMap<>();

    public List<ModemDBEntry> getEntries() {
        return dbes.values().stream().collect(Collectors.toList());
    }

    public @Nullable ModemDBEntry getEntry(InsteonAddress address) {
        return dbes.get(address);
    }

    public boolean hasEntry(InsteonAddress address) {
        return dbes.containsKey(address);
    }

    public @Nullable ProductData getProductData(InsteonAddress address) {
        ModemDBEntry dbe = getEntry(address);
        return dbe != null ? dbe.getProductData() : null;
    }

    public boolean hasProductData(InsteonAddress address) {
        return getProductData(address) != null;
    }

    public boolean isComplete() {
        return complete;
    }

    public void setIsComplete(boolean complete) {
        this.complete = complete;
    }

    /**
     * Adds record to database
     *
     * @param msg link record message to add
     */
    public void addRecord(Msg msg) {
        try {
            InsteonAddress linkAddr = msg.getAddress("LinkAddr");
            ModemDBEntry dbe = getEntry(linkAddr);
            if (dbe == null) {
                dbe = new ModemDBEntry(linkAddr);
                dbes.put(linkAddr, dbe);
            }

            ModemDBRecord dbr = ModemDBRecord.fromRecordMsg(msg);
            dbe.addRecord(dbr);
        } catch (FieldException e) {
            logger.warn("cannot access field:", e);
        }
    }

    /**
     * Deletes record from database
     *
     * @param msg link record message to delete
     */
    public void deleteRecord(Msg msg) {
        try {
            InsteonAddress linkAddr = msg.getAddress("LinkAddr");
            ModemDBEntry dbe = getEntry(linkAddr);
            if (dbe == null) {
                return;
            }

            ModemDBRecord dbr = ModemDBRecord.fromRecordMsg(msg);
            dbe.deleteRecord(dbr);

            if (dbe.getRecords().isEmpty()) {
                dbes.remove(linkAddr);
            }
        } catch (FieldException e) {
            logger.warn("cannot access field:", e);
        }
    }

    /**
     * Sets product data for a defined database entry
     *
     * @param msg product data message to use
     */
    public void setProductData(Msg msg) {
        try {
            InsteonAddress fromAddr = msg.getAddress("fromAddress");
            ModemDBEntry dbe = getEntry(fromAddr);
            if (dbe == null || dbe.hasProductData()) {
                return;
            }

            InsteonAddress toAddr = msg.getAddress("toAddress");
            String deviceCategory = ByteUtils.getHexString(toAddr.getHighByte());
            String subCategory = ByteUtils.getHexString(toAddr.getMiddleByte());
            int firmware = toAddr.getLowByte() & 0xFF;
            int hardware = msg.getInt("command2");

            ProductData productData = ProductDataLoader.instance().getProductData(deviceCategory, subCategory);
            productData.setFirmwareVersion(firmware);
            productData.setHardwareVersion(hardware);
            dbe.setProductData(productData);

            if (logger.isTraceEnabled()) {
                logger.trace("got product data for {} as {}", fromAddr, productData);
            }
        } catch (FieldException e) {
            logger.warn("cannot access field:", e);
        }
    }

    /**
     * Clears all database entries
     */
    public void clearEntries() {
        logger.debug("clearing modem db!");
        dbes.clear();
        complete = false;
    }

    /**
     * Logs all database entries
     */
    public void logEntries() {
        if (logger.isDebugEnabled()) {
            if (dbes.isEmpty()) {
                logger.debug("the modem database is empty");
            } else {
                logger.debug("the modem database has {} entries:", dbes.size());
                getEntries().stream().map(String::valueOf).forEach(logger::debug);
                if (logger.isTraceEnabled()) {
                    logger.trace("---------------- start of modem link records ----------------");
                    getEntries().stream().map(ModemDBEntry::getRecords).flatMap(List::stream).map(String::valueOf)
                            .forEach(logger::trace);
                    logger.trace("----------------- end of modem link records -----------------");
                }
            }
        }
    }

    /**
     * Logs a database entry for a given address
     *
     * @param address the database link address to log
     */
    public void logEntry(InsteonAddress address) {
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
     * Returns a list of related devices for a given broadcast group
     *
     * @param group the broadcast group
     * @return list of related device addresses
     */
    public List<InsteonAddress> getRelatedDevices(int group) {
        return getEntries().stream().filter(dbe -> dbe.getControllerGroups().contains(group))
                .map(ModemDBEntry::getAddress).collect(Collectors.toList());
    }

    /**
     * Returns a list of all broadcast groups
     *
     * @return list of all broadcast groups
     */
    public List<Integer> getBroadcastGroups() {
        return getEntries().stream().map(ModemDBEntry::getControllerGroups).flatMap(List::stream).distinct()
                .filter(group -> group != 0).collect(Collectors.toList());
    }

    /**
     * Returns if a broadcast group is in modem database
     *
     * @param group the broadcast group
     * @return true if the broadcast group number is in modem database
     */
    public boolean hasBroadcastGroup(int group) {
        return getEntries().stream().anyMatch(dbe -> dbe.getControllerGroups().contains(group));
    }
}
