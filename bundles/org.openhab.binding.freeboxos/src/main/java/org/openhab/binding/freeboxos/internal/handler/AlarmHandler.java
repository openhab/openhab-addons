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
package org.openhab.binding.freeboxos.internal.handler;

<<<<<<< Upstream, based on origin/main
<<<<<<< Upstream, based on origin/main
<<<<<<< Upstream, based on origin/main
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
        if (value != null) {
            switch (channelId) {
                case NODE_BATTERY:
                    return DecimalType.valueOf(value);
                case ALARM_PIN:
                    return StringType.valueOf(value);
                case ALARM_SOUND, ALARM_VOLUME:
                    return new QuantityType<>(value + " %");
                case ALARM_TIMEOUT1, ALARM_TIMEOUT2, ALARM_TIMEOUT3:
                    return new QuantityType<>(value + " s");
            }
        }
        return UnDefType.NULL;
=======
import static org.openhab.binding.freeboxos.internal.FreeboxOsBindingConstants.BINDING_ID;

=======
import java.math.BigDecimal;
import java.util.Comparator;
>>>>>>> cff27ca Saving work
import java.util.Map;
=======
import static org.openhab.binding.freeboxos.internal.FreeboxOsBindingConstants.*;
>>>>>>> 9aef877 Rebooting Home Node part

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
        if (value != null) {
            switch (channelId) {
                case NODE_BATTERY:
                    return DecimalType.valueOf(value);
                case ALARM_PIN:
                    return StringType.valueOf(value);
                case ALARM_SOUND, ALARM_VOLUME:
                    return new QuantityType<>(value + " %");
                case ALARM_TIMEOUT1, ALARM_TIMEOUT2, ALARM_TIMEOUT3:
                    return new QuantityType<>(value + " s");
            }
        }
<<<<<<< Upstream, based on origin/main
        return super.internalHandleCommand(channelId, command);
>>>>>>> 6340384 Commiting work
=======
        return UnDefType.NULL;
>>>>>>> 9aef877 Rebooting Home Node part
    }
}
