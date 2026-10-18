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
package org.openhab.binding.venstarthermostat.internal.dto;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link VenstarRuntimeData} represents the list of runtimes returned from the REST API.
 *
 * @author Matthew Davies - Initial contribution
 */
@NonNullByDefault
public class VenstarRuntimeData {
    private @Nullable List<VenstarRuntime> runtimes;

    public VenstarRuntimeData() {
    }

    public VenstarRuntimeData(List<VenstarRuntime> runtimes) {
        this.runtimes = runtimes;
    }

    public @Nullable List<VenstarRuntime> getRuntimes() {
        return runtimes;
    }

    public void setRuntimes(List<VenstarRuntime> runtimes) {
        this.runtimes = runtimes;
    }
}
