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
package org.openhab.binding.iotawatt.internal.model;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.unit.Units;

/**
 * Enum for each channel type of IoTaWatt supported by this binding.
 *
 * @author Peter Rosenberg - Initial contribution
 */
@NonNullByDefault
public enum IoTaWattChannelType {
    /**
     * Electrical current
     */
    AMPS("amps", "amps", "Number:power", Units.AMPERE),
    /**
     * AC Frequency
     */
    FREQUENCY("frequency", "frequency", "Number:Frequency", Units.HERTZ),
    /**
     * Power Factor
     */
    POWER_FACTOR("power-factor", "power-factor", "Number:Dimensionless", Units.ONE),
    /**
     * Apparent Power
     */
    APPARENT_POWER("apparent-power", "apparent-power", "Number:power", Units.VOLT_AMPERE),
    /**
     * Reactive Power
     */
    REACTIVE_POWER("reactive-power", "reactive-power", "Number:power", Units.VAR),
    /**
     * Reactive Power Hour
     */
    REACTIVE_POWER_HOUR("reactive-power-hour", "reactive-power-hour", "Number:Energy", Units.VAR_HOUR),
    /**
     * Voltage
     */
    VOLTAGE("voltage", "voltage", "Number:ElectricPotential", Units.VOLT),
    /**
     * Watt, Active Power
     */
    WATTS("watts", "watts", "Number:Power", Units.WATT),
    /**
     * Phase
     */
    PHASE("phase", "phase", "Number:Dimensionless", Units.ONE);

    /**
     * Id of the channel in XML definition channel-type id.
     */
    public final String typeId;
    /**
     * Defines the last part of the channel UID.
     */
    public final String channelIdSuffix;
    /**
     * The value type the channel accepts.
     */
    public final String acceptedItemType;
    /**
     * The unit of the channel.
     */
    public final Unit<?> unit;

    /**
     * Creates an IoTaWattChannelType
     * 
     * @param typeId The TypeId
     * @param channelIdSuffix The suffix of the channelId
     * @param acceptedItemType The acceptedItemType
     * @param unit The unit of the channel
     */
    IoTaWattChannelType(String typeId, String channelIdSuffix, String acceptedItemType, Unit<?> unit) {
        this.acceptedItemType = acceptedItemType;
        this.typeId = typeId;
        this.channelIdSuffix = channelIdSuffix;
        this.unit = unit;
    }

    /**
     * Gets an IoTaWattChannelType
     * 
     * @param value The units to get an IoTaWattChannelType from
     * @return The IoTaWattChannelType
     */
    public static IoTaWattChannelType fromOutputUnits(String value) {
        return switch (value) {
            case "Amps" -> IoTaWattChannelType.AMPS;
            case "Hz" -> IoTaWattChannelType.FREQUENCY;
            case "PF" -> IoTaWattChannelType.POWER_FACTOR;
            case "VA" -> IoTaWattChannelType.APPARENT_POWER;
            case "VAR" -> IoTaWattChannelType.REACTIVE_POWER;
            case "VARh" -> IoTaWattChannelType.REACTIVE_POWER_HOUR;
            case "Volts" -> IoTaWattChannelType.VOLTAGE;
            case "Watts" -> IoTaWattChannelType.WATTS;
            default -> throw new IllegalArgumentException("Unknown value " + value);
        };
    }
}
