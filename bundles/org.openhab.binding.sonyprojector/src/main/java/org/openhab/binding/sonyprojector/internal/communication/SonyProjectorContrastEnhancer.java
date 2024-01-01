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
 * Represents the different contrast enhancer modes available for the projector
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public enum SonyProjectorContrastEnhancer {

    // Category 1: VW260, VW270, VW285, VW295, VW300, VW315, VW320, VW328, VW350, VW365, VW385, VW500, VW515, VW520,
    // VW528, VW550, VW570, VW600, VW665, VW675, VW695, VW760, VW870, VW885, VW995, VW1000ES, VW1100ES, HW40ES, HW45ES,
    // HW50ES, HW55ES, HW58ES, HW60, HW65, HW68
    CAT1_HIGH(1, "High", new byte[] { 0x00, 0x02 }),
    CAT1_MIDDLE(1, "Middle", new byte[] { 0x00, 0x03 }),
    CAT1_LOW(1, "Low", new byte[] { 0x00, 0x01 }),
    CAT1_OFF(1, "Off", new byte[] { 0x00, 0x00 }),

    // Category 2: VW40, VW50, VW60, VW70, VW80, VW100, VW200, HW10, HW15, HW20
    CAT2_HIGH(2, "High", new byte[] { 0x00, 0x02 }),
    CAT2_LOW(2, "Low", new byte[] { 0x00, 0x01 }),
    CAT2_OFF(2, "Off", new byte[] { 0x00, 0x00 }),

    // Category 3: VW85, VW90, VW95, HW30ES
    CAT3_LEVEL_MINUS_3(3, "-3", new byte[] { (byte) 0xFF, (byte) 0xFD }),
    CAT3_LEVEL_MINUS_2(3, "-2", new byte[] { (byte) 0xFF, (byte) 0xFE }),
    CAT3_LEVEL_MINUS_1(3, "-1", new byte[] { (byte) 0xFF, (byte) 0xFF }),
    CAT3_LEVEL_0(3, "0", new byte[] { 0x00, 0x00 }),
    CAT3_LEVEL_PLUS_1(3, "1", new byte[] { 0x00, 0x01 }),
    CAT3_LEVEL_PLUS_2(3, "2", new byte[] { 0x00, 0x02 }),
    CAT3_LEVEL_PLUS_3(3, "3", new byte[] { 0x00, 0x03 });

    private int category;
    private String name;
    private byte[] dataCode;

    /**
     * Constructor
     *
     * @param category a category of projector models for which the contrast enhancer mode is available
     * @param name the name of the contrast enhancer mode
     * @param dataCode the data code identifying the contrast enhancer mode
     */
    private SonyProjectorContrastEnhancer(int category, String name, byte[] dataCode) {
        this.category = category;
        this.name = name;
        this.dataCode = dataCode;
    }

    /**
     * Get the category of projector models for the current contrast enhancer mode
     *
     * @return the category of projector models
     */
    public int getCategory() {
        return category;
    }

    /**
     * Get the data code identifying the current contrast enhancer mode
     *
     * @return the data code
     */
    public byte[] getDataCode() {
        return dataCode;
    }

    /**
     * Get the name of the current contrast enhancer mode
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the list of {@link StateOption} associated to the available contrast enhancer modes for a particular category
     * of projector models
     *
     * @param category a category of projector models
     *
     * @return the list of {@link StateOption} associated to the available contrast enhancer modes for a provided
     *         category of projector models
     */
    public static List<StateOption> getStateOptions(int category) {
        List<StateOption> options = new ArrayList<>();
        for (SonyProjectorContrastEnhancer value : SonyProjectorContrastEnhancer.values()) {
            if (value.getCategory() == category) {
                options.add(new StateOption(value.getName(), value.getName()));
            }
        }
        return options;
    }

    /**
     * Get the contrast enhancer mode associated to a name for a particular category of projector models
     *
     * @param category a category of projector models
     * @param name the name used to identify the contrast enhancer mode
     *
     * @return the contrast enhancer mode associated to the searched name for the provided category of projector models
     *
     * @throws SonyProjectorException - If no contrast enhancer mode is associated to the searched name for the provided
     *             category
     */
    public static SonyProjectorContrastEnhancer getFromName(int category, String name) throws SonyProjectorException {
        for (SonyProjectorContrastEnhancer value : SonyProjectorContrastEnhancer.values()) {
            if (value.getCategory() == category && value.getName().equals(name)) {
                return value;
            }
        }
        throw new SonyProjectorException("Invalid name for a contrast enhancer mode: " + name);
    }

    /**
     * Get the contrast enhancer mode associated to a data code for a particular category of projector models
     *
     * @param category a category of projector models
     * @param dataCode the data code used to identify the contrast enhancer mode
     *
     * @return the contrast enhancer mode associated to the searched data code for the provided category of projector
     *         models
     *
     * @throws SonyProjectorException - If no contrast enhancer mode is associated to the searched data code for the
     *             provided category
     */
    public static SonyProjectorContrastEnhancer getFromDataCode(int category, byte[] dataCode)
            throws SonyProjectorException {
        for (SonyProjectorContrastEnhancer value : SonyProjectorContrastEnhancer.values()) {
            if (value.getCategory() == category && Arrays.equals(dataCode, value.getDataCode())) {
                return value;
            }
        }
        throw new SonyProjectorException(
                "Invalid data code for a contrast enhancer mode: " + HexUtils.bytesToHex(dataCode));
    }
}
