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
package org.openhab.binding.bluelink.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * Basic vehicle actions for the Bluelink binding.
 * <p>
 * These actions should be available for all API regions.
 *
 * @author Marcus Better - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = BaseVehicleActions.class)
@ThingActionsScope(name = "bluelink")
@NonNullByDefault
public class BaseVehicleActions implements ThingActions {
    protected @Nullable BluelinkVehicleHandler handler;

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        this.handler = (BluelinkVehicleHandler) handler;
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }

    @RuleAction(label = "@text/action.force-refresh.label", description = "@text/action.force-refresh.desc")
    public void forceRefresh() {
        final BluelinkVehicleHandler hnd = handler;
        if (hnd != null) {
            hnd.refreshVehicleStatus(true);
        }
    }

    public static void forceRefresh(final @Nullable ThingActions actions) {
        if (actions instanceof VehicleControlActions va) {
            va.forceRefresh();
        } else {
            throw new IllegalArgumentException("expected VehicleControlActions");
        }
    }
}
