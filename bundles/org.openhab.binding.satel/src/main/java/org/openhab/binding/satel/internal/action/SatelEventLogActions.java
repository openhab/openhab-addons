/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.satel.internal.action;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.satel.internal.handler.SatelEventLogHandler;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.ActionOutput;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
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
public class SatelEventLogActions implements ThingActions {

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

    @RuleAction(label = "@text/actionReadEventLabel", description = "@text/actionReadEventDesc")
    public @ActionOutput(name = "index", type = "java.lang.Integer", label = "@text/actionOutputIndexLabel", description = "@text/actionOutputIndexDesc") @ActionOutput(name = "prev_index", type = "java.lang.Integer", label = "@text/actionOutputPrevIndexLabel", description = "@text/actionOutputPrevIndexDesc") @ActionOutput(name = "timestamp", type = "java.time.ZonedDateTime", label = "@text/actionOutputTimestampLabel", description = "@text/actionOutputTimestampDesc") @ActionOutput(name = "description", type = "java.lang.String", label = "@text/actionOutputDescriptionLabel", description = "@text/actionOutputDescriptionDesc") @ActionOutput(name = "details", type = "java.lang.String", label = "@text/actionOutputDetailsLabel", description = "@text/actionOutputDetailsDesc") Map<String, Object> readEvent(
            @ActionInput(name = "index", label = "@text/actionInputIndexLabel", description = "@text/actionInputIndexDesc") @Nullable Number index) {
        logger.debug("satel.readEvent called with input: index={}", index);

        Map<String, Object> result = new HashMap<>();
        SatelEventLogHandler handler = this.handler;
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

    public static Map<String, Object> readEvent(ThingActions actions, @Nullable Number index) {
        return ((SatelEventLogActions) actions).readEvent(index);
    }
}
