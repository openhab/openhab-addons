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
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.souliss.SoulissBindingConstants;
import org.openhab.binding.souliss.SoulissBindingProtocolConstants;
import org.openhab.binding.souliss.SoulissBindingUDPConstants;
import org.openhab.binding.souliss.handler.SoulissGatewayHandler;
import org.openhab.binding.souliss.handler.SoulissGenericTypical;
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
        ArrayList<Short> mac = new ArrayList<Short>();
        for (int ig = 7; ig < checklen; ig++) {
            mac.add((short) (packet.getData()[ig] & 0xFF));
        }
        // Last number of IP of Original Destination Address (2 byte)

        decodeMacaco((short) (packet.getData()[5] & 0xFF), mac);
    }

    /**
     * Decodes lower level MaCaCo packet
     *
     * @param lastByteGatewayIP
     *
     * @param macacoPck
     */
    private void decodeMacaco(short lastByteGatewayIP, ArrayList<Short> macacoPck) {
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
    private void decodePing(short lastByteGatewayIP, ArrayList<Short> mac) {
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

    private void decodePingBroadcast(ArrayList<Short> macaco) throws UnknownHostException {
        String IP = macaco.get(5) + "." + macaco.get(6) + "." + macaco.get(7) + "." + macaco.get(8);
        byte[] addr = { new Short(macaco.get(5)).byteValue(), new Short(macaco.get(6)).byteValue(),
                new Short(macaco.get(7)).byteValue(), new Short(macaco.get(8)).byteValue() };
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
    private void decodeTypRequest(short lastByteGatewayIP, ArrayList<Short> mac) {
        try {
            short tgtnode = mac.get(3);
            int numberOf = mac.get(4);

            SoulissGatewayHandler gateway = (SoulissGatewayHandler) SoulissBindingNetworkParameters
                    .getGateway(lastByteGatewayIP).getHandler();
            if (gateway != null) {
                int typXnodo = gateway.getMaxTypicalXnode();

                // creates Souliss nodes
                for (int j = 0; j < numberOf; j++) {
                    if (mac.get(5 + j) != 0) {// create only not-empty typicals
                        if (!(mac.get(5 + j) == SoulissBindingProtocolConstants.Souliss_T_related)) {
                            short typical = mac.get(5 + j);
                            short slot = (short) (j % typXnodo);
                            short node = (short) (j / typXnodo + tgtnode);
                            if (discoverResult != null) {
                                logger.debug("Thing Detected. IP (last byte): {}, Typical: 0x{}, Node: {}, Slot: {} ",
                                        lastByteGatewayIP, Integer.toHexString(typical), node, slot);
                                discoverResult.thingDetected_Typicals(lastByteGatewayIP, typical, node, slot);
                            } else {
                                logger.debug("decodeTypRequest aborted. 'discoverResult' is null");
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
    private void decodeActionMessages(short lastByteGatewayIP, ArrayList<Short> mac) {
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
                short value[] = { mac.get(5), mac.get(6) };

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
    private void decodeDBStructRequest(short lastByteGatewayIP, ArrayList<Short> mac) {
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
    private void decodeHealthyRequest(short lastByteGatewayIP, ArrayList<Short> mac) {
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
                            if (((SoulissGenericTypical) handler).getNode() == tgtnode) {
                                ((SoulissGenericTypical) handler).setHealty(Short.valueOf(mac.get(i)));
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

    private void decodeStateRequest(short lastByteGatewayIP, ArrayList<Short> mac) {
        int tgtnode = mac.get(3);
        SoulissGatewayHandler gateway = null;
        try {
            gateway = (SoulissGatewayHandler) SoulissBindingNetworkParameters.getGateway(lastByteGatewayIP)
                    .getHandler();
        } catch (Exception ex) {
        }

        Iterator thingsIterator;
        if (gateway != null && gateway.IPAddressOnLAN != null
                && Short.parseShort(gateway.IPAddressOnLAN.split("\\.")[3]) == lastByteGatewayIP) {
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
                            && Short.parseShort(((SoulissGenericTypical) handler).getGatewayIP().toString()
                                    .split("\\.")[3]) == lastByteGatewayIP) {

                        if (((SoulissGenericTypical) handler).getNode() == tgtnode) { // execute it
                                                                                      // only
                                                                                      // if it is
                                                                                      // node
                                                                                      // to update
                            // ...now check slot
                            int slot = ((SoulissGenericTypical) handler).getSlot();
                            // get typical value
                            short sVal = getByteAtSlot(mac, slot);
                            OnOffType typicalState = null;
                            // update Txx
                            switch (sUID_Array[1]) {
                                case SoulissBindingConstants.T11:
                                    logger.debug("Decoding " + SoulissBindingConstants.T11 + " packet");
                                    typicalState = getOHState_OnOff_FromSoulissVal(sVal);
                                    ((SoulissT11Handler) handler).setState(typicalState);
                                    break;
                                case SoulissBindingConstants.T12:
                                    logger.debug("Decoding " + SoulissBindingConstants.T12 + " packet");
                                    if (sVal == SoulissBindingProtocolConstants.Souliss_T1n_OnCoil_Auto) {
                                        ((SoulissT12Handler) handler).setState(OnOffType.ON);
                                        ((SoulissT12Handler) handler).setState_Automode(OnOffType.ON);
                                    } else if (sVal == SoulissBindingProtocolConstants.Souliss_T1n_OffCoil_Auto) {
                                        ((SoulissT12Handler) handler).setState(OnOffType.OFF);
                                        ((SoulissT12Handler) handler).setState_Automode(OnOffType.ON);

                                    } else if (sVal == SoulissBindingProtocolConstants.Souliss_T1n_OnCoil) {
                                        ((SoulissT12Handler) handler).setState(OnOffType.ON);
                                        ((SoulissT12Handler) handler).setState_Automode(OnOffType.OFF);
                                    } else if (sVal == SoulissBindingProtocolConstants.Souliss_T1n_OffCoil) {
                                        ((SoulissT12Handler) handler).setState(OnOffType.OFF);
                                        ((SoulissT12Handler) handler).setState_Automode(OnOffType.OFF);
                                    }
                                    break;
                                case SoulissBindingConstants.T13:
                                    logger.debug("Decoding " + SoulissBindingConstants.T13 + " packet");
                                    typicalState = getOHState_OnOff_FromSoulissVal(sVal);
                                    ((SoulissT13Handler) handler).setState(typicalState);
                                    ((SoulissT13Handler) handler).setState(getOHState_OpenClose_FromSoulissVal(sVal));
                                    break;
                                case SoulissBindingConstants.T14:
                                    logger.debug("Decoding " + SoulissBindingConstants.T14 + " packet");
                                    typicalState = getOHState_OnOff_FromSoulissVal(sVal);
                                    ((SoulissT14Handler) handler).setState(typicalState);
                                    break;
                                case SoulissBindingConstants.T16:
                                    logger.debug("Decoding " + SoulissBindingConstants.T16 + " packet");
                                    typicalState = getOHState_OnOff_FromSoulissVal(sVal);
                                    ((SoulissT16Handler) handler).setState(typicalState);
                                    ((SoulissT16Handler) handler).setStateRGB(getByteAtSlot(mac, slot + 1),
                                            getByteAtSlot(mac, slot + 2), getByteAtSlot(mac, slot + 3));
                                    break;

                                case SoulissBindingConstants.T18:
                                    logger.debug("Decoding " + SoulissBindingConstants.T18 + " packet");
                                    typicalState = getOHState_OnOff_FromSoulissVal(sVal);
                                    ((SoulissT18Handler) handler).setState(typicalState);
                                    break;

                                case SoulissBindingConstants.T19:
                                    logger.debug("Decoding " + SoulissBindingConstants.T19 + " packet");
                                    typicalState = getOHState_OnOff_FromSoulissVal(sVal);
                                    ((SoulissT19Handler) handler).setState(typicalState);
                                    ((SoulissT19Handler) handler).setDimmerValue(getByteAtSlot(mac, slot + 1));
                                    break;

                                case SoulissBindingConstants.T1A:
                                    logger.debug("Decoding " + SoulissBindingConstants.T1A + " packet");
                                    ((SoulissT1AHandler) handler).setState(StringType.valueOf(String.valueOf(sVal)));
                                    break;
                                case SoulissBindingConstants.T21:
                                case SoulissBindingConstants.T22:
                                    logger.debug("Decoding " + SoulissBindingConstants.T21 + "/"
                                            + SoulissBindingConstants.T22 + " packet");
                                    if (sVal == SoulissBindingProtocolConstants.Souliss_T2n_Coil_Open) {
                                        ((SoulissT22Handler) handler).setState(UpDownType.UP);
                                        ((SoulissT22Handler) handler).setState_Message(
                                                SoulissBindingConstants.ROLLERSHUTTER_MESSAGE_OPENING_CHANNEL);
                                    } else if (sVal == SoulissBindingProtocolConstants.Souliss_T2n_Coil_Close) {
                                        ((SoulissT22Handler) handler).setState(UpDownType.DOWN);
                                        ((SoulissT22Handler) handler).setState_Message(
                                                SoulissBindingConstants.ROLLERSHUTTER_MESSAGE_CLOSING_CHANNEL);
                                    }
                                    switch (sVal) {
                                        case SoulissBindingProtocolConstants.Souliss_T2n_Coil_Stop:
                                            ((SoulissT22Handler) handler).setState_Message(
                                                    SoulissBindingConstants.ROLLERSHUTTER_MESSAGE_STOP_CHANNEL);
                                            break;
                                        case SoulissBindingProtocolConstants.Souliss_T2n_Coil_Off:
                                            ((SoulissT22Handler) handler).setState_Message(
                                                    SoulissBindingConstants.ROLLERSHUTTER_MESSAGE_OPENING_CHANNEL);
                                            break;
                                        case SoulissBindingProtocolConstants.Souliss_T2n_LimSwitch_Close:
                                            ((SoulissT22Handler) handler).setState_Message(
                                                    SoulissBindingConstants.ROLLERSHUTTER_MESSAGE_LIMITSWITCH_CLOSE_CHANNEL);
                                            break;
                                        case SoulissBindingProtocolConstants.Souliss_T2n_LimSwitch_Open:
                                            ((SoulissT22Handler) handler).setState_Message(
                                                    SoulissBindingConstants.ROLLERSHUTTER_MESSAGE_LIMITSWITCH_OPEN_CHANNEL);
                                            break;
                                        case SoulissBindingProtocolConstants.Souliss_T2n_NoLimSwitch:
                                            ((SoulissT22Handler) handler).setState_Message(
                                                    SoulissBindingConstants.ROLLERSHUTTER_MESSAGE_LIMITSWITCH_OPEN_CHANNEL);
                                            break;
                                        case SoulissBindingProtocolConstants.Souliss_T2n_Timer_Off:
                                            ((SoulissT22Handler) handler).setState_Message(
                                                    SoulissBindingConstants.ROLLERSHUTTER_MESSAGE_TIMER_OFF);
                                            break;
                                        case SoulissBindingProtocolConstants.Souliss_T2n_State_Open:
                                            ((SoulissT22Handler) handler).setState_Message(
                                                    SoulissBindingConstants.ROLLERSHUTTER_MESSAGE_STATE_OPEN_CHANNEL);
                                            break;
                                        case SoulissBindingProtocolConstants.Souliss_T2n_State_Close:
                                            ((SoulissT22Handler) handler).setState_Message(
                                                    SoulissBindingConstants.ROLLERSHUTTER_MESSAGE_STATE_CLOSE_CHANNEL);
                                            break;
                                        // }
                                    }
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

                                    String sMessage = "";
                                    switch (getBitState(sVal, 0)) {
                                        case 0:
                                            sMessage = SoulissBindingConstants.T31_OFF_MESSAGE_SYSTEM_CHANNEL;
                                            break;
                                        case 1:
                                            sMessage = SoulissBindingConstants.T31_ON_MESSAGE_SYSTEM_CHANNEL;
                                            break;
                                    }
                                    ((SoulissT31Handler) handler).setState(StringType.valueOf(sMessage));

                                    switch (getBitState(sVal, 7)) {
                                        case 0:
                                            sMessage = SoulissBindingConstants.T31_HEATINGMODE_MESSAGE_MODE_CHANNEL;
                                            break;
                                        case 1:
                                            sMessage = SoulissBindingConstants.T31_COOLINGMODE_MESSAGE_MODE_CHANNEL;
                                            break;
                                    }
                                    ((SoulissT31Handler) handler).setState(StringType.valueOf(sMessage));

                                    // button indicante se il sistema sta andando o meno
                                    switch (getBitState(sVal, 1) + getBitState(sVal, 2)) {
                                        case 0:
                                            sMessage = SoulissBindingConstants.T31_OFF_MESSAGE_FIRE_CHANNEL;
                                            break;
                                        case 1:
                                            sMessage = SoulissBindingConstants.T31_ON_MESSAGE_FIRE_CHANNEL;
                                            break;
                                    }
                                    ((SoulissT31Handler) handler).setState(StringType.valueOf(sMessage));

                                    // FAN SPEED
                                    switch (getBitState(sVal, 3) + getBitState(sVal, 4) + getBitState(sVal, 5)) {
                                        case 0:
                                            sMessage = SoulissBindingConstants.T31_FANOFF_MESSAGE_FAN_CHANNEL;
                                            break;
                                        case 1:
                                            sMessage = SoulissBindingConstants.T31_FANLOW_MESSAGE_FAN_CHANNEL;
                                            break;
                                        case 2:
                                            sMessage = SoulissBindingConstants.T31_FANMEDIUM_MESSAGE_FAN_CHANNEL;
                                            break;
                                        case 3:
                                            sMessage = SoulissBindingConstants.T31_FANHIGH_MESSAGE_FAN_CHANNEL;
                                            break;
                                    }

                                    ((SoulissT31Handler) handler).setState(StringType.valueOf(sMessage));

                                    // SLOT 1-2: Temperature Value
                                    float val = getFloatAtSlot(mac, slot + 1);
                                    if (!Float.isNaN(val)) {
                                        ((SoulissT31Handler) handler)
                                                .setMeasuredValue(DecimalType.valueOf(String.valueOf(val)));
                                    }

                                    // SLOT 3-4: Setpoint Value
                                    val = getFloatAtSlot(mac, slot + 3);
                                    if (!Float.isNaN(val)) {
                                        ((SoulissT31Handler) handler)
                                                .setSetpointValue(DecimalType.valueOf(String.valueOf(val)));
                                    }
                                    break;
                                case SoulissBindingConstants.T41:
                                    switch (sVal) {
                                        case SoulissBindingProtocolConstants.Souliss_T4n_NoAntitheft:
                                            ((SoulissT41Handler) handler).setState(OnOffType.OFF);
                                            ((SoulissT41Handler) handler).setState(StringType
                                                    .valueOf(SoulissBindingConstants.T4N_ALARMOFF_MESSAGE_CHANNEL));
                                            break;
                                        case SoulissBindingProtocolConstants.Souliss_T4n_Antitheft:
                                            ((SoulissT41Handler) handler).setState(OnOffType.ON);
                                            ((SoulissT41Handler) handler).setState(StringType
                                                    .valueOf(SoulissBindingConstants.T4N_ALARMOFF_MESSAGE_CHANNEL));
                                            break;
                                        case SoulissBindingProtocolConstants.Souliss_T4n_InAlarm:
                                            ((SoulissT41Handler) handler).setState(StringType
                                                    .valueOf(SoulissBindingConstants.T4N_ALARMON_MESSAGE_CHANNEL));
                                            break;
                                        case SoulissBindingProtocolConstants.Souliss_T4n_Armed:
                                            ((SoulissT41Handler) handler).setState(
                                                    StringType.valueOf(SoulissBindingConstants.T4N_ONOFFALARM_CHANNEL));
                                            break;
                                    }
                                    break;
                                case SoulissBindingConstants.T42:
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
                                        ((SoulissT5nHandler) handler).setState(
                                                DecimalType.valueOf(Float.toString(getFloatAtSlot(mac, slot))));
                                    }
                                    break;
                                case SoulissBindingConstants.T61:
                                    logger.debug("Decoding " + SoulissBindingConstants.T61 + " packet");
                                    if (!Float.isNaN(getFloatAtSlot(mac, slot))) {
                                        ((SoulissT61Handler) handler).setState(
                                                DecimalType.valueOf(Float.toString(getFloatAtSlot(mac, slot))));
                                    }
                                    break;
                                case SoulissBindingConstants.T62:
                                    logger.debug("Decoding " + SoulissBindingConstants.T62 + " packet");
                                    if (!Float.isNaN(getFloatAtSlot(mac, slot))) {
                                        ((SoulissT62Handler) handler).setState(
                                                DecimalType.valueOf(Float.toString(getFloatAtSlot(mac, slot))));
                                    }
                                    break;
                                case SoulissBindingConstants.T63:
                                    logger.debug("Decoding " + SoulissBindingConstants.T63 + " packet");
                                    if (!Float.isNaN(getFloatAtSlot(mac, slot))) {
                                        ((SoulissT63Handler) handler).setState(
                                                DecimalType.valueOf(Float.toString(getFloatAtSlot(mac, slot))));
                                    }
                                    break;
                                case SoulissBindingConstants.T64:
                                    logger.debug("Decoding " + SoulissBindingConstants.T64 + " packet");
                                    if (!Float.isNaN(getFloatAtSlot(mac, slot))) {
                                        ((SoulissT64Handler) handler).setState(
                                                DecimalType.valueOf(Float.toString(getFloatAtSlot(mac, slot))));
                                    }
                                    break;
                                case SoulissBindingConstants.T65:
                                    logger.debug("Decoding " + SoulissBindingConstants.T65 + " packet");
                                    if (!Float.isNaN(getFloatAtSlot(mac, slot))) {
                                        ((SoulissT65Handler) handler).setState(
                                                DecimalType.valueOf(Float.toString(getFloatAtSlot(mac, slot))));
                                    }
                                    break;
                                case SoulissBindingConstants.T66:
                                    logger.debug("Decoding " + SoulissBindingConstants.T66 + " packet");
                                    if (!Float.isNaN(getFloatAtSlot(mac, slot))) {
                                        ((SoulissT66Handler) handler).setState(
                                                DecimalType.valueOf(Float.toString(getFloatAtSlot(mac, slot))));
                                    }
                                    break;
                                case SoulissBindingConstants.T67:
                                    logger.debug("Decoding " + SoulissBindingConstants.T67 + " packet");
                                    if (!Float.isNaN(getFloatAtSlot(mac, slot))) {
                                        ((SoulissT67Handler) handler).setState(
                                                DecimalType.valueOf(Float.toString(getFloatAtSlot(mac, slot))));
                                    }
                                    break;
                                case SoulissBindingConstants.T68:
                                    logger.debug("Decoding " + SoulissBindingConstants.T68 + " packet");
                                    if (!Float.isNaN(getFloatAtSlot(mac, slot))) {
                                        ((SoulissT68Handler) handler).setState(
                                                DecimalType.valueOf(Float.toString(getFloatAtSlot(mac, slot))));
                                    }
                                    break;

                                case SoulissBindingConstants.TOPICS:
                                    logger.debug("Decoding " + SoulissBindingConstants.TOPICS + " packet");
                                    if (!Float.isNaN(getFloatAtSlot(mac, slot))) {
                                        ((SoulissTopicsHandler) handler).setState(
                                                DecimalType.valueOf(Float.toString(getFloatAtSlot(mac, slot))));
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

    private OnOffType getOHState_OnOff_FromSoulissVal(short sVal) {
        if (sVal == SoulissBindingProtocolConstants.Souliss_T1n_OnCoil) {
            return OnOffType.ON;
        } else if (sVal == SoulissBindingProtocolConstants.Souliss_T1n_OffCoil) {
            return OnOffType.OFF;
        } else if (sVal == SoulissBindingProtocolConstants.Souliss_T1n_OnFeedback) {
            return OnOffType.ON;
        } else if (sVal == SoulissBindingProtocolConstants.Souliss_T1n_OffFeedback) {
            return OnOffType.OFF;
        }
        return null;
    }

    private OpenClosedType getOHState_OpenClose_FromSoulissVal(short sVal) {
        if (sVal == SoulissBindingProtocolConstants.Souliss_T1n_OnCoil) {
            return OpenClosedType.CLOSED;
        } else if (sVal == SoulissBindingProtocolConstants.Souliss_T1n_OffCoil) {
            return OpenClosedType.OPEN;
        }
        return null;
    }

    private Short getByteAtSlot(ArrayList<Short> mac, int slot) {
        return mac.get(5 + slot);

    }

    private float getFloatAtSlot(ArrayList<Short> mac, int slot) {
        int iOutput = mac.get(5 + slot);
        int iOutput2 = mac.get(5 + slot + 1);
        // ora ho i due bytes, li converto
        int shifted = iOutput2 << 8;
        float ret = HalfFloatUtils.toFloat(shifted + iOutput);
        return ret;
    }

    public short getBitState(short vRaw, int iBit) {
        final int MASK_BIT_1 = 0x1;

        if (((vRaw >>> iBit) & MASK_BIT_1) == 0) {
            return 0;
        } else {
            return 1;
        }
    }

}
