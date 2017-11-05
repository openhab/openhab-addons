/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.meterreader.internal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

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
    private boolean printMeterInfo;
    /**
     * Map of all values captured from the device during the read request.
     */
    private Map<String, MeterValue> valueCache;
    private byte[] initMessage;
    /**
     * The id of the SML device from openHAB configuration.
     */
    private String deviceId;

    /**
     * Used to establish the device connection
     */
    IMeterReaderConnector<T> connector;
    private List<MeterValueListener> valueChangeListeners;
    private final static Logger logger = LoggerFactory.getLogger(MeterDevice.class);

    public MeterDevice(String deviceId, String serialPort, byte[] initMessage, int baudrate, int baudrateChangeDelay,
            ProtocolMode protocolMode) {
        super();
        this.deviceId = deviceId;
        this.valueCache = new HashMap<String, MeterValue>();
        this.valueChangeListeners = new ArrayList<>();
        this.printMeterInfo = true;
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
        return new ArrayList<>(this.valueCache.keySet());
    }

    /**
     * Read values from this device an store them locally against their OBIS code.
     *
     * @return
     *
     * @throws Exception
     */
    protected Cancelable readValues(ScheduledExecutorService executorService, long period) throws Exception {
        if (connector == null) {
            throw new IllegalArgumentException("{}: connector is not instantiated: " + this.toString());
        }

        try {
            connector.addValueChangeListener((value) -> {
                Map<String, MeterValue> obisCodes = new HashMap<>(valueCache);
                clearValueCache();
                populateValueCache(value);
                printInfo();
                Collection<String> newObisCodes = getObisCodes();
                // notify every removed obis code.
                obisCodes.values().stream().filter((val) -> !newObisCodes.contains(val.getObisCode()))
                        .forEach((val) -> notifyValuesRemoved(val));
            });

            ScheduledFuture<?> future = executorService.scheduleWithFixedDelay(() -> {
                try {
                    connector.getMeterValues(initMessage);
                } catch (IOException e) {
                    notifyReadingError(e);
                }

            }, 0, period, TimeUnit.SECONDS);

            return new Cancelable() {

                @Override
                public void cancel() {
                    future.cancel(true);
                    connector.closeConnection();
                }
            };

        } catch (Exception ex) {
            logger.error("{}: Error during receive values from device: {}", this.toString(), ex.getMessage());
            throw ex;
        }

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
        this.valueChangeListeners.forEach((listener) -> listener.valueChanged(value));
    }

    @Override
    public String toString() {
        return this.getDeviceId();
    }

    public void addValueChangeListener(MeterValueListener valueChangeListener) {
        this.valueChangeListeners.add(valueChangeListener);
    }

    public void removeValueChangeListener(MeterValueListener valueChangeListener) {
        this.valueChangeListeners.remove(valueChangeListener);
    }

    private void notifyValuesRemoved(MeterValue value) {
        this.valueChangeListeners.forEach((listener) -> listener.valueRemoved(value));
    }

    private void notifyReadingError(Exception e) {
        this.valueChangeListeners.forEach((listener) -> listener.errorOccoured(e));
    }

    /**
     * Logs the object information with all given SML values to OSGi console.
     *
     * It's only called once - except the config was updated.
     */
    private void printInfo() {
        if (this.getPrintMeterInfo()) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(this.toString());
            stringBuilder.append(System.lineSeparator());

            for (Entry<String, MeterValue> entry : valueCache.entrySet()) {
                stringBuilder.append("Obis: " + entry.getKey() + " " + entry.getValue().toString());
                stringBuilder.append(System.lineSeparator());
            }

            logger.info("", stringBuilder);
            setPrintMeterInfo(false);
        }
    }

    /**
     * Gets if the object information has to be logged to OSGi console.
     *
     * @return true if the object information should be logged, otherwise false.
     */
    private Boolean getPrintMeterInfo() {
        return this.printMeterInfo;
    }

    /**
     * Sets if the object information has to be logged to OSGi console.
     */
    private void setPrintMeterInfo(Boolean printMeterInfo) {
        this.printMeterInfo = printMeterInfo;
    }

}