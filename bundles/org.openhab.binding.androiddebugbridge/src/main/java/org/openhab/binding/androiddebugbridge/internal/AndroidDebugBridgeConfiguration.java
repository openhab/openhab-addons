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
package org.openhab.binding.androiddebugbridge.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link AndroidDebugBridgeConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Miguel Álvarez - Initial contribution
 */
@NonNullByDefault
public class AndroidDebugBridgeConfiguration {
    /**
     * The IP address to use for connecting to the Android device.
     */
    public String ip = "";
    /**
     * Sample configuration parameter. Replace with your own.
     */
    public int port;
    /**
     * Time for scheduled state check.
     */
    public int refreshTime = 30;
    /**
     * Command timeout seconds.
     */
    public int timeout = 5;
    /**
     * Record input duration in seconds.
     */
    public int recordDuration = 5;
    /**
     * Percent to increase/decrease volume.
     */
    public int volumeStepPercent = 15;
    /**
     * Assumed max volume for devices with android versions that do not expose this value (>=android 11).
     */
    public int deviceMaxVolume = 25;
    /**
     * Max ADB command consecutive timeouts to force to reset the connection. (0 for disabled)
     */
    public int maxADBTimeouts;
    /**
     * Settings key for android versions where volume is gather using settings command (>=android 11).
     */
    public String volumeSettingKey = "volume_music_hdmi";
    /**
     * Configure media state detection behavior by package
     */
    public @Nullable String mediaStateJSONConfig;
}
