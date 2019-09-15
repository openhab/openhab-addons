/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.senechome.internal.json;

import java.io.Serializable;

import com.google.gson.annotations.SerializedName;

/**
 * Json model of senec home devices: This sub model contains grid related power values.
 *
 * @author Steven Schwarznau - Initial Contribution
 */
public class SenecHomeLimitation implements Serializable {

    private static final long serialVersionUID = -8990871346958824085L;

    public @SerializedName("POWER_RATIO") String powerLimitation;

    @Override
    public String toString() {
        return "SenecHomePower [powerLimitation=" + powerLimitation + "]";
    }
}