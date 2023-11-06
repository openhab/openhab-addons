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

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Represents the different kinds of commands
 *
 * @author Markus Wehrle - Initial contribution
 * @author Laurent Garnier - Transform into an enum and rename it
 */
@NonNullByDefault
public enum SonyProjectorItem {

    // Not available for VW40, VW50, VW60, VW70, VW80, VW85, VW90, VW95, VW100, VW200, VW1000ES, VW1100ES,
    // HW10, HW15, HW20, HW30ES, HW35ES, HW40ES, HW50ES, HW55ES, HW58ES
    POWER("Power", new byte[] { 0x01, 0x30 }),

    INPUT("Input", new byte[] { 0x00, 0x01 }),
    CALIBRATION_PRESET("Calibration Preset", new byte[] { 0x00, 0x02 }),
    CONTRAST("Contrast", new byte[] { 0x00, 0x10 }),
    BRIGHTNESS("Brigtness", new byte[] { 0x00, 0x11 }),
    COLOR("Color", new byte[] { 0x00, 0x12 }),
    HUE("Hue", new byte[] { 0x00, 0x13 }),
    SHARPNESS("Sharpness", new byte[] { 0x00, 0x14 }),
    COLOR_TEMP("Color Temperature", new byte[] { 0x00, 0x17 }),

    // Not available for VW100, VW200
    LAMP_CONTROL("Lamp Control", new byte[] { 0x00, 0x1A }),

    CONTRAST_ENHANCER("Contrast Enhancer", new byte[] { 0x00, 0x1C }),

    // Not available for VW260, VW270, VW285, VW295, VW300, VW315, VW320, VW328, VW350, VW365, HW35ES, HW40ES, HW45ES,
    // HW58ES
    IRIS_MODE("Iris Mode", new byte[] { 0x00, 0x1D }),

    // Not available for VW315, VW320, VW328, VW365, VW515, VW520, VW528, VW665, HW60, HW65, HW68
    REAL_COLOR("Real Color Processing", new byte[] { 0x00, 0x1E }),

    // Not available for VW40, VW50, VW60
    FILM_MODE("Film Mode", new byte[] { 0x00, 0x1F }),

    ASPECT("Aspect", new byte[] { 0x00, 0x20 }),
    GAMMA_CORRECTION("Gamma Correction", new byte[] { 0x00, 0x22 }),
    COLOR_SPACE("Color Space", new byte[] { 0x00, 0x3B }),
    PICTURE_MUTING("Picture Muting", new byte[] { 0x00, 0x30 }),
    NR("NR", new byte[] { 0x00, 0x25 }),

    // Not available for VW40, VW50, VW60, VW100, VW200, VW260, VW270, VW285, VW295, VW300, VW315, VW320, VW328, VW350,
    // VW365, VW385, VW500, VW515, VW520, VW528, VW550, VW570, VW600, VW665, VW675, VW760, VW870, VW885, VW995,
    // VW1000ES, VW1100ES, HW35ES, HW40ES, HW45ES, HW50ES, HW55ES, HW58ES, HW60, HW65, HW68
    BLOCK_NR("Block NR", new byte[] { 0x00, 0x26 }),
    MOSQUITO_NR("Mosquito NR", new byte[] { 0x00, 0x27 }),

    // Not available for VW40, VW50, VW60, VW70, VW80, VW85, VW90, VW95, VW100, VW200, HW10, HW15, HW20, HW30ES
    MPEG_NR("MPEG NR", new byte[] { 0x00, 0x6C }),

    // Not available for VW260, VW270, VW285, VW295, VW300, VW315, VW320, VW328, VW350, VW365, VW385, VW500, VW515,
    // VW520, VW528, VW550, VW570, VW600, VW665, VW675, VW760, VW870, VW885, VW995, HW45ES, HW60, HW65, HW68
    OVERSCAN("Overscan", new byte[] { 0x00, 0x23 }),

    // Not available for VW100, VW260, VW270, VW285, VW295, VW300, VW315, VW320, VW328, VW350, VW365, VW385, VW500,
    // VW515, VW520, VW528, VW550, VW570, VW600, VW665, VW675, VW760, VW870, VW885, VW995, VW1000ES, VW1100ES, HW35ES,
    // HW40ES, HW45ES, HW50ES, HW55ES, HW58ES, HW60, HW65, HW68
    IRIS_SENSITIVITY("Iris Sensitivity", new byte[] { 0x00, 0x56 }),

    // Not available for VW100, VW260, VW270, VW285, VW295, VW300, VW315, VW320, VW328, VW350, VW365, VW760, VW870,
    // VW885, VW995, HW35ES, HW40ES, HW45ES, HW58ES
    IRIS_MANUAL("Iris Manual", new byte[] { 0x00, 0x57 }),

    // Not available for VW40, VW50, VW60, VW70, VW100, VW260, VW270, VW285, VW295, VW300, VW315, VW320, VW328, VW350,
    // VW365, VW385, VW500, VW515, VW520, VW528, VW570, VW600, VW665, VW675, VW695, VW760, VW870, VW885, VW995, HW10,
    // HW15, HW20, HW30ES, HW45ES, HW60, HW65, HW68
    FILM_PROJECTION("Film Projection", new byte[] { 0x00, 0x58 }),

    // Not available for VW40, VW50, VW60, VW70, VW100, HW10, HW15, HW20, HW30ES
    MOTION_ENHANCER("Motion Enhancer", new byte[] { 0x00, 0x59 }),

    // Not available for VW40, VW50, VW60, VW100
    XVCOLOR("xvColor", new byte[] { 0x00, 0x5A }),

    // Not available for VW40, VW50, VW60, VW70, VW80, VW85, VW90, VW100, VW200, VW260, VW270, VW285, VW295, VW300,
    // VW315, VW320, VW328, VW350, VW365, HW10, HW15, HW20, HW30ES, HW35ES, HW40ES, HW45ES, HW50ES, HW55ES, HW58ES,
    // HW60, HW65, HW68
    PICTURE_POSITION("Picture Position", new byte[] { 0x00, 0x66 }),

    // Not available for VW40, VW50, VW60, VW70, VW85, VW95, VW100, HW15, HW20, HW30ES
    REALITY_CREATION("Reality Creation", new byte[] { 0x00, 0x67 }),

    // Not available for VW40, VW50, VW60, VW70, VW85, VW95, VW100, VW315, VW320, VW328, VW365, VW1000ES, VW1100ES
    // HW15, HW20, HW30ES, HW35ES, HW40ES, HW50ES, HW55ES, HW58ES, HW60, HW65, HW68
    HDR("HDR", new byte[] { 0x00, 0x7C }),

    // Not available for VW40, VW50, VW60, VW70, VW85, VW95, VW100, VW1000ES, VW1100ES
    // HW15, HW20, HW30ES, HW35ES, HW40ES, HW50ES, HW55ES, HW58ES
    INPUT_LAG_REDUCTION("Input Lag Reduction", new byte[] { 0x00, (byte) 0x99 }),

    STATUS_ERROR("Status Error", new byte[] { 0x01, 0x01 }),
    STATUS_POWER("Status Power", new byte[] { 0x01, 0x02 }),
    LAMP_USE_TIME("Lamp Use Time", new byte[] { 0x01, 0x13 }),

    // Not available for VW40, VW50, VW60, VW70, VW100
    STATUS_ERROR2("Status Error 2", new byte[] { 0x01, 0x25 }),

    IR_POWER_ON("Power On", new byte[] { 0x17, 0x2E }),
    IR_POWER_OFF("Power Off", new byte[] { 0x17, 0x2F }),

    CATEGORY_CODE("Category Code", new byte[] { (byte) 0x80, 0x00 }),
    MODEL_NAME("Model Name", new byte[] { (byte) 0x80, 0x01 }),
    SERIAL_NUMBER("Serial Number", new byte[] { (byte) 0x80, 0x02 }),
    INSTALLATION_LOCATION("Installation Location", new byte[] { (byte) 0x80, 0x03 });

    private String name;
    private byte[] code;

    /**
     *
     * @param name the item name
     * @param code the data code associated to the item
     */
    private SonyProjectorItem(String name, byte[] code) {
        this.name = name;
        this.code = code;
    }

    /**
     * Get the data code associated to the current item
     *
     * @return the data code
     */
    public byte[] getCode() {
        return code;
    }

    /**
     * Get the item name
     *
     * @return the name
     */
    public String getName() {
        return name;
    }
}
