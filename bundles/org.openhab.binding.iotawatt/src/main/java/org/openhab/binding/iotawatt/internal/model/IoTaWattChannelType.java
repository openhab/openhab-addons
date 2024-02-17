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
    WATTS("watts", "watts", "Number:Power", Units.WATT),
    VOLTAGE("voltage", "voltage", "Number:ElectricPotential", Units.VOLT),
    FREQUENCY("frequency", "frequency", "Number:Frequency", Units.HERTZ);

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

    IoTaWattChannelType(String typeId, String channelIdSuffix, String acceptedItemType, Unit<?> unit) {
        this.acceptedItemType = acceptedItemType;
        this.typeId = typeId;
        this.channelIdSuffix = channelIdSuffix;
        this.unit = unit;
    }
}
