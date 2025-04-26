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
package org.openhab.binding.tuya.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link DeviceConfiguration} holds the configuration of a single device
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class DeviceConfiguration {
    public String productId = "";
    public String deviceId = "";
    public String localKey = "";

    public String ip = "";
    public String protocol = "";

    public int pollingInterval = 0;
}
