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

    UpdateStatusV2(String i18nKey) {
        this.i18nKey = i18nKey;
    }

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

    @Override
    public String toString() {
        String s = this.name().replace("_", " ");
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }

    public String getI18nKey() {
        return i18nKey;
    }
}
