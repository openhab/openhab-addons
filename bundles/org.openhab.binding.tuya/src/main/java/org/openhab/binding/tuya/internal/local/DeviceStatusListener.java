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
package org.openhab.binding.tuya.internal.local;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link DeviceStatusListener} encapsulates device status data
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public interface DeviceStatusListener {
    void processDeviceStatus(Map<Integer, Object> deviceStatus);

    void connectionStatus(boolean status);
}
