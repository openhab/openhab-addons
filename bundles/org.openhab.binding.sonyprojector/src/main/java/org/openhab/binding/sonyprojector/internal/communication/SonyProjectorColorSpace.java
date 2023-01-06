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
package org.openhab.binding.sonyprojector.internal.communication;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.sonyprojector.internal.SonyProjectorException;
import org.openhab.core.types.StateOption;
import org.openhab.core.util.HexUtils;

/**
 * Represents the different color spaces available for the projector
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public enum SonyProjectorColorSpace {

    // Category 1: VW300, VW315, VW320, VW328, VW350, VW365, VW500, VW600, HW60, HW65, HW68
    CAT1_BT709(1, "BT709", new byte[] { 0x00, 0x00 }),
    CAT1_SPACE1(1, "ColorSpace1", new byte[] { 0x00, 0x03 }),
    CAT1_SPACE2(1, "ColorSpace2", new byte[] { 0x00, 0x04 }),
    CAT1_SPACE3(1, "ColorSpace3", new byte[] { 0x00, 0x05 }),
    CAT1_CUSTOM(1, "Custom", new byte[] { 0x00, 0x06 }),

    // Category 2: VW260, VW270, VW285, VW295, VW385, VW515, VW520, VW528, VW550, VW570, VW665, VW675, VW695, VW760,
    // VW870, VW885, VW995
    CAT2_BT709(2, "BT709", new byte[] { 0x00, 0x00 }),
    CAT2_BT2020(2, "BT2020", new byte[] { 0x00, 0x08 }),
    CAT2_SPACE1(2, "ColorSpace1", new byte[] { 0x00, 0x03 }),
    CAT2_SPACE2(2, "ColorSpace2", new byte[] { 0x00, 0x04 }),
    CAT2_SPACE3(2, "ColorSpace3", new byte[] { 0x00, 0x05 }),
    CAT2_CUSTOM(2, "Custom", new byte[] { 0x00, 0x06 }),

    // Category 3: VW40, VW50, VW60, VW70, VW80, VW100, VW200, HW10, HW15, HW20
    CAT3_NORMAL(3, "Normal", new byte[] { 0x00, 0x00 }),
    CAT3_WIDE(3, "Wide", new byte[] { 0x00, 0x01 }),

    // Category 4: VW85, VW90, VW95, HW30ES
    CAT4_NORMAL(4, "Normal", new byte[] { 0x00, 0x00 }),
    CAT4_WIDE1(4, "Wide1", new byte[] { 0x00, 0x01 }),
    CAT4_WIDE2(4, "Wide2", new byte[] { 0x00, 0x02 }),
    CAT4_WIDE3(4, "Wide3", new byte[] { 0x00, 0x03 }),

    // Category 5: VW1000ES, VW1100ES
    CAT5_BT709(5, "BT709", new byte[] { 0x00, 0x00 }),
    CAT5_DCI(5, "DCI", new byte[] { 0x00, 0x01 }),
    CAT5_ADOBE_RGB(5, "AdobeRGB", new byte[] { 0x00, 0x02 }),
    CAT5_SPACE1(5, "ColorSpace1", new byte[] { 0x00, 0x03 }),
    CAT5_SPACE2(5, "ColorSpace2", new byte[] { 0x00, 0x04 }),
    CAT5_SPACE3(5, "ColorSpace3", new byte[] { 0x00, 0x05 }),

    // Category 6: HW35ES, HW40ES, HW45ES, HW50ES, HW55ES, HW58ES
    CAT6_BT709(6, "BT709", new byte[] { 0x00, 0x00 }),
    CAT6_SPACE1(6, "ColorSpace1", new byte[] { 0x00, 0x01 }),
    CAT6_SPACE2(6, "ColorSpace2", new byte[] { 0x00, 0x02 }),
    CAT6_SPACE3(6, "ColorSpace3", new byte[] { 0x00, 0x03 });

    private int category;
    private String name;
    private byte[] dataCode;

    /**
     * Constructor
     *
     * @param category a category of projector models for which the color space is available
     * @param name the name of the color space
     * @param dataCode the data code identifying the color space
     */
    private SonyProjectorColorSpace(int category, String name, byte[] dataCode) {
        this.category = category;
        this.name = name;
        this.dataCode = dataCode;
    }

    /**
     * Get the category of projector models for the current color space
     *
     * @return the category of projector models
     */
    public int getCategory() {
        return category;
    }

    /**
     * Get the data code identifying the current color space
     *
     * @return the data code
     */
    public byte[] getDataCode() {
        return dataCode;
    }

    /**
     * Get the name of the current color space
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the list of {@link StateOption} associated to the available color spaces for a particular category of
     * projector models
     *
     * @param category a category of projector models
     *
     * @return the list of {@link StateOption} associated to the available color spaces for a provided category of
     *         projector models
     */
    public static List<StateOption> getStateOptions(int category) {
        List<StateOption> options = new ArrayList<>();
        for (SonyProjectorColorSpace value : SonyProjectorColorSpace.values()) {
            if (value.getCategory() == category) {
                options.add(new StateOption(value.getName(), value.getName()));
            }
        }
        return options;
    }

    /**
     * Get the color space associated to a name for a particular category of projector models
     *
     * @param category a category of projector models
     * @param name the name used to identify the color space
     *
     * @return the color space associated to the searched name for the provided category of projector models
     *
     * @throws SonyProjectorException - If no color space is associated to the searched name for the provided category
     */
    public static SonyProjectorColorSpace getFromName(int category, String name) throws SonyProjectorException {
        for (SonyProjectorColorSpace value : SonyProjectorColorSpace.values()) {
            if (value.getCategory() == category && value.getName().equals(name)) {
                return value;
            }
        }
        throw new SonyProjectorException("Invalid name for a color space: " + name);
    }

    /**
     * Get the color space associated to a data code for a particular category of projector models
     *
     * @param category a category of projector models
     * @param dataCode the data code used to identify the color space
     *
     * @return the color space associated to the searched data code for the provided category of projector models
     *
     * @throws SonyProjectorException - If no color space is associated to the searched data code for the provided
     *             category
     */
    public static SonyProjectorColorSpace getFromDataCode(int category, byte[] dataCode) throws SonyProjectorException {
        for (SonyProjectorColorSpace value : SonyProjectorColorSpace.values()) {
            if (value.getCategory() == category && Arrays.equals(dataCode, value.getDataCode())) {
                return value;
            }
        }
        throw new SonyProjectorException("Invalid data code for a color space: " + HexUtils.bytesToHex(dataCode));
    }
}
