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
package org.openhab.binding.roborock.internal.action;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.roborock.internal.RoborockVacuumHandler;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.ActionOutput;
import org.openhab.core.automation.annotation.ActionOutputs;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * Exposes Roborock vacuum actions to automation rules.
 *
 * @author reyhard - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = RoborockVacuumActions.class)
@ThingActionsScope(name = "roborock")
@NonNullByDefault
public class RoborockVacuumActions implements ThingActions {
    private @Nullable RoborockVacuumHandler handler;

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof RoborockVacuumHandler roborockVacuumHandler) {
            this.handler = roborockVacuumHandler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }

    @RuleAction(label = "@text/action.download-rrmap.label", description = "@text/action.download-rrmap.description")
    public @Nullable @ActionOutputs({
            @ActionOutput(name = "result", label = "@text/action.download-rrmap.result", type = "java.lang.String") }) String downloadRrMap(
                    @ActionInput(name = "directory", label = "@text/action.download-rrmap.directory.label", description = "@text/action.download-rrmap.directory.description", type = "java.lang.String") @Nullable String directory) {
        RoborockVacuumHandler handler = this.handler;
        return handler != null ? handler.downloadRrMap(directory) : null;
    }

    public static @Nullable String downloadRrMap(@Nullable ThingActions actions) {
        return downloadRrMap(actions, null);
    }

    public static @Nullable String downloadRrMap(@Nullable ThingActions actions, @Nullable String directory) {
        if (actions instanceof RoborockVacuumActions roborockActions) {
            return roborockActions.downloadRrMap(directory);
        }
        return null;
    }
}
