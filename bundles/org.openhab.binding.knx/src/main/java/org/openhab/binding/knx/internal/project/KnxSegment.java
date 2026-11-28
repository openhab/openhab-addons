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
package org.openhab.binding.knx.internal.project;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A segment of a line (ETS 6 and later).
 *
 * @param id the full id, {@code P-xxxx-n_S-n}
 * @param number the segment number, if stated
 * @param mediumTypeRefId the medium of the segment, a master data id such as {@code MT-0}, if stated
 * @param devices the devices in the segment
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
public record KnxSegment(String id, @Nullable Integer number, @Nullable String mediumTypeRefId,
        List<KnxDevice> devices) {

    public KnxSegment {
        devices = List.copyOf(devices);
    }
}
