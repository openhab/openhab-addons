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
package org.openhab.binding.cul.max.internal.handler;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.cul.CULCommunicationException;
import org.openhab.binding.cul.CULListener;
import org.openhab.binding.cul.max.internal.message.sequencers.MessageSequencer;
import org.openhab.binding.cul.max.internal.messages.*;
import org.openhab.binding.cul.max.internal.messages.constants.MaxCulDevice;
import org.openhab.binding.cul.max.internal.messages.constants.MaxCulMsgType;
import org.openhab.binding.cul.max.internal.messages.constants.ThermostatControlMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle messages going to and from the CUL device. Make sure to intercept
 * control command responses first before passing on valid MAX! messages to the
 * binding itself for processing.
 *
 * @author Paul Hampson (cyclingengineer) - Initial contribution
 * @author Johannes Goehr (johgoe) - Migration to OpenHab 3.0
 * @since 1.6.0
 */
@NonNullByDefault
public class MaxCulMsgHandler implements CULListener {

    private final Logger logger = LoggerFactory.getLogger(MaxCulMsgHandler.class);

    class SenderQueueItem {
        BaseMsg msg;
        @Nullable
        Date expiry;
        int retryCount = 0;

        public SenderQueueItem(BaseMsg msg) {
            this.msg = msg;
        }
    }

    private Date lastTransmit = new Date();
    private Date endOfQueueTransmit;

    private int msgCount = 0;
    private MaxCulCunBridgeHandler cul;
    private String srcAddr;
    private HashMap<Byte, MessageSequencer> sequenceRegister;
    private LinkedList<SenderQueueItem> sendQueue;
    private ConcurrentHashMap<Byte, SenderQueueItem> pendingAckQueue;

    private Map<SenderQueueItem, Timer> sendQueueTimers = new HashMap<SenderQueueItem, Timer>();

    private final Set<String> rfAddressesToSpyOn;

    private boolean listenMode = false;

    public static final int MESSAGE_EXPIRY_PERIOD = 10000;

    public MaxCulMsgHandler(String srcAddr, Set<String> rfAddressesToSpyOn, MaxCulCunBridgeHandler cul) {
        this.srcAddr = srcAddr;
        this.sequenceRegister = new HashMap<Byte, MessageSequencer>();
        this.sendQueue = new LinkedList<SenderQueueItem>();
        this.pendingAckQueue = new ConcurrentHashMap<Byte, SenderQueueItem>();
        this.lastTransmit = new Date(); /* init as now */
        this.endOfQueueTransmit = this.lastTransmit;
        this.rfAddressesToSpyOn = rfAddressesToSpyOn;
        this.cul = cul;
    }

    private byte getMessageCount() {
        this.msgCount += 1;
        this.msgCount &= 0xFF;
        return (byte) this.msgCount;
    }

    private boolean enoughCredit(int requiredCredit, boolean fastSend) {
        int availableCredit = getCreditStatus();
        int preambleCredit = fastSend ? 0 : 100;
        boolean result = (availableCredit >= (requiredCredit + preambleCredit));
        logger.debug("Fast Send? {}, preambleCredit = {}, requiredCredit = {}, availableCredit = {}, enoughCredit? {}",
                fastSend, preambleCredit, requiredCredit, availableCredit, result);
        return result;
    }

    private int getCreditStatus() {
        return cul.getCredit10ms();
    }

    private void transmitMessage(BaseMsg data, @Nullable SenderQueueItem queueItem) {
        try {
            cul.send(data.rawMsg);
        } catch (CULCommunicationException e) {
            logger.error("Unable to send CUL message {} because: {}", data, e.getMessage());
        }
        /* update surplus credit value */
        boolean fastSend = false;
        if (data.isPartOfSequence()) {
            MessageSequencer messageSequencer = data.getMessageSequencer();
            if (messageSequencer != null) {
                fastSend = messageSequencer.useFastSend();
            }
        }
        enoughCredit(data.requiredCredit(), fastSend);
        this.lastTransmit = new Date();
        if (this.endOfQueueTransmit.before(this.lastTransmit)) {
            /* hit a time after the queue finished tx'ing */
            this.endOfQueueTransmit = this.lastTransmit;
        }

        if (data.msgType != MaxCulMsgType.ACK) {
            /* awaiting ack now */
            SenderQueueItem qi = queueItem;
            if (qi == null) {
                qi = new SenderQueueItem(data);
            }
            qi.expiry = new Date(this.lastTransmit.getTime() + MESSAGE_EXPIRY_PERIOD);

            this.pendingAckQueue.put(qi.msg.msgCount, qi);

            /* schedule a check of pending acks */
            TimerTask ackCheckTask = new TimerTask() {
                @Override
                public void run() {
                    checkPendingAcks();
                }
            };
            Timer timer = new Timer();
            timer.schedule(ackCheckTask, qi.expiry);
        }
    }

    public void sendMessage(BaseMsg msg) {
        sendMessage(msg, null);
    }

    /**
     * Send a raw Base Message
     *
     * @param msg Base message to send
     * @param queueItem queue item (used for retransmission)
     */
    private void sendMessage(BaseMsg msg, @Nullable SenderQueueItem queueItem) {
        Timer timer = null;

        if (msg.readyToSend()) {
            if (enoughCredit(msg.requiredCredit(), msg.isFastSend()) && this.sendQueue.isEmpty()) {
                /*
                 * send message as we have enough credit and nothing is on the
                 * queue waiting
                 */
                logger.debug("Sending message immediately. Message is {} => {}", msg.msgType, msg.rawMsg);
                transmitMessage(msg, queueItem);
                logger.debug("Credit required {}", msg.requiredCredit());
            } else {
                /*
                 * message is going on the queue - this means that the device
                 * may well go to standby before it receives it so change into
                 * long slow send format with big preamble
                 */
                msg.setFastSend(false);
                /*
                 * don't have enough credit or there are messages ahead of us so
                 * queue up the item and schedule a task to process it
                 */
                SenderQueueItem qi = queueItem;
                if (qi == null) {
                    qi = new SenderQueueItem(msg);
                }

                TimerTask task = new TimerTask() {
                    @Override
                    public void run() {
                        SenderQueueItem topItem = sendQueue.remove();
                        logger.debug("Checking credit");
                        if (enoughCredit(topItem.msg.requiredCredit(), topItem.msg.isFastSend())) {
                            logger.debug("Sending item from queue. Message is {} => {}", topItem.msg.msgType,
                                    topItem.msg.rawMsg);
                            if (topItem.msg.msgType == MaxCulMsgType.TIME_INFO) {
                                ((TimeInfoMsg) topItem.msg).updateTime();
                            }
                            transmitMessage(topItem.msg, topItem);
                        } else {
                            logger.error("Not enough credit after waiting. This is bad. Queued command is discarded");
                        }
                    }
                };

                timer = new Timer();
                sendQueueTimers.put(qi, timer);
                /*
                 * calculate when we want to TX this item in the queue, with a
                 * margin of 2 credits. x1000 as we accumulate 1 x 10ms credit
                 * every 1000ms
                 */
                int requiredCredit = msg.isFastSend() ? 0 : 100 + msg.requiredCredit() + 2;
                this.endOfQueueTransmit = new Date(this.endOfQueueTransmit.getTime() + (requiredCredit * 1000));
                timer.schedule(task, this.endOfQueueTransmit);
                this.sendQueue.add(qi);

                logger.debug("Added message to queue to be TX'd at {}", this.endOfQueueTransmit.toString());
            }

            if (msg.isPartOfSequence()) {
                /* add to sequence register if part of a sequence */
                logger.debug("Message {} is part of sequence. Adding to register.", msg.msgCount);
                sequenceRegister.put(msg.msgCount, msg.getMessageSequencer());
            }
        } else {
            logger.error("Tried to send a message that wasn't ready!");
        }
    }

    /**
     * Check the ACK queue for any pending acks that have expired
     */
    public void checkPendingAcks() {
        Date now = new Date();

        for (SenderQueueItem qi : pendingAckQueue.values()) {
            if (now.after(qi.expiry)) {
                logger.error("Packet {} ({}) lost - timeout", qi.msg.msgCount, qi.msg.msgType);
                pendingAckQueue.remove(qi.msg.msgCount); // remove from ACK
                // queue
                if (sequenceRegister.containsKey(qi.msg.msgCount)) {
                    /* message sequencer handles failed packet */
                    MessageSequencer msgSeq = sequenceRegister.get(qi.msg.msgCount);
                    if (msgSeq != null) {
                        sequenceRegister.remove(qi.msg.msgCount); // remove from
                        // register
                        // first as
                        // packetLost
                        // could add it
                        // again
                        msgSeq.packetLost(qi.msg);
                    }
                } else if (qi.retryCount < 3) {
                    /* retransmit */
                    qi.retryCount++;
                    logger.debug("Retransmitting packet {} attempt {}", qi.msg.msgCount, qi.retryCount);
                    sendMessage(qi.msg, qi);
                } else {
                    logger.error("Transmission of packet {} failed 3 times, message was: {} to address {} => {}",
                            qi.msg.msgCount, qi.msg.msgType, qi.msg.dstAddrStr, qi.msg.rawMsg);
                }
            }
        }
    }

    private void listenModeHandler(String data) {
        try {
            switch (BaseMsg.getMsgType(data)) {
                case WALL_THERMOSTAT_CONTROL:
                    new WallThermostatControlMsg(data).printMessage();
                    break;
                case TIME_INFO:
                    new TimeInfoMsg(data).printMessage();
                    break;
                case SET_TEMPERATURE:
                    new SetTemperatureMsg(data).printMessage();
                    break;
                case ACK:
                    new AckMsg(data).printMessage();
                    break;
                case PAIR_PING:
                    new PairPingMsg(data).printMessage();
                    break;
                case PAIR_PONG:
                    new PairPongMsg(data).printMessage();
                    break;
                case THERMOSTAT_STATE:
                    new ThermostatStateMsg(data).printMessage();
                    break;
                case SET_GROUP_ID:
                    new SetGroupIdMsg(data).printMessage();
                    break;
                case WAKEUP:
                    new WakeupMsg(data).printMessage();
                    break;
                case WALL_THERMOSTAT_STATE:
                    new WallThermostatStateMsg(data).printMessage();
                    break;
                case PUSH_BUTTON_STATE:
                    new PushButtonMsg(data).printMessage();
                    break;
                case SHUTTER_CONTACT_STATE:
                    new ShutterContactStateMsg(data).printMessage();
                    break;
                case ADD_LINK_PARTNER:
                case CONFIG_TEMPERATURES:
                case CONFIG_VALVE:
                case CONFIG_WEEK_PROFILE:
                case REMOVE_GROUP_ID:
                case REMOVE_LINK_PARTNER:
                case RESET:
                case SET_COMFORT_TEMPERATURE:
                case SET_DISPLAY_ACTUAL_TEMP:
                case SET_ECO_TEMPERATURE:
                case UNKNOWN:
                default:
                    BaseMsg baseMsg = new BaseMsg(data);
                    baseMsg.printMessage();
                    break;

            }
        } catch (MaxCulProtocolException e) {
            logger.warn("'{}' violates Max! CUL protocol.", data);
        }
    }

    @Override
    public synchronized void dataReceived(String data) {
        try {
            logger.debug("MaxCulSender Received {}", data);
            if (data.startsWith("Z")) {
                if (listenMode) {
                    listenModeHandler(data);
                    return;
                }

                /* Check message is destined for us */
                if (BaseMsg.isForUs(data, srcAddr)) {
                    boolean passToBinding = true;
                    /* Handle Internal Messages */
                    MaxCulMsgType msgType = BaseMsg.getMsgType(data);
                    if (msgType == MaxCulMsgType.ACK) {
                        passToBinding = false;

                        AckMsg msg = new AckMsg(data);

                        if (pendingAckQueue.containsKey(msg.msgCount) && msg.dstAddrStr.compareTo(srcAddr) == 0) {
                            SenderQueueItem qi = pendingAckQueue.remove(msg.msgCount);
                            /* verify ACK */
                            if (qi != null && (qi.msg.dstAddrStr.equalsIgnoreCase(msg.srcAddrStr))
                                    && (qi.msg.srcAddrStr.equalsIgnoreCase(msg.dstAddrStr))) {
                                if (msg.getIsNack()) {
                                    /* NAK'd! */
                                    // TODO resend?
                                    logger.error("Message was NAK'd, packet lost");
                                } else {
                                    logger.debug("Message {} ACK'd ok!", msg.msgCount);
                                    try {
                                        forwardAsBroadcastIfSpyIsEnabled(data);
                                    } catch (Exception e1) {
                                        logger.warn("Could not forward msg {}", data, e1);
                                    }
                                }
                            }
                        } else {
                            logger.info("Got ACK for message {} but it wasn't in the queue", msg.msgCount);
                        }

                    }
                    BaseMsg baseMsg = new BaseMsg(data);
                    if (sequenceRegister.containsKey(baseMsg.msgCount)) {
                        passToBinding = false;
                        /*
                         * message found in sequence register, so it will be handled
                         * by the sequence
                         */
                        BaseMsg bMsg = baseMsg;
                        logger.debug("Message {} is part of sequence. Running next step in sequence.", bMsg.msgCount);
                        MessageSequencer messageSequencer = sequenceRegister.get(bMsg.msgCount);
                        if (messageSequencer != null) {
                            messageSequencer.runSequencer(bMsg);
                            sequenceRegister.remove(bMsg.msgCount);
                        }
                    }

                    if (passToBinding) {
                        /* pass data to binding for processing */
                        this.cul.maxCulMsgReceived(data, false);
                    }
                } else if (BaseMsg.isForUs(data, "000000")) {
                    switch (BaseMsg.getMsgType(data)) {
                        case PAIR_PING:
                        case WALL_THERMOSTAT_CONTROL:
                        case THERMOSTAT_STATE:
                        case WALL_THERMOSTAT_STATE:
                            this.cul.maxCulMsgReceived(data, true);
                            break;
                        default:
                            logger.debug("Unhandled broadcast message of type {}", BaseMsg.getMsgType(data).toString());
                            break;

                    }
                } else {
                    forwardAsBroadcastIfSpyIsEnabled(data);
                }
            }
        } catch (MaxCulProtocolException e) {
            logger.warn("Invalid message received: '{}'", data);
        }
    }

    private void forwardAsBroadcastIfSpyIsEnabled(String data) {
        try {
            BaseMsg bMsg = new BaseMsg(data);
            if (rfAddressesToSpyOn.contains(bMsg.srcAddrStr) && (BaseMsg.getMsgType(data) != MaxCulMsgType.PAIR_PING)) {
                /*
                 * pass data to binding for processing - pretend it is
                 * broadcast so as not to ACK
                 */
                this.cul.maxCulMsgReceived(data, true);
            }
        } catch (MaxCulProtocolException e) {
            logger.warn("Invalid message received: '{}'", data);
        }
    }

    @Override
    public void error(Exception e) {
        /*
         * Ignore errors for now - not sure what I would need to handle here at
         * the moment
         */
        logger.error("Received CUL Error", e);
    }

    public void startSequence(MessageSequencer ps, BaseMsg msg) {
        logger.debug("Starting sequence");
        ps.runSequencer(msg);
    }

    /**
     * Send response to PairPing as part of a message sequence
     *
     * @param dstAddr Address of device to respond to
     * @param msgSeq Message sequence to associate
     */
    public void sendPairPong(String dstAddr, @Nullable MessageSequencer msgSeq) {
        PairPongMsg pp = new PairPongMsg(getMessageCount(), (byte) 0, (byte) 0, this.srcAddr, dstAddr);
        pp.setMessageSequencer(msgSeq);
        sendMessage(pp);
    }

    public void sendPairPong(String dstAddr) {
        sendPairPong(dstAddr, null);
    }

    /**
     * Send a wakeup message as part of a message sequence
     *
     * @param devAddr Address of device to respond to
     * @param msgSeq Message sequence to associate
     */
    public void sendWakeup(String devAddr, MessageSequencer msgSeq) {
        WakeupMsg msg = new WakeupMsg(getMessageCount(), (byte) 0x0, (byte) 0, this.srcAddr, devAddr);
        msg.setMessageSequencer(msgSeq);
        sendMessage(msg);
    }

    /**
     * Send time information to device that has requested it as part of a
     * message sequence
     *
     * @param dstAddr Address of device to respond to
     * @param tzStr Time Zone String
     * @param msgSeq Message sequence to associate
     */
    public void sendTimeInfo(String dstAddr, String tzStr, @Nullable MessageSequencer msgSeq) {
        TimeInfoMsg msg = new TimeInfoMsg(getMessageCount(), (byte) 0x0, (byte) 0, this.srcAddr, dstAddr, tzStr);
        msg.setMessageSequencer(msgSeq);
        sendMessage(msg);
    }

    /**
     * Send time information to device in fast mode
     *
     * @param dstAddr Address of device to respond to
     * @param tzStr Time Zone String
     */
    public void sendTimeInfoFast(String dstAddr, String tzStr) {
        TimeInfoMsg msg = new TimeInfoMsg(getMessageCount(), (byte) 0x0, (byte) 0, this.srcAddr, dstAddr, tzStr);
        msg.setFastSend(true);
        sendMessage(msg);
    }

    /**
     * Send time information to device that has requested it
     *
     * @param dstAddr Address of device to respond to
     * @param tzStr Time Zone String
     */
    public void sendTimeInfo(String dstAddr, String tzStr) {
        sendTimeInfo(dstAddr, tzStr, null);
    }

    /**
     * Set the group ID on a device
     *
     * @param devAddr Address of device to set group ID on
     * @param group_id Group id to set
     * @param msgSeq Message sequence to associate
     */
    public void sendSetGroupId(String devAddr, byte group_id, MessageSequencer msgSeq) {
        SetGroupIdMsg msg = new SetGroupIdMsg(getMessageCount(), (byte) 0x0, this.srcAddr, devAddr, group_id);
        msg.setMessageSequencer(msgSeq);
        sendMessage(msg);
    }

    /**
     * Send an ACK response to a message
     *
     * @param msg Message we are acking
     */
    public void sendAck(BaseMsg msg) {
        AckMsg ackMsg = new AckMsg(msg.msgCount, (byte) 0x0, msg.groupid, this.srcAddr, msg.srcAddrStr, false);
        ackMsg.setFastSend(true); // all ACKs are sent to waiting device.
        sendMessage(ackMsg);
    }

    /**
     * Send an NACK response to a message
     *
     * @param msg Message we are nacking
     */
    public void sendNack(BaseMsg msg) {
        AckMsg nackMsg = new AckMsg(msg.msgCount, (byte) 0x0, msg.groupid, this.srcAddr, msg.srcAddrStr, false);
        nackMsg.setFastSend(true); // all NACKs are sent to waiting device.
        sendMessage(nackMsg);
    }

    /**
     * Send a set temperature message
     *
     * @param devAddr Radio addr of device
     * @param mode Mode to set e.g. AUTO or MANUAL
     * @param temp Temperature value to send
     */
    public void sendSetTemperature(String devAddr, ThermostatControlMode mode, double temp) {
        if (ThermostatControlMode.UNKOWN == mode) {
            logger.warn("ThermostatControlMode.UNKOWN is not supported. Skip set temperature to {}.", temp);
            return;
        }
        SetTemperatureMsg msg = new SetTemperatureMsg(getMessageCount(), (byte) 0x0, (byte) 0x0, this.srcAddr, devAddr,
                temp, mode);
        sendMessage(msg);
    }

    /**
     * Send a set eco temperature message
     *
     * @param devAddr Radio addr of device
     */
    public void sendSetEcoTemperature(String devAddr) {
        SetEcoTempMsg msg = new SetEcoTempMsg(getMessageCount(), (byte) 0x0, (byte) 0x0, this.srcAddr, devAddr);
        sendMessage(msg);
    }

    /**
     * Send a set comfort temperature message
     *
     * @param devAddr Radio addr of device
     */
    public void sendSetComfortTemperature(String devAddr) {
        SetComfortTempMsg msg = new SetComfortTempMsg(getMessageCount(), (byte) 0x0, (byte) 0x0, this.srcAddr, devAddr);
        sendMessage(msg);
    }

    /**
     * Send week profile
     *
     * @param devAddr Radio addr of device
     * @param msgSeq Message sequencer to associate with this message
     * @param weekProfilePart week profile value
     * @param secondHalf flag if the control points > 7 should be send
     */
    public void sendWeekProfile(String devAddr, MessageSequencer msgSeq, MaxCulWeekProfilePart weekProfilePart,
            boolean secondHalf) {
        ConfigWeekProfileMsg cfgWeekProfileMsg = new ConfigWeekProfileMsg(getMessageCount(), (byte) 0, (byte) 0,
                this.srcAddr, devAddr, weekProfilePart, secondHalf);
        cfgWeekProfileMsg.setMessageSequencer(msgSeq);
        sendMessage(cfgWeekProfileMsg);
    }

    /**
     * Send temperature configuration message
     *
     * @param devAddr Radio addr of device
     * @param msgSeq Message sequencer to associate with this message
     * @param comfortTemp comfort temperature value
     * @param ecoTemp Eco temperature value
     * @param maxTemp Maximum Temperature value
     * @param minTemp Minimum temperature value
     * @param offset Offset Temperature value
     * @param windowOpenTemp Window open temperature value
     * @param windowOpenTime Window open time value
     */
    public void sendConfigTemperatures(String devAddr, MessageSequencer msgSeq, double comfortTemp, double ecoTemp,
            double maxTemp, double minTemp, double offset, double windowOpenTemp, double windowOpenTime) {
        ConfigTemperaturesMsg cfgTempMsg = new ConfigTemperaturesMsg(getMessageCount(), (byte) 0, (byte) 0,
                this.srcAddr, devAddr, comfortTemp, ecoTemp, maxTemp, minTemp, offset, windowOpenTemp, windowOpenTime);
        cfgTempMsg.setMessageSequencer(msgSeq);
        sendMessage(cfgTempMsg);
    }

    /**
     * Link one device to another
     *
     * @param devAddr Destination device address
     * @param msgSeq Associated message sequencer
     * @param partnerAddr Radio address of partner
     * @param devType Type of device
     */
    public void sendAddLinkPartner(String devAddr, MessageSequencer msgSeq, String partnerAddr, MaxCulDevice devType) {
        AddLinkPartnerMsg addLinkMsg = new AddLinkPartnerMsg(getMessageCount(), (byte) 0, (byte) 0, this.srcAddr,
                devAddr, partnerAddr, devType);
        addLinkMsg.setMessageSequencer(msgSeq);
        sendMessage(addLinkMsg);
    }

    /**
     * Send a reset message to device
     *
     * @param devAddr Address of device to reset
     */
    public void sendReset(String devAddr) {
        ResetMsg resetMsg = new ResetMsg(getMessageCount(), (byte) 0, (byte) 0, this.srcAddr, devAddr);
        sendMessage(resetMsg);
    }

    /**
     * Set listen mode status. Doing this will stop proper message processing
     * and will just turn this message handler into a snooper.
     *
     * @param listenModeOn TRUE sets listen mode to ON
     */
    public void setListenMode(boolean listenModeOn) {
        listenMode = listenModeOn;
        logger.debug("Listen Mode is {}", (listenMode ? "ON" : "OFF"));
    }

    public boolean getListenMode() {
        return listenMode;
    }

    public void sendSetDisplayActualTemp(String devAddr, boolean displayActualTemp) {
        SetDisplayActualTempMsg displaySettingMsg = new SetDisplayActualTempMsg(getMessageCount(), (byte) 0, (byte) 0,
                this.srcAddr, devAddr, displayActualTemp);
        sendMessage(displaySettingMsg);
    }
}
