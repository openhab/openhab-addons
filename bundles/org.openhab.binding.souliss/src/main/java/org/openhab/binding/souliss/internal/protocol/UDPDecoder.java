/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.souliss.internal.protocol;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.souliss.internal.SoulissBindingConstants;
import org.openhab.binding.souliss.internal.SoulissProtocolConstants;
import org.openhab.binding.souliss.internal.SoulissUDPConstants;
import org.openhab.binding.souliss.internal.discovery.DiscoverResult;
import org.openhab.binding.souliss.internal.handler.SoulissGatewayHandler;
import org.openhab.binding.souliss.internal.handler.SoulissGenericHandler;
import org.openhab.binding.souliss.internal.handler.SoulissT11Handler;
import org.openhab.binding.souliss.internal.handler.SoulissT12Handler;
import org.openhab.binding.souliss.internal.handler.SoulissT13Handler;
import org.openhab.binding.souliss.internal.handler.SoulissT14Handler;
import org.openhab.binding.souliss.internal.handler.SoulissT16Handler;
import org.openhab.binding.souliss.internal.handler.SoulissT18Handler;
import org.openhab.binding.souliss.internal.handler.SoulissT19Handler;
import org.openhab.binding.souliss.internal.handler.SoulissT1AHandler;
import org.openhab.binding.souliss.internal.handler.SoulissT22Handler;
import org.openhab.binding.souliss.internal.handler.SoulissT31Handler;
import org.openhab.binding.souliss.internal.handler.SoulissT41Handler;
import org.openhab.binding.souliss.internal.handler.SoulissT42Handler;
import org.openhab.binding.souliss.internal.handler.SoulissT5nHandler;
import org.openhab.binding.souliss.internal.handler.SoulissT61Handler;
import org.openhab.binding.souliss.internal.handler.SoulissT62Handler;
import org.openhab.binding.souliss.internal.handler.SoulissT63Handler;
import org.openhab.binding.souliss.internal.handler.SoulissT64Handler;
import org.openhab.binding.souliss.internal.handler.SoulissT65Handler;
import org.openhab.binding.souliss.internal.handler.SoulissT66Handler;
import org.openhab.binding.souliss.internal.handler.SoulissT67Handler;
import org.openhab.binding.souliss.internal.handler.SoulissT68Handler;
import org.openhab.binding.souliss.internal.handler.SoulissTopicsHandler;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.ThingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class decodes incoming Souliss packets, starting from decodevNet
 *
 * @author Tonino Fazio - Initial contribution
 * @author Luca Calcaterra - Refactor for OH3
 * @author Alessandro Del Pex - Souliss App
 */
@NonNullByDefault
public class UDPDecoder {

    private final Logger logger = LoggerFactory.getLogger(UDPDecoder.class);
    private @Nullable DiscoverResult discoverResult;
    private @Nullable SoulissGatewayHandler gwHandler;

    private @Nullable Byte lastByteGatewayIp = null;

    public UDPDecoder(Bridge bridge, @Nullable DiscoverResult pDiscoverResult) {
        this.gwHandler = (SoulissGatewayHandler) bridge.getHandler();
        this.discoverResult = pDiscoverResult;
        var localGwHandler = this.gwHandler;
        if (localGwHandler != null) {
            this.lastByteGatewayIp = (byte) Integer
                    .parseInt(localGwHandler.getGwConfig().gatewayLanAddress.split("\\.")[3]);
        }
    }

    /**
     * Get packet from VNET Frame
     *
     * @param packet
     *            incoming datagram
     */
    public void decodeVNetDatagram(DatagramPacket packet) {
        int checklen = packet.getLength();
        ArrayList<Byte> mac = new ArrayList<>();
        for (var ig = 7; ig < checklen; ig++) {
            mac.add((byte) (packet.getData()[ig] & 0xFF));
        }

        // Check if decoded Gw equal to ip of bridge handler or 1 (action messages)
        Byte gwCheck = (byte) (packet.getData()[5] & 0xFF);
        if ((gwCheck == 1) || (gwCheck.equals(this.lastByteGatewayIp))) {
            decodeMacaco((byte) (packet.getData()[5] & 0xFF), mac);
        }
    }

    /**
     * Decodes lower level MaCaCo packet
     *
     * @param lastByteGatewayIP
     *
     * @param macacoPck
     */
    private void decodeMacaco(byte lastByteGatewayIP, ArrayList<Byte> macacoPck) {
        int functionalCode = macacoPck.get(0);
        switch (functionalCode) {
            case SoulissUDPConstants.SOULISS_UDP_FUNCTION_PING_RESP:
                logger.debug("Received functional code: 0x{}- Ping answer", Integer.toHexString(functionalCode));
                decodePing(macacoPck);
                break;

            case SoulissUDPConstants.SOULISS_UDP_FUNCTION_DISCOVER_GW_NODE_BCAST_RESP:
                logger.debug("Received functional code: 0x{} - Discover a gateway node answer (broadcast)",
                        Integer.toHexString(functionalCode));
                try {
                    decodePingBroadcast(macacoPck);
                } catch (UnknownHostException e) {
                    logger.warn("Error: {}", e.getLocalizedMessage());
                }
                break;

            case SoulissUDPConstants.SOULISS_UDP_FUNCTION_POLL_RESP:
                logger.debug("Received functional code: 0x{} - subscribe response",
                        Integer.toHexString(functionalCode));
                decodeStateRequest(macacoPck);
                break;
            case SoulissUDPConstants.SOULISS_UDP_FUNCTION_SUBSCRIBE_RESP:
                logger.debug("Received functional code: 0x{} - Read state answer", Integer.toHexString(functionalCode));
                decodeStateRequest(macacoPck);
                break;

            // Answer for assigned typical logic
            case SoulissUDPConstants.SOULISS_UDP_FUNCTION_TYP_RESP:
                logger.debug("Received functional code: 0x{}- Read typical logic answer",
                        Integer.toHexString(functionalCode));
                decodeTypRequest(lastByteGatewayIP, macacoPck);
                break;
            // Answer
            case SoulissUDPConstants.SOULISS_UDP_FUNCTION_HEALTHY_RESP:
                // nodes healthy
                logger.debug("Received functional code: 0x{} - Nodes Healthy", Integer.toHexString(functionalCode));
                decodeHealthyRequest(macacoPck);
                break;

            case (byte) SoulissUDPConstants.SOULISS_UDP_FUNCTION_DBSTRUCT_RESP:
                logger.debug("Received functional code: 0x{} - Database structure answer",
                        Integer.toHexString(functionalCode));
                decodeDBStructRequest(macacoPck);
                break;
            case 0x83:
                logger.debug("Functional code not supported");
                break;
            case 0x84:
                logger.debug("Data out of range");
                break;
            case 0x85:
                logger.debug("Subscription refused");
                break;
            case (byte) SoulissUDPConstants.SOULISS_UDP_FUNCTION_ACTION_MESSAGE:
                logger.debug("Received functional code: 0x{} - Action Message (Topic)",
                        Integer.toHexString(functionalCode));
                decodeActionMessages(macacoPck);
                break;
            default:
                logger.debug("Received functional code: 0x{} - unused by OH Binding",
                        Integer.toHexString(functionalCode));
        }
    }

    /**
     * @param mac
     */
    private void decodePing(ArrayList<Byte> mac) {
        // not used
        int putIn1 = mac.get(1);
        // not used
        int putIn2 = mac.get(2);
        logger.debug("decodePing: putIn code: {}, {}", putIn1, putIn2);
        var localGwHandler = this.gwHandler;
        if (localGwHandler != null) {
            localGwHandler.gatewayDetected();
        }
    }

    private void decodePingBroadcast(ArrayList<Byte> macaco) throws UnknownHostException {
        String ip = macaco.get(5) + "." + macaco.get(6) + "." + macaco.get(7) + "." + macaco.get(8);
        byte[] addr = { (macaco.get(5)).byteValue(), (macaco.get(6)).byteValue(), (macaco.get(7)).byteValue(),
                (macaco.get(8)).byteValue() };
        logger.debug("decodePingBroadcast. Gateway Discovery. IP: {}", ip);

        var localDiscoverResult = this.discoverResult;
        if (localDiscoverResult != null) {
            localDiscoverResult.gatewayDetected(InetAddress.getByAddress(addr), macaco.get(8).toString());
        } else {
            logger.debug("decodePingBroadcast aborted. 'discoverResult' is null");
        }
    }

    /**
     * decode Typicals Request Packet
     * It read Souliss Network and create OH items
     *
     * @param lastByteGatewayIP
     *
     * @param mac
     */
    private void decodeTypRequest(byte lastByteGatewayIP, ArrayList<Byte> mac) {
        var localGwHandler = this.gwHandler;
        if (localGwHandler != null) {
            int typXnodo = localGwHandler.getMaxTypicalXnode();

            byte tgtnode = mac.get(3);
            int numberOf = mac.get(4);

            // creates Souliss nodes
            for (var j = 0; j < numberOf; j++) {
                // create only not-empty typicals
                if ((mac.get(5 + j) != 0) && (mac.get(5 + j) != SoulissProtocolConstants.SOULISS_T_RELATED)) {
                    byte typical = mac.get(5 + j);
                    byte slot = (byte) (j % typXnodo);
                    byte node = (byte) (j / typXnodo + tgtnode);
                    logger.debug("Thing Detected. IP (last byte): {}, Typical: 0x{}, Node: {}, Slot: {} ",
                            lastByteGatewayIP, Integer.toHexString(typical), node, slot);

                    var localDiscoverResult = this.discoverResult;
                    if (localDiscoverResult != null) {
                        localDiscoverResult.thingDetectedTypicals(lastByteGatewayIP, typical, node, slot);
                    } else {
                        logger.debug("decodeTypRequest aborted. 'discoverResult' is null");
                    }
                }
            }
        }
    }

    /**
     * decode Typicals Request Packet
     * It read Action Messages on Souliss Network and create items
     *
     * @param lastByteGatewayIP
     *
     * @param mac
     */
    private void decodeActionMessages(ArrayList<Byte> mac) {
        String sTopicNumber;
        String sTopicVariant;
        float fRet = 0;

        try {
            // A 16-bit Topic Number: Define the topic itself
            // A 8-bit Topic Variant : Define a variant for the topic

            String[] sTopicNumberArray = { Integer.toHexString(mac.get(2)).toUpperCase(),
                    Integer.toHexString(mac.get(1)).toUpperCase() };
            if (sTopicNumberArray[0].length() == 1) {
                sTopicNumberArray[0] = "0" + sTopicNumberArray[0];
            }
            if (sTopicNumberArray[1].length() == 1) {
                sTopicNumberArray[1] = "0" + sTopicNumberArray[1];
            }
            sTopicNumber = sTopicNumberArray[0] + sTopicNumberArray[1];
            logger.debug("Topic Number: 0x{}", sTopicNumber);

            sTopicVariant = Integer.toHexString(mac.get(3)).toUpperCase();
            if (sTopicVariant.length() == 1) {
                sTopicVariant = "0" + sTopicVariant;
            }
            logger.debug("Topic Variant: 0x{}", sTopicVariant);
            if (mac.get(4) == 1) {
                fRet = mac.get(5);
                logger.debug("Topic Value (Payload one byte): {} ", Integer.toHexString(mac.get(5)).toUpperCase());
            } else if (mac.get(4) == 2) {
                byte[] value = { mac.get(5), mac.get(6) };

                int shifted = value[1] << 8;
                fRet = HalfFloatUtils.toFloat(shifted + value[0]);
                logger.debug("Topic Value (Payload 2 bytes): {}", fRet);
            }
            var localGwHandler = this.gwHandler;
            if (localGwHandler != null) {
                var listThings = localGwHandler.getThing().getThings();

                Boolean bIsPresent = false;

                for (Thing t : listThings) {
                    if (t.getUID().toString().split(":")[2]
                            .equals(sTopicNumber + SoulissBindingConstants.UUID_NODE_SLOT_SEPARATOR + sTopicVariant)) {
                        var topicHandler = (SoulissTopicsHandler) (t.getHandler());
                        if (topicHandler != null) {
                            topicHandler.setState(DecimalType.valueOf(Float.toString(fRet)));
                            bIsPresent = true;
                        }
                    }
                }
                var localDiscoverResult = this.discoverResult;
                if (localDiscoverResult != null && !bIsPresent) {
                    localDiscoverResult.thingDetectedActionMessages(sTopicNumber, sTopicVariant);
                }
            }
        } catch (Exception uy) {
            logger.warn("decodeActionMessages ERROR");
        }
    }

    /**
     * decode DB Struct Request Packet
     * It return Souliss Network:
     * node number
     * max supported number of nodes
     * max typical per node
     * max requests
     * See Souliss wiki for details
     *
     * @param lastByteGatewayIP
     *
     * @param mac
     */
    private void decodeDBStructRequest(ArrayList<Byte> mac) {
        int nodes = mac.get(5);
        int maxTypicalXnode = mac.get(7);

        SoulissGatewayHandler localGwHandler = this.gwHandler;
        if (localGwHandler != null) {
            localGwHandler.setNodes(nodes);
            localGwHandler.setMaxTypicalXnode(maxTypicalXnode);
            localGwHandler.dbStructAnswerReceived();
        }
    }

    /**
     * Decodes a souliss nodes health request
     *
     * @param macaco
     *            packet
     */
    private void decodeHealthyRequest(ArrayList<Byte> mac) {
        int numberOf = mac.get(4);

        for (var i = 5; i < 5 + numberOf; i++) {
            var localGwHandler = this.gwHandler;
            if (localGwHandler != null) {
                // build an array containing healths
                List<Thing> listaThings = localGwHandler.getThing().getThings();

                ThingHandler handler = null;
                for (Thing thing : listaThings) {
                    if (thing.getThingTypeUID().equals(SoulissBindingConstants.TOPICS_THING_TYPE)) {
                        continue;
                    }
                    handler = thing.getHandler();
                    if (handler != null) {
                        int tgtnode = i - 5;
                        if (((SoulissGenericHandler) handler).getNode() == tgtnode) {
                            ((SoulissGenericHandler) handler).setHealthy((mac.get(i)));
                        }
                    } else {
                        logger.debug("decode Healthy Request Warning. Thing handler is null");
                    }
                }
            }
        }
    }

    private void decodeStateRequest(ArrayList<Byte> mac) {
        int tgtnode = mac.get(3);

        Iterator<Thing> thingsIterator = null;
        var localGwHandler = this.gwHandler;
        if (localGwHandler != null) {
            thingsIterator = localGwHandler.getThing().getThings().iterator();

            var bFound = false;
            Thing typ = null;
            while (thingsIterator.hasNext() && !bFound) {
                typ = thingsIterator.next();
                // if a topic continue
                // ignoring it
                if (typ.getThingTypeUID().equals(SoulissBindingConstants.TOPICS_THING_TYPE)) {
                    continue;
                }
                String[] sUIDArray = typ.getUID().getAsString().split(":");
                ThingHandler handler = typ.getHandler();
                // execute it only if binding is Souliss and update is for my
                // Gateway
                if (handler != null) {
                    // execute it
                    // only
                    // if it is
                    // node
                    // to update
                    if (((SoulissGenericHandler) handler).getNode() == tgtnode) {
                        // ...now check slot
                        int slot = ((SoulissGenericHandler) handler).getSlot();
                        // get typical value
                        var sVal = getByteAtSlot(mac, slot);
                        var decodingLiteralLabel = "Decoding {}{}";
                        var packetLabel = " packet";
                        // update Txx
                        switch (sUIDArray[1]) {
                            case SoulissBindingConstants.T11:
                                logger.debug(decodingLiteralLabel, SoulissBindingConstants.T11, packetLabel);
                                ((SoulissT11Handler) handler).setRawState(sVal);
                                break;
                            case SoulissBindingConstants.T12:
                                logger.debug(decodingLiteralLabel, SoulissBindingConstants.T12, packetLabel);
                                ((SoulissT12Handler) handler).setRawState(sVal);
                                break;
                            case SoulissBindingConstants.T13:
                                logger.debug(decodingLiteralLabel, SoulissBindingConstants.T13, packetLabel);
                                ((SoulissT13Handler) handler).setRawState(sVal);
                                break;
                            case SoulissBindingConstants.T14:
                                logger.debug(decodingLiteralLabel, SoulissBindingConstants.T14, packetLabel);
                                ((SoulissT14Handler) handler).setRawState(sVal);
                                break;
                            case SoulissBindingConstants.T16:
                                logger.debug(decodingLiteralLabel, SoulissBindingConstants.T16, packetLabel);
                                ((SoulissT16Handler) handler).setRawStateCommand(sVal);
                                ((SoulissT16Handler) handler).setRawStateRgb(getByteAtSlot(mac, slot + 1),
                                        getByteAtSlot(mac, slot + 2), getByteAtSlot(mac, slot + 3));
                                break;

                            case SoulissBindingConstants.T18:
                                logger.debug(decodingLiteralLabel, SoulissBindingConstants.T18, packetLabel);
                                ((SoulissT18Handler) handler).setRawState(sVal);
                                break;

                            case SoulissBindingConstants.T19:
                                logger.debug(decodingLiteralLabel, SoulissBindingConstants.T19, packetLabel);
                                ((SoulissT19Handler) handler).setRawState(sVal);
                                ((SoulissT19Handler) handler).setRawStateDimmerValue(getByteAtSlot(mac, slot + 1));
                                break;

                            case SoulissBindingConstants.T1A:
                                logger.debug(decodingLiteralLabel, SoulissBindingConstants.T1A, packetLabel);
                                ((SoulissT1AHandler) handler).setRawState(sVal);
                                break;
                            case SoulissBindingConstants.T21:
                            case SoulissBindingConstants.T22:
                                logger.debug(decodingLiteralLabel,
                                        SoulissBindingConstants.T21 + "/" + SoulissBindingConstants.T22, packetLabel);
                                ((SoulissT22Handler) handler).setRawState(sVal);
                                break;
                            case SoulissBindingConstants.T31:
                                logger.debug("Decoding {}/{}", SoulissBindingConstants.T31,
                                        SoulissBindingConstants.T31);
                                logger.debug("packet: ");
                                logger.debug("- bit0 (system on-off): {}", getBitState(sVal, 0));
                                logger.debug("- bit1 (heating on-off): {}", getBitState(sVal, 1));
                                logger.debug("- bit2 (cooling on-off): {}", getBitState(sVal, 2));
                                logger.debug("- bit3 (fan1 on-off): {}", getBitState(sVal, 3));
                                logger.debug("- bit4 (fan2 on-off): {}", getBitState(sVal, 4));
                                logger.debug("- bit5 (fan3 on-off): {}", getBitState(sVal, 5));
                                logger.debug("- bit6 (Manual/automatic fan mode): {}", getBitState(sVal, 6));
                                logger.debug("- bit7 (heating/cooling mode): {}", getBitState(sVal, 7));

                                ((SoulissT31Handler) handler).setRawStateValues(sVal, getFloatAtSlot(mac, slot + 1),
                                        getFloatAtSlot(mac, slot + 3));

                                break;
                            case SoulissBindingConstants.T41:
                                ((SoulissT41Handler) handler).setRawState(sVal);
                                break;
                            case SoulissBindingConstants.T42:
                                ((SoulissT42Handler) handler).setRawState(sVal);
                                switch (sVal) {
                                    case SoulissProtocolConstants.SOULISS_T4N_NO_ANTITHEFT:
                                        ((SoulissT42Handler) handler).setState(StringType
                                                .valueOf(SoulissBindingConstants.T4N_ALARMOFF_MESSAGE_CHANNEL));
                                        break;
                                    case SoulissProtocolConstants.SOULISS_T4N_ALARM:
                                        ((SoulissT42Handler) handler).setState(StringType
                                                .valueOf(SoulissBindingConstants.T4N_ALARMON_MESSAGE_CHANNEL));
                                        break;
                                    default:
                                        break;
                                }
                                break;
                            case SoulissBindingConstants.T51:
                            case SoulissBindingConstants.T52:
                            case SoulissBindingConstants.T53:
                            case SoulissBindingConstants.T54:
                            case SoulissBindingConstants.T55:
                            case SoulissBindingConstants.T56:
                            case SoulissBindingConstants.T57:
                            case SoulissBindingConstants.T58:
                                logger.debug("Decoding T5n packet");
                                if (!Float.isNaN(getFloatAtSlot(mac, slot))) {
                                    ((SoulissT5nHandler) handler).setFloatValue(getFloatAtSlot(mac, slot));
                                }
                                break;
                            case SoulissBindingConstants.T61:
                                logger.debug(decodingLiteralLabel, SoulissBindingConstants.T61, packetLabel);
                                if (!Float.isNaN(getFloatAtSlot(mac, slot))) {
                                    ((SoulissT61Handler) handler).setFloatValue(getFloatAtSlot(mac, slot));
                                }
                                break;
                            case SoulissBindingConstants.T62:
                                logger.debug(decodingLiteralLabel, SoulissBindingConstants.T62, packetLabel);
                                if (!Float.isNaN(getFloatAtSlot(mac, slot))) {
                                    ((SoulissT62Handler) handler).setFloatValue(getFloatAtSlot(mac, slot));
                                }
                                break;
                            case SoulissBindingConstants.T63:
                                logger.debug(decodingLiteralLabel, SoulissBindingConstants.T63, packetLabel);
                                if (!Float.isNaN(getFloatAtSlot(mac, slot))) {
                                    ((SoulissT63Handler) handler).setFloatValue(getFloatAtSlot(mac, slot));
                                }
                                break;
                            case SoulissBindingConstants.T64:
                                logger.debug(decodingLiteralLabel, SoulissBindingConstants.T64, packetLabel);
                                if (!Float.isNaN(getFloatAtSlot(mac, slot))) {
                                    ((SoulissT64Handler) handler).setFloatValue(getFloatAtSlot(mac, slot));
                                }
                                break;
                            case SoulissBindingConstants.T65:
                                logger.debug(decodingLiteralLabel, SoulissBindingConstants.T65, packetLabel);
                                if (!Float.isNaN(getFloatAtSlot(mac, slot))) {
                                    ((SoulissT65Handler) handler).setFloatValue(getFloatAtSlot(mac, slot));
                                }
                                break;
                            case SoulissBindingConstants.T66:
                                logger.debug(decodingLiteralLabel, SoulissBindingConstants.T66, packetLabel);
                                if (!Float.isNaN(getFloatAtSlot(mac, slot))) {
                                    ((SoulissT66Handler) handler).setFloatValue(getFloatAtSlot(mac, slot));
                                }
                                break;
                            case SoulissBindingConstants.T67:
                                logger.debug(decodingLiteralLabel, SoulissBindingConstants.T67, packetLabel);
                                if (!Float.isNaN(getFloatAtSlot(mac, slot))) {
                                    ((SoulissT67Handler) handler).setFloatValue(getFloatAtSlot(mac, slot));
                                }
                                break;
                            case SoulissBindingConstants.T68:
                                logger.debug(decodingLiteralLabel, SoulissBindingConstants.T68, packetLabel);
                                if (!Float.isNaN(getFloatAtSlot(mac, slot))) {
                                    ((SoulissT68Handler) handler).setFloatValue(getFloatAtSlot(mac, slot));
                                }
                                break;

                            case SoulissBindingConstants.TOPICS:
                                logger.debug(decodingLiteralLabel, SoulissBindingConstants.TOPICS, packetLabel);
                                if (!Float.isNaN(getFloatAtSlot(mac, slot))) {
                                    ((SoulissTopicsHandler) handler).setFloatValue(getFloatAtSlot(mac, slot));
                                }
                                break;
                            default:
                                logger.debug("Unsupported typical");
                        }
                    }
                }
            }
        }
    }

    private byte getByteAtSlot(ArrayList<Byte> mac, int slot) {
        return mac.get(5 + slot);
    }

    private float getFloatAtSlot(ArrayList<Byte> mac, int slot) {
        int iOutput = mac.get(5 + slot) & 0xFF;
        int iOutput2 = mac.get(5 + slot + 1) & 0xFF;
        // we have two bytes, convert them...
        int shifted = iOutput2 << 8;
        return HalfFloatUtils.toFloat(shifted + iOutput);
    }

    public byte getBitState(byte vRaw, int iBit) {
        final var maskBit1 = 0x1;

        if (((vRaw >>> iBit) & maskBit1) == 0) {
            return 0;
        } else {
            return 1;
        }
    }
}
