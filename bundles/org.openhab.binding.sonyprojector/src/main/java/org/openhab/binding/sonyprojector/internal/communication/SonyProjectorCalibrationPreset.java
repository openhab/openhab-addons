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
 * Represents the different calibration presets available for the projector
 *
 * @author Markus Wehrle - Initial contribution
 * @author Laurent Garnier - Transform into an enum and consider differences between models
 */
@NonNullByDefault
public enum SonyProjectorCalibrationPreset {

    // Category 1: VW260, VW270, VW285, VW295, VW300, VW315, VW320, VW328, VW350, VW365, VW385, VW500, VW515, VW520,
    // VW528, VW550, VW570, VW600, VW665, VW675, VW695, VW760, VW870, VW885, VW995, HW40ES, HW45ES, HW50ES, HW55ES,
    // HW58ES, HW60, HW65, HW68
    CAT1_CINEMA_FILM1(1, "Film1", new byte[] { 0x00, 0x00 }),
    CAT1_CINEMA_FILM2(1, "Film2", new byte[] { 0x00, 0x01 }),
    CAT1_REFERENCE(1, "Reference", new byte[] { 0x00, 0x02 }),
    CAT1_TV(1, "TV", new byte[] { 0x00, 0x03 }),
    CAT1_PHOTO(1, "Photo", new byte[] { 0x00, 0x04 }),
    CAT1_GAME(1, "Game", new byte[] { 0x00, 0x05 }),
    CAT1_BRT_CINE(1, "BRTCINE", new byte[] { 0x00, 0x06 }),
    CAT1_BRT_TV(1, "BRTTV", new byte[] { 0x00, 0x07 }),
    CAT1_USER(1, "User", new byte[] { 0x00, 0x08 }),

    // Category 2: VW40, VW50, VW60, VW70, VW80, VW100, VW200, HW10, HW15, HW20
    CAT2_DYNAMIC(2, "Dynamic", new byte[] { 0x00, 0x00 }),
    CAT2_STANDARD(2, "Standard", new byte[] { 0x00, 0x01 }),
    CAT2_CINEMA(2, "Cinema", new byte[] { 0x00, 0x02 }),
    CAT2_USER1(2, "User1", new byte[] { 0x00, 0x03 }),
    CAT2_USER2(2, "User2", new byte[] { 0x00, 0x04 }),
    CAT2_USER3(2, "User3", new byte[] { 0x00, 0x05 }),

    // Category 3: VW85, VW90
    CAT3_DYNAMIC(3, "Dynamic", new byte[] { 0x00, 0x00 }),
    CAT3_STANDARD(3, "Standard", new byte[] { 0x00, 0x01 }),
    CAT3_CINEMA1(3, "Cinema1", new byte[] { 0x00, 0x02 }),
    CAT3_CINEMA2(3, "Cinema2", new byte[] { 0x00, 0x03 }),
    CAT3_CINEMA3(3, "Cinema3", new byte[] { 0x00, 0x04 }),
    CAT3_USER(3, "User", new byte[] { 0x00, 0x05 }),

    // Category 4: VW95, HW30ES
    CAT4_DYNAMIC(4, "Dynamic", new byte[] { 0x00, 0x00 }),
    CAT4_STANDARD(4, "Standard", new byte[] { 0x00, 0x01 }),
    CAT4_CINEMA1(4, "Cinema1", new byte[] { 0x00, 0x02 }),
    CAT4_CINEMA2(4, "Cinema2", new byte[] { 0x00, 0x03 }),
    CAT4_CINEMA3(4, "Cinema3", new byte[] { 0x00, 0x04 }),
    CAT4_GAME(4, "Game", new byte[] { 0x00, 0x05 }),
    CAT4_PHOTO(4, "Photo", new byte[] { 0x00, 0x06 }),
    CAT4_USER1(4, "User1", new byte[] { 0x00, 0x07 }),
    CAT4_USER2(4, "User2", new byte[] { 0x00, 0x08 }),

    // Category 5: VW1000ES, VW1100ES
    CAT5_CINEMA_FILM1(5, "Film1", new byte[] { 0x00, 0x00 }),
    CAT5_CINEMA_FILM2(5, "Film2", new byte[] { 0x00, 0x01 }),
    CAT5_CINEMA_DIGITAL(5, "Digital", new byte[] { 0x00, 0x02 }),
    CAT5_REFERENCE(5, "Reference", new byte[] { 0x00, 0x03 }),
    CAT5_TV(5, "TV", new byte[] { 0x00, 0x04 }),
    CAT5_PHOTO(5, "Photo", new byte[] { 0x00, 0x05 }),
    CAT5_GAME(5, "Game", new byte[] { 0x00, 0x06 }),
    CAT5_BRT_CINE(5, "BRTCINE", new byte[] { 0x00, 0x07 }),
    CAT5_BRT_TV(5, "BRTTV", new byte[] { 0x00, 0x08 });

    private int category;
    private String name;
    private byte[] dataCode;

    /**
     * Constructor
     *
     * @param category a category of projector models for which the calibration preset is available
     * @param name the name of the calibration preset
     * @param dataCode the data code identifying the calibration preset
     */
    private SonyProjectorCalibrationPreset(int category, String name, byte[] dataCode) {
        this.category = category;
        this.name = name;
        this.dataCode = dataCode;
    }

    /**
     * Get the category of projector models for the current calibration preset
     *
     * @return the category of projector models
     */
    public int getCategory() {
        return category;
    }

    /**
     * Get the data code identifying the current calibration preset
     *
     * @return the data code
     */
    public byte[] getDataCode() {
        return dataCode;
    }

    /**
     * Get the name of the current calibration preset
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the list of {@link StateOption} associated to the available calibration presets for a particular category of
     * projector models
     *
     * @param category a category of projector models
     *
     * @return the list of {@link StateOption} associated to the available calibration presets for a provided category
     *         of projector models
     */
    public static List<StateOption> getStateOptions(int category) {
        List<StateOption> options = new ArrayList<>();
        for (SonyProjectorCalibrationPreset value : SonyProjectorCalibrationPreset.values()) {
            if (value.getCategory() == category) {
                options.add(new StateOption(value.getName(), value.getName()));
            }
        }
        return options;
    }

    /**
     * Get the calibration preset associated to a name for a particular category of projector models
     *
     * @param category a category of projector models
     * @param name the name used to identify the calibration preset
     *
     * @return the calibration preset associated to the searched name for the provided category of projector models
     *
     * @throws SonyProjectorException - If no calibration preset is associated to the searched name for the provided
     *             category
     */
    public static SonyProjectorCalibrationPreset getFromName(int category, String name) throws SonyProjectorException {
        for (SonyProjectorCalibrationPreset value : SonyProjectorCalibrationPreset.values()) {
            if (value.getCategory() == category && value.getName().equals(name)) {
                return value;
            }
        }
        throw new SonyProjectorException("Invalid name for a calibration preset: " + name);
    }

    /**
     * Get the calibration preset associated to a data code for a particular category of projector models
     *
     * @param category a category of projector models
     * @param dataCode the data code used to identify the calibration preset
     *
     * @return the calibration preset associated to the searched data code for the provided category of projector models
     *
     * @throws SonyProjectorException - If no calibration preset is associated to the searched data code for the
     *             provided category
     */
    public static SonyProjectorCalibrationPreset getFromDataCode(int category, byte[] dataCode)
            throws SonyProjectorException {
        for (SonyProjectorCalibrationPreset value : SonyProjectorCalibrationPreset.values()) {
            if (value.getCategory() == category && Arrays.equals(dataCode, value.getDataCode())) {
                return value;
            }
        }
        throw new SonyProjectorException(
                "Invalid data code for a calibration preset: " + HexUtils.bytesToHex(dataCode));
    }
}
