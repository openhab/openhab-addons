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

import java.util.Date;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * HDMI Sync Box Device Information DTO - Extended information (only available
 * to registered clients)
 * 
 * @author Patrik Gfeller - Initial Contribution
 * 
 * @see <a href=
 *      "https://developers.meethue.com/develop/hue-entertainment/hue-hdmi-sync-box-api/#Resource%20Table">Hue
 *      HDMI Sync Box API</a>
 */
@NonNullByDefault
public class HueSyncDetailedDeviceInfo extends HueSyncDeviceInfo {
    public @Nullable HueSyncDetailedDeviceInfoWifi wifi;
    public @Nullable HueSyncDetailedDeviceInfoUpdate update;

    /** UTC time when last check for update was performed. */
    public @Nullable Date lastCheckedUpdate;
    /**
     * Build number that is available to update to. Item is set to null when there
     * is no update available.
     */
    public int updatableBuildNumber;
    /**
     * User readable version of the firmware the device can upgrade to. Item is set
     * to null when there is no update available.
     */
    public int updatableFirmwareVersion;
    /**
     * 1 = regular;
     * 0 = off in powersave, passthrough or sync mode;
     * 2 = dimmed in powersave or passthrough mode and off in sync mode
     */
    public int ledMode;

    /** none, doSoftwareRestart, doFirmwareUpdate */
    public @Nullable String action;
    public @Nullable String pushlink;
}
