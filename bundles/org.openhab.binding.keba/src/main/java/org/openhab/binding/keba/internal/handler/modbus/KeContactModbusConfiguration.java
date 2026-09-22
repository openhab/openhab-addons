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
package org.openhab.binding.keba.internal.handler.modbus;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link KeContactModbusConfiguration} class contains fields mapping thing configuration parameters of the
 * {@code kecontact-modbus} Thing type.
 *
 * @author Karel Goderis - Initial contribution
 */
@NonNullByDefault
public class KeContactModbusConfiguration {

    public @Nullable String ipAddress;
    public int port = 502;
    public int unitId = 255;
    public int refreshInterval = 12;
    public int refreshIntervalSlow = 60;
}
