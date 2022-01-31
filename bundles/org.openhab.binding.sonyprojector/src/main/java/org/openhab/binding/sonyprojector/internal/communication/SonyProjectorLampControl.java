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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.sonyprojector.internal.SonyProjectorException;
import org.openhab.core.types.StateOption;
import org.openhab.core.util.HexUtils;

/**
 * Represents the different lamp control modes available for the projector
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public enum SonyProjectorLampControl {

    HIGH("High", new byte[] { 0x00, 0x01 }),
    LOW("Low", new byte[] { 0x00, 0x00 });

    private String name;
    private byte[] dataCode;

    /**
     * Constructor
     *
     * @param name the name of the lamp control mode
     * @param dataCode the data code identifying the lamp control mode
     */
    private SonyProjectorLampControl(String name, byte[] dataCode) {
        this.name = name;
        this.dataCode = dataCode;
    }

    /**
     * Get the data code identifying the current lamp control mode
     *
     * @return the data code
     */
    public byte[] getDataCode() {
        return dataCode;
    }

    /**
     * Get the name of the current lamp control mode
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the list of {@link StateOption} associated to the available lamp control modes
     *
     * @return the list of {@link StateOption} associated to the available lamp control modes
     */
    public static List<StateOption> getStateOptions() {
        List<StateOption> options = new ArrayList<>();
        for (SonyProjectorLampControl value : SonyProjectorLampControl.values()) {
            options.add(new StateOption(value.getName(), value.getName()));
        }
        return options;
    }

    /**
     * Get the lamp control mode associated to a name
     *
     * @param name the name used to identify the lamp control mode
     *
     * @return the lamp control mode associated to the searched name
     *
     * @throws SonyProjectorException - If no lamp control mode is associated to the searched name
     */
    public static SonyProjectorLampControl getFromName(String name) throws SonyProjectorException {
        for (SonyProjectorLampControl value : SonyProjectorLampControl.values()) {
            if (value.getName().equals(name)) {
                return value;
            }
        }
        throw new SonyProjectorException("Invalid name for a lamp control mode: " + name);
    }

    /**
     * Get the lamp control mode associated to a data code
     *
     * @param dataCode the data code used to identify the lamp control mode
     *
     * @return the lamp control mode associated to the searched data code
     *
     * @throws SonyProjectorException - If no lamp control mode is associated to the searched data code
     */
    public static SonyProjectorLampControl getFromDataCode(byte[] dataCode) throws SonyProjectorException {
        for (SonyProjectorLampControl value : SonyProjectorLampControl.values()) {
            if (Arrays.equals(dataCode, value.getDataCode())) {
                return value;
            }
        }
        throw new SonyProjectorException("Invalid data code for a lamp control mode: " + HexUtils.bytesToHex(dataCode));
    }
}
