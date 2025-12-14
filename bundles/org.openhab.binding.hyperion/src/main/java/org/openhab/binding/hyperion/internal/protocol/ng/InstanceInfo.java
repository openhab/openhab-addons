/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.hyperion.internal.protocol.ng;

import com.google.gson.annotations.SerializedName;

public class InstanceInfo {

    @SerializedName("friendly_name")
    private String friendlyName;

    @SerializedName("instance")
    private int instance;

    @SerializedName("running")
    private boolean running;

    public String getFriendlyName() {
        return friendlyName;
    }

    public int getInstance() {
        return instance;
    }

    public boolean isRunning() {
        return running;
    }
}
