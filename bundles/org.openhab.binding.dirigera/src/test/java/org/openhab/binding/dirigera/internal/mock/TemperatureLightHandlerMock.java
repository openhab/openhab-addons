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
package org.openhab.binding.dirigera.internal.mock;

import static org.mockito.Mockito.mock;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.dirigera.internal.DirigeraStateDescriptionProvider;
import org.openhab.binding.dirigera.internal.handler.light.TemperatureLightHandler;
import org.openhab.core.thing.Thing;

/**
 * {@link TemperatureLightHandlerMock} mock for accessing protected methods
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class TemperatureLightHandlerMock extends TemperatureLightHandler {
    public TemperatureLightHandlerMock() {
        super(mock(Thing.class), Map.of(), mock(DirigeraStateDescriptionProvider.class));
    }

    @Override
    public long getKelvin(int percent) {
        return super.getKelvin(percent);
    }

    @Override
    public int getPercent(long kelvin) {
        return super.getPercent(kelvin);
    }
}
