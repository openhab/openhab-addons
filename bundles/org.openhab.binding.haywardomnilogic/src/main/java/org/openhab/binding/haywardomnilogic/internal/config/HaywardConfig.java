/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
 * The {@link HaywardConfig} class contains fields mapping thing configuration parameters.
 *
 * @author Matt Myers - Initial contribution
 */

@NonNullByDefault
public class HaywardConfig {
    public String endpointUrl = "";
    public String username = "";
    public String password = "";
    public int alarmPollTime = 60;
    public int telemetryPollTime = 10;
}
