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
package org.openhab.binding.pushbullet.internal.action;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.binding.ThingActions;
import org.eclipse.smarthome.core.thing.binding.ThingActionsScope;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.pushbullet.internal.handler.PushbulletHandler;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.ActionOutput;
import org.openhab.core.automation.annotation.RuleAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PushbulletActions} class defines rule actions for sending notifications
 * <p>
 * <b>Note:</b>The static method <b>invokeMethodOf</b> handles the case where
 * the test <i>actions instanceof PushbulletActions</i> fails. This test can fail
 * due to an issue in openHAB core v2.5.0 where the {@link PushbulletActions} class
 * can be loaded by a different classloader than the <i>actions</i> instance.
 *
 * @author Hakan Tandogan - Initial contribution
 */
@ThingActionsScope(name = "pushbullet")
@NonNullByDefault
public class PushbulletActions implements ThingActions, IPushbulletActions {

    private final Logger logger = LoggerFactory.getLogger(PushbulletActions.class);

    private @Nullable PushbulletHandler handler;

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        this.handler = (PushbulletHandler) handler;
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return this.handler;
    }

    @Override
    @RuleAction(label = "@text/actionSendPushbulletNoteLabel", description = "@text/actionSendPushbulletNoteDesc")
    public @ActionOutput(name = "success", type = "java.lang.Boolean") Boolean sendPushbulletNote(
            @ActionInput(name = "recipient", label = "@text/actionSendPushbulletNoteInputRecipientLabel", description = "@text/actionSendPushbulletNoteInputRecipientDesc") @Nullable String recipient,
            @ActionInput(name = "title", label = "@text/actionSendPushbulletNoteInputTitleLabel", description = "@text/actionSendPushbulletNoteInputTitleDesc") @Nullable String title,
            @ActionInput(name = "message", label = "@text/actionSendPushbulletNoteInputMessageLabel", description = "@text/actionSendPushbulletNoteInputMessageDesc") @Nullable String message) {
        logger.trace("sendPushbulletNote '{}', '{}', '{}'", recipient, title, message);

        // Use local variable so the SAT check can do proper flow analysis
        PushbulletHandler localHandler = handler;

        if (localHandler == null) {
            logger.warn("Pushbullet service Handler is null!");
            return false;
        }

        return localHandler.sendPush(recipient, title, message, "note");
    }

    public static boolean sendPushbulletNote(@Nullable ThingActions actions, @Nullable String recipient,
            @Nullable String title, @Nullable String message) {
        return invokeMethodOf(actions).sendPushbulletNote(recipient, title, message);
    }

    @Override
    @RuleAction(label = "@text/actionSendPushbulletNoteLabel", description = "@text/actionSendPushbulletNoteDesc")
    public @ActionOutput(name = "success", type = "java.lang.Boolean") Boolean sendPushbulletNote(
            @ActionInput(name = "recipient", label = "@text/actionSendPushbulletNoteInputRecipientLabel", description = "@text/actionSendPushbulletNoteInputRecipientDesc") @Nullable String recipient,
            @ActionInput(name = "message", label = "@text/actionSendPushbulletNoteInputMessageLabel", description = "@text/actionSendPushbulletNoteInputMessageDesc") @Nullable String message) {
        logger.trace("sendPushbulletNote '{}', '{}'", recipient, message);

        // Use local variable so the SAT check can do proper flow analysis
        PushbulletHandler localHandler = handler;

        if (localHandler == null) {
            logger.warn("Pushbullet service Handler is null!");
            return false;
        }

        return localHandler.sendPush(recipient, message, "note");
    }

    public static boolean sendPushbulletNote(@Nullable ThingActions actions, @Nullable String recipient,
            @Nullable String message) {
        return invokeMethodOf(actions).sendPushbulletNote(recipient, message);
    }

    private static IPushbulletActions invokeMethodOf(@Nullable ThingActions actions) {
        if (actions == null) {
            throw new IllegalArgumentException("actions cannot be null");
        }
        if (actions.getClass().getName().equals(PushbulletActions.class.getName())) {
            if (actions instanceof IPushbulletActions) {
                return (IPushbulletActions) actions;
            } else {
                return (IPushbulletActions) Proxy.newProxyInstance(IPushbulletActions.class.getClassLoader(),
                        new Class[] { IPushbulletActions.class }, (Object proxy, Method method, Object[] args) -> {
                            Method m = actions.getClass().getDeclaredMethod(method.getName(),
                                    method.getParameterTypes());
                            return m.invoke(actions, args);
                        });
            }
        }
        throw new IllegalArgumentException("Actions is not an instance of PushbulletActions");
    }
}
