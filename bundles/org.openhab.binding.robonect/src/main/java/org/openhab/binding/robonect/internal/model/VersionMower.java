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

import org.eclipse.jdt.annotation.Nullable;

/**
 * Mower software and hardware versions reported by version-1 Robonect firmware.
 */
public class VersionMower {

    private @Nullable VersionHardware hardware;
    private @Nullable VersionMsw msw;
    private @Nullable VersionSub sub;

    /**
     * Returns the hardware.
     *
     * @return the hardware, or {@code null} if not reported
     */
    public @Nullable VersionHardware getHardware() {
        return hardware;
    }

    /**
     * Returns the mower software.
     *
     * @return the mower software, or {@code null} if not reported
     */
    public @Nullable VersionMsw getMsw() {
        return msw;
    }

    /**
     * Returns the sub.
     *
     * @return the sub, or {@code null} if not reported
     */
    public @Nullable VersionSub getSub() {
        return sub;
    }
}
