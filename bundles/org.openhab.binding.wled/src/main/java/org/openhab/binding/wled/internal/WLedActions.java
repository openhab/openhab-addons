/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.RuleAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link WLedActions} is responsible for Actions.
 *
 * @author Matthew Skinner - Initial contribution
 */

@ThingActionsScope(name = "wled")
@NonNullByDefault
public class WLedActions implements ThingActions {
    public final Logger logger = LoggerFactory.getLogger(getClass());
    private @Nullable WLedHandler handler;

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        this.handler = (WLedHandler) handler;
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }

    @RuleAction(label = "Save Preset", description = "Save a WLED state to a preset location")
    public void savePreset(
            @ActionInput(name = "presetNumber", label = "Save Preset Number", description = "Enter the number of the preset you wish to save") int presetNumber) {
        if (presetNumber > 0 && handler != null) {
            handler.savePreset(presetNumber);
        }
    }

    public static void savePreset(@Nullable ThingActions actions, int presetNumber) {
        if (actions instanceof WLedActions) {
            ((WLedActions) actions).savePreset(presetNumber);
        } else {
            throw new IllegalArgumentException("Instance is not a WLED class.");
        }
    }
}
