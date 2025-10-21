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
package org.openhab.binding.unifiprotect.internal.api.dto;

/**
 * Floodlight device DTO.
 *
 * @author Dan Cunningham - Initial contribution
 */
public class Light extends Device {
    public LightModeSettings lightModeSettings;
    public LightDeviceSettings lightDeviceSettings;
    public Boolean isDark;
    public Boolean isLightOn;
    public Boolean isLightForceEnabled;
    public Long lastMotion; // epoch millis or null
    public Boolean isPirMotionDetected;
    public String camera; // nullable
}
