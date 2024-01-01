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
package org.openhab.binding.souliss.internal.protocol;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;
import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.souliss.internal.SoulissBindingConstants;
import org.openhab.binding.souliss.internal.SoulissUDPConstants;
import org.openhab.binding.souliss.internal.handler.SoulissGatewayHandler;
import org.openhab.binding.souliss.internal.handler.SoulissGenericHandler;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provide to take packet, and send it to regular interval to Souliss
 * Network
 *
 * @author Tonino Fazio - Initial contribution
 * @author Luca Calcaterra - Refactor for OH3
 */
@NonNullByDefault
public class SendDispatcherRunnable implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(SendDispatcherRunnable.class);

    private @Nullable SoulissGatewayHandler gwHandler;
    static boolean bPopSuspend = false;
    protected static ArrayList<PacketStruct> packetsList = new ArrayList<>();
    private long startTime = System.currentTimeMillis();
    static int iDelay = 0; // equal to 0 if array is empty
    static int sendMinDelay = 0;

    public SendDispatcherRunnable(Bridge bridge) {
        this.gwHandler = (SoulissGatewayHandler) bridge.getHandler();
    }

    /**
     * Put packet to send in ArrayList PacketList
     */
    public static synchronized void put(DatagramPacket packetToPUT, Logger logger) {
        bPopSuspend = true;
        var bPacchettoGestito = false;
        // I extract the node addressed by the incoming packet. returns -1 if the package is not of the
        // SOULISS_UDP_FUNCTION_FORCE type
        int node = getNode(packetToPUT);
        if (node >= 0) {
            logger.debug("Push packet in queue - Node {}", node);
        }

        if (packetsList.isEmpty() || node < 0) {
            bPacchettoGestito = false;
        } else {
            // OPTIMIZER
            // scan packets list to sent
            for (var i = 0; i < packetsList.size(); i++) {
                if (node >= 0 && getNode(packetsList.get(i).getPacket()) == node && !packetsList.get(i).getSent()) {
                    // frame for the same node already present in the list
                    logger.debug("Frame UPD per nodo {} già presente in coda. Esecuzione ottimizzazione.", node);
                    bPacchettoGestito = true;
                    // if the packet to be inserted is shorter (or equal) than the one in the queue
                    // then I overwrite the bytes of the packet present in the queue
                    if (packetToPUT.getData().length <= packetsList.get(i).getPacket().getData().length) {
                        // it scrolls the command bytes and if the byte is non-zero overwrites the byte present in the
                        // queued packet
                        logger.trace("Optimizer. Packet to push: {}", macacoToString(packetToPUT.getData()));
                        logger.trace("Optimizer. Previous frame: {}",
                                macacoToString(packetsList.get(i).getPacket().getData()));
                        // typical values ​​start from byte 12 onwards
                        for (var j = 12; j < packetToPUT.getData().length; j++) {
                            // if the j-th byte is different from zero then
                            // I overwrite it with the byte of the packet already present
                            if (packetToPUT.getData()[j] != 0) {
                                packetsList.get(i).getPacket().getData()[j] = packetToPUT.getData()[j];
                            }
                        }
                        logger.debug("Optimizer. Previous frame modified to: {}",
                                macacoToString(packetsList.get(i).getPacket().getData()));
                    } else {
                        // if the packet to be inserted is longer than the one in the list then
                        // I overwrite the bytes of the packet to be inserted, then I delete the one in the list
                        // and insert the new one
                        if (packetToPUT.getData().length > packetsList.get(i).getPacket().getData().length) {
                            for (var j = 12; j < packetsList.get(i).getPacket().getData().length; j++) {
                                // if the j-th byte is different from zero then I overwrite it with the byte of the
                                // packet already present
                                if ((packetsList.get(i).getPacket().getData()[j] != 0)
                                        && (packetToPUT.getData()[j] == 0)) {
                                    // overwrite the bytes of the last frame
                                    // only if the byte equals zero.
                                    // If the last frame is nonzero
                                    // takes precedence and must override
                                    packetToPUT.getData()[j] = packetsList.get(i).getPacket().getData()[j];

                                }
                            }
                            // removes the packet
                            logger.debug("Optimizer. Remove frame: {}",
                                    macacoToString(packetsList.get(i).getPacket().getData()));
                            packetsList.remove(i);
                            // inserts the new
                            logger.debug("Optimizer. Add frame: {}", macacoToString(packetToPUT.getData()));
                            packetsList.add(new PacketStruct(packetToPUT));
                        }
                    }
                }
            }
        }

        if (!bPacchettoGestito) {
            logger.debug("Add packet: {}", macacoToString(packetToPUT.getData()));
            packetsList.add(new PacketStruct(packetToPUT));
        }
        bPopSuspend = false;
    }

    @Override
    public void run() {
        DatagramSocket sender = null;

        try (var channel = DatagramChannel.open()) {
            if (checkTime()) {
                PacketStruct sp = pop();
                if (sp != null) {
                    logger.debug(
                            "SendDispatcherJob - Functional Code 0x{} - Packet: {} - Elementi rimanenti in lista: {}",
                            Integer.toHexString(sp.getPacket().getData()[7]), macacoToString(sp.getPacket().getData()),
                            packetsList.size());

                    sender = channel.socket();
                    sender.setReuseAddress(true);
                    sender.setBroadcast(true);

                    var localGwHandler = this.gwHandler;
                    if (localGwHandler != null) {
                        var sa = new InetSocketAddress(localGwHandler.getGwConfig().preferredLocalPortNumber);
                        sender.bind(sa);
                        sender.send(sp.getPacket());
                    }
                }

                // compare the states in memory with the frames sent.
                // If match deletes the frame from the sent list
                safeSendCheck();

                resetTime();
            }
        } catch (Exception e) {
            logger.warn("{}", e.getMessage());
        } finally {
            if (sender != null && !sender.isClosed()) {
                sender.close();
            }
        }
    }

    /**
     * Get node number from packet
     */
    private static int getNode(DatagramPacket packet) {
        // 7 is the byte of the VNet frame at which I find the command code
        // 10 is the byte of the VNet frame at which I find the node ID
        if (packet.getData()[7] == SoulissUDPConstants.SOULISS_UDP_FUNCTION_FORCE) {
            return packet.getData()[10];
        }
        return -1;
    }

    private static String macacoToString(byte[] frame2) {
        byte[] frame = frame2.clone();
        var sb = new StringBuilder();
        sb.append("HEX: [");
        for (byte b : frame) {
            sb.append(String.format("%02X ", b));
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * check frame updates with packetList, where flag "sent" is true. If all
     * commands was executed there delete packet in list.
     */
    public void safeSendCheck() {
        int node;
        int iSlot;
        SoulissGenericHandler localTyp;
        var sCmd = "";
        byte bExpected;

        var sExpected = "";

        // scan of the sent packets list
        for (var i = 0; i < packetsList.size(); i++) {

            if (packetsList.get(i).getSent()) {
                node = getNode(packetsList.get(i).getPacket());
                iSlot = 0;
                for (var j = 12; j < packetsList.get(i).getPacket().getData().length; j++) {
                    // I check the slot only if the command is different from ZERO
                    if ((packetsList.get(i).getPacket().getData()[j] != 0) && (this.gwHandler != null)) {
                        localTyp = getHandler(node, iSlot, this.logger);

                        if (localTyp != null) {
                            bExpected = localTyp.getExpectedRawState(packetsList.get(i).getPacket().getData()[j]);

                            // if the expected value of the typical is -1 then it means that the typical does not
                            // support the
                            // function
                            // secureSend
                            if (bExpected < 0) {
                                localTyp = null;
                            }

                            // translate the command sent with the expected state e
                            // then compare with the current state
                            if (logger.isDebugEnabled() && localTyp != null) {
                                sCmd = Integer.toHexString(packetsList.get(i).getPacket().getData()[j]);
                                // command sent
                                sCmd = sCmd.length() < 2 ? "0x0" + sCmd.toUpperCase() : "0x" + sCmd.toUpperCase();
                                sExpected = Integer.toHexString(bExpected);
                                sExpected = sExpected.length() < 2 ? "0x0" + sExpected.toUpperCase()
                                        : "0x" + sExpected.toUpperCase();
                                logger.debug(
                                        "Compare. Node: {} Slot: {} Node Name: {} Command: {} Expected Souliss State: {} - Actual OH item State: {}",
                                        node, iSlot, localTyp.getLabel(), sCmd, sExpected, localTyp.getRawState());
                            }

                            if (localTyp != null && checkExpectedState(localTyp.getRawState(), bExpected)) {
                                // if the value of the typical matches the value
                                // transmitted then I set the byte to zero.
                                // when all bytes are equal to zero then
                                // delete the frame
                                packetsList.get(i).getPacket().getData()[j] = 0;
                                logger.debug("{} Node: {} Slot: {} - OK Expected State", localTyp.getLabel(), node,
                                        iSlot);
                            } else if (localTyp == null) {
                                if (bExpected < 0) {
                                    // if the typical is not managed then I set the byte of the relative slot to zero
                                    packetsList.get(i).getPacket().getData()[j] = 0;
                                } else {
                                    // if there is no typical at slot j then it means that it is one
                                    // slot
                                    // connected
                                    // to the previous one (ex: RGB, T31, ...)
                                    // then if slot j-1 = 0 then j can also be set to 0
                                    if (packetsList.get(i).getPacket().getData()[j - 1] == 0) {
                                        packetsList.get(i).getPacket().getData()[j] = 0;
                                    }
                                }

                            }
                        }
                    }
                    iSlot++;
                }

                // if the value of all bytes that make up the packet is 0 then I remove the packet from
                // list
                // also if the timout has elapsed then I set the packet to be resent
                if (checkAllsSlotZero(packetsList.get(i).getPacket())) {
                    logger.debug("Command packet executed - Removed");
                    packetsList.remove(i);
                } else {
                    // if the frame is not equal to zero I check the TIMEOUT and if
                    // it has expired so I set the SENT flag to false
                    long time = System.currentTimeMillis();

                    SoulissGatewayHandler localGwHandler = this.gwHandler;
                    if (localGwHandler != null) {
                        if ((localGwHandler.getGwConfig().timeoutToRequeue < time - packetsList.get(i).getTime())
                                && (localGwHandler.getGwConfig().timeoutToRemovePacket < time
                                        - packetsList.get(i).getTime())) {
                            logger.debug("Packet Execution timeout - Removed");
                            packetsList.remove(i);
                        } else {
                            logger.debug("Packet Execution timeout - Requeued");
                            packetsList.get(i).setSent(false);
                        }

                    }
                }
            }
        }
    }

    private @Nullable SoulissGenericHandler getHandler(int node, int slot, Logger logger) {
        SoulissGatewayHandler localGwHandler = this.gwHandler;

        Iterator<Thing> thingsIterator;
        if (localGwHandler != null) {
            thingsIterator = localGwHandler.getThing().getThings().iterator();
            Thing typ = null;
            while (thingsIterator.hasNext()) {
                typ = thingsIterator.next();
                if (typ.getThingTypeUID().equals(SoulissBindingConstants.TOPICS_THING_TYPE)) {
                    continue;
                }
                SoulissGenericHandler handler = (SoulissGenericHandler) typ.getHandler();

                // execute it only if binding is Souliss and update is for my
                // Gateway
                if ((handler != null) && (handler.getNode() == node && handler.getSlot() == slot)) {
                    return handler;
                }
            }
        }
        return null;
    }

    private static boolean checkExpectedState(byte itemState, byte expectedState) {
        // if expected state is null than return true. The frame will not requeued
        if (expectedState <= -1) {
            return true;
        }
        return itemState == expectedState;
    }

    private static boolean checkAllsSlotZero(DatagramPacket packet) {
        var bflag = true;
        for (var j = 12; j < packet.getData().length; j++) {
            if ((packet.getData()[j] != 0)) {
                bflag = false;
            }
        }
        return bflag;
    }

    long t = 0;
    long tPrec = 0;

    /**
     * Pop SocketAndPacket from ArrayList PacketList
     */
    @Nullable
    private synchronized PacketStruct pop() {
        synchronized (this) {
            SoulissGatewayHandler localGwHandler = this.gwHandler;

            // don't pop if bPopSuspend = true
            // bPopSuspend is set by the put method
            if ((localGwHandler != null) && (!bPopSuspend)) {
                t = System.currentTimeMillis();

                // brings the interval to the minimum only if:
                // the length of the tail less than or equal to 1;
                // if the SEND_DELAY time has elapsed.

                if (packetsList.size() <= 1) {
                    iDelay = sendMinDelay;
                } else {
                    iDelay = localGwHandler.getGwConfig().sendInterval;

                }

                var iPacket = 0;
                var bFlagWhile = true;
                // discard packages already sent
                while ((iPacket < packetsList.size()) && bFlagWhile) {
                    if (packetsList.get(iPacket).getSent()) {
                        iPacket++;
                    } else {
                        bFlagWhile = false;
                    }
                }

                boolean tFlag = (t - tPrec) >= localGwHandler.getGwConfig().sendInterval;

                // if we have reached the end of the list and then all
                // packets have already been sent so I also place the tFlag
                // to false (as if the timeout hasn't elapsed yet)
                if (iPacket >= packetsList.size()) {
                    tFlag = false;
                }

                if ((!packetsList.isEmpty()) && tFlag) {
                    tPrec = System.currentTimeMillis();

                    // extract the first element of the list
                    PacketStruct sp = packetsList.get(iPacket);

                    // PACKAGE MANAGEMENT: deleted from the list or
                    // marked as sent if it is a FORCE
                    if (packetsList.get(iPacket).getPacket()
                            .getData()[7] == SoulissUDPConstants.SOULISS_UDP_FUNCTION_FORCE) {
                        // flag sent set to true
                        packetsList.get(iPacket).setSent(true);
                        // set time
                        packetsList.get(iPacket).setTime(System.currentTimeMillis());
                    } else {
                        packetsList.remove(iPacket);
                    }

                    logger.debug("POP: {} packets in memory", packetsList.size());
                    if (logger.isDebugEnabled()) {
                        var iPacketSentCounter = 0;
                        var i = 0;
                        while ((i < packetsList.size())) {
                            if (packetsList.get(i).getSent()) {
                                iPacketSentCounter++;
                            }
                            i++;
                        }
                        logger.debug("POP: {}  force frame sent", iPacketSentCounter);
                    }

                    logger.debug("Pop frame {} - Delay for 'SendDispatcherThread' setted to {} mills.",
                            macacoToString(sp.getPacket().getData()), iDelay);
                    return sp;
                }
            }

        }
        return null;
    }

    private void resetTime() {
        startTime = System.currentTimeMillis();
    }

    private boolean checkTime() {
        return startTime < (System.currentTimeMillis() - iDelay);
    }
}
