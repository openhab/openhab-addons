/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeboxos.internal.api.Response;
import org.openhab.binding.freeboxos.internal.api.rest.FbxDevice;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link VirtualMachine} is the Java class used to map the "VirtualMachine"
 * structure used by the Virtual Machine API
 * https://dev.freebox.fr/sdk/os/lan/#lan-browser
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class VirtualMachine extends FbxDevice {
    public class VirtualMachineResponse extends Response<VirtualMachine> {
    }

    public class VirtualMachinesResponse extends Response<List<VirtualMachine>> {
    }

    public static enum Status {
        UNKNOWN,
        @SerializedName("stopped")
        STOPPED,
        @SerializedName("running")
        RUNNING;
    }

    private Status status = Status.UNKNOWN;

    public Status getStatus() {
        return status;
    }
}
