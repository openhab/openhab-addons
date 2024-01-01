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
package org.openhab.binding.ambientweather.internal.config;

/**
 * The {@link BridgeConfig} is responsible for storing the
 * Ambient Weather bridge thing configuration.
 *
 * @author Mark Hilbush - Initial contribution
 */
public class BridgeConfig {
    /**
     * API key
     */
    public String apiKey;

    /**
     * Application key
     */
    public String applicationKey;
}
