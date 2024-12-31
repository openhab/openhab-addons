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

/**
 * HDMI Sync Box Device Information - Automatic Firmware update
 * 
 * @author Patrik Gfeller - Initial Contribution
 * 
 * @see <a href=
 *      "https://developers.meethue.com/develop/hue-entertainment/hue-hdmi-sync-box-api/#Resource%20Table">Hue
 *      HDMI Sync Box API</a>
 */
@NonNullByDefault
public class HueSyncDeviceDetailedUpdateInfo {
    /**
     * Sync Box checks daily for a firmware update. If true, an available update
     * will automatically be installed. This will be postponed if Sync Box is
     * passing through content to the TV and being used.
     */
    public boolean autoUpdateEnabled;
    /**
     * TC hour when the automatic update will check and execute, values 0 – 23.
     * Default is 10. Ideally this value should be set to 3AM according to user’s
     * timezone.
     */
    public int autoUpdateTime;
}
