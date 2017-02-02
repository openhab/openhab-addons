/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.dsmr.internal.device.p1telegram;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.regex.Pattern;

import org.openhab.binding.dsmr.internal.device.cosem.CosemObject;
import org.openhab.binding.dsmr.internal.device.cosem.CosemObjectFactory;
import org.openhab.binding.dsmr.internal.device.p1telegram.P1TelegramListener.TelegramState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The P1TelegramParser is a class that will read P1-port data create a full P1
 * telegram.
 *
 * Data can be parsed in chunks. If a full P1 telegram is received, listeners are notified
 *
 * @author M. Volaart
 * @since 2.1.0
 */
public class P1TelegramParser {
    // Logger
    private final Logger logger = LoggerFactory.getLogger(P1TelegramParser.class);

    private static final String CRC_PATTERN = "[0-9A-Z]{4}";

    // State of the parser
    private static enum State {
        // Wait for the '/' character
        WAIT_FOR_START,
        // '/' character seen
        HEADER,
        // Waiting for the header to end with a CR & LF
        CRLF,
        // Handling OBIS Identifier
        DATA_OBIS_ID,
        // Parsing OBIS value
        DATA_OBIS_VALUE,
        // OBIS value end seen ')'
        DATA_OBIS_VALUE_END,
        // Parsing CRC value following '!'
        CRC_VALUE
    };

    /* internal state variables */
    private StringBuffer obisId = new StringBuffer();
    private StringBuffer cosemObjectValuesString = new StringBuffer();
    private StringBuffer crcValue = new StringBuffer();
    private CRC16 crc;
    private State state = State.WAIT_FOR_START;
    private boolean lenientMode = false;
    private TelegramState telegramState;

    // Helper classes
    private CosemObjectFactory factory;

    // Received Cosem Objects in the received P1Telegram
    private List<CosemObject> cosemObjects = new LinkedList<CosemObject>();

    // Listener
    private P1TelegramListener telegramListener = null;

    /**
     * Creates a new P1TelegramParser
     *
     * @param lenientMode boolean specifying if parsing must be done lenient
     */
    public P1TelegramParser(boolean lenientMode, P1TelegramListener telegramListener) {
        this.telegramListener = telegramListener;

        factory = new CosemObjectFactory();
        state = State.WAIT_FOR_START;
        crc = new CRC16(CRC16.Polynom.CRC16_IBM);
        telegramState = TelegramState.OK;

        this.lenientMode = lenientMode;
    }

    /**
     * Parses data. If parsing is not ready yet nothing will be returned. If
     * parsing fails completely nothing will be returned. If parsing succeeds
     * (partial) the received OBIS messages will be returned.
     *
     * @param data
     *            byte data
     * @param offset
     *            offset tot start in the data buffer
     * @param length
     *            number of bytes to parse
     */
    public void parseData(byte[] data, int offset, int length) {
        if (logger.isTraceEnabled()) {
            logger.trace("Data: {}, state before parsing: {}", new String(Arrays.copyOfRange(data, offset, length)),
                    state);
        }
        for (int i = offset; i < (offset + length); i++) {
            char c = (char) data[i];

            switch (state) {
                case WAIT_FOR_START:
                    if (c == '/') {
                        setState(State.HEADER);
                    }
                    break;
                case HEADER:
                    if (c == '\r') {
                        setState(State.CRLF);
                    }
                    break;
                case CRLF:
                    if (Character.isWhitespace(c)) {
                        // do nothing
                    } else if (Character.isDigit(c)) {
                        setState(State.DATA_OBIS_ID);
                    } else {
                        handleUnexpectedCharacter(c);

                        setState(State.WAIT_FOR_START);
                    }
                    break;
                case DATA_OBIS_ID:
                    if (Character.isWhitespace(c)) {
                        // ignore
                    } else if (Character.isDigit(c) || c == ':' || c == '-' || c == '.' || c == '*') {
                        // do nothing
                    } else if (c == '(') {
                        setState(State.DATA_OBIS_VALUE);
                    } else if (c == '!') {
                        handleUnexpectedCharacter(c);
                        if (!lenientMode) {
                            setState(State.WAIT_FOR_START);
                        } else {
                            // Clear current Obis Data (Keep already received data)
                            clearObisData();
                            setState(State.CRC_VALUE);
                        }
                    } else {
                        handleUnexpectedCharacter(c);

                        if (lenientMode) {
                            clearObisData();
                            setState(State.DATA_OBIS_ID);
                        } else {
                            setState(State.WAIT_FOR_START);
                        }
                    }
                    break;
                case DATA_OBIS_VALUE:
                    if (c == ')') {
                        setState(State.DATA_OBIS_VALUE_END);
                    }
                    break;
                case DATA_OBIS_VALUE_END:
                    if (Character.isWhitespace(c)) {
                        // ignore
                    } else if (Character.isDigit(c)) {
                        setState(State.DATA_OBIS_ID);
                    } else if (c == '(') {
                        setState(State.DATA_OBIS_VALUE);
                    } else if (c == '!') {
                        setState(State.CRC_VALUE);
                    } else {
                        handleUnexpectedCharacter(c);
                        if (!lenientMode) {
                            setState(State.WAIT_FOR_START);
                        } // Other wise try to recover in lenient mode
                    }
                    break;

                case CRC_VALUE:
                    /*
                     * Normally the P1 telegram ends with a \r\n sequence
                     * If we already see a '/' character we also assume the current
                     * P1 telegram is correctly finished
                     */
                    if (c == '\r' || c == '/') {
                        logger.debug("telegramState {}, crcValue to check 0x{}", telegramState, crcValue);
                        // Only perform CRC check if telegram is still ok
                        if (telegramState == TelegramState.OK && crcValue.length() > 0) {
                            if (Pattern.matches(CRC_PATTERN, crcValue)) {
                                int crcP1Telegram = Integer.parseInt(crcValue.toString(), 16);
                                int calculatedCRC = crc.getCurrentCRCCode();

                                if (logger.isDebugEnabled()) {
                                    logger.debug("received CRC value: {}, calculated CRC value: 0x{}", crcValue,
                                            String.format("%04X", calculatedCRC));
                                }
                                if (crcP1Telegram != calculatedCRC) {
                                    logger.debug("CRC value does not match, p1 Telegram failed");

                                    telegramState = TelegramState.CRC_ERROR;
                                }
                            } else {
                                telegramState = TelegramState.CRC_ERROR;
                            }
                        }

                        if (factory != null && telegramListener != null) {
                            /*
                             * Send messages asynchronous to keep reading the Serial port.
                             * If there are no messages to send, still let the listener
                             * known that there are no messages and if the parsing was successful
                             * This enables the listener to reinitialize the Serial Port for example
                             */
                            FutureTask<Void> task = new FutureTask<Void>(
                                    new P1TelegramCallable(cosemObjects, telegramState));
                            task.run();
                        }

                        setState(State.WAIT_FOR_START);
                        if (c == '/') {
                            /*
                             * Immediately proceed to the next state (robust implementation for meter that do not follow
                             * the specification
                             */
                            setState(State.HEADER);
                        }
                    }
                    break;
            }

            handleCharacter(c);
        }
        logger.trace("State after parsing: {}", state);
    }

    /**
     * Abort the current telegram
     */
    public void abortTelegram() {
        setState(State.WAIT_FOR_START);
    }

    /**
     * Handles an unexpected character. The character will be logged and the current telegram is marked corrupted
     *
     * @param c the unexpected character
     */
    private void handleUnexpectedCharacter(char c) {
        logger.debug("Unexpected character '{}' in state: {}. This P1 telegram is marked as failed", c, state);

        telegramState = TelegramState.DATA_CORRUPTION;
    }

    /**
     * Stores a single character
     *
     * @param c the character to process
     */
    private void handleCharacter(char c) {

        switch (state) {
            case WAIT_FOR_START:
                // ignore the data
                break;
            case HEADER:
                crc.processByte((byte) c);
                break;
            case CRLF:
                crc.processByte((byte) c);
                break;
            case DATA_OBIS_ID:
                obisId.append(c);
                crc.processByte((byte) c);
                break;
            case DATA_OBIS_VALUE:
                cosemObjectValuesString.append(c);
                crc.processByte((byte) c);
                break;
            case DATA_OBIS_VALUE_END:
                cosemObjectValuesString.append(c);
                crc.processByte((byte) c);
                break;
            case CRC_VALUE:
                if (c == '!') {
                    crc.processByte((byte) c);
                } else {
                    crcValue.append(c);
                }
                // CRC data is not part of received data
                break;
            default:
                break;
        }
    }

    /**
     * Clears all internal state
     */
    private void clearInternalData() {
        obisId.setLength(0);
        cosemObjectValuesString.setLength(0);
        crcValue.setLength(0);
        crc.initialize();
        cosemObjects.clear();
    }

    /**
     * Clears all the current OBIS data. I.e.
     * - current OBIS identifier
     * - current OBIS value
     * - current OBIS data object
     */
    private void clearObisData() {
        obisId.setLength(0);
        cosemObjectValuesString.setLength(0);
    }

    /**
     * Store the current CosemObject in the list of received cosem Objects
     */
    private void storeCurrentCosemObject() {
        CosemObject cosemObject = factory.getCosemObject(obisId.toString(), cosemObjectValuesString.toString());

        logger.trace("Storing {}", cosemObject);

        if (cosemObject != null) {
            logger.trace("Adding {} to list of Cosem Objects", cosemObject);
            cosemObjects.add(cosemObject);
        }
    }

    /**
     * @param state the new state to set
     */
    private void setState(State newState) {
        synchronized (state) {
            switch (newState) {
                case HEADER:
                    // Clear CRC data and mark current telegram as OK
                    crc.initialize();
                    break;
                case WAIT_FOR_START:
                    // Clears internal state data and mark current telegram as OK
                    clearInternalData();
                    telegramState = TelegramState.OK;
                    break;
                case DATA_OBIS_ID:
                    // If the current state is CRLF we are processing the header and don't have a cosem object yet
                    if (state != State.CRLF) {
                        storeCurrentCosemObject();
                        clearObisData();
                    }
                    break;
                case CRC_VALUE:
                    storeCurrentCosemObject();
                    clearObisData();
                    break;
                default:
                    break;
            }
            state = newState;
        }
    }

    /**
     * @return the lenientMode
     */
    public boolean isLenientMode() {
        return lenientMode;
    }

    /**
     * @param lenientMode the lenientMode to set
     */
    public void setLenientMode(boolean lenientMode) {
        this.lenientMode = lenientMode;
    }

    /**
     * P1TelegramCallable enables sending OBIS messages asynchronous to the OpenHAB2 system
     *
     * @author M. Volaart
     * @since 2.1.0
     */
    private class P1TelegramCallable implements Callable<Void> {
        private List<CosemObject> cosemObjects;
        private TelegramState p1TelegramParseState;

        /**
         * Constructs a new P1TelegramCallable object
         *
         * @param obisMessages the obis messages to send
         * @param p1TelegramParsedOK if the P1 telegram was parsed successfully
         */
        P1TelegramCallable(List<CosemObject> obisMessages, TelegramState p1TelegramParseState) {
            this.cosemObjects = obisMessages;
            this.p1TelegramParseState = p1TelegramParseState;
        }

        /**
         * Worker
         *
         * Calls the listener for new P1 telegrams
         */
        @Override
        public Void call() {
            telegramListener.telegramReceived(cosemObjects, p1TelegramParseState);

            return null;
        }
    }
}
