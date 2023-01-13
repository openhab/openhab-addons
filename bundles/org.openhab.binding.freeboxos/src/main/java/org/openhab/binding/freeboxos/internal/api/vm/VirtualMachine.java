/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.freeboxos.internal.api.ApiConstants.VmStatus;

/**
 * The {@link VirtualMachine} is the Java class used to map the "VirtualMachine" structure used by the Virtual Machine
 * API
 *
 * https://dev.freebox.fr/sdk/os/lan/#lan-browser
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class VirtualMachine {
    private int id;
    private @Nullable String name;
    private @Nullable String mac;
    private VmStatus status = VmStatus.UNKNOWN;

    public int getId() {
        return id;
    }

    public String getName() {
        return Objects.requireNonNull(name);
    }

    public String getMac() {
        return Objects.requireNonNull(mac).toLowerCase();
    }

    public VmStatus getStatus() {
        return status;
    }
}
