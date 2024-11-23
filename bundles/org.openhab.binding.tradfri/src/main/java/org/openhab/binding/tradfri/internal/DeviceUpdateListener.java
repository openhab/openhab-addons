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
package org.openhab.binding.tradfri.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.tradfri.internal.handler.TradfriGatewayHandler;

import com.google.gson.JsonObject;

/**
 * {@link DeviceUpdateListener} can register on the {@link TradfriGatewayHandler} to be
 * informed about details about devices.
 *
 * @author Kai Kreuzer - Initial contribution
 */
@NonNullByDefault
public interface DeviceUpdateListener {

    /**
     * This method is called when new device information is received.
     *
     * @param instanceId The instance id of the device
     * @param data the json data describing the device
     */
    void onUpdate(String instanceId, JsonObject data);
}
