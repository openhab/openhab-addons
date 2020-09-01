/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.haywardomnilogic.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link HaywardOmniLogixConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Matt Myers - Initial Contribution
 */

@NonNullByDefault
public class HaywardConfig {
    public String hostname = "";
    public String username = "";
    public String password = "";
    public int alarmPollTime = 60;
    public int telemetryPollTime = 10;
    public int commandPollDelay = 10;
    public String token = "";
    public String mspSystemID = "";
    public String userID = "";
    public String backyardName = "";
    public String address = "";
    public String firstName = "";
    public String lastName = "";
    public String roleType = "";
}
