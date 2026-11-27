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
 * this class is used to map the live data json response via private API
 *
 * @author Georg Kunz - initial contribution
 */
@NonNullByDefault
public class LiveDataResponsePrivateApi {

    public static class Grid {
        public @Nullable Boolean isActive;
        public @Nullable Double currentPower;
        public @Nullable String status;
    }

    public static class Consumption {
        public @Nullable Boolean isActive;
        public @Nullable Double currentPower;
        public @Nullable Boolean isConsuming;
    }

    public static class SolarProduction {
        public @Nullable Boolean isActive;
        public @Nullable Double currentPower;
        public @Nullable Boolean isProducing;
    }

    public static class DcStorage {
        public @Nullable Boolean isActive;
        public @Nullable Double currentPower;
        public @Nullable Double chargeLevel;
        public @Nullable Double blockCount;
        public @Nullable String status;
        public @Nullable String storagePlan;
    }

    public static class EnergyProducers {
        public @Nullable List<String> producers;
    }

    public @Nullable Grid grid;
    public @Nullable Consumption consumption;
    public @Nullable SolarProduction solarProduction;
    public @Nullable DcStorage dcStorage;

    public @Nullable String lastUpdateTime;
    public @Nullable Boolean isRealTime;
    public @Nullable Boolean isCommunicating;
    public @Nullable Integer updateRefreshRate;
}
