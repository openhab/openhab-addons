/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.sbus.handler.config;

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
     * The host address of the Sbus bridge
     */
    public String host = "localhost";

    /**
     * The port number for Sbus communication
     */
    public int port = Sbus.DEFAULT_PORT;
}
