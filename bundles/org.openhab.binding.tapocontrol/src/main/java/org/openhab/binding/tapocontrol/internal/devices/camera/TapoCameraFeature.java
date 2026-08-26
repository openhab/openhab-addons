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
package org.openhab.binding.tapocontrol.internal.devices.camera;

/**
 * Camera features detected at runtime. All features are assumed present until a read for
 * one of them fails with a method-specific error; failed features are dropped for the
 * current session and may come back after a reconnect.
 *
 * @author Kai Kreuzer - Initial contribution
 */
public enum TapoCameraFeature {
    ALARM,
    PRIVACY,
    MOTION_DETECTION,
    PRESETS,
    LED
}
