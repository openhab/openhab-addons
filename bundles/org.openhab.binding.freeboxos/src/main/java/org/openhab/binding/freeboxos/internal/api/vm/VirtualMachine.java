/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.freeboxos.internal.api.vm;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link VirtualMachine} is the Java class used to map the "LanHost"
 * structure used by the Lan Hosts Browser API
 * https://dev.freebox.fr/sdk/os/lan/#lan-browser
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class VirtualMachine {
    public static enum Status {
        UNKNOWN,
        @SerializedName("stopped")
        STOPPED,
        @SerializedName("running")
        RUNNING;
    }

    private @NonNullByDefault({}) String mac;
    private int id;
    private @Nullable Status status;
    private @Nullable String name;

    public int getId() {
        return id;
    }

    public String getMac() {
        return mac.toLowerCase();
    }

    public Status getStatus() {
        Status localStatus = status;
        return localStatus != null ? localStatus : Status.UNKNOWN;
    }

    public @Nullable String getName() {
        return name;
    }
}
