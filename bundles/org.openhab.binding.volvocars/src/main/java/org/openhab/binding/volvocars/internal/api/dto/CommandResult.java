/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.volvocars.internal.api.dto;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Result of <code>POST /connected-vehicle/v2/vehicles/{vin}/commands/{command}</code>.
 *
 * @author Michael Weger - Initial contribution
 */
public class CommandResult {
    public static final String STATUS_COMPLETED = "COMPLETED";

    public @Nullable String invokeStatus;
    public @Nullable String message;

    public boolean isCompleted() {
        return STATUS_COMPLETED.equals(invokeStatus);
    }
}
