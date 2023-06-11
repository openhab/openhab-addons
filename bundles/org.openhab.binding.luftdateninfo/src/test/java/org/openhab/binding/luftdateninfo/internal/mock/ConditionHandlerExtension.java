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
package org.openhab.binding.luftdateninfo.internal.mock;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.luftdateninfo.internal.handler.ConditionHandler;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.State;

/**
 * The {@link NoiseHandlerExtension} Test Noise Handler Extension with additonal state queries
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class ConditionHandlerExtension extends ConditionHandler {

    public ConditionHandlerExtension(Thing thing) {
        super(thing);
    }

    public ConfigStatus getConfigStatus() {
        return configStatus;
    }

    public UpdateStatus getUpdateStatus() {
        return lastUpdateStatus;
    }

    public @Nullable State getTemperature() {
        return temperatureCache;
    }

    public @Nullable State getHumidity() {
        return humidityCache;
    }

    public @Nullable State getPressure() {
        return pressureCache;
    }

    public @Nullable State getPressureSea() {
        return pressureSeaCache;
    }
}
