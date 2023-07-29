/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import static org.openhab.binding.onewire.internal.OwBindingConstants.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.onewire.internal.OwException;
import org.openhab.binding.onewire.internal.OwPageBuffer;
import org.openhab.binding.onewire.internal.SensorId;
import org.openhab.binding.onewire.internal.device.OwSensorType;
import org.openhab.binding.onewire.internal.discovery.OwDiscoveryService;
import org.openhab.binding.onewire.internal.owserver.OwfsDirectChannelConfig;
import org.openhab.binding.onewire.internal.owserver.OwserverConnection;
import org.openhab.binding.onewire.internal.owserver.OwserverConnectionState;
import org.openhab.binding.onewire.internal.owserver.OwserverDeviceParameter;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OwserverBridgeHandler} class implements the refresher and the interface for reading from the bridge
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class OwserverBridgeHandler extends BaseBridgeHandler {
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_OWSERVER);

    private final Logger logger = LoggerFactory.getLogger(OwserverBridgeHandler.class);
    protected boolean refreshable = false;

    protected ScheduledFuture<?> refreshTask = scheduler.scheduleWithFixedDelay(this::refresh, 1, 1000,
            TimeUnit.MILLISECONDS);

    // thing update
    private final Queue<@Nullable Thing> thingPropertiesUpdateQueue = new ConcurrentLinkedQueue<>();

    private static final int RECONNECT_AFTER_FAIL_TIME = 5000; // in ms
    private final OwserverConnection owserverConnection;

    private final List<OwfsDirectChannelConfig> channelConfigs = new ArrayList<>();

    public OwserverBridgeHandler(Bridge bridge) {
        super(bridge);
        this.owserverConnection = new OwserverConnection(this);
    }

    public OwserverBridgeHandler(Bridge bridge, OwserverConnection owserverConnection) {
        super(bridge);
        this.owserverConnection = owserverConnection;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void initialize() {
        Configuration configuration = getConfig();

        if (configuration.get(CONFIG_ADDRESS) != null) {
            owserverConnection.setHost((String) configuration.get(CONFIG_ADDRESS));
        }
        if (configuration.get(CONFIG_PORT) != null) {
            owserverConnection.setPort(((BigDecimal) configuration.get(CONFIG_PORT)).intValue());
        }

        for (Channel channel : thing.getChannels()) {
            if (CHANNEL_TYPE_UID_OWFS_NUMBER.equals(channel.getChannelTypeUID())
                    || CHANNEL_TYPE_UID_OWFS_STRING.equals(channel.getChannelTypeUID())) {
                final OwfsDirectChannelConfig channelConfig = channel.getConfiguration()
                        .as(OwfsDirectChannelConfig.class);
                if (channelConfig.initialize(channel.getUID(), channel.getAcceptedItemType())) {
                    channelConfigs.add(channelConfig);
                } else {
                    logger.info("configuration mismatch: {}", channelConfig);
                }
            }
        }

        // makes it possible for unit tests to differentiate direct update and
        // postponed update through the owserverConnection:
        updateStatus(ThingStatus.UNKNOWN);

        scheduler.execute(() -> {
            synchronized (owserverConnection) {
                owserverConnection.start();
            }
        });

        if (refreshTask.isCancelled()) {
            refreshTask = scheduler.scheduleWithFixedDelay(this::refresh, 1, 1000, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * refresh all sensors on this bridge
     */
    private void refresh() {
        try {
            long now = System.currentTimeMillis();
            if (!refreshable) {
                logger.trace("refresh requested by thread ID {} denied, as not refresheable",
                        Thread.currentThread().getId());
                return;
            }

            // refresh thing channels
            List<Thing> thingList = getThing().getThings();
            int thingCount = thingList.size();
            Iterator<Thing> childListIterator = thingList.iterator();
            logger.trace("refreshTask with thread ID {} starts at {}, {} childs", Thread.currentThread().getId(), now,
                    thingCount);
            while (childListIterator.hasNext() && refreshable) {
                Thing owThing = childListIterator.next();

                logger.trace("refresh: getting handler for {} ({} to go)", owThing.getUID(), thingCount);
                OwBaseThingHandler owHandler = (OwBaseThingHandler) owThing.getHandler();
                if (owHandler != null) {
                    if (owHandler.isRefreshable()) {
                        logger.trace("{} initialized, refreshing", owThing.getUID());
                        owHandler.refresh(OwserverBridgeHandler.this, now);
                    } else {
                        logger.trace("{} not initialized, skipping refresh", owThing.getUID());
                    }
                } else {
                    logger.debug("{} handler missing", owThing.getUID());
                }
                thingCount--;
            }

            if (!refreshable) {
                logger.trace("refresh aborted, as brige became non-refresheable.");
                return;
            }
            refreshBridgeChannels(now);

            // update thing properties (only one per refresh cycle)
            if (!refreshable) {
                logger.trace("refresh aborted, as brige became non-refresheable.");
                return;
            }
            Thing updateThing = thingPropertiesUpdateQueue.poll();
            if (updateThing != null) {
                logger.trace("update: getting handler for {} ({} total in list)", updateThing.getUID(),
                        thingPropertiesUpdateQueue.size());
                OwBaseThingHandler owHandler = (OwBaseThingHandler) updateThing.getHandler();
                if (owHandler != null) {
                    try {
                        owHandler.updateSensorProperties(this);
                        owHandler.initialize();
                        logger.debug("{} successfully updated properties, removing from property update list",
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

        } catch (RuntimeException e) {
            // catching RuntimeException because scheduled tasks finish once an exception occurs
            logger.error("refresh encountered exception of {}: {}, please report bug", e.getClass(), e.getMessage());
        }
    }

    @Override
    public void dispose() {
        refreshable = false;
        if (!refreshTask.isCancelled()) {
            refreshTask.cancel(false);
        }
        owserverConnection.stop();
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
    public List<SensorId> getDirectory(String basePath) throws OwException {
        synchronized (owserverConnection) {
            return owserverConnection.getDirectory(basePath);
        }
    }

    /**
     * check the presence of a sensor on the bus
     *
     * @param sensorId the sensor's full ID
     * @return ON if present, OFF if missing
     * @throws OwException in case an error occurs
     */
    public State checkPresence(SensorId sensorId) throws OwException {
        synchronized (owserverConnection) {
            return owserverConnection.checkPresence(sensorId.getFullPath());
        }
    }

    /**
     * get a sensors type string
     *
     * @param sensorId the sensor's full ID
     * @return a String containing the sensor type
     * @throws OwException in case an error occurs
     */
    public OwSensorType getType(SensorId sensorId) throws OwException {
        OwSensorType sensorType = OwSensorType.UNKNOWN;
        synchronized (owserverConnection) {
            try {
                sensorType = OwSensorType.valueOf(owserverConnection.readString(sensorId + "/type"));
            } catch (IllegalArgumentException ignored) {
            }
        }
        return sensorType;
    }

    /**
     * get full sensor information stored in pages (not available on all sensors)
     *
     * @param sensorId the sensor's full ID
     * @return a OwPageBuffer object containing the requested information
     * @throws OwException in case an error occurs
     */
    public OwPageBuffer readPages(SensorId sensorId) throws OwException {
        synchronized (owserverConnection) {
            return owserverConnection.readPages(sensorId.getFullPath());
        }
    }

    /**
     * read a single decimal value from a sensor
     *
     * @param sensorId the sensor's full ID
     * @param parameter device parameters needed for this request
     * @return a DecimalType
     * @throws OwException in case an error occurs
     */
    public State readDecimalType(SensorId sensorId, OwserverDeviceParameter parameter) throws OwException {
        synchronized (owserverConnection) {
            return owserverConnection.readDecimalType(parameter.getPath(sensorId));
        }
    }

    /**
     * read a BitSet value from a sensor
     *
     * @param sensorId the sensor's full ID
     * @param parameter device parameters needed for this request
     * @return a BitSet
     * @throws OwException in case an error occurs
     */
    public BitSet readBitSet(SensorId sensorId, OwserverDeviceParameter parameter) throws OwException {
        return BitSet.valueOf(new long[] { ((DecimalType) readDecimalType(sensorId, parameter)).longValue() });
    }

    /**
     * read an array of decimal values from a sensor
     *
     * @param sensorId the sensor's full ID
     * @param parameter device parameters needed for this request
     * @return a list of DecimalType values
     * @throws OwException in case an error occurs
     */
    public List<State> readDecimalTypeArray(SensorId sensorId, OwserverDeviceParameter parameter) throws OwException {
        synchronized (owserverConnection) {
            return owserverConnection.readDecimalTypeArray(parameter.getPath(sensorId));
        }
    }

    /**
     * read a string from a sensor
     *
     * @param sensorId the sensor's full ID
     * @param parameter device parameters needed for this request
     * @return a String
     * @throws OwException in case an error occurs
     */
    public String readString(SensorId sensorId, OwserverDeviceParameter parameter) throws OwException {
        synchronized (owserverConnection) {
            return owserverConnection.readString(parameter.getPath(sensorId));
        }
    }

    /**
     * writes a DecimalType to the sensor
     *
     * @param sensorId the sensor's full ID
     * @param parameter device parameters needed for this request
     * @throws OwException in case an error occurs
     */
    public void writeDecimalType(SensorId sensorId, OwserverDeviceParameter parameter, DecimalType value)
            throws OwException {
        synchronized (owserverConnection) {
            owserverConnection.writeDecimalType(parameter.getPath(sensorId), value);
        }
    }

    /**
     * writes a BitSet to the sensor
     *
     * @param sensorId the sensor's full ID
     * @param parameter device parameters needed for this request
     * @throws OwException in case an error occurs
     */
    public void writeBitSet(SensorId sensorId, OwserverDeviceParameter parameter, BitSet value) throws OwException {
        writeDecimalType(sensorId, parameter, new DecimalType(value.toLongArray()[0]));
    }

    /**
     * returns if this bridge is refreshable
     *
     * @return true if implementation reports communication ready
     */
    public boolean isRefreshable() {
        return refreshable;
    }

    /**
     * updates the thing status with the current connection state
     *
     * @param connectionState current connection state
     */
    public void reportConnectionState(OwserverConnectionState connectionState) {
        logger.debug("Updating owserverconnectionstate to {}", connectionState);
        switch (connectionState) {
            case FAILED:
                refreshable = false;
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                scheduler.schedule(() -> {
                    synchronized (owserverConnection) {
                        owserverConnection.start();
                    }
                }, RECONNECT_AFTER_FAIL_TIME, TimeUnit.MILLISECONDS);
                break;
            case STOPPED:
                refreshable = false;
                break;
            case OPENED:
            case CLOSED:
                refreshable = true;
                updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
                break;
        }
    }

    /**
     * refreshes channels attached to the bridge
     *
     * @param now current time
     */
    public void refreshBridgeChannels(long now) {
        for (OwfsDirectChannelConfig channelConfig : channelConfigs) {
            if (now > channelConfig.lastRefresh + channelConfig.refreshCycle) {
                State value;
                try {
                    synchronized (owserverConnection) {
                        if (channelConfig.acceptedItemType.equals("String")) {
                            value = new StringType(owserverConnection.readString(channelConfig.path));
                        } else if (channelConfig.acceptedItemType.equals("Number")) {
                            value = owserverConnection.readDecimalType(channelConfig.path);
                        } else {
                            logger.debug("mismatched configuration, itemType unknown for channel {}",
                                    channelConfig.channelUID);
                            continue;
                        }
                    }

                    final ChannelUID channelUID = channelConfig.channelUID;
                    if (channelUID == null) {
                        throw new OwException("channelUID is null");
                    }
                    updateState(channelUID, value);
                    logger.trace("updated {} to {}", channelConfig.channelUID, value);

                    channelConfig.lastRefresh = now;
                } catch (OwException e) {
                    logger.debug("could not read direct channel {}: {}", channelConfig.channelUID, e.getMessage());
                }
            }
        }
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(OwDiscoveryService.class);
    }
}
