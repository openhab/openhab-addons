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
    public @Nullable String macAddress;
    public @Nullable String heaterId;

    @Override
    public String toString() {
        return "MillheatHeaterConfiguration [macAddress=" + macAddress + ", heaterId=" + heaterId + "]";
    }
}
