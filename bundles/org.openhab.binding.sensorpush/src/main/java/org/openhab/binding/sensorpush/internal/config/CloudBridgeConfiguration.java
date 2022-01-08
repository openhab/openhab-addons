/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.sensorpush.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link CloudBridgeConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Bob Adair - Initial contribution
 */
@NonNullByDefault
public class CloudBridgeConfiguration {
    /** Cloud service user */
    public String user = "";
    /** Cloud service password */
    public String password = "";
    /** Polling interval in minutes */
    public int poll = 5;
    /** Request timeout in seconds */
    public int timeout = 30;
}
