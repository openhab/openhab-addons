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
package org.openhab.binding.matter.internal.actions;

import java.util.concurrent.ExecutionException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.matter.internal.MatterBindingConstants;
import org.openhab.binding.matter.internal.handler.ControllerHandler;
import org.openhab.binding.matter.internal.util.ResourceHelper;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.ActionOutput;
import org.openhab.core.automation.annotation.ActionOutputs;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MatterControllerActions}
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
@Component(scope = ServiceScope.PROTOTYPE, service = MatterControllerActions.class)
@ThingActionsScope(name = "matter")
public class MatterControllerActions implements ThingActions {
    public final Logger logger = LoggerFactory.getLogger(getClass());
    private @Nullable ControllerHandler handler;

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        this.handler = (ControllerHandler) handler;
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }

    @RuleAction(label = MatterBindingConstants.THING_ACTION_LABEL_CONTROLLER_PAIR_DEVICE, description = MatterBindingConstants.THING_ACTION_DESC_CONTROLLER_PAIR_DEVICE)
    public @Nullable @ActionOutputs({
            @ActionOutput(name = "result", label = MatterBindingConstants.THING_ACTION_LABEL_CONTROLLER_PAIR_DEVICE_RESULT, type = "java.lang.String") }) String pairDevice(
                    @ActionInput(name = "code", label = MatterBindingConstants.THING_ACTION_LABEL_CONTROLLER_PAIR_DEVICE_CODE, description = MatterBindingConstants.THING_ACTION_DESC_CONTROLLER_PAIR_DEVICE_CODE, type = "java.lang.String") String code) {
        ControllerHandler handler = this.handler;
        if (handler != null) {
            try {
                handler.startScan(code).get();
                return ResourceHelper.getResourceString(MatterBindingConstants.THING_ACTION_RESULT_DEVICE_ADDED);
            } catch (InterruptedException | ExecutionException e) {
                return ResourceHelper.getResourceString(MatterBindingConstants.THING_ACTION_RESULT_PAIRING_FAILED)
                        + e.getLocalizedMessage();
            }
        }
        return ResourceHelper.getResourceString(MatterBindingConstants.THING_ACTION_RESULT_NO_HANDLER);
    }
}
