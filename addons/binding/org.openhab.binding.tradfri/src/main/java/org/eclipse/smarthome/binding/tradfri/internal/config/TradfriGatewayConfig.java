/**
 * Copyright (c) 2014,2019 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.binding.tradfri.internal.config;

/**
 * Configuration class for the gateway.
 *
 * @author Kai Kreuzer - Initial contribution
 */
public class TradfriGatewayConfig {

    public String host;
    public int port = 5684; // default port
    public String code;
    public String identity;
    public String preSharedKey;
}
