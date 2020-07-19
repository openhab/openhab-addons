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
package org.openhab.binding.boschshc.internal.shuttercontrol;

import com.google.gson.annotations.SerializedName;

public class ShutterControlState {
    @SerializedName("@type")
    public String type = "shutterControlState";

    /**
     * Current open ratio of shutter (0.0 [closed] to 1.0 [open])
     */
    public double level;

    /**
     * Current operation state of shutter
     */
    public OperationState operationState;

    public ShutterControlState() {
        this.level = 0.0;
    }

    public ShutterControlState(double level) {
        this.level = level;
    }
}
