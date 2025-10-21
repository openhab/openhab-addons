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
 * Camera device DTO.
 *
 * Matches the camera schema.
 *
 * @author Dan Cunningham - Initial contribution
 */
public class Camera extends Device {
    public Boolean isMicEnabled;
    public OsdSettings osdSettings;
    public LedSettings ledSettings;
    public LcdMessage lcdMessage;
    public Integer micVolume; // 0-100
    public Integer activePatrolSlot; // nullable
    public VideoMode videoMode;
    public HdrType hdrType;
    public CameraFeatureFlags featureFlags;
    public SmartDetectSettings smartDetectSettings;
}
