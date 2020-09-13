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
package org.openhab.binding.satel.action;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.binding.ThingActions;
import org.eclipse.smarthome.core.thing.binding.ThingActionsScope;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.satel.internal.handler.SatelEventLogHandler;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.ActionOutput;
import org.openhab.core.automation.annotation.RuleAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Automation action handler service for reading Satel event log.
 *
 * @author Krzysztof Goworek - Initial contribution
 * @see SatelEventLogHandler
 */
@ThingActionsScope(name = "satel")
@NonNullByDefault
public class SatelEventLogActions implements ThingActions, ISatelEventLogActions {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private @Nullable SatelEventLogHandler handler;

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof SatelEventLogHandler) {
            this.handler = (SatelEventLogHandler) handler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }

    @Override
    @RuleAction(label = "@text/actionReadEventLabel", description = "@text/actionReadEventDesc")
    public @ActionOutput(name = "index", type = "java.lang.Integer", label = "@text/actionOutputIndexLabel", description = "@text/actionOutputIndexDesc") @ActionOutput(name = "prev_index", type = "java.lang.Integer", label = "@text/actionOutputPrevIndexLabel", description = "@text/actionOutputPrevIndexDesc") @ActionOutput(name = "timestamp", type = "java.time.ZonedDateTime", label = "@text/actionOutputTimestampLabel", description = "@text/actionOutputTimestampDesc") @ActionOutput(name = "description", type = "java.lang.String", label = "@text/actionOutputDescriptionLabel", description = "@text/actionOutputDescriptionDesc") @ActionOutput(name = "details", type = "java.lang.String", label = "@text/actionOutputDetailsLabel", description = "@text/actionOutputDetailsDesc") Map<String, Object> readEvent(
            @ActionInput(name = "index", label = "@text/actionInputIndexLabel", description = "@text/actionInputIndexDesc") @Nullable Number index) {
        logger.debug("satel.readEvent called with input: index={}", index);

        Map<String, Object> result = new HashMap<>();
        if (handler != null) {
            handler.readEvent(index == null ? -1 : index.intValue()).ifPresent(event -> {
                result.put("index", event.getIndex());
                result.put("prev_index", event.getPrevIndex());
                result.put("timestamp", event.getTimestamp());
                result.put("description", event.getDescription());
                result.put("details", event.getDetails());
            });
        }
        return result;
    }

    public static Map<String, Object> readEvent(@Nullable ThingActions actions, @Nullable Number index) {
        return invokeMethodOf(actions).readEvent(index);
    }

    private static ISatelEventLogActions invokeMethodOf(@Nullable ThingActions actions) {
        if (actions == null) {
            throw new IllegalArgumentException("actions cannot be null");
        } else if (actions instanceof ISatelEventLogActions) {
            return (ISatelEventLogActions) actions;
        } else if (actions.getClass().getName().equals(SatelEventLogActions.class.getName())) {
            return (ISatelEventLogActions) Proxy.newProxyInstance(ISatelEventLogActions.class.getClassLoader(),
                    new Class[] { ISatelEventLogActions.class }, (Object proxy, Method method, Object[] args) -> {
                        Method m = actions.getClass().getDeclaredMethod(method.getName(), method.getParameterTypes());
                        return m.invoke(actions, args);
                    });
        }
        throw new IllegalArgumentException("actions is not an instance of ISatelEventLogActions");
    }
}
