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

import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Supported vacuum communication modes.
 *
 * @author Paul Smedley - Initial contribution
 * @author Maciej Pham - direct/cloud mode routing support
 */
@NonNullByDefault
public enum RoborockCommunicationMode {
    CLOUD,
    DIRECT;

    public static RoborockCommunicationMode fromConfigValue(String configuredValue) {
        return "direct".equals(configuredValue.trim().toLowerCase(Locale.ROOT)) ? DIRECT : CLOUD;
    }

    public String toConfigValue() {
        return name().toLowerCase(Locale.ROOT);
    }
}
