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
package org.openhab.binding.tellstick.internal.conf;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Configuration class for Tellstick bridge used to connect to the
 * Telldus local API.
 *
 * @author Jan Gustafsson - Initial contribution
 */
@NonNullByDefault
public class TelldusLocalConfiguration {
    public String ipAddress = "";
    public String accessToken = "";
    public long refreshInterval = 60000;
}
