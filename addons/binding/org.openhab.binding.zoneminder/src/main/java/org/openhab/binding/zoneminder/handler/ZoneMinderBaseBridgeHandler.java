/**
 * Copyright (c) 2014-2016 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zoneminder.handler;

import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.openhab.binding.zoneminder.ZoneMinderConstants;
import org.openhab.binding.zoneminder.internal.ZoneMinderMonitorEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ZoneMinderBaseBridgeHandler extends BaseBridgeHandler
        implements ZoneMinderHandler, ZoneMinderMonitorEventListener {

    /**
     * Logger
     */
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /** The ZoneMinder bridge type. */
    private ZoneMinderThingType zoneMinderBridgeType = null;

    private ScheduledFuture<?> taskWatchDog = null;

    /** Connection status for the bridge. */
    private boolean connected = false;

    protected Boolean isAlive = false;

    private Runnable watchDogRunnable = new Runnable() {

        @Override
        public void run() {
            try {

                updateAvaliabilityStatus();

            } catch (Exception exception) {
                logger.error("Server WatchDog::run(): Exception: ", exception);
            }
        }
    };

    /**
     * Constructor
     *
     * @param bridge
     */
    public ZoneMinderBaseBridgeHandler(Bridge bridge, ZoneMinderThingType zoneMinderBridgeType) {
        super(bridge);
        this.zoneMinderBridgeType = zoneMinderBridgeType;
    }

    protected void startWatchDogTask() {
        taskWatchDog = startTask(watchDogRunnable, 10, TimeUnit.SECONDS);
    }

    protected void stopWatchDogTask() {
        stopTask(taskWatchDog);
        taskWatchDog = null;
    }

    /**
     *
     */
    public ZoneMinderBaseThingHandler getZoneMinderThingHandlerFromZoneMinderId(ThingTypeUID thingTypeUID,
            String zoneMinderId) {

        // Inform thing handlers of connection
        List<Thing> things = getThing().getThings();

        for (Thing thing : things) {
            ZoneMinderBaseThingHandler thingHandler = (ZoneMinderBaseThingHandler) thing.getHandler();
            logger.debug("getZoneMinderThingHandlerFromZoneMinderId(): '{}' ?= '{}'", thingHandler.getZoneMinderId(),
                    zoneMinderId);
            if ((thingHandler.getZoneMinderId().equals(zoneMinderId))
                    && (thing.getThingTypeUID().equals(thingTypeUID))) {
                return thingHandler;
            }
        }
        return null;
    }

    /**
     * Method to Update a Channel
     *
     * @param channel
     */
    @Override
    public void updateChannel(ChannelUID channel) {

        switch (channel.getId()) {
            case ZoneMinderConstants.CHANNEL_IS_ALIVE:
                updateState(channel, (isAlive() ? OnOffType.ON : OnOffType.OFF));
                break;
            default:
                logger.error(
                        "updateChannel() in base class, called for an unknown channel '{}', this channel must be handled in super class.",
                        channel.getId());
        }
    }

    /**
     * Connect The Bridge.
     */
    protected synchronized void connect() {
        onDisconnected();

        openConnection();

        if (isConnected()) {
            onConnected();
        }
    }

    /**
     * Disconnect The Bridge.
     */
    private synchronized void disconnect() {

        closeConnection();

        if (!isConnected()) {
            onDisconnected();
        }
    }

    /**
     * Returns connection status.
     */
    public synchronized Boolean isConnected() {
        return connected;
    }

    public Boolean isAlive() {
        return isAlive;
    }

    /**
     * Set connection status.
     *
     * @param connected
     */
    private synchronized void setConnected(boolean connected) {

        if (this.connected != connected) {
            this.connected = connected;

            if (connected == true) {
                onConnected();
            } else if (connected == false) {
                onDisconnected();
            }
        }

    }

    /**
     * Set channel 'bridge_connection'.
     *
     * @param connected
     */
    protected void setBridgeConnection(boolean connected) {
        logger.debug("setBridgeConnection(): Set Bridge to {}", connected ? ThingStatus.ONLINE : ThingStatus.OFFLINE);

        Bridge bridge = getBridge();
        if (bridge != null) {
            ThingStatus status = bridge.getStatus();
            logger.debug("Bridge ThingStatus is: {}", status);
        }

        setConnected(connected);

    }

    /**
     * Runs when connection established.
     */
    public void onConnected() {
        logger.debug("onConnected(): Bridge Connected!");

        onBridgeConnected(this);

        refreshThing();
        // Inform thing handlers of connection
        List<Thing> things = getThing().getThings();

        for (Thing thing : things) {
            ZoneMinderBaseThingHandler thingHandler = (ZoneMinderBaseThingHandler) thing.getHandler();

            if (thingHandler != null) {
                thingHandler.onBridgeConnected(this);
                logger.trace("onConnected(): Bridge - {}, Thing - {}, Thing Handler - {}", thing.getBridgeUID(),
                        thing.getUID(), thingHandler);
            }
        }
    }

    /**
     * Runs when disconnected.
     */
    public void onDisconnected() {
        logger.debug("onDisconnected(): Bridge Disconnected!");

        onBridgeDisconnected(this);

        // Inform thing handlers of disconnection
        List<Thing> things = getThing().getThings();

        for (Thing thing : things) {
            ZoneMinderBaseThingHandler thingHandler = (ZoneMinderBaseThingHandler) thing.getHandler();

            if (thingHandler != null) {
                thingHandler.onBridgeDisconnected(this);
                logger.trace("onDisconnected(): Bridge - {}, Thing - {}, Thing Handler - {}", thing.getBridgeUID(),
                        thing.getUID(), thingHandler);
            }
        }
    }

    /**
     * Method for opening a connection to ZoneMinder Server.
     */
    abstract Boolean openConnection();

    /**
     * Method for closing a connection to ZoneMinder Server.
     */
    abstract void closeConnection();

    /**
     * Method to start a data refresh task.
     */
    protected ScheduledFuture<?> startTask(Runnable command, long refreshInterval, TimeUnit unit) {
        ScheduledFuture<?> task = null;
        logger.debug("Starting ZoneMinder Bridge Monitor Task. Command='{}'", command.toString());
        if (refreshInterval == 0) {
            return task;
        }

        if (task == null || task.isCancelled()) {
            task = scheduler.scheduleWithFixedDelay(command, 0, refreshInterval, unit);
        }
        return task;
    }

    /**
     * Method to stop the datarefresh task.
     */
    protected void stopTask(ScheduledFuture<?> task) {
        logger.debug("Stopping ZoneMinder Bridge Monitor Task. Task='{}'", task.toString());
        if (task != null && !task.isCancelled()) {
            task.cancel(true);
        }
    }

    /**
     * A bridge could eventually also have channels -> so we think of the bridge as a thing.
     */
    protected abstract void refreshThing();

    protected abstract void onRefreshLowPriorityPriorityData();

    protected abstract void onRefreshHighPriorityPriorityData();

    /**
     * Method for updating High priority Data.
     */

    public synchronized void refreshHighPriorityPriorityData() {
        logger.debug("Refreshing high priority data from ZoneMinder Server Task - '{}'", getThing().getUID());

        if (!isConnected()) {
            logger.error("Not Connected to the ZoneMinder Server!");
            connect();
        }

        onRefreshHighPriorityPriorityData();
    }

    /**
     * Method for updating Low priority Data.
     */

    public synchronized void refreshLowPriorityPriorityData() {
        logger.debug("Refreshing low priority data from ZoneMinder Server Task - '{}'", getThing().getUID());

        if (isConnected()) {
            onRefreshLowPriorityPriorityData();
        } else {
            logger.error("Not Connected to the ZoneMinder Server!");
            connect();
        }
    }

}
