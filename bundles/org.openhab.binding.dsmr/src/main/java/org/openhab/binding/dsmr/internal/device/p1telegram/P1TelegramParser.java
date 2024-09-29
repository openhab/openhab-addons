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
package org.openhab.binding.dsmr.internal.device.p1telegram;

import java.nio.charset.StandardCharsets;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.dsmr.internal.device.connector.DSMRErrorStatus;
import org.openhab.binding.dsmr.internal.device.cosem.CosemObject;
import org.openhab.binding.dsmr.internal.device.cosem.CosemObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link P1TelegramParser} is a class that will read P1-port data create a full P1
 * telegram.
 *
 * Data can be parsed in chunks. If a full P1 telegram is received, listeners are notified
 *
 * @author M. Volaart - Initial contribution
 * @author Hilbrand Bouwkamp - Removed asynchronous call and some clean up
 */
@NonNullByDefault
public class P1TelegramParser implements TelegramParser {

    /**
     * State of the parser
     */
    private enum State {
        /** Wait for the '/' character */
        WAIT_FOR_START,
        /** '/' character seen */
        HEADER,
        /** Waiting for the header to end with a CR & LF */
        CRLF,
        /** Handling OBIS Identifier */
        DATA_OBIS_ID,
        /** Parsing OBIS value */
        DATA_OBIS_VALUE,
        /** OBIS value end seen ')' */
        DATA_OBIS_VALUE_END,
        /** Parsing CRC value following '!' */
        CRC_VALUE
    }

    /**
     * Pattern for the CRC-code
     */
    private static final String CRC_PATTERN = "[0-9A-Z]{4}";

    private final Logger logger = LoggerFactory.getLogger(P1TelegramParser.class);

    /* internal state variables */

    /**
     * current obisId buffer.
     */
    private final StringBuilder obisId = new StringBuilder();

    /**
     * Current cosem object values buffer.
     */
    private final StringBuilder obisValue = new StringBuilder();

    /**
     * In lenient mode store raw data and log when a complete message is received.
     */
    private final StringBuilder rawData = new StringBuilder();

    /**
     * Current crc value read.
     */
    private final StringBuilder crcValue = new StringBuilder();

    /**
     * CRC calculation helper
     */
    private final CRC16 crc;

    /**
     * Current state of the P1 telegram parser
     */
    private volatile State state = State.WAIT_FOR_START;

    /**
     * Work in lenient mode (more fault tolerant)
     */
    private volatile boolean lenientMode;

    /**
     * Current telegram state
     */
    private volatile Optional<DSMRErrorStatus> telegramState = Optional.empty();

    /**
     * CosemObjectFactory helper class
     */
    private final CosemObjectFactory factory;

    /**
     * Received Cosem Objects in the P1Telegram that is currently received
     */
    private final List<Entry<String, String>> cosemObjects = new ArrayList<>();

    /**
     * List of Cosem Object values that are not known to this binding.
     */
    private final List<Entry<String, String>> unknownCosemObjects = new ArrayList<>();

    /**
     * Listener for new P1 telegrams
     */
    private final P1TelegramListener telegramListener;

    /**
     * Enable in tests. Will throw an exception on CRC error.
     */
    private final boolean test;

    /**
     * Creates a new P1TelegramParser
     *
     * @param telegramListener
     */
    public P1TelegramParser(final P1TelegramListener telegramListener) {
        this(telegramListener, false);
    }

    public P1TelegramParser(final P1TelegramListener telegramListener, final boolean test) {
        this.telegramListener = telegramListener;
        this.test = test;

        factory = new CosemObjectFactory();
        state = State.WAIT_FOR_START;
        crc = new CRC16(CRC16.Polynom.CRC16_IBM);
        telegramState = Optional.empty();
    }

    /**
     * Parses data. If a complete message is received the message will be passed to the telegramListener.
     *
     * @param data byte data to parse
     * @param length number of bytes to parse
     */
    @Override
    public void parse(final byte[] data, final int length) {
        if (lenientMode || logger.isTraceEnabled()) {
            final String rawBlock = new String(data, 0, length, StandardCharsets.UTF_8);

            if (lenientMode) {
                rawData.append(rawBlock);
            }
            if (logger.isTraceEnabled()) {
                logger.trace("Raw data: {}, Parser state entering parseData: {}", rawBlock, state);
            }
        }
        for (int i = 0; i < length; i++) {
            final char c = (char) data[i];

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
                    if (Character.isWhitespace(c)) { // NOPMD EmptyIfStmt
                        // do nothing
                    } else if (Character.isDigit(c)) {
                        setState(State.DATA_OBIS_ID);
                    } else {
                        handleUnexpectedCharacter(c);

                        setState(State.WAIT_FOR_START);
                    }
                    break;
                case DATA_OBIS_ID:
                    if (Character.isWhitespace(c)) { // NOPMD EmptyIfStmt
                        // ignore
                    } else if (Character.isDigit(c) || c == ':' || c == '-' || c == '.' || c == '*') { // NOPMD
                        // do nothing
                    } else if (c == '(') {
                        setState(State.DATA_OBIS_VALUE);
                    } else if (c == '!') {
                        handleUnexpectedCharacter(c);
                        if (lenientMode) {
                            // Clear current Obis Data (Keep already received data)
                            clearObisData();
                            setState(State.CRC_VALUE);
                        } else {
                            setState(State.WAIT_FOR_START);
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
                    if (Character.isWhitespace(c)) { // NOPMD EmptyIfStmt
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
                        logger.trace("telegramState {}, crcValue to check 0x{}", telegramState, crcValue);
                        // Only perform CRC check if telegram is still ok

                        if (telegramState.isEmpty() && crcValue.length() > 0) {
                            telegramState = checkCRC();
                        }
                        processTelegram();
                        reset();
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

    private Optional<DSMRErrorStatus> checkCRC() {
        final Optional<DSMRErrorStatus> telegramState;

        if (Pattern.matches(CRC_PATTERN, crcValue)) {
            final int crcP1Telegram = Integer.parseInt(crcValue.toString(), 16);
            final int calculatedCRC = crc.getCurrentCRCCode();

            if (logger.isDebugEnabled()) {
                logger.trace("received CRC value: {}, calculated CRC value: 0x{}", crcValue,
                        String.format("%04X", calculatedCRC));
            }
            if (crcP1Telegram != calculatedCRC) {
                if (test) {
                    throw new IllegalArgumentException(
                            String.format("Invalid CRC. Read: %s, expected: %04X", crcValue, calculatedCRC));
                }
                logger.trace("CRC value does not match, p1 Telegram failed");

                telegramState = Optional.of(DSMRErrorStatus.TELEGRAM_CRC_ERROR);
            } else {
                telegramState = Optional.empty();
            }
        } else {
            telegramState = Optional.of(DSMRErrorStatus.TELEGRAM_CRC_ERROR);
        }
        return telegramState;
    }

    private void processTelegram() {
        telegramState.ifPresentOrElse(error -> telegramListener.onError(error, ""),
                () -> telegramListener.telegramReceived(constructTelegram()));
    }

    private P1Telegram constructTelegram() {
        final List<CosemObject> cosemObjectsCopy = new ArrayList<>();

        cosemObjects.stream().forEach(e -> addCosemObject(cosemObjectsCopy, e));
        if (lenientMode) {
            return new P1Telegram(cosemObjectsCopy, rawData.toString(),
                    unknownCosemObjects.isEmpty() ? Collections.emptyList() : new ArrayList<>(unknownCosemObjects));
        } else {
            return new P1Telegram(cosemObjectsCopy);
        }
    }

    private void addCosemObject(final List<CosemObject> objects, final Entry<String, String> cosemEntry) {
        final String obisIdString = cosemEntry.getKey();
        final String obisValueString = cosemEntry.getValue();
        final CosemObject cosemObject = factory.getCosemObject(obisIdString, obisValueString);

        if (cosemObject == null) {
            if (lenientMode) {
                unknownCosemObjects.add(new SimpleEntry<>(obisIdString, obisValueString));
            }
        } else {
            logger.trace("Adding {} to list of Cosem Objects", cosemObject);
            objects.add(cosemObject);
        }
    }

    @Override
    public void reset() {
        setState(State.WAIT_FOR_START);
    }

    /**
     * Handles an unexpected character. The character will be logged and the current telegram is marked corrupted
     *
     * @param c the unexpected character
     */
    private void handleUnexpectedCharacter(final char c) {
        logger.debug("Unexpected character '{}' in state: {}. This P1 telegram is marked as failed", c, state);

        telegramState = Optional.of(DSMRErrorStatus.TELEGRAM_DATA_CORRUPTION);
        telegramListener.onError(DSMRErrorStatus.TELEGRAM_DATA_CORRUPTION, "");
    }

    /**
     * Stores a single character
     *
     * @param c the character to process
     */
    private void handleCharacter(final char c) {
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
                obisValue.append(c);
                crc.processByte((byte) c);
                break;
            case DATA_OBIS_VALUE_END:
                obisValue.append(c);
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
        obisValue.setLength(0);
        rawData.setLength(0);
        crcValue.setLength(0);
        crc.initialize();
        cosemObjects.clear();
        unknownCosemObjects.clear();
    }

    /**
     * Clears all the current OBIS data. I.e.
     * - current OBIS identifier
     * - current OBIS value
     */
    private void clearObisData() {
        obisId.setLength(0);
        obisValue.setLength(0);
    }

    /**
     * Store the current CosemObject in the list of received cosem Objects
     */
    private void storeCurrentCosemObject() {
        final String obisIdString = obisId.toString();

        if (!obisIdString.isEmpty()) {
            cosemObjects.add(new SimpleEntry<>(obisIdString, obisValue.toString()));
        }
        clearObisData();
    }

    /**
     * @param newState the new state to set
     */
    private void setState(final State newState) {
        synchronized (state) {
            switch (newState) {
                case HEADER:
                    // Clear CRC data and mark current telegram as OK
                    crc.initialize();
                    break;
                case WAIT_FOR_START:
                    // Clears internal state data and mark current telegram as OK
                    clearInternalData();
                    telegramState = Optional.empty();
                    break;
                case DATA_OBIS_ID:
                    // If the current state is CRLF we are processing the header and don't have a cosem object yet
                    if (state != State.CRLF) {
                        storeCurrentCosemObject();
                    }
                    break;
                case CRC_VALUE:
                    storeCurrentCosemObject();
                    break;
                default:
                    break;
            }
            state = newState;
        }
    }

    @Override
    public void setLenientMode(final boolean lenientMode) {
        this.lenientMode = lenientMode;
    }
}
