/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.unifi.internal.api.dto;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.Expose;

/**
 * The {@link UniFiPortOverrides} represents the data model of UniFi port overrides.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
public class UniFiPortOverrides {

    @Expose
    private final List<UnfiPortOverride> portOverrides = new ArrayList<>();

    public void addPortOverride(final UnfiPortOverride unfiPortOverride) {
        portOverrides.add(unfiPortOverride);
    }

    public void addPortOverride(final int portIdx, final String portconfId, final String poeMode) {
        portOverrides.add(new UnfiPortOverride(portIdx, portconfId, poeMode));
    }

    @Override
    public String toString() {
        return String.format("UniFiPortOverrides: {}", String.join(", ", portOverrides.toArray(new String[0])));
    }
}
