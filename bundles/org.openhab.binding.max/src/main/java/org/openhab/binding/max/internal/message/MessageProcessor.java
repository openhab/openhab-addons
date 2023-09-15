/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.max.internal.message;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.max.internal.exceptions.IncompleteMessageException;
import org.openhab.binding.max.internal.exceptions.IncorrectMultilineIndexException;
import org.openhab.binding.max.internal.exceptions.MessageIsWaitingException;
import org.openhab.binding.max.internal.exceptions.NoMessageAvailableException;
import org.openhab.binding.max.internal.exceptions.UnprocessableMessageException;
import org.openhab.binding.max.internal.exceptions.UnsupportedMessageTypeException;

/**
 * The message processor was introduced to combine multiple received lines to
 * one single message. There are cases, when the MAX! Cube sends multiple
 * messages (M-Message for example). The message processor acts as stack for
 * received messages. Every received line should be added to the processor.
 * After every added line, the message processor analyses the line. It is not
 * possible to add additional lines when there is a message ready to be
 * processed.
 *
 * @author Christian Rockrohr <christian@rockrohr.de> - Initial contribution
 */
@NonNullByDefault
public class MessageProcessor {

    public static final String SEPARATOR = ":";

    /**
     * The message that was created from last line received. (Null if no message
     * available yet)
     */

    private @Nullable Message currentMessage;

    /**
     * <pre>
     * If more that one single line is required to create a message
     *    numberOfRequiredLines holds the number of required messages to complete
     *    receivedLines holds the lines received so far
     *    currentMessageType indicates which message type is currently on stack
     * </pre>
     */
    private @Nullable Integer numberOfRequiredLines;
    private List<String> receivedLines = new ArrayList<>();
    private @Nullable MessageType currentMessageType;

    /**
     * Resets the current status and processed lines. Should be used after
     * processing a message
     */
    public void reset() {
        this.currentMessage = null;
        receivedLines.clear();
        currentMessageType = null;
        numberOfRequiredLines = null;
    }

    /**
     * Analyses the line and creates a message when possible. If the line
     * indicates, that additional lines are required to create a complete
     * message, the message processor keeps the line in memory and awaits
     * additional lines. If the new line does not fit into current state
     * (incomplete M: message on stack but L: message line received) a
     * IncompleteMessageException is thrown.
     *
     * @param line
     *            is the new line received
     * @return true if a message could be created by this line, false in any
     *         other cases (line was stacked, error, ...)
     * @throws MessageIsWaitingException
     *             when a line was added without pulling the previous message
     * @throws IncompleteMessageException
     *             when a line was added that does not belong to current message
     *             stack
     * @throws UnsupportedMessageTypeException
     *             in case the line starts with an unknown message indicator
     * @throws UnprocessableMessageException
     *             is thrown when there was a known message indicator found, but
     *             message could not be parsed correctly.
     * @throws IncorrectMultilineIndexException
     */
    public Boolean addReceivedLine(String line) throws IncompleteMessageException, MessageIsWaitingException,
            UnsupportedMessageTypeException, UnprocessableMessageException, IncorrectMultilineIndexException {
        if (this.currentMessage != null) {
            throw new MessageIsWaitingException();
        }

        MessageType messageType = getMessageType(line);

        if (messageType == null) {
            throw new UnsupportedMessageTypeException();
        }

        if ((this.currentMessageType != null) && (!messageType.equals(this.currentMessageType))) {
            throw new IncompleteMessageException();
        }

        Boolean result = true;

        switch (messageType) {
            case H:
                this.currentMessage = new HMessage(line);
                break;
            case C:
                this.currentMessage = new CMessage(line);
                break;
            case L:
                this.currentMessage = new LMessage(line);
                break;
            case S:
                this.currentMessage = new SMessage(line);
                break;
            case M:
                result = handleMMessageLine(line);
                break;
            case N:
                this.currentMessage = new NMessage(line);
                break;
            case F:
                this.currentMessage = new FMessage(line);
                break;
            case A:
                this.currentMessage = new AMessage(line);
                break;
            default:
        }

        return result;
    }

    private Boolean handleMMessageLine(String line)
            throws UnprocessableMessageException, IncompleteMessageException, IncorrectMultilineIndexException {
        Boolean result = false;

        String[] tokens = line.split(Message.DELIMETER); // M:00,01,xyz.....

        try {
            Integer index = Integer.valueOf(tokens[0].replaceFirst("M:", "")); // M:00
            Integer counter = Integer.valueOf(tokens[1]); // 01

            if (this.numberOfRequiredLines == null) {
                switch (counter) {
                    case 0:
                        throw new UnprocessableMessageException();
                    case 1:
                        this.currentMessage = new MMessage(line);
                        result = true;
                        break;
                    default:
                        this.numberOfRequiredLines = counter;
                        this.currentMessageType = MessageType.M;
                        if (index == 0) {
                            this.receivedLines.add(line);
                        } else {
                            throw new IncorrectMultilineIndexException();
                        }
                }
            } else {
                if (!counter.equals(this.numberOfRequiredLines) || index != this.receivedLines.size()) {
                    throw new IncorrectMultilineIndexException();
                }

                receivedLines.add(tokens[2]);

                if (index + 1 == receivedLines.size()) {
                    String newLine = "";
                    for (String curLine : receivedLines) {
                        newLine += curLine;
                    }
                    this.currentMessage = new MMessage(newLine);
                    result = true;
                }
            }
        } catch (IncorrectMultilineIndexException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new UnprocessableMessageException();
        }

        return result;
    }

    /**
     * @return true if there is a message waiting to be pulled
     */
    public boolean isMessageAvailable() {
        return this.currentMessage != null;
    }

    /**
     * Pulls the message from the stack when there is one available. This needs
     * to be done before next line can be added into message processor. When
     * message is pulled, the message processor is reseted and ready to process
     * next line.
     *
     * @return Message
     * @throws NoMessageAvailableException
     *             when there was no message on the stack
     */
    @Nullable
    public Message pull() throws NoMessageAvailableException {
        final Message result = this.currentMessage;
        if (this.currentMessage == null) {
            throw new NoMessageAvailableException();
        }
        reset();
        return result;
    }

    /**
     * Processes the raw TCP data read from the MAX protocol, returning the
     * corresponding MessageType.
     *
     * @param line
     *            the raw data provided read from the MAX protocol
     * @return MessageType of the line added
     */
    @Nullable
    private static MessageType getMessageType(String line) {
        for (MessageType msgType : MessageType.values()) {
            if (line.startsWith(msgType.name() + SEPARATOR)) {
                return msgType;
            }
        }
        return null;
    }
}
