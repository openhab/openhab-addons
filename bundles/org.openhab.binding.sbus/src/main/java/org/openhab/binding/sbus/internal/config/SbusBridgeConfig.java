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
package org.openhab.binding.sbus.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

import ro.ciprianpascu.sbus.Sbus;

/**
 * The {@link SbusBridgeConfig} class contains fields mapping bridge configuration parameters.
 *
 * @author Ciprian Pascu - Initial contribution
 */
@NonNullByDefault
public class SbusBridgeConfig {
    /**
     * The host address of the SBUS bridge
     */
    public String host = "";

    /**
     * The port number for SBUS communication
     */
    public int port = Sbus.DEFAULT_PORT;
}
