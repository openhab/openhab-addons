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
package org.openhab.binding.solaredge.internal.model;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Measurement envelope returned by SolarEdge Monitoring API V2 power and energy endpoints.
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
public class MeasurementsResponsePublicApiV2 {

    public static class Measurement {
        public @Nullable String timestamp;
        public @Nullable Double value;
    }

    public @Nullable String unit;
    public @Nullable String resolution;
    public @Nullable List<Measurement> values;
}
