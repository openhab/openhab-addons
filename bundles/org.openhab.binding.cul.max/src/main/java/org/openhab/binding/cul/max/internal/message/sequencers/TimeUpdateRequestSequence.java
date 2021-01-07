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

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.cul.max.internal.handler.MaxCulMsgHandler;
import org.openhab.binding.cul.max.internal.messages.AckMsg;
import org.openhab.binding.cul.max.internal.messages.BaseMsg;
import org.openhab.binding.cul.max.internal.messages.MaxCulProtocolException;
import org.openhab.binding.cul.max.internal.messages.TimeInfoMsg;
import org.openhab.binding.cul.max.internal.messages.constants.MaxCulMsgType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle Time Update requests. Very simple sequence that simply responds to a
 * time request and then handles the response
 *
 * @author Paul Hampson (cyclingengineer) - Initial contribution
 * @author Johannes Goehr (johgoe) - Migration to OpenHab 3.0
 * @since 1.6.0
 */
@NonNullByDefault
public class TimeUpdateRequestSequence implements MessageSequencer {

    private enum TimeUpdateRequestState {
        RESPOND_TO_REQUEST,
        HANDLE_RESPONSE,
        FINISHED
    }

    private final Logger logger = LoggerFactory.getLogger(TimeUpdateRequestSequence.class);

    private static final long TIME_TOLERANCE = 30000; // 30s margin
    TimeUpdateRequestState state = TimeUpdateRequestState.RESPOND_TO_REQUEST;
    private MaxCulMsgHandler messageHandler;
    private int pktLostCount = 0;
    private String tzStr;

    public TimeUpdateRequestSequence(String tz, MaxCulMsgHandler messageHandler) {
        this.tzStr = tz;
        this.messageHandler = messageHandler;
    }

    /**
     * Compare two times and check they are within a certain tolerance
     *
     * @param a
     *            Time A
     * @param b
     *            Time B
     * @param t
     *            Tolerance in milliseconds
     * @return true if within tolerance
     */
    private boolean isValidDeviation(Calendar a, Calendar b, long t) {
        return (Math.abs(a.getTimeInMillis() - b.getTimeInMillis()) <= t);
    }

    @Override
    public void runSequencer(BaseMsg msg) {
        pktLostCount = 0;
        switch (state) {
            case RESPOND_TO_REQUEST:
                if (BaseMsg.getMsgType(msg.rawMsg) == MaxCulMsgType.TIME_INFO) {
                    try {
                        TimeInfoMsg timeMsg = new TimeInfoMsg(msg.rawMsg);
                        Calendar timeInfo = timeMsg.getTimeInfo();
                        if (timeInfo != null && isValidDeviation(timeInfo, new GregorianCalendar(), TIME_TOLERANCE)) {
                            messageHandler.sendAck(msg);
                            state = TimeUpdateRequestState.FINISHED;
                        } else {
                            messageHandler.sendTimeInfo(msg.srcAddrStr, tzStr, this);
                            state = TimeUpdateRequestState.HANDLE_RESPONSE;
                        }
                    } catch (MaxCulProtocolException e) {
                        logger.error("Message was invalid. Continue Time Update Sequence");
                        break;
                    }
                } else {
                    state = TimeUpdateRequestState.FINISHED;
                    logger.debug("Got invalid message type for Time Update Sequence");
                }
                break;
            case HANDLE_RESPONSE:
                /* check for ACK */
                if (msg.msgType == MaxCulMsgType.ACK) {
                    try {
                        AckMsg ack = new AckMsg(msg.rawMsg);
                        if (ack.getIsNack()) {
                            logger.error("TIME_INFO was nacked. Ending sequence");
                        }
                    } catch (MaxCulProtocolException e) {
                        logger.error("Message was invalid. Continue Time Update Sequence");
                        break;
                    }
                }
                state = TimeUpdateRequestState.FINISHED;
                break;
            case FINISHED:
                /* do nothing */
                break;
            default:
                logger.error("Invalid state for TimeUpdate Request Message Sequence!");
                break;

        }
    }

    @Override
    public void packetLost(BaseMsg msg) {
        pktLostCount++;
        logger.debug("Lost {} packets", pktLostCount);
        if (pktLostCount < 3) {
            logger.debug("Attempt retransmission");
            messageHandler.sendMessage(msg);
        } else {
            logger.error("Lost {} packets. Ending Sequence in state {}", pktLostCount, this.state);
            state = TimeUpdateRequestState.FINISHED;
        }
    }

    @Override
    public boolean isComplete() {
        return (state == TimeUpdateRequestState.FINISHED);
    }

    @Override
    public boolean useFastSend() {
        return true;
    }
}
