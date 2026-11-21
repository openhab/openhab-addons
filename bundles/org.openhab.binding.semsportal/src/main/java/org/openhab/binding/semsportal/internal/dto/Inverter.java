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
package org.openhab.binding.semsportal.internal.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * An inverter of a power station, as found in the {@link InverterStatusResponse}
 *
 * @author Julio Vilmar Gesser - Initial contribution
 */
@NonNullByDefault
public class Inverter {
    private final String serialNumber;
    private final @Nullable String model;
    private final int status;

    public Inverter(String serialNumber, @Nullable String model, int status) {
        this.serialNumber = serialNumber;
        this.model = model;
        this.status = status;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public @Nullable String getModel() {
        return model;
    }

    /**
     * @return the status code of the inverter: 1 or 5 if it is producing, -1 if it is offline
     */
    public int getStatus() {
        return status;
    }
}
