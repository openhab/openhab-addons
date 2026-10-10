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
package org.openhab.binding.melcloud.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.melcloud.internal.logging.SensitiveDataMasker;

/**
 * Config class for a Heatpump device.
 *
 * @author Wietse van Buitenen - Initial Contribution
 *
 */
@NonNullByDefault
public class HeatpumpDeviceConfig {
    public Integer deviceID = 0;
    public @Nullable Integer buildingID;
    public Integer pollingInterval = 360;

    @Override
    public String toString() {
        Integer building = buildingID;
        return "[deviceID=" + SensitiveDataMasker.maskId(deviceID.toString()) + ", buildingID="
                + (building == null ? "null" : SensitiveDataMasker.maskId(building.toString())) + ", pollingInterval="
                + pollingInterval + "]";
    }
}
