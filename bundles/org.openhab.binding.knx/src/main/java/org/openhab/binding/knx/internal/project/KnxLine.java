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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A line of the topology. Up to ETS 5 the devices and the medium sit on the line; from ETS 6 on they sit on
 * the line's segments.
 *
 * @param id the full id, {@code P-xxxx-n_L-n}
 * @param address the line number, 0 to 15
 * @param name the name, {@code ""} when it has none
 * @param mediumTypeRefId the medium of the line, a master data id such as {@code MT-0}, if stated on the line
 * @param segments the segments of the line
 * @param devices the devices directly on the line
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
public record KnxLine(String id, int address, String name, @Nullable String mediumTypeRefId, List<KnxSegment> segments,
        List<KnxDevice> devices) {

    public KnxLine {
        segments = List.copyOf(segments);
        devices = List.copyOf(devices);
    }

    /** The devices directly on the line followed by those in its segments. */
    public List<KnxDevice> allDevices() {
        List<KnxDevice> all = new ArrayList<>(devices);
        for (KnxSegment segment : segments) {
            all.addAll(segment.devices());
        }
        return List.copyOf(all);
    }
}
