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
 * This POJO represents Pulse Request Param
 *
 * The outgoing JSON should look like this:
 *
 * {"id": 22, "method": "pulse", "params": {"delta": 30, "duration": 900}}
 *
 * @author Sara Geleskie Damiano - Initial contribution
 *
 */
@NonNullByDefault
public class PulseRequestParam implements Param {
    @Expose
    private int delta;
    @Expose
    private int duration;

    public PulseRequestParam(int delta, int duration) {
        this.delta = delta;
        this.duration = duration;
    }

    public int getDelta() {
        return delta;
    }

    public void setDelta(int delta) {
        this.delta = delta;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.delta = duration;
    }
}
