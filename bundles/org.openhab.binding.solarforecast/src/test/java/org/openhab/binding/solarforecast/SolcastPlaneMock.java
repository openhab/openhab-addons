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
package org.openhab.binding.solarforecast;

import static org.mockito.Mockito.mock;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.solarforecast.internal.SolarForecastBindingConstants;
import org.openhab.binding.solarforecast.internal.solcast.SolcastObject;
import org.openhab.binding.solarforecast.internal.solcast.handler.SolcastPlaneHandler;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.internal.ThingImpl;

/**
 * The {@link SolcastPlaneMock} mocks Plane Handler for solcast
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class SolcastPlaneMock extends SolcastPlaneHandler {

    public SolcastPlaneMock(SolcastObject sco) {
        super(new ThingImpl(SolarForecastBindingConstants.SOLCAST_PLANE, new ThingUID("test", "plane")),
                mock(HttpClient.class));
        super.setCallback(new CallbackMock());
        super.setForecast(sco);
    }
}
