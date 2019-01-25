/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.onewire.internal.handler;

import java.util.BitSet;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.onewire.internal.OwException;
import org.openhab.binding.onewire.internal.OwPageBuffer;
import org.openhab.binding.onewire.internal.SensorId;
import org.openhab.binding.onewire.internal.device.OwDeviceParameterMap;
import org.openhab.binding.onewire.internal.device.OwSensorType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OwBaseBridgeHandler} class implements the refresher and the interface for reading from the bridge
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public abstract class OwBaseBridgeHandler extends BaseBridgeHandler {
    private final Logger logger = LoggerFactory.getLogger(OwBaseBridgeHandler.class);

    // set by implementation when bridge is ready
    protected boolean refreshable = false;

    public OwBaseBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    protected ScheduledFuture<?> refreshTask = scheduler.scheduleWithFixedDelay(() -> refresh(), 1, 1000,
            TimeUnit.MILLISECONDS);

    // thing update
    private final Queue<@Nullable Thing> thingPropertiesUpdateQueue = new ConcurrentLinkedQueue<>();

    /**
     * refresh all sensors on this bridge
     */
    private void refresh() {
        if (refreshable) {
            long now = System.currentTimeMillis();

            // refresh thing channels
            List<Thing> thingList = getThing().getThings();
            int thingCount = thingList.size();
            Iterator<Thing> childListIterator = thingList.iterator();
            logger.trace("refreshTask starts at {}, {} childs", now, thingCount);
            while (childListIterator.hasNext() && refreshable) {
                Thing owThing = childListIterator.next();

                logger.trace("refresh: getting handler for {} ({} to go)", owThing.getUID(), thingCount);
                OwBaseThingHandler owHandler = (OwBaseThingHandler) owThing.getHandler();
                if (owHandler != null) {
                    if (owHandler.isRefreshable()) {
                        logger.trace("{} initialized, refreshing", owThing.getUID());
                        owHandler.refresh(OwBaseBridgeHandler.this, now);
                    } else {
                        logger.trace("{} not initialized, skipping refresh", owThing.getUID());
                    }
                } else {
                    logger.debug("{} handler missing", owThing.getUID());
                }
                thingCount--;
            }

            refreshBridgeChannels(now);

            // update thing properties (only one per refresh cycle)
            Thing updateThing = thingPropertiesUpdateQueue.poll();
            if (updateThing != null) {
                logger.trace("update: getting handler for {} ({} total in list)", updateThing.getUID(),
                        thingPropertiesUpdateQueue.size());
                OwBaseThingHandler owHandler = (OwBaseThingHandler) updateThing.getHandler();
                if (owHandler != null) {
                    try {
                        owHandler.updateSensorProperties(this);
                        owHandler.initialize();
                        logger.debug("{} sucessfully updated properties, removing from property update list",
                                updateThing.getUID());
                    } catch (OwException e) {
                        thingPropertiesUpdateQueue.add(updateThing);
                        logger.debug("updating thing properties for {} failed: {}, adding to end of list",
                                updateThing.getUID(), e.getMessage());
                    }
                } else {
                    logger.debug("{} is missing handler, removing from property update list", updateThing.getUID());
                }
            }
        }
    }

    @Override
    public void initialize() {
        if (refreshTask.isCancelled()) {
            refreshTask = scheduler.scheduleWithFixedDelay(() -> refresh(), 1, 1000, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public void dispose() {
        refreshable = false;
        if (!refreshTask.isCancelled()) {
            refreshTask.cancel(false);
        }
    }

    /**
     * schedules a thing for updating the thing properties
     *
     * @param thing the thing to be updated
     */
    public void scheduleForPropertiesUpdate(Thing thing) {
        thingPropertiesUpdateQueue.add(thing);
    }

    /**
     * get all sensors attached to this bridge
     *
     * @return a list of all sensor-IDs
     */
    public abstract List<SensorId> getDirectory(String basePath) throws OwException;

    /**
     * check the presence of a sensor on the bus
     *
     * @param sensorId the sensor's full ID
     * @return ON if present, OFF if missing
     * @throws OwException
     */
    public abstract State checkPresence(SensorId sensorId) throws OwException;

    /**
     * get a sensors type string
     *
     * @param sensorId the sensor's full ID
     * @return a String containing the sensor type
     * @throws OwException
     */
    public abstract OwSensorType getType(SensorId sensorId) throws OwException;

    /**
     * get full sensor information stored in pages (not available on all sensors)
     *
     * @param sensorId the sensor's full ID
     * @return a OwPageBuffer object containing the requested information
     * @throws OwException
     */
    public abstract OwPageBuffer readPages(SensorId sensorId) throws OwException;

    /**
     * read a single decimal value from a sensor
     *
     * @param sensorId sensorId the sensor's full ID
     * @param parameter device parameters needed for this request
     * @return a DecimalType
     * @throws OwException
     */
    public abstract State readDecimalType(SensorId sensorId, OwDeviceParameterMap parameter) throws OwException;

    /**
     * read a BitSet value from a sensor
     *
     * @param sensorId sensorId the sensor's full ID
     * @param parameter device parameters needed for this request
     * @return a BitSet
     * @throws OwException
     */
    public BitSet readBitSet(SensorId sensorId, OwDeviceParameterMap parameter) throws OwException {
        return BitSet.valueOf(new long[] { ((DecimalType) readDecimalType(sensorId, parameter)).longValue() });
    }

    /**
     * read an array of decimal values from a sensor
     *
     * @param sensorId sensorId the sensor's full ID
     * @param parameter device parameters needed for this request
     * @return a list of DecimalType values
     * @throws OwException
     */
    public abstract List<State> readDecimalTypeArray(SensorId sensorId, OwDeviceParameterMap parameter)
            throws OwException;

    /**
     * read a string from a sensor
     *
     * @param sensorId sensorId the sensor's full ID
     * @param parameter device parameters needed for this request
     * @return a String
     * @throws OwException
     */
    public abstract String readString(SensorId sensorId, OwDeviceParameterMap parameter) throws OwException;

    /**
     * writes a DecimalType to the sensor
     *
     * @param sensorId sensorId the sensor's full ID
     * @param parameter device parameters needed for this request
     * @throws OwException
     */
    public abstract void writeDecimalType(SensorId sensorId, OwDeviceParameterMap parameter, DecimalType value)
            throws OwException;

    /**
     * writes a BitSet to the sensor
     *
     * @param sensorId sensorId the sensor's full ID
     * @param parameter device parameters needed for this request
     * @throws OwException
     */
    public void writeBitSet(SensorId sensorId, OwDeviceParameterMap parameter, BitSet value) throws OwException {
        writeDecimalType(sensorId, parameter, new DecimalType(value.toLongArray()[0]));
    }

    /**
     * returns if this bridge is refreshable
     *
     * @return true if implementation reports communication ready
     * @throws OwException
     */
    public boolean isRefreshable() {
        return refreshable;
    }

    /**
     * refreshes channels attached to the bridge
     *
     * @param now current time
     */
    public void refreshBridgeChannels(long now) {
    }
}
