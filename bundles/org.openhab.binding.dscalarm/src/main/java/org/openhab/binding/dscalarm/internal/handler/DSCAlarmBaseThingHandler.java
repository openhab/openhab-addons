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
package org.openhab.binding.dscalarm.internal.handler;

import static org.openhab.binding.dscalarm.internal.DSCAlarmBindingConstants.PANEL_MESSAGE;

import java.util.EventObject;
import java.util.List;

import org.openhab.binding.dscalarm.internal.DSCAlarmCode;
import org.openhab.binding.dscalarm.internal.DSCAlarmMessage;
import org.openhab.binding.dscalarm.internal.DSCAlarmMessage.DSCAlarmMessageInfoType;
import org.openhab.binding.dscalarm.internal.config.DSCAlarmPanelConfiguration;
import org.openhab.binding.dscalarm.internal.config.DSCAlarmPartitionConfiguration;
import org.openhab.binding.dscalarm.internal.config.DSCAlarmZoneConfiguration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract class for a DSC Alarm Thing Handler.
 *
 * @author Russell Stephens - Initial Contribution
 */
public abstract class DSCAlarmBaseThingHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(DSCAlarmBaseThingHandler.class);

    /** Bridge Handler for the Thing. */
    public DSCAlarmBaseBridgeHandler dscAlarmBridgeHandler = null;

    /** DSC Alarm Thing type. */
    private DSCAlarmThingType dscAlarmThingType = null;

    /** DSC Alarm Properties. */

    private boolean thingHandlerInitialized = false;

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

    @Override
    public void initialize() {
        logger.debug("Initializing DSC Alarm Thing handler - Thing Type: {}; Thing ID: {}.", dscAlarmThingType,
                this.getThing().getUID());

        getConfiguration(dscAlarmThingType);

        Bridge bridge = getBridge();
        initializeThingHandler(bridge != null ? bridge.getStatus() : null);
        this.setThingHandlerInitialized(true);
    }

    @Override
    public void dispose() {
        logger.debug("Thing {} disposed.", getThing().getUID());
        this.setThingHandlerInitialized(false);
        super.dispose();
    }

    /**
     * Method to Initialize Thing Handler.
     */
    private void initializeThingHandler(ThingStatus bridgeStatus) {
        if (getDSCAlarmBridgeHandler() != null && bridgeStatus != null) {
            if (bridgeStatus == ThingStatus.ONLINE) {
                Thing thing = getThing();
                List<Channel> channels = thing.getChannels();
                logger.debug("initializeThingHandler(): Initialize Thing Handler - {}", thing.getUID());

                for (Channel channel : channels) {
                    if (channel.getAcceptedItemType().equals("DateTime")) {
                        updateChannel(channel.getUID(), 0, "0000010100");
                    } else {
                        updateChannel(channel.getUID(), 0, "");
                    }
                }

                if (dscAlarmThingType.equals(DSCAlarmThingType.PANEL)) {
                    dscAlarmBridgeHandler.setUserCode(getUserCode());
                }

                updateStatus(ThingStatus.ONLINE);

                logger.debug("initializeThingHandler(): Thing Handler Initialized - {}", thing.getUID());
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
                logger.debug("initializeThingHandler(): Thing '{}' is set to OFFLINE because bridge is OFFLINE",
                        thing.getUID());
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED);
            logger.debug("initializeThingHandler(): Thing '{}' is set to OFFLINE because bridge is uninitialized",
                    thing.getUID());
        }
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
     * @param state
     * @param description
     */
    public abstract void updateChannel(ChannelUID channel, int state, String description);

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

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        logger.debug("bridgeStatusChanged(): Bridge Status: '{}' - Thing '{}' Status: '{}'!", bridgeStatusInfo,
                getThing().getUID(), getThing().getStatus());
        initializeThingHandler(bridgeStatusInfo.getStatus());
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
                DSCAlarmPartitionConfiguration partitionConfiguration = getConfigAs(
                        DSCAlarmPartitionConfiguration.class);
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
            if (ch.getUID().equals(channelUID)) {
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
    public boolean isThingHandlerInitialized() {
        return thingHandlerInitialized;
    }

    /**
     * Set Thing Handler refresh status.
     *
     * @param deviceInitialized
     */
    public void setThingHandlerInitialized(boolean refreshed) {
        this.thingHandlerInitialized = refreshed;
    }

    /**
     * Method to set the panel message.
     *
     * @param dscAlarmMessage
     */
    public void setPanelMessage(DSCAlarmMessage dscAlarmMessage) {
        ChannelUID channelUID = new ChannelUID(getThing().getUID(), PANEL_MESSAGE);
        String message = dscAlarmMessage.getMessageInfo(DSCAlarmMessageInfoType.DESCRIPTION);
        DSCAlarmCode dscAlarmCode = DSCAlarmCode
                .getDSCAlarmCodeValue(dscAlarmMessage.getMessageInfo(DSCAlarmMessageInfoType.CODE));

        if ((dscAlarmCode == DSCAlarmCode.CommandAcknowledge || dscAlarmCode == DSCAlarmCode.TimeDateBroadcast)
                && getSuppressAcknowledgementMsgs()) {
            return;
        } else {
            updateChannel(channelUID, 0, message);
            logger.debug("setPanelMessage(): Panel Message Set to - {}", message);
        }
    }
}
