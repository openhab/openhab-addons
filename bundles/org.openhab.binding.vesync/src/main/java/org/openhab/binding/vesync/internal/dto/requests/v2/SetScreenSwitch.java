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
 * The {@link SetScreenSwitch} class is used as a DTO to hold a payload for the
 * managed device bypass requests to set a child lock switch.
 *
 * @author David Goodyear - Initial contribution
 */
public class SetScreenSwitch extends EmptyPayload {

    public SetScreenSwitch(final boolean enabled) {
        this.screenSwitch = enabled ? 1 : 0;
    }

    @SerializedName("screenSwitch")
    public int screenSwitch = -1;
}
