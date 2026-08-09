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
package org.openhab.binding.atv.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Thing configuration for an Apple TV or AirPlay speaker.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class AtvConfiguration {

    /** Stable device identifier (MAC); the Thing's representation property. */
    public String macAddress = "";
    /** IP address; populated and refreshed by discovery, or set manually. */
    public String host = "";
    /** Friendly name presented to the device while pairing. */
    public String name = "";
    /**
     * PIN entered during pairing. A single field reused for each pairing step in turn (an Apple TV pairs
     * AirPlay then Companion, each with its own PIN); the binding routes it to the step it is waiting on.
     * Temporary - cleared after each step and once fully paired.
     */
    public String pin = "";
    public String airplayCredentials = "";
    public String companionCredentials = "";
    public String raopCredentials = "";
    /** Password for password-protected AirPlay speakers. */
    public String password = "";
    /** Default volume (0-100) for audio played to this device as an openHAB audio sink. */
    public int notificationVolume = 50;
    /**
     * Seconds between active power-state health checks; each rebuilds the connection if the device does
     * not answer or its reported state disagrees with the tracked state. 0 disables the health check.
     */
    public int healthCheckInterval = 30;
    /**
     * Backup: minutes of no updates of any kind after which the connection is transparently rebuilt;
     * 0 disables it. Recommended 15 or higher.
     */
    public int staleTimeout = 0;
}
