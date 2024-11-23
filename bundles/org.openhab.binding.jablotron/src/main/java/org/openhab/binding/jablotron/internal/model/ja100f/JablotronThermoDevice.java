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
package org.openhab.binding.jablotron.internal.model.ja100f;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link JablotronThermoDevice} class defines the thermal device object
 * for the getThermoDevices response
 *
 * @author Ondrej Pecta - Initial contribution
 */
@NonNullByDefault
public class JablotronThermoDevice {

    @SerializedName(value = "object-device-id")
    String objectDeviceId = "";

    String name = "";

    public String getObjectDeviceId() {
        return objectDeviceId;
    }

    public String getName() {
        return name;
    }
}
