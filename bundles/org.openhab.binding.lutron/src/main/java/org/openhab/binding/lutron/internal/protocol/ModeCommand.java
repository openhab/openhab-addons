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
package org.openhab.binding.lutron.internal.protocol;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lutron.internal.handler.LeapBridgeHandler;
import org.openhab.binding.lutron.internal.protocol.leap.LeapCommand;
import org.openhab.binding.lutron.internal.protocol.lip.LutronCommandType;
import org.openhab.binding.lutron.internal.protocol.lip.LutronOperation;
import org.openhab.binding.lutron.internal.protocol.lip.TargetType;

/**
 * Lutron MODE command object
 *
 * @author Bob Adair - Initial contribution
 */
@NonNullByDefault
public class ModeCommand extends LutronCommandNew {
    public static final Integer ACTION_STEP = 1;

    private final Integer action;
    private final @Nullable Integer parameter;

    public ModeCommand(LutronOperation operation, Integer integrationId, Integer action, @Nullable Integer parameter) {
        super(TargetType.GREENMODE, operation, LutronCommandType.MODE, integrationId);
        this.action = action;
        this.parameter = parameter;
    }

    @Override
    public String lipCommand() {
        StringBuilder builder = new StringBuilder().append(operation).append(commandType);
        builder.append(',').append(integrationId);
        builder.append(',').append(action);
        if (parameter != null) {
            builder.append(',').append(parameter);
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
