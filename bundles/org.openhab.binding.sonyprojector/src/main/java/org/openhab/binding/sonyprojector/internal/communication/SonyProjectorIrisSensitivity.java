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
package org.openhab.binding.sonyprojector.internal.communication;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.sonyprojector.internal.SonyProjectorException;
import org.openhab.core.types.StateOption;
import org.openhab.core.util.HexUtils;

/**
 * Represents the different iris sensitivities available for the projector
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public enum SonyProjectorIrisSensitivity {

    RECOMMEND("Recommend", new byte[] { 0x00, 0x00 }),
    FAST("Fast", new byte[] { 0x00, 0x01 }),
    SLOW("Slow", new byte[] { 0x00, 0x02 });

    private String name;
    private byte[] dataCode;

    /**
     * Constructor
     *
     * @param name the name of the iris sensitivity
     * @param dataCode the data code identifying the iris sensitivity
     */
    private SonyProjectorIrisSensitivity(String name, byte[] dataCode) {
        this.name = name;
        this.dataCode = dataCode;
    }

    /**
     * Get the data code identifying the current iris sensitivity
     *
     * @return the data code
     */
    public byte[] getDataCode() {
        return dataCode;
    }

    /**
     * Get the name of the current iris sensitivity
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the list of {@link StateOption} associated to the available iris sensitivities
     *
     * @return the list of {@link StateOption} associated to the available iris sensitivities
     */
    public static List<StateOption> getStateOptions() {
        List<StateOption> options = new ArrayList<>();
        for (SonyProjectorIrisSensitivity value : SonyProjectorIrisSensitivity.values()) {
            options.add(new StateOption(value.getName(), value.getName()));
        }
        return options;
    }

    /**
     * Get the iris sensitivity associated to a name
     *
     * @param name the name used to identify the iris sensitivity
     *
     * @return the iris sensitivity associated to the searched name
     *
     * @throws SonyProjectorException - If no iris sensitivity is associated to the searched name
     */
    public static SonyProjectorIrisSensitivity getFromName(String name) throws SonyProjectorException {
        for (SonyProjectorIrisSensitivity value : SonyProjectorIrisSensitivity.values()) {
            if (value.getName().equals(name)) {
                return value;
            }
        }
        throw new SonyProjectorException("Invalid name for an iris sensitivity: " + name);
    }

    /**
     * Get the iris sensitivity associated to a data code
     *
     * @param dataCode the data code used to identify the iris sensitivity
     *
     * @return the iris sensitivity associated to the searched data code
     *
     * @throws SonyProjectorException - If no iris sensitivity is associated to the searched data code
     */
    public static SonyProjectorIrisSensitivity getFromDataCode(byte[] dataCode) throws SonyProjectorException {
        for (SonyProjectorIrisSensitivity value : SonyProjectorIrisSensitivity.values()) {
            if (Arrays.equals(dataCode, value.getDataCode())) {
                return value;
            }
        }
        throw new SonyProjectorException("Invalid data code for an iris sensitivity: " + HexUtils.bytesToHex(dataCode));
    }
}
