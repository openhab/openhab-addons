/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.satel.internal.command;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.satel.internal.event.EventDispatcher;
import org.openhab.binding.satel.internal.protocol.SatelMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for all command classes.
 *
 * @author Krzysztof Goworek - Initial contribution
 */
@NonNullByDefault
public abstract class SatelCommandBase extends SatelMessage implements SatelCommand {

    private final Logger logger = LoggerFactory.getLogger(SatelCommandBase.class);

    /**
     * Used in extended (INT-RS v2.xx) command version.
     */
    protected static final byte[] EXTENDED_CMD_PAYLOAD = { 0x00 };

    private static final byte COMMAND_RESULT_CODE = (byte) 0xef;

    private volatile State state = State.NEW;

    private boolean logResponseError = true;

    private @Nullable SatelMessage response;

    /**
     * Creates new command basing on command code and extended command flag.
     *
     * @param commandCode command code
     * @param extended if <code>true</code> command will be sent as extended (256 zones or outputs)
     */
    public SatelCommandBase(byte commandCode, boolean extended) {
        this(commandCode, extended ? EXTENDED_CMD_PAYLOAD : EMPTY_PAYLOAD);
    }

    /**
     * Creates new instance with specified command code and payload.
     *
     * @param command command code
     * @param payload command payload
     */
    public SatelCommandBase(byte commandCode, byte[] payload) {
        super(commandCode, payload);
    }

    @Override
    public State getState() {
        return state;
    }

    @Override
    public void setState(State state) {
        synchronized (this) {
            this.state = state;
            this.notifyAll();
        }
    }

    @Override
    public SatelMessage getRequest() {
        return this;
    }

    @Override
    public final boolean matches(@Nullable SatelMessage response) {
        return response != null
                && (response.getCommand() == getCommand() || response.getCommand() == COMMAND_RESULT_CODE);
    }

    @Override
    public final boolean handleResponse(EventDispatcher eventDispatcher, SatelMessage response) {
        // if response is valid, store it for future use
        if (response.getCommand() == COMMAND_RESULT_CODE) {
            if (!hasCommandSucceeded(response)) {
                return false;
            }
        } else if (response.getCommand() != getCommand()) {
            logger.debug("Response code does not match command {}: {}", String.format("%02X", getCommand()), response);
            return false;
        } else if (!isResponseValid(response)) {
            return false;
        }
        this.response = response;
        handleResponseInternal(eventDispatcher);
        return true;
    }

    public void ignoreResponseError() {
        this.logResponseError = false;
    }

    protected SatelMessage getResponse() {
        final SatelMessage response = this.response;
        if (response != null) {
            return response;
        } else {
            throw new IllegalStateException("Response not yet received for command. " + this.toString());
        }
    }

    /**
     * Checks whether given response is valid for the command.
     *
     * @param response message to check
     * @return <code>true</code> if given message is valid response for the command
     */
    protected abstract boolean isResponseValid(SatelMessage response);

    /**
     * Overriden in subclasses allows to execute action specific to given command (i.e. dispatch an event).
     *
     * @param eventDispatcher event dispatcher
     */
    protected void handleResponseInternal(final EventDispatcher eventDispatcher) {
    }

    protected static int bcdToInt(byte[] bytes, int offset, int size) {
        int result = 0, digit;
        int byteIdx = offset;
        int digits = size * 2;
        for (int i = 0; i < digits; ++i) {
            if (i % 2 == 0) {
                digit = (bytes[byteIdx] >> 4) & 0x0f;
            } else {
                digit = (bytes[byteIdx]) & 0x0f;
                byteIdx += 1;
            }
            result = result * 10 + digit;
        }
        return result;
    }

    protected boolean hasCommandSucceeded(SatelMessage response) {
        // validate response message
        if (response.getCommand() != COMMAND_RESULT_CODE) {
            logger.debug("Invalid response code: {}. {}", String.format("%02X", response.getCommand()), getRequest());
            return false;
        }
        if (response.getPayload().length != 1) {
            logger.debug("Invalid payload length: {}. {}", response.getPayload().length, getRequest());
            return false;
        }

        // check result code
        byte responseCode = response.getPayload()[0];
        String errorMsg;

        switch (responseCode) {
            case 0:
                // success
                return true;
            case 0x01:
                errorMsg = "Requesting user code not found";
                break;
            case 0x02:
                errorMsg = "No access";
                break;
            case 0x03:
                errorMsg = "Selected user does not exist";
                break;
            case 0x04:
                errorMsg = "Selected user already exists";
                break;
            case 0x05:
                errorMsg = "Wrong code or code already exists";
                break;
            case 0x06:
                errorMsg = "Telephone code already exists";
                break;
            case 0x07:
                errorMsg = "Changed code is the same";
                break;
            case 0x08:
                errorMsg = "Other error";
                break;
            case 0x11:
                errorMsg = "Can not arm, but can use force arm";
                break;
            case 0x12:
                errorMsg = "Can not arm";
                break;
            case (byte) 0xff:
                logger.trace("Command accepted");
                return true;
            default:
                if (responseCode >= 0x80 && responseCode <= 0x8f) {
                    errorMsg = String.format("Other error: %02X", responseCode);
                } else {
                    errorMsg = String.format("Unknown result code: %02X", responseCode);
                }
        }

        if (logResponseError) {
            logger.info("{}. {}", errorMsg, getRequest());
        }
        return false;
    }

    /**
     * Decodes firmware version and release date from command payload
     *
     * @param offset starting offset in payload
     * @return decoded firmware version and release date as string
     */
    public String getVersion(int offset) {
        // build version string
        final byte[] payload = getResponse().getPayload();
        String verStr = new String(payload, offset, 1) + "." + new String(payload, offset + 1, 2) + " "
                + new String(payload, offset + 3, 4) + "-" + new String(payload, offset + 7, 2) + "-"
                + new String(payload, offset + 9, 2);
        return verStr;
    }
}
