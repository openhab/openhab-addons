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
package org.openhab.binding.bluetooth.generic.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.bluetooth.BluetoothBindingConstants;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link GenericBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Connor Petty - Initial contribution
 */
@NonNullByDefault
public class GenericBindingConstants {

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_GENERIC = new ThingTypeUID(BluetoothBindingConstants.BINDING_ID,
            "generic");

    // Field properties
    public static final String PROPERTY_FIELD_NAME = "FieldName";
    public static final String PROPERTY_FIELD_INDEX = "FieldIndex";

    // Characteristic properties
    public static final String PROPERTY_FLAGS = "Flags";
    public static final String PROPERTY_SERVICE_UUID = "ServiceUUID";
    public static final String PROPERTY_CHARACTERISTIC_UUID = "CharacteristicUUID";
}
