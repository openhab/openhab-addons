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
 * Lutron SYSVAR command object
 *
 * @author Bob Adair - Initial contribution
 */
@NonNullByDefault
public class SysvarCommand extends LutronCommandNew {
    public static final Integer ACTION_GETSETSYSVAR = 1;

    private final Integer action;
    private final @Nullable Object parameter;

    /**
     * SysvarCommand constructor
     *
     * @param operation
     * @param integrationId
     * @param action
     * @param parameter
     */
    public SysvarCommand(LutronOperation operation, Integer integrationId, Integer action, @Nullable Object parameter) {
        super(TargetType.SYSVAR, operation, LutronCommandType.SYSVAR, integrationId);
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
        return null; // No equivalent LEAP command
    }

    @Override
    public String toString() {
        return lipCommand();
    }
}
