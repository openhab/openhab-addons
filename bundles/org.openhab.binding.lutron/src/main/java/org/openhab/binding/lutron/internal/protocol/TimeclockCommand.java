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
package org.openhab.binding.lutron.internal.protocol;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lutron.internal.handler.LeapBridgeHandler;
import org.openhab.binding.lutron.internal.protocol.leap.LeapCommand;
import org.openhab.binding.lutron.internal.protocol.lip.LutronCommandType;
import org.openhab.binding.lutron.internal.protocol.lip.LutronOperation;
import org.openhab.binding.lutron.internal.protocol.lip.TargetType;

/**
 * Lutron TIMECLOCK command object
 *
 * @author Bob Adair - Initial contribution
 */
@NonNullByDefault
public class TimeclockCommand extends LutronCommandNew {
    public static final Integer ACTION_CLOCKMODE = 1;
    public static final Integer ACTION_SUNRISE = 2;
    public static final Integer ACTION_SUNSET = 3;
    public static final Integer ACTION_EXECEVENT = 5;
    public static final Integer ACTION_SETEVENT = 6;
    public static final Integer EVENT_ENABLE = 1;
    public static final Integer EVENT_DISABLE = 2;

    private final Integer action;
    private final @Nullable Object parameter;
    private final @Nullable Boolean enable;

    /**
     * TimeclockCommand constructor
     *
     * @param targetType
     * @param operation
     * @param integrationId
     * @param action
     * @param parameter
     * @param enable true = enable, false = disable
     */
    public TimeclockCommand(LutronOperation operation, Integer integrationId, Integer action,
            @Nullable Object parameter, @Nullable Boolean enable) {
        super(TargetType.TIMECLOCK, operation, LutronCommandType.TIMECLOCK, integrationId);
        this.action = action;
        this.parameter = parameter;
        this.enable = enable;
    }

    @Override
    public String lipCommand() {
        StringBuilder builder = new StringBuilder().append(operation).append(commandType);
        builder.append(',').append(integrationId);
        builder.append(',').append(action);
        if (parameter != null) {
            builder.append(',').append(parameter);
        }
        if (enable != null) {
            builder.append(',');
            builder.append((enable) ? '1' : '2');
        }

        return builder.toString();
    }

    @Override
    public @Nullable LeapCommand leapCommand(LeapBridgeHandler bridgeHandler, @Nullable Integer leapZone) {
        return null;
    }

    @Override
    public String toString() {
        return lipCommand();
    }
}
