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
package org.openhab.binding.sonyprojector.internal.communication.serial;

import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.i18n.CommunicationException;
import org.openhab.core.util.HexUtils;

/**
 * Represents the different error codes returned by the projector in serial mode
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public enum SonyProjectorSerialError {

    COMPLETE("Complete", new byte[] { 0x00, 0x00 }),
    UNDEFINED_COMMAND("Undefined Command", new byte[] { 0x01, 0x01 }),
    SIZE_ERROR("Size Error", new byte[] { 0x01, 0x04 }),
    SELECT_ERROR("Select Error", new byte[] { 0x01, 0x05 }),
    RANGE_OVER("Range Over", new byte[] { 0x01, 0x06 }),
    NOT_APPLICABLE("Not Applicable", new byte[] { 0x01, 0x0A }),
    CHECK_SUM_ERROR("Check Sum Error", new byte[] { (byte) 0xF0, 0x10 }),
    FRAMING_ERROR("Framing Error", new byte[] { (byte) 0xF0, 0x20 }),
    PARITY_ERROR("Parity Error", new byte[] { (byte) 0xF0, 0x30 }),
    OVER_RUN_ERROR("Over Run Error", new byte[] { (byte) 0xF0, 0x40 }),
    OTHER_COMM_ERROR("Other Comm Error", new byte[] { (byte) 0xF0, 0x50 });

    private String message;
    private byte[] dataCode;

    /**
     * Constructor
     *
     * @param message the error message
     * @param dataCode the data code identifying the error
     */
    private SonyProjectorSerialError(String message, byte[] dataCode) {
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
     * @param dataCode the data code used to identify the error
     *
     * @return the error associated to the searched data code
     *
     * @throws CommunicationException if no error is associated to the searched data code
     */
    public static SonyProjectorSerialError getFromDataCode(byte[] dataCode) throws CommunicationException {
        for (SonyProjectorSerialError value : SonyProjectorSerialError.values()) {
            if (Arrays.equals(dataCode, value.getDataCode())) {
                return value;
            }
        }
        throw new CommunicationException("Unknwon error code: " + HexUtils.bytesToHex(dataCode));
    }
}
