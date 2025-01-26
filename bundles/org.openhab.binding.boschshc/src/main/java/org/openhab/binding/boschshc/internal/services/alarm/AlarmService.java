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
package org.openhab.binding.boschshc.internal.services.alarm;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.boschshc.internal.exceptions.BoschSHCException;
import org.openhab.binding.boschshc.internal.services.BoschSHCService;
import org.openhab.binding.boschshc.internal.services.alarm.dto.AlarmServiceState;
import org.openhab.binding.boschshc.internal.services.alarm.dto.AlarmState;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.Command;

/**
 * Alarm service for smoke detectors.
 * 
 * @author David Pace - Initial contribution
 *
 */
@NonNullByDefault
public class AlarmService extends BoschSHCService<AlarmServiceState> {
    public AlarmService() {
        super("Alarm", AlarmServiceState.class);
    }

    @Override
    public AlarmServiceState handleCommand(Command command) throws BoschSHCException {
        if (command instanceof StringType stringCommand) {
            AlarmServiceState state = new AlarmServiceState();
            state.value = AlarmState.from(stringCommand.toFullString());
            return state;
        }
        return super.handleCommand(command);
    }
}
