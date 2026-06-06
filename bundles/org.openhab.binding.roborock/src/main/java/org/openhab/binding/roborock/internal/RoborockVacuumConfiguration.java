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
package org.openhab.binding.roborock.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link RoborockVacuumConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Paul Smedley - Initial contribution
 */
@NonNullByDefault
public class RoborockVacuumConfiguration {

    public static final String REFRESH_ON = "on";
    public static final String REFRESH_OFF = "off";
    public static final int MAP_REFRESH_CLOUD_CLEANING_DEFAULT_SECONDS = 30;
    public static final int MAP_REFRESH_DIRECT_CLEANING_DEFAULT_SECONDS = 15;
    public static final int MAP_REFRESH_CLOUD_CLEANING_MIN_SECONDS = 30;
    public static final int MAP_REFRESH_DIRECT_CLEANING_MIN_SECONDS = 15;
    public static final int LEGACY_REFRESH_DEFAULT_MINUTES = 5;
    public static final int CLOUD_REFRESH_MIN_SECONDS = 60;

    /**
     * Vacuum configuration parameters.
     */
    public String duid = "";
    public int refresh = LEGACY_REFRESH_DEFAULT_MINUTES; // legacy input in minutes
    public int fastRefreshInterval = 15; // in seconds, used while vacuum channel is ON
    public int cloudRefreshInterval = 0; // in seconds, fallback to legacy refresh when <= 0
    public String communication = RoborockCommunicationMode.CLOUD.toConfigValue();
    public String localHost = "";
    public int localPort = 58867;
    public String cloudMapRefresh = REFRESH_ON;
    public String cloudMetadataRefresh = REFRESH_ON;
    public int mapRefreshCloudCleaningInterval = MAP_REFRESH_CLOUD_CLEANING_DEFAULT_SECONDS;
    public int mapRefreshDirectCleaningInterval = MAP_REFRESH_DIRECT_CLEANING_DEFAULT_SECONDS;

    public int getRefreshIntervalSeconds() {
        return VacuumRefreshPolicy.normalizeLegacyRefreshMinutesToSeconds(refresh, LEGACY_REFRESH_DEFAULT_MINUTES,
                CLOUD_REFRESH_MIN_SECONDS);
    }

    public int getCloudRefreshIntervalSeconds() {
        return VacuumRefreshPolicy.normalizeIntervalSeconds(cloudRefreshInterval, getRefreshIntervalSeconds(),
                CLOUD_REFRESH_MIN_SECONDS);
    }

    public int getFastRefreshIntervalSeconds() {
        return fastRefreshInterval > 0 ? fastRefreshInterval : 15;
    }

    public int getMapRefreshCloudCleaningIntervalSeconds() {
        return VacuumRefreshPolicy.normalizeIntervalSeconds(mapRefreshCloudCleaningInterval,
                MAP_REFRESH_CLOUD_CLEANING_DEFAULT_SECONDS, MAP_REFRESH_CLOUD_CLEANING_MIN_SECONDS);
    }

    public int getMapRefreshDirectCleaningIntervalSeconds() {
        return VacuumRefreshPolicy.normalizeIntervalSeconds(mapRefreshDirectCleaningInterval,
                MAP_REFRESH_DIRECT_CLEANING_DEFAULT_SECONDS, MAP_REFRESH_DIRECT_CLEANING_MIN_SECONDS);
    }

    public int getMapRefreshDuringCleaningIntervalSeconds(RoborockCommunicationMode mode) {
        return mode == RoborockCommunicationMode.DIRECT ? getMapRefreshDirectCleaningIntervalSeconds()
                : getMapRefreshCloudCleaningIntervalSeconds();
    }

    public RoborockCommunicationMode getCommunicationMode() {
        return RoborockCommunicationMode.fromConfigValue(communication);
    }

    public @Nullable String getLocalHostOrNull() {
        String value = localHost.trim();
        return value.isEmpty() ? null : value;
    }

    public boolean isCloudMapRefreshEnabled() {
        return !REFRESH_OFF.equalsIgnoreCase(cloudMapRefresh);
    }

    public boolean isCloudMetadataRefreshEnabled() {
        return !REFRESH_OFF.equalsIgnoreCase(cloudMetadataRefresh);
    }
}
