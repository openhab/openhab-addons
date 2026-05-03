/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.smartthings.internal.handler;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.smartthings.internal.SmartThingsBindingConstants;
import org.openhab.binding.smartthings.internal.api.SmartThingsApi;
import org.openhab.binding.smartthings.internal.type.SmartThingsException;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.ActionOutput;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * The {@link SmartThingsActions} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Laurent Arnal - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = SmartThingsActions.class)
@ThingActionsScope(name = "smartthings") // Your bindings id is usually the scope
@NonNullByDefault
public class SmartThingsActions implements ThingActions {
    private @Nullable SmartThingsThingHandler handler;

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof SmartThingsThingHandler) {
            this.handler = (SmartThingsThingHandler) handler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }

    @ActionOutput(name = "result", type = "String", label = "@text/sendCommandActions.resultLabel", description = "@text/sendCommandActions.resultDesc")
    @RuleAction(label = "@text/sendCommandActions.sendCommandLabel", description = "@text/sendCommandActions.sendCommandDesc")
    public String sendCommand(
            @ActionInput(name = "json", label = "@text/sendCommandActions.jsonLabel", description = "@text/sendCommandActions.jsonDesc") @Nullable String jsonCmd)
            throws SmartThingsException {
        SmartThingsThingHandler lcHandler = handler;

        if (lcHandler == null) {
            throw new SmartThingsException("handler is not initialized");
        }

        Map<String, String> properties = lcHandler.getThing().getProperties();
        String deviceId = properties.get(SmartThingsBindingConstants.DEVICE_ID);
        if (deviceId == null) {
            return "Missing device id";
        }

        if (jsonCmd == null) {
            return "JsonCmd empty";
        }

        SmartThingsApi api = lcHandler.getApi();

        if (api == null) {
            throw new SmartThingsException("api is not initialized");
        }
        try {
            return api.sendCommand(deviceId, jsonCmd);
        } catch (SmartThingsException e) {
            return "Error during command execution:" + SmartThingsException.getRootCauseMessage(e);
        }
    }

    public static String sendCommand(@Nullable ThingActions actions, @Nullable String jsonCmd)
            throws SmartThingsException {
        if (actions instanceof SmartThingsActions) {
            return ((SmartThingsActions) actions).sendCommand(jsonCmd);
        } else {
            throw new IllegalArgumentException("Instance is not an SmartThingsActions class.");
        }
    }
}
