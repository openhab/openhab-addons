/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.dscalarm.handler;

import java.util.EventObject;
import java.util.List;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.dscalarm.config.DSCAlarmPanelConfiguration;
import org.openhab.binding.dscalarm.config.DSCAlarmPartitionConfiguration;
import org.openhab.binding.dscalarm.config.DSCAlarmZoneConfiguration;
import org.openhab.binding.dscalarm.internal.DSCAlarmProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract class for a DSC Alarm Thing Handler.
 *
 * @author Russell Stephens - Initial Contribution
 */
public abstract class DSCAlarmBaseThingHandler extends BaseThingHandler {

    private Logger logger = LoggerFactory.getLogger(DSCAlarmBaseThingHandler.class);

    /** Bridge Handler for the Thing. */
    public DSCAlarmBaseBridgeHandler dscAlarmBridgeHandler = null;

    /** DSC Alarm Thing type. */
    private DSCAlarmThingType dscAlarmThingType = null;

    /** DSC Alarm Properties. */
    public DSCAlarmProperties properties = null;

    /** This refresh status. */
    private boolean thingRefreshed = false;

    /** User Code for some DSC Alarm commands. */
    private String userCode = null;

    /** Suppress Acknowledge messages when received. */
    private boolean suppressAcknowledgementMsgs = false;

    /** Partition Number. */
    private int partitionNumber;

    /** Zone Number. */
    private int zoneNumber;

    /**
     * Constructor.
     *
     * @param thing
     */
    public DSCAlarmBaseThingHandler(Thing thing) {
        super(thing);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize() {
        logger.debug("Initializing DSC Alarm Thing handler - Thing Type: {}; Thing ID: {}.", dscAlarmThingType, this.getThing().getUID());

        getConfiguration(dscAlarmThingType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
        logger.debug("Thing {} disposed.", getThing().getUID());

        this.setThingRefreshed(false);
        super.dispose();
    }

    /**
     * Method to Refresh Thing Handler.
     */
    public void refreshThing() {

        if (getDSCAlarmBridgeHandler() != null) {
            logger.debug("refreshThing(): Bridge '{}' Found for Thing '{}'!", dscAlarmBridgeHandler.getThing().getUID(), this.getThing().getUID());

            Thing thing = getThing();
            List<Channel> channels = thing.getChannels();
            logger.debug("refreshThing(): Refreshing Thing - {}", thing.getUID());

            this.properties = new DSCAlarmProperties();

            for (Channel channel : channels) {
                updateChannel(channel.getUID());
            }

            if (dscAlarmThingType.equals(DSCAlarmThingType.PANEL)) {
                dscAlarmBridgeHandler.setUserCode(getUserCode());
            }

            this.setThingRefreshed(true);
        }
        logger.debug("refreshThing(): Thing Refreshed - {}", thing.getUID());
    }

    /**
     * Get the Bridge Handler for the DSC Alarm.
     *
     * @return dscAlarmBridgeHandler
     */
    public synchronized DSCAlarmBaseBridgeHandler getDSCAlarmBridgeHandler() {

        if (this.dscAlarmBridgeHandler == null) {

            Bridge bridge = getBridge();

            if (bridge == null) {
                logger.debug("getDSCAlarmBridgeHandler(): Unable to get bridge!");
                return null;
            }

            logger.debug("getDSCAlarmBridgeHandler(): Bridge for '{}' - '{}'", getThing().getUID(), bridge.getUID());

            ThingHandler handler = bridge.getHandler();

            if (handler instanceof DSCAlarmBaseBridgeHandler) {
                this.dscAlarmBridgeHandler = (DSCAlarmBaseBridgeHandler) handler;
            } else {
                logger.debug("getDSCAlarmBridgeHandler(): Unable to get bridge handler!");
            }
        }

        return this.dscAlarmBridgeHandler;
    }

    /**
     * Method to Update a Channel
     *
     * @param channel
     */
    public abstract void updateChannel(ChannelUID channel);

    /**
     * Method to Update Device Properties.
     *
     * @param channelUID
     * @param state
     * @param description
     */
    public abstract void updateProperties(ChannelUID channelUID, int state, String description);

    /**
     * Receives DSC Alarm Events from the bridge.
     *
     * @param event.
     * @param thing
     */
    public abstract void dscAlarmEventReceived(EventObject event, Thing thing);

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    public void onBridgeConnected(DSCAlarmBaseBridgeHandler bridge) {
        logger.debug("onBridgeConnected(): Bridge '{}' is connected", bridge.getThing().getUID());

        if (bridge.getThing().getUID().equals(getThing().getBridgeUID())) {
            updateStatus(bridge.getThing().getStatus());
            refreshThing();
        }
    }

    public void onBridgeDisconnected(DSCAlarmBaseBridgeHandler bridge) {
        logger.debug("onBridgeDisconnected(): Bridge '{}' disconnected", bridge.getThing().getUID());

        if (bridge.getThing().getUID().equals(getThing().getBridgeUID())) {
            this.setThingRefreshed(false);
        }
    }

    /**
     * Get the thing configuration.
     *
     * @param dscAlarmDeviceType
     */
    private void getConfiguration(DSCAlarmThingType dscAlarmDeviceType) {

        switch (dscAlarmDeviceType) {
            case PANEL:
                DSCAlarmPanelConfiguration panelConfiguration = getConfigAs(DSCAlarmPanelConfiguration.class);
                setUserCode(panelConfiguration.userCode);
                setSuppressAcknowledgementMsgs(panelConfiguration.suppressAcknowledgementMsgs);
                break;
            case PARTITION:
                DSCAlarmPartitionConfiguration partitionConfiguration = getConfigAs(DSCAlarmPartitionConfiguration.class);
                setPartitionNumber(partitionConfiguration.partitionNumber.intValue());
                break;
            case ZONE:
                DSCAlarmZoneConfiguration zoneConfiguration = getConfigAs(DSCAlarmZoneConfiguration.class);
                setPartitionNumber(zoneConfiguration.partitionNumber.intValue());
                setZoneNumber(zoneConfiguration.zoneNumber.intValue());
                break;
            case KEYPAD:
            default:
                break;
        }
    }

    /**
     * Get the DSC Alarm Thing type.
     *
     * @return dscAlarmThingType
     */
    public DSCAlarmThingType getDSCAlarmThingType() {
        return dscAlarmThingType;
    }

    /**
     * Set the DSC Alarm Thing type.
     *
     * @param dscAlarmDeviceType
     */
    public void setDSCAlarmThingType(DSCAlarmThingType dscAlarmDeviceType) {
        if (dscAlarmDeviceType == null) {
            String thingType = getThing().getThingTypeUID().toString().split(":")[1];
            this.dscAlarmThingType = DSCAlarmThingType.getDSCAlarmThingType(thingType);
        } else {
            this.dscAlarmThingType = dscAlarmDeviceType;
        }
    }

    /**
     * Get suppressAcknowledgementMsgs.
     *
     * @return suppressAcknowledgementMsgs
     */
    public boolean getSuppressAcknowledgementMsgs() {
        return suppressAcknowledgementMsgs;
    }

    /**
     * Set suppressAcknowledgementMsgs.
     *
     * @param suppressAckMsgs
     */
    public void setSuppressAcknowledgementMsgs(boolean suppressAckMsgs) {
        this.suppressAcknowledgementMsgs = suppressAckMsgs;
    }

    /**
     * Get Partition Number.
     *
     * @return partitionNumber
     */
    public int getPartitionNumber() {
        return partitionNumber;
    }

    /**
     * Set Partition Number.
     *
     * @param partitionNumber
     */
    public void setPartitionNumber(int partitionNumber) {
        this.partitionNumber = partitionNumber;
    }

    /**
     * Get Zone Number.
     *
     * @return zoneNumber
     */
    public int getZoneNumber() {
        return zoneNumber;
    }

    /**
     * Set Zone Number.
     *
     * @param zoneNumber
     */
    public void setZoneNumber(int zoneNumber) {
        this.zoneNumber = zoneNumber;
    }

    /**
     * Get User Code.
     *
     * @return userCode
     */
    public String getUserCode() {
        return userCode;
    }

    /**
     * Set User Code.
     *
     * @param userCode
     */
    public void setUserCode(String userCode) {
        this.userCode = userCode;
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
}
