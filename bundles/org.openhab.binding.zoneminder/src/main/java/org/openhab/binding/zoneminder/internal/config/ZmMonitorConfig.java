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
package org.openhab.binding.zoneminder.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link ZmMonitorConfig} class contains fields mapping thing configuration parameters.
 *
 * @author Mark Hilbush - Initial contribution
 */
@NonNullByDefault
public class ZmMonitorConfig {

    /**
     * Monitor Id
     */
    public @Nullable String monitorId;

    /**
     * Interval in seconds with which image is refreshed. If null, image
     * will not be refreshed.
     */
    public @Nullable Integer imageRefreshInterval;

    /**
     * Duration in seconds after which the alarm will be turned off
     */
    public @Nullable Integer alarmDuration;
}
