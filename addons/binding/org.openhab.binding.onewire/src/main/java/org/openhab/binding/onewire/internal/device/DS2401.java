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
package org.openhab.binding.onewire.internal.device;

import static org.openhab.binding.onewire.internal.OwBindingConstants.*;

import java.util.Collections;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.onewire.internal.OwException;
import org.openhab.binding.onewire.internal.SensorId;
import org.openhab.binding.onewire.internal.handler.OwBaseBridgeHandler;
import org.openhab.binding.onewire.internal.handler.OwBaseThingHandler;

/**
 * The {@link DS2401} class defines an DS2401 (iButton) device
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class DS2401 extends AbstractOwDevice {
    public static final Set<OwChannelConfig> CHANNELS = Collections
            .singleton(new OwChannelConfig(CHANNEL_PRESENT, CHANNEL_TYPE_UID_PRESENT));

    public DS2401(SensorId sensorId, OwBaseThingHandler callback) {
        super(sensorId, callback);
        isConfigured = true;
    }

    @Override
    public void configureChannels() throws OwException {
    }

    @Override
    public void refresh(OwBaseBridgeHandler bridgeHandler, Boolean forcedRefresh) throws OwException {
    }
}
