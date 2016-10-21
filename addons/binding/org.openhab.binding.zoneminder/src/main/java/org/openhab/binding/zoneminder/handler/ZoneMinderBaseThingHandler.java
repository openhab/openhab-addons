/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zoneminder.handler;

import java.util.EventObject;
import java.util.List;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.zoneminder.ZoneMinderConstants;
import org.openhab.binding.zoneminder.internal.ZoneMinderMonitorEventListener;
import org.openhab.binding.zoneminder.internal.api.MonitorDaemonStatus;
import org.openhab.binding.zoneminder.internal.config.ZoneMinderThingConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ZoneMinderBaseThingHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Martin S. Eskildsen - Initial contribution
 */
public abstract class ZoneMinderBaseThingHandler extends BaseThingHandler
        implements ZoneMinderMonitorEventListener, ZoneMinderHandler {

    public static final int EVENT_REFRESH_INTERVAL = 200;

    /** Logger for the Thing. */
    private Logger logger = LoggerFactory.getLogger(ZoneMinderBaseThingHandler.class);

    /** Bridge Handler for the Thing. */
    public ZoneMinderBaseBridgeHandler zoneMinderBridgeHandler = null;

    /** This refresh status. */
    private boolean thingRefreshed = false;

    /** Unique Id of the thing in zoneminder. */
    private String zoneMinderId;

    /** Configuration from OpenHAB */
    protected ZoneMinderThingConfig configuration;

    public ZoneMinderBaseThingHandler(Thing thing) {
        super(thing);
    }

    /**
     * Initializes the monitor.
     *
     * @author Martin S. Eskildsen
     *
     */
    @Override
    public void initialize() {
        logger.debug("Initializing ZoneMinder Thing handler - Thing Type: {}; Thing ID: {}.", getZoneMinderThingType(),
                this.getThing().getUID());

        super.initialize();
    }

    @Override
    public void dispose() {

    }

    /**
     * Helper method for getting ChannelUID from ChannelId.
     *
     * @author Martin S. Eskildsen
     */
    public synchronized ChannelUID getChannelUIDFromChannelId(String id) {
        Channel ch = thing.getChannel(id);
        return ch.getUID();
    }

    /**
     * Method to Refresh Thing Handler.
     */
    public final void refreshThing() {

        if (getZoneMinderBridgeHandler() != null) {
            logger.debug("refreshThing(): Bridge '{}' Found for Thing '{}'!",
                    zoneMinderBridgeHandler.getThing().getUID(), this.getThing().getUID());

            Thing thing = getThing();
            List<Channel> channels = thing.getChannels();
            logger.debug("refreshThing(): Refreshing Thing - {}", thing.getUID());

            for (Channel channel : channels) {
                updateChannel(channel.getUID());
            }

            this.setThingRefreshed(true);
        }
        logger.debug("refreshThing(): Thing Refreshed - {}", thing.getUID());
    }

    /**
     * Get the Bridge Handler for ZoneMinder.
     *
     * @return zoneMinderBridgeHandler
     */
    public synchronized ZoneMinderBaseBridgeHandler getZoneMinderBridgeHandler() {

        if (this.zoneMinderBridgeHandler == null) {

            Bridge bridge = getBridge();

            if (bridge == null) {
                logger.debug("getZoneMinderBridgeHandler(): Unable to get bridge!");
                return null;
            }

            logger.debug("getZoneMidnerBridgeHandler(): Bridge for '{}' - '{}'", getThing().getUID(), bridge.getUID());

            ThingHandler handler = bridge.getHandler();

            if (handler instanceof ZoneMinderBaseBridgeHandler) {
                this.zoneMinderBridgeHandler = (ZoneMinderBaseBridgeHandler) handler;
            } else {
                logger.debug("getZoneMinderBridgeHandler(): Unable to get bridge handler!");
            }
        }

        return this.zoneMinderBridgeHandler;
    }

    /**
     * Method to Update a Channel
     *
     * @param channel
     */
    @Override
    public void updateChannel(ChannelUID channel) {
        OnOffType onOffType;

        switch (channel.getId()) {
            case ZoneMinderConstants.CHANNEL_MONITOR_ONLINE:
                onOffType = isAlive() ? OnOffType.ON : OnOffType.OFF;
                updateState(channel, onOffType);
            default:
                logger.error(
                        "updateChannel() in base class, called for an unknown channel '{}', this channel must be handled in super class.",
                        channel.getId());
        }
    }

    /**
     * Method to Update Device Properties.
     *
     * @param channelUID
     * @param state
     * @param description
     */
    public abstract void updateProperties(ChannelUID channelUID, int state, String description);

    /**
     * Receives ZoneMinder Events from the bridge.
     *
     * @param event.
     * @param thing
     */
    public abstract void ZoneMinderEventReceived(EventObject event, Thing thing);

    protected abstract void checkIsAlive(MonitorDaemonStatus monitorStatus);

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    public void onBridgeConnected(ZoneMinderBaseBridgeHandler bridge) {
        logger.debug("onBridgeConnected(): Bridge '{}' is connected", bridge.getThing().getUID());

        if (bridge.getThing().getUID().equals(getThing().getBridgeUID())) {
            updateStatus(bridge.getThing().getStatus());
            refreshThing();
        }
    }

    public void onBridgeDisconnected(ZoneMinderBaseBridgeHandler bridge) {
        logger.debug("onBridgeDisconnected(): Bridge '{}' disconnected", bridge.getThing().getUID());

        if (bridge.getThing().getUID().equals(getThing().getBridgeUID())) {
            this.setThingRefreshed(false);
        }
    }

    /**
     * Get Channel by ChannelUID.
     *
     * @param channelUID
     */
    public Channel getChannel(ChannelUID channelUID) {
        Channel channel = null;

        List<Channel> channels = getThing().getChannels();

        for (Channel ch : channels) {
            if (channelUID == ch.getUID()) {
                channel = ch;
                break;
            }
        }

        return channel;
    }

    /**
     * Get Thing Handler refresh status.
     *
     * @return thingRefresh
     */
    public boolean isThingRefreshed() {
        return thingRefreshed;
    }

    /**
     * Set Thing Handler refresh status.
     *
     * @param deviceInitialized
     */
    public void setThingRefreshed(boolean refreshed) {
        this.thingRefreshed = refreshed;
    }

    protected abstract String getZoneMinderThingType();

    private Object getConfigValue(String configKey) {
        return getThing().getConfiguration().getProperties().get(configKey);
    }

    /*
     * Helper to get a value from configuration as a String
     *
     * @author Martin S. Eskildsen
     *
     */
    protected String getConfigValueAsString(String configKey) {
        return (String) getConfigValue(configKey);
    }

    /*
     * Helper to get a value from configuration as a Integer
     *
     * @author Martin S. Eskildsen
     *
     */
    protected Integer getConfigValueAsInteger(String configKey) {
        return (Integer) getConfigValue(configKey);
    }
}
