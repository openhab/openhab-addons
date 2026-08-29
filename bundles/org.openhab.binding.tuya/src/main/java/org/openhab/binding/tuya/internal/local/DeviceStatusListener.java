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
package org.openhab.binding.tuya.internal.local;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link DeviceStatusListener} encapsulates device status data
 *
 * @author Jan N. Klug - Initial contribution
 * @author Maciej Jarzebowski - Report the sub-device a status belongs to
 */
@NonNullByDefault
public interface DeviceStatusListener {
    /**
     * Called when a status message was received.
     *
     * @param cid the node id of the sub-device the status belongs to, or {@code null} if it belongs to the device the
     *            connection is established with
     * @param deviceStatus the reported data points
     */
    void processDeviceStatus(@Nullable String cid, Map<Integer, Object> deviceStatus);

    void connectionStatus(boolean status, int initialDelay);
}
