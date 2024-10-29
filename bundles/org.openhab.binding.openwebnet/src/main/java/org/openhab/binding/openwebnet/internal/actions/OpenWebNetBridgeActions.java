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
package org.openhab.binding.openwebnet.internal.actions;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.openwebnet.internal.handler.OpenWebNetBridgeHandler;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.ActionOutput;
import org.openhab.core.automation.annotation.ActionOutputs;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.openwebnet4j.OpenGateway;
import org.openwebnet4j.communication.OWNException;
import org.openwebnet4j.communication.Response;
import org.openwebnet4j.message.BaseOpenMessage;
import org.openwebnet4j.message.FrameException;
import org.openwebnet4j.message.OpenMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OpenWebNetBridgeActions} defines the Bridge actions for the
 * openwebnet binding.
 *
 * @author Massimo Valla - Initial contribution
 */

@ThingActionsScope(name = "openwebnet")
@NonNullByDefault
public class OpenWebNetBridgeActions implements ThingActions {

    private final Logger logger = LoggerFactory.getLogger(OpenWebNetBridgeActions.class);
    private @Nullable OpenWebNetBridgeHandler bridgeHandler;

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        this.bridgeHandler = (OpenWebNetBridgeHandler) handler;
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return bridgeHandler;
    }

    @RuleAction(label = "sendMessage", description = "@text/action.sendMessage.desc")
    public @ActionOutput(label = "Success", type = "java.lang.Boolean") Boolean sendMessage(
            @ActionInput(name = "message", label = "message", description = "@text/action.sendMessage.input.message.desc") @Nullable String message) {
        @Nullable
        Boolean s = (Boolean) sendMessageInternal(message).get("success");
        if (s != null) {
            return s;
        } else {
            return Boolean.FALSE;
        }
    }

    @RuleAction(label = "sendMessageWithResponse", description = "@text/action.sendMessageWithResponse.desc")
    public @ActionOutputs({ @ActionOutput(name = "success", label = "Success", type = "java.lang.Boolean"),
            @ActionOutput(name = "responseMessages", label = "Response Messages", type = "java.util.List<String>") }) Map<String, Object> sendMessageWithResponse(
                    @ActionInput(name = "message", label = "message", description = "@text/action.sendMessage.input.message.desc") @Nullable String message) {
        return sendMessageInternal(message);
    }

    private Map<String, Object> sendMessageInternal(@Nullable String message) {
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("success", Boolean.FALSE);
        responseMap.put("responseMessages", Collections.emptyList());

        if (message == null || message.isBlank()) {
            logger.warn("openwebnet sendMessage: cannot send message, message is null or empty");
            return responseMap;
        }
        OpenWebNetBridgeHandler handler = bridgeHandler;
        if (handler == null) {
            logger.warn("openwebnet sendMessage: cannot send message, bridgeHandler is null.");
            return responseMap;
        }
        OpenMessage msg;
        try {
            msg = BaseOpenMessage.parse(message);
        } catch (FrameException e) {
            logger.warn("openwebnet skipping sending message '{}': {}.", message, e.getMessage());
            return responseMap;
        }

        OpenGateway gw = handler.getGateway();
        if (gw != null && gw.isConnected()) {
            try {
                Response res = gw.send(msg);
                logger.debug("sent message {} to gateway. Response: {}.", msg, res.getResponseMessages());
                responseMap.put("success", res.isSuccess());
                List<String> resultList = res.getResponseMessages().stream().map(rm -> rm.getFrameValue())
                        .collect(Collectors.toList());
                responseMap.put("responseMessages", resultList);
                return responseMap;
            } catch (OWNException e) {
                logger.warn("openwebnet exception while sending message '{}' to gateway: {}.", msg, e.getMessage());
                return responseMap;
            }
        } else {
            logger.warn("openwebnet skipping sendMessage for bridge {}: gateway is not connected.",
                    handler.getThing().getUID());
            return responseMap;
        }
    }

    // legacy delegate methods
    public static Boolean sendMessage(@Nullable ThingActions actions, String message) {
        if (actions instanceof OpenWebNetBridgeActions openwebnetBridgeActions) {
            return openwebnetBridgeActions.sendMessage(message);
        } else {
            throw new IllegalArgumentException("Instance is not an OpenWebNetBridgeActions class.");
        }
    }

    public static Map<String, Object> sendMessageWithResponse(@Nullable ThingActions actions, String message) {
        if (actions instanceof OpenWebNetBridgeActions openwebnetBridgeActions) {
            return openwebnetBridgeActions.sendMessageWithResponse(message);
        } else {
            throw new IllegalArgumentException("Instance is not an OpenWebNetBridgeActions class.");
        }
    }
}
