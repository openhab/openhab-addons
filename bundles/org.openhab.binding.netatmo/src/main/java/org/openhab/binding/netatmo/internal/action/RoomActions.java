/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.netatmo.internal.action;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.NetatmoConstants.SetpointMode;
import org.openhab.binding.netatmo.internal.handler.RoomHandler;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link RoomActions} defines thing actions for RoomHandler.
 *
 * @author Markus Dillmann - Initial contribution
 */
@ThingActionsScope(name = "netatmo")
@NonNullByDefault
public class RoomActions implements ThingActions {
    private final Logger logger = LoggerFactory.getLogger(RoomActions.class);

    private @Nullable RoomHandler handler;

    public RoomActions() {
        logger.trace("Netatmo room actions service created");
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof RoomHandler) {
            this.handler = (RoomHandler) handler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }

    /**
     * The setRoomThermpoint room thing action
     */
    @RuleAction(label = "@text/actionLabel", description = "@text/actionDesc")
    public void setRoomThermpoint(
            @ActionInput(name = "setpoint", label = "@text/actionInputSetpointLabel", description = "@text/actionInputSetpointDesc") @Nullable Double temp,
            @ActionInput(name = "endtime", label = "@text/actionInputEndtimeLabel", description = "@text/actionInputEndtimeDesc") @Nullable Long endTime) {
        setRoomThermpoint(temp, endTime, null);
    }

    @RuleAction(label = "@text/actionLabel", description = "@text/actionDesc")
    public void setRoomThermpoint(
            @ActionInput(name = "mode", label = "@text/actionInputModeLabel", description = "@text/actionInputModeDesc") @Nullable String mode,
            @ActionInput(name = "endtime", label = "@text/actionInputEndtimeLabel", description = "@text/actionInputEndtimeDesc") @Nullable Long endTime) {
        setRoomThermpoint(null, endTime, mode);
    }

    @RuleAction(label = "@text/actionLabel", description = "@text/actionDesc")
    public void setRoomThermpoint(
            @ActionInput(name = "setpoint", label = "@text/actionInputSetpointLabel", description = "@text/actionInputSetpointDesc") @Nullable Double temp,
            @ActionInput(name = "endtime", label = "@text/actionInputEndtimeLabel", description = "@text/actionInputEndtimeDesc") @Nullable Long endTime,
            @ActionInput(name = "mode", label = "@text/actionInputModeLabel", description = "@text/actionInputModeDesc") @Nullable String mode) {
        RoomHandler roomHandler = handler;
        if (roomHandler == null) {
            logger.debug("Handler not set for room thing actions.");
            return;
        }

        String targetMode = mode;
        Long targetEndTime = endTime;
        Double targetTemp = temp;
        if (targetMode != null) {
            if (!(targetMode.equals(SetpointMode.MAX.toString()) || targetMode.equals(SetpointMode.HOME.toString())
                    || targetMode.equals(SetpointMode.MANUAL.toString()))) {
                logger.debug("Mode can only be MAX, HOME or MANUAL for a room");
                return;
            }
        }
        if (temp != null) {
            logger.debug("Temperature provided, mode forced to MANUAL.");
            targetMode = SetpointMode.MANUAL.toString();
            if (targetEndTime == null) {
                logger.debug("Temperature provided but no endtime given, action ignored");
                return;
            }
        } else {
            if (targetMode == null) {
                logger.debug("mode is required if no temperature setpoint provided");
                return;
            } else if (targetMode.equalsIgnoreCase(SetpointMode.HOME.toString())) {
                targetEndTime = 0L;
                targetTemp = 0.0;
            }
        }

        try {
            roomHandler.thingActionCallSetRoomThermTemp(targetTemp != null ? targetTemp : 0,
                    targetEndTime != null ? targetEndTime : 0L, SetpointMode.valueOf(targetMode));
        } catch (IllegalArgumentException e) {
            logger.debug("Ignoring setRoomThermpoint command due to illegal argument exception: {}", e.getMessage());
        }
    }

    /**
     * Static setLevel method for Rules DSL backward compatibility
     */
    public static void setRoomThermpoint(ThingActions actions, @Nullable Double temp, @Nullable Long endTime,
            @Nullable String mode) {
        ((RoomActions) actions).setRoomThermpoint(temp, endTime, mode);
    }

    public static void setRoomThermpoint(ThingActions actions, @Nullable Double temp, @Nullable Long endTime) {
        setRoomThermpoint(actions, temp, endTime, null);
    }

    public static void setRoomThermpoint(ThingActions actions, @Nullable String mode, @Nullable Long endTime) {
        setRoomThermpoint(actions, null, endTime, mode);
    }
}
