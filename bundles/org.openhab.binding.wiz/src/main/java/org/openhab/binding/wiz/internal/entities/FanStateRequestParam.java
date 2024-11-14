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
 * This POJO represents Fan State Request Param
 *
 * @author Stefan Fussenegger - Initial Contribution
 */
@NonNullByDefault
public class FanStateRequestParam implements Param {
    @Expose
    private int fanState; // true = 1, false = 0

    public FanStateRequestParam(int fanState) {
        this.fanState = fanState;
    }

    public int getFanState() {
        return fanState;
    }

    public void setFanState(int fanState) {
        this.fanState = fanState;
    }
}
