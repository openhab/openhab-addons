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
package org.openhab.binding.myenergi.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link CommandStatus} is a DTO class used to represent a high level
 * summary of a Zappi device. It's used to deserialize JSON API results.
 *
 * @author Rene Scherer - Initial contribution
 *
 */
@NonNullByDefault
public class CommandStatus {

    // {"status":0,"statustext":"","asn":"s0.myenergi.net"}

    @Nullable
    public Integer status;

    @SerializedName("statustext")
    @Nullable
    public String statusText;

    @Nullable
    public String asn;

    @Override
    public String toString() {
        return "CommandStatus [status=" + status + ", statusText=" + statusText + ", asn=" + asn + "]";
    }
}
