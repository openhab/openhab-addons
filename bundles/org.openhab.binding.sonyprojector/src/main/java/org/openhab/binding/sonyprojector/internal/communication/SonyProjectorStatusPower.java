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
package org.openhab.binding.sonyprojector.internal.communication;

import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.sonyprojector.internal.SonyProjectorException;
import org.openhab.core.util.HexUtils;

/**
 * Represents the different power status of the projector
 *
 * @author Markus Wehrle - Initial contribution
 * @author Laurent Garnier - Transform into an enum
 */
@NonNullByDefault
public enum SonyProjectorStatusPower {

    STANDBY(new byte[] { 0x00, 0x00 }, false),
    START_UP(new byte[] { 0x00, 0x01 }, true),
    STARTUP_LAMP(new byte[] { 0x00, 0x02 }, true),
    POWER_ON(new byte[] { 0x00, 0x03 }, true),
    COOLING1(new byte[] { 0x00, 0x04 }, false),
    COOLING2(new byte[] { 0x00, 0x05 }, false),
    SAVING_COOLING1(new byte[] { 0x00, 0x06 }, false),
    SAVING_COOLING2(new byte[] { 0x00, 0x07 }, false),
    SAVING_STANDBY(new byte[] { 0x00, 0x08 }, false);

    private byte[] dataCode;
    private boolean on;

    /**
     * Constructor
     *
     * @param dataCode the data code identifying the power status
     * @param on the associated ON or OFF status of the power status
     */
    private SonyProjectorStatusPower(byte[] dataCode, boolean on) {
        this.dataCode = dataCode;
        this.on = on;
    }

    /**
     * Get the data code identifying the current power status
     *
     * @return the data code
     */
    public byte[] getDataCode() {
        return dataCode;
    }

    /**
     * Get the associated ON or OFF status of the current power status
     *
     * @return true if the current power status ios associated to an ON status
     */
    public boolean isOn() {
        return on;
    }

    /**
     * Get the power status associated to a data code
     *
     * @param dataCode the data code used to identify the power status
     *
     * @return the power status associated to the searched data code
     *
     * @throws SonyProjectorException - If no power status is associated to the searched data code
     */
    public static SonyProjectorStatusPower getFromDataCode(byte[] dataCode) throws SonyProjectorException {
        for (SonyProjectorStatusPower value : SonyProjectorStatusPower.values()) {
            if (Arrays.equals(dataCode, value.getDataCode())) {
                return value;
            }
        }
        throw new SonyProjectorException("Invalid data code for a power status: " + HexUtils.bytesToHex(dataCode));
    }
}
