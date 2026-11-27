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
package org.openhab.binding.bmwcardata.internal.stream;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bmwcardata.internal.api.dto.TelematicValue;

/**
 * Receives what a {@link StreamSource} produces. Callbacks arrive on the threads of the MQTT client and must return
 * quickly.
 *
 * @author Mark Herwege - Initial contribution
 * @author Michael Weger - Adaptation to the BMW CarData binding
 */
@NonNullByDefault
public interface StreamListener {

    /**
     * New data of a vehicle.
     *
     * @param vin vehicle identification number
     * @param data values by technical descriptor
     */
    void onStreamData(String vin, Map<String, TelematicValue> data);

    /**
     * The stream state changed.
     *
     * @param state new state
     * @param detail human readable reason, e.g. the connection error
     */
    void onStreamStateChanged(StreamState state, @Nullable String detail);
}
