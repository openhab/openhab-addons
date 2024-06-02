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
package org.openhab.binding.wled.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.wled.internal.handlers.WLedBridgeHandler;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link WLedActions} is responsible for Actions.
 *
 * @author Matthew Skinner - Initial contribution
 */

@Component(scope = ServiceScope.PROTOTYPE, service = WLedActions.class)
@ThingActionsScope(name = "wled")
@NonNullByDefault
public class WLedActions implements ThingActions {
    public final Logger logger = LoggerFactory.getLogger(getClass());
    private @Nullable WLedBridgeHandler handler;

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        this.handler = (WLedBridgeHandler) handler;
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }

    @RuleAction(label = "save state to preset", description = "Save a WLED state to a preset slot")
    public void savePreset(
            @ActionInput(name = "presetNumber", label = "Preset Slot", description = "Number for the preset slot you wish to use") int presetNumber) {
        savePreset(presetNumber, "");
    }

    public static void savePreset(@Nullable ThingActions actions, int presetNumber) {
        if (actions instanceof WLedActions wLedActions) {
            wLedActions.savePreset(presetNumber, "");
        } else {
            throw new IllegalArgumentException("Instance is not a WLED class.");
        }
    }

    @RuleAction(label = "save state to preset", description = "Save a WLED state to a preset slot")
    public void savePreset(
            @ActionInput(name = "presetNumber", label = "Preset Slot", description = "Number for the preset slot you wish to use") int presetNumber,
            @ActionInput(name = "presetName", label = "Preset Name", description = "Name for the preset that you wish to use") String presetName) {
        WLedBridgeHandler localHandler = handler;
        if (localHandler != null) {
            localHandler.savePreset(presetNumber, presetName);
        }
    }

    public static void savePreset(@Nullable ThingActions actions, int presetNumber, String presetName) {
        if (actions instanceof WLedActions wLedActions) {
            wLedActions.savePreset(presetNumber, presetName);
        } else {
            throw new IllegalArgumentException("Instance is not a WLED class.");
        }
    }
}
