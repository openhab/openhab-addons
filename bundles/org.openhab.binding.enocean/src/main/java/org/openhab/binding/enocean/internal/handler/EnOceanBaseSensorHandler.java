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
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.enocean.internal.config.EnOceanBaseConfig;
import org.openhab.binding.enocean.internal.eep.EEP;
import org.openhab.binding.enocean.internal.eep.EEPFactory;
import org.openhab.binding.enocean.internal.eep.EEPType;
import org.openhab.binding.enocean.internal.messages.BasePacket;
import org.openhab.binding.enocean.internal.messages.ERP1Message;
import org.openhab.binding.enocean.internal.messages.ERP1Message.RORG;
import org.openhab.binding.enocean.internal.transceiver.PacketListener;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.link.ItemChannelLinkRegistry;
import org.openhab.core.thing.type.ChannelKind;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.openhab.core.util.HexUtils;

/**
 * @author Daniel Weber - Initial contribution
 *         This class defines base functionality for receiving eep messages.
 */
@NonNullByDefault
public class EnOceanBaseSensorHandler extends EnOceanBaseThingHandler implements PacketListener {

    // List of all thing types which support receiving of eep messages
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_ROOMOPERATINGPANEL,
            THING_TYPE_MECHANICALHANDLE, THING_TYPE_CONTACT, THING_TYPE_TEMPERATURESENSOR,
            THING_TYPE_TEMPERATUREHUMIDITYSENSOR, THING_TYPE_GASSENSOR, THING_TYPE_ROCKERSWITCH,
            THING_TYPE_OCCUPANCYSENSOR, THING_TYPE_LIGHTTEMPERATUREOCCUPANCYSENSOR, THING_TYPE_LIGHTSENSOR,
            THING_TYPE_PUSHBUTTON, THING_TYPE_AUTOMATEDMETERSENSOR, THING_TYPE_ENVIRONMENTALSENSOR,
            THING_TYPE_MULTFUNCTIONSMOKEDETECTOR, THING_TYPE_WINDOWSASHHANDLESENSOR);

    protected final Hashtable<RORG, EEPType> receivingEEPTypes = new Hashtable<>();

    protected @Nullable ScheduledFuture<?> responseFuture = null;

    public EnOceanBaseSensorHandler(Thing thing, ItemChannelLinkRegistry itemChannelLinkRegistry) {
        super(thing, itemChannelLinkRegistry);
    }

    @Override
    void initializeConfig() {
        config = getConfigAs(EnOceanBaseConfig.class);
    }

    @Override
    @Nullable
    Collection<EEPType> getEEPTypes() {
        return Collections.unmodifiableCollection(receivingEEPTypes.values());
    }

    @Override
    boolean validateConfig() {
        receivingEEPTypes.clear();
        try {
            config.receivingEEPId.forEach(receivingEEP -> {
                EEPType receivingEEPType = EEPType.getType(receivingEEP);
                EEPType existingKey = receivingEEPTypes.putIfAbsent(receivingEEPType.getRORG(), receivingEEPType);
                if (existingKey != null) {
                    throw new IllegalArgumentException("Receiving more than one EEP of the same RORG is not supported");
                }
            });
            if (config.receivingSIGEEP) {
                receivingEEPTypes.put(EEPType.SigBatteryStatus.getRORG(), EEPType.SigBatteryStatus);
            }
        } catch (IllegalArgumentException e) {
            String eMessage = e.getMessage();
            configurationErrorDescription = eMessage != null ? eMessage
                    : "IllegalArgumentException without a message was thrown";
            return false;
        }

        updateChannels();

        if (!validateEnoceanId(config.enoceanId)) {
            configurationErrorDescription = "EnOceanId is not a valid EnOceanId";
            return false;
        }

        if (!config.enoceanId.equals(EMPTYENOCEANID)) {
            EnOceanBridgeHandler handler = getBridgeHandler();
            if (handler != null) {
                handler.addPacketListener(this);
            }
        }

        return true;
    }

    @Override
    public long getEnOceanIdToListenTo() {
        return Long.parseLong(config.enoceanId, 16);
    }

    @Override
    public void handleRemoval() {
        EnOceanBridgeHandler handler = getBridgeHandler();
        if (handler != null) {
            handler.removePacketListener(this);
        }
        super.handleRemoval();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // sensor things cannot send any messages, hence they are not allowed to handle any command
        // The only possible command would be "Refresh"
    }

    protected Predicate<Channel> channelFilter(EEPType eepType, byte[] senderId) {
        return c -> {

            boolean result = eepType.isChannelSupported(c);
            return (isLinked(c.getUID()) || c.getKind() == ChannelKind.TRIGGER) && result;
        };
    }

    protected void sendRequestResponse() {
        throw new UnsupportedOperationException("Sensor cannot send responses");
    }

    @Override
    public void packetReceived(BasePacket packet) {
        ERP1Message msg = (ERP1Message) packet;

        EEPType localReceivingType = receivingEEPTypes.get(msg.getRORG());
        if (localReceivingType == null) {
            return;
        }

        EEP eep = EEPFactory.buildEEP(localReceivingType, (ERP1Message) packet);
        logger.debug("ESP Packet payload {} for {} received", HexUtils.bytesToHex(packet.getPayload()),
                HexUtils.bytesToHex(msg.getSenderId()));

        if (eep.isValid()) {
            byte[] senderId = msg.getSenderId();

            // try to interpret received message for all linked or trigger channels
            getThing().getChannels().stream().filter(channelFilter(localReceivingType, senderId))
                    .sorted(Comparator.comparing(Channel::getKind)) // handle state channels first
                    .forEachOrdered(channel -> {

                        ChannelTypeUID channelTypeUID = channel.getChannelTypeUID();
                        String channelTypeId = (channelTypeUID != null) ? channelTypeUID.getId() : "";

                        String channelId = channel.getUID().getId();
                        Configuration channelConfig = channel.getConfiguration();

                        switch (channel.getKind()) {
                            case STATE:
                                State result = eep.convertToState(channelId, channelTypeId, channelConfig,
                                        this::getCurrentState);

                                // if message can be interpreted (result != UnDefType.UNDEF) => update item state
                                if (result != UnDefType.UNDEF) {
                                    updateState(channelId, result);
                                }
                                break;
                            case TRIGGER:
                                String lastEvent = lastEvents.get(channelId);
                                String event = eep.convertToEvent(channelId, channelTypeId, lastEvent, channelConfig);
                                if (event != null) {
                                    triggerChannel(channel.getUID(), event);
                                    lastEvents.put(channelId, event);
                                }
                                break;
                        }
                    });

            if (localReceivingType.getRequstesResponse()) {
                // fire trigger for receive
                triggerChannel(prepareAnswer, "requestAnswer");
                // Send response after 100ms
                ScheduledFuture<?> responseFuture = this.responseFuture;
                if (responseFuture == null || responseFuture.isDone()) {
                    this.responseFuture = scheduler.schedule(this::sendRequestResponse, 100, TimeUnit.MILLISECONDS);
                }
            }
        }
    }
}
