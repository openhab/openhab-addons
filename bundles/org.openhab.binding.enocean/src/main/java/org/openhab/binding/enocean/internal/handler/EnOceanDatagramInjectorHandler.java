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
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.enocean.internal.EnOceanChannelDescription;
import org.openhab.binding.enocean.internal.config.EnOceanDatagramInjectorConfig;
import org.openhab.binding.enocean.internal.eep.EEP;
import org.openhab.binding.enocean.internal.eep.EEPFactory;
import org.openhab.binding.enocean.internal.eep.EEPType;
import org.openhab.binding.enocean.internal.injector.InjectorProfileType;
import org.openhab.binding.enocean.internal.messages.BasePacket;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.link.ItemChannelLinkRegistry;
import org.openhab.core.thing.type.ChannelKind;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.util.HexUtils;

/**
 * Send-only handler for profile-based datagram injection.
 */
@NonNullByDefault
public class EnOceanDatagramInjectorHandler extends EnOceanBaseThingHandler {

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_DATAGRAMINJECTOR);

    private byte[] senderId = new byte[0];
    private byte[] destinationId = new byte[0];

    private static final long MOTION_RETRIGGER_INTERVAL_SECONDS = 60;
    private @Nullable ScheduledFuture<?> motionRetriggerJob;

    private @Nullable InjectorProfileType sendingProfileType = null;

    public EnOceanDatagramInjectorHandler(Thing thing, ItemChannelLinkRegistry itemChannelLinkRegistry) {
        super(thing, itemChannelLinkRegistry);
    }

    @Override
    void initializeConfig() {
        config = getConfigAs(EnOceanDatagramInjectorConfig.class);
    }

    private EnOceanDatagramInjectorConfig getConfiguration() {
        return (EnOceanDatagramInjectorConfig) config;
    }

    @Override
    @Nullable
    Collection<EEPType> getEEPTypes() {
        InjectorProfileType localProfile = sendingProfileType;
        if (localProfile == null) {
            return Collections.emptyList();
        }
        return Collections.singleton(localProfile.getSendingEEPType());
    }

    @Override
    boolean validateConfig() {
        EnOceanDatagramInjectorConfig localConfig = getConfiguration();

        if (localConfig.sendingProfileId.isBlank()) {
            configurationErrorDescription = "Sending profile must be provided";
            return false;
        }

        InjectorProfileType localSendingProfileType;
        try {
            localSendingProfileType = InjectorProfileType.getType(localConfig.sendingProfileId);
            sendingProfileType = localSendingProfileType;
        } catch (IllegalArgumentException e) {
            configurationErrorDescription = "Sending profile is not supported";
            return false;
        }

        if (localSendingProfileType != InjectorProfileType.MOTION_A5_07_01) {
            stopMotionRetriggerJob();
        }

        EnOceanBridgeHandler bridgeHandler = getBridgeHandler();
        if (bridgeHandler == null) {
            configurationErrorDescription = "Bridge is not available";
            return false;
        }
        if (!bridgeHandler.isRS485Enabled()) {
            configurationErrorDescription = "Datagram injector requires RS485 mode enabled on the bridge";
            return false;
        }

        try {
            String senderAddress = localConfig.senderAddress;
            if (senderAddress == null || !validateEnoceanId(senderAddress)) {
                configurationErrorDescription = "Sender address is not valid";
                return false;
            }
            senderId = HexUtils.hexToBytes(senderAddress);
            destinationId = new byte[] { (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff };
            this.updateProperty(PROPERTY_SENDINGENOCEAN_ID, HexUtils.bytesToHex(this.senderId));
        } catch (Exception e) {
            configurationErrorDescription = "Configuration is not valid";
            return false;
        }

        ensureProfileChannels(localSendingProfileType);

        return true;
    }

    private void ensureProfileChannels(InjectorProfileType profileType) {
        String profileChannelId = profileType.getChannelId();
        String profileChannelTypeId = profileType.getChannelTypeId();
        EnOceanChannelDescription channelDescription = CHANNELID2CHANNELDESCRIPTION.get(profileChannelTypeId);
        if (channelDescription == null) {
            return;
        }

        List<Channel> channels = new LinkedList<>(getThing().getChannels());
        boolean changed = channels.removeIf(channel -> !profileChannelId.equals(channel.getUID().getId()));

        boolean currentChannelExists = channels.stream().anyMatch(channel -> {
            if (!profileChannelId.equals(channel.getUID().getId())) {
                return false;
            }
            ChannelTypeUID channelTypeUID = channel.getChannelTypeUID();
            return channelTypeUID != null && channelDescription.channelTypeUID.equals(channelTypeUID);
        });

        if (!currentChannelExists) {
            channels.removeIf(channel -> profileChannelId.equals(channel.getUID().getId()));
            Channel channel = ChannelBuilder
                    .create(new ChannelUID(getThing().getUID(), profileChannelId), channelDescription.acceptedItemType)
                    .withType(channelDescription.channelTypeUID)
                    .withKind(channelDescription.isStateChannel ? ChannelKind.STATE : ChannelKind.TRIGGER)
                    .withLabel(channelDescription.label).build();
            channels.add(channel);
            changed = true;
        }

        if (changed) {
            ThingBuilder thingBuilder = editThing();
            thingBuilder.withChannels(channels);
            updateThing(thingBuilder.build());
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command == RefreshType.REFRESH) {
            return;
        }

        InjectorProfileType localSendingProfileType = sendingProfileType;
        if (localSendingProfileType == null) {
            return;
        }

        Channel channel = getThing().getChannel(channelUID);
        if (channel == null || !isLinked(channelUID)) {
            return;
        }

        String channelId = channelUID.getId();
        ChannelTypeUID channelTypeUID = channel.getChannelTypeUID();
        String channelTypeId = (channelTypeUID != null) ? channelTypeUID.getId() : "";

        if (!localSendingProfileType.isChannelSupported(channelId, channelTypeId)) {
            return;
        }

        try {
            boolean wasSent = sendMessage(localSendingProfileType, channelId, channelTypeId, command,
                    channel.getConfiguration());
            if (wasSent && localSendingProfileType.getChannelId().equals(channelId) && command instanceof State state) {
                updateState(channelUID, state);
            }

            if (localSendingProfileType == InjectorProfileType.MOTION_A5_07_01
                    && localSendingProfileType.getChannelId().equals(channelId) && command instanceof OnOffType onOff) {
                if (onOff == OnOffType.ON) {
                    startMotionRetriggerJob(localSendingProfileType, channelId, channelTypeId,
                            channel.getConfiguration());
                } else {
                    stopMotionRetriggerJob();
                    updateState(channelUID, OnOffType.OFF);
                }
            }
        } catch (IllegalArgumentException e) {
            logger.warn("Exception while sending telegram!", e);
        }
    }

    private synchronized void startMotionRetriggerJob(InjectorProfileType profileType, String channelId,
            String channelTypeId, @Nullable Configuration channelConfig) {
        stopMotionRetriggerJob();
        motionRetriggerJob = scheduler.scheduleWithFixedDelay(() -> {
            try {
                sendMessage(profileType, channelId, channelTypeId, OnOffType.ON, channelConfig);
            } catch (Exception e) {
                logger.debug("Motion retrigger send failed", e);
            }
        }, MOTION_RETRIGGER_INTERVAL_SECONDS, MOTION_RETRIGGER_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }

    private synchronized void stopMotionRetriggerJob() {
        ScheduledFuture<?> localJob = motionRetriggerJob;
        if (localJob != null) {
            localJob.cancel(true);
            motionRetriggerJob = null;
        }
    }

    private boolean sendMessage(InjectorProfileType profileType, String channelId, String channelTypeId,
            Command command, @Nullable Configuration channelConfig) {
        EEP eep = EEPFactory.createEEP(profileType.getSendingEEPType());
        if (eep.convertFromCommand(channelId, channelTypeId, command, id -> getCurrentState(id), channelConfig)
                .hasData()) {
            BasePacket msg = eep.setSenderId(senderId).setDestinationId(destinationId)
                    .setSuppressRepeating(getConfiguration().suppressRepeating).getERP1Message();
            if (msg == null) {
                logger.warn("cannot send an empty message");
                return false;
            }
            EnOceanBridgeHandler handler = getBridgeHandler();
            if (handler != null) {
                handler.sendMessage(msg, null);
                return true;
            }
        }

        return false;
    }

    @Override
    public void dispose() {
        stopMotionRetriggerJob();
    }

    @Override
    public void handleRemoval() {
        stopMotionRetriggerJob();
        super.handleRemoval();
    }
}
