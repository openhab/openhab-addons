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
package org.openhab.binding.freeboxos.internal.handler;

import static org.openhab.binding.freeboxos.internal.FreeboxOsBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeboxos.internal.api.rest.HomeManager;
import org.openhab.binding.freeboxos.internal.api.rest.HomeManager.EndpointState;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * The {@link AlarmHandler} is responsible for handling everything associated to
 * any Freebox Home Alarm thing type.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class AlarmHandler extends HomeNodeHandler {

    public AlarmHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected State getChannelState(HomeManager homeManager, String channelId, EndpointState state) {
        String value = state.value();

        if (value == null) {
            return UnDefType.NULL;
        }

        return switch (channelId) {
            case NODE_BATTERY -> DecimalType.valueOf(value);
            case ALARM_PIN -> StringType.valueOf(value);
            case ALARM_SOUND, ALARM_VOLUME -> QuantityType.valueOf(value + " %");
            case ALARM_TIMEOUT1, ALARM_TIMEOUT2, ALARM_TIMEOUT3 -> QuantityType.valueOf(value + " s");
            default -> UnDefType.NULL;
        };
    }
}
