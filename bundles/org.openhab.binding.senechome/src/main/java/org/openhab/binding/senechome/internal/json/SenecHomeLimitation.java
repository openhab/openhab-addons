/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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