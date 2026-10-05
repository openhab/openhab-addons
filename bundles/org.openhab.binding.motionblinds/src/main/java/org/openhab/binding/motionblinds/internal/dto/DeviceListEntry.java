/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.motionblinds.internal.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link DeviceListEntry} is one element of the {@code data} array of a {@code GetDeviceListAck}.
 * A Wi-Fi motor lists itself as its only entry.
 *
 * @author Oleksandr Mishchuk - Initial contribution
 */
@NonNullByDefault
public class DeviceListEntry {
    public @Nullable String mac;
    public @Nullable String deviceType;
}
