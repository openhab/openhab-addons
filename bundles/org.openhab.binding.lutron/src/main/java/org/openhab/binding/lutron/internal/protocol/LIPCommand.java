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
package org.openhab.binding.lutron.internal.protocol;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lutron.internal.handler.LeapBridgeHandler;
import org.openhab.binding.lutron.internal.protocol.leap.LeapCommand;
import org.openhab.binding.lutron.internal.protocol.lip.LutronCommandType;
import org.openhab.binding.lutron.internal.protocol.lip.LutronOperation;
import org.openhab.binding.lutron.internal.protocol.lip.TargetType;

/**
 * Generic LIP command for use inside bridge handler
 *
 * @author Bob Adair - Initial contribution
 */
@NonNullByDefault
public class LIPCommand extends LutronCommandNew {
    private final Object[] parameters;

    public LIPCommand(TargetType targetType, LutronOperation operation, LutronCommandType CommandType,
            @Nullable Integer integrationId, Object... parameters) {
        super(targetType, operation, CommandType, integrationId);
        this.parameters = parameters;
    }

    @Override
    public String lipCommand() {
        StringBuilder builder = new StringBuilder().append(operation).append(commandType);
        if (integrationId != null) {
            builder.append(',').append(integrationId);
        }
        if (parameters != null) { // This CAN be null
            for (Object parameter : parameters) {
                builder.append(',').append(parameter);
            }
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

    public int getNumberParameter(int position) {
        if (parameters.length > position && parameters[position] instanceof Number) {
            Number num = (Number) parameters[position];
            return num.intValue();
        } else {
            throw new IllegalArgumentException("Invalid command parameter");
        }
    }
}
