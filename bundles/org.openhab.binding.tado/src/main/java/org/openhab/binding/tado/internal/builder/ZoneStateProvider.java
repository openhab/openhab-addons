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
package org.openhab.binding.tado.internal.builder;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.tado.internal.api.ApiException;
import org.openhab.binding.tado.internal.api.model.ZoneState;
import org.openhab.binding.tado.internal.handler.TadoZoneHandler;

/**
 * Wrapper for zone state to support lazy loading.
 *
 * @author Dennis Frommknecht - Initial contribution
 */
@NonNullByDefault
public class ZoneStateProvider {
    private final TadoZoneHandler zoneHandler;
    private @Nullable ZoneState zoneState;

    public ZoneStateProvider(TadoZoneHandler zoneHandler) {
        this.zoneHandler = zoneHandler;
    }

    public synchronized ZoneState getZoneState() throws IOException, ApiException {
        ZoneState zoneState = this.zoneState;
        if (zoneState == null) {
            zoneState = this.zoneState = zoneHandler.getZoneState();
        }
        return zoneState;
    }
}
