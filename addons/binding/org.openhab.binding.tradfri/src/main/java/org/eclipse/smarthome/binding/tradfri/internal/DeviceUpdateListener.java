/**
 * Copyright (c) 2014,2019 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.binding.tradfri.internal;

import org.eclipse.smarthome.binding.tradfri.handler.TradfriGatewayHandler;

import com.google.gson.JsonObject;

/**
 * {@link DeviceUpdateListener} can register on the {@link TradfriGatewayHandler} to be
 * informed about details about devices.
 *
 * @author Kai Kreuzer - Initial contribution
 */
public interface DeviceUpdateListener {

    /**
     * This method is called when new device information is received.
     *
     * @param instanceId The instance id of the device
     * @param data the json data describing the device
     */
    public void onUpdate(String instanceId, JsonObject data);
}
