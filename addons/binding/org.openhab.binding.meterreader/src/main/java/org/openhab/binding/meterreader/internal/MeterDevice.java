/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.meterreader.internal;

import java.util.Collection;
import java.util.HashMap;

import org.openhab.binding.meterreader.connectors.IMeterReaderConnector;
import org.openhab.binding.meterreader.internal.helper.ProtocolMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author MatthiasS
 *
 * @param <T>
 */
public abstract class MeterDevice<T> {

    /**
     * Controls wether the device info is logged to the OSGi console.
     */
    protected boolean printMeterInfo;
    /**
     * Map of all values captured from the device during the read request.
     */
    protected HashMap<String, MeterValue> valueCache;
    protected byte[] initMessage;
    /**
     * The id of the SML device from openHAB configuration.
     */
    String deviceId;

    /**
     * Used to establish the device connection
     */
    IMeterReaderConnector<T> connector;
    private final static Logger logger = LoggerFactory.getLogger(MeterDevice.class);

    public MeterDevice(String deviceId, String serialPort, byte[] initMessage, int baudrate, int baudrateChangeDelay,
            ProtocolMode protocolMode) {
        super();
        this.deviceId = deviceId;
        this.valueCache = new HashMap<String, MeterValue>();
        this.connector = createConnector(serialPort, baudrate, baudrateChangeDelay, protocolMode);
    }

    protected abstract IMeterReaderConnector<T> createConnector(String serialPort, int baudrate,
            int baudrateChangeDelay, ProtocolMode protocolMode);

    /**
     * Gets the configured deviceId.
     *
     * @return the id of the SmlDevice from openHAB configuration.
     */
    public String getDeviceId() {
        return deviceId;
    }

    /**
     * Returns the specified OBIS value if available.
     *
     * @param obis the OBIS code which value should be retrieved.
     * @return the OBIS value as String if available - otherwise null.
     */
    public String getValue(String obisId) {
        MeterValue smlValue = getSmlValue(obisId);
        if (smlValue != null) {
            return smlValue.getValue();
        }
        return null;
    }

    /**
     * Returns the specified OBIS value if available.
     *
     * @param obis the OBIS code which value should be retrieved.
     * @return the OBIS value if available - otherwise null.
     */
    public MeterValue getSmlValue(String obisId) {

        if (valueCache.containsKey(obisId)) {
            return valueCache.get(obisId);
        }

        return null;
    }

    public Collection<String> getObisCodes() {
        return this.valueCache.keySet();
    }

    /**
     * Read values from this device an store them locally against their OBIS code.
     *
     * @throws Exception
     */
    protected void readValues() throws Exception {
        T smlFile = null;

        if (connector == null) {
            logger.error("{}: connector is not instantiated", this.toString());
            return;
        }

        try {
            smlFile = connector.getMeterValues(initMessage);
            clearValueCache();
        } catch (Exception ex) {
            logger.error("{}: Error during receive values from device: {}", this.toString(), ex.getMessage());
            throw ex;
        }

        populateValueCache(smlFile);
    }

    /**
     * Deletes all cached values.
     *
     * The method will always be called before new values are populated.
     */
    protected void clearValueCache() {
        valueCache.clear();
    }

    protected abstract void populateValueCache(T smlFile);

    protected void addObisCache(String obisCode, MeterValue value) {
        this.valueCache.put(obisCode, value);
    }

    @Override
    public String toString() {
        return this.getDeviceId();
    }
}