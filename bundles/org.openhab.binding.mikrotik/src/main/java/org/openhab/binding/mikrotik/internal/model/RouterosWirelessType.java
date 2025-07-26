/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.mikrotik.internal.model;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link RouterosWirelessType} enum define RouterOS wireless types.
 *
 * @author Yurii - Initial contribution
 */

@NonNullByDefault
public enum RouterosWirelessType {
    NONE,
    WIRELESS,
    WIFIWAVE2,
    WIFI,
    DUAL;

    private static final String WIFI_PACKAGE = "wifi-qcom";
    private static final String WIFI_AC_PACKAGE = "wifi-qcom-ac";
    private static final String WIFIWAVE2_PACKAGE = "wifiwave2";
    private static final String WIRELESS_PACKAGE = "wireless";

    public static RouterosWirelessType resolveFromPkgSet(Set<String> installedPkgs,
            @Nullable RouterosRouterboardInfo rbInfo) {
        if (installedPkgs.contains(WIFI_PACKAGE)) {
            return WIFI;
        }

        if (installedPkgs.contains(WIFI_AC_PACKAGE)) {
            return WIFI;
        }

        if (installedPkgs.contains(WIFIWAVE2_PACKAGE)) {
            return WIFIWAVE2;
        }

        if (installedPkgs.contains(WIRELESS_PACKAGE)) {
            return DUAL;
        }

        if (rbInfo == null) {
            return NONE;
        }

        if (rbInfo.hasBuiltinCapsMan()) {
            return WIFI;
        }

        return WIRELESS;
    }
}
