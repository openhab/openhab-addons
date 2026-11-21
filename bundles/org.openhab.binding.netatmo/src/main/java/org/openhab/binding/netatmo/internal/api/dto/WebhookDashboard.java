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
package org.openhab.binding.netatmo.internal.api.dto;

import java.time.ZonedDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.TrendDescription;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link WebhookDashboard} holds data pushed for modules through webhook
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class WebhookDashboard {
    public @Nullable Double rain;
    public @Nullable Double co2;
    public @Nullable Boolean co2Calibrating;
    public @NonNullByDefault({}) String firmware;
    public @Nullable Boolean homekitMigrated;
    public @Nullable Boolean homekitPaired;
    public @Nullable Double humidity;
    public @NonNullByDefault({}) String hwVersion;
    public @NonNullByDefault({}) String id;
    public @Nullable Double noiseAvg;
    public @Nullable Double noiseCurrent;
    @SerializedName("pressure_abs")
    public @Nullable Double absolutePressure;
    public @Nullable Double pressureSea;
    public @Nullable Double temperature;
    @SerializedName("trend_pressure")
    public @Nullable TrendDescription pressureTrend;
    @SerializedName("trend_temperature")
    public @Nullable TrendDescription tempTrend;
    public @Nullable Boolean usbStatus;
    @SerializedName(value = "rf_status", alternate = { "wifi_status" })
    public int radioStatus = -1;
    public @Nullable Integer rfAmbStatus;

    public @Nullable Boolean windGaugeFactoryCalibrated;

    public @Nullable Integer battery_vp;
    @SerializedName(value = "timestamp", alternate = { "last_seen" })
    public @Nullable ZonedDateTime timestamp;
}
