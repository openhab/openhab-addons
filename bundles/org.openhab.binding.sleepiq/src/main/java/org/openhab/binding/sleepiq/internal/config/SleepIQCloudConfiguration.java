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
package org.openhab.binding.sleepiq.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Configuration class for the SleepIQ cloud.
 *
 * @author Gregory Moyer - Initial contribution
 */
@NonNullByDefault
public class SleepIQCloudConfiguration {
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String POLLING_INTERVAL = "pollingInterval";

    public String username = "";
    public String password = "";
    public int pollingInterval;
}
