/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
 * The {@link JablotronState} class defines the state object for the
 * getSections response
 *
 * @author Ondrej Pecta - Initial contribution
 */
@NonNullByDefault
public class JablotronState {

    @SerializedName(value = "cloud-component-id", alternate = "component-id")
    String cloudComponentId = "";

    @SerializedName(value = "object-device-id")
    String objectDeviceId = "";

    String state = "";
    float temperature = 0;

    public String getCloudComponentId() {
        return cloudComponentId;
    }

    public String getState() {
        return state;
    }

    public float getTemperature() {
        return temperature;
    }

    public String getObjectDeviceId() {
        return objectDeviceId;
    }
}
