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
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public enum UpdateStatusV2 {
    NO_UPDATE,
    UPDATE_PENDING,
    READY_TO_INSTALL,
    INSTALLING;

    public static UpdateStatusV2 of(@Nullable String value) {
        if (value != null) {
            try {
                return valueOf(value.toUpperCase());
            } catch (IllegalArgumentException e) {
                // fall through
            }
        }
        return NO_UPDATE;
    }

    public static UpdateStatusV2 of(UpdateStatusV1 statusV1) {
        return switch (statusV1) {
            case READYTOINSTALL -> READY_TO_INSTALL;
            case AVAILABLE, DOWNLOADING -> UPDATE_PENDING;
            case INSTALLING -> INSTALLING;
            default -> NO_UPDATE;
        };
    }

    @Override
    public String toString() {
        String s = name();
        return s.substring(0, 1) + s.substring(1).replace("_", " ").toLowerCase();
    }
}
