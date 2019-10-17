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
package org.openhab.binding.souliss.internal.protocol;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.souliss.SoulissBindingConstants;
import org.openhab.binding.souliss.SoulissBindingProtocolConstants;
import org.openhab.binding.souliss.SoulissBindingUDPConstants;
import org.openhab.binding.souliss.handler.SoulissGatewayHandler;
import org.openhab.binding.souliss.handler.SoulissGenericHandler;
import org.openhab.binding.souliss.handler.SoulissT11Handler;
import org.openhab.binding.souliss.handler.SoulissT12Handler;
import org.openhab.binding.souliss.handler.SoulissT13Handler;
import org.openhab.binding.souliss.handler.SoulissT14Handler;
import org.openhab.binding.souliss.handler.SoulissT16Handler;
import org.openhab.binding.souliss.handler.SoulissT18Handler;
import org.openhab.binding.souliss.handler.SoulissT19Handler;
import org.openhab.binding.souliss.handler.SoulissT1AHandler;
import org.openhab.binding.souliss.handler.SoulissT22Handler;
import org.openhab.binding.souliss.handler.SoulissT31Handler;
import org.openhab.binding.souliss.handler.SoulissT41Handler;
import org.openhab.binding.souliss.handler.SoulissT42Handler;
import org.openhab.binding.souliss.handler.SoulissT5nHandler;
import org.openhab.binding.souliss.handler.SoulissT61Handler;
import org.openhab.binding.souliss.handler.SoulissT62Handler;
import org.openhab.binding.souliss.handler.SoulissT63Handler;
import org.openhab.binding.souliss.handler.SoulissT64Handler;
import org.openhab.binding.souliss.handler.SoulissT65Handler;
import org.openhab.binding.souliss.handler.SoulissT66Handler;
import org.openhab.binding.souliss.handler.SoulissT67Handler;
import org.openhab.binding.souliss.handler.SoulissT68Handler;
import org.openhab.binding.souliss.handler.SoulissTopicsHandler;
import org.openhab.binding.souliss.internal.discovery.SoulissDiscoverJob.DiscoverResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class decodes incoming Souliss packets, starting from decodevNet
 *
 * @author Alessandro Del Pex
 * @author Tonino Fazio
 * @since 1.7.0
 */
public class SoulissBindingUDPDecoder {

    private static Logger logger = LoggerFactory.getLogger(SoulissBindingUDPDecoder.class);
    private static DiscoverResult discoverResult;

    public SoulissBindingUDPDecoder(DiscoverResult _discoverResult) {
        SoulissBindingUDPDecoder.discoverResult = _discoverResult;
    }

    /**
     * Get packet from VNET Frame
     *
     * @param packet
     *            incoming datagram
     */
    public void decodeVNetDatagram(DatagramPacket packet) {
        int checklen = packet.getLength();
        ArrayList<Byte> mac = new ArrayList<Byte>();
        for (int ig = 7; ig < checklen; ig++) {
            mac.add((byte) (packet.getData()[ig] & 0xFF));
        }
        // Last number of IP of Original Destination Address (2 byte)

        decodeMacaco((byte) (packet.getData()[5] & 0xFF), mac);
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

            case SoulissBindingUDPConstants.Souliss_UDP_function_ping_resp:
                logger.debug("Received functional code: 0x" + Integer.toHexString(functionalCode) + " - Ping answer");
                decodePing(lastByteGatewayIP, macacoPck);
                break;
            case SoulissBindingUDPConstants.Souliss_UDP_function_discover_GW_node_bcas_resp:
                logger.debug("Received functional code: 0x" + Integer.toHexString(functionalCode)
                        + " - Discover a gateway node answer (broadcast)");
                try {
                    decodePingBroadcast(macacoPck);
                } catch (UnknownHostException e) {
                    logger.debug("Error: {}", e.getLocalizedMessage());
                    logger.error("Error:", e);
                }
                break;

            case SoulissBindingUDPConstants.Souliss_UDP_function_subscribe_resp:
            case SoulissBindingUDPConstants.Souliss_UDP_function_poll_resp:
                logger.debug(
                        "Received functional code: 0x" + Integer.toHexString(functionalCode) + " - Read state answer");
                decodeStateRequest(lastByteGatewayIP, macacoPck);
                break;

            case SoulissBindingUDPConstants.Souliss_UDP_function_typreq_resp:// Answer for assigned typical logic
                logger.debug("Received functional code: 0x" + Integer.toHexString(functionalCode)
                        + " - Read typical logic answer");
                decodeTypRequest(lastByteGatewayIP, macacoPck);
                break;

            case SoulissBindingUDPConstants.Souliss_UDP_function_healthy_resp:// Answer
                // nodes healty
                logger.debug("Received functional code: 0x" + Integer.toHexString(functionalCode) + " - Nodes Healthy");
                decodeHealthyRequest(lastByteGatewayIP, macacoPck);
                break;

            case (byte) SoulissBindingUDPConstants.Souliss_UDP_function_db_struct_resp:
                logger.debug("Received functional code: 0x" + Integer.toHexString(functionalCode)
                        + " - Database structure answer");
                decodeDBStructRequest(lastByteGatewayIP, macacoPck);
                break;
            // case 0x83:
            // logger.debug("Functional code not supported");
            // break;
            // case 0x84:
            // logger.debug("Data out of range");
            // break;
            // case 0x85:
            // logger.debug("Subscription refused");
            // break;
            // default:
            // logger.debug("Unknown functional code");
            // break;
            case (byte) SoulissBindingUDPConstants.Souliss_UDP_function_ActionMessage:
                logger.debug("Received functional code: 0x" + Integer.toHexString(functionalCode)
                        + " Action Message (Topic)");
                decodeActionMessages(lastByteGatewayIP, macacoPck);
                break;
            default:
                logger.debug("Received functional code: 0x" + Integer.toHexString(functionalCode)
                        + " - unused by OH Binding");
        }
    }

    /**
     * @param mac
     */
    private void decodePing(byte lastByteGatewayIP, ArrayList<Byte> mac) {
        int putIn_1 = mac.get(1); // not used
        int putIn_2 = mac.get(2); // not used
        logger.debug("decodePing: putIn code: {}, {}", putIn_1, putIn_2);

        SoulissGatewayHandler gateway = null;
        if (SoulissBindingNetworkParameters.getGateway(lastByteGatewayIP) != null) {
            gateway = (SoulissGatewayHandler) SoulissBindingNetworkParameters.getGateway(lastByteGatewayIP)
                    .getHandler();
        }
        if (gateway != null) {
            gateway.gatewayDetected();
        }
    }

    private void decodePingBroadcast(ArrayList<Byte> macaco) throws UnknownHostException {
        String IP = macaco.get(5) + "." + macaco.get(6) + "." + macaco.get(7) + "." + macaco.get(8);
        byte[] addr = { new Byte(macaco.get(5)).byteValue(), new Byte(macaco.get(6)).byteValue(),
                new Byte(macaco.get(7)).byteValue(), new Byte(macaco.get(8)).byteValue() };
        logger.debug("decodePingBroadcast. Gateway Discovery. IP: {}", IP);

        if (discoverResult != null) {
            discoverResult.gatewayDetected(InetAddress.getByAddress(addr), macaco.get(8).toString());
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
        try {
            byte tgtnode = mac.get(3);
            int numberOf = mac.get(4);

            SoulissGatewayHandler gateway;
            if (SoulissBindingNetworkParameters.getGateway(lastByteGatewayIP) != null) {
                gateway = (SoulissGatewayHandler) SoulissBindingNetworkParameters.getGateway(lastByteGatewayIP)
                        .getHandler();
                if (gateway != null) {
                    int typXnodo = gateway.getMaxTypicalXnode();

                    // creates Souliss nodes
                    for (int j = 0; j < numberOf; j++) {
                        if (mac.get(5 + j) != 0) {// create only not-empty typicals
                            if (!(mac.get(5 + j) == SoulissBindingProtocolConstants.Souliss_T_related)) {
                                byte typical = mac.get(5 + j);
                                byte slot = (byte) (j % typXnodo);
                                byte node = (byte) (j / typXnodo + tgtnode);
                                if (discoverResult != null) {
                                    logger.debug(
                                            "Thing Detected. IP (last byte): {}, Typical: 0x{}, Node: {}, Slot: {} ",
                                            lastByteGatewayIP, Integer.toHexString(typical), node, slot);
                                    discoverResult.thingDetected_Typicals(lastByteGatewayIP, typical, node, slot);
                                } else {
                                    logger.debug("decodeTypRequest aborted. 'discoverResult' is null");
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception uy) {
            logger.error("decodeTypRequest ERROR");
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
    private void decodeActionMessages(byte lastByteGatewayIP, ArrayList<Byte> mac) {
        String sTopicNumber;
        String sTopicVariant;
        float fRet = 0;

        try {
            // A 16-bit Topic Number: Define the topic itself
            // A 8-bit Topic Variant : Define a variant for the topic

            String sTopicNumberArray[] = { Integer.toHexString(mac.get(2)).toUpperCase(),
                    Integer.toHexString(mac.get(1)).toUpperCase() };
            if (sTopicNumberArray[0].length() == 1) {
                sTopicNumberArray[0] = "0" + sTopicNumberArray[0];
            }
            if (sTopicNumberArray[1].length() == 1) {
                sTopicNumberArray[1] = "0" + sTopicNumberArray[1];
            }
            sTopicNumber = sTopicNumberArray[0] + sTopicNumberArray[1];
            logger.debug("Topic Number: 0x" + sTopicNumberArray[0] + sTopicNumberArray[1]);

            sTopicVariant = Integer.toHexString(mac.get(3)).toUpperCase();
            if (sTopicVariant.length() == 1) {
                sTopicVariant = "0" + sTopicVariant;
            }
            logger.debug("Topic Variant: 0x" + sTopicVariant);
            if (mac.get(4) == 1) {
                fRet = mac.get(5);
                logger.debug("Topic Value (Payload one byte): " + Integer.toHexString(mac.get(5)).toUpperCase());
            } else if (mac.get(4) == 2) {
                byte value[] = { mac.get(5), mac.get(6) };

                int shifted = value[1] << 8;
                fRet = HalfFloatUtils.toFloat(shifted + value[0]);
                logger.debug("Topic Value (Payload 2 bytes): " + fRet);
            }

            try {
                ConcurrentHashMap<String, Thing> gwMaps = SoulissBindingNetworkParameters.getHashTableTopics();
                Collection<Thing> gwMapsCollection = gwMaps.values();
                SoulissTopicsHandler topicHandler;
                boolean bIsPresent = false;

                for (Thing t : gwMapsCollection) {
                    if (t.getUID().toString().split(":")[2]
                            .equals(sTopicNumber + SoulissBindingConstants.UUID_NODE_SLOT_SEPARATOR + sTopicVariant)) {
                        topicHandler = (SoulissTopicsHandler) (t.getHandler());
                        if (topicHandler != null) {
                            topicHandler.setState(DecimalType.valueOf(Float.toString(fRet)));
                            bIsPresent = true;
                        }
                    }
                }
                if (discoverResult != null && !bIsPresent) {
                    discoverResult.thingDetected_ActionMessages(sTopicNumber, sTopicVariant);
                }

            } catch (Exception ex) {
            }

        } catch (Exception uy) {
            logger.error("decodeActionMessages ERROR");
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
    private void decodeDBStructRequest(byte lastByteGatewayIP, ArrayList<Byte> mac) {
        try {
            int nodes = mac.get(5);
            int maxnodes = mac.get(6);
            int maxTypicalXnode = mac.get(7);
            int maxrequests = mac.get(8);

            SoulissGatewayHandler gateway = (SoulissGatewayHandler) SoulissBindingNetworkParameters
                    .getGateway(lastByteGatewayIP).getHandler();
            if (gateway != null) {
                gateway.setNodes(nodes);
                gateway.setMaxnodes(maxnodes);
                gateway.setMaxTypicalXnode(maxTypicalXnode);
                gateway.setMaxrequests(maxrequests);

                // db Struct Answer from lastByteGatewayIP
                gateway.dbStructAnswerReceived();
            }
        } catch (Exception e) {
            logger.error("decodeDBStructRequest: SoulissNetworkParameter update ERROR");
        }
    }

    /**
     * Decodes a souliss nodes health request
     *
     * @param macaco
     *            packet
     */
    private void decodeHealthyRequest(byte lastByteGatewayIP, ArrayList<Byte> mac) {
        int numberOf = mac.get(4);
        SoulissGatewayHandler gateway = null;
        try {

            gateway = (SoulissGatewayHandler) SoulissBindingNetworkParameters.getGateway(lastByteGatewayIP)
                    .getHandler();
        } catch (Exception ex) {
        }

        if (gateway != null) {
            for (int i = 5; i < 5 + numberOf; i++) {

                // build an array containing healths
                List<Thing> listaThings = gateway.getThing().getThings();
                ThingHandler handler = null;
                for (Thing thing : listaThings) {
                    if (thing != null) {
                        handler = thing.getHandler();
                        if (handler != null) {
                            int tgtnode = i - 5;
                            if (((SoulissGenericHandler) handler).getNode() == tgtnode) {
                                ((SoulissGenericHandler) handler).setHealty(Byte.valueOf(mac.get(i)));
                            }
                        } else {
                            logger.debug("decode Healthy Request Warning. Thing handler is null");
                        }
                    }
                }
            }
        } else {
            logger.debug("decode Healthy Request Warning. Gateway is null");
        }
    }

    private void decodeStateRequest(byte lastByteGatewayIP, ArrayList<Byte> mac) {
        int tgtnode = mac.get(3);
        SoulissGatewayHandler gateway = null;
        try {
            gateway = (SoulissGatewayHandler) SoulissBindingNetworkParameters.getGateway(lastByteGatewayIP)
                    .getHandler();
        } catch (Exception ex) {
        }

        Iterator thingsIterator;
        if (gateway != null && gateway.IPAddressOnLAN != null
                && Byte.parseByte(gateway.IPAddressOnLAN.split("\\.")[3]) == lastByteGatewayIP) {
            thingsIterator = gateway.getThing().getThings().iterator();
            boolean bFound = false;
            Thing typ = null;
            while (thingsIterator.hasNext() && !bFound) {
                typ = (Thing) thingsIterator.next();
                String sUID_Array[] = typ.getUID().getAsString().split(":");
                ThingHandler handler = typ.getHandler();
                if (handler != null) { // execute it only if binding is Souliss and update is for my
                                       // Gateway
                    if (sUID_Array[0].equals(SoulissBindingConstants.BINDING_ID)
                            && Byte.parseByte(((SoulissGenericHandler) handler).getGatewayIP().toString()
                                    .split("\\.")[3]) == lastByteGatewayIP) {

                        if (((SoulissGenericHandler) handler) != null
                                && ((SoulissGenericHandler) handler).getNode() == tgtnode) { // execute it
                                                                                             // only
                                                                                             // if it is
                                                                                             // node
                                                                                             // to update
                            // ...now check slot
                            int slot = ((SoulissGenericHandler) handler).getSlot();
                            // get typical value
                            byte sVal = getByteAtSlot(mac, slot);
                            OnOffType typicalState = null;
                            // update Txx
                            switch (sUID_Array[1]) {
                                case SoulissBindingConstants.T11:
                                    logger.debug("Decoding " + SoulissBindingConstants.T11 + " packet");
                                    ((SoulissT11Handler) handler).setRawState(sVal);
                                    break;
                                case SoulissBindingConstants.T12:
                                    logger.debug("Decoding " + SoulissBindingConstants.T12 + " packet");
                                    ((SoulissT12Handler) handler).setRawState(sVal);
                                    break;
                                case SoulissBindingConstants.T13:
                                    logger.debug("Decoding " + SoulissBindingConstants.T13 + " packet");
                                    ((SoulissT13Handler) handler).setRawState(sVal);
                                    break;
                                case SoulissBindingConstants.T14:
                                    logger.debug("Decoding " + SoulissBindingConstants.T14 + " packet");
                                    ((SoulissT14Handler) handler).setRawState(sVal);
                                    break;
                                case SoulissBindingConstants.T16:
                                    logger.debug("Decoding " + SoulissBindingConstants.T16 + " packet");
                                    ((SoulissT16Handler) handler).setRawState_command(sVal);
                                    ((SoulissT16Handler) handler).setRawState_RGB(getByteAtSlot(mac, slot + 1),
                                            getByteAtSlot(mac, slot + 2), getByteAtSlot(mac, slot + 3));
                                    break;

                                case SoulissBindingConstants.T18:
                                    logger.debug("Decoding " + SoulissBindingConstants.T18 + " packet");
                                    ((SoulissT18Handler) handler).setRawState(sVal);
                                    break;

                                case SoulissBindingConstants.T19:
                                    logger.debug("Decoding " + SoulissBindingConstants.T19 + " packet");
                                    ((SoulissT19Handler) handler).setRawState(sVal);
                                    ((SoulissT19Handler) handler).setRawStateDimmerValue(getByteAtSlot(mac, slot + 1));
                                    break;

                                case SoulissBindingConstants.T1A:
                                    logger.debug("Decoding " + SoulissBindingConstants.T1A + " packet");
                                    ((SoulissT1AHandler) handler).setRawState(sVal);
                                    break;
                                case SoulissBindingConstants.T21:
                                case SoulissBindingConstants.T22:
                                    logger.debug("Decoding " + SoulissBindingConstants.T21 + "/"
                                            + SoulissBindingConstants.T22 + " packet");
                                    ((SoulissT22Handler) handler).setRawState(sVal);
                                    break;
                                case SoulissBindingConstants.T31:
                                    logger.debug("Decoding " + SoulissBindingConstants.T31 + "/"
                                            + SoulissBindingConstants.T31 + " packet" + " -- bit0 (system on-off): "
                                            + getBitState(sVal, 0) + " - bit1 (heating on-off): " + getBitState(sVal, 1)
                                            + " - bit2 (cooling on-off): " + getBitState(sVal, 2)
                                            + " - bit3 (fan1 on-off): " + getBitState(sVal, 3)
                                            + " - bit4 (fan2 on-off): " + getBitState(sVal, 4)
                                            + " - bit5 (fan3 on-off): " + getBitState(sVal, 5)
                                            + " - bit6 (Manual/automatic fan mode): " + getBitState(sVal, 6)
                                            + " - bit7 (heating/cooling mode): " + getBitState(sVal, 7));
                                    try {
                                        ((SoulissT31Handler) handler).setRawStateValues(sVal, getFloatAtSlot(mac, slot + 1),
                                                getFloatAtSlot(mac, slot + 3));
                                    } catch (Exception e) {
                                        // TODO Auto-generated catch block
                                        e.printStackTrace();
                                    }

                                    break;
                                case SoulissBindingConstants.T41:
                                    ((SoulissT41Handler) handler).setRawState(sVal);
                                    break;
                                case SoulissBindingConstants.T42:
                                    ((SoulissT42Handler) handler).setRawState(sVal);
                                    switch (sVal) {
                                        case SoulissBindingProtocolConstants.Souliss_T4n_NoAntitheft:
                                            ((SoulissT42Handler) handler).setState(StringType
                                                    .valueOf(SoulissBindingConstants.T4N_ALARMOFF_MESSAGE_CHANNEL));
                                            break;
                                        case SoulissBindingProtocolConstants.Souliss_T4n_Alarm:
                                            ((SoulissT42Handler) handler).setState(StringType
                                                    .valueOf(SoulissBindingConstants.T4N_ALARMON_MESSAGE_CHANNEL));
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
                                    logger.debug("Decoding " + SoulissBindingConstants.T61 + " packet");
                                    if (!Float.isNaN(getFloatAtSlot(mac, slot))) {
                                        ((SoulissT61Handler) handler).setFloatValue(getFloatAtSlot(mac, slot));
                                    }
                                    break;
                                case SoulissBindingConstants.T62:
                                    logger.debug("Decoding " + SoulissBindingConstants.T62 + " packet");
                                    if (!Float.isNaN(getFloatAtSlot(mac, slot))) {
                                        ((SoulissT62Handler) handler).setFloatValue(getFloatAtSlot(mac, slot));
                                    }
                                    break;
                                case SoulissBindingConstants.T63:
                                    logger.debug("Decoding " + SoulissBindingConstants.T63 + " packet");
                                    if (!Float.isNaN(getFloatAtSlot(mac, slot))) {
                                        ((SoulissT63Handler) handler).setFloatValue(getFloatAtSlot(mac, slot));
                                    }
                                    break;
                                case SoulissBindingConstants.T64:
                                    logger.debug("Decoding " + SoulissBindingConstants.T64 + " packet");
                                    if (!Float.isNaN(getFloatAtSlot(mac, slot))) {
                                        ((SoulissT64Handler) handler).setFloatValue(getFloatAtSlot(mac, slot));
                                    }
                                    break;
                                case SoulissBindingConstants.T65:
                                    logger.debug("Decoding " + SoulissBindingConstants.T65 + " packet");
                                    if (!Float.isNaN(getFloatAtSlot(mac, slot))) {
                                        ((SoulissT65Handler) handler).setFloatValue(getFloatAtSlot(mac, slot));
                                    }
                                    break;
                                case SoulissBindingConstants.T66:
                                    logger.debug("Decoding " + SoulissBindingConstants.T66 + " packet");
                                    if (!Float.isNaN(getFloatAtSlot(mac, slot))) {
                                        ((SoulissT66Handler) handler).setFloatValue(getFloatAtSlot(mac, slot));
                                    }
                                    break;
                                case SoulissBindingConstants.T67:
                                    logger.debug("Decoding " + SoulissBindingConstants.T67 + " packet");
                                    if (!Float.isNaN(getFloatAtSlot(mac, slot))) {
                                        ((SoulissT67Handler) handler).setFloatValue(getFloatAtSlot(mac, slot));
                                    }
                                    break;
                                case SoulissBindingConstants.T68:
                                    logger.debug("Decoding " + SoulissBindingConstants.T68 + " packet");
                                    if (!Float.isNaN(getFloatAtSlot(mac, slot))) {
                                        ((SoulissT68Handler) handler).setFloatValue(getFloatAtSlot(mac, slot));
                                    }
                                    break;

                                case SoulissBindingConstants.TOPICS:
                                    logger.debug("Decoding " + SoulissBindingConstants.TOPICS + " packet");
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
    }

    private byte getByteAtSlot(ArrayList<Byte> mac, int slot) {
        return mac.get(5 + slot);

    }

    private float getFloatAtSlot(ArrayList<Byte> mac, int slot) {
        int iOutput = mac.get(5 + slot) & 0xFF;
        int iOutput2 = mac.get(5 + slot + 1) & 0xFF;
        // ora ho i due bytes, li converto
        int shifted = iOutput2 << 8;
        float ret = HalfFloatUtils.toFloat(shifted + iOutput);
        return ret;
    }

    public byte getBitState(byte vRaw, int iBit) {
        final int MASK_BIT_1 = 0x1;

        if (((vRaw >>> iBit) & MASK_BIT_1) == 0) {
            return 0;
        } else {
            return 1;
        }
    }

}
