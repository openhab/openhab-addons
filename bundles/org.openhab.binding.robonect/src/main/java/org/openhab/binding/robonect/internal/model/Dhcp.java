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
package org.openhab.binding.robonect.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * DHCP range returned for the optional access point.
 *
 * @author Nico Brüttner - Initial contribution
 */
@NonNullByDefault
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
