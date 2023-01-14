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
 * Represents the different video inputs available for the projector
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public enum SonyProjectorInput {

    // Category 1: VW260, VW270, VW285, VW295, VW300, VW315, VW320, VW328, VW350, VW365, VW385, VW500, VW515, VW520,
    // VW528, VW550, VW570, VW600, VW665, VW675, VW695, VW760, VW870, VW885, VW995, HW45ES, HW60, HW65, HW68
    CAT1_HDMI1(1, "HDMI1", new byte[] { 0x00, 0x02 }),
    CAT1_HDMI2(1, "HDMI2", new byte[] { 0x00, 0x03 }),

    // Category 2: VW40, VW50, VW60, VW70, VW80, VW85, VW200, HW10, HW15, HW20
    CAT2_VIDEO(2, "Video", new byte[] { 0x00, 0x00 }),
    CAT2_SVIDEO(2, "SVideo", new byte[] { 0x00, 0x01 }),
    CAT2_INPUT_A(2, "InputA", new byte[] { 0x00, 0x02 }),
    CAT2_COMPONENT(2, "Component", new byte[] { 0x00, 0x03 }),
    CAT2_HDMI1(2, "HDMI1", new byte[] { 0x00, 0x04 }),
    CAT2_HDMI2(2, "HDMI2", new byte[] { 0x00, 0x05 }),

    // Category 3: VW95, VW1000ES, VW1100ES, HW30ES, HW40ES, HW50ES, HW55ES, HW58ES
    CAT3_INPUT_A(3, "InputA", new byte[] { 0x00, 0x02 }),
    CAT3_COMPONENT(3, "Component", new byte[] { 0x00, 0x03 }),
    CAT3_HDMI1(3, "HDMI1", new byte[] { 0x00, 0x04 }),
    CAT3_HDMI2(3, "HDMI2", new byte[] { 0x00, 0x05 }),

    // Category 4: VW100
    CAT4_VIDEO(4, "Video", new byte[] { 0x00, 0x00 }),
    CAT4_SVIDEO(4, "SVideo", new byte[] { 0x00, 0x01 }),
    CAT4_INPUT_A(4, "InputA", new byte[] { 0x00, 0x02 }),
    CAT4_COMPONENT(4, "Component", new byte[] { 0x00, 0x03 }),
    CAT4_HDMI(4, "HDMI", new byte[] { 0x00, 0x04 }),
    CAT4_DVI(4, "DVI", new byte[] { 0x00, 0x05 }),

    // Category 5: VW90
    CAT5_VIDEO(5, "Video", new byte[] { 0x00, 0x00 }),
    CAT5_INPUT_A(5, "InputA", new byte[] { 0x00, 0x02 }),
    CAT5_COMPONENT(5, "Component", new byte[] { 0x00, 0x03 }),
    CAT5_HDMI1(5, "HDMI1", new byte[] { 0x00, 0x04 }),
    CAT5_HDMI2(5, "HDMI2", new byte[] { 0x00, 0x05 });

    private int category;
    private String name;
    private byte[] dataCode;

    /**
     * Constructor
     *
     * @param category a category of projector models for which the video input is available
     * @param name the name of the video input
     * @param dataCode the data code identifying the video input
     */
    private SonyProjectorInput(int category, String name, byte[] dataCode) {
        this.category = category;
        this.name = name;
        this.dataCode = dataCode;
    }

    /**
     * Get the category of projector models for the current video input
     *
     * @return the category of projector models
     */
    public int getCategory() {
        return category;
    }

    /**
     * Get the data code identifying the current video input
     *
     * @return the data code
     */
    public byte[] getDataCode() {
        return dataCode;
    }

    /**
     * Get the name of the current video input
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the list of {@link StateOption} associated to the available video inputs for a particular category of
     * projector models
     *
     * @param category a category of projector models
     *
     * @return the list of {@link StateOption} associated to the available video inputs for a provided category of
     *         projector models
     */
    public static List<StateOption> getStateOptions(int category) {
        List<StateOption> options = new ArrayList<>();
        for (SonyProjectorInput value : SonyProjectorInput.values()) {
            if (value.getCategory() == category) {
                options.add(new StateOption(value.getName(), value.getName()));
            }
        }
        return options;
    }

    /**
     * Get the video input associated to a name for a particular category of projector models
     *
     * @param category a category of projector models
     * @param name the name used to identify the video input
     *
     * @return the video input associated to the searched name for the provided category of projector models
     *
     * @throws SonyProjectorException - If no video input is associated to the searched name for the provided category
     */
    public static SonyProjectorInput getFromName(int category, String name) throws SonyProjectorException {
        for (SonyProjectorInput value : SonyProjectorInput.values()) {
            if (value.getCategory() == category && value.getName().equals(name)) {
                return value;
            }
        }
        throw new SonyProjectorException("Invalid name for a video input: " + name);
    }

    /**
     * Get the video input associated to a data code for a particular category of projector models
     *
     * @param category a category of projector models
     * @param dataCode the data code used to identify the video input
     *
     * @return the video input associated to the searched data code for the provided category of projector models
     *
     * @throws SonyProjectorException - If no video input is associated to the searched data code for the provided
     *             category
     */
    public static SonyProjectorInput getFromDataCode(int category, byte[] dataCode) throws SonyProjectorException {
        for (SonyProjectorInput value : SonyProjectorInput.values()) {
            if (value.getCategory() == category && Arrays.equals(dataCode, value.getDataCode())) {
                return value;
            }
        }
        throw new SonyProjectorException("Invalid data code for a video input: " + HexUtils.bytesToHex(dataCode));
    }
}
