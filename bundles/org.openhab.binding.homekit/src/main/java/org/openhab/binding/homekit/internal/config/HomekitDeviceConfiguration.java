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
package org.openhab.binding.homekit.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link HomekitBridgeConfiguration} contains fields mapping device configuration parameters.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class HomekitDeviceConfiguration {

    public @Nullable String ipV4Address; // dotted ipV4 address of the device
    public @Nullable String protocolVersion; // e.g. "1.0" HAP protocol version
    public @Nullable Integer deviceCategory; // e.g. 2 the HomeKit device category
    public @Nullable String pairingCode; // e.g. "031-45-154" the device pairing code
}
