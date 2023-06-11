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

import com.google.gson.JsonObject;

/**
 * The {@link DeviceProperty} class represents the DeviceProperty node in the response JSON.
 *
 * @author Jacob Laursen - Initial contribution
 **/
public class DeviceProperty {
    public String Name;
    public String Value;
    public int Polling;
    public JsonObject Metadata;

    public DeviceProperty() {
    }
}
