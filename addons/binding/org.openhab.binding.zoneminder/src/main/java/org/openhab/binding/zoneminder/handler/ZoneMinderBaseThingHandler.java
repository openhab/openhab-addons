/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zoneminder.handler;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.jdt.annotation.NonNull;
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
import org.openhab.binding.zoneminder.internal.RefreshPriority;
import org.openhab.binding.zoneminder.internal.config.ZoneMinderThingConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import name.eskildsen.zoneminder.IZoneMinderConnectionHandler;

/**
 * The {@link ZoneMinderBaseThingHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Martin S. Eskildsen - Initial contribution
 */
public abstract class ZoneMinderBaseThingHandler extends BaseThingHandler implements IZoneMinderHandler {
    // https://www.eclipse.org/smarthome/documentation/development/bindings/thing-handler.html
    private final ReentrantLock lockRefresh = new ReentrantLock();
    private final ReentrantLock lockAlarm = new ReentrantLock();

    /** Logger for the Thing. */
    private Logger logger = LoggerFactory.getLogger(ZoneMinderBaseThingHandler.class);

    /** Bridge Handler for the Thing. */
    public ZoneMinderServerBridgeHandler zoneMinderBridgeHandler;

    /** This refresh status. */
    private AtomicInteger thingRefresh = new AtomicInteger(1);

    private long alarmTimeoutTimestamp = 0;

    /** ZoneMinder ConnectionHandler */
    private IZoneMinderConnectionHandler zoneMinderConnection;

    /** Configuration from openHAB */
    protected ZoneMinderThingConfig configuration;

    private RefreshPriority refreshPriority = RefreshPriority.PRIORITY_NORMAL;

    protected boolean isThingOnline() {
        try {
            if ((thing.getStatus() == ThingStatus.ONLINE) && getZoneMinderBridgeHandler().isOnline()) {
                return true;
            }
        } catch (Exception ex) {
            logger.error("{}: context='isThingOnline' Exception occurred", getLogIdentifier(), ex);
        }
        return false;
    }

    public RefreshPriority getThingRefreshPriority() {
        return refreshPriority;
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
        updateStatus(ThingStatus.OFFLINE);
    }

    protected boolean isConnected() {
        if ((getThing().getStatus() != ThingStatus.ONLINE) || (zoneMinderConnection == null)
                || (getZoneMinderBridgeHandler() == null)) {
            return false;
        }
        return getZoneMinderBridgeHandler().isConnected();
    }

    protected IZoneMinderConnectionHandler aquireSession() {
        return aquireSessionInternal(false);
    }

    protected IZoneMinderConnectionHandler aquireSessionWait() {
        return aquireSessionInternal(false);
    }

    private IZoneMinderConnectionHandler aquireSessionInternal(boolean timeout) {
        boolean result = true;
        if (result) {
            return zoneMinderConnection;
        }

        return null;
    }

    protected void releaseSession() {
        // lockSession.unlock();
    }

    protected boolean forceStartAlarmRefresh() {
        lockAlarm.lock();
        try {
            if (refreshPriority != RefreshPriority.PRIORITY_ALARM) {
                logger.debug("{}: context='startAlarmRefresh' Starting ALARM refresh...", getLogIdentifier());
                refreshPriority = RefreshPriority.PRIORITY_ALARM;

                // If already activated and called again, it is the event from ZoneMidner that was triggered from
                // openHAB
                alarmTimeoutTimestamp = -1;
            }
        } finally {
            lockAlarm.unlock();
        }
        return true;
    }

    /**
     * Method to start a priority data refresh task.
     */

    protected boolean startAlarmRefresh(long timeout) {
        lockAlarm.lock();
        try {
            if (refreshPriority != RefreshPriority.PRIORITY_ALARM) {
                logger.debug("{}: context='startAlarmRefresh' Starting ALARM refresh...", getLogIdentifier());
                refreshPriority = RefreshPriority.PRIORITY_ALARM;
                alarmTimeoutTimestamp = System.currentTimeMillis() + timeout * 1000;
            }
        } finally {
            lockAlarm.unlock();
        }
        return true;
    }

    protected void tryStopAlarmRefresh() {
        lockAlarm.lock();
        try {
            if ((alarmTimeoutTimestamp == -1) || (refreshPriority != RefreshPriority.PRIORITY_ALARM)) {
                return;
            }
            if (alarmTimeoutTimestamp < System.currentTimeMillis()) {
                logger.debug("{}: context='tryStopAlarmRefresh' - Alarm refresh timed out - stopping alarm refresh ...",
                        getLogIdentifier());
                refreshPriority = RefreshPriority.PRIORITY_NORMAL;

                alarmTimeoutTimestamp = 0;
            }
        } finally {
            lockAlarm.unlock();
        }
    }

    /**
     * Method to stop the data Refresh task.
     */
    protected void forceStopAlarmRefresh() {
        lockAlarm.lock();
        try {
            if (refreshPriority == RefreshPriority.PRIORITY_ALARM) {
                logger.debug("{}: context='forceStopAlarmRefresh' Stopping ALARM refresh...", getLogIdentifier());
                refreshPriority = RefreshPriority.PRIORITY_NORMAL;
                alarmTimeoutTimestamp = 0;
            }
        } finally {
            lockAlarm.unlock();
        }
    }

    protected void onThingStatusChanged(ThingStatus thingStatus) {
    }

    @Override
    public void dispose() {
    }

    /**
     * Helper method for getting ChannelUID from ChannelId.
     *
     */

    public ChannelUID getChannelUIDFromChannelId(@NonNull String id) {
        Channel ch = thing.getChannel(id);
        if (ch == null) {
            return null;
        }
        return ch.getUID();
    }

    protected abstract void onFetchData(RefreshPriority refreshPriority);

    /**
     * Method to Refresh Thing Handler.
     */
    public final void refreshThing(RefreshPriority refreshPriority) {
        boolean isLocked = false;
        try {
            if (!isConnected()) {
                return;
            }

            if (refreshPriority == RefreshPriority.PRIORITY_ALARM) {
                if (!lockRefresh.tryLock()) {
                    logger.warn(
                            "{}: context='refreshThing' Failed to obtain refresh lock for '{}' - refreshThing skipped!",
                            getLogIdentifier(), getThing().getUID());
                    isLocked = false;
                    return;
                }
            } else {
                lockRefresh.lock();
            }
            isLocked = true;

            if (getZoneMinderBridgeHandler() != null) {
                onFetchData(refreshPriority);
            } else {
                logger.warn(
                        "{}: context='refreshThing' - BridgeHandler was not accessible for '{}', skipping refreshThing",
                        getLogIdentifier(), getThing().getUID());
            }

            if (!isThingRefreshed()) {
                Thing thing = getThing();
                List<Channel> channels = thing.getChannels();
                logger.debug("{}: context=refreshThing': Refreshing Channels for '{}'", getLogIdentifier(),
                        thing.getUID());

                for (Channel channel : channels) {
                    updateChannel(channel.getUID());
                }
                this.channelRefreshDone();
            }
        } catch (Exception ex) {
            logger.error("{}: context='refreshThing' - Exception when refreshing '{}' ", getLogIdentifier(),
                    getThing().getUID(), ex);
        } finally {
            if (isLocked) {
                lockRefresh.unlock();
            }
        }
    }

    /**
     * Get the Bridge Handler for ZoneMinder.
     *
     * @return zoneMinderBridgeHandler
     */
    public /* synchronized */ZoneMinderServerBridgeHandler getZoneMinderBridgeHandler() {
        if (this.zoneMinderBridgeHandler == null) {
            Bridge bridge = getBridge();

            if (bridge == null) {
                logger.warn("{}: context='getZoneMinderBridgeHandler' - Unable to get bridge!", getLogIdentifier());
                return null;
            }

            logger.debug("{}: context='getZoneMinderBridgeHandler' Bridge for '{}' - '{}'", getLogIdentifier(),
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
                logger.debug("{}: context='getZoneMinderBridgeHandler' Unable to get bridge handler!",
                        getLogIdentifier());
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
        switch (channel.getId()) {
            case ZoneMinderConstants.CHANNEL_ONLINE:
                updateState(channel, getChannelBoolAsOnOffState(isThingOnline()));
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
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        super.bridgeStatusChanged(bridgeStatusInfo);
    }

    @Override
    public void onBridgeDisconnected(ZoneMinderServerBridgeHandler bridge) {
        zoneMinderConnection = null;
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
        return (thingRefresh.get() > 0) ? false : true;
        // return (thingRefresh > 0) ? false : true;
    }

    /**
     * Set Thing Handler refresh status.
     *
     * @param {boolean} refreshed Sets status refreshed of thing
     */
    public void requestChannelRefresh() {
        thingRefresh.incrementAndGet();
        // this.thingRefresh = this.thingRefresh + 1;
    }

    public void channelRefreshDone() {
        if (thingRefresh.decrementAndGet() < 0) {
            thingRefresh.set(0);
        }
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

            logger.error("{}: Exception occurred in 'getChannelStringAsStringState' ", getLogIdentifier(), ex);
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
            logger.error("{}: Exception occurred in 'getChannelBoolAsOnOffState()' ", getLogIdentifier(), ex);
        }

        return state;
    }

    @Override
    public void onBridgeConnected(ZoneMinderServerBridgeHandler bridge, IZoneMinderConnectionHandler connection) {
        zoneMinderConnection = connection;
    }

    @Override
    public abstract String getLogIdentifier();

    protected void updateThingStatus(ThingStatus thingStatus, ThingStatusDetail statusDetail,
            String statusDescription) {
        ThingStatusInfo curStatusInfo = thing.getStatusInfo();
        String curDescription = ((curStatusInfo.getDescription() == null) ? "" : curStatusInfo.getDescription());

        // Status changed
        if (!curStatusInfo.getStatus().equals(thingStatus) || !curStatusInfo.getStatusDetail().equals(statusDetail)
                || !curDescription.equals(statusDescription)) {
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

            onThingStatusChanged(thingStatus);
        }
    }
}
