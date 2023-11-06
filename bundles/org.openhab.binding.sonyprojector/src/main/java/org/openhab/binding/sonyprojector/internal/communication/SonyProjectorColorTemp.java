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
 * Represents the different color temperatures available for the projector
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public enum SonyProjectorColorTemp {

    // Category 1: VW260, VW270, VW285, VW295, VW300, VW315, VW320, VW328, VW350, VW365, VW385, VW500, VW515, VW520,
    // VW528, VW550, VW570, VW600, VW665, VW675, VW695, VW760, VW870, VW885, VW995, HW60, HW65, HW68
    CAT1_D93(1, "D93", new byte[] { 0x00, 0x00 }),
    CAT1_D75(1, "D75", new byte[] { 0x00, 0x01 }),
    CAT1_D65(1, "D65", new byte[] { 0x00, 0x02 }),
    CAT1_D55(1, "D55", new byte[] { 0x00, 0x09 }),
    CAT1_CUSTOM1(1, "Custom1", new byte[] { 0x00, 0x03 }),
    CAT1_CUSTOM2(1, "Custom2", new byte[] { 0x00, 0x04 }),
    CAT1_CUSTOM3(1, "Custom3", new byte[] { 0x00, 0x05 }),
    CAT1_CUSTOM4(1, "Custom4", new byte[] { 0x00, 0x06 }),
    CAT1_CUSTOM5(1, "Custom5", new byte[] { 0x00, 0x08 }),

    // Category 2: VW40, VW50, VW60, VW100, VW200
    CAT2_HIGH(2, "High", new byte[] { 0x00, 0x00 }),
    CAT2_MIDDLE(2, "Middle", new byte[] { 0x00, 0x01 }),
    CAT2_LOW(2, "Low", new byte[] { 0x00, 0x02 }),
    CAT2_CUSTOM1(2, "Custom1", new byte[] { 0x00, 0x03 }),
    CAT2_CUSTOM2(2, "Custom2", new byte[] { 0x00, 0x04 }),
    CAT2_CUSTOM3(2, "Custom3", new byte[] { 0x00, 0x05 }),

    // Category 3: VW70, VW80, HW10, HW15, HW20
    CAT3_HIGH(3, "High", new byte[] { 0x00, 0x00 }),
    CAT3_MIDDLE(3, "Middle", new byte[] { 0x00, 0x01 }),
    CAT3_LOW(3, "Low", new byte[] { 0x00, 0x02 }),
    CAT3_CUSTOM1(3, "Custom1", new byte[] { 0x00, 0x03 }),
    CAT3_CUSTOM2(3, "Custom2", new byte[] { 0x00, 0x04 }),
    CAT3_CUSTOM3(3, "Custom3", new byte[] { 0x00, 0x05 }),
    CAT3_CUSTOM4(3, "Custom4", new byte[] { 0x00, 0x06 }),

    // Category 4: VW85, VW90, VW95, HW30
    CAT4_HIGH(4, "High", new byte[] { 0x00, 0x00 }),
    CAT4_MIDDLE(4, "Middle", new byte[] { 0x00, 0x01 }),
    CAT4_LOW1(4, "Low1", new byte[] { 0x00, 0x02 }),
    CAT4_LOW2(4, "Low2", new byte[] { 0x00, 0x07 }),
    CAT4_CUSTOM1(4, "Custom1", new byte[] { 0x00, 0x03 }),
    CAT4_CUSTOM2(4, "Custom2", new byte[] { 0x00, 0x04 }),
    CAT4_CUSTOM3(4, "Custom3", new byte[] { 0x00, 0x05 }),
    CAT4_CUSTOM4(4, "Custom4", new byte[] { 0x00, 0x06 }),
    CAT4_CUSTOM5(4, "Custom5", new byte[] { 0x00, 0x08 }),

    // Category 5: VW1000ES, VW1100ES
    CAT5_D93(5, "D93", new byte[] { 0x00, 0x00 }),
    CAT5_D75(5, "D75", new byte[] { 0x00, 0x01 }),
    CAT5_D65(5, "D65", new byte[] { 0x00, 0x02 }),
    CAT5_D55(5, "D55", new byte[] { 0x00, 0x09 }),
    CAT5_DCI(5, "DCI", new byte[] { 0x00, 0x07 }),
    CAT5_CUSTOM1(5, "Custom1", new byte[] { 0x00, 0x03 }),
    CAT5_CUSTOM2(5, "Custom2", new byte[] { 0x00, 0x04 }),
    CAT5_CUSTOM3(5, "Custom3", new byte[] { 0x00, 0x05 }),
    CAT5_CUSTOM4(5, "Custom4", new byte[] { 0x00, 0x06 }),
    CAT5_CUSTOM5(5, "Custom5", new byte[] { 0x00, 0x08 }),

    // Category 6: HW35ES, HW40ES, HW45ES, HW58ES
    CAT6_D93(6, "D93", new byte[] { 0x00, 0x00 }),
    CAT6_D75(6, "D75", new byte[] { 0x00, 0x01 }),
    CAT6_D65(6, "D65", new byte[] { 0x00, 0x02 }),
    CAT6_D55(6, "D55", new byte[] { 0x00, 0x07 }),
    CAT6_CUSTOM(6, "Custom", new byte[] { 0x00, 0x08 }),

    // Category 7: HW50ES, HW55ES
    CAT7_D93(7, "D93", new byte[] { 0x00, 0x00 }),
    CAT7_D75(7, "D75", new byte[] { 0x00, 0x01 }),
    CAT7_D65(7, "D65", new byte[] { 0x00, 0x02 }),
    CAT7_D55(7, "D55", new byte[] { 0x00, 0x07 }),
    CAT7_CUSTOM1(7, "Custom1", new byte[] { 0x00, 0x03 }),
    CAT7_CUSTOM2(7, "Custom2", new byte[] { 0x00, 0x04 }),
    CAT7_CUSTOM3(7, "Custom3", new byte[] { 0x00, 0x05 }),
    CAT7_CUSTOM4(7, "Custom4", new byte[] { 0x00, 0x06 }),
    CAT7_CUSTOM5(7, "Custom5", new byte[] { 0x00, 0x08 });

    private int category;
    private String name;
    private byte[] dataCode;

    /**
     * Constructor
     *
     * @param category a category of projector models for which the color temperature is available
     * @param name the name of the color temperature
     * @param dataCode the data code identifying the color temperature
     */
    private SonyProjectorColorTemp(int category, String name, byte[] dataCode) {
        this.category = category;
        this.name = name;
        this.dataCode = dataCode;
    }

    /**
     * Get the category of projector models for the current color temperature
     *
     * @return the category of projector models
     */
    public int getCategory() {
        return category;
    }

    /**
     * Get the data code identifying the current color temperature
     *
     * @return the data code
     */
    public byte[] getDataCode() {
        return dataCode;
    }

    /**
     * Get the name of the current color temperature
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the list of {@link StateOption} associated to the available color temperatures for a particular category of
     * projector models
     *
     * @param category a category of projector models
     *
     * @return the list of {@link StateOption} associated to the available color temperatures for a provided category of
     *         projector models
     */
    public static List<StateOption> getStateOptions(int category) {
        List<StateOption> options = new ArrayList<>();
        for (SonyProjectorColorTemp value : SonyProjectorColorTemp.values()) {
            if (value.getCategory() == category) {
                options.add(new StateOption(value.getName(), value.getName()));
            }
        }
        return options;
    }

    /**
     * Get the color temperature associated to a name for a particular category of projector models
     *
     * @param category a category of projector models
     * @param name the name used to identify the color temperature
     *
     * @return the color temperature associated to the searched name for the provided category of projector models
     *
     * @throws SonyProjectorException - If no color temperature is associated to the searched name for the provided
     *             category
     */
    public static SonyProjectorColorTemp getFromName(int category, String name) throws SonyProjectorException {
        for (SonyProjectorColorTemp value : SonyProjectorColorTemp.values()) {
            if (value.getCategory() == category && value.getName().equals(name)) {
                return value;
            }
        }
        throw new SonyProjectorException("Invalid name for a color temperature: " + name);
    }

    /**
     * Get the color temperature associated to a data code for a particular category of projector models
     *
     * @param category a category of projector models
     * @param dataCode the data code used to identify the color temperature
     *
     * @return the color temperature associated to the searched data code for the provided category of projector models
     *
     * @throws SonyProjectorException - If no color temperature is associated to the searched data code for the provided
     *             category
     */
    public static SonyProjectorColorTemp getFromDataCode(int category, byte[] dataCode) throws SonyProjectorException {
        for (SonyProjectorColorTemp value : SonyProjectorColorTemp.values()) {
            if (value.getCategory() == category && Arrays.equals(dataCode, value.getDataCode())) {
                return value;
            }
        }
        throw new SonyProjectorException("Invalid data code for a color temperature: " + HexUtils.bytesToHex(dataCode));
    }
}
