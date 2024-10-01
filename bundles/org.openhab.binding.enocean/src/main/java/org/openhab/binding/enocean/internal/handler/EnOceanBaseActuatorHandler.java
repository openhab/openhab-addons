/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.enocean.internal.config.EnOceanActuatorConfig;
import org.openhab.binding.enocean.internal.eep.EEP;
import org.openhab.binding.enocean.internal.eep.EEPFactory;
import org.openhab.binding.enocean.internal.eep.EEPType;
import org.openhab.binding.enocean.internal.messages.BasePacket;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.link.ItemChannelLinkRegistry;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.util.HexUtils;

/**
 *
 * @author Daniel Weber - Initial contribution
 *         This class defines base functionality for sending eep messages. This class extends EnOceanBaseSensorHandler
 *         class as most actuator things send status or response messages, too.
 */
@NonNullByDefault
public class EnOceanBaseActuatorHandler extends EnOceanBaseSensorHandler {

    // List of thing types which support sending of eep messages
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_CENTRALCOMMAND,
            THING_TYPE_MEASUREMENTSWITCH, THING_TYPE_GENERICTHING, THING_TYPE_ROLLERSHUTTER, THING_TYPE_THERMOSTAT,
            THING_TYPE_HEATRECOVERYVENTILATION);

    protected byte[] senderId = new byte[0]; // base id of bridge + senderIdOffset, used for sending msg
    protected byte[] destinationId = new byte[0]; // in case of broadcast FFFFFFFF otherwise the enocean id of the
                                                  // device

    protected @Nullable EEPType sendingEEPType = null;

    private @Nullable ScheduledFuture<?> refreshJob; // used for polling current status of thing

    public EnOceanBaseActuatorHandler(Thing thing, ItemChannelLinkRegistry itemChannelLinkRegistry) {
        super(thing, itemChannelLinkRegistry);
    }

    /**
     *
     * @param senderIdOffset to be validated
     * @return true if senderIdOffset is between ]0;128[ and is not used yet
     */
    private boolean validateSenderIdOffset(@Nullable Integer senderIdOffset) {
        if (senderIdOffset == null) {
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
    @Nullable
    Collection<EEPType> getEEPTypes() {
        Collection<EEPType> r = super.getEEPTypes();

        if (sendingEEPType == null) {
            return r;
        }
        if (r == null) {
            r = Collections.emptyList();
        }
        return Collections.unmodifiableCollection(Stream
                .concat(r.stream(), Collections.singletonList(sendingEEPType).stream()).collect(Collectors.toList()));
    }

    @Override
    boolean validateConfig() {
        EnOceanActuatorConfig config = getConfiguration();

        if (config.sendingEEPId.isEmpty()) {
            configurationErrorDescription = "Sending EEP must be provided";
            return false;
        }

        EEPType localEEPType = null;
        try {
            localEEPType = EEPType.getType(getConfiguration().sendingEEPId);
            sendingEEPType = localEEPType;
        } catch (IllegalArgumentException e) {
            configurationErrorDescription = "Sending EEP is not supported";
            return false;
        }

        if (super.validateConfig()) {
            try {
                if (localEEPType.getSupportsRefresh()) {
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
                configurationErrorDescription = "Configuration is not valid";
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
        EnOceanBridgeHandler bridgeHandler = getBridgeHandler();
        if (bridgeHandler == null) {
            return false;
        }

        // Generic things are treated as actuator things, however to support also generic sensors one can omit
        // senderIdOffset
        // TODO: seperate generic actuators from generic sensors?
        Integer senderOffset = getConfiguration().senderIdOffset;

        if ((senderOffset == null && THING_TYPE_GENERICTHING.equals(this.getThing().getThingTypeUID()))) {
            return true;
        }

        // if senderIdOffset is not set, the next free senderIdOffset is determined
        if (senderOffset == null) {
            Configuration updateConfig = editConfiguration();
            senderOffset = bridgeHandler.getNextSenderId(thing);
            getConfiguration().senderIdOffset = senderOffset;
            if (senderOffset == null) {
                configurationErrorDescription = "Could not get a free sender Id from Bridge";
                return false;
            }
            updateConfig.put(PARAMETER_SENDERIDOFFSET, senderOffset);
            updateConfiguration(updateConfig);
        }

        byte[] baseId = bridgeHandler.getBaseId();
        baseId[3] = (byte) ((baseId[3] + senderOffset) & 0xFF);
        this.senderId = baseId;
        this.updateProperty(PROPERTY_SENDINGENOCEAN_ID, HexUtils.bytesToHex(this.senderId));
        bridgeHandler.addSender(senderOffset, thing);
        return true;
    }

    private void refreshStates() {
        logger.debug("polling channels");
        if (thing.getStatus().equals(ThingStatus.ONLINE)) {
            for (Channel channel : this.getThing().getChannels()) {
                handleCommand(channel.getUID(), RefreshType.REFRESH);
            }
        }
    }

    @Override
    protected void sendRequestResponse() {
        sendMessage(VIRTUALCHANNEL_SEND_COMMAND, VIRTUALCHANNEL_SEND_COMMAND, OnOffType.ON, null);
    }

    protected void sendMessage(String channelId, String channelTypeId, Command command,
            @Nullable Configuration channelConfig) {
        EEPType sendType = sendingEEPType;
        if (sendType == null) {
            logger.warn("cannot send a message with an empty EEPType");
            return;
        }
        EEP eep = EEPFactory.createEEP(sendType);
        if (eep.convertFromCommand(channelId, channelTypeId, command, id -> getCurrentState(id), channelConfig)
                .hasData()) {
            BasePacket msg = eep.setSenderId(senderId).setDestinationId(destinationId)
                    .setSuppressRepeating(getConfiguration().suppressRepeating).getERP1Message();
            if (msg == null) {
                logger.warn("cannot send an empty message");
                return;
            }
            EnOceanBridgeHandler handler = getBridgeHandler();
            if (handler != null) {
                handler.sendMessage(msg, null);
            }
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // We must have a valid sendingEEPType and sender id to send commands
        EEPType localsendingType = sendingEEPType;
        if (localsendingType == null) {
            return;
        }

        // check if the channel is linked otherwise do nothing
        String channelId = channelUID.getId();
        Channel channel = getThing().getChannel(channelUID);
        if (channel == null || !isLinked(channelUID)) {
            return;
        }

        ChannelTypeUID channelTypeUID = channel.getChannelTypeUID();
        String channelTypeId = (channelTypeUID != null) ? channelTypeUID.getId() : "";

        // check if we do support refreshs
        if (command == RefreshType.REFRESH) {
            if (!localsendingType.getSupportsRefresh()) {
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

        try {
            Configuration channelConfig = channel.getConfiguration();
            sendMessage(channelId, channelTypeId, command, channelConfig);
        } catch (IllegalArgumentException e) {
            logger.warn("Exception while sending telegram!", e);
        }
    }

    @Override
    public void handleRemoval() {
        EnOceanBridgeHandler bridgeHandler = getBridgeHandler();
        if (bridgeHandler != null) {
            Integer senderOffset = getConfiguration().senderIdOffset;
            if (senderOffset != null && senderOffset > 0) {
                bridgeHandler.removeSender(senderOffset);
            }

            if (bridgeHandler.isSmackClient(this.thing)) {
                logger.warn("Removing smack client (ThingId: {}) without teach out!", this.thing.getUID().getId());
            }
        }

        super.handleRemoval();
    }

    @Override
    public void dispose() {
        ScheduledFuture<?> refreshJob = this.refreshJob;
        if (refreshJob != null) {
            refreshJob.cancel(true);
            this.refreshJob = null;
        }
    }
}
