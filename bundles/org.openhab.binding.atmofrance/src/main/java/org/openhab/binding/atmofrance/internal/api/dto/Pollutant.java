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
package org.openhab.binding.atmofrance.internal.api.dto;

import java.util.EnumSet;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link Pollutant} enum lists all pollutants tracked by Atmo France
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public enum Pollutant {
    PM25,
    PM10,
    NO2,
    O3,
    SO2;

    public static final EnumSet<Pollutant> AS_SET = EnumSet.allOf(Pollutant.class);
}
