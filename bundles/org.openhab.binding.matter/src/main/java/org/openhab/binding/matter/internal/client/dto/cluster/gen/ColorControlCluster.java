/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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

// AUTO-GENERATED, DO NOT EDIT!

package org.openhab.binding.matter.internal.client.dto.cluster.gen;

import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.openhab.binding.matter.internal.client.dto.cluster.ClusterCommand;

/**
 * ColorControl
 *
 * @author Dan Cunningham - Initial contribution
 */
public class ColorControlCluster extends BaseCluster {

    public static final int CLUSTER_ID = 0x0300;
    public static final String CLUSTER_NAME = "ColorControl";
    public static final String CLUSTER_PREFIX = "colorControl";
    public static final String ATTRIBUTE_CLUSTER_REVISION = "clusterRevision";
    public static final String ATTRIBUTE_FEATURE_MAP = "featureMap";
    public static final String ATTRIBUTE_CURRENT_HUE = "currentHue";
    public static final String ATTRIBUTE_CURRENT_SATURATION = "currentSaturation";
    public static final String ATTRIBUTE_REMAINING_TIME = "remainingTime";
    public static final String ATTRIBUTE_CURRENT_X = "currentX";
    public static final String ATTRIBUTE_CURRENT_Y = "currentY";
    public static final String ATTRIBUTE_DRIFT_COMPENSATION = "driftCompensation";
    public static final String ATTRIBUTE_COMPENSATION_TEXT = "compensationText";
    public static final String ATTRIBUTE_COLOR_TEMPERATURE_MIREDS = "colorTemperatureMireds";
    public static final String ATTRIBUTE_COLOR_MODE = "colorMode";
    public static final String ATTRIBUTE_OPTIONS = "options";
    public static final String ATTRIBUTE_NUMBER_OF_PRIMARIES = "numberOfPrimaries";
    public static final String ATTRIBUTE_PRIMARY1X = "primary1X";
    public static final String ATTRIBUTE_PRIMARY1Y = "primary1Y";
    public static final String ATTRIBUTE_PRIMARY1INTENSITY = "primary1Intensity";
    public static final String ATTRIBUTE_PRIMARY2X = "primary2X";
    public static final String ATTRIBUTE_PRIMARY2Y = "primary2Y";
    public static final String ATTRIBUTE_PRIMARY2INTENSITY = "primary2Intensity";
    public static final String ATTRIBUTE_PRIMARY3X = "primary3X";
    public static final String ATTRIBUTE_PRIMARY3Y = "primary3Y";
    public static final String ATTRIBUTE_PRIMARY3INTENSITY = "primary3Intensity";
    public static final String ATTRIBUTE_PRIMARY4X = "primary4X";
    public static final String ATTRIBUTE_PRIMARY4Y = "primary4Y";
    public static final String ATTRIBUTE_PRIMARY4INTENSITY = "primary4Intensity";
    public static final String ATTRIBUTE_PRIMARY5X = "primary5X";
    public static final String ATTRIBUTE_PRIMARY5Y = "primary5Y";
    public static final String ATTRIBUTE_PRIMARY5INTENSITY = "primary5Intensity";
    public static final String ATTRIBUTE_PRIMARY6X = "primary6X";
    public static final String ATTRIBUTE_PRIMARY6Y = "primary6Y";
    public static final String ATTRIBUTE_PRIMARY6INTENSITY = "primary6Intensity";
    public static final String ATTRIBUTE_WHITE_POINT_X = "whitePointX";
    public static final String ATTRIBUTE_WHITE_POINT_Y = "whitePointY";
    public static final String ATTRIBUTE_COLOR_POINT_RX = "colorPointRx";
    public static final String ATTRIBUTE_COLOR_POINT_RY = "colorPointRy";
    public static final String ATTRIBUTE_COLOR_POINT_RINTENSITY = "colorPointRIntensity";
    public static final String ATTRIBUTE_COLOR_POINT_GX = "colorPointGx";
    public static final String ATTRIBUTE_COLOR_POINT_GY = "colorPointGy";
    public static final String ATTRIBUTE_COLOR_POINT_GINTENSITY = "colorPointGIntensity";
    public static final String ATTRIBUTE_COLOR_POINT_BX = "colorPointBx";
    public static final String ATTRIBUTE_COLOR_POINT_BY = "colorPointBy";
    public static final String ATTRIBUTE_COLOR_POINT_BINTENSITY = "colorPointBIntensity";
    public static final String ATTRIBUTE_ENHANCED_CURRENT_HUE = "enhancedCurrentHue";
    public static final String ATTRIBUTE_ENHANCED_COLOR_MODE = "enhancedColorMode";
    public static final String ATTRIBUTE_COLOR_LOOP_ACTIVE = "colorLoopActive";
    public static final String ATTRIBUTE_COLOR_LOOP_DIRECTION = "colorLoopDirection";
    public static final String ATTRIBUTE_COLOR_LOOP_TIME = "colorLoopTime";
    public static final String ATTRIBUTE_COLOR_LOOP_START_ENHANCED_HUE = "colorLoopStartEnhancedHue";
    public static final String ATTRIBUTE_COLOR_LOOP_STORED_ENHANCED_HUE = "colorLoopStoredEnhancedHue";
    public static final String ATTRIBUTE_COLOR_CAPABILITIES = "colorCapabilities";
    public static final String ATTRIBUTE_COLOR_TEMP_PHYSICAL_MIN_MIREDS = "colorTempPhysicalMinMireds";
    public static final String ATTRIBUTE_COLOR_TEMP_PHYSICAL_MAX_MIREDS = "colorTempPhysicalMaxMireds";
    public static final String ATTRIBUTE_COUPLE_COLOR_TEMP_TO_LEVEL_MIN_MIREDS = "coupleColorTempToLevelMinMireds";
    public static final String ATTRIBUTE_START_UP_COLOR_TEMPERATURE_MIREDS = "startUpColorTemperatureMireds";

    public Integer clusterRevision; // 65533 ClusterRevision
    public FeatureMap featureMap; // 65532 FeatureMap
    /**
     * The CurrentHue attribute contains the current hue value of the light. It is updated as fast as practical during
     * commands that change the hue.
     * The hue in degrees shall be related to the CurrentHue attribute by the relationship:
     * Hue &#x3D; &quot;CurrentHue&quot; * 360 / 254
     * where CurrentHue is in the range from 0 to 254 inclusive.
     * Changes to this attribute shall only be marked as reportable in the following cases:
     * • At most once per second or
     * • At the end of the movement/transition.
     */
    public Integer currentHue; // 0 uint8 R V
    /**
     * Indicates the current saturation value of the light. It is updated as fast as practical during commands that
     * change the saturation.
     * The saturation (on a scale from 0.0 to 1.0) shall be related to the CurrentSaturation attribute by the
     * relationship:
     * Saturation &#x3D; &quot;CurrentSaturation&quot; / 254
     * where CurrentSaturation is in the range from 0 to 254 inclusive.
     * Changes to this attribute shall only be marked as reportable in the following cases:
     * • At most once per second or
     * • At the end of the movement/transition.
     */
    public Integer currentSaturation; // 1 uint8 R V
    /**
     * Indicates the time remaining, in 1/10ths of a second, until transitions due to the currently active command will
     * be complete.
     * Changes to this attribute shall only be marked as reportable in the following cases:
     * • When it changes from 0 to any value higher than 10, or
     * • When it changes, with a delta larger than 10, caused by the invoke of a command, or
     * • When it changes to 0.
     * For commands with a transition time or changes to the transition time less than 1 second, changes to this
     * attribute shall NOT be reported.
     * As this attribute is not being reported during a regular countdown, clients SHOULD NOT rely on the reporting of
     * this attribute in order to keep track of the remaining duration.
     */
    public Integer remainingTime; // 2 uint16 R V
    /**
     * Indicates the current value of the normalized chromaticity value x, as defined in the CIE xyY Color Space. It is
     * updated as fast as practical during commands that change the color.
     * The value of x shall be related to the CurrentX attribute by the relationship
     * x &#x3D; &quot;CurrentX&quot; / 65536
     * where CurrentX is in the range from 0 to 65279 inclusive.
     * Changes to this attribute shall only be marked as reportable in the following cases:
     * • At most once per second or
     * • At the end of the movement/transition.
     */
    public Integer currentX; // 3 uint16 R V
    /**
     * Indicates the current value of the normalized chromaticity value y, as defined in the CIE xyY Color Space. It is
     * updated as fast as practical during commands that change the color.
     * The value of y shall be related to the CurrentY attribute by the relationship
     * y &#x3D; &quot;CurrentY&quot; / 65536
     * where CurrentY is in the range from 0 to 65279 inclusive.
     * Changes to this attribute shall only be marked as reportable in the following cases:
     * • At most once per second or
     * • At the end of the movement/transition.
     */
    public Integer currentY; // 4 uint16 R V
    /**
     * This attribute shall indicate what mechanism, if any, is in use for compensation for color/intensity drift over
     * time.
     */
    public DriftCompensationEnum driftCompensation; // 5 DriftCompensationEnum R V
    /**
     * This attribute shall contain a textual indication of what mechanism, if any, is in use to compensate for
     * color/intensity drift over time.
     */
    public String compensationText; // 6 string R V
    /**
     * Indicates a scaled inverse of the current value of the color temperature. The unit of ColorTemperatureMireds is
     * the mired (micro reciprocal degree), a.k.a. mirek (micro reciprocal kelvin). It is updated as fast as practical
     * during commands that change the color.
     * Changes to this attribute shall only be marked as reportable in the following cases:
     * • At most once per second or
     * • At the end of the movement/transition.
     * The color temperature value in kelvins shall be related to the ColorTemperatureMireds attribute in mired by the
     * relationship
     * &quot;Color temperature [K]&quot; &#x3D; &quot;1,000,000&quot; / &quot;ColorTemperatureMireds&quot;
     * where ColorTemperatureMireds is in the range from 1 to 65279 inclusive, giving a color temperature range from
     * 1,000,000 K to 15.32 K.
     * If this attribute is implemented then the ColorMode attribute shall also be implemented.
     */
    public Integer colorTemperatureMireds; // 7 uint16 R V
    /**
     * Indicates which attributes are currently determining the color of the device.
     * The value of the ColorMode attribute cannot be written directly - it is set upon reception of any command in
     * section Commands to the appropriate mode for that command.
     */
    public ColorModeEnum colorMode; // 8 ColorModeEnum R V
    /**
     * Indicates a bitmap that determines the default behavior of some cluster commands. Each command that is dependent
     * on the Options attribute shall first construct a temporary Options bitmap that is in effect during the command
     * processing. The temporary Options bitmap has the same format and meaning as the Options attribute, but includes
     * any bits that may be overridden by command fields.
     * This attribute is meant to be changed only during commissioning.
     * Below is the format and description of the Options attribute and temporary Options bitmap and the effect on
     * dependent commands.
     * Command execution shall NOT continue beyond the Options processing if all of these criteria are true:
     * • The On/Off cluster exists on the same endpoint as this cluster.
     * • The OnOff attribute of the On/Off cluster, on this endpoint, is FALSE.
     * • The value of the ExecuteIfOff bit is 0.
     */
    public OptionsBitmap options; // 15 OptionsBitmap RW VO
    /**
     * Indicates the number of color primaries implemented on this device. A value of null shall indicate that the
     * number of primaries is unknown.
     * Where this attribute is implemented, the attributes below for indicating the “x” and “y” color values of the
     * primaries shall also be implemented for each of the primaries from 1 to NumberOfPrimaries, without leaving gaps.
     * Implementation of the Primary1Intensity attribute and subsequent intensity attributes is optional.
     */
    public Integer numberOfPrimaries; // 16 uint8 R V
    /**
     * Indicates the normalized chromaticity value x for this primary, as defined in the CIE xyY Color Space.
     * The value of x shall be related to the Primary1X attribute by the relationship x &#x3D; Primary1X / 65536
     * (Primary1X in the range 0 to 65279 inclusive)
     */
    public Integer primary1X; // 17 uint16 R V
    /**
     * Indicates the normalized chromaticity value y for this primary, as defined in the CIE xyY Color Space.
     * The value of y shall be related to the Primary1Y attribute by the relationship y &#x3D; Primary1Y / 65536
     * (Primary1Y in the range 0 to 65279 inclusive)
     */
    public Integer primary1Y; // 18 uint16 R V
    /**
     * Indicates a representation of the maximum intensity of this primary as defined in the Dimming Light Curve in the
     * Ballast Configuration cluster (see Ballast Configuration Cluster), normalized such that the primary with the
     * highest maximum intensity contains the value 254.
     * A value of null shall indicate that this primary is not available.
     */
    public Integer primary1Intensity; // 19 uint8 R V
    public Integer primary2X; // 21 uint16 R V
    public Integer primary2Y; // 22 uint16 R V
    public Integer primary2Intensity; // 23 uint8 R V
    public Integer primary3X; // 25 uint16 R V
    public Integer primary3Y; // 26 uint16 R V
    public Integer primary3Intensity; // 27 uint8 R V
    public Integer primary4X; // 32 uint16 R V
    public Integer primary4Y; // 33 uint16 R V
    public Integer primary4Intensity; // 34 uint8 R V
    public Integer primary5X; // 36 uint16 R V
    public Integer primary5Y; // 37 uint16 R V
    public Integer primary5Intensity; // 38 uint8 R V
    public Integer primary6X; // 40 uint16 R V
    public Integer primary6Y; // 41 uint16 R V
    public Integer primary6Intensity; // 42 uint8 R V
    /**
     * Indicates the normalized chromaticity value x, as defined in the CIE xyY Color Space, of the current white point
     * of the device.
     * The value of x shall be related to the WhitePointX attribute by the relationship x &#x3D; WhitePointX / 65536
     * (WhitePointX in the range 0 to 65279 inclusive)
     */
    public Integer whitePointX; // 48 uint16 RW VM
    /**
     * Indicates the normalized chromaticity value y, as defined in the CIE xyY Color Space, of the current white point
     * of the device.
     * The value of y shall be related to the WhitePointY attribute by the relationship y &#x3D; WhitePointY / 65536
     * (WhitePointY in the range 0 to 65279 inclusive)
     */
    public Integer whitePointY; // 49 uint16 RW VM
    /**
     * Indicates the normalized chromaticity value x, as defined in the CIE xyY Color Space, of the red color point of
     * the device.
     * The value of x shall be related to the ColorPointRX attribute by the relationship x &#x3D; ColorPointRX / 65536
     * (ColorPointRX in the range 0 to 65279 inclusive)
     */
    public Integer colorPointRx; // 50 uint16 RW VM
    /**
     * Indicates the normalized chromaticity value y, as defined in the CIE xyY Color Space, of the red color point of
     * the device.
     * The value of y shall be related to the ColorPointRY attribute by the relationship y &#x3D; ColorPointRY / 65536
     * (ColorPointRY in the range 0 to 65279 inclusive)
     */
    public Integer colorPointRy; // 51 uint16 RW VM
    /**
     * Indicates a representation of the relative intensity of the red color point as defined in the Dimming Light Curve
     * in the Ballast Configuration cluster (see Ballast Configuration Cluster), normalized such that the color point
     * with the highest relative intensity contains the value 254.
     * A value of null shall indicate an invalid value.
     */
    public Integer colorPointRIntensity; // 52 uint8 RW VM
    public Integer colorPointGx; // 54 uint16 RW VM
    public Integer colorPointGy; // 55 uint16 RW VM
    public Integer colorPointGIntensity; // 56 uint8 RW VM
    public Integer colorPointBx; // 58 uint16 RW VM
    public Integer colorPointBy; // 59 uint16 RW VM
    public Integer colorPointBIntensity; // 60 uint8 RW VM
    /**
     * Indicates the non-equidistant steps along the CIE 1931 color triangle, and it provides 16-bits precision.
     * The upper 8 bits of this attribute shall be used as an index in the implementation specific XY lookup table to
     * provide the non-equidistant steps. The lower 8 bits shall be used to interpolate between these steps in a linear
     * way in order to provide color zoom for the user.
     * To provide compatibility with clients not supporting EHUE, the CurrentHue attribute shall contain a hue value in
     * the range 0 to 254, calculated from the EnhancedCurrentHue attribute.
     * Changes to this attribute shall only be marked as reportable in the following cases:
     * • At most once per second or
     * • At the end of the movement/transition.
     */
    public Integer enhancedCurrentHue; // 16384 uint16 R V
    /**
     * Indicates which attributes are currently determining the color of the device.
     * To provide compatibility with clients not supporting EHUE, the original ColorMode attribute shall indicate
     * CurrentHue and CurrentSaturation when the light uses the EnhancedCurrentHue attribute. If the ColorMode attribute
     * is changed, its new value shall be copied to the EnhancedColorMode attribute.
     */
    public EnhancedColorModeEnum enhancedColorMode; // 16385 EnhancedColorModeEnum R V
    /**
     * Indicates the current active status of the color loop. If this attribute has the value 0, the color loop shall
     * NOT be active. If this attribute has the value 1, the color loop shall be active.
     */
    public ColorLoopActive colorLoopActive; // 16386 enum16 R V
    /**
     * Indicates the current direction of the color loop. If this attribute has the value 0, the EnhancedCurrentHue
     * attribute shall be decremented. If this attribute has the value 1, the EnhancedCurrentHue attribute shall be
     * incremented.
     */
    public ColorLoopDirectionEnum colorLoopDirection; // 16387 ColorLoopDirectionEnum R V
    /**
     * Indicates the number of seconds it shall take to perform a full color loop, i.e., to cycle all values of the
     * EnhancedCurrentHue attribute (between 0 and 65534).
     */
    public Integer colorLoopTime; // 16388 uint16 R V
    /**
     * Indicates the value of the EnhancedCurrentHue attribute from which the color loop shall be started.
     */
    public Integer colorLoopStartEnhancedHue; // 16389 uint16 R V
    /**
     * Indicates the value of the EnhancedCurrentHue attribute before the color loop was started. Once the color loop is
     * complete, the EnhancedCurrentHue attribute shall be restored to this value.
     */
    public Integer colorLoopStoredEnhancedHue; // 16390 uint16 R V
    /**
     * Indicates the color control capabilities of the device.
     * Bits 0-4 of the ColorCapabilities attribute shall have the same values as the corresponding bits of the
     * FeatureMap attribute. All other bits in ColorCapabilities shall be 0.
     */
    public ColorCapabilities colorCapabilities; // 16394 map16 R V
    /**
     * This attribute shall indicate the minimum mired value supported by the hardware. ColorTempPhysicalMinMireds
     * corresponds to the maximum color temperature in kelvins supported by the hardware.
     * ColorTempPhysicalMinMireds &lt;&#x3D; ColorTemperatureMireds.
     */
    public Integer colorTempPhysicalMinMireds; // 16395 uint16 R V
    /**
     * This attribute shall indicate the maximum mired value supported by the hardware. ColorTempPhysicalMaxMireds
     * corresponds to the minimum color temperature in kelvins supported by the hardware.
     * ColorTemperatureMireds &lt;&#x3D; ColorTempPhysicalMaxMireds.
     */
    public Integer colorTempPhysicalMaxMireds; // 16396 uint16 R V
    /**
     * Indicates a lower bound on the value of the ColorTemperatureMireds attribute for the purposes of coupling the
     * ColorTemperatureMireds attribute to the CurrentLevel attribute when the CoupleColorTempToLevel bit of the Options
     * attribute of the Level Control cluster is equal to 1. When coupling the ColorTemperatureMireds attribute to the
     * CurrentLevel attribute, this value shall correspond to a CurrentLevel value of 254 (100%).
     * This attribute shall be set such that the following relationship exists: ColorTempPhysicalMinMireds &lt;&#x3D;
     * CoupleColorTempToLevelMinMireds &lt;&#x3D; ColorTemperatureMireds
     * Note that since this attribute is stored as a micro reciprocal degree (mired) value (i.e. color temperature in
     * kelvins &#x3D; 1,000,000 / CoupleColorTempToLevelMinMireds), the CoupleColorTempToLevelMinMireds attribute
     * corresponds to an upper bound on the value of the color temperature in kelvins supported by the device.
     */
    public Integer coupleColorTempToLevelMinMireds; // 16397 uint16 R V
    /**
     * Indicates the desired startup color temperature value the light shall use when it is supplied with power and this
     * value shall be reflected in the ColorTemperatureMireds attribute. In addition, the ColorMode and
     * EnhancedColorMode attributes shall be set to 2 (ColorTemperatureMireds). The values of the
     * StartUpColorTemperatureMireds attribute are listed in the table below,
     */
    public Integer startUpColorTemperatureMireds; // 16400 uint16 RW VM

    // Enums
    /**
     * Indicates the current active status of the color loop. If this attribute has the value 0, the color loop shall
     * NOT be active. If this attribute has the value 1, the color loop shall be active.
     */
    public enum ColorLoopActive implements MatterEnum {
        INACTIVE(0, "Inactive"),
        ACTIVE(1, "Active");

        public final Integer value;
        public final String label;

        private ColorLoopActive(Integer value, String label) {
            this.value = value;
            this.label = label;
        }

        @Override
        public Integer getValue() {
            return value;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    public enum DriftCompensationEnum implements MatterEnum {
        NONE(0, "None"),
        OTHER_OR_UNKNOWN(1, "Other Or Unknown"),
        TEMPERATURE_MONITORING(2, "Temperature Monitoring"),
        OPTICAL_LUMINANCE_MONITORING_AND_FEEDBACK(3, "Optical Luminance Monitoring And Feedback"),
        OPTICAL_COLOR_MONITORING_AND_FEEDBACK(4, "Optical Color Monitoring And Feedback");

        public final Integer value;
        public final String label;

        private DriftCompensationEnum(Integer value, String label) {
            this.value = value;
            this.label = label;
        }

        @Override
        public Integer getValue() {
            return value;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    public enum ColorModeEnum implements MatterEnum {
        CURRENT_HUE_AND_CURRENT_SATURATION(0, "Current Hue And Current Saturation"),
        CURRENT_X_AND_CURRENT_Y(1, "Current X And Current Y"),
        COLOR_TEMPERATURE_MIREDS(2, "Color Temperature Mireds");

        public final Integer value;
        public final String label;

        private ColorModeEnum(Integer value, String label) {
            this.value = value;
            this.label = label;
        }

        @Override
        public Integer getValue() {
            return value;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    public enum EnhancedColorModeEnum implements MatterEnum {
        CURRENT_HUE_AND_CURRENT_SATURATION(0, "Current Hue And Current Saturation"),
        CURRENT_X_AND_CURRENT_Y(1, "Current X And Current Y"),
        COLOR_TEMPERATURE_MIREDS(2, "Color Temperature Mireds"),
        ENHANCED_CURRENT_HUE_AND_CURRENT_SATURATION(3, "Enhanced Current Hue And Current Saturation");

        public final Integer value;
        public final String label;

        private EnhancedColorModeEnum(Integer value, String label) {
            this.value = value;
            this.label = label;
        }

        @Override
        public Integer getValue() {
            return value;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    public enum DirectionEnum implements MatterEnum {
        SHORTEST(0, "Shortest"),
        LONGEST(1, "Longest"),
        UP(2, "Up"),
        DOWN(3, "Down");

        public final Integer value;
        public final String label;

        private DirectionEnum(Integer value, String label) {
            this.value = value;
            this.label = label;
        }

        @Override
        public Integer getValue() {
            return value;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    public enum MoveModeEnum implements MatterEnum {
        STOP(0, "Stop"),
        UP(1, "Up"),
        DOWN(3, "Down");

        public final Integer value;
        public final String label;

        private MoveModeEnum(Integer value, String label) {
            this.value = value;
            this.label = label;
        }

        @Override
        public Integer getValue() {
            return value;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    public enum StepModeEnum implements MatterEnum {
        UP(1, "Up"),
        DOWN(3, "Down");

        public final Integer value;
        public final String label;

        private StepModeEnum(Integer value, String label) {
            this.value = value;
            this.label = label;
        }

        @Override
        public Integer getValue() {
            return value;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    public enum ColorLoopActionEnum implements MatterEnum {
        DEACTIVATE(0, "Deactivate"),
        ACTIVATE_FROM_COLOR_LOOP_START_ENHANCED_HUE(1, "Activate From Color Loop Start Enhanced Hue"),
        ACTIVATE_FROM_ENHANCED_CURRENT_HUE(2, "Activate From Enhanced Current Hue");

        public final Integer value;
        public final String label;

        private ColorLoopActionEnum(Integer value, String label) {
            this.value = value;
            this.label = label;
        }

        @Override
        public Integer getValue() {
            return value;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    public enum ColorLoopDirectionEnum implements MatterEnum {
        DECREMENT(0, "Decrement"),
        INCREMENT(1, "Increment");

        public final Integer value;
        public final String label;

        private ColorLoopDirectionEnum(Integer value, String label) {
            this.value = value;
            this.label = label;
        }

        @Override
        public Integer getValue() {
            return value;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    // Bitmaps
    /**
     * Indicates the color control capabilities of the device.
     * Bits 0-4 of the ColorCapabilities attribute shall have the same values as the corresponding bits of the
     * FeatureMap attribute. All other bits in ColorCapabilities shall be 0.
     */
    public static class ColorCapabilities {
        public boolean hueSaturation;
        public boolean enhancedHue;
        public boolean colorLoop;
        public boolean xY;
        public boolean colorTemperature;

        public ColorCapabilities(boolean hueSaturation, boolean enhancedHue, boolean colorLoop, boolean xY,
                boolean colorTemperature) {
            this.hueSaturation = hueSaturation;
            this.enhancedHue = enhancedHue;
            this.colorLoop = colorLoop;
            this.xY = xY;
            this.colorTemperature = colorTemperature;
        }
    }

    public static class OptionsBitmap {
        /**
         * Dependency on On/Off cluster
         * This bit shall indicate if this cluster server instance has a dependency with the On/Off cluster.
         */
        public boolean executeIfOff;

        public OptionsBitmap(boolean executeIfOff) {
            this.executeIfOff = executeIfOff;
        }
    }

    /**
     * This data type is derived from map8 and is used in the ColorLoopSet command.
     */
    public static class UpdateFlagsBitmap {
        /**
         * Device adheres to the associated action field.
         * This bit shall indicate whether the server adheres to the Action field in order to process the command.
         * • 0 &#x3D; Device shall ignore the Action field.
         * • 1 &#x3D; Device shall adhere to the Action field.
         */
        public boolean updateAction;
        /**
         * Device updates the associated direction attribute.
         * This bit shall indicate whether the device updates the ColorLoopDirection attribute with the Direction field.
         * • 0 &#x3D; Device shall ignore the Direction field.
         * • 1 &#x3D; Device shall update the ColorLoopDirection attribute with the value of the Direction field.
         */
        public boolean updateDirection;
        /**
         * Device updates the associated time attribute.
         * This bit shall indicate whether the device updates the ColorLoopTime attribute with the Time field.
         * • 0 &#x3D; Device shall ignore the Time field.
         * • 1 &#x3D; Device shall update the value of the ColorLoopTime attribute with the value of the Time field.
         */
        public boolean updateTime;
        /**
         * Device updates the associated start hue attribute.
         * This bit shall indicate whether the device updates the ColorLoopStartEnhancedHue attribute with the value of
         * the StartHue field.
         * • 0 &#x3D; Device shall ignore the StartHue field.
         * • 1 &#x3D; Device shall update the value of the ColorLoopStartEnhancedHue attribute with the value of the
         * StartHue field.
         */
        public boolean updateStartHue;

        public UpdateFlagsBitmap(boolean updateAction, boolean updateDirection, boolean updateTime,
                boolean updateStartHue) {
            this.updateAction = updateAction;
            this.updateDirection = updateDirection;
            this.updateTime = updateTime;
            this.updateStartHue = updateStartHue;
        }
    }

    public static class FeatureMap {
        /**
         * 
         * Supports color specification via hue/saturation.
         */
        public boolean hueSaturation;
        /**
         * 
         * Enhanced hue is supported.
         */
        public boolean enhancedHue;
        /**
         * 
         * Color loop is supported.
         */
        public boolean colorLoop;
        /**
         * 
         * Supports color specification via XY.
         */
        public boolean xy;
        /**
         * 
         * Supports specification of color temperature.
         */
        public boolean colorTemperature;

        public FeatureMap(boolean hueSaturation, boolean enhancedHue, boolean colorLoop, boolean xy,
                boolean colorTemperature) {
            this.hueSaturation = hueSaturation;
            this.enhancedHue = enhancedHue;
            this.colorLoop = colorLoop;
            this.xy = xy;
            this.colorTemperature = colorTemperature;
        }
    }

    public ColorControlCluster(BigInteger nodeId, int endpointId) {
        super(nodeId, endpointId, 768, "ColorControl");
    }

    protected ColorControlCluster(BigInteger nodeId, int endpointId, int clusterId, String clusterName) {
        super(nodeId, endpointId, clusterId, clusterName);
    }

    // commands
    public static ClusterCommand moveToHue(Integer hue, DirectionEnum direction, Integer transitionTime,
            OptionsBitmap optionsMask, OptionsBitmap optionsOverride) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (hue != null) {
            map.put("hue", hue);
        }
        if (direction != null) {
            map.put("direction", direction);
        }
        if (transitionTime != null) {
            map.put("transitionTime", transitionTime);
        }
        if (optionsMask != null) {
            map.put("optionsMask", optionsMask);
        }
        if (optionsOverride != null) {
            map.put("optionsOverride", optionsOverride);
        }
        return new ClusterCommand("moveToHue", map);
    }

    public static ClusterCommand moveHue(MoveModeEnum moveMode, Integer rate, OptionsBitmap optionsMask,
            OptionsBitmap optionsOverride) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (moveMode != null) {
            map.put("moveMode", moveMode);
        }
        if (rate != null) {
            map.put("rate", rate);
        }
        if (optionsMask != null) {
            map.put("optionsMask", optionsMask);
        }
        if (optionsOverride != null) {
            map.put("optionsOverride", optionsOverride);
        }
        return new ClusterCommand("moveHue", map);
    }

    public static ClusterCommand stepHue(StepModeEnum stepMode, Integer stepSize, Integer transitionTime,
            OptionsBitmap optionsMask, OptionsBitmap optionsOverride) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (stepMode != null) {
            map.put("stepMode", stepMode);
        }
        if (stepSize != null) {
            map.put("stepSize", stepSize);
        }
        if (transitionTime != null) {
            map.put("transitionTime", transitionTime);
        }
        if (optionsMask != null) {
            map.put("optionsMask", optionsMask);
        }
        if (optionsOverride != null) {
            map.put("optionsOverride", optionsOverride);
        }
        return new ClusterCommand("stepHue", map);
    }

    public static ClusterCommand moveToSaturation(Integer saturation, Integer transitionTime, OptionsBitmap optionsMask,
            OptionsBitmap optionsOverride) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (saturation != null) {
            map.put("saturation", saturation);
        }
        if (transitionTime != null) {
            map.put("transitionTime", transitionTime);
        }
        if (optionsMask != null) {
            map.put("optionsMask", optionsMask);
        }
        if (optionsOverride != null) {
            map.put("optionsOverride", optionsOverride);
        }
        return new ClusterCommand("moveToSaturation", map);
    }

    public static ClusterCommand moveSaturation(MoveModeEnum moveMode, Integer rate, OptionsBitmap optionsMask,
            OptionsBitmap optionsOverride) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (moveMode != null) {
            map.put("moveMode", moveMode);
        }
        if (rate != null) {
            map.put("rate", rate);
        }
        if (optionsMask != null) {
            map.put("optionsMask", optionsMask);
        }
        if (optionsOverride != null) {
            map.put("optionsOverride", optionsOverride);
        }
        return new ClusterCommand("moveSaturation", map);
    }

    public static ClusterCommand stepSaturation(StepModeEnum stepMode, Integer stepSize, Integer transitionTime,
            OptionsBitmap optionsMask, OptionsBitmap optionsOverride) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (stepMode != null) {
            map.put("stepMode", stepMode);
        }
        if (stepSize != null) {
            map.put("stepSize", stepSize);
        }
        if (transitionTime != null) {
            map.put("transitionTime", transitionTime);
        }
        if (optionsMask != null) {
            map.put("optionsMask", optionsMask);
        }
        if (optionsOverride != null) {
            map.put("optionsOverride", optionsOverride);
        }
        return new ClusterCommand("stepSaturation", map);
    }

    public static ClusterCommand moveToHueAndSaturation(Integer hue, Integer saturation, Integer transitionTime,
            OptionsBitmap optionsMask, OptionsBitmap optionsOverride) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (hue != null) {
            map.put("hue", hue);
        }
        if (saturation != null) {
            map.put("saturation", saturation);
        }
        if (transitionTime != null) {
            map.put("transitionTime", transitionTime);
        }
        if (optionsMask != null) {
            map.put("optionsMask", optionsMask);
        }
        if (optionsOverride != null) {
            map.put("optionsOverride", optionsOverride);
        }
        return new ClusterCommand("moveToHueAndSaturation", map);
    }

    public static ClusterCommand moveToColor(Integer colorX, Integer colorY, Integer transitionTime,
            OptionsBitmap optionsMask, OptionsBitmap optionsOverride) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (colorX != null) {
            map.put("colorX", colorX);
        }
        if (colorY != null) {
            map.put("colorY", colorY);
        }
        if (transitionTime != null) {
            map.put("transitionTime", transitionTime);
        }
        if (optionsMask != null) {
            map.put("optionsMask", optionsMask);
        }
        if (optionsOverride != null) {
            map.put("optionsOverride", optionsOverride);
        }
        return new ClusterCommand("moveToColor", map);
    }

    public static ClusterCommand moveColor(Integer rateX, Integer rateY, OptionsBitmap optionsMask,
            OptionsBitmap optionsOverride) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (rateX != null) {
            map.put("rateX", rateX);
        }
        if (rateY != null) {
            map.put("rateY", rateY);
        }
        if (optionsMask != null) {
            map.put("optionsMask", optionsMask);
        }
        if (optionsOverride != null) {
            map.put("optionsOverride", optionsOverride);
        }
        return new ClusterCommand("moveColor", map);
    }

    public static ClusterCommand stepColor(Integer stepX, Integer stepY, Integer transitionTime,
            OptionsBitmap optionsMask, OptionsBitmap optionsOverride) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (stepX != null) {
            map.put("stepX", stepX);
        }
        if (stepY != null) {
            map.put("stepY", stepY);
        }
        if (transitionTime != null) {
            map.put("transitionTime", transitionTime);
        }
        if (optionsMask != null) {
            map.put("optionsMask", optionsMask);
        }
        if (optionsOverride != null) {
            map.put("optionsOverride", optionsOverride);
        }
        return new ClusterCommand("stepColor", map);
    }

    public static ClusterCommand moveToColorTemperature(Integer colorTemperatureMireds, Integer transitionTime,
            OptionsBitmap optionsMask, OptionsBitmap optionsOverride) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (colorTemperatureMireds != null) {
            map.put("colorTemperatureMireds", colorTemperatureMireds);
        }
        if (transitionTime != null) {
            map.put("transitionTime", transitionTime);
        }
        if (optionsMask != null) {
            map.put("optionsMask", optionsMask);
        }
        if (optionsOverride != null) {
            map.put("optionsOverride", optionsOverride);
        }
        return new ClusterCommand("moveToColorTemperature", map);
    }

    /**
     * This command allows the light to be moved in a smooth continuous transition from their current hue to a target
     * hue.
     */
    public static ClusterCommand enhancedMoveToHue(Integer enhancedHue, DirectionEnum direction, Integer transitionTime,
            OptionsBitmap optionsMask, OptionsBitmap optionsOverride) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (enhancedHue != null) {
            map.put("enhancedHue", enhancedHue);
        }
        if (direction != null) {
            map.put("direction", direction);
        }
        if (transitionTime != null) {
            map.put("transitionTime", transitionTime);
        }
        if (optionsMask != null) {
            map.put("optionsMask", optionsMask);
        }
        if (optionsOverride != null) {
            map.put("optionsOverride", optionsOverride);
        }
        return new ClusterCommand("enhancedMoveToHue", map);
    }

    /**
     * This command allows the light to start a continuous transition starting from their current hue.
     */
    public static ClusterCommand enhancedMoveHue(MoveModeEnum moveMode, Integer rate, OptionsBitmap optionsMask,
            OptionsBitmap optionsOverride) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (moveMode != null) {
            map.put("moveMode", moveMode);
        }
        if (rate != null) {
            map.put("rate", rate);
        }
        if (optionsMask != null) {
            map.put("optionsMask", optionsMask);
        }
        if (optionsOverride != null) {
            map.put("optionsOverride", optionsOverride);
        }
        return new ClusterCommand("enhancedMoveHue", map);
    }

    /**
     * This command allows the light to be moved in a stepped transition from their current hue, resulting in a linear
     * transition through XY space.
     */
    public static ClusterCommand enhancedStepHue(StepModeEnum stepMode, Integer stepSize, Integer transitionTime,
            OptionsBitmap optionsMask, OptionsBitmap optionsOverride) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (stepMode != null) {
            map.put("stepMode", stepMode);
        }
        if (stepSize != null) {
            map.put("stepSize", stepSize);
        }
        if (transitionTime != null) {
            map.put("transitionTime", transitionTime);
        }
        if (optionsMask != null) {
            map.put("optionsMask", optionsMask);
        }
        if (optionsOverride != null) {
            map.put("optionsOverride", optionsOverride);
        }
        return new ClusterCommand("enhancedStepHue", map);
    }

    /**
     * This command allows the light to be moved in a smooth continuous transition from their current hue to a target
     * hue and from their current saturation to a target saturation.
     */
    public static ClusterCommand enhancedMoveToHueAndSaturation(Integer enhancedHue, Integer saturation,
            Integer transitionTime, OptionsBitmap optionsMask, OptionsBitmap optionsOverride) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (enhancedHue != null) {
            map.put("enhancedHue", enhancedHue);
        }
        if (saturation != null) {
            map.put("saturation", saturation);
        }
        if (transitionTime != null) {
            map.put("transitionTime", transitionTime);
        }
        if (optionsMask != null) {
            map.put("optionsMask", optionsMask);
        }
        if (optionsOverride != null) {
            map.put("optionsOverride", optionsOverride);
        }
        return new ClusterCommand("enhancedMoveToHueAndSaturation", map);
    }

    /**
     * This command allows a color loop to be activated such that the color light cycles through its range of hues.
     */
    public static ClusterCommand colorLoopSet(UpdateFlagsBitmap updateFlags, ColorLoopActionEnum action,
            ColorLoopDirectionEnum direction, Integer time, Integer startHue, OptionsBitmap optionsMask,
            OptionsBitmap optionsOverride) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (updateFlags != null) {
            map.put("updateFlags", updateFlags);
        }
        if (action != null) {
            map.put("action", action);
        }
        if (direction != null) {
            map.put("direction", direction);
        }
        if (time != null) {
            map.put("time", time);
        }
        if (startHue != null) {
            map.put("startHue", startHue);
        }
        if (optionsMask != null) {
            map.put("optionsMask", optionsMask);
        }
        if (optionsOverride != null) {
            map.put("optionsOverride", optionsOverride);
        }
        return new ClusterCommand("colorLoopSet", map);
    }

    /**
     * This command is provided to allow MoveTo and Step commands to be stopped.
     */
    public static ClusterCommand stopMoveStep(OptionsBitmap optionsMask, OptionsBitmap optionsOverride) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (optionsMask != null) {
            map.put("optionsMask", optionsMask);
        }
        if (optionsOverride != null) {
            map.put("optionsOverride", optionsOverride);
        }
        return new ClusterCommand("stopMoveStep", map);
    }

    /**
     * This command allows the color temperature of the light to be moved at a specified rate.
     */
    public static ClusterCommand moveColorTemperature(MoveModeEnum moveMode, Integer rate,
            Integer colorTemperatureMinimumMireds, Integer colorTemperatureMaximumMireds, OptionsBitmap optionsMask,
            OptionsBitmap optionsOverride) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (moveMode != null) {
            map.put("moveMode", moveMode);
        }
        if (rate != null) {
            map.put("rate", rate);
        }
        if (colorTemperatureMinimumMireds != null) {
            map.put("colorTemperatureMinimumMireds", colorTemperatureMinimumMireds);
        }
        if (colorTemperatureMaximumMireds != null) {
            map.put("colorTemperatureMaximumMireds", colorTemperatureMaximumMireds);
        }
        if (optionsMask != null) {
            map.put("optionsMask", optionsMask);
        }
        if (optionsOverride != null) {
            map.put("optionsOverride", optionsOverride);
        }
        return new ClusterCommand("moveColorTemperature", map);
    }

    /**
     * This command allows the color temperature of the light to be stepped with a specified step size.
     */
    public static ClusterCommand stepColorTemperature(StepModeEnum stepMode, Integer stepSize, Integer transitionTime,
            Integer colorTemperatureMinimumMireds, Integer colorTemperatureMaximumMireds, OptionsBitmap optionsMask,
            OptionsBitmap optionsOverride) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (stepMode != null) {
            map.put("stepMode", stepMode);
        }
        if (stepSize != null) {
            map.put("stepSize", stepSize);
        }
        if (transitionTime != null) {
            map.put("transitionTime", transitionTime);
        }
        if (colorTemperatureMinimumMireds != null) {
            map.put("colorTemperatureMinimumMireds", colorTemperatureMinimumMireds);
        }
        if (colorTemperatureMaximumMireds != null) {
            map.put("colorTemperatureMaximumMireds", colorTemperatureMaximumMireds);
        }
        if (optionsMask != null) {
            map.put("optionsMask", optionsMask);
        }
        if (optionsOverride != null) {
            map.put("optionsOverride", optionsOverride);
        }
        return new ClusterCommand("stepColorTemperature", map);
    }

    @Override
    public @NonNull String toString() {
        String str = "";
        str += "clusterRevision : " + clusterRevision + "\n";
        str += "featureMap : " + featureMap + "\n";
        str += "currentHue : " + currentHue + "\n";
        str += "currentSaturation : " + currentSaturation + "\n";
        str += "remainingTime : " + remainingTime + "\n";
        str += "currentX : " + currentX + "\n";
        str += "currentY : " + currentY + "\n";
        str += "driftCompensation : " + driftCompensation + "\n";
        str += "compensationText : " + compensationText + "\n";
        str += "colorTemperatureMireds : " + colorTemperatureMireds + "\n";
        str += "colorMode : " + colorMode + "\n";
        str += "options : " + options + "\n";
        str += "numberOfPrimaries : " + numberOfPrimaries + "\n";
        str += "primary1X : " + primary1X + "\n";
        str += "primary1Y : " + primary1Y + "\n";
        str += "primary1Intensity : " + primary1Intensity + "\n";
        str += "primary2X : " + primary2X + "\n";
        str += "primary2Y : " + primary2Y + "\n";
        str += "primary2Intensity : " + primary2Intensity + "\n";
        str += "primary3X : " + primary3X + "\n";
        str += "primary3Y : " + primary3Y + "\n";
        str += "primary3Intensity : " + primary3Intensity + "\n";
        str += "primary4X : " + primary4X + "\n";
        str += "primary4Y : " + primary4Y + "\n";
        str += "primary4Intensity : " + primary4Intensity + "\n";
        str += "primary5X : " + primary5X + "\n";
        str += "primary5Y : " + primary5Y + "\n";
        str += "primary5Intensity : " + primary5Intensity + "\n";
        str += "primary6X : " + primary6X + "\n";
        str += "primary6Y : " + primary6Y + "\n";
        str += "primary6Intensity : " + primary6Intensity + "\n";
        str += "whitePointX : " + whitePointX + "\n";
        str += "whitePointY : " + whitePointY + "\n";
        str += "colorPointRx : " + colorPointRx + "\n";
        str += "colorPointRy : " + colorPointRy + "\n";
        str += "colorPointRIntensity : " + colorPointRIntensity + "\n";
        str += "colorPointGx : " + colorPointGx + "\n";
        str += "colorPointGy : " + colorPointGy + "\n";
        str += "colorPointGIntensity : " + colorPointGIntensity + "\n";
        str += "colorPointBx : " + colorPointBx + "\n";
        str += "colorPointBy : " + colorPointBy + "\n";
        str += "colorPointBIntensity : " + colorPointBIntensity + "\n";
        str += "enhancedCurrentHue : " + enhancedCurrentHue + "\n";
        str += "enhancedColorMode : " + enhancedColorMode + "\n";
        str += "colorLoopActive : " + colorLoopActive + "\n";
        str += "colorLoopDirection : " + colorLoopDirection + "\n";
        str += "colorLoopTime : " + colorLoopTime + "\n";
        str += "colorLoopStartEnhancedHue : " + colorLoopStartEnhancedHue + "\n";
        str += "colorLoopStoredEnhancedHue : " + colorLoopStoredEnhancedHue + "\n";
        str += "colorCapabilities : " + colorCapabilities + "\n";
        str += "colorTempPhysicalMinMireds : " + colorTempPhysicalMinMireds + "\n";
        str += "colorTempPhysicalMaxMireds : " + colorTempPhysicalMaxMireds + "\n";
        str += "coupleColorTempToLevelMinMireds : " + coupleColorTempToLevelMinMireds + "\n";
        str += "startUpColorTemperatureMireds : " + startUpColorTemperatureMireds + "\n";
        return str;
    }
}
