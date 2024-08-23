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
package org.openhab.binding.bondhome.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * This POJO represents the version information of the bond bridge
 *
 * The incoming JSON looks like this:
 *
 * {"target": "snowbird", "fw_ver": "v2.5.2", "fw_date": "Fri Feb 22 14:13:25
 * -03 2019", "make": "Olibra LLC", "model": "model", "branding_profile":
 * "O_SNOWBIRD", "uptime_s": 380, "_": "c342ae74"}
 *
 * @author Sara Geleskie Damiano - Initial contribution
 */
@NonNullByDefault
public class BondSysVersion {
    // The current state hash
    @SerializedName("_")
    @Expose(serialize = false, deserialize = true)
    public @Nullable String hash;

    @Expose(serialize = true, deserialize = true)
    public @Nullable String target;

    @SerializedName("fw_ver")
    @Expose(serialize = true, deserialize = true)
    public @Nullable String firmwareVersion;

    @SerializedName("fw_date")
    @Expose(serialize = true, deserialize = true)
    public @Nullable String firmwareDate;

    @Expose(serialize = true, deserialize = true)
    public @Nullable String make;

    @Expose(serialize = true, deserialize = true)
    public @Nullable String model;

    @SerializedName("branding_profile")
    @Expose(serialize = true, deserialize = true)
    public @Nullable String brandingProfile;

    @Expose(serialize = true, deserialize = true)
    public @Nullable String bondid;

    @SerializedName("upgrade_http")
    @Expose(serialize = true, deserialize = true)
    public @Nullable Boolean upgradeHttp;

    @Expose(serialize = true, deserialize = true)
    public int api;

    @SerializedName("uptime_s")
    @Expose(serialize = true, deserialize = true)
    public int uptimeSeconds;
}
