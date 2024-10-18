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
package org.openhab.binding.huesync.internal.api.dto.device;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * HDMI Sync Box Device Information - Wifi connection information
 * 
 * @author Patrik Gfeller - Initial Contribution
 * 
 * @see <a href=
 *      "https://developers.meethue.com/develop/hue-entertainment/hue-hdmi-sync-box-api/#Resource%20Table">Hue
 *      HDMI Sync Box API</a>
 */
@NonNullByDefault
public class HueSyncDeviceDetailedWifiInfo {
    /** Wifi SSID */
    public @Nullable String ssid;
    /**
     * 0 = not connected;
     * 1 = weak;
     * 2 = fair;
     * 3 = good;
     * 4 = excellent
     */
    public int strength;
}
