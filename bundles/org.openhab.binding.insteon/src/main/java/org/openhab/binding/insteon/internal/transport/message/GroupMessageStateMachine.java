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
package org.openhab.binding.insteon.internal.transport.message;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Ideally, Insteon ALL LINK messages are received in this order, and
 * only a single one of each:
 *
 * BCAST (a broadcast message from the device to all group members)
 * CLEAN (a cleanup point-to-point message to ensure more reliable transmission)
 * SUCCESS (a broadcast report of success or failure of cleanup, with cmd1=0x06)
 *
 * But often, the BCAST, CLEAN and SUCCESS messages are retransmitted multiple times,
 * or (less frequently) messages are lost. The present state machine was developed
 * to remove duplicates, yet make sure that a single lost message does not cause
 * the binding to miss an update.
 *
 * @formatter:off
 *                          "SUCCESS"
 *                         EXPECT_BCAST
 *                    ^ /                ^ \
 *           SUCCESS / /                  \ \ [BCAST]
 *                  / /['CLEAN']  'SUCCESS'\ \
 *                 / /                      \ \
 *                / V         CLEAN          \ V
 * "CLEAN" EXPECT_SUCCESS <-------------- EXPECT_CLEAN "BCAST"
 *                         -------------->
 *                            ['BCAST']
 * @formatter:on
 *
 * How to read this diagram:
 *
 * Regular, expected, non-duplicate messages do not have any quotes around them,
 * and lead to the obvious state transitions.
 *
 * The types in [square brackets] are transitions that cause a state
 * update to be published when they occur.
 *
 * The presence of double quotes indicates a duplicate that does not lead
 * to any state transitions, i.e. it is simply ignored.
 *
 * Single quotes indicate a message that is the result of a single dropped
 * message, and leads to a state transition, in some cases even to a state
 * update to be published.
 *
 * For instance at the top of the diagram, if a "SUCCESS" message is received
 * when in state EXPECT_BCAST, it is considered a duplicate (it has "").
 *
 * When in state EXPECT_SUCCESS though, receiving a ['BCAST'] is most likely because
 * the SUCCESS message was missed, and therefore it is considered the result
 * of a single lost message (has '' around it). The state changes to EXPECT_CLEAN,
 * and the message should lead to publishing of a state update (it has [] around it).
 *
 * @author Bernd Pfrommer - Initial contribution
 * @author Rob Nielsen - Port to openHAB 2 insteon binding
 * @author Jeremy Setton - Rewrite insteon binding
 */
@NonNullByDefault
public class GroupMessageStateMachine {
    private static final int GROUP_STATE_TIMEOUT = 10000; // in milliseconds

    /**
     * The different kinds of Insteon ALL Link (Group) messages that can be received.
     * Here is a typical sequence:
     * BCAST:
     * IN:Cmd:0x50|fromAddress:20.AC.99|toAddress:00.00.01|messageFlags:0xCB=ALL_LINK_BROADCAST:3:2|command1:0x13|
     * command2:0x00|
     * CLEAN:
     * IN:Cmd:0x50|fromAddress:20.AC.99|toAddress:23.9B.65|messageFlags:0x41=ALL_LINK_CLEANUP:1:0|command1:0x13|command2
     * :0x01|
     * SUCCESS:
     * IN:Cmd:0x50|fromAddress:20.AC.99|toAddress:13.03.01|messageFlags:0xCB=ALL_LINK_BROADCAST:3:2|command1:0x06|
     * command2:0x00|
     */
    private enum GroupMessageType {
        BCAST,
        CLEAN,
        SUCCESS
    }

    /**
     * The state of the machine (i.e. what message we are expecting next).
     * The usual state should be EXPECT_BCAST
     */
    private enum State {
        EXPECT_BCAST,
        EXPECT_CLEAN,
        EXPECT_SUCCESS
    }

    private State state = State.EXPECT_BCAST;
    private boolean duplicate = false;
    private byte lastCmd1 = 0;
    private long lastTimestamp = 0;

    /**
     * Returns if group message is duplicate
     *
     * @param msg the group message
     * @return true if the group message is duplicate
     * @throws FieldException
     */
    public boolean isDuplicate(Msg msg) throws FieldException {
        byte cmd1 = msg.isAllLinkSuccessReport() ? msg.getInsteonAddress("toAddress").getHighByte()
                : msg.getByte("command1");
        long timestamp = msg.getTimestamp();

        if (cmd1 != lastCmd1 || timestamp != lastTimestamp) {
            GroupMessageType type = msg.isAllLinkSuccessReport() ? GroupMessageType.SUCCESS
                    : msg.isAllLinkCleanup() ? GroupMessageType.CLEAN : GroupMessageType.BCAST;

            update(cmd1, timestamp, type);
        }

        return duplicate;
    }

    /**
     * Updates the state machine
     *
     * @param cmd1 cmd1 from the message received
     * @param timestamp timestamp from the message received
     * @param type the group message type that was received
     */
    private void update(byte cmd1, long timestamp, GroupMessageType type) {
        boolean isNewGroupMsg = cmd1 != lastCmd1 || Math.abs(timestamp - lastTimestamp) > GROUP_STATE_TIMEOUT;

        switch (type) {
            case BCAST:
                switch (state) {
                    case EXPECT_BCAST:
                    case EXPECT_SUCCESS:
                        duplicate = false;
                        break;
                    case EXPECT_CLEAN:
                        duplicate = !isNewGroupMsg;
                        break;
                }
                state = State.EXPECT_CLEAN;
                break;
            case CLEAN:
                switch (state) {
                    case EXPECT_BCAST:
                        duplicate = !isNewGroupMsg;
                        break;
                    case EXPECT_CLEAN:
                    case EXPECT_SUCCESS:
                        duplicate = true;
                        break;
                }
                state = State.EXPECT_SUCCESS;
                break;
            case SUCCESS:
                switch (state) {
                    case EXPECT_BCAST:
                        duplicate = !isNewGroupMsg;
                        break;
                    case EXPECT_CLEAN:
                    case EXPECT_SUCCESS:
                        duplicate = true;
                        break;
                }
                state = State.EXPECT_BCAST;
                break;
        }

        lastCmd1 = cmd1;
        lastTimestamp = timestamp;
    }
}
