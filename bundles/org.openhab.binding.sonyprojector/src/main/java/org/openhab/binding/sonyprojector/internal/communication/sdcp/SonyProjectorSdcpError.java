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
package org.openhab.binding.sonyprojector.internal.communication.sdcp;

import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.i18n.CommunicationException;
import org.openhab.core.util.HexUtils;

/**
 * Represents the different error codes returned by the projector in Ethernet mode
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public enum SonyProjectorSdcpError {

    INVALID_ITEM("Invalid Item", new byte[] { 0x01, 0x01 }),
    INVALID_ITEM_REQUEST("Invalid Item Request", new byte[] { 0x01, 0x02 }),
    INVALID_LENGTH("Invalid Length", new byte[] { 0x01, 0x03 }),
    INVALID_DATA("Invalid Data", new byte[] { 0x01, 0x04 }),
    SHORT_DATA("Short Data", new byte[] { 0x01, 0x11 }),
    NOT_APPLICABLE_ITEM("Not Applicable Item", new byte[] { 0x01, (byte) 0x80 }),
    DIFFERENT_COMMUNITY("Different Community", new byte[] { 0x02, 0x01 }),
    INVALID_VERSION("Invalid Version", new byte[] { 0x03, 0x01 }),
    INVALID_CATEGORY("Invalid Category", new byte[] { 0x03, 0x02 }),
    INVALID_REQUEST("Invalid Request", new byte[] { 0x03, 0x03 }),
    SHORT_HEADER("Short Header", new byte[] { 0x03, 0x11 }),
    SHORT_COMMUNITY("Short Community", new byte[] { 0x03, 0x12 }),
    SHORT_COMMAND("Short Command", new byte[] { 0x03, 0x13 }),
    NETWORK_TIMEOUT("Network Timeout", new byte[] { 0x20, 0x01 }),
    COMM_TIMEOUT("Comm Timeout", new byte[] { (byte) 0xF0, 0x01 }),
    CHECK_SUM_ERROR("Check Sum Error", new byte[] { (byte) 0xF0, 0x10 }),
    FRAMING_ERROR("Framing Error", new byte[] { (byte) 0xF0, 0x20 }),
    PARITY_ERROR("Parity Error", new byte[] { (byte) 0xF0, 0x30 }),
    OVER_RUN_ERROR("Over Run Error", new byte[] { (byte) 0xF0, 0x40 }),
    OTHER_COMM_ERROR("Other Comm Error", new byte[] { (byte) 0xF0, 0x50 }),
    UNKNOWN_RESPONSE("Unknown Response", new byte[] { (byte) 0xF0, (byte) 0xF0 }),
    READ_ERROR("Read Error", new byte[] { (byte) 0xF1, 0x10 }),
    WRITE_ERROR("Write Error", new byte[] { (byte) 0xF1, 0x20 });

    private String message;
    private byte[] dataCode;

    /**
     * Constructor
     *
     * @param message the error message
     * @param dataCode the data code identifying the error
     */
    private SonyProjectorSdcpError(String message, byte[] dataCode) {
        this.message = message;
        this.dataCode = dataCode;
    }

    /**
     * Get the error message
     *
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Get the data code identifying the error
     *
     * @return the data code
     */
    public byte[] getDataCode() {
        return dataCode;
    }

    /**
     * Get the error associated to a data code
     *
     * @param dataCode the data code
     *
     * @return the error associated to the data code
     *
     * @throws CommunicationException if no error is associated to the data code
     */
    public static SonyProjectorSdcpError getFromDataCode(byte[] dataCode) throws CommunicationException {
        for (SonyProjectorSdcpError value : SonyProjectorSdcpError.values()) {
            if (Arrays.equals(dataCode, value.getDataCode())) {
                return value;
            }
        }
        throw new CommunicationException("Unknwon error code: " + HexUtils.bytesToHex(dataCode));
    }
}
