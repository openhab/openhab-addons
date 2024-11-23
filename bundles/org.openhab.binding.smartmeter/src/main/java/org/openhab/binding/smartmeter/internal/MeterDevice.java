/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.smartmeter.internal;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

import javax.measure.Quantity;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.smartmeter.connectors.IMeterReaderConnector;
import org.openhab.binding.smartmeter.internal.helper.ProtocolMode;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import io.reactivex.exceptions.UndeliverableException;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.Schedulers;

/**
 * This represents a meter device.
 * All read values of the device are cached here and can be obtained. The reading can be started with
 * {@link #readValues(long, ScheduledExecutorService, Duration)}
 *
 * @author Matthias Steigenberger - Initial contribution
 *
 * @param <T> The type of Payload which is read from the device.
 */
@NonNullByDefault
public abstract class MeterDevice<T> {

    private static final int RETRY_DELAY = 2;
    private final Logger logger = LoggerFactory.getLogger(MeterDevice.class);
    /**
     * Controls wether the device info is logged to the OSGi console.
     */
    private boolean printMeterInfo;
    /**
     * Map of all values captured from the device during the read request.
     */
    private Map<String, MeterValue<?>> valueCache;
    private byte @Nullable [] initMessage;
    /**
     * The id of the SML device from openHAB configuration.
     */
    private String deviceId;
    /**
     * Used to establish the device connection
     */
    IMeterReaderConnector<T> connector;
    private List<MeterValueListener> valueChangeListeners;

    public MeterDevice(Supplier<SerialPortManager> serialPortManagerSupplier, String deviceId, String serialPort,
            byte @Nullable [] initMessage, int baudrate, int baudrateChangeDelay, ProtocolMode protocolMode) {
        super();
        this.deviceId = deviceId;
        this.valueCache = new HashMap<>();
        this.valueChangeListeners = new CopyOnWriteArrayList<>();
        this.printMeterInfo = true;
        this.connector = createConnector(serialPortManagerSupplier, serialPort, baudrate, baudrateChangeDelay,
                protocolMode);
        RxJavaPlugins.setErrorHandler(error -> {
            if (error == null) {
                logger.warn("Fatal but unknown error occurred");
                return;
            }
            if (error instanceof UndeliverableException) {
                error = error.getCause();
            }
            if (error instanceof IOException) {
                logger.warn("Connection related issue occurred: {}", error.getMessage());
                return;
            }
            logger.warn("Fatal error occurred", error);
        });
    }

    /**
     * Creates the actual connector that handles the serial port communication and protocol.
     *
     * @param serialPortManagerSupplier Supplies the {@link SerialPortManager} which is used to obtain the serial port
     *            implementation
     * @param serialPort The name of the port to communicate with.
     * @param baudrate The Baudrate to set for communication.
     * @param baudrateChangeDelay The delay which is used before changing the baudrate (used only for specific
     *            protocols).
     * @param protocolMode The {@link ProtocolMode} to use.
     * @return The connector which handles the serial port communication.
     */
    protected abstract IMeterReaderConnector<T> createConnector(Supplier<SerialPortManager> serialPortManagerSupplier,
            String serialPort, int baudrate, int baudrateChangeDelay, ProtocolMode protocolMode);

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
     * @param obisId the OBIS code which value should be retrieved.
     * @return the OBIS value as String if available - otherwise null.
     */
    @Nullable
    public String getValue(String obisId) {
        MeterValue<?> smlValue = getMeterValue(obisId);
        if (smlValue != null) {
            return smlValue.getValue();
        }
        return null;
    }

    /**
     * Returns the specified OBIS value if available.
     *
     * @param obisId the OBIS code which value should be retrieved.
     * @return the OBIS value if available - otherwise null.
     */
    @SuppressWarnings("unchecked")
    @Nullable
    public <Q extends Quantity<Q>> MeterValue<Q> getMeterValue(String obisId) {
        if (valueCache.containsKey(obisId)) {
            return (MeterValue<Q>) valueCache.get(obisId);
        }
        return null;
    }

    /**
     * Gets all currently available OBIS codes.
     *
     * @return All cached OBIS codes.
     */
    public Collection<String> getObisCodes() {
        return new ArrayList<>(this.valueCache.keySet());
    }

    /**
     * Read values from this device a store them locally against their OBIS code.
     *
     * If there is an error in reading, it will be retried
     * {@value org.openhab.binding.smartmeter.connectors.ConnectorBase#NUMBER_OF_RETRIES} times.
     * The retry will be delayed by {@code period} seconds.
     * If its still failing, the connection will be closed and opened again.
     *
     * @return The {@link Disposable} which needs to be disposed whenever not used anymore.
     *
     */
    public Disposable readValues(long timeout, ScheduledExecutorService executorService, Duration period) {
        return Flowable.fromPublisher(connector.getMeterValues(initMessage, period, executorService))
                .timeout(timeout + period.toMillis(), TimeUnit.MILLISECONDS, Schedulers.from(executorService))
                .doOnSubscribe(sub -> {
                    logger.debug("Opening connection to {}", getDeviceId());
                    connector.openConnection();
                }).doOnError(ex -> {
                    if (ex instanceof TimeoutException) {
                        logger.debug("Timeout occured for {}; {}", getDeviceId(), ex.getMessage());
                    } else {
                        logger.debug("Failed to read: {}. Closing connection and trying again in {} seconds...; {}",
                                ex.getMessage(), RETRY_DELAY, getDeviceId(), ex);
                    }
                    connector.closeConnection();
                    notifyReadingError(ex);
                }).doOnCancel(connector::closeConnection).doOnComplete(connector::closeConnection).share()
                .retryWhen(
                        publisher -> publisher.delay(RETRY_DELAY, TimeUnit.SECONDS, Schedulers.from(executorService)))
                .subscribeOn(Schedulers.from(executorService), true).subscribe((value) -> {
                    Map<String, MeterValue<?>> obisCodes = new HashMap<>(valueCache);
                    clearValueCache();
                    populateValueCache(value);
                    printInfo();
                    Collection<String> newObisCodes = getObisCodes();
                    // notify every removed obis code.
                    obisCodes.values().stream().filter((val) -> !newObisCodes.contains(val.getObisCode()))
                            .forEach((val) -> notifyValuesRemoved(val));
                });
    }

    /**
     * Deletes all cached values.
     *
     * The method will always be called before new values are populated.
     */
    protected void clearValueCache() {
        valueCache.clear();
    }

    /**
     * Called whenever a new value was made available. The value cache needs to be filled here with
     * {@link #addObisCache(MeterValue)}.
     *
     * @param payload The actual payload value.
     */
    protected abstract <Q extends Quantity<Q>> void populateValueCache(T payload);

    /**
     * Adds a {@link MeterValue} to the current cache.
     *
     * @param value The value to add.
     */
    protected <Q extends Quantity<Q>> void addObisCache(MeterValue<Q> value) {
        logger.debug("Value changed: {}", value);
        this.valueCache.put(value.getObisCode(), value);
        this.valueChangeListeners.forEach((listener) -> {
            try {
                listener.valueChanged(value);
            } catch (Exception e) {
                logger.error("Meter listener failed", e);
            }
        });
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Device: ");
        stringBuilder.append(getDeviceId());
        stringBuilder.append(System.lineSeparator());

        for (Entry<String, MeterValue<?>> entry : valueCache.entrySet()) {
            stringBuilder.append("Obis: " + entry.getKey() + " " + entry.getValue().toString());
            stringBuilder.append(System.lineSeparator());
        }
        return stringBuilder.toString();
    }

    /**
     * Adds a {@link MeterValueListener} to the list of listeners which gets notified on new values being read.
     *
     * @param valueChangeListener The new {@link MeterValueListener}
     */
    public void addValueChangeListener(MeterValueListener valueChangeListener) {
        this.valueChangeListeners.add(valueChangeListener);
    }

    /**
     * Removes a {@link MeterValueListener} from the list of listeners.
     *
     * @param valueChangeListener The listener to remove.
     */
    public void removeValueChangeListener(MeterValueListener valueChangeListener) {
        this.valueChangeListeners.remove(valueChangeListener);
    }

    private <Q extends Quantity<Q>> void notifyValuesRemoved(MeterValue<Q> value) {
        this.valueChangeListeners.forEach((listener) -> listener.valueRemoved(value));
    }

    private void notifyReadingError(Throwable e) {
        this.valueChangeListeners.forEach((listener) -> listener.errorOccurred(e));
    }

    /**
     * Logs the object information with all given SML values to OSGi console.
     *
     * It's only called once - except the config was updated.
     */
    protected void printInfo() {
        if (this.getPrintMeterInfo()) {
            logger.info("Read out following values: {}", toString());
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
