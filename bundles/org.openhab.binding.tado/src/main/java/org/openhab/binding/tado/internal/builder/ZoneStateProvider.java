/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.tado.internal.builder;

import java.io.IOException;

import org.openhab.binding.tado.internal.api.ApiException;
import org.openhab.binding.tado.internal.api.model.ZoneState;
import org.openhab.binding.tado.internal.handler.TadoZoneHandler;

/**
 * Wrapper for zone state to support lazy loading.
 *
 * @author Dennis Frommknecht - Initial contribution
 */
public class ZoneStateProvider {
    private TadoZoneHandler zoneHandler;
    private ZoneState zoneState;

    public ZoneStateProvider(TadoZoneHandler zoneHandler) {
        this.zoneHandler = zoneHandler;
    }

    ZoneState getZoneState() throws IOException, ApiException {
        if (this.zoneState == null) {
            ZoneState retrievedZoneState = zoneHandler.getZoneState();
            // empty zone state behaves like a NULL object
            this.zoneState = retrievedZoneState != null ? retrievedZoneState : new ZoneState();
        }

        return this.zoneState;
    }
}
