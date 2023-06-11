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
package org.openhab.binding.boschshc.internal.devices.bridge.dto;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.boschshc.internal.services.dto.BoschSHCServiceState;

import com.google.gson.JsonElement;

/**
 * Represents a device status update as represented by the Smart Home
 * Controller.
 *
 * @author Stefan Kästle - Initial contribution
 * @author Christian Oeing - refactorings of e.g. server registration
 */
public class DeviceServiceData extends BoschSHCServiceState {

    /**
     * Url path of the service the update came from.
     */
    public String path;

    /**
     * Name of service the update came from.
     */
    public String id;

    /**
     * Current state of device. Serialized as JSON.
     */
    public @Nullable JsonElement state;

    /**
     * Id of device the update is for.
     */
    public @Nullable String deviceId;

    /**
     * An optional object containing information about device faults.
     * <p>
     * Example: low battery warnings are stored as faults with category <code>WARNING</code>
     */
    public @Nullable Faults faults;

    public DeviceServiceData() {
        super("DeviceServiceData");
    }

    @Override
    public String toString() {
        return this.deviceId + " state: " + this.type;
    }
}
