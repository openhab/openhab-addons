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
 * Represents the different film projection modes available for the projector
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public enum SonyProjectorFilmProjection {

    // Category 1: VW80, VW85, VW200
    CAT1_MODE1(1, "Mode1", new byte[] { 0x00, 0x01 }),
    CAT1_MODE2(1, "Mode2", new byte[] { 0x00, 0x02 }),
    CAT1_MODE3(1, "Mode3", new byte[] { 0x00, 0x03 }),
    CAT1_OFF(1, "Off", new byte[] { 0x00, 0x00 }),

    // Category 2: VW90, VW95
    CAT2_MODE1(2, "Mode1", new byte[] { 0x00, 0x01 }),
    CAT2_MODE2(2, "Mode2", new byte[] { 0x00, 0x02 }),
    CAT2_OFF(2, "Off", new byte[] { 0x00, 0x00 }),

    // Category 3: VW1000ES, VW1100ES, HW35ES, HW40ES, HW50ES, HW55ES, HW58ES
    CAT3_ON(3, "On", new byte[] { 0x00, 0x01 }),
    CAT3_OFF(3, "Off", new byte[] { 0x00, 0x00 });

    private int category;
    private String name;
    private byte[] dataCode;

    /**
     * Constructor
     *
     * @param category a category of projector models for which the film projection mode is available
     * @param name the name of the film projection mode
     * @param dataCode the data code identifying the film projection mode
     */
    private SonyProjectorFilmProjection(int category, String name, byte[] dataCode) {
        this.category = category;
        this.name = name;
        this.dataCode = dataCode;
    }

    /**
     * Get the category of projector models for the current film projection mode
     *
     * @return the category of projector models
     */
    public int getCategory() {
        return category;
    }

    /**
     * Get the data code identifying the current film projection mode
     *
     * @return the data code
     */
    public byte[] getDataCode() {
        return dataCode;
    }

    /**
     * Get the name of the current film projection mode
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the list of {@link StateOption} associated to the available film projection modes for a particular category
     * of projector models
     *
     * @param category a category of projector models
     *
     * @return the list of {@link StateOption} associated to the available film projection modes for a provided category
     *         of projector models
     */
    public static List<StateOption> getStateOptions(int category) {
        List<StateOption> options = new ArrayList<>();
        for (SonyProjectorFilmProjection value : SonyProjectorFilmProjection.values()) {
            if (value.getCategory() == category) {
                options.add(new StateOption(value.getName(), value.getName()));
            }
        }
        return options;
    }

    /**
     * Get the film projection mode associated to a name for a particular category of projector models
     *
     * @param category a category of projector models
     * @param name the name used to identify the film projection mode
     *
     * @return the film projection mode associated to the searched name for the provided category of projector models
     *
     * @throws SonyProjectorException - If no film projection mode is associated to the searched name for the provided
     *             category
     */
    public static SonyProjectorFilmProjection getFromName(int category, String name) throws SonyProjectorException {
        for (SonyProjectorFilmProjection value : SonyProjectorFilmProjection.values()) {
            if (value.getCategory() == category && value.getName().equals(name)) {
                return value;
            }
        }
        throw new SonyProjectorException("Invalid name for a film projection mode: " + name);
    }

    /**
     * Get the film projection mode associated to a data code for a particular category of projector models
     *
     * @param category a category of projector models
     * @param dataCode the data code used to identify the film projection mode
     *
     * @return the film projection mode associated to the searched data code for the provided category of projector
     *         models
     *
     * @throws SonyProjectorException - If no film projection mode is associated to the searched data code for the
     *             provided category
     */
    public static SonyProjectorFilmProjection getFromDataCode(int category, byte[] dataCode)
            throws SonyProjectorException {
        for (SonyProjectorFilmProjection value : SonyProjectorFilmProjection.values()) {
            if (value.getCategory() == category && Arrays.equals(dataCode, value.getDataCode())) {
                return value;
            }
        }
        throw new SonyProjectorException(
                "Invalid data code for a film projection mode: " + HexUtils.bytesToHex(dataCode));
    }
}
