/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
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
import org.openhab.binding.zoneminder.internal.command.ZoneMinderEvent;
import org.openhab.binding.zoneminder.internal.command.ZoneMinderMessage.ZoneMinderRequestType;
import org.openhab.binding.zoneminder.internal.command.ZoneMinderOutgoingRequest;
import org.openhab.binding.zoneminder.internal.command.ZoneMinderTelnetEvent;
import org.openhab.binding.zoneminder.internal.command.http.ZoneMinderHttpRequest;
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

    /** Connection status for the bridge. */
    private boolean connected = false;

    /**
     * Constructor
     *
     * @param bridge
     */
    public ZoneMinderBaseBridgeHandler(Bridge bridge, ZoneMinderThingType zoneMinderBridgeType) {
        super(bridge);
        this.zoneMinderBridgeType = zoneMinderBridgeType;
    }

    /**
     * Register the Discovery Service.
     *
     * @param discoveryService
     */
    // public void registerDiscoveryService(DSCAlarmDiscoveryService discoveryService) {
    // if (discoveryService == null) {
    // throw new IllegalArgumentException("registerDiscoveryService(): Illegal Argument. Not allowed to be Null!");
    // } else {
    // this.zoneMinderDiscoveryService = discoveryService;
    // logger.trace("registerDiscoveryService(): Discovery Service Registered!");
    // }
    // }

    /**
     * Unregister the Discovery Service.
     */
    // public void unregisterDiscoveryService() {
    // zoneMinderDiscoveryService = null;
    // logger.trace("unregisterDiscoveryService(): Discovery Service Unregistered!");
    // }

    /**
     *
     */
    public ZoneMinderBaseThingHandler getZoneMinderThingHandlerFromZoneMinderId(ThingTypeUID thingTypeUID,
            String zoneMinderId) {

        // BaseThingHandler myThing = thingRegistry.get(thingTypeUID.);
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

    protected abstract void checkIsAlive();

    /**
     * Method to Update a Channel
     *
     * @param channel
     */
    @Override
    public void updateChannel(ChannelUID channel) {
        OnOffType onOffType;

        switch (channel.getId()) {
            case ZoneMinderConstants.CHANNEL_ONLINE:

                // onOffType = isAlive() ? OnOffType.ON : OnOffType.OFF;
                // updateState(channel, onOffType);
                setConnected(isAlive());
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
    public synchronized boolean isConnected() {
        return connected;
    }

    /**
     * Set connection status.
     *
     * @param connected
     */
    public synchronized void setConnected(boolean connected) {
        if (this.connected != connected) {
            updateState(ZoneMinderConstants.CHANNEL_ONLINE, connected ? OnOffType.ON : OnOffType.OFF);
        }
        ThingStatus thingStatus = connected ? ThingStatus.ONLINE : ThingStatus.OFFLINE;
        if (thingStatus != this.thing.getStatus()) {
            updateStatus(thingStatus);
        }

        this.connected = connected;
    }

    /**
     * Set channel 'bridge_connection'.
     *
     * @param connected
     */
    public void setBridgeConnection(boolean connected) {
        logger.debug("setBridgeConnection(): Set Bridge to {}", connected ? ThingStatus.ONLINE : ThingStatus.OFFLINE);

        setConnected(connected);
    }

    /**
     * Runs when connected.
     */
    public void onConnected() {
        logger.debug("onConnected(): Bridge Connected!");

        setBridgeConnection(true);

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

        setBridgeConnection(false);

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
    abstract void openConnection();

    /**
     * Method for closing a connection to ZoneMinder Server.
     */
    abstract void closeConnection();

    /**
     * Method to start a data refresh task.
     */
    protected ScheduledFuture<?> startRefreshDataTask(Runnable command, long refreshInterval) {
        ScheduledFuture<?> task = null;
        logger.debug("Starting ZoneMinder Bridge Monitor Task. Command='{}'", command.toString());
        if (task == null || task.isCancelled()) {
            task = scheduler.scheduleAtFixedRate(command, 0, refreshInterval, TimeUnit.SECONDS);
        }
        return task;
    }

    /**
     * Method to stop the datarefresh task.
     */
    protected void stopRefreshDataTask(ScheduledFuture<?> task) {
        logger.debug("Stopping ZoneMinder Bridge Monitor Task. Task='{}'", task.toString());
        if (task != null && !task.isCancelled()) {
            task.cancel(true);
        }
    }

    /**
     * Handles an incoming message from ZoneMinder Server.
     *
     * @param incomingMessage
     */
    public synchronized void handleIncomingTelnetMessage(String incomingMessage) {
        if (incomingMessage != null && incomingMessage != "") {
            ZoneMinderTelnetEvent message = new ZoneMinderTelnetEvent(incomingMessage);
            logger.debug("handleIncomingMessage(): Message received: {} - {}", incomingMessage,
                    message.toCommandString());

            ZoneMinderEvent event = new ZoneMinderEvent(ZoneMinderConstants.THING_TYPE_THING_ZONEMINDER_MONITOR,
                    message);
            notifyZoneMinderEvent(event);

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

        if (isConnected()) {
            onRefreshHighPriorityPriorityData();
        } else {
            logger.error("Not Connected to the ZoneMinder Server!");
            connect();
        }
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

    @Override
    public void notifyZoneMinderEvent(ZoneMinderEvent event) {

        ZoneMinderBaseThingHandler thing = getZoneMinderThingHandlerFromZoneMinderId(event.getThingTypeUID(),
                event.getZoneMinderId());

        // If thing not found, then it is not to this thing that it belongs :-)
        if (thing != null) {
            thing.notifyZoneMinderEvent(event);
        }
    }

    public abstract boolean sendZoneMinderHttpRequest(ZoneMinderHttpRequest requestType);

    public boolean sendZoneMinderTelnetRequest(ZoneMinderRequestType requestType, ZoneMinderOutgoingRequest request) {
        switch (requestType) {
            default:
                return onHandleZoneMinderTelnetRequest(requestType, request);
        }
    }

    @Override
    public void updateStatus(ThingStatus status) {
        super.updateStatus(status);
        updateState(ZoneMinderConstants.CHANNEL_ONLINE,
                ((status == ThingStatus.ONLINE) ? OnOffType.ON : OnOffType.OFF));

    }

    protected abstract boolean onHandleZoneMinderTelnetRequest(ZoneMinderRequestType requestType,
            ZoneMinderOutgoingRequest request);

}
