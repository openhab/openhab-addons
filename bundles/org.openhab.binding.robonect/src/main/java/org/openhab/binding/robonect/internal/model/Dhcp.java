/* Copyright (c) 2010-2026 Contributors to the openHAB project
 * SPDX-License-Identifier: EPL-2.0 */
package org.openhab.binding.robonect.internal.model;

import org.eclipse.jdt.annotation.Nullable;

/** DHCP range returned for the optional access point. */
public class Dhcp {
    private @Nullable Boolean enable;
    private @Nullable String start;
    private @Nullable String end;

    public @Nullable Boolean getEnable() {
        return enable;
    }

    public @Nullable String getStart() {
        return start;
    }

    public @Nullable String getEnd() {
        return end;
    }
}
