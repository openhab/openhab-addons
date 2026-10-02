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
package org.openhab.binding.motionblinds.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link MotionBlindsConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Oleksandr Mishchuk - Initial contribution
 */
@NonNullByDefault
public class MotionBlindsConfiguration {

    /**
     * MAC address of the motor as reported by the device (12 hex digits, no separators).
     */
    public String macAddress = "";

    /**
     * 16 character key from the Connector / Motion Blinds app (including dashes).
     */
    public String key = "";

    /**
     * Status polling interval in seconds. Push reports and heartbeats keep the state up to date in between.
     */
    public int refreshInterval = 300;

    /**
     * Travel of the blind in percent that turns the slats from closed on one side to closed on the other side.
     */
    public double tiltTravel = 3;

    /**
     * Travel in percent that does not turn the slats when the blind starts to move up from fully lowered.
     */
    public double tiltSlack = 2;
}
