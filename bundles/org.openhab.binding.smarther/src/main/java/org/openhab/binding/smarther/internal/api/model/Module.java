/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.smarther.internal.api.model;

import com.google.gson.annotations.SerializedName;

/**
 * Smarther API Module DTO class.
 *
 * @author Fabio Possieri - Initial contribution
 */
public class Module {

    @SerializedName("device")
    private String deviceType;
    private String id;
    private String name;

    public String getDeviceType() {
        return deviceType;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return String.format("id=%s, name=%s, type=%s", id, name, deviceType);
    }

}
