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
package org.openhab.binding.bticinosmarther.internal.api.dto;

import org.openhab.binding.bticinosmarther.internal.util.StringUtil;

import com.google.gson.annotations.SerializedName;

/**
 * The {@code Module} class defines the dto for Smarther API chronothermostat module object.
 *
 * @author Fabio Possieri - Initial contribution
 */
public class Module {

    @SerializedName("device")
    private String deviceType;
    private String id;
    private String name;

    /**
     * Returns the device type of the chronothermostat module.
     *
     * @return a string containing the module device type
     */
    public String getDeviceType() {
        return StringUtil.capitalizeAll(deviceType);
    }

    /**
     * Returns the identifier of the chronothermostat module.
     *
     * @return a string containing the module identifier
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the chronothermostat module reference label (i.e. the module "name").
     *
     * @return a string containing the module reference label
     */
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return String.format("id=%s, name=%s, type=%s", id, name, deviceType);
    }
}
