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
package org.openhab.binding.linktap.configuration;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link LinkTapDeviceConfiguration} class contains fields mapping the configuration parameters for a LinkTap
 * device's configuration.
 *
 * @author David Goodyear - Initial contribution
 */
@NonNullByDefault
public class LinkTapDeviceConfiguration {

    /**
     * The clear text device name as reported by the API.
     */
    public String name = "";

    /**
     * The device id as stored by the gateway to address the device.
     */
    public String id = "";

    /**
     * If enabled the device, will enable all alerts during device initialization.
     */
    public boolean enableAlerts = true;
}
