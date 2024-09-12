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
import org.openhab.core.types.CommandOption;
import org.openhab.core.types.StateOption;
import org.openhab.core.util.HexUtils;

/**
 * Represents the different aspect modes available for the projector
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public enum SonyProjectorAspect {

    // Category 1: VW260, VW270, VW285, VW295, VW300, VW315, VW320, VW328, VW350, VW365, VW385, VW500, VW515, VW520,
    // VW528, VW550, VW570, VW600, VW665, VW675, VW695, VW760, VW885, VW1000ES, VW1100ES
    CAT1_NORMAL(1, "Normal", new byte[] { 0x00, 0x01 }, new byte[] { 0x1B, 0x41 }),
    CAT1_V_STRETCH(1, "VStretch", new byte[] { 0x00, 0x0B }, new byte[] { 0x1B, 0x44 }),
    CAT1_185(1, "185", new byte[] { 0x00, 0x0C }, new byte[] { 0x1B, 0x45 }),
    CAT1_235(1, "235", new byte[] { 0x00, 0x0D }, new byte[] { 0x1B, 0x46 }),
    CAT1_STRETCH(1, "Stretch", new byte[] { 0x00, 0x0E }, new byte[] { 0x1B, 0x47 }),
    CAT1_SQUEEZE(1, "Squeeze", new byte[] { 0x00, 0x0F }, new byte[] { 0x1B, 0x48 }),

    // Category 2: VW40, VW50, HW10, HW15, HW20, HW30
    CAT2_FULL(2, "Full", new byte[] { 0x00, 0x00 }, new byte[] { 0x1B, 0x42 }),
    CAT2_NORMAL(2, "Normal", new byte[] { 0x00, 0x01 }, new byte[] { 0x1B, 0x41 }),
    CAT2_WIDE(2, "WideZoom", new byte[] { 0x00, 0x02 }, new byte[] { 0x1B, 0x3E }),
    CAT2_ZOOM(2, "Zoom", new byte[] { 0x00, 0x03 }, new byte[] { 0x1B, 0x43 }),
    CAT2_FULL1(2, "Full1", new byte[] { 0x00, 0x07 }, new byte[] { 0x1B, 0x3F }),
    CAT2_FULL2(2, "Full2", new byte[] { 0x00, 0x08 }, new byte[] { 0x1B, 0x40 }),

    // Category 3: VW60, VW70, VW80, VW85, VW90, VW95, VW200
    CAT3_FULL(3, "Full", new byte[] { 0x00, 0x00 }, new byte[] { 0x1B, 0x42 }),
    CAT3_NORMAL(3, "Normal", new byte[] { 0x00, 0x01 }, new byte[] { 0x1B, 0x41 }),
    CAT3_WIDE(3, "WideZoom", new byte[] { 0x00, 0x02 }, new byte[] { 0x1B, 0x3E }),
    CAT3_ZOOM(3, "Zoom", new byte[] { 0x00, 0x03 }, new byte[] { 0x1B, 0x43 }),
    CAT3_FULL1(3, "Full1", new byte[] { 0x00, 0x07 }, new byte[] { 0x1B, 0x3F }),
    CAT3_FULL2(3, "Full2", new byte[] { 0x00, 0x08 }, new byte[] { 0x1B, 0x40 }),
    CAT3_ANAMORPHIC(3, "Anamorphic", new byte[] { 0x00, 0x0B }, new byte[] { 0x1B, 0x44 }),

    // Category 4: VW100
    CAT4_FULL(4, "Full", new byte[] { 0x00, 0x00 }, new byte[] { 0x1B, 0x42 }),
    CAT4_NORMAL(4, "Normal", new byte[] { 0x00, 0x01 }, new byte[] { 0x1B, 0x41 }),
    CAT4_WIDE(4, "WideZoom", new byte[] { 0x00, 0x02 }, new byte[] { 0x1B, 0x3E }),
    CAT4_ZOOM(4, "Zoom", new byte[] { 0x00, 0x03 }, new byte[] { 0x1B, 0x43 }),
    CAT3_SUBTITLE(4, "Subtitle", new byte[] { 0x00, 0x04 }, new byte[] { 0x1B, 0x49 }), // IR code not confirmed

    // Category 5: HW40ES, HW50ES, HW55ES, HW58ES
    CAT5_FULL(5, "Full", new byte[] { 0x00, 0x00 }, new byte[] { 0x1B, 0x42 }),
    CAT5_NORMAL(5, "Normal", new byte[] { 0x00, 0x01 }, new byte[] { 0x1B, 0x41 }),
    CAT5_WIDE(5, "WideZoom", new byte[] { 0x00, 0x02 }, new byte[] { 0x1B, 0x3E }),
    CAT5_ZOOM(5, "Zoom", new byte[] { 0x00, 0x03 }, new byte[] { 0x1B, 0x43 }),
    CAT5_V_STRETCH(5, "VStretch", new byte[] { 0x00, 0x0B }, new byte[] { 0x1B, 0x44 }),
    CAT5_STRETCH(5, "Stretch", new byte[] { 0x00, 0x0E }, new byte[] { 0x1B, 0x47 }),
    CAT5_SQUEEZE(5, "Squeeze", new byte[] { 0x00, 0x0F }, new byte[] { 0x1B, 0x48 }),

    // Category 6: HW45ES, HW60, HW65, HW68
    CAT6_NORMAL(6, "Normal", new byte[] { 0x00, 0x01 }, new byte[] { 0x1B, 0x41 }),
    CAT6_V_STRETCH(6, "VStretch", new byte[] { 0x00, 0x0B }, new byte[] { 0x1B, 0x44 }),
    CAT6_STRETCH(6, "Stretch", new byte[] { 0x00, 0x0E }, new byte[] { 0x1B, 0x47 }),
    CAT6_SQUEEZE(6, "Squeeze", new byte[] { 0x00, 0x0F }, new byte[] { 0x1B, 0x48 });

    private int category;
    private String name;
    private byte[] dataCode;
    private byte[] irCode;

    /**
     * Constructor
     *
     * @param category a category of projector models for which the aspect mode is available
     * @param name the name of the aspect mode
     * @param dataCode the data code identifying the aspect mode
     * @param irCode the IR code for the aspect mode
     */
    private SonyProjectorAspect(int category, String name, byte[] dataCode, byte[] irCode) {
        this.category = category;
        this.name = name;
        this.dataCode = dataCode;
        this.irCode = irCode;
    }

    /**
     * Get the category of projector models for the current aspect mode
     *
     * @return the category of projector models
     */
    public int getCategory() {
        return category;
    }

    /**
     * Get the data code identifying the current aspect mode
     *
     * @return the data code
     */
    public byte[] getDataCode() {
        return dataCode;
    }

    /**
     * Get the IR code for the current aspect mode
     *
     * @return the IR code
     */
    public byte[] getIrCode() {
        return irCode;
    }

    /**
     * Get the name of the current aspect mode
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the list of {@link StateOption} associated to the available aspect modes for a particular category of
     * projector models
     *
     * @param category a category of projector models
     *
     * @return the list of {@link StateOption} associated to the available aspect modes for a provided category of
     *         projector models
     */
    public static List<StateOption> getStateOptions(int category) {
        List<StateOption> options = new ArrayList<>();
        for (SonyProjectorAspect value : SonyProjectorAspect.values()) {
            if (value.getCategory() == category) {
                options.add(new StateOption(value.getName(), value.getName()));
            }
        }
        return options;
    }

    /**
     * Get the list of {@link CommandOption} associated to the available aspect modes for a particular category of
     * projector models
     *
     * @param category a category of projector models
     *
     * @return the list of {@link CommandOption} associated to the available aspect modes for a provided category of
     *         projector models
     */
    public static List<CommandOption> getCommandOptions(int category) {
        List<CommandOption> options = new ArrayList<>();
        for (SonyProjectorAspect value : SonyProjectorAspect.values()) {
            if (value.getCategory() == category) {
                options.add(new CommandOption("ASPECT_" + value.getName().toUpperCase(),
                        "@text/channel-type.sonyprojector.aspect.state.option." + value.getName()));
            }
        }
        return options;
    }

    /**
     * Get the aspect mode associated to a name for a particular category of projector models
     *
     * @param category a category of projector models
     * @param name the name used to identify the aspect mode
     *
     * @return the aspect mode associated to the searched name for the provided category of projector models
     *
     * @throws SonyProjectorException - If no aspect mode is associated to the searched name for the provided category
     */
    public static SonyProjectorAspect getFromName(int category, String name) throws SonyProjectorException {
        for (SonyProjectorAspect value : SonyProjectorAspect.values()) {
            if (value.getCategory() == category && value.getName().equalsIgnoreCase(name)) {
                return value;
            }
        }
        throw new SonyProjectorException("Invalid name for an aspect mode: " + name);
    }

    /**
     * Get the aspect mode associated to a data code for a particular category of projector models
     *
     * @param category a category of projector models
     * @param dataCode the data code used to identify the aspect mode
     *
     * @return the aspect mode associated to the searched data code for the provided category of projector models
     *
     * @throws SonyProjectorException - If no aspect mode is associated to the searched data code for the provided
     *             category
     */
    public static SonyProjectorAspect getFromDataCode(int category, byte[] dataCode) throws SonyProjectorException {
        for (SonyProjectorAspect value : SonyProjectorAspect.values()) {
            if (value.getCategory() == category && Arrays.equals(dataCode, value.getDataCode())) {
                return value;
            }
        }
        throw new SonyProjectorException("Invalid data code for an aspect mode: " + HexUtils.bytesToHex(dataCode));
    }
}
