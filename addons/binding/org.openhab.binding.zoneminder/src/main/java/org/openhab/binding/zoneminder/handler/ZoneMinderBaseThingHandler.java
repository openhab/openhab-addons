/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
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
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.zoneminder.ZoneMinderConstants;
import org.openhab.binding.zoneminder.internal.DataRefreshPriorityEnum;
import org.openhab.binding.zoneminder.internal.config.ZoneMinderThingConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import name.eskildsen.zoneminder.IZoneMinderConnectionInfo;
import name.eskildsen.zoneminder.IZoneMinderSession;
import name.eskildsen.zoneminder.ZoneMinderFactory;
import name.eskildsen.zoneminder.exception.ZoneMinderUrlNotFoundException;

/**
 * The {@link ZoneMinderBaseThingHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Martin S. Eskildsen - Initial contribution
 */
public abstract class ZoneMinderBaseThingHandler extends BaseThingHandler implements ZoneMinderHandler {

    /** Logger for the Thing. */
    private Logger logger = LoggerFactory.getLogger(ZoneMinderBaseThingHandler.class);

    /** Bridge Handler for the Thing. */
    public ZoneMinderServerBridgeHandler zoneMinderBridgeHandler = null;

    /** This refresh status. */
    private boolean thingRefreshed = false;

    /** Unique Id of the thing in zoneminder. */
    private String zoneMinderId;

    /** ZoneMidner ConnectionInfo */
    private IZoneMinderConnectionInfo zoneMinderConnection = null;

    private Lock lockSession = new ReentrantLock();
    private IZoneMinderSession zoneMinderSession = null;

    /** Configuration from openHAB */
    protected ZoneMinderThingConfig configuration;

    private DataRefreshPriorityEnum _refreshPriority = DataRefreshPriorityEnum.SCHEDULED;

    protected boolean isOnline() {

        if (zoneMinderSession == null) {
            return false;
        }

        if (!zoneMinderSession.isConnected()) {
            return false;
        }

        return true;
    }

    public DataRefreshPriorityEnum getRefreshPriority() {
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

        updateStatus(ThingStatus.ONLINE);
        try {

        } catch (Exception ex) {
            logger.error("{}: BridgeHandler failed to initialize. Exception='{}'", getLogIdentifier(), ex.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR);
        } finally {
        }
    }

    protected boolean isConnected() {
        if (zoneMinderSession == null) {
            return false;
        }
        return zoneMinderSession.isConnected();
    }

    protected IZoneMinderSession aquireSession() {
        lockSession.lock();
        return zoneMinderSession;
    }

    protected void releaseSession() {
        lockSession.unlock();
    }

    /**
     * Method to start a priority data refresh task.
     */

    protected boolean startPriorityRefresh() {

        logger.info("[MONITOR-{}]: Starting High Priority Refresh", getZoneMinderId());
        _refreshPriority = DataRefreshPriorityEnum.HIGH_PRIORITY;
        return true;
    }

    /**
     * Method to stop the data Refresh task.
     */
    protected void stopPriorityRefresh() {
        logger.info("{}: Stopping Priority Refresh for Monitor", getLogIdentifier());
        _refreshPriority = DataRefreshPriorityEnum.SCHEDULED;
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

    protected abstract void onFetchData();

    /**
     * Method to Refresh Thing Handler.
     */
    public final synchronized void refreshThing(IZoneMinderSession session, DataRefreshPriorityEnum refreshPriority) {

        if ((refreshPriority != getRefreshPriority()) && (!isConnected())) {
            return;
        }

        if (refreshPriority == DataRefreshPriorityEnum.HIGH_PRIORITY) {
            logger.debug("{}: Performing HIGH PRIORITY refresh", getLogIdentifier());
        } else {
            logger.debug("{}: Performing refresh", getLogIdentifier());
        }

        if (getZoneMinderBridgeHandler() != null) {
            if (isConnected()) {

                logger.debug("{}: refreshThing(): Bridge '{}' Found for Thing '{}'!", getLogIdentifier(),
                        getThing().getUID(), this.getThing().getUID());

                onFetchData();
            }
        }

        Thing thing = getThing();
        List<Channel> channels = thing.getChannels();
        logger.debug("{}: refreshThing(): Refreshing Thing - {}", getLogIdentifier(), thing.getUID());

        for (Channel channel : channels) {
            updateChannel(channel.getUID());
        }

        this.setThingRefreshed(true);
        logger.debug("[{}: refreshThing(): Thing Refreshed - {}", getLogIdentifier(), thing.getUID());

    }

    /**
     * Get the Bridge Handler for ZoneMinder.
     *
     * @return zoneMinderBridgeHandler
     */
    public synchronized ZoneMinderServerBridgeHandler getZoneMinderBridgeHandler() {

        if (this.zoneMinderBridgeHandler == null) {

            Bridge bridge = getBridge();

            if (bridge == null) {
                logger.debug("{}: getZoneMinderBridgeHandler(): Unable to get bridge!", getLogIdentifier());
                return null;
            }

            logger.debug("{}: getZoneMinderBridgeHandler(): Bridge for '{}' - '{}'", getLogIdentifier(),
                    getThing().getUID(), bridge.getUID());
            ThingHandler handler = null;
            try {
                handler = bridge.getHandler();
            } catch (Exception ex) {
                logger.debug("{}: Exception in 'getZoneMinderBridgeHandler()': {}", getLogIdentifier(),
                        ex.getMessage());
            }

            if (handler instanceof ZoneMinderServerBridgeHandler) {
                this.zoneMinderBridgeHandler = (ZoneMinderServerBridgeHandler) handler;
            } else {
                logger.debug("{}: getZoneMinderBridgeHandler(): Unable to get bridge handler!", getLogIdentifier());
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
            case ZoneMinderConstants.CHANNEL_ONLINE:
                updateState(channel, getChannelBoolAsOnOffState(isOnline()));
                break;
            default:
                logger.error(
                        "{}: updateChannel() in base class, called for an unknown channel '{}', this channel must be handled in super class.",
                        getLogIdentifier(), channel.getId());
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void onBridgeConnected(ZoneMinderServerBridgeHandler bridge, IZoneMinderConnectionInfo connection)
            throws IllegalArgumentException, GeneralSecurityException, IOException, ZoneMinderUrlNotFoundException {
        lockSession.lock();
        try {
            zoneMinderSession = ZoneMinderFactory.CreateSession(connection);

        } finally {
            lockSession.unlock();
        }
    }

    @Override
    public void onBridgeDisconnected(ZoneMinderServerBridgeHandler bridge) {

        if (bridge.getThing().getUID().equals(getThing().getBridgeUID())) {

            this.setThingRefreshed(false);
        }

        lockSession.lock();
        try {
            zoneMinderSession = null;

        } finally {
            lockSession.unlock();
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

    protected State getChannelStringAsStringState(String channelValue) {
        State state = UnDefType.UNDEF;

        try {
            if (isConnected()) {
                state = new StringType(channelValue);
            }

        } catch (Exception ex) {
            logger.error("{}", ex.getMessage());
        }

        return state;

    }

    protected State getChannelBoolAsOnOffState(boolean value) {
        State state = UnDefType.UNDEF;

        try {
            if (isConnected()) {
                state = value ? OnOffType.ON : OnOffType.OFF;
            }

        } catch (Exception ex) {
            logger.error("{}: Exception occurred in 'getChannelBoolAsOnOffState()' (Exception='{}')",
                    getLogIdentifier(), ex.getMessage());
        }

        return state;
    }

    @Override
    public abstract String getLogIdentifier();

    protected void updateThingStatus(ThingStatus thingStatus, ThingStatusDetail statusDetail,
            String statusDescription) {

        ThingStatusInfo curStatusInfo = thing.getStatusInfo();
        String curDescription = ((curStatusInfo.getDescription() == null) ? "" : curStatusInfo.getDescription());
        // Status changed
        if ((curStatusInfo.getStatus() != thingStatus) || (curStatusInfo.getStatusDetail() != statusDetail)
                || (curDescription != statusDescription)) {

            // Update Status correspondingly
            if ((thingStatus == ThingStatus.OFFLINE) && (statusDetail != ThingStatusDetail.NONE)) {
                logger.info("{}: Thing status changed from '{}' to '{}' (DetailedStatus='{}', Description='{}')",
                        getLogIdentifier(), thing.getStatus(), thingStatus, statusDetail, statusDescription);
                updateStatus(thingStatus, statusDetail, statusDescription);
            } else {
                logger.info("{}: Thing status changed from '{}' to '{}'", getLogIdentifier(), thing.getStatus(),
                        thingStatus);
                updateStatus(thingStatus);
            }
        }
    }

}
