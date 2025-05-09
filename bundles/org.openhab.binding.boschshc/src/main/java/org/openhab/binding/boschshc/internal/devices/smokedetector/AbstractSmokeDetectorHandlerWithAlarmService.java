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
package org.openhab.binding.boschshc.internal.devices.smokedetector;

import static org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants.CHANNEL_ALARM;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.boschshc.internal.devices.AbstractSmokeDetectorHandler;
import org.openhab.binding.boschshc.internal.exceptions.BoschSHCException;
import org.openhab.binding.boschshc.internal.services.alarm.AlarmService;
import org.openhab.binding.boschshc.internal.services.alarm.dto.AlarmServiceState;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;

/**
 * Abstract handler for smoke detectors that have an <code>Alarm</code> service.
 * <p>
 * Note that this includes Smoke Detector and Smoke Detector II, but not Twinguard.
 * 
 * @author David Pace - Initial contribution
 *
 */
@NonNullByDefault
public abstract class AbstractSmokeDetectorHandlerWithAlarmService extends AbstractSmokeDetectorHandler {

    private AlarmService alarmService;

    protected AbstractSmokeDetectorHandlerWithAlarmService(Thing thing) {
        super(thing);
        this.alarmService = new AlarmService();
    }

    @Override
    protected void initializeServices() throws BoschSHCException {
        super.initializeServices();

        this.registerService(alarmService, this::updateChannels, List.of(CHANNEL_ALARM));
    }

    private void updateChannels(AlarmServiceState state) {
        updateState(CHANNEL_ALARM, new StringType(state.value.toString()));
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        super.handleCommand(channelUID, command);

        if (CHANNEL_ALARM.equals(channelUID.getId())) {
            this.handleServiceCommand(this.alarmService, command);
        }
    }
}
