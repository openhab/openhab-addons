/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.onewire.internal.device;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.onewire.internal.OwException;
import org.openhab.binding.onewire.internal.SensorId;
import org.openhab.binding.onewire.internal.handler.OwBaseThingHandler;
import org.openhab.binding.onewire.internal.handler.OwserverBridgeHandler;

/**
 * The {@link DS2401} class defines a DS2401 (iButton) device
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class DS2401 extends AbstractOwDevice {
    public DS2401(SensorId sensorId, OwBaseThingHandler callback) {
        super(sensorId, callback);
        isConfigured = true;
    }

    @Override
    public void configureChannels() throws OwException {
    }

    @Override
    public void refresh(OwserverBridgeHandler bridgeHandler, Boolean forcedRefresh) throws OwException {
    }
}
