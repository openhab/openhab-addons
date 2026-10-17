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
package org.openhab.binding.hasslink.internal.api.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link HomeAssistantConfig} DTO represents the configuration snapshot returned by Home Assistant in response to
 * {@code openhab_bridge/get_config}.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public class HomeAssistantConfig {

    public @Nullable String locationName;
    public @Nullable String timeZone;
    public @Nullable String currency;
    public @Nullable Double latitude;
    public @Nullable Double longitude;
    public @Nullable Double elevation;
    public @Nullable UnitSystem unitSystem;

    /**
     * Contains the unit names configured in Home Assistant.
     */
    public static class UnitSystem {
        public @Nullable String length;
        public @Nullable String accumulatedPrecipitation;
        public @Nullable String area;
        public @Nullable String mass;
        public @Nullable String pressure;
        public @Nullable String temperature;
        public @Nullable String volume;
        public @Nullable String windSpeed;
    }
}
