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
package org.openhab.binding.flume.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * {@link FlumeCloudConnectorConfig} implements the http-based REST API to access the Flume Cloud
 *
 * @author Jeff James - Initial contribution
 */
@NonNullByDefault
public class FlumeCloudConnectorConfig {
    public String username = "";
    public String password = "";
    public String clientId = "";
    public String clientSecret = "";
    public int pollingInterval;
}
