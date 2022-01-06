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
package org.openhab.binding.hpprinter.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link HPPrinterConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Stewart Cossey - Initial contribution
 */
@NonNullByDefault
public class HPPrinterConfiguration {
    public static final String IP_ADDRESS = "ipAddress";
    public static final String USAGE_INTERVAL = "usageInterval";
    public static final String STATUS_INTERVAL = "statusInterval";
    public static final String UUID = "uuid";

    public @Nullable String ipAddress;
    public int usageInterval;
    public int statusInterval;
    public @Nullable String uuid;
}
