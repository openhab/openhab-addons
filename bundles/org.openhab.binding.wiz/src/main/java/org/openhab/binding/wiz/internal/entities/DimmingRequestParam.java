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
 * This POJO represents Dimming Request Param
 *
 * The outgoing JSON should look like this:
 *
 * {"id": 24, "method": "setPilot", "params": {"dimming": 10}}
 *
 * NOTE: Dimming cannot be set below 10%. Sending a command with a value of less
 * than 10 will cause the bulb to reply with an error.
 *
 * @author Sriram Balakrishnan - Initial contribution
 *
 */
@NonNullByDefault
public class DimmingRequestParam extends StateRequestParam {
    @Expose
    private int dimming;

    public DimmingRequestParam(int dimming) {
        super(true);
        setDimming(dimming);
    }

    public int getDimming() {
        return dimming;
    }

    public void setDimming(int dimming) {
        if (dimming <= 10) {
            dimming = 10;
        }
        if (dimming >= 100) {
            dimming = 100;
        }
        this.dimming = dimming;
    }
}
