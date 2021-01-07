/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.cul.internal.config;

;

/**
 * Configuration class for {@link MaxCulBindingConstants} bridge used to connect to the
 * cul device.
 *
 * @author Johannes Goehr (johgoe) - Initial contribution
 */
public class MaxCUNBridgeConfiguration {

    /**
     * Name of the CUN device
     */
    public String networkPath;

    /**
     * Set timezone you want the units to be set to.
     */
    public String timezone;
}
