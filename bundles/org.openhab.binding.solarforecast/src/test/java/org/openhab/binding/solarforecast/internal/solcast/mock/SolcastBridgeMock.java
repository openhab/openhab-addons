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
package org.openhab.binding.solarforecast.internal.solcast.mock;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.solarforecast.internal.solcast.handler.SolcastBridgeHandler;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.binding.ThingHandlerCallback;

/**
 * The {@link SolcastBridgeMock} mocks bridge handler for solar.forecast
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class SolcastBridgeMock extends SolcastBridgeHandler {

    public SolcastBridgeMock(Bridge bridge) {
        super(bridge);
    }

    @Override
    public @Nullable ThingHandlerCallback getCallback() {
        return super.getCallback();
    }

    @Override
    public void updateTimeseries() {
        super.updateTimeseries();
    }

    @Override
    public void updateChannels() {
        super.updateChannels();
    }

    @Override
    public void updateConfiguration(Configuration config) {
        super.updateConfiguration(config);
    }
}
