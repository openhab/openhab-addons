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
package org.openhab.binding.hue.internal.api.dto.clip2.enums;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hue.internal.api.dto.clip1.enums.UpdateStatusV1;

import com.google.gson.annotations.SerializedName;

/**
 * Enum for device software update status.
 * 
 * Note: UPDATE_AVAILABLE and INSTALL_FAILED are not officially documented but are inferred
 * from third party sources.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public enum UpdateStatusV2 {
    @SerializedName("no_update")
    NO_UPDATE("@text/update.state.no-update"),

    @SerializedName("update_available")
    UPDATE_AVAILABLE("@text/update.state.update-available"),

    @SerializedName("update_pending")
    UPDATE_PENDING("@text/update.state.update-pending"),

    @SerializedName("ready_to_install")
    READY_TO_INSTALL("@text/update.state.update-ready-to-install"),

    @SerializedName("installing")
    INSTALLING("@text/update.state.installing-update"),

    @SerializedName("install_failed")
    INSTALL_FAILED("@text/update.state.update-install-failed");

    private final String i18nKey;

    UpdateStatusV2(String i18nKey) {
        this.i18nKey = i18nKey;
    }

    /**
     * Create a v2 enum from a v1 enum. Returns null if the input is null.
     */
    public static @Nullable UpdateStatusV2 of(@Nullable UpdateStatusV1 statusV1) {
        if (statusV1 == null) {
            return null;
        }
        return switch (statusV1) {
            case ALL_READY_TO_INSTALL, ANY_READY_TO_INSTALL, READY_TO_INSTALL -> READY_TO_INSTALL;
            case TRANSFERRING, DOWNLOADING -> UPDATE_PENDING;
            case INSTALLING -> INSTALLING;
            default -> NO_UPDATE;
        };
    }

    /**
     * Reverse lookup for the enum based on the toString value. Returns null if no match is found.
     */
    public static @Nullable UpdateStatusV2 reverseLookup(@Nullable String text) {
        if (text == null) {
            return null;
        }
        for (UpdateStatusV2 status : values()) {
            if (status.toString().equalsIgnoreCase(text.trim())) {
                return status;
            }
        }
        return null;
    }

    /**
     * Returns a "Title case" version of the name e.g. "READY_TO_INSTALL" becomes "Ready to install"
     */
    @Override
    public String toString() {
        String s = this.name().replace("_", " ");
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }

    /**
     * Returns an i18n key for localisation of the update status text
     */
    public String i18nKey() {
        return i18nKey;
    }
}
