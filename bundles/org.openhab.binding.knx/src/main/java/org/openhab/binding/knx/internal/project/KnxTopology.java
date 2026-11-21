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

/**
 * The topology of the installation.
 *
 * @param areas the areas in file order
 * @param unplacedDevices devices the project keeps outside any line; they have no individual address
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
public record KnxTopology(List<KnxArea> areas, List<KnxDevice> unplacedDevices) {

    public KnxTopology {
        areas = List.copyOf(areas);
        unplacedDevices = List.copyOf(unplacedDevices);
    }

    /** Every device: those on a line in topology order, then the unplaced ones. */
    public List<KnxDevice> devices() {
        List<KnxDevice> devices = new ArrayList<>();
        for (KnxArea area : areas) {
            for (KnxLine line : area.lines()) {
                devices.addAll(line.allDevices());
            }
        }
        devices.addAll(unplacedDevices);
        return List.copyOf(devices);
    }
}
