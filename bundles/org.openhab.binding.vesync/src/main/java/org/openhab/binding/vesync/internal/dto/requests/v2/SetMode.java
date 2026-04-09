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
package org.openhab.binding.vesync.internal.dto.requests.v2;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link SetMode} class is used as a DTO to hold a payload for the
 * managed device bypass requests to set a mode.
 *
 * @author David Goodyear - Initial contribution
 */
public class SetMode extends EmptyPayload {

    public SetMode(final String mode) {
        this.mode = mode;
    }

    @SerializedName("mode")
    public String mode = "";
}
