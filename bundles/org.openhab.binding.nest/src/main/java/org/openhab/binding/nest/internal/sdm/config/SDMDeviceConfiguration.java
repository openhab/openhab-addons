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
package org.openhab.binding.nest.internal.sdm.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link SDMDeviceConfiguration} contains the configuration parameter values for a SDM device.
 *
 * @author Brian Higginbotham - Initial contribution
 * @author Wouter Born - Initial contribution
 */
@NonNullByDefault
public class SDMDeviceConfiguration {

    public static final String DEVICE_ID = "deviceId";
    public String deviceId = "";

    public static final String REFRESH_INTERVAL = "refreshInterval";
    public int refreshInterval = 300;
}
