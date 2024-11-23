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
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.sonyprojector.internal.SonyProjectorException;
import org.openhab.core.types.CommandOption;

/**
 * Represents the different kinds of commands
 *
 * @author Markus Wehrle - Initial contribution
 * @author Laurent Garnier - Transform into an enum and rename it
 * @author Laurent Garnier - Add more IR commands
 */
@NonNullByDefault
public enum SonyProjectorItem {

    // Not available for VW40, VW50, VW60, VW70, VW80, VW85, VW90, VW95, VW100, VW200, VW1000ES, VW1100ES,
    // HW10, HW15, HW20, HW30ES, HW35ES, HW40ES, HW50ES, HW55ES, HW58ES
    POWER("Power On/Off", new byte[] { 0x01, 0x30 }, new byte[] { 0x17, 0x15 }),
    POWER_ON("Power On", null, new byte[] { 0x17, 0x2E }),
    POWER_OFF("Power Off", null, new byte[] { 0x17, 0x2F }),

    INPUT("Input", new byte[] { 0x00, 0x01 }, new byte[] { 0x17, 0x57 }, "input"),
    CALIBRATION_PRESET("Calibration Preset", new byte[] { 0x00, 0x02 }, new byte[] { 0x19, 0x5B }, "calibrationpreset"),
    CONTRAST("Contrast", new byte[] { 0x00, 0x10 }),
    CONTRAST_UP("Contrast +", null, new byte[] { 0x17, 0x18 }),
    CONTRAST_DOWN("Contrast -", null, new byte[] { 0x17, 0x19 }),
    BRIGHTNESS("Brigtness", new byte[] { 0x00, 0x11 }),
    BRIGHTNESS_UP("Brightness +", null, new byte[] { 0x17, 0x1E }),
    BRIGHTNESS_DOWN("Brightness -", null, new byte[] { 0x17, 0x1F }),
    COLOR("Color", new byte[] { 0x00, 0x12 }),
    COLOR_UP("Color +", null, new byte[] { 0x17, 0x1A }),
    COLOR_DOWN("Color -", null, new byte[] { 0x17, 0x1B }),
    HUE("Hue", new byte[] { 0x00, 0x13 }),
    HUE_UP("Hue +", null, new byte[] { 0x17, 0x20 }),
    HUE_DOWN("Hue -", null, new byte[] { 0x17, 0x21 }),
    SHARPNESS("Sharpness", new byte[] { 0x00, 0x14 }),
    SHARPNESS_UP("Sharpness +", null, new byte[] { 0x17, 0x22 }),
    SHARPNESS_DOWN("Sharpness -", null, new byte[] { 0x17, 0x23 }),
    CONTRAST_ENHANCER("Contrast Enhancer", new byte[] { 0x00, 0x1C }, new byte[] { 0x17, 0x07 }, "contrastenhancer"),
    COLOR_TEMP("Color Temperature", new byte[] { 0x00, 0x17 }, new byte[] { 0x19, 0x5C }, "colortemperature"),
    GAMMA_CORRECTION("Gamma Correction", new byte[] { 0x00, 0x22 }, new byte[] { 0x19, 0x5E }, "gammacorrection"),
    COLOR_SPACE("Color Space", new byte[] { 0x00, 0x3B }, new byte[] { 0x19, 0x4B }, "colorspace"),

    PICTURE_MUTING("Picture Muting", new byte[] { 0x00, 0x30 }, new byte[] { 0x17, 0x24 }, "picturemuting"),
    NR("NR", new byte[] { 0x00, 0x25 }),

    // Not available for VW100, VW200
    LAMP_CONTROL("Lamp Control", new byte[] { 0x00, 0x1A }),

    // Not available for VW315, VW320, VW328, VW365, VW515, VW520, VW528, VW665, HW60, HW65, HW68
    REAL_COLOR("Real Color Processing", new byte[] { 0x00, 0x1E }, new byte[] { 0x19, 0x08 }),

    // Not available for VW40, VW50, VW60, VW70, VW85, VW95, VW100, HW15, HW20, HW30ES
    REALITY_CREATION("Reality Creation", new byte[] { 0x00, 0x67 }, new byte[] { 0x19, 0x4C }),

    // Not available for VW40, VW50, VW60
    FILM_MODE("Film Mode", new byte[] { 0x00, 0x1F }),

    // Not available for VW40, VW50, VW60, VW70, VW100, VW260, VW270, VW285, VW295, VW300, VW315, VW320, VW328, VW350,
    // VW365, VW385, VW500, VW515, VW520, VW528, VW570, VW600, VW665, VW675, VW695, VW760, VW870, VW885, VW995, HW10,
    // HW15, HW20, HW30ES, HW45ES, HW60, HW65, HW68
    FILM_PROJECTION("Film Projection", new byte[] { 0x00, 0x58 }, new byte[] { 0x17, 0x08 }, "filmprojection"),

    // Not available for VW40, VW50, VW60, VW70, VW100, HW10, HW15, HW20, HW30ES
    MOTION_ENHANCER("Motion Enhancer", new byte[] { 0x00, 0x59 }, new byte[] { 0x17, 0x05 }, "motionenhancer"),

    // Not available for VW40, VW50, VW60, VW100
    XVCOLOR("xvColor", new byte[] { 0x00, 0x5A }),

    // Not available for VW260, VW270, VW285, VW295, VW300, VW315, VW320, VW328, VW350, VW365, HW35ES, HW40ES, HW45ES,
    // HW58ES
    IRIS_MODE("Iris Mode", new byte[] { 0x00, 0x1D }, new byte[] { 0x19, 0x5F }, "irismode"),

    // Not available for VW100, VW260, VW270, VW285, VW295, VW300, VW315, VW320, VW328, VW350, VW365, VW385, VW500,
    // VW515, VW520, VW528, VW550, VW570, VW600, VW665, VW675, VW760, VW870, VW885, VW995, VW1000ES, VW1100ES, HW35ES,
    // HW40ES, HW45ES, HW50ES, HW55ES, HW58ES, HW60, HW65, HW68
    IRIS_SENSITIVITY("Iris Sensitivity", new byte[] { 0x00, 0x56 }),

    // Not available for VW100, VW260, VW270, VW285, VW295, VW300, VW315, VW320, VW328, VW350, VW365, VW760, VW870,
    // VW885, VW995, HW35ES, HW40ES, HW45ES, HW58ES
    IRIS_MANUAL("Iris Manual", new byte[] { 0x00, 0x57 }),

    // Not available for VW40, VW50, VW60, VW100, VW200, VW260, VW270, VW285, VW295, VW300, VW315, VW320, VW328, VW350,
    // VW365, VW385, VW500, VW515, VW520, VW528, VW550, VW570, VW600, VW665, VW675, VW760, VW870, VW885, VW995,
    // VW1000ES, VW1100ES, HW35ES, HW40ES, HW45ES, HW50ES, HW55ES, HW58ES, HW60, HW65, HW68
    BLOCK_NR("Block NR", new byte[] { 0x00, 0x26 }),
    MOSQUITO_NR("Mosquito NR", new byte[] { 0x00, 0x27 }),

    // Not available for VW40, VW50, VW60, VW70, VW80, VW85, VW90, VW95, VW100, VW200, HW10, HW15, HW20, HW30ES
    MPEG_NR("MPEG NR", new byte[] { 0x00, 0x6C }),

    // Not available for VW40, VW50, VW60, VW70, VW85, VW95, VW100, VW315, VW320, VW328, VW365, VW1000ES, VW1100ES
    // HW15, HW20, HW30ES, HW35ES, HW40ES, HW50ES, HW55ES, HW58ES, HW60, HW65, HW68
    HDR("HDR", new byte[] { 0x00, 0x7C }),

    ASPECT("Aspect", new byte[] { 0x00, 0x20 }, new byte[] { 0x19, 0x6E }, "aspect"),

    // Not available for VW260, VW270, VW285, VW295, VW300, VW315, VW320, VW328, VW350, VW365, VW385, VW500, VW515,
    // VW520, VW528, VW550, VW570, VW600, VW665, VW675, VW760, VW870, VW885, VW995, HW45ES, HW60, HW65, HW68
    OVERSCAN("Overscan", new byte[] { 0x00, 0x23 }),

    // Not available for VW40, VW50, VW60, VW70, VW80, VW85, VW90, VW100, VW200, VW260, VW270, VW285, VW295, VW300,
    // VW315, VW320, VW328, VW350, VW365, HW10, HW15, HW20, HW30ES, HW35ES, HW40ES, HW45ES, HW50ES, HW55ES, HW58ES,
    // HW60, HW65, HW68
    PICTURE_POSITION("Picture Position", new byte[] { 0x00, 0x66 }),

    PICTURE_POS_185("Picture Position 1.85:1", null, new byte[] { 0x1B, 0x20 }),
    PICTURE_POS_235("Picture Position 2.35:1", null, new byte[] { 0x1B, 0x21 }),
    PICTURE_POS_CUSTOM1("Picture Position Custom 1", null, new byte[] { 0x1B, 0x22 }),
    PICTURE_POS_CUSTOM2("Picture Position Custom 2", null, new byte[] { 0x1B, 0x23 }),
    PICTURE_POS_CUSTOM3("Picture Position Custom 3", null, new byte[] { 0x1B, 0x24 }),

    // Not available for VW40, VW50, VW60, VW70, VW85, VW95, VW100, VW1000ES, VW1100ES
    // HW15, HW20, HW30ES, HW35ES, HW40ES, HW50ES, HW55ES, HW58ES
    INPUT_LAG_REDUCTION("Input Lag Reduction", new byte[] { 0x00, (byte) 0x99 }),

    STATUS_ERROR("Status Error", new byte[] { 0x01, 0x01 }),
    STATUS_POWER("Status Power", new byte[] { 0x01, 0x02 }),
    LAMP_USE_TIME("Lamp Use Time", new byte[] { 0x01, 0x13 }),

    // Not available for VW40, VW50, VW60, VW70, VW100
    STATUS_ERROR2("Status Error 2", new byte[] { 0x01, 0x25 }),

    CATEGORY_CODE("Category Code", new byte[] { (byte) 0x80, 0x00 }),
    MODEL_NAME("Model Name", new byte[] { (byte) 0x80, 0x01 }),
    SERIAL_NUMBER("Serial Number", new byte[] { (byte) 0x80, 0x02 }),
    INSTALLATION_LOCATION("Installation Location", new byte[] { (byte) 0x80, 0x03 }),

    MAC_ADDRESS("MAC Address", new byte[] { (byte) 0x90, 0x00 }),
    IP_ADDRESS("IP Address", new byte[] { (byte) 0x90, 0x01 }),

    MENU("Menu", null, new byte[] { 0x17, 0x29 }),
    UP("Cursor UP", null, new byte[] { 0x17, 0x35 }),
    DOWN("Cursor DOWN", null, new byte[] { 0x17, 0x36 }),
    LEFT("Cursor LEFT", null, new byte[] { 0x17, 0x34 }),
    RIGHT("Cursor RIGHT", null, new byte[] { 0x17, 0x33 }),
    ENTER("Enter", null, new byte[] { 0x17, 0x5A }),
    RESET("Reset", null, new byte[] { 0x17, 0x7B }),
    MEMORY("Memory", null, new byte[] { 0x17, 0x5E }),
    STATUS_ON("Status On", null, new byte[] { 0x17, 0x25 }),
    STATUS_OFF("Status Off", null, new byte[] { 0x17, 0x26 }),

    ADJUST_PICTURE("Adjust Picture", null, new byte[] { 0x19, 0x09 }),

    COLOR_CORRECTION("Color Correction", null, new byte[] { 0x1B, 0x1C }),

    PITCH("Screen Pitch", null, new byte[] { 0x17, 0x47 }),
    SHIFT("Screen Shift", null, new byte[] { 0x17, 0x48 }),

    APA("APA", null, new byte[] { 0x19, 0x60 }),
    DOT_PHASE("Dot Phase", null, new byte[] { 0x19, 0x61 }),

    V_KEYSTONE("V Keystone", null, new byte[] { 0x19, 0x3A }),
    V_KEYSTONE_UP("V Keystone +", null, new byte[] { 0x19, 0x00 }),
    V_KEYSTONE_DOWN("V Keystone -", null, new byte[] { 0x19, 0x01 }),
    LENS_CONTROL("Lens Control", null, new byte[] { 0x19, 0x78 }),
    LENS_SHIFT("Lens Shift", null, new byte[] { 0x19, 0x63 }),
    LENS_SHIFT_LEFT("Lens Shift LEFT", null, new byte[] { 0x19, 0x02 }),
    LENS_SHIFT_RIGHT("Lens Shift RIGHT", null, new byte[] { 0x19, 0x03 }),

    LENS_SHIFT_UP("Lens Shift UP", null, new byte[] { 0x17, 0x72 }),
    LENS_SHIFT_DOWN("Lens Shift DOWN", null, new byte[] { 0x17, 0x73 }),

    LENS_ZOOM("Lens Zoom", null, new byte[] { 0x19, 0x62 }),

    LENS_ZOOM_LARGE("Lens Zoom Large", null, new byte[] { 0x17, 0x77 }),
    LENS_ZOOM_SMALL("Lens Zoom Small", null, new byte[] { 0x17, 0x78 }),

    LENS_FOCUS("Lens Focus", null, new byte[] { 0x19, 0x64 }),

    LENS_FOCUS_FAR("Lens Focus Far", null, new byte[] { 0x17, 0x74 }),
    LENS_FOCUS_NEAR("Lens Focus Near", null, new byte[] { 0x17, 0x75 }),

    LENS_POSITION("Lens Position", null, new byte[] { 0x1B, 0x18 }),

    MODE_3D("3D", null, new byte[] { 0x19, 0x3B }),

    OPTIONS("Options", null, new byte[] { 0x1B, 0x6E }),
    EXIT("Exit", null, new byte[] { 0x1B, 0x6F }),
    SYNC_MENU("Sync Menu", null, new byte[] { 0x1B, 0x70 }),
    PLAY("Play", null, new byte[] { 0x1B, 0x71 }),
    STOP("Stop", null, new byte[] { 0x1B, 0x72 }),
    PAUSE("Pause", null, new byte[] { 0x1B, 0x73 }),
    FAST_REWIND("Fast Rewind", null, new byte[] { 0x1B, 0x74 }),
    FAST_FORWARD("Fast Forward", null, new byte[] { 0x1B, 0x75 }),
    PREVIOUS("Previous", null, new byte[] { 0x1B, 0x76 }),
    NEXT("Next", null, new byte[] { 0x1B, 0x77 });

    private String name;
    private byte @Nullable [] code;
    private byte @Nullable [] irCode;
    private @Nullable String channelType;

    /**
     *
     * @param name the item name
     * @param code the data code associated to the item
     */
    private SonyProjectorItem(String name, byte @Nullable [] code) {
        this(name, code, null, null);
    }

    /**
     *
     * @param name the item name
     * @param code the data code associated to the item
     * @param irCode the IR code associated to the item
     */
    private SonyProjectorItem(String name, byte @Nullable [] code, byte @Nullable [] irCode) {
        this(name, code, irCode, null);
    }

    /**
     *
     * @param name the item name
     * @param code the data code associated to the item
     * @param irCode the IR code associated to the item
     * @param channelType the channel type id to consider to retrieve the command option label
     */
    private SonyProjectorItem(String name, byte @Nullable [] code, byte @Nullable [] irCode,
            @Nullable String channelType) {
        this.name = name;
        this.code = code;
        this.irCode = irCode;
        this.channelType = channelType;
    }

    /**
     * Get the data code associated to the current item
     *
     * @return the data code or null if undefined
     */
    public byte @Nullable [] getCode() {
        return code;
    }

    /**
     * Get the IR code associated to the current item
     *
     * @return the IR code or null if undefined
     */
    public byte @Nullable [] getIrCode() {
        return irCode;
    }

    /**
     * Get the item name
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the channel type id to consider to retrieve the command option label
     *
     * @return the channel type id
     */
    public @Nullable String getChannelType() {
        return channelType;
    }

    public static boolean isValidIrCode(byte @Nullable [] irCode) {
        if (irCode != null && irCode.length == 2) {
            return irCode[0] == 0x17 || irCode[0] == 0x19 || irCode[0] == 0x1B;
        }
        return false;
    }

    /**
     * Get the command associated to a value
     *
     * @param val the value used to identify the command
     *
     * @return the command associated to the searched value
     *
     * @throws SonyProjectorException - If no command is associated to the searched value
     */
    public static SonyProjectorItem getFromValue(String val) throws SonyProjectorException {
        for (SonyProjectorItem value : SonyProjectorItem.values()) {
            if (value.name().equalsIgnoreCase(val)) {
                return value;
            }
        }
        throw new SonyProjectorException("Invalid value for a command: " + val);
    }

    /**
     * Get the list of {@link CommandOption} associated to the available IR commands
     *
     * @param inputOptions the command options associated to the video inputs
     * @param presetOptions the command options associated to the calibration presets
     * @param aspectOptions the command options associated to the aspect ratios
     *
     * @return the list of {@link CommandOption} associated to the available IR commands
     */
    public static List<CommandOption> getIRCommandOptions(List<CommandOption> inputOptions,
            List<CommandOption> presetOptions, List<CommandOption> aspectOptions) {
        List<CommandOption> options = new ArrayList<>();
        for (SonyProjectorItem value : SonyProjectorItem.values()) {
            if (isValidIrCode(value.getIrCode())) {
                options.add(new CommandOption(value.name(), value.getChannelType() == null ? value.getName()
                        : "@text/channel-type.sonyprojector." + value.getChannelType() + ".label"));
            }
            if (value == INPUT) {
                options.addAll(inputOptions);
            } else if (value == CALIBRATION_PRESET) {
                options.addAll(presetOptions);
            } else if (value == ASPECT) {
                options.addAll(aspectOptions);
            }
        }
        return options;
    }
}
