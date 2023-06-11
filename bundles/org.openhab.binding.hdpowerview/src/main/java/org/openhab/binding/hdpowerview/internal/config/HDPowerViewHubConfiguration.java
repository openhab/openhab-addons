/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.hdpowerview.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Basic configuration for the HD PowerView hub
 *
 * @author Andy Lintner - Initial contribution
 */
@NonNullByDefault
public class HDPowerViewHubConfiguration {

    public static final String HOST = "host";

    public @Nullable String host;

    public long refresh;
    public long hardRefresh;
    public long hardRefreshBatteryLevel;
}
