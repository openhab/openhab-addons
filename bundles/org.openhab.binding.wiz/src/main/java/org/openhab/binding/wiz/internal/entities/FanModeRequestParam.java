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
 * This POJO represents Fan Reverse Request Param
 *
 * @author Cody Cutrer - Initial Contribution
 */
@NonNullByDefault
public class FanModeRequestParam implements Param {
    @Expose
    private int fanMode; // true = 1, false = 0

    public FanModeRequestParam(int fanMode) {
        this.fanMode = fanMode;
    }

    public int getFanMode() {
        return fanMode;
    }

    public void setFanMode(int fanMode) {
        this.fanMode = fanMode;
    }
}
