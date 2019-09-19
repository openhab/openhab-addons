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
package org.openhab.binding.nanoleaf.internal.model;

import com.google.gson.annotations.SerializedName;

/**
 * Represents color temperature of the light panels
 *
 * @author Martin Raepple - Initial contribution
 */
public class Command {

    @SerializedName("write")
    private Write write;

    public Write getWrite() {
        return write;
    }

    public void setWrite(Write write) {
        this.write = write;
    }

}
