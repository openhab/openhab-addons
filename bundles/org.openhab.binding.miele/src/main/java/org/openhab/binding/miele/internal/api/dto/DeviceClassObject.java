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
package org.openhab.binding.miele.internal.api.dto;

import com.google.gson.JsonArray;

/**
 * The {@link DeviceClassObject} class represents the DeviceClassObject node in the response JSON.
 *
 * @author Jacob Laursen - Initial contribution
 **/
public class DeviceClassObject {
    public String DeviceClassType;
    public JsonArray Operations;
    public String DeviceClass;
    public JsonArray Properties;

    public DeviceClassObject() {
    }
}
