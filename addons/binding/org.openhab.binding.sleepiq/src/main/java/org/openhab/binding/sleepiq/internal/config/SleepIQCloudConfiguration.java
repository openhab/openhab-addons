/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sleepiq.internal.config;

/**
 * Configuration class for the SleepIQ cloud.
 *
 * @author Gregory Moyer - Initial contribution
 */
public class SleepIQCloudConfiguration {
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String POLLING_INTERVAL = "pollingInterval";

    public String username;
    public String password;
    public int pollingInterval;
}
