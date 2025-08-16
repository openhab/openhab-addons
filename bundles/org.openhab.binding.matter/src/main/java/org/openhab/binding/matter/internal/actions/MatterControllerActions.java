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
import org.openhab.binding.matter.internal.client.MatterErrorCode;
import org.openhab.binding.matter.internal.client.MatterRequestException;
import org.openhab.binding.matter.internal.controller.MatterControllerClient;
import org.openhab.binding.matter.internal.handler.ControllerHandler;
import org.openhab.binding.matter.internal.util.TranslationService;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.ActionOutput;
import org.openhab.core.automation.annotation.ActionOutputs;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MatterControllerActions} exposes Matter related actions for the Matter Controller Thing.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
@Component(scope = ServiceScope.PROTOTYPE, service = MatterControllerActions.class)
@ThingActionsScope(name = "matter")
public class MatterControllerActions implements ThingActions {
    public final Logger logger = LoggerFactory.getLogger(getClass());
    private @Nullable ControllerHandler handler;
    private final TranslationService translationService;

    @Activate
    public MatterControllerActions(@Reference TranslationService translationService) {
        this.translationService = translationService;
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof ControllerHandler controllerHandler) {
            this.handler = controllerHandler;
        }
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
                return translationService.getTranslation(MatterBindingConstants.THING_ACTION_RESULT_DEVICE_ADDED);
            } catch (InterruptedException e) {
                return handler.getTranslation(MatterBindingConstants.THING_ACTION_RESULT_PAIRING_FAILED,
                        e.getLocalizedMessage());
            } catch (ExecutionException e) {
                if (e.getCause() instanceof MatterRequestException matterRequestException) {
                    MatterErrorCode errorCode = matterRequestException.getErrorCode();
                    if (errorCode != null) {
                        return handler.getTranslation(errorCode.getTranslationKey());
                    } else {
                        return handler.getTranslation(MatterBindingConstants.THING_ACTION_RESULT_PAIRING_FAILED,
                                matterRequestException.getErrorMessage());
                    }
                }
                return handler.getTranslation(MatterBindingConstants.THING_ACTION_RESULT_PAIRING_FAILED,
                        e.getLocalizedMessage());
            }
        }
        return translationService.getTranslation(MatterBindingConstants.THING_ACTION_RESULT_NO_HANDLER);
    }

    @RuleAction(label = MatterBindingConstants.THING_ACTION_LABEL_CONTROLLER_GET_DEBUG_NODE_DATA, description = MatterBindingConstants.THING_ACTION_DESC_CONTROLLER_GET_DEBUG_NODE_DATA)
    public @Nullable @ActionOutputs({
            @ActionOutput(name = "result", label = MatterBindingConstants.THING_ACTION_LABEL_CONTROLLER_GET_DEBUG_NODE_DATA_RESULT, type = "java.lang.String") }) String getDebugNodeData() {
        ControllerHandler handler = this.handler;
        if (handler != null) {
            MatterControllerClient client = handler.getClient();
            if (client != null) {
                try {
                    return client.getAllDataForAllNodes().get();
                } catch (InterruptedException | ExecutionException e) {
                    return handler.getTranslation(
                            MatterBindingConstants.THING_ACTION_LABEL_CONTROLLER_GET_DEBUG_NODE_DATA_FAILED,
                            e.getLocalizedMessage());
                }
            }
        }
        return translationService.getTranslation(MatterBindingConstants.THING_ACTION_RESULT_NO_HANDLER);
    }
}
