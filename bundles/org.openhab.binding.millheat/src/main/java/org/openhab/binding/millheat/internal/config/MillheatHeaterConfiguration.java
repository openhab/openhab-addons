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
package org.openhab.binding.millheat.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link MillheatHeaterConfiguration} class contains heater thing configuration parameters.
 *
 * @author Arne Seime - Initial contribution
 * @author Petter L. H. Eide - Device identifiers are cloud API UUIDs
 */
@NonNullByDefault
public class MillheatHeaterConfiguration {
    /**
     * Wi-Fi mac address. Unchanged by the move to the cloud API, so it is the most stable way to
     * identify a heater. Either this or {@link #heaterId} must be given.
     */
    public @Nullable String macAddress;
    /**
     * Device UUID as issued by the cloud API. Numeric identifiers from the old service are not
     * valid here. Either this or {@link #macAddress} must be given.
     */
    public @Nullable String heaterId;
    /**
     * Nominal heater panel power. Retained only for configurations written before the cloud API
     * started reporting measured power, which it now does.
     *
     * @deprecated the {@code currentEnergy} channel no longer needs it.
     */
    @Deprecated
    public @Nullable Integer power;

    @Override
    public String toString() {
        return "MillheatHeaterConfiguration [macAddress=" + macAddress + ", heaterId=" + heaterId + ", power=" + power
                + "]";
    }
}
