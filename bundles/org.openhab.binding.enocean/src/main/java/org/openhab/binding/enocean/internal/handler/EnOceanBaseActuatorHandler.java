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
package org.openhab.binding.enocean.internal.handler;

import static org.openhab.binding.enocean.internal.EnOceanBindingConstants.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.util.HexUtils;
import org.openhab.binding.enocean.internal.config.EnOceanActuatorConfig;
import org.openhab.binding.enocean.internal.eep.EEP;
import org.openhab.binding.enocean.internal.eep.EEPFactory;
import org.openhab.binding.enocean.internal.eep.EEPType;
import org.openhab.binding.enocean.internal.messages.ESP3Packet;

/**
 *
 * @author Daniel Weber - Initial contribution
 *         This class defines base functionality for sending eep messages. This class extends EnOceanBaseSensorHandler
 *         class as most actuator things send status or response messages, too.
 */
public class EnOceanBaseActuatorHandler extends EnOceanBaseSensorHandler {

    // List of thing types which support sending of eep messages
    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES = new HashSet<ThingTypeUID>(
            Arrays.asList(THING_TYPE_CENTRALCOMMAND, THING_TYPE_MEASUREMENTSWITCH, THING_TYPE_GENERICTHING,
                    THING_TYPE_ROLLERSHUTTER, THING_TYPE_THERMOSTAT));

    protected byte[] senderId; // base id of bridge + senderIdOffset, used for sending msg
    protected byte[] destinationId; // in case of broadcast FFFFFFFF otherwise the enocean id of the device

    protected EEPType sendingEEPType = null;

    private ScheduledFuture<?> refreshJob; // used for polling current status of thing

    public EnOceanBaseActuatorHandler(Thing thing) {
        super(thing);
    }

    /**
     *
     * @param senderIdOffset to be validated
     * @return true if senderIdOffset is between ]0;128[ and is not used yet
     */
    private boolean validateSenderIdOffset(int senderIdOffset) {
        if (senderIdOffset == -1) {
            return true;
        }

        if (senderIdOffset > 0 && senderIdOffset < 128) {

            EnOceanBridgeHandler bridgeHandler = getBridgeHandler();
            if (bridgeHandler != null) {
                return !bridgeHandler.existsSender(senderIdOffset, this.thing);
            }
        }

        return false;
    }

    @Override
    void initializeConfig() {
        config = getConfigAs(EnOceanActuatorConfig.class);
    }

    protected EnOceanActuatorConfig getConfiguration() {
        return (EnOceanActuatorConfig) config;
    }

    @Override
    boolean validateConfig() {
        if (super.validateConfig()) {

            try {
                sendingEEPType = EEPType.getType(getConfiguration().sendingEEPId);
                updateChannels(sendingEEPType, false);

                if (sendingEEPType.getSupportsRefresh()) {
                    if (getConfiguration().pollingInterval > 0) {
                        refreshJob = scheduler.scheduleWithFixedDelay(() -> {
                            try {
                                refreshStates();
                            } catch (Exception e) {

                            }
                        }, 30, getConfiguration().pollingInterval, TimeUnit.SECONDS);
                    }
                }

                if (getConfiguration().broadcastMessages) {
                    destinationId = new byte[] { (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff };
                } else {
                    destinationId = HexUtils.hexToBytes(config.enoceanId);
                }

            } catch (Exception e) {
                configurationErrorDescription = "Sending EEP is not supported";
                return false;
            }

            if (validateSenderIdOffset(getConfiguration().senderIdOffset)) {
                return initializeIdForSending();
            } else {
                configurationErrorDescription = "Sender Id is not valid for bridge";
            }
        }

        return false;
    }

    private boolean initializeIdForSending() {
        // Generic things are treated as actuator things, however to support also generic sensors one can define a
        // senderIdOffset of -1
        // TODO: seperate generic actuators from generic sensors?
        String thingTypeId = this.getThing().getThingTypeUID().getId();
        String genericThingTypeId = THING_TYPE_GENERICTHING.getId();

        if (getConfiguration().senderIdOffset == -1 && thingTypeId.equals(genericThingTypeId)) {
            return true;
        }

        EnOceanBridgeHandler bridgeHandler = getBridgeHandler();
        if (bridgeHandler == null) {
            return false;
        }

        // if senderIdOffset is not set (=> defaults to -1) or set to -1, the next free senderIdOffset is determined
        if (getConfiguration().senderIdOffset == -1) {
            Configuration updateConfig = editConfiguration();
            getConfiguration().senderIdOffset = bridgeHandler.getNextSenderId(thing);
            if (getConfiguration().senderIdOffset == -1) {
                configurationErrorDescription = "Could not get a free sender Id from Bridge";
                return false;
            }
            updateConfig.put(PARAMETER_SENDERIDOFFSET, getConfiguration().senderIdOffset);
            updateConfiguration(updateConfig);
        }

        byte[] baseId = bridgeHandler.getBaseId();
        baseId[3] = (byte) ((baseId[3] & 0xFF) + getConfiguration().senderIdOffset);
        this.senderId = baseId;

        this.updateProperty(PROPERTY_ENOCEAN_ID, HexUtils.bytesToHex(this.senderId));
        bridgeHandler.addSender(getConfiguration().senderIdOffset, thing);

        return true;
    }

    private void refreshStates() {

        logger.debug("polling channels");
        if (thing.getStatus().equals(ThingStatus.ONLINE)) {
            for (Channel channel : getLinkedChannels()) {
                handleCommand(channel.getUID(), RefreshType.REFRESH);
            }
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        // We must have a valid sendingEEPType and sender id to send commands
        if (sendingEEPType == null || senderId == null) {
            return;
        }

        String channelId = channelUID.getId();
        Channel channel = getThing().getChannel(channelId);
        if (channel == null) {
            return;
        }

        ChannelTypeUID channelTypeUID = channel.getChannelTypeUID();
        String channelTypeId = (channelTypeUID != null) ? channelTypeUID.getId() : "";

        // check if we do support refreshs
        if (command == RefreshType.REFRESH) {
            if (!sendingEEPType.getSupportsRefresh()) {
                return;
            }

            // receiving status cannot be refreshed
            switch (channelTypeId) {
                case CHANNEL_RSSI:
                case CHANNEL_REPEATCOUNT:
                case CHANNEL_LASTRECEIVED:
                    return;
            }
        }

        // check if the channel is linked otherwise do nothing
        if (!getLinkedChannels().contains(channel)) {
            return;
        }

        try {
            Configuration channelConfig = channel.getConfiguration();

            EEP eep = EEPFactory.createEEP(sendingEEPType);
            eep.convertFromCommand(channelId, channelTypeId, command, channelState, channelConfig);

            if (eep.hasData()) {
                ESP3Packet msg = eep.setSenderId(senderId).setDestinationId(destinationId)
                        .setSuppressRepeating(getConfiguration().suppressRepeating).getERP1Message();

                getBridgeHandler().sendMessage(msg, null);
            }

        } catch (IllegalArgumentException e) {
            logger.warn("Exception while sending telegram!", e);
        }
    }

    @Override
    public void handleRemoval() {
        if (getConfiguration().senderIdOffset > 0) {
            EnOceanBridgeHandler bridgeHandler = getBridgeHandler();
            if (bridgeHandler != null) {
                bridgeHandler.removeSender(getConfiguration().senderIdOffset);
            }
        }

        super.handleRemoval();
    }

    @Override
    public void dispose() {
        if (refreshJob != null && !refreshJob.isCancelled()) {
            refreshJob.cancel(true);
            refreshJob = null;
        }
    }
}
