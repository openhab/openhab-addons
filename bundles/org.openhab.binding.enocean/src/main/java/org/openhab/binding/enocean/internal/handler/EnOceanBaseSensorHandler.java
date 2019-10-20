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

import static java.util.Collections.unmodifiableCollection;
import static org.openhab.binding.enocean.internal.EnOceanBindingConstants.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.function.Predicate;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.link.ItemChannelLinkRegistry;
import org.eclipse.smarthome.core.thing.type.ChannelKind;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.eclipse.smarthome.core.util.HexUtils;
import org.openhab.binding.enocean.internal.config.EnOceanBaseConfig;
import org.openhab.binding.enocean.internal.eep.EEP;
import org.openhab.binding.enocean.internal.eep.EEPFactory;
import org.openhab.binding.enocean.internal.eep.EEPType;
import org.openhab.binding.enocean.internal.messages.ERP1Message;
import org.openhab.binding.enocean.internal.messages.ERP1Message.RORG;
import org.openhab.binding.enocean.internal.messages.ESP3Packet;
import org.openhab.binding.enocean.internal.transceiver.ESP3PacketListener;

/**
 *
 * @author Daniel Weber - Initial contribution
 *         This class defines base functionality for receiving eep messages.
 */
public class EnOceanBaseSensorHandler extends EnOceanBaseThingHandler implements ESP3PacketListener {

    // List of all thing types which support receiving of eep messages
    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES = new HashSet<>(
            Arrays.asList(THING_TYPE_ROOMOPERATINGPANEL, THING_TYPE_MECHANICALHANDLE, THING_TYPE_CONTACT,
                    THING_TYPE_TEMPERATURESENSOR, THING_TYPE_TEMPERATUREHUMIDITYSENSOR, THING_TYPE_ROCKERSWITCH,
                    THING_TYPE_OCCUPANCYSENSOR, THING_TYPE_LIGHTTEMPERATUREOCCUPANCYSENSOR, THING_TYPE_LIGHTSENSOR,
                    THING_TYPE_PUSHBUTTON, THING_TYPE_AUTOMATEDMETERSENSOR));

    protected Hashtable<RORG, EEPType> receivingEEPTypes = null;

    public EnOceanBaseSensorHandler(Thing thing, ItemChannelLinkRegistry itemChannelLinkRegistry) {
        super(thing, itemChannelLinkRegistry);
    }

    @Override
    void initializeConfig() {
        config = getConfigAs(EnOceanBaseConfig.class);
    }

    @Override
    Collection<EEPType> getEEPTypes() {
        if (receivingEEPTypes == null) {
            return Collections.emptyList();
        }

        return unmodifiableCollection(receivingEEPTypes.values());
    }

    @Override
    boolean validateConfig() {
        receivingEEPTypes = null;

        try {
            if (config.receivingEEPId != null && !config.receivingEEPId.isEmpty()) {
                receivingEEPTypes = new Hashtable<>();

                for (String receivingEEP : config.receivingEEPId) {
                    if (receivingEEP == null) {
                        continue;
                    }

                    EEPType receivingEEPType = EEPType.getType(receivingEEP);
                    if (receivingEEPTypes.containsKey(receivingEEPType.getRORG())) {
                        configurationErrorDescription = "Receiving more than one EEP of the same RORG is not supported";
                        return false;
                    }

                    receivingEEPTypes.put(receivingEEPType.getRORG(), receivingEEPType);
                }
            } else {
                receivingEEPTypes = null;
            }
        } catch (Exception e) {
            configurationErrorDescription = "Receiving EEP is not supported";
            return false;
        }

        updateChannels();

        if (receivingEEPTypes != null) {
            if (!validateEnoceanId(config.enoceanId)) {
                configurationErrorDescription = "EnOceanId is not a valid EnOceanId";
                return false;
            }

            if (!config.enoceanId.equals(EMPTYENOCEANID)) {
                getBridgeHandler().addPacketListener(this);
            }
        }

        return true;
    }

    @Override
    public long getSenderIdToListenTo() {
        return Long.parseLong(config.enoceanId, 16);
    }

    @Override
    public void handleRemoval() {

        if (getBridgeHandler() != null) {
            getBridgeHandler().removePacketListener(this);
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
            boolean result = eepType.GetSupportedChannels().containsKey(c.getUID().getId());
            return (isLinked(c.getUID()) || c.getKind() == ChannelKind.TRIGGER) && result;
        };
    }

    @Override
    public void espPacketReceived(ESP3Packet packet) {

        if (receivingEEPTypes == null) {
            return;
        }

        ERP1Message msg = (ERP1Message) packet;
        EEPType receivingEEPType = receivingEEPTypes.get(msg.getRORG());
        if (receivingEEPType == null) {
            return;
        }

        EEP eep = EEPFactory.buildEEP(receivingEEPType, (ERP1Message) packet);
        logger.debug("ESP Packet payload {} for {} received", HexUtils.bytesToHex(packet.getPayload()),
                HexUtils.bytesToHex(msg.getSenderId()));

        if (eep.isValid()) {
            byte[] senderId = msg.getSenderId();

            // try to interpret received message for all linked or trigger channels
            getThing().getChannels().stream().filter(channelFilter(receivingEEPType, senderId))
                    .sorted((c1, c2) -> c1.getKind().compareTo(c2.getKind())) // handle state channels first
                    .forEachOrdered(channel -> {

                        ChannelTypeUID channelTypeUID = channel.getChannelTypeUID();
                        String channelTypeId = (channelTypeUID != null) ? channelTypeUID.getId() : "";

                        String channelId = channel.getUID().getId();
                        Configuration channelConfig = channel.getConfiguration();

                        switch (channel.getKind()) {
                            case STATE:
                                State result = eep.convertToState(channelId, channelTypeId, channelConfig,
                                        id -> getCurrentState(id));

                                // if message can be interpreted (result != UnDefType.UNDEF) => update item state
                                if (result != null && result != UnDefType.UNDEF) {
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
        }
    }
}
