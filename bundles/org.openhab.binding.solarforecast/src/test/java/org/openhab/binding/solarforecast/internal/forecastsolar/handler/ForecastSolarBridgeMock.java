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
package org.openhab.binding.solarforecast.internal.forecastsolar.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.PointType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.binding.ThingHandlerCallback;

/**
 * The {@link ForecastSolarBridgeMock} mocks bridge handler for solar.forecast
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class ForecastSolarBridgeMock extends ForecastSolarBridgeHandler {

    public ForecastSolarBridgeMock(Bridge bridge, PointType location) {
        super(bridge, location);
    }

    @Override
    public @Nullable ThingHandlerCallback getCallback() {
        return super.getCallback();
    }

    @Override
    public void forecastUpdate() {
        super.forecastUpdate();
    }

    @Override
    public void updateConfiguration(Configuration config) {
        super.updateConfiguration(config);
    }
}
