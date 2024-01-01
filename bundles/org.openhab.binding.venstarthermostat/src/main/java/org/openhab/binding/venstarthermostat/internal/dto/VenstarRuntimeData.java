/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

/**
 * The {@link VenstarRuntimeData} represents the list of runtimes returned from the REST API.
 *
 * @author Matthew Davies - Initial contribution
 */
@NonNullByDefault
public class VenstarRuntimeData {
    private List<VenstarRuntime> runtimes;

    public VenstarRuntimeData() {
        super();
        runtimes = List.of();
    }

    public VenstarRuntimeData(List<VenstarRuntime> runtimes) {
        super();
        this.runtimes = runtimes;
    }

    public List<VenstarRuntime> getRuntimes() {
        return runtimes;
    }

    public void setRuntimes(List<VenstarRuntime> runtimes) {
        this.runtimes = runtimes;
    }
}
