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
package org.openhab.binding.unifiprotect.internal.api.priv.dto.devices;

import java.util.List;

import org.openhab.binding.unifiprotect.internal.api.priv.dto.base.UniFiProtectAdoptableDevice;

/**
 * Chime device model for UniFi Protect
 *
 * @author Dan Cunningham - Initial contribution
 */
public class Chime extends UniFiProtectAdoptableDevice {

    public Integer volume;
    public String apMac;
    public Integer apRssi;
    public String elementInfo;
    public List<String> cameraIds;
    public Integer repeatTimes;
    public RingtoneSettings ringtoneSettings;

    public static class RingtoneSettings {
        public String defaultRingtone;
        public List<String> presets;
    }
}
