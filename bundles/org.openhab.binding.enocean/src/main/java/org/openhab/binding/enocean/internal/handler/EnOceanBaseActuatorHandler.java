/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
import org.openhab.binding.enocean.internal.eep.StateMachineProvider;
import org.openhab.binding.enocean.internal.messages.BasePacket;
import org.openhab.binding.enocean.internal.statemachine.STMStateMachine;
import org.openhab.binding.enocean.internal.statemachine.STMTransitionConfiguration;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.link.ItemChannelLinkRegistry;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.util.HexUtils;

/**
 *
 * @author Daniel Weber - Initial contribution
 * @author Sven Schad - added state machine for blinds/rollershutter
 *         This class defines base functionality for sending eep messages. This class extends EnOceanBaseSensorHandler
 *         class as most actuator things send status or response messages, too.
 *
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

            // Check if EEP implements StateMachineProvider interface for state machine support
            EEP eep = EEPFactory.createEEP(localEEPType);
            if (eep instanceof StateMachineProvider<?, ?> stmEEP) {
                Thing thing = getThing();
                STMTransitionConfiguration<?, ?> stmConfig = stmEEP.getTransitionConfiguration(thing);
                if (stmConfig != null) {
                    @SuppressWarnings({ "unchecked", "rawtypes" })
                    STMStateMachine<?, ?> builtStm = STMStateMachine.build((STMTransitionConfiguration) stmConfig,
                            stmEEP.getInitialState(), scheduler, this::onStateChanged);
                    stm = builtStm;
                    if (stm != null) {
                        @SuppressWarnings({ "unchecked", "rawtypes" })
                        final STMStateMachine rawStm = stm;
                        stmEEP.getRequiredCallbackActions(thing)
                                .forEach(a -> rawStm.register(a, this::processStoredCommand));
                        restoreStateMachineState();
                    }
                }

                // Remove channels based on configuration mode
                Set<String> channelsToRemove = stmEEP.getChannelsToRemove(thing);
                if (!channelsToRemove.isEmpty()) {
                    ThingBuilder thingBuilder = editThing();
                    for (String channelId : channelsToRemove) {
                        Channel channel = thing.getChannel(channelId);
                        if (channel != null) {
                            thingBuilder.withoutChannel(channel.getUID());
                        }
                    }
                    updateThing(thingBuilder.build());
                }
                logger.debug("STM initialized via StateMachineProvider interface");
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
        // TODO: separate generic actuators from generic sensors?
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
        // sendMessage(VIRTUALCHANNEL_SEND_COMMAND, VIRTUALCHANNEL_SEND_COMMAND, OnOffType.ON, null);
        sendMessage(new ChannelUID(thing.getUID(), VIRTUALCHANNEL_SEND_COMMAND), OnOffType.ON);
    }

    protected void sendMessage(ChannelUID channelUID, Command command) {

        EEPType sendType = sendingEEPType;
        if (sendType == null) {
            logger.warn("cannot send a message with an empty EEPType");
            return;
        }
        EEP eep = EEPFactory.createEEP(sendType);

        if (eep.convertFromCommand(thing, channelUID, command, id -> getCurrentState(id), stm).hasData()) {
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
            sendMessage(channelUID, command);
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

    /**
     * Callback invoked when the state machine changes state.
     * Updates the state channel and persists the state to Thing properties.
     *
     * @param newState the new state
     */
    private void onStateChanged(Enum<?> newState) {
        updateState(CHANNEL_STATEMACHINESTATE, new StringType(newState.name()));
        updateProperty("lastSTMState", newState.name());
        logger.debug("STM state changed to {}, persisted to Thing properties", newState);
    }

    /**
     * Restores the state machine state from Thing properties after restart.
     * Note: State restoration requires the state machine to be initialized first.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void restoreStateMachineState() {
        String lastState = getThing().getProperties().get("lastSTMState");
        if (lastState != null && stm != null) {
            try {
                // Get the state enum class from the state machine's current state
                Enum<?> currentState = stm.getState();
                Class<? extends Enum> stateClass = currentState.getDeclaringClass();
                Enum<?> restoredState = Enum.valueOf(stateClass, lastState);
                ((STMStateMachine) stm).restoreState(restoredState);
                logger.debug("Restored STM state from properties: {}", restoredState);
            } catch (IllegalArgumentException e) {
                logger.debug("Could not restore STM state '{}', using initial state", lastState);
            }
        }
    }

    /**
     * Processes a stored command after a state transition.
     * This is called via callback when calibration or positioning completes.
     */
    private void processStoredCommand() {
        STMStateMachine<?, ?> stateMachine = stm;
        if (stateMachine == null) {
            return;
        }

        String channel = stateMachine.getStoredChannel();
        Command command = stateMachine.getStoredCommand();

        if (channel != null && command != null) {
            logger.debug("Processing stored command {} for channel {}", command, channel);
            stateMachine.clearStoredCommand();

            // Schedule the command processing with a short delay
            stateMachine.scheduleDelayed(() -> {
                Channel ch = getThing().getChannel(channel);
                if (ch != null) {
                    handleCommand(ch.getUID(), command);
                }
            }, 100);
        }
    }
}
