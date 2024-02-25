/**
 * Copyright (c) 2024-2024 Contributors to the openHAB project
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
package org.openhab.binding.huesync.internal.api.dto;

/**
 * HDMI Sync Box Device Information DTO
 * 
 * @author Patrik Gfeller - Initial Contribution
 * 
 * @see <a href=
 *      "https://developers.meethue.com/develop/hue-entertainment/hue-hdmi-sync-box-api/#Resource%20Table">Hue
 *      HDMI Sync Box API</a>
 */
public class HueSyncDeviceInfo {
    /** Friendly name of the device */
    public String name;
    /** Device Type identifier */
    public String deviceType;
    /**
     * Capitalized hex string of the 6 byte / 12 characters device id without
     * delimiters. Used as unique id on label, certificate common name, hostname
     * etc.
     */
    public String uniqueId;
    /**
     * Increased between firmware versions when api changes. Only apiLevel >= 7 is
     * supported.
     */
    public int apiLevel;
    /**
     * User readable version of the device firmware, starting with decimal major
     * .minor .maintenance format e.g. “1.12.3”
     */
    public String firmwareVersion;
    /**
     * Build number of the firmware. Unique for every build with newer builds
     * guaranteed a higher number than older.
     */
    public int buildNumber;
    public boolean termsAgreed;
    /** uninitialized, disconnected, lan, wan */
    public String wifiState;
    public String ipAddress;
    public HueSyncDeviceInfoCapabilities capabilities;
    public boolean beta;
    public boolean overheating;
    public boolean bluetooth;
}
