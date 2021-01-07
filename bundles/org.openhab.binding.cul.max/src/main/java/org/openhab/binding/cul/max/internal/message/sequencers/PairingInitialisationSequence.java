/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.cul.max.internal.message.sequencers;

import java.util.Iterator;
import java.util.Set;

import org.openhab.binding.cul.max.internal.handler.MaxCulMsgHandler;
import org.openhab.binding.cul.max.internal.handler.MaxDevicesHandler;
import org.openhab.binding.cul.max.internal.messages.AckMsg;
import org.openhab.binding.cul.max.internal.messages.BaseMsg;
import org.openhab.binding.cul.max.internal.messages.MaxCulWeekProfilePart;
import org.openhab.binding.cul.max.internal.messages.PairPingMsg;
import org.openhab.binding.cul.max.internal.messages.constants.MaxCulDevice;
import org.openhab.binding.cul.max.internal.messages.constants.MaxCulMsgType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle the pairing and initialisation sequence of a new device. This should
 * be called when the device has been verified etc as this will just send the
 * pong without verifying whether we should or not.
 *
 * @author Paul Hampson (cyclingengineer) - Initial contribution
 * @author Johannes Goehr (johgoe) - Migration to OpenHab 3.0
 */
public class PairingInitialisationSequence implements MessageSequencer {

    private enum PairingInitialisationState {
        INITIAL_PING,
        PONG_ACKED,
        GROUP_ID_ACKED,
        CONFIG_TEMPS_ACKED,
        SENDING_ASSOCIATIONS,
        SENDING_ASSOCIATIONS_ACKED,
        SENDING_WEEK_PROFILE,
        RETX_WAKEUP_ACK,
        SENDING_WEEK_PROFILE_ACKED,
        FINISHED;
    }

    private static final Logger logger = LoggerFactory.getLogger(PairingInitialisationSequence.class);

    private PairingInitialisationState state = PairingInitialisationState.INITIAL_PING;

    private String devAddr;
    private byte group_id;
    private MaxCulMsgHandler messageHandler;
    private int pktLostCount = 0;
    private MaxCulDevice deviceType = MaxCulDevice.UNKNOWN;
    private MaxDevicesHandler maxDevicesHandler;
    private Iterator<MaxDevicesHandler> assocIter;
    private Iterator<MaxCulWeekProfilePart> weekProfileIter;
    private boolean useFast = true;

    /* place to keep stuff when going through ReTx */
    private BaseMsg reTxMsg;
    private PairingInitialisationState reTxState;

    private MaxCulWeekProfilePart currentWeekProfilePart;
    private boolean secondHalf;

    public PairingInitialisationSequence(byte group_id, MaxCulMsgHandler messageHandler,
            MaxDevicesHandler maxDevicesHandler) {
        this.group_id = group_id;
        this.messageHandler = messageHandler;
        this.maxDevicesHandler = maxDevicesHandler;
    }

    @Override
    public void runSequencer(BaseMsg msg) {
        /*
         * This sequence is taken from observations of activity between the MAX!
         * Cube and a wall thermostat and refined using some experimentation :)
         */
        if (state != PairingInitialisationState.RETX_WAKEUP_ACK) {
            pktLostCount = 0; // reset counter - ack received
        }
        try {
            logger.debug("Sequence State: {}", state);
            switch (state) {
                case INITIAL_PING:
                    /* get device type */
                    PairPingMsg ppMsg = new PairPingMsg(msg.rawMsg);
                    this.deviceType = MaxCulDevice.getDeviceTypeFromInt(ppMsg.type);
                    this.devAddr = msg.srcAddrStr;
                    /* Send PONG - assumes PING is checked */
                    logger.debug("Sending PONG: {}", this.devAddr);
                    messageHandler.sendPairPong(devAddr, this);
                    state = PairingInitialisationState.PONG_ACKED;
                    break;
                case PONG_ACKED:
                    if (msg.msgType == MaxCulMsgType.ACK) {
                        AckMsg ack = new AckMsg(msg.rawMsg);
                        if (!ack.getIsNack()) {
                            logger.debug("PONG_ACKED received: {}", this.devAddr);
                            if (this.deviceType == MaxCulDevice.PUSH_BUTTON
                                    || this.deviceType == MaxCulDevice.SHUTTER_CONTACT) {
                                /* for a push button or a shutter contact we're done now */
                                logger.debug("{} pairing FINISHED: {}", this.deviceType, devAddr);
                                state = PairingInitialisationState.FINISHED;
                            } else {
                                /* send group id information */
                                logger.debug("Sending GROUP_ID: {}", devAddr);
                                messageHandler.sendSetGroupId(devAddr, group_id, this);
                                state = PairingInitialisationState.GROUP_ID_ACKED;
                            }
                        } else {
                            logger.error("PAIR_PONG was nacked. Ending sequence");
                            state = PairingInitialisationState.FINISHED;
                        }
                    } else {
                        logger.error("Received {} when expecting ACK: {}", msg.msgType, msg.srcAddrStr);
                    }
                    break;
                case GROUP_ID_ACKED:
                    if (msg.msgType == MaxCulMsgType.ACK) {
                        AckMsg ack = new AckMsg(msg.rawMsg);
                        if (!ack.getIsNack() && (this.deviceType == MaxCulDevice.RADIATOR_THERMOSTAT
                                || this.deviceType == MaxCulDevice.WALL_THERMOSTAT
                                || this.deviceType == MaxCulDevice.RADIATOR_THERMOSTAT_PLUS)) {
                            logger.debug("GROUP_ID_ACKED received: {}", msg.srcAddrStr);
                            // send temps for comfort/eco etc
                            logger.debug("sendConfigTemperatures: {}", devAddr);
                            messageHandler.sendConfigTemperatures(devAddr, this, maxDevicesHandler.getComfortTemp(),
                                    maxDevicesHandler.getEcoTemp(), maxDevicesHandler.getMaxTemp(),
                                    maxDevicesHandler.getMinTemp(), maxDevicesHandler.getMeasurementOffset(),
                                    maxDevicesHandler.getWindowOpenTemperature(),
                                    maxDevicesHandler.getWindowOpenDuration());
                            state = PairingInitialisationState.CONFIG_TEMPS_ACKED;
                        } else {
                            logger.error("SET_GROUP_ID was nacked. Ending sequence: {}", msg.srcAddrStr);
                            state = PairingInitialisationState.FINISHED;
                        }
                    } else {
                        logger.error("Received {} when expecting ACK: {}", msg.msgType, msg.srcAddrStr);
                    }
                    break;
                case CONFIG_TEMPS_ACKED:
                    if (msg.msgType == MaxCulMsgType.ACK) {
                        AckMsg ack = new AckMsg(msg.rawMsg);
                        if (!ack.getIsNack()) {
                            logger.debug("CONFIG_TEMPS_ACKED received: {}", this.devAddr);
                            /*
                             * associate device with us so we get updates - we pretend
                             * to be the MAX! Cube
                             */
                            logger.debug("sendAddLinkPartner for us: {}", devAddr);
                            messageHandler.sendAddLinkPartner(devAddr, this, msg.dstAddrStr, MaxCulDevice.CUBE);

                            /*
                             * if there are more associations to come then set up
                             * iterator and goto state to transmit more associations
                             */
                            Set<MaxDevicesHandler> associations = maxDevicesHandler.getAssociations();
                            if (associations != null && associations.isEmpty() == false) {
                                assocIter = associations.iterator();
                                logger.debug("prepare next sendAddLinkPartner: {}", devAddr);
                                state = PairingInitialisationState.SENDING_ASSOCIATIONS;
                            } else {
                                logger.debug("No user configured associations: {}", devAddr);
                                state = PairingInitialisationState.SENDING_ASSOCIATIONS_ACKED;
                            }
                        } else {
                            logger.error("CONFIG_TEMPERATURES was nacked. Ending sequence: {}", devAddr);
                            state = PairingInitialisationState.FINISHED;
                        }
                    } else {
                        logger.error("Received {} when expecting ACK: {}", msg.msgType, msg.srcAddrStr);
                    }
                    break;
                case SENDING_ASSOCIATIONS:
                    if (msg.msgType == MaxCulMsgType.ACK) {
                        AckMsg ack = new AckMsg(msg.rawMsg);
                        if (!ack.getIsNack()) {
                            logger.debug("SENDING_ASSOCIATIONS_ACKED received: {}", ack.srcAddrStr);
                            if (assocIter.hasNext()) /*
                                                      * this should always be true, but
                                                      * good to check
                                                      */ {
                                MaxDevicesHandler partnerCfg = assocIter.next();
                                logger.debug("sendAddLinkPartner for {}: {}", partnerCfg.getRfAddress(), devAddr);
                                messageHandler.sendAddLinkPartner(this.devAddr, this, partnerCfg.getRfAddress(),
                                        MaxCulDevice.getDeviceTypeFromThingTypeUID(
                                                partnerCfg.getThing().getThingTypeUID()));
                                /*
                                 * if it's the last association message then wait for
                                 * last ACK
                                 */
                                if (assocIter.hasNext()) {
                                    logger.debug("prepare next sendAddLinkPartner: {}", devAddr);
                                    state = PairingInitialisationState.SENDING_ASSOCIATIONS;
                                } else {
                                    logger.debug("last sendAddLinkPartner waiting for ACK: {}", devAddr);
                                    state = PairingInitialisationState.SENDING_ASSOCIATIONS_ACKED;
                                }
                            } else {
                                // TODO NOTE: if further states are added then ensure
                                // you go to the right state. I.e. when all associations
                                // are done
                                logger.debug("prepare next sendingWeekProfile: {}", devAddr);
                                state = PairingInitialisationState.SENDING_WEEK_PROFILE;
                            }
                        } else {
                            logger.error("SENDING_ASSOCIATIONS was nacked. Ending sequence: {}", devAddr);
                            state = PairingInitialisationState.FINISHED;
                        }
                    } else {
                        logger.error("Received {} when expecting ACK: {}", msg.msgType, msg.srcAddrStr);
                    }
                    break;
                case SENDING_ASSOCIATIONS_ACKED:
                    if (msg.msgType == MaxCulMsgType.ACK) {
                        AckMsg ack = new AckMsg(msg.rawMsg);
                        if (!ack.getIsNack()) {
                            logger.debug("SENDING_ASSOCIATIONS_ACKED received: {}", this.devAddr);
                            if ((this.deviceType == MaxCulDevice.WALL_THERMOSTAT
                                    || this.deviceType == MaxCulDevice.RADIATOR_THERMOSTAT_PLUS)
                                    && !maxDevicesHandler.getWeekProfile().getWeekProfileParts().isEmpty()) {
                                weekProfileIter = maxDevicesHandler.getWeekProfile().getWeekProfileParts().iterator();
                                secondHalf = false;
                                if (weekProfileIter.hasNext()) {
                                    currentWeekProfilePart = weekProfileIter.next();
                                    logger.debug("sendWeekProfile Part 1: {}", devAddr);
                                    messageHandler.sendWeekProfile(devAddr, this, currentWeekProfilePart, secondHalf);
                                    state = PairingInitialisationState.SENDING_WEEK_PROFILE;
                                } else {
                                    logger.debug("Pairing FINISHED: {}", devAddr);
                                    state = PairingInitialisationState.FINISHED;
                                }
                            } else {
                                logger.debug("Pairing FINISHED: {}", devAddr);
                                state = PairingInitialisationState.FINISHED;
                            }
                        } else {
                            logger.error("SENDING_ASSOCIATIONS_ACKED was nacked. Ending sequence");
                            state = PairingInitialisationState.FINISHED;
                        }
                    } else {
                        logger.error("Received {} when expecting ACK: {}", msg.msgType, msg.srcAddrStr);
                    }
                    break;
                case SENDING_WEEK_PROFILE:
                    if (msg.msgType == MaxCulMsgType.ACK) {
                        AckMsg ack = new AckMsg(msg.rawMsg);
                        if (!ack.getIsNack()) {
                            logger.debug("SENDING_WEEK_PROFILE_ACKED received: {}", this.devAddr);
                            // And then the remaining 6
                            if (!secondHalf && currentWeekProfilePart.getControlPoints().size() > 7) {
                                secondHalf = true;
                                logger.debug("sendWeekProfile Part 1+: {}", devAddr);
                                messageHandler.sendWeekProfile(devAddr, this, currentWeekProfilePart, secondHalf);
                                /*
                                 * if it's the last week profile part message then wait for
                                 * last ACK
                                 */
                                if (weekProfileIter.hasNext()) {
                                    logger.debug("prepare next sendingWeekProfile: {}", devAddr);
                                    state = PairingInitialisationState.SENDING_WEEK_PROFILE;
                                } else {
                                    logger.debug("last sendingWeekProfile waiting for ACK: {}", devAddr);
                                    state = PairingInitialisationState.SENDING_WEEK_PROFILE_ACKED;
                                }
                                break;
                            }
                            // First 7 controlpoints
                            secondHalf = false;
                            if (weekProfileIter.hasNext()) {
                                currentWeekProfilePart = weekProfileIter.next();
                                logger.debug("sendWeekProfile Part 1: {}", devAddr);
                                messageHandler.sendWeekProfile(devAddr, this, currentWeekProfilePart, secondHalf);
                                /*
                                 * if it's the last week profile part message then wait for
                                 * last ACK
                                 */
                                if (weekProfileIter.hasNext()) {
                                    state = PairingInitialisationState.SENDING_WEEK_PROFILE;
                                } else {
                                    state = PairingInitialisationState.SENDING_WEEK_PROFILE_ACKED;
                                }
                            } else {
                                // TODO NOTE: if further states are added then ensure
                                // you go to the right state. I.e. when all week profile parts
                                // are done
                                logger.debug("Pairing FINISHED: {}", devAddr);
                                state = PairingInitialisationState.FINISHED;
                            }
                        } else {
                            logger.error("SENDING_ASSOCIATIONS was nacked. Ending sequence: {}", devAddr);
                            state = PairingInitialisationState.FINISHED;
                        }
                    } else {
                        logger.error("Received {} when expecting ACK: {}", msg.msgType, msg.srcAddrStr);
                    }
                    break;
                case SENDING_WEEK_PROFILE_ACKED:
                    if (msg.msgType == MaxCulMsgType.ACK) {
                        AckMsg ack = new AckMsg(msg.rawMsg);
                        if (!ack.getIsNack()) {
                            logger.debug("SENDING_WEEK_PROFILE_ACKED received: {}", this.devAddr);
                            logger.debug("Pairing FINISHED: {}", devAddr);
                            state = PairingInitialisationState.FINISHED;
                        } else {
                            logger.error("SENDING_WEEK_PROFILE was nacked. Ending sequence: {}", devAddr);
                            state = PairingInitialisationState.FINISHED;
                        }
                    } else {
                        logger.error("Received {} when expecting ACK: {}", msg.msgType, msg.srcAddrStr);
                    }
                    break;
                case FINISHED:
                    /* done, do nothing */
                    break;
                case RETX_WAKEUP_ACK:
                    /* here are waiting for an ACK after sending a wakeup message */
                    if (msg.msgType == MaxCulMsgType.ACK) {
                        AckMsg ack = new AckMsg(msg.rawMsg);
                        if (!ack.getIsNack()) {
                            logger.debug("Attempt retransmission - resuming: {}", ack.srcAddrStr);
                            this.useFast = true;
                            messageHandler.sendMessage(reTxMsg);
                            state = reTxState; // resume back to previous state
                        } else {
                            logger.error("WAKEUP for ReTx was nacked. Ending sequence: {}", ack.srcAddrStr);
                            state = PairingInitialisationState.FINISHED;
                        }
                    } else {
                        logger.error("Received {} when expecting ACK: {}", msg.msgType, msg.srcAddrStr);
                    }
                    break;
                default:
                    logger.error("Invalid state for PairingInitialisation Message Sequence!");
                    break;
            }
        } catch (Exception e) {
            logger.warn("Unexpected exception occurs on run", e);
        }
    }

    @Override
    public boolean isComplete() {
        return state == PairingInitialisationState.FINISHED;
    }

    @Override
    public void packetLost(BaseMsg msg) {
        pktLostCount++;
        logger.debug("Lost {} packets", pktLostCount);
        if (pktLostCount < 3) {
            /* send WAKEUP to allow us to send messages in fast mode */
            logger.debug("Attempt retransmission - first wakeup device");
            this.useFast = false;
            messageHandler.sendWakeup(msg.dstAddrStr, this);
            /* save current state, but avoid overwriting on second attempt */
            if (this.state != PairingInitialisationState.RETX_WAKEUP_ACK) {
                this.reTxMsg = msg;
                this.reTxState = state;
            }
            state = PairingInitialisationState.RETX_WAKEUP_ACK;
        } else {
            logger.error("Lost {} packets. Ending Sequence in state {}", pktLostCount, this.state);
            state = PairingInitialisationState.FINISHED;
        }
    }

    @Override
    public boolean useFastSend() {
        // only use fast send when not sending the wakup msg in PONG_ACKED
        return (useFast);
    }
}
