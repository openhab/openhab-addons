/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tado.internal.builder;

import java.io.IOException;

import org.openhab.binding.tado.handler.TadoZoneHandler;
import org.openhab.binding.tado.internal.api.TadoClientException;
import org.openhab.binding.tado.internal.api.model.ZoneState;

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

    ZoneState getZoneState() throws IOException, TadoClientException {
        if (this.zoneState == null) {
            ZoneState retrievedZoneState = zoneHandler.getZoneState();
            // empty zone state behaves like a NULL object
            this.zoneState = retrievedZoneState != null ? retrievedZoneState : new ZoneState();
        }

        return this.zoneState;
    }
}
