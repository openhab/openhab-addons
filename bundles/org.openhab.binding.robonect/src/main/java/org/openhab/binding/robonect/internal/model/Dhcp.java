/* Copyright (c) 2010-2026 Contributors to the openHAB project
 * SPDX-License-Identifier: EPL-2.0 */
package org.openhab.binding.robonect.internal.model;

import org.eclipse.jdt.annotation.Nullable;

/**
 * DHCP range returned for the optional access point.
 *
 * @author Nico Brüttner - Initial contribution
 */
public class Dhcp {
    private @Nullable Boolean enable;
    private @Nullable String start;
    private @Nullable String end;

    /**
     * Reports whether DHCP is enabled.
     *
     * @return whether DHCP is enabled, or {@code null} if absent
     */
    public @Nullable Boolean getEnable() {
        return enable;
    }

    /**
     * Returns the start of the DHCP address range.
     *
     * @return the start of the DHCP address range, or {@code null} if absent
     */
    public @Nullable String getStart() {
        return start;
    }

    /**
     * Returns the end of the DHCP address range.
     *
     * @return the end of the DHCP address range, or {@code null} if absent
     */
    public @Nullable String getEnd() {
        return end;
    }
}
