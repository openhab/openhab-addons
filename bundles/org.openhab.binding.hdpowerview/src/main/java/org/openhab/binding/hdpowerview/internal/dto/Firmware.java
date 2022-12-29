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
package org.openhab.binding.hdpowerview.internal.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Firmware version information for HD PowerView components
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class Firmware {
    @Nullable
    public String name;
    public int revision;
    public int subRevision;
    public int build;

    @Override
    public String toString() {
        return String.format("%d.%d.%d", revision, subRevision, build);
    }
}
