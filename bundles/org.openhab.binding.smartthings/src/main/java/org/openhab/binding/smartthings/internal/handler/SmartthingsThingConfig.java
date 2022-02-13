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
package org.openhab.binding.smartthings.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Configuration data for Smartthings device
 *
 * @author Bob Raker - Initial contribution
 */
@NonNullByDefault
public class SmartthingsThingConfig {

    /**
     * The user assigned name used in the Smartthings hub (required)
     */
    public String smartthingsName = "";
    /**
     * The device location (optional)
     */
    public String smartthingsLocation = "";
    /**
     * Timeout (defaults to 3 seconds)
     */
    public int smartthingsTimeout = 3;
}
