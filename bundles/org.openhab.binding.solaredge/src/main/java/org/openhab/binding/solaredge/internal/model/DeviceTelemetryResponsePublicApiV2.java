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
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Telemetry returned for site meters and storage devices by Monitoring API V2.
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
public class DeviceTelemetryResponsePublicApiV2 {
    public static class Measurement {
        public @Nullable String timestamp;
        public @Nullable Double value;
    }

    public static class Series {
        public @Nullable String unit;
        public @Nullable List<Measurement> values;
    }

    public static class MeterTelemetry {
        public @Nullable Series productionEnergy;
        public @Nullable Series productionPower;
        public @Nullable Series consumptionEnergy;
        public @Nullable Series consumptionPower;
        public @Nullable Series importEnergy;
        public @Nullable Series importPower;
        public @Nullable Series exportEnergy;
        public @Nullable Series exportPower;
    }

    public static class StorageTelemetry {
        public @Nullable Series chargePower;
        public @Nullable Series dischargePower;
        public @Nullable Series chargeEnergy;
        public @Nullable Series dischargeEnergy;
        public @Nullable Series remainingEnergy;
        public @Nullable Series stateOfEnergy;
    }

    public @Nullable String resolution;
    public @Nullable Map<String, MeterTelemetry> meters;
    public @Nullable Map<String, StorageTelemetry> storage;
}
