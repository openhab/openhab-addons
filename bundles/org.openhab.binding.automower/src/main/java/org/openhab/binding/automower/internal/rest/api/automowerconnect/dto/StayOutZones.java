/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.automower.internal.rest.api.automowerconnect.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * @author MikeTheTux - Initial contribution
 */
public class StayOutZones {
    private Boolean dirty;
    private List<StayOutZone> zones = new ArrayList<>();

    public Boolean isDirty() {
        return dirty;
    }

    public void setDirty(Boolean dirty) {
        this.dirty = dirty;
    }

    public List<StayOutZone> getZones() {
        return zones;
    }

    public void setZones(List<StayOutZone> zones) {
        this.zones = zones;
    }
}
