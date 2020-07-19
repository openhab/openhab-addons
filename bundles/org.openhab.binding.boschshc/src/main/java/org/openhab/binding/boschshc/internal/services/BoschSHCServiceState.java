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
package org.openhab.binding.boschshc.internal.services;

import com.google.gson.annotations.SerializedName;

public class BoschSHCServiceState {
    @SerializedName("@type")
    private final String type;

    protected BoschSHCServiceState(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
