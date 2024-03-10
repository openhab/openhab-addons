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
package org.openhab.binding.boschshc.internal.devices.universalswitch;

import static org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants.CHANNEL_KEY_CODE;
import static org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants.CHANNEL_KEY_EVENT_TIMESTAMP;
import static org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants.CHANNEL_KEY_EVENT_TYPE;
import static org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants.CHANNEL_KEY_NAME;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.boschshc.internal.devices.AbstractBatteryPoweredDeviceHandler;
import org.openhab.binding.boschshc.internal.exceptions.BoschSHCException;
import org.openhab.binding.boschshc.internal.services.keypad.KeypadService;
import org.openhab.binding.boschshc.internal.services.keypad.dto.KeypadServiceState;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Thing;

/**
 * Handler for a universally configurable switch with two buttons.
 * 
 * @author David Pace - Initial contribution
 *
 */
@NonNullByDefault
public class UniversalSwitchHandler extends AbstractBatteryPoweredDeviceHandler {

    private TimeZoneProvider timeZoneProvider;

    public UniversalSwitchHandler(Thing thing, TimeZoneProvider timeZoneProvider) {
        super(thing);
        this.timeZoneProvider = timeZoneProvider;
    }

    @Override
    protected void initializeServices() throws BoschSHCException {
        super.initializeServices();

        createService(KeypadService::new, this::updateChannels,
                List.of(CHANNEL_KEY_CODE, CHANNEL_KEY_NAME, CHANNEL_KEY_EVENT_TYPE, CHANNEL_KEY_EVENT_TIMESTAMP));
    }

    private void updateChannels(KeypadServiceState keypadServiceState) {
        updateState(CHANNEL_KEY_CODE, new DecimalType(keypadServiceState.keyCode));
        updateState(CHANNEL_KEY_NAME, new StringType(keypadServiceState.keyName.toString()));
        updateState(CHANNEL_KEY_EVENT_TYPE, new StringType(keypadServiceState.eventType.toString()));

        Instant instant = Instant.ofEpochMilli(keypadServiceState.eventTimestamp);
        updateState(CHANNEL_KEY_EVENT_TIMESTAMP,
                new DateTimeType(ZonedDateTime.ofInstant(instant, timeZoneProvider.getTimeZone())));
    }
}
