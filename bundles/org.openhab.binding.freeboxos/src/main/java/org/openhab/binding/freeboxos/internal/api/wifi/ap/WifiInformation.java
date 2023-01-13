/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.freeboxos.internal.api.wifi.ap;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class WifiInformation {
    private @Nullable String band;
    private @Nullable String ssid;
    private int signal = 1; // Valid RSSI values goes from -120 to 0

    public String getBand() {
        return Objects.requireNonNull(band);
    }

    public String getSsid() {
        return Objects.requireNonNull(ssid);
    }

    public int getSignal() {
        return signal;
    }
}
