/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.freeboxos.internal.api.login;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link Session} is the Java class used to map the
 * structure used by the response of the open session API
 * https://dev.freebox.fr/sdk/os/login/#
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public class Session {
    public enum Permission {
        @SerializedName("parental")
        PARENTAL,
        @SerializedName("contacts")
        CONTACTS,
        @SerializedName("explorer")
        EXPLORER,
        @SerializedName("tv")
        TV,
        @SerializedName("wdo")
        WDO,
        @SerializedName("downloader")
        DOWNLOADER,
        @SerializedName("profile")
        PROFILE,
        @SerializedName("camera")
        CAMERA,
        @SerializedName("settings")
        SETTINGS,
        @SerializedName("calls")
        CALLS,
        @SerializedName("home")
        HOME,
        @SerializedName("pvr")
        PVR,
        @SerializedName("vm")
        VM,
        @SerializedName("player")
        PLAYER;
    }

    private @NonNullByDefault({}) String sessionToken;
    private @Nullable String challenge;
    private @NonNullByDefault({}) Map<Permission, @Nullable Boolean> permissions;

    public String getSessionToken() {
        return sessionToken;
    }

    public @Nullable String getChallenge() {
        return challenge;
    }

    public Map<Permission, @Nullable Boolean> getPermissions() {
        return permissions;
    }
}
