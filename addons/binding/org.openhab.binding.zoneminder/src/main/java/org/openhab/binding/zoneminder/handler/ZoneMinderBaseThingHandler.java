/**
 * Copyright (c) 2014-2016 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zoneminder.handler;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.GeneralSecurityException;
import java.util.EventObject;
import java.util.List;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.zoneminder.ZoneMinderConstants;
import org.openhab.binding.zoneminder.internal.RefreshPriorityEnum;
import org.openhab.binding.zoneminder.internal.ZoneMinderMonitorEventListener;
import org.openhab.binding.zoneminder.internal.config.ZoneMinderThingConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import name.eskildsen.zoneminder.ZoneMinderConnection;
import name.eskildsen.zoneminder.ZoneMinderFactory;
import name.eskildsen.zoneminder.ZoneMinderSession;
import name.eskildsen.zoneminder.api.ZoneMinderData;
import name.eskildsen.zoneminder.exception.ZoneMinderUrlNotFoundException;

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
    public ZoneMinderServerBridgeHandler zoneMinderBridgeHandler = null;

    /** This refresh status. */
    private boolean thingRefreshed = false;

    protected Boolean isAlive = false;

    /** Unique Id of the thing in zoneminder. */
    private String zoneMinderId;

    private ZoneMinderConnection zoneMinderConnection = null;
    private ZoneMinderSession zoneMinderSession = null;

    // TODO:: Removed
    // private Map<String, ZoneMinderData> zmMonitorDataArr = new HashMap<String, ZoneMinderData>();

    /** Configuration from OpenHAB */
    protected ZoneMinderThingConfig configuration;

    private RefreshPriorityEnum _refreshPriority = RefreshPriorityEnum.SCHEDULED;

    public RefreshPriorityEnum getRefreshPriority() {
        return _refreshPriority;
    }

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
        try {

        } catch (Exception ex) {
            logger.error("'ZoneMinderServerBridgeHandler' failed to initialize. Exception='{}'", ex.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR);
        } finally {
        }
    }

    protected ZoneMinderSession getZoneMinderSession() {
        return zoneMinderSession;
    }

    protected boolean isConnected() {
        if (zoneMinderSession == null) {
            return false;
        }
        // TODO:: FIX THIS
        return true;
        // return zoneMinderSession.isConnected();
    }

    /**
     * Method to start a priority data refresh task.
     */

    protected boolean startPriorityRefresh() {

        logger.info("Starting High Priority Refresh for Monitor '{}'", getZoneMinderId());
        _refreshPriority = RefreshPriorityEnum.HIGH_PRIORITY;
        return true;
    }

    /**
     * Method to stop the data Refresh task.
     */
    protected void stopPriorityRefresh() {
        logger.info("Stopping Priority Refresh for Monitor '{}'", getZoneMinderId());
        _refreshPriority = RefreshPriorityEnum.SCHEDULED;
    }

    @Override
    public void dispose() {

    }

    /**
     * Helper method for getting ChannelUID from ChannelId.
     *
     */
    public ChannelUID getChannelUIDFromChannelId(String id) {
        Channel ch = thing.getChannel(id);
        return ch.getUID();
    }

    protected abstract void onFetchData(ZoneMinderSession session);

    /**
     * Method to Refresh Thing Handler.
     */
    public synchronized final void refreshThing(RefreshPriorityEnum refreshPriority, ZoneMinderSession session) {

        if ((refreshPriority != getRefreshPriority()) && (!isConnected())) {
            return;
        }

        if (refreshPriority == RefreshPriorityEnum.HIGH_PRIORITY) {
            logger.debug("Performing HIGH PRIORITY UPDATE for monitor '{}'.....", getZoneMinderId());
        }

        if (isConnected()) {
            if (getZoneMinderBridgeHandler() != null) {
                logger.debug("refreshThing(): Bridge '{}' Found for Thing '{}'!",
                        zoneMinderBridgeHandler.getThing().getUID(), this.getThing().getUID());

                onFetchData(session);

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
    }

    /**
     * Get the Bridge Handler for ZoneMinder.
     *
     * @return zoneMinderBridgeHandler
     */
    public /* synchronized */ ZoneMinderServerBridgeHandler getZoneMinderBridgeHandler() {

        if (this.zoneMinderBridgeHandler == null) {

            Bridge bridge = getBridge();

            if (bridge == null) {
                logger.debug("getZoneMinderBridgeHandler(): Unable to get bridge!");
                return null;
            }

            logger.debug("getZoneMinderBridgeHandler(): Bridge for '{}' - '{}'", getThing().getUID(), bridge.getUID());
            ThingHandler handler = null;
            try {
                handler = bridge.getHandler();
            } catch (Exception ex) {
                logger.debug(String.format("Exception in 'getZoneMinderBridgeHandler()': {}", ex.getMessage()));
            }

            if (handler instanceof ZoneMinderServerBridgeHandler) {
                this.zoneMinderBridgeHandler = (ZoneMinderServerBridgeHandler) handler;
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
            case ZoneMinderConstants.CHANNEL_IS_ALIVE:
                updateState(channel, (isAlive ? OnOffType.ON : OnOffType.OFF));
                break;
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

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void onBridgeConnected(ZoneMinderServerBridgeHandler bridge, ZoneMinderConnection connection)
            throws IllegalArgumentException, GeneralSecurityException, IOException, ZoneMinderUrlNotFoundException {
        logger.debug("onBridgeConnected(): Bridge '{}' is connected", bridge.getThing().getUID());

        zoneMinderSession = ZoneMinderFactory.CreateSession(connection);

        // TODO:: Seems to be CAllled elsewhere
        /*
         * if (bridge.getThing().getUID().equals(getThing().getBridgeUID())) {
         * updateAvaliabilityStatus(connection);
         *
         * refreshThing(RefreshPriorityEnum.SCHEDULED, zoneMinderSession);
         *
         * }
         */
    }

    @Override
    public void onBridgeDisconnected(ZoneMinderServerBridgeHandler bridge) {
        logger.debug("onBridgeDisconnected(): Bridge '{}' disconnected", bridge.getThing().getUID());

        zoneMinderSession = null;

        if (bridge.getThing().getUID().equals(getThing().getBridgeUID())) {

            this.setThingRefreshed(false);
        }
    }

    /**
     * Get Channel by ChannelUID.
     *
     * @param {ChannelUID} channelUID Identifier of Channel
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
     * @param {boolean} refreshed Sets status refreshed of thing
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

    protected BigDecimal getConfigValueAsBigDecimal(String configKey) {
        return (BigDecimal) getConfigValue(configKey);
    }

    // TODO:: REWRITE This
    @Override
    public void notifyZoneMinderApiDataUpdated(ThingTypeUID thingTypeUID, String ZoneMinderId,
            List<ZoneMinderData> arrData) {
        // TODO: RENOVED
        /*
         * synchronized (zmMonitorDataArr) {
         * for (ZoneMinderData data : arrData) {
         * String dataClassKey = data.getKey();
         * if (zmMonitorDataArr.containsKey(dataClassKey)) {
         * zmMonitorDataArr.remove(dataClassKey);
         * }
         * zmMonitorDataArr.put(dataClassKey, data);
         * }
         * }
         */
    }
    /*
     * protected ZoneMinderData getZoneMinderData(Class type) {
     *
     * synchronized (zmMonitorDataArr) {
     * if (zmMonitorDataArr.containsKey(type.getSimpleName())) {
     * return zmMonitorDataArr.get(type.getSimpleName());
     * }
     * }
     * return null;
     * }
     */
}
