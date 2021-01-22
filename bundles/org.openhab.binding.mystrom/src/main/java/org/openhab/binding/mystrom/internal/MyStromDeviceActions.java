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
package org.openhab.binding.mystrom.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MyStromDeviceActions} class defines thing actions for myStrom devices.
 *
 * @author Frederic Chastagnol - Initial contribution
 */
@ThingActionsScope(name = "myStrom")
@NonNullByDefault
public class MyStromDeviceActions implements ThingActions {

    private final Logger logger = LoggerFactory.getLogger(MyStromDeviceActions.class);
    private @Nullable ThingHandler handler;

    @Override
    public void setThingHandler(@Nullable ThingHandler thingHandler) {
        handler = thingHandler;
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }

    /**
     * Reboot thing action
     */
    @RuleAction(label = "Refresh device properties", description = "Refresh the device properties.")
    public void refreshProperties() {
        if (handler instanceof MyStromAbstractHandler) {
            try {
                ((MyStromAbstractHandler) handler).updateProperties();
            } catch (MyStromException e) {
                logger.warn("Error while refreshing properties {}", e.getMessage());
            }
        }
    }

    // Static method for Rules DSL backward compatibility
    public static void refreshProperties(ThingActions actions) {
        ((MyStromDeviceActions) actions).refreshProperties();
    }
}
