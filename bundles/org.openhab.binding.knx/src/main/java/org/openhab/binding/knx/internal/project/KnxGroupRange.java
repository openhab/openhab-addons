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
 * A folder of the group address tree. The tree is for display only: an address is defined by its number,
 * not by the range it sits in.
 *
 * @param id the full id, {@code P-xxxx-n_GR-n}
 * @param name the name, {@code ""} when it has none
 * @param description the description, if any
 * @param rangeStart the first raw address of the range, if stated
 * @param rangeEnd the last raw address of the range, if stated
 * @param ranges the ranges inside this one
 * @param groupAddresses the addresses directly in this range
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
public record KnxGroupRange(String id, String name, @Nullable String description, @Nullable Integer rangeStart,
        @Nullable Integer rangeEnd, List<KnxGroupRange> ranges, List<KnxGroupAddress> groupAddresses) {

    public KnxGroupRange {
        ranges = List.copyOf(ranges);
        groupAddresses = List.copyOf(groupAddresses);
    }
}
