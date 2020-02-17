/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

package org.openhab.binding.rootedtoon.internal.client;

import org.eclipse.jdt.annotation.NonNull;
import org.openhab.binding.rootedtoon.internal.client.model.PowerUsageInfo;
import org.openhab.binding.rootedtoon.internal.client.model.RealtimeUsageInfo;
import org.openhab.binding.rootedtoon.internal.client.model.ThermostatInfo;

/**
 *
 * @author daanmeijer - Initial Contribution
 *
 */
public interface ToonClientEventListener {
    void newPowerUsageInfo(@NonNull PowerUsageInfo paramPowerUsageInfo);

    void newThermostatInfo(@NonNull ThermostatInfo paramThermostatInfo);

    void newRealtimeUsageInfo(@NonNull RealtimeUsageInfo paramRealtimeUsageInfo);
}