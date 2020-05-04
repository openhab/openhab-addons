/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.neohub.internal;

import static org.openhab.binding.neohub.internal.NeoHubBindingConstants.*;

import javax.measure.Unit;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Thing;

/**
 * The {@link NeoContactHandler} is the OpenHAB Handler for NeoContact devices
 * 
 * Note: inherits almost all the functionality of a {@link NeoBaseHandler}
 * 
 * @author Andrew Fiddian-Green - Initial contribution
 * 
 */
public class NeoContactHandler extends NeoBaseHandler {

    public NeoContactHandler(Thing thing) {
        super(thing);
    }

    // =========== methods of NeoBaseHandler that are overridden ================

    @Override
    protected void toOpenHabSendChannelValues(NeoHubInfoResponse.DeviceInfo deviceInfo, Unit<?> temperatureUnit) {
        toOpenHabSendValueDebounced(CHAN_CONTACT_STATE, OnOffType.from(deviceInfo.isCoolInputOn()));

        toOpenHabSendValueDebounced(CHAN_BATTERY_LOW_ALARM, OnOffType.from(deviceInfo.isBatteryLow()));
    }
}
