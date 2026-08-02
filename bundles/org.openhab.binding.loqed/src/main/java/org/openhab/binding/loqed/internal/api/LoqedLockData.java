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
package org.openhab.binding.loqed.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * Data returned for one lock by the LOQED Integrations API.
 *
 * @author Ondrej Pecta - Initial contribution
 */
@NonNullByDefault
public class LoqedLockData {
    public boolean online = true;
    public String id = "";
    public String name = "";
    @SerializedName("model_name")
    public String modelName = "";
    @SerializedName("battery_percentage")
    public int batteryPercentage = -1;
    @SerializedName("battery_type")
    public String batteryType = "unknown";
    @SerializedName("bolt_state")
    public BoltState boltState = BoltState.UNKNOWN;
    @SerializedName("party_mode")
    public @Nullable Boolean partyMode;
    @SerializedName("guest_access_mode")
    public @Nullable Boolean guestAccessMode;
    @SerializedName("twist_assist")
    public @Nullable Boolean twistAssist;
    @SerializedName("touch_to_connect")
    public @Nullable Boolean touchToConnect;
}
