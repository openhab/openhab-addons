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
import org.openhab.binding.lutron.internal.protocol.leap.Request;
import org.openhab.binding.lutron.internal.protocol.lip.LutronCommandType;
import org.openhab.binding.lutron.internal.protocol.lip.LutronOperation;
import org.openhab.binding.lutron.internal.protocol.lip.TargetType;

/**
 * Lutron GROUP command object
 *
 * @author Bob Adair - Initial contribution
 */
@NonNullByDefault
public class GroupCommand extends LutronCommandNew {
    public static final Integer ACTION_GROUPSTATE = 3;
    public static final Integer STATE_GRP_OCCUPIED = 3;
    public static final Integer STATE_GRP_UNOCCUPIED = 4;
    public static final Integer STATE_GRP_UNKNOWN = 255;

    private final Integer action;
    private final @Nullable Integer state;

    /**
     * GroupCommand constructor
     *
     * @param targetType
     * @param operation
     * @param integrationId
     * @param action
     * @param state
     */
    public GroupCommand(LutronOperation operation, Integer integrationId, Integer action, @Nullable Integer state) {
        super(TargetType.GROUP, operation, LutronCommandType.GROUP, integrationId);
        this.action = action;
        this.state = state;
    }

    @Override
    public String lipCommand() {
        StringBuilder builder = new StringBuilder().append(operation).append(commandType);
        builder.append(',').append(integrationId);
        builder.append(',').append(action);
        if (state != null) {
            builder.append(',').append(state);
        }

        return builder.toString();
    }

    @Override
    public @Nullable LeapCommand leapCommand(LeapBridgeHandler bridgeHandler, @Nullable Integer leapZone) {
        if (action.equals(GroupCommand.ACTION_GROUPSTATE)) {
            // Get status for all occupancy groups because you can't query just one
            return new LeapCommand(Request.getOccupancyGroupStatus());
        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        return lipCommand();
    }
}
