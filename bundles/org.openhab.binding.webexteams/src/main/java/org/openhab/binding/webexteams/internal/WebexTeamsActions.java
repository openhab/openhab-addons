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
package org.openhab.binding.webexteams.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.ActionOutput;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link WebexTeamsActions} class defines rule actions for sending messages
 *
 * @author Tom Deckers - Initial contribution
 */
@ThingActionsScope(name = "webexteams")
@NonNullByDefault
public class WebexTeamsActions implements ThingActions {

    private final Logger logger = LoggerFactory.getLogger(WebexTeamsActions.class);
    private @Nullable WebexTeamsHandler handler;

    @RuleAction(label = "@text/sendMessageActionLabel", description = "@text/sendMessageActionDescription")
    public @ActionOutput(name = "success", type = "java.lang.Boolean") Boolean sendMessage(
            @ActionInput(name = "text") @Nullable String text) {
        if (text == null) {
            logger.warn("Cannot send Message as text is missing.");
            return false;
        }

        final WebexTeamsHandler handler = this.handler;
        if (handler == null) {
            logger.debug("Handler is null, cannot send message.");
            return false;
        } else {
            return handler.sendMessage(text);
        }
    }

    @RuleAction(label = "@text/sendMessageAttActionLabel", description = "@text/sendMessageAttActionDescription")
    public @ActionOutput(name = "success", type = "java.lang.Boolean") Boolean sendMessage(
            @ActionInput(name = "text") @Nullable String text, @ActionInput(name = "attach") @Nullable String attach) {
        if (text == null) {
            logger.warn("Cannot send Message as text is missing.");
            return false;
        }
        if (attach == null) {
            logger.warn("Cannot send Message as attach is missing.");
            return false;
        }

        final WebexTeamsHandler handler = this.handler;
        if (handler == null) {
            logger.debug("Handler is null, cannot send message.");
            return false;
        } else {
            return handler.sendMessage(text, attach);
        }
    }

    @RuleAction(label = "@text/sendRoomMessageActionLabel", description = "@text/sendRoomMessageActionDescription")
    public @ActionOutput(name = "success", type = "java.lang.Boolean") Boolean sendRoomMessage(
            @ActionInput(name = "roomId") @Nullable String roomId, @ActionInput(name = "text") @Nullable String text) {
        if (text == null) {
            logger.warn("Cannot send Message as text is missing.");
            return false;
        }
        if (roomId == null) {
            logger.warn("Cannot send Message as roomId is missing.");
            return false;
        }

        final WebexTeamsHandler handler = this.handler;
        if (handler == null) {
            logger.debug("Handler is null, cannot send message.");
            return false;
        } else {
            return handler.sendRoomMessage(roomId, text);
        }
    }

    @RuleAction(label = "@text/sendRoomMessageAttActionLabel", description = "@text/sendRoomMessageAttActionDescription")
    public @ActionOutput(name = "success", type = "java.lang.Boolean") Boolean sendRoomMessage(
            @ActionInput(name = "roomId") @Nullable String roomId, @ActionInput(name = "text") @Nullable String text,
            @ActionInput(name = "attach") @Nullable String attach) {
        if (text == null) {
            logger.warn("Cannot send Message as text is missing.");
            return false;
        }
        if (roomId == null) {
            logger.warn("Cannot send Message as roomId is missing.");
            return false;
        }
        if (attach == null) {
            logger.warn("Cannot send Message as attach is missing.");
            return false;
        }
        final WebexTeamsHandler handler = this.handler;
        if (handler == null) {
            logger.debug("Handler is null, cannot send message.");
            return false;
        } else {
            return handler.sendRoomMessage(roomId, text, attach);
        }
    }

    @RuleAction(label = "@text/sendPersonMessageActionLabel", description = "@text/sendPersonMessageActionDescription")
    public @ActionOutput(name = "success", type = "java.lang.Boolean") Boolean sendPersonMessage(
            @ActionInput(name = "personEmail") @Nullable String personEmail,
            @ActionInput(name = "text") @Nullable String text) {
        if (text == null) {
            logger.warn("Cannot send Message as text is missing.");
            return false;
        }
        if (personEmail == null) {
            logger.warn("Cannot send Message as personEmail is missing.");
            return false;
        }

        final WebexTeamsHandler handler = this.handler;
        if (handler == null) {
            logger.debug("Handler is null, cannot send message.");
            return false;
        } else {
            return handler.sendPersonMessage(personEmail, text);
        }
    }

    @RuleAction(label = "@text/sendPersonMessageAttActionLabel", description = "@text/sendPersonMessageAttActionDescription")
    public @ActionOutput(name = "success", type = "java.lang.Boolean") Boolean sendPersonMessage(
            @ActionInput(name = "personEmail") @Nullable String personEmail,
            @ActionInput(name = "text") @Nullable String text, @ActionInput(name = "attach") @Nullable String attach) {
        if (text == null) {
            logger.warn("Cannot send Message as text is missing.");
            return false;
        }
        if (personEmail == null) {
            logger.warn("Cannot send Message as personEmail is missing.");
            return false;
        }
        if (attach == null) {
            logger.warn("Cannot send Message as attach is missing.");
            return false;
        }

        final WebexTeamsHandler handler = this.handler;
        if (handler == null) {
            logger.debug("Handler is null, cannot send message.");
            return false;
        } else {
            return handler.sendPersonMessage(personEmail, text, attach);
        }
    }

    public static boolean sendMessage(@Nullable ThingActions actions, @Nullable String text) {
        if (actions instanceof WebexTeamsActions teamsActions) {
            return teamsActions.sendMessage(text);
        } else {
            throw new IllegalArgumentException("Instance is not a WebexTeamsActions class.");
        }
    }

    public static boolean sendMessage(@Nullable ThingActions actions, @Nullable String text, @Nullable String attach) {
        if (actions instanceof WebexTeamsActions teamsActions) {
            return teamsActions.sendMessage(text, attach);
        } else {
            throw new IllegalArgumentException("Instance is not a WebexTeamsActions class.");
        }
    }

    public static boolean sendRoomMessage(@Nullable ThingActions actions, @Nullable String roomId,
            @Nullable String text) {
        if (actions instanceof WebexTeamsActions teamsActions) {
            return teamsActions.sendRoomMessage(roomId, text);
        } else {
            throw new IllegalArgumentException("Instance is not a WebexTeamsActions class.");
        }
    }

    public static boolean sendRoomMessage(@Nullable ThingActions actions, @Nullable String roomId,
            @Nullable String text, @Nullable String attach) {
        if (actions instanceof WebexTeamsActions teamsActions) {
            return teamsActions.sendRoomMessage(roomId, text, attach);
        } else {
            throw new IllegalArgumentException("Instance is not a WebexTeamsActions class.");
        }
    }

    public static boolean sendPersonMessage(@Nullable ThingActions actions, @Nullable String personEmail,
            @Nullable String text) {
        if (actions instanceof WebexTeamsActions teamsActions) {
            return teamsActions.sendPersonMessage(personEmail, text);
        } else {
            throw new IllegalArgumentException("Instance is not a WebexTeamsActions class.");
        }
    }

    public static boolean sendPersonMessage(@Nullable ThingActions actions, @Nullable String personEmail,
            @Nullable String text, @Nullable String attach) {
        if (actions instanceof WebexTeamsActions teamsActions) {
            return teamsActions.sendPersonMessage(personEmail, text, attach);
        } else {
            throw new IllegalArgumentException("Instance is not a WebexTeamsActions class.");
        }
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof WebexTeamsHandler teamsHandler) {
            this.handler = teamsHandler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }
}
