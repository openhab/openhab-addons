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
package org.openhab.binding.avmfritz.internal.actions;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.avmfritz.internal.handler.AVMFritzPowerMeterActionsHandler;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;

/**
 * The {@link AVMFritzPowerMeterActions} defines thing actions for power meter devices / groups of the avmfritz binding.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@ThingActionsScope(name = "avmfritz")
@NonNullByDefault
public class AVMFritzPowerMeterActions implements ThingActions {

    private @Nullable AVMFritzPowerMeterActionsHandler handler;

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        this.handler = (AVMFritzPowerMeterActionsHandler) handler;
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }

    @RuleAction(label = "@text/enablePowerMeterHighRefreshActionLabel", description = "@text/enablePowerMeterHighRefreshActionDescription")
    public void enablePowerMeterHighRefresh(
            @ActionInput(name = "Device Id", label = "@text/enablePowerMeterHighRefreshInputLabel", description = "@text/enablePowerMeterHighRefreshInputDescription", type = "java.lang.Long", required = true) @Nullable Long deviceId) {
        AVMFritzPowerMeterActionsHandler actionsHandler = handler;
        if (actionsHandler == null) {
            throw new IllegalArgumentException("AVMFritzPowerMeterDeviceHandler ThingHandler is null!");
        }
        if (deviceId == null) {
            throw new IllegalArgumentException("Cannot enable power meter high refresh as 'deviceId' is null!");
        }
        actionsHandler.enablePowerMeterHighRefresh(deviceId.longValue());
    }

    public static void enablePowerMeterHighRefresh(ThingActions actions, @Nullable Long deviceId) {
        ((AVMFritzPowerMeterActions) actions).enablePowerMeterHighRefresh(deviceId);
    }

    @RuleAction(label = "@text/disablePowerMeterHighRefreshActionLabel", description = "@text/disablePowerMeterHighRefreshDescription")
    public void disablePowerMeterHighRefresh() {
        AVMFritzPowerMeterActionsHandler actionsHandler = handler;
        if (actionsHandler == null) {
            throw new IllegalArgumentException("AVMFritzPowerMeterDeviceHandler ThingHandler is null!");
        }
        actionsHandler.disablePowerMeterHighRefresh();
    }

    public static void disablePowerMeterHighRefresh(ThingActions actions) {
        ((AVMFritzPowerMeterActions) actions).disablePowerMeterHighRefresh();
    }
}
