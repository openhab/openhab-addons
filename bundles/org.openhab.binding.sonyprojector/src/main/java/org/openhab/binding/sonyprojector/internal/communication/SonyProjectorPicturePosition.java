/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.types.StateOption;
import org.eclipse.smarthome.core.util.HexUtils;
import org.openhab.binding.sonyprojector.internal.SonyProjectorException;

/**
 * Represents the different picture positions available for the projector
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public enum SonyProjectorPicturePosition {

    // Category 1: VW385, VW500, VW515, VW520, VW528, VW550, VW570, VW600, VW665, VW675, VW695, VW760, VW870, VW885,
    // VW995, VW1000ES, VW1100ES
    CAT1_185(1, "185", "1.85:1", new byte[] { 0x00, 0x00 }),
    CAT1_235(1, "235", "2.35:1", new byte[] { 0x00, 0x01 }),
    CAT1_CUSTOM1(1, "Custom1", "Custom 1", new byte[] { 0x00, 0x02 }),
    CAT1_CUSTOM2(1, "Custom2", "Custom 2", new byte[] { 0x00, 0x03 }),
    CAT1_CUSTOM3(1, "Custom3", "Custom 3", new byte[] { 0x00, 0x04 }),

    // Category 2: VW95
    CAT2_POSITION1(2, "Position1", "Position 1", new byte[] { 0x00, 0x00 }),
    CAT2_POSITION2(2, "Position2", "Position 2", new byte[] { 0x00, 0x01 }),
    CAT2_POSITION3(2, "Position3", "Position 3", new byte[] { 0x00, 0x02 }),
    CAT2_POSITION4(2, "Position4", "Position 4", new byte[] { 0x00, 0x03 }),
    CAT2_POSITION5(2, "Position5", "Position 5", new byte[] { 0x00, 0x04 });

    private int category;
    private String name;
    private @Nullable String label;
    private byte[] dataCode;

    /**
     * Constructor
     *
     * @param category a category of projector models for which the picture position is available
     * @param name the name of the picture position
     * @param label the label of the picture position; can be null when the label is identical to the name
     * @param dataCode the data code identifying the picture position
     */
    private SonyProjectorPicturePosition(int category, String name, @Nullable String label, byte[] dataCode) {
        this.category = category;
        this.name = name;
        this.label = label;
        this.dataCode = dataCode;
    }

    /**
     * Get the category of projector models for the current picture position
     *
     * @return the category of projector models
     */
    public int getCategory() {
        return category;
    }

    /**
     * Get the data code identifying the current picture position
     *
     * @return the data code
     */
    public byte[] getDataCode() {
        return dataCode;
    }

    /**
     * Get the label of the current picture position
     *
     * @return the label
     */
    public @Nullable String getLabel() {
        return label;
    }

    /**
     * Get the name of the current picture position
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the list of {@link StateOption} associated to the available picture positions for a particular category of
     * projector models
     *
     * @param category a category of projector models
     *
     * @return the list of {@link StateOption} associated to the available picture positions for a provided category of
     *         projector models
     */
    public static List<StateOption> getStateOptions(int category) {
        List<StateOption> options = new ArrayList<>();
        for (SonyProjectorPicturePosition value : SonyProjectorPicturePosition.values()) {
            if (value.getCategory() == category) {
                options.add(new StateOption(value.getName(),
                        value.getLabel() != null ? value.getLabel() : value.getName()));
            }
        }
        return options;
    }

    /**
     * Get the picture position associated to a name for a particular category of projector models
     *
     * @param category a category of projector models
     * @param name the name used to identify the picture position
     *
     * @return the picture position associated to the searched name for the provided category of projector models
     *
     * @throws SonyProjectorException - If no picture position is associated to the searched name for the provided
     *             category
     */
    public static SonyProjectorPicturePosition getFromName(int category, String name) throws SonyProjectorException {
        for (SonyProjectorPicturePosition value : SonyProjectorPicturePosition.values()) {
            if (value.getCategory() == category && value.getName().equals(name)) {
                return value;
            }
        }
        throw new SonyProjectorException("Invalid name for a picture position: " + name);
    }

    /**
     * Get the picture position associated to a data code for a particular category of projector models
     *
     * @param category a category of projector models
     * @param dataCode the data code used to identify the picture position
     *
     * @return the picture position associated to the searched data code for the provided category of projector models
     *
     * @throws SonyProjectorException - If no picture position is associated to the searched data code for the provided
     *             category
     */
    public static SonyProjectorPicturePosition getFromDataCode(int category, byte[] dataCode)
            throws SonyProjectorException {
        for (SonyProjectorPicturePosition value : SonyProjectorPicturePosition.values()) {
            if (value.getCategory() == category && Arrays.equals(dataCode, value.getDataCode())) {
                return value;
            }
        }
        throw new SonyProjectorException("Invalid data code for a picture position: " + HexUtils.bytesToHex(dataCode));
    }
}
