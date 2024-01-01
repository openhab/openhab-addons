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
 * Represents the different iris modes available for the projector
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public enum SonyProjectorIrisMode {

    // Category 1: VW385, VW500, VW515, VW520, VW528, VW550, VW570, VW600, VW665, VW675, VW695, VW760, VW870, VW885,
    // VW995, HW60, HW65, HW68
    CAT1_FULL(1, "Full", new byte[] { 0x00, 0x02 }),
    CAT1_LIMITED(1, "Limited", new byte[] { 0x00, 0x03 }),
    CAT1_OFF(1, "Off", new byte[] { 0x00, 0x00 }),

    // Category 2: VW40, VW50, VW60
    CAT2_ON(2, "On", new byte[] { 0x00, 0x01 }),
    CAT2_AUTO1(2, "Auto1", new byte[] { 0x00, 0x02 }),
    CAT2_AUTO2(2, "Auto2", new byte[] { 0x00, 0x03 }),
    CAT2_OFF(2, "Off", new byte[] { 0x00, 0x00 }),

    // Category 3: VW70, VW80, VW85, VW90, VW95, VW200, HW10, HW15, HW20, HW30ES
    CAT3_AUTO1(3, "Auto1", new byte[] { 0x00, 0x02 }),
    CAT3_AUTO2(3, "Auto2", new byte[] { 0x00, 0x03 }),
    CAT3_MANUAL(3, "Manual", new byte[] { 0x00, 0x01 }),
    CAT3_OFF(3, "Off", new byte[] { 0x00, 0x00 }),

    // Category 4: VW100
    CAT4_ON(4, "On", new byte[] { 0x00, 0x01 }),
    CAT4_AUTO(4, "Auto", new byte[] { 0x00, 0x02 }),
    CAT4_OFF(4, "Off", new byte[] { 0x00, 0x00 }),

    // Category 5: VW1000ES, VW1100ES, HW50ES, HW55ES
    CAT5_AUTO_FULL(5, "AutoFull", new byte[] { 0x00, 0x02 }),
    CAT5_AUTO_LIMITED(5, "AutoLimited", new byte[] { 0x00, 0x03 }),
    CAT5_MANUAL(5, "Manual", new byte[] { 0x00, 0x01 }),
    CAT5_OFF(5, "Off", new byte[] { 0x00, 0x00 });

    private int category;
    private String name;
    private byte[] dataCode;

    /**
     * Constructor
     *
     * @param category a category of projector models for which the iris mode is available
     * @param name the name of the iris mode
     * @param dataCode the data code identifying the iris mode
     */
    private SonyProjectorIrisMode(int category, String name, byte[] dataCode) {
        this.category = category;
        this.name = name;
        this.dataCode = dataCode;
    }

    /**
     * Get the category of projector models for the current iris mode
     *
     * @return the category of projector models
     */
    public int getCategory() {
        return category;
    }

    /**
     * Get the data code identifying the current iris mode
     *
     * @return the data code
     */
    public byte[] getDataCode() {
        return dataCode;
    }

    /**
     * Get the name of the current iris mode
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the list of {@link StateOption} associated to the available iris modes for a particular category of projector
     * models
     *
     * @param category a category of projector models
     *
     * @return the list of {@link StateOption} associated to the available iris modes for a provided category of
     *         projector models
     */
    public static List<StateOption> getStateOptions(int category) {
        List<StateOption> options = new ArrayList<>();
        for (SonyProjectorIrisMode value : SonyProjectorIrisMode.values()) {
            if (value.getCategory() == category) {
                options.add(new StateOption(value.getName(), value.getName()));
            }
        }
        return options;
    }

    /**
     * Get the iris mode associated to a name for a particular category of projector models
     *
     * @param category a category of projector models
     * @param name the name used to identify the iris mode
     *
     * @return the iris mode associated to the searched name for the provided category of projector models
     *
     * @throws SonyProjectorException - If no iris mode is associated to the searched name for the provided category
     */
    public static SonyProjectorIrisMode getFromName(int category, String name) throws SonyProjectorException {
        for (SonyProjectorIrisMode value : SonyProjectorIrisMode.values()) {
            if (value.getCategory() == category && value.getName().equals(name)) {
                return value;
            }
        }
        throw new SonyProjectorException("Invalid name for an iris mode: " + name);
    }

    /**
     * Get the iris mode associated to a data code for a particular category of projector models
     *
     * @param category a category of projector models
     * @param dataCode the data code used to identify the iris mode
     *
     * @return the iris mode associated to the searched data code for the provided category of projector models
     *
     * @throws SonyProjectorException - If no iris mode is associated to the searched data code for the provided
     *             category
     */
    public static SonyProjectorIrisMode getFromDataCode(int category, byte[] dataCode) throws SonyProjectorException {
        for (SonyProjectorIrisMode value : SonyProjectorIrisMode.values()) {
            if (value.getCategory() == category && Arrays.equals(dataCode, value.getDataCode())) {
                return value;
            }
        }
        throw new SonyProjectorException("Invalid data code for an iris mode: " + HexUtils.bytesToHex(dataCode));
    }
}
