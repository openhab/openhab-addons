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

/**
 * An area of the topology.
 *
 * @param id the full id, {@code P-xxxx-n_A-n}
 * @param address the area number, 0 to 15
 * @param name the name, {@code ""} when it has none
 * @param lines the lines of the area
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
public record KnxArea(String id, int address, String name, List<KnxLine> lines) {

    public KnxArea {
        lines = List.copyOf(lines);
    }
}
