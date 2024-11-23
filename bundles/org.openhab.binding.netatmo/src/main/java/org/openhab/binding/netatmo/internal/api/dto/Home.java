/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.netatmo.internal.api.dto;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.netatmo.internal.api.data.ModuleType;

/**
 * The {@link Home} holds home information.
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */

@NonNullByDefault
public class Home extends Device implements Location {
    private double[] coordinates = {};
    private double altitude;
    private List<HomeEvent> events = List.of();

    @Override
    public ModuleType getType() {
        return ModuleType.HOME;
    }

    @Override
    public double getAltitude() {
        return altitude;
    }

    @Override
    public double[] getCoordinates() {
        return coordinates;
    }

    public List<HomeEvent> getEvents() {
        return events;
    }
}
