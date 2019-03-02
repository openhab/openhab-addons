/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.smartthings.config;

/**
 * Configuration data for Smartthings device
 *
 * @author Bob Raker - Initial contribution
 *
 */
public class SmartthingsThingConfig {

    /**
     * The user assigned name used in the Smartthings hub (required)
     */
    public String smartthingsName = null;
    /**
     * The device location (optional)
     */
    public String smartthingsLocation = null;
}
