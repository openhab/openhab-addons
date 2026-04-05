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

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

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
    NO_UPDATE("@text/update.state.no-update"),
    UPDATE_AVAILABLE("@text/update.state.update-available"),
    UPDATE_PENDING("@text/update.state.update-pending"),
    READY_TO_INSTALL("@text/update.state.update-ready-to-install"),
    INSTALLING("@text/update.state.installing-update"),
    INSTALL_FAILED("@text/update.state.update-install-failed");

    private final String i18nKey;

    private static final Set<UpdateStatusV2> UPDATE_READY_STATES = //
            Set.of(READY_TO_INSTALL, UPDATE_AVAILABLE, UPDATE_PENDING);

    UpdateStatusV2(String i18nKey) {
        this.i18nKey = i18nKey;
    }

    /**
     * Create enum from a JSON string. Returns null if the input is null or does not match any enum value.
     */
    public static @Nullable UpdateStatusV2 of(@Nullable String value) {
        if (value != null) {
            try {
                return valueOf(value.toUpperCase());
            } catch (IllegalArgumentException e) {
                // fall through
            }
        }
        return null;
    }

    /**
     * Create a v2 enum from a v1 enum. Returns null if the input is null.
     */
    public static @Nullable UpdateStatusV2 of(@Nullable UpdateStatusV1 statusV1) {
        if (statusV1 == null) {
            return null;
        }
        return switch (statusV1) {
            case ALLREADYTOINSTALL, ANYREADYTOINSTALL, READYTOINSTALL -> READY_TO_INSTALL;
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

    /**
     * Returns true if the update status indicates that an update is ready to be installed
     */
    public boolean isUpdateReady() {
        return UPDATE_READY_STATES.contains(this);
    }
}
