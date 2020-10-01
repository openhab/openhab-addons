/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.freebox.internal.api.model;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link VirtualMachine} is the Java class used to map the "LanHost"
 * structure used by the Lan Hosts Browser API
 * https://dev.freebox.fr/sdk/os/lan/#lan-browser
 *
 * @author Gaël L'hopital - Initial contribution
 */
public class VirtualMachine {
    public static enum Status {
        @SerializedName("stopped")
        STOPPED,
        @SerializedName("running")
        RUNNING;
    }

    private String mac;
    private int id;
    private Status status;
    private String name;

    public int getId() {
        return id;
    }

    public String getMac() {
        return mac;
    }

    public Status getStatus() {
        return status;
    }

    public String getName() {
        return name;
    }
}
