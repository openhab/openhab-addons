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
 * Represents the different gamma corrections available for the projector
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public enum SonyProjectorGammaCorrection {

    // Category 1: VW260, VW270, VW285, VW295, VW300, VW315, VW320, VW328, VW350, VW365, VW385, VW500, VW515, VW520,
    // VW528, VW550, VW570, VW600, VW665, VW675, VW695, VW760, VW870, VW885, VW995, VW1000ES, VW1100ES, HW50ES, HW55ES,
    // HW60, HW65, HW68
    CAT1_18(1, "1.8", new byte[] { 0x00, 0x01 }),
    CAT1_20(1, "2.0", new byte[] { 0x00, 0x02 }),
    CAT1_21(1, "2.1", new byte[] { 0x00, 0x03 }),
    CAT1_22(1, "2.2", new byte[] { 0x00, 0x04 }),
    CAT1_24(1, "2.4", new byte[] { 0x00, 0x05 }),
    CAT1_26(1, "2.6", new byte[] { 0x00, 0x06 }),
    CAT1_GAMMA7(1, "Gamma7", new byte[] { 0x00, 0x07 }),
    CAT1_GAMMA8(1, "Gamma8", new byte[] { 0x00, 0x08 }),
    CAT1_GAMMA9(1, "Gamma9", new byte[] { 0x00, 0x09 }),
    CAT1_GAMMA10(1, "Gamma10", new byte[] { 0x00, 0x0A }),
    CAT1_OFF(1, "Off", new byte[] { 0x00, 0x00 }),

    // Category 2: VW40, VW50, VW60, VW100, VW200, HW10
    CAT2_GAMMA1(2, "Gamma1", new byte[] { 0x00, 0x01 }),
    CAT2_GAMMA2(2, "Gamma2", new byte[] { 0x00, 0x02 }),
    CAT2_GAMMA3(2, "Gamma3", new byte[] { 0x00, 0x03 }),
    CAT2_OFF(2, "Off", new byte[] { 0x00, 0x00 }),

    // Category 3: VW70, VW80, HW15, HW20
    CAT3_GAMMA1(3, "Gamma1", new byte[] { 0x00, 0x01 }),
    CAT3_GAMMA2(3, "Gamma2", new byte[] { 0x00, 0x02 }),
    CAT3_GAMMA3(3, "Gamma3", new byte[] { 0x00, 0x03 }),
    CAT3_GAMMA4(3, "Gamma4", new byte[] { 0x00, 0x04 }),
    CAT3_GAMMA5(3, "Gamma5", new byte[] { 0x00, 0x05 }),
    CAT3_GAMMA6(3, "Gamma6", new byte[] { 0x00, 0x06 }),
    CAT3_OFF(3, "Off", new byte[] { 0x00, 0x00 }),

    // Category 4: HW30ES
    CAT4_GAMMA1(4, "Gamma1", new byte[] { 0x00, 0x01 }),
    CAT4_GAMMA2(4, "Gamma2", new byte[] { 0x00, 0x02 }),
    CAT4_GAMMA3(4, "Gamma3", new byte[] { 0x00, 0x03 }),
    CAT4_GAMMA4(4, "Gamma4", new byte[] { 0x00, 0x04 }),
    CAT4_GAMMA5(4, "Gamma5", new byte[] { 0x00, 0x05 }),
    CAT4_GAMMA6(4, "Gamma6", new byte[] { 0x00, 0x06 }),
    CAT4_GAMMA7(4, "Gamma7", new byte[] { 0x00, 0x07 }),
    CAT4_GAMMA8(4, "Gamma8", new byte[] { 0x00, 0x08 }),
    CAT4_OFF(4, "Off", new byte[] { 0x00, 0x00 }),

    // Category 5: VW85, VW90, VW95
    CAT5_GAMMA1(5, "Gamma1", new byte[] { 0x00, 0x01 }),
    CAT5_GAMMA2(5, "Gamma2", new byte[] { 0x00, 0x02 }),
    CAT5_GAMMA3(5, "Gamma3", new byte[] { 0x00, 0x03 }),
    CAT5_GAMMA4(5, "Gamma4", new byte[] { 0x00, 0x04 }),
    CAT5_GAMMA5(5, "Gamma5", new byte[] { 0x00, 0x05 }),
    CAT5_GAMMA6(5, "Gamma6", new byte[] { 0x00, 0x06 }),
    CAT5_GAMMA7(5, "Gamma7", new byte[] { 0x00, 0x07 }),
    CAT5_GAMMA8(5, "Gamma8", new byte[] { 0x00, 0x08 }),
    CAT5_GAMMA9(5, "Gamma9", new byte[] { 0x00, 0x09 }),
    CAT5_GAMMA10(5, "Gamma10", new byte[] { 0x00, 0x0A }),
    CAT5_OFF(5, "Off", new byte[] { 0x00, 0x00 }),

    // Category 6: HW35ES, HW40ES, HW45ES, HW58ES
    CAT6_20(6, "2.0", new byte[] { 0x00, 0x02 }),
    CAT6_22(6, "2.2", new byte[] { 0x00, 0x04 }),
    CAT6_24(6, "2.4", new byte[] { 0x00, 0x05 }),
    CAT6_GAMMA4(6, "Gamma4", new byte[] { 0x00, 0x08 }),
    CAT6_GAMMA5(6, "Gamma5", new byte[] { 0x00, 0x09 }),
    CAT6_GAMMA6(6, "Gamma6", new byte[] { 0x00, 0x0A }),
    CAT6_OFF(6, "Off", new byte[] { 0x00, 0x00 });

    private int category;
    private String name;
    private byte[] dataCode;

    /**
     * Constructor
     *
     * @param category a category of projector models for which the gamma correction is available
     * @param name the name of the gamma correction
     * @param dataCode the data code identifying the gamma correction
     */
    private SonyProjectorGammaCorrection(int category, String name, byte[] dataCode) {
        this.category = category;
        this.name = name;
        this.dataCode = dataCode;
    }

    /**
     * Get the category of projector models for the current gamma correction
     *
     * @return the category of projector models
     */
    public int getCategory() {
        return category;
    }

    /**
     * Get the data code identifying the current gamma correction
     *
     * @return the data code
     */
    public byte[] getDataCode() {
        return dataCode;
    }

    /**
     * Get the name of the current gamma correction
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the list of {@link StateOption} associated to the available gamma corrections for a particular category of
     * projector models
     *
     * @param category a category of projector models
     *
     * @return the list of {@link StateOption} associated to the available gamma corrections for a provided category of
     *         projector models
     */
    public static List<StateOption> getStateOptions(int category) {
        List<StateOption> options = new ArrayList<>();
        for (SonyProjectorGammaCorrection value : SonyProjectorGammaCorrection.values()) {
            if (value.getCategory() == category) {
                options.add(new StateOption(value.getName(), value.getName()));
            }
        }
        return options;
    }

    /**
     * Get the gamma correction associated to a name for a particular category of projector models
     *
     * @param category a category of projector models
     * @param name the name used to identify the gamma correction
     *
     * @return the gamma correction associated to the searched name for the provided category of projector models
     *
     * @throws SonyProjectorException - If no gamma correction is associated to the searched name for the provided
     *             category
     */
    public static SonyProjectorGammaCorrection getFromName(int category, String name) throws SonyProjectorException {
        for (SonyProjectorGammaCorrection value : SonyProjectorGammaCorrection.values()) {
            if (value.getCategory() == category && value.getName().equals(name)) {
                return value;
            }
        }
        throw new SonyProjectorException("Invalid nam for a gamma correctione: " + name);
    }

    /**
     * Get the gamma correction associated to a data code for a particular category of projector models
     *
     * @param category a category of projector models
     * @param dataCode the data code used to identify the gamma correction
     *
     * @return the gamma correction associated to the searched data code for the provided category of projector models
     *
     * @throws SonyProjectorException - If no gamma correction is associated to the searched data code for the provided
     *             category
     */
    public static SonyProjectorGammaCorrection getFromDataCode(int category, byte[] dataCode)
            throws SonyProjectorException {
        for (SonyProjectorGammaCorrection value : SonyProjectorGammaCorrection.values()) {
            if (value.getCategory() == category && Arrays.equals(dataCode, value.getDataCode())) {
                return value;
            }
        }
        throw new SonyProjectorException("Invalid data code for a gamma correctione: " + HexUtils.bytesToHex(dataCode));
    }
}
