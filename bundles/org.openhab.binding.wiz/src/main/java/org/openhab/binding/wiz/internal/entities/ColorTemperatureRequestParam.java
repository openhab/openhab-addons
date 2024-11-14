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
package org.openhab.binding.wiz.internal.entities;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.Expose;

/**
 * This POJO represents Color Request Param
 *
 * The outgoing JSON should look like this:
 *
 * {"id": 24, "method": "setPilot", "params": {"temp": 3000}}
 *
 * @author Alexander Seeliger - Initial contribution
 *
 */
@NonNullByDefault
public class ColorTemperatureRequestParam implements Param {
    @Expose
    private int temp;

    public ColorTemperatureRequestParam(int temp) {
        this.temp = temp;
    }

    public int getColorTemperature() {
        return temp;
    }

    public void setColorTemperature(int temp) {
        this.temp = temp;
    }
}
