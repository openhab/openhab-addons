/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.enocean.handler;

import static org.openhab.binding.enocean.EnOceanBindingConstants.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
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
    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES = new HashSet<ThingTypeUID>(
            Arrays.asList(THING_TYPE_ROOMOPERATINGPANEL, THING_TYPE_MECHANICALHANDLE, THING_TYPE_CONTACT,
                    THING_TYPE_TEMPERATURESENSOR, THING_TYPE_HUMIDITYTEMPERATURESENSOR, THING_TYPE_ROCKERSWITCH,
                    THING_TYPE_LIGHTTEMPERATUREOCCUPANCYSENSOR, THING_TYPE_PUSHBUTTON));

    protected Hashtable<RORG, EEPType> receivingEEPTypes = null;
    protected String enoceanId; // enoceanId to listen to. This is the thingId

    public EnOceanBaseSensorHandler(Thing thing) {
        super(thing);
    }

    protected void setReceivingEEP(EnOceanBaseConfig cfg) {
        Configuration c = getConfig();
        Object r = c.get(PARAMETER_RECEIVINGEEPID);
        if (r != null && r instanceof String) {
            cfg.setReceivingEEPId(r.toString());
        }
        if (r != null && r instanceof ArrayList<?>) {
            cfg.setReceivingEEPId((ArrayList<String>) r);
        }
    }

    @Override
    boolean validateConfig() {
        receivingEEPTypes = new Hashtable<>();
        EnOceanBaseConfig cfg = getConfigAs(EnOceanBaseConfig.class);
        setReceivingEEP(cfg);

        try {
            if (cfg.getReceivingEEPId() != null && !cfg.getReceivingEEPId().isEmpty()) {

                boolean first = true;
                for (String receivingEEP : cfg.getReceivingEEPId()) {
                    if (receivingEEP == null) {
                        continue;
                    }

                    EEPType receivingEEPType = EEPType.getType(receivingEEP);
                    if (receivingEEPTypes.containsKey(receivingEEPType.getRORG())) {
                        configurationErrorDescription = "Receiving more than one EEP of the same RORG is not dupported";
                        return false;
                    }

                    receivingEEPTypes.put(receivingEEPType.getRORG(), receivingEEPType);
                    updateChannels(receivingEEPType, first);
                    first = false;
                }
            } else {
                receivingEEPTypes = null;
            }
        } catch (Exception e) {
            configurationErrorDescription = "Receiving EEP is not supported";
            return false;
        }

        enoceanId = thing.getUID().getId();
        if (validateEnoceanId(enoceanId)) {
            if (receivingEEPTypes != null) {
                getBridgeHandler().addPacketListener(this);
            }
            return true;
        }

        configurationErrorDescription = "Thing Id is not a valid Enocean Id";
        return false;
    }

    @Override
    public long getSenderIdToListenTo() {
        return Long.parseLong(enoceanId, 16);
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

    @Override
    public void espPacketReceived(ESP3Packet packet) {

        if (receivingEEPTypes == null) {
            return;
        }

        try {

            ERP1Message msg = (ERP1Message) packet;
            EEPType receivingEEPType = receivingEEPTypes.get(msg.getRORG());
            if (receivingEEPType == null) {
                return;
            }

            EEP eep = EEPFactory.buildEEP(receivingEEPType, (ERP1Message) packet);
            logger.debug("ESP Packet {} for {} received", HexUtils.bytesToHex(packet.getPayload()),
                    this.getThing().getUID().getId());

            if (eep.isValid()) {

                // try to interpret received message for all linked channels
                receivingEEPType.GetChannelIds().stream().filter(id -> getLinkedChannels().containsKey(id))
                        .forEach(id -> {
                            if (id == null) {
                                return;
                            }

                            Channel channel = getLinkedChannels().get(id);
                            Configuration config = channel.getConfiguration();
                            switch (channel.getKind()) {
                                case STATE:
                                    State currentState = channelState.get(id);
                                    State result = eep.convertToState(id, config, currentState);

                                    // if message can be interpreted (result != UnDefType.UNDEF) => update item state
                                    if (result != null && result != UnDefType.UNDEF) {
                                        updateState(id, result);
                                        channelState.put(id, result); // update internal state map
                                    }
                                    break;
                                case TRIGGER:
                                    String lastEvent = lastEvents.get(id);
                                    String event = eep.convertToEvent(id, lastEvent, config);
                                    if (event != null) {
                                        triggerChannel(channel.getUID(), event);
                                        lastEvents.put(id, event);
                                    }
                                    break;
                            }
                        });
            }
        } catch (Exception e) {
            logger.debug(e.getMessage());
        }
    }
}
