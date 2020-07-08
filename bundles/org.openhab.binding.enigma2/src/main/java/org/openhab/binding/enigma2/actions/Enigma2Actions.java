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
package org.openhab.binding.enigma2.actions;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.binding.ThingActions;
import org.eclipse.smarthome.core.thing.binding.ThingActionsScope;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.enigma2.handler.Enigma2Handler;
import org.openhab.binding.enigma2.internal.Enigma2BindingConstants;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.RuleAction;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * This is the automation engine actions handler service for the
 * enigma2 actions.
 *
 * @author Guido Dolfen - Initial contribution
 */
@ThingActionsScope(name = "enigma2")
@NonNullByDefault
public class Enigma2Actions implements ThingActions, IEnigma2Actions {
    private @Nullable Enigma2Handler handler;

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        this.handler = (Enigma2Handler) handler;
    }

    @Override
    public @Nullable Enigma2Handler getThingHandler() {
        return this.handler;
    }

    @Override
    @RuleAction(label = "@text/actions.enigma2.send-rc-button.label", description = "@text/actions.enigma2.send-rc-button.description")
    @SuppressWarnings("null")
    public void sendRcCommand(
            @ActionInput(name = "rcButton", label = "@text/actions-input.enigma2.rc-button.label", description = "@text/actions-input.enigma2.rc-button.description") String rcButton) {
        handler.sendRcCommand(rcButton);
    }

    @Override
    @RuleAction(label = "@text/actions.enigma2.send-info.label", description = "@text/actions.enigma2.send-info.description")
    @SuppressWarnings("null")
    public void sendInfo(
            @ActionInput(name = "text", label = "@text/actions-input.enigma2.text.label", description = "@text/actions-input.enigma2.text.description") String text) {
        handler.sendInfo(Enigma2BindingConstants.MESSAGE_TIMEOUT, text);
    }

    @Override
    @RuleAction(label = "@text/actions.enigma2.send-info.label", description = "@text/actions.enigma2.send-info.description")
    @SuppressWarnings("null")
    public void sendInfo(
            @ActionInput(name = "text", label = "@text/actions-input.enigma2.text.label", description = "@text/actions-input.enigma2.text.description") String text,
            @ActionInput(name = "timeout", label = "@text/actions-input.enigma2.timeout.label", description = "@text/actions-input.enigma2.timeout.description") int timeout) {
        handler.sendInfo(timeout, text);
    }

    @Override
    @RuleAction(label = "@text/actions.enigma2.send-warning.label", description = "@text/actions.enigma2.send-warning.description")
    @SuppressWarnings("null")
    public void sendWarning(
            @ActionInput(name = "text", label = "@text/actions-input.enigma2.text.label", description = "@text/actions-input.enigma2.text.description") String text) {
        handler.sendWarning(Enigma2BindingConstants.MESSAGE_TIMEOUT, text);
    }

    @Override
    @RuleAction(label = "@text/actions.enigma2.send-warning.label", description = "@text/actions.enigma2.send-warning.description")
    @SuppressWarnings("null")
    public void sendWarning(
            @ActionInput(name = "text", label = "@text/actions-input.enigma2.text.label", description = "@text/actions-input.enigma2.text.description") String text,
            @ActionInput(name = "timeout", label = "@text/actions-input.enigma2.timeout.label", description = "@text/actions-input.enigma2.timeout.description") int timeout) {
        handler.sendWarning(timeout, text);
    }

    @Override
    @RuleAction(label = "@text/actions.enigma2.send-error.label", description = "@text/actions.enigma2.send-error.description")
    @SuppressWarnings("null")
    public void sendError(
            @ActionInput(name = "text", label = "@text/actions-input.enigma2.text.label", description = "@text/actions-input.enigma2.text.description") String text) {
        handler.sendError(Enigma2BindingConstants.MESSAGE_TIMEOUT, text);
    }

    @Override
    @RuleAction(label = "@text/actions.enigma2.send-error.label", description = "@text/actions.enigma2.send-error.description")
    @SuppressWarnings("null")
    public void sendError(
            @ActionInput(name = "text", label = "@text/actions-input.enigma2.text.label", description = "@text/actions-input.enigma2.text.description") String text,
            @ActionInput(name = "timeout", label = "@text/actions-input.enigma2.timeout.label", description = "@text/actions-input.enigma2.timeout.description") int timeout) {
        handler.sendError(timeout, text);
    }

    @Override
    @RuleAction(label = "@text/actions.enigma2.send-error.label", description = "@text/actions.enigma2.send-question.description")
    @SuppressWarnings("null")
    public void sendQuestion(
            @ActionInput(name = "text", label = "@text/actions-input.enigma2.text.label", description = "@text/actions-input.enigma2.text.description") String text) {
        handler.sendQuestion(Enigma2BindingConstants.MESSAGE_TIMEOUT, text);
    }

    @Override
    @RuleAction(label = "@text/actions.enigma2.send-error.label", description = "@text/actions.enigma2.send-question.description")
    @SuppressWarnings("null")
    public void sendQuestion(
            @ActionInput(name = "text", label = "@text/actions-input.enigma2.text.label", description = "@text/actions-input.enigma2.text.description") String text,
            @ActionInput(name = "timeout", label = "@text/actions-input.enigma2.timeout.label", description = "@text/actions-input.enigma2.timeout.description") int timeout) {
        handler.sendQuestion(timeout, text);
    }

    // delegation methods for "legacy" rule support
    public static void sendRcCommand(@Nullable ThingActions actions, String rcButton) {
        invokeMethodOf(actions).sendRcCommand(rcButton);
    }

    public static void sendInfo(@Nullable ThingActions actions, String info) {
        invokeMethodOf(actions).sendInfo(info);
    }

    public static void sendInfo(@Nullable ThingActions actions, String info, int timeout) {
        invokeMethodOf(actions).sendInfo(info, timeout);
    }

    public static void sendWarning(@Nullable ThingActions actions, String warning) {
        invokeMethodOf(actions).sendWarning(warning);
    }

    public static void sendWarning(@Nullable ThingActions actions, String warning, int timeout) {
        invokeMethodOf(actions).sendWarning(warning, timeout);
    }

    public static void sendError(@Nullable ThingActions actions, String error) {
        invokeMethodOf(actions).sendError(error);
    }

    public static void sendError(@Nullable ThingActions actions, String error, int timeout) {
        invokeMethodOf(actions).sendError(error, timeout);
    }

    public static void sendQuestion(@Nullable ThingActions actions, String text) {
        invokeMethodOf(actions).sendQuestion(text);
    }

    public static void sendQuestion(@Nullable ThingActions actions, String text, int timeout) {
        invokeMethodOf(actions).sendQuestion(text, timeout);
    }

    private static IEnigma2Actions invokeMethodOf(@Nullable ThingActions actions) {
        if (actions == null) {
            throw new IllegalArgumentException("actions cannot be null");
        }
        if (actions.getClass().getName().equals(Enigma2Actions.class.getName())) {
            if (actions instanceof IEnigma2Actions) {
                return (IEnigma2Actions) actions;
            } else {
                return (IEnigma2Actions) Proxy.newProxyInstance(IEnigma2Actions.class.getClassLoader(),
                        new Class[] { IEnigma2Actions.class }, (Object proxy, Method method, Object[] args) -> {
                            Method m = actions.getClass().getDeclaredMethod(method.getName(),
                                    method.getParameterTypes());
                            return m.invoke(actions, args);
                        });
            }
        }
        throw new IllegalArgumentException("Actions is not an instance of Enigma2Actions");
    }
}
