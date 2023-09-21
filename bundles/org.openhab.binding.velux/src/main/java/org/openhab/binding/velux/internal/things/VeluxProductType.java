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
package org.openhab.binding.velux.internal.things;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.velux.internal.VeluxBindingConstants;

/**
 * <B>Velux</B> product characteristics: Product Type.
 * <P>
 * See <a href=
 * "https://velcdn.azureedge.net/~/media/com/api/klf200/technical%20specification%20for%20klf%20200%20api.pdf#page=112">KLF200
 * List of actuator types and their use of Main Parameter and Functional Parameters</a>
 * <P>
 * Methods in handle this type of information:
 * <UL>
 * <LI>{@link #get(int)} to convert a value into the VeluxProductType.</LI>
 * <LI>{@link #toString(int)} to convert a value into the description of the VeluxProductType.</LI>
 * </UL>
 *
 * @see VeluxKLFAPI
 *
 * @author Guenther Schreiner - initial contribution.
 */
@NonNullByDefault
public enum VeluxProductType {
    SLIDER_SHUTTER,
    SLIDER_WINDOW,
    SWITCH,
    UNDEFTYPE;

    public enum ActuatorType {
        UNDEFTYPE((short) 0xffff, VeluxBindingConstants.UNKNOWN, VeluxProductType.SWITCH),
        BLIND_1_0((short) 0x0040, "Interior Venetian Blind", VeluxProductType.SLIDER_SHUTTER),
        ROLLERSHUTTER_2_0((short) 0x0080, "Roller Shutter", VeluxProductType.SLIDER_SHUTTER),
        ROLLERSHUTTER_2_1((short) 0x0081, "Roller Shutter", VeluxProductType.SLIDER_SHUTTER),
        ROLLERSHUTTER_2_2((short) 0x0082, "Roller Shutter", VeluxProductType.SLIDER_SHUTTER),
        AWNING_3_0((short) 0x00C0, "Vertical Exterior Awning", VeluxProductType.SLIDER_SHUTTER),
        WINDOW_4_0((short) 0x0100, "Window opener", VeluxProductType.SLIDER_WINDOW),
        WINDOW_4_1((short) 0x0101, "Window opener", VeluxProductType.SLIDER_WINDOW),
        OPENER_5_0((short) 0x0140, "Garage door opener", VeluxProductType.SLIDER_SHUTTER),
        OPENER_5_8((short) 0x017A, "Garage door opener", VeluxProductType.SLIDER_SHUTTER),
        LIGHT_6_0((short) 0x0180, "Light", VeluxProductType.SLIDER_SHUTTER),
        LIGHT_6_5((short) 0x01BA, "Light", VeluxProductType.SLIDER_SHUTTER),
        OPENER_7_0((short) 0x01C0, "Gate opener", VeluxProductType.SLIDER_SHUTTER),
        OPENER_7_5((short) 0x01FA, "Gate opener", VeluxProductType.SLIDER_SHUTTER),
        LOCK_9_0((short) 0x0240, "Door lock", VeluxProductType.SLIDER_SHUTTER),
        LOCK_9_1((short) 0x0241, "Window lock", VeluxProductType.SLIDER_SHUTTER),
        BLIND_10((short) 0x0280, "Vertical Interior Blinds", VeluxProductType.SLIDER_SHUTTER),
        SHUTTER_13((short) 0x0340, "Dual Roller Shutter", VeluxProductType.SLIDER_SHUTTER),
        SWITCH_15((short) 0x03C0, "On/Off switch", VeluxProductType.SWITCH),
        AWNING_16((short) 0x0400, "Horizontal awning", VeluxProductType.SLIDER_SHUTTER),
        BLIND_17((short) 0x0440, "Exterior Venetian blind", VeluxProductType.SLIDER_SHUTTER),
        BLIND_18((short) 0x0480, "Louver blind", VeluxProductType.SLIDER_SHUTTER),
        TRACK_19((short) 0x04C0, "Curtain track", VeluxProductType.SLIDER_SHUTTER),
        POINT_20((short) 0x0500, "Ventilation point", VeluxProductType.SLIDER_SHUTTER),
        POINT_20_1((short) 0x0501, "Ventilation point", VeluxProductType.SLIDER_SHUTTER),
        POINT_20_2((short) 0x0502, "Ventilation point", VeluxProductType.SLIDER_SHUTTER),
        POINT_20_3((short) 0x0503, "Ventilation point", VeluxProductType.SLIDER_SHUTTER),
        HEATING_21((short) 0x0540, "Exterior heating", VeluxProductType.SLIDER_SHUTTER),
        HEATING_21_5((short) 0x57A, "Exterior heating", VeluxProductType.SLIDER_SHUTTER),
        SHUTTER_24_0((short) 0x0600, "Swinging Shutters", VeluxProductType.SLIDER_SHUTTER),
        SHUTTER_24_1((short) 0x0601, "Swinging Shutters", VeluxProductType.SLIDER_SHUTTER),;

        // Class internal

        private short nodeType;
        private String description;
        private VeluxProductType typeClass;

        // Reverse-lookup map for getting an ActuatorType from a TypeId
        private static final Map<Integer, ActuatorType> LOOKUPTYPEID2ENUM = Stream.of(ActuatorType.values())
                .collect(Collectors.toMap(ActuatorType::getNodeType, Function.identity()));

        // Constructor

        private ActuatorType(short nodeType, String description, VeluxProductType typeClass) {
            this.nodeType = nodeType;
            this.description = description;
            this.typeClass = typeClass;
        }

        // Class access methods

        public int getNodeType() {
            return nodeType;
        }

        public String getDescription() {
            return description;
        }

        public VeluxProductType getTypeClass() {
            return typeClass;
        }

        public static ActuatorType get(int nodeType) {
            return LOOKUPTYPEID2ENUM.getOrDefault(nodeType, ActuatorType.UNDEFTYPE);
        }
    }

    // Class access methods

    public static VeluxProductType get(int nodeType) {
        if (ActuatorType.get(nodeType) != ActuatorType.UNDEFTYPE) {
            return ActuatorType.get(nodeType).typeClass;
        } else {
            return VeluxProductType.UNDEFTYPE;
        }
    }

    public static String toString(int nodeType) {
        return ActuatorType.get(nodeType).getDescription();
    }
}
