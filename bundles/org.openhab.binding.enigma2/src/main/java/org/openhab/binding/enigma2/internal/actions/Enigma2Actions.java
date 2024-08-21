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
package org.openhab.binding.enigma2.internal.actions;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.enigma2.internal.Enigma2BindingConstants;
import org.openhab.binding.enigma2.internal.handler.Enigma2Handler;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * This is the automation engine actions handler service for the
 * enigma2 actions.
 *
 * @author Guido Dolfen - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = Enigma2Actions.class)
@ThingActionsScope(name = "enigma2")
@NonNullByDefault
public class Enigma2Actions implements ThingActions {
    private @Nullable Enigma2Handler handler;

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        this.handler = (Enigma2Handler) handler;
    }

    @Override
    public @Nullable Enigma2Handler getThingHandler() {
        return handler;
    }

    @RuleAction(label = "@text/actions.enigma2.send-rc-button.label", description = "@text/actions.enigma2.send-rc-button.description")
    @SuppressWarnings("null")
    public void sendRcCommand(
            @ActionInput(name = "rcButton", label = "@text/actions-input.enigma2.rc-button.label", description = "@text/actions-input.enigma2.rc-button.description") String rcButton) {
        handler.sendRcCommand(rcButton);
    }

    @RuleAction(label = "@text/actions.enigma2.send-info.label", description = "@text/actions.enigma2.send-info.description")
    @SuppressWarnings("null")
    public void sendInfo(
            @ActionInput(name = "text", label = "@text/actions-input.enigma2.text.label", description = "@text/actions-input.enigma2.text.description") String text) {
        handler.sendInfo(Enigma2BindingConstants.MESSAGE_TIMEOUT, text);
    }

    @RuleAction(label = "@text/actions.enigma2.send-info.label", description = "@text/actions.enigma2.send-info.description")
    @SuppressWarnings("null")
    public void sendInfo(
            @ActionInput(name = "text", label = "@text/actions-input.enigma2.text.label", description = "@text/actions-input.enigma2.text.description") String text,
            @ActionInput(name = "timeout", label = "@text/actions-input.enigma2.timeout.label", description = "@text/actions-input.enigma2.timeout.description") int timeout) {
        handler.sendInfo(timeout, text);
    }

    @RuleAction(label = "@text/actions.enigma2.send-warning.label", description = "@text/actions.enigma2.send-warning.description")
    @SuppressWarnings("null")
    public void sendWarning(
            @ActionInput(name = "text", label = "@text/actions-input.enigma2.text.label", description = "@text/actions-input.enigma2.text.description") String text) {
        handler.sendWarning(Enigma2BindingConstants.MESSAGE_TIMEOUT, text);
    }

    @RuleAction(label = "@text/actions.enigma2.send-warning.label", description = "@text/actions.enigma2.send-warning.description")
    @SuppressWarnings("null")
    public void sendWarning(
            @ActionInput(name = "text", label = "@text/actions-input.enigma2.text.label", description = "@text/actions-input.enigma2.text.description") String text,
            @ActionInput(name = "timeout", label = "@text/actions-input.enigma2.timeout.label", description = "@text/actions-input.enigma2.timeout.description") int timeout) {
        handler.sendWarning(timeout, text);
    }

    @RuleAction(label = "@text/actions.enigma2.send-error.label", description = "@text/actions.enigma2.send-error.description")
    @SuppressWarnings("null")
    public void sendError(
            @ActionInput(name = "text", label = "@text/actions-input.enigma2.text.label", description = "@text/actions-input.enigma2.text.description") String text) {
        handler.sendError(Enigma2BindingConstants.MESSAGE_TIMEOUT, text);
    }

    @RuleAction(label = "@text/actions.enigma2.send-error.label", description = "@text/actions.enigma2.send-error.description")
    @SuppressWarnings("null")
    public void sendError(
            @ActionInput(name = "text", label = "@text/actions-input.enigma2.text.label", description = "@text/actions-input.enigma2.text.description") String text,
            @ActionInput(name = "timeout", label = "@text/actions-input.enigma2.timeout.label", description = "@text/actions-input.enigma2.timeout.description") int timeout) {
        handler.sendError(timeout, text);
    }

    @RuleAction(label = "@text/actions.enigma2.send-error.label", description = "@text/actions.enigma2.send-question.description")
    @SuppressWarnings("null")
    public void sendQuestion(
            @ActionInput(name = "text", label = "@text/actions-input.enigma2.text.label", description = "@text/actions-input.enigma2.text.description") String text) {
        handler.sendQuestion(Enigma2BindingConstants.MESSAGE_TIMEOUT, text);
    }

    @RuleAction(label = "@text/actions.enigma2.send-error.label", description = "@text/actions.enigma2.send-question.description")
    @SuppressWarnings("null")
    public void sendQuestion(
            @ActionInput(name = "text", label = "@text/actions-input.enigma2.text.label", description = "@text/actions-input.enigma2.text.description") String text,
            @ActionInput(name = "timeout", label = "@text/actions-input.enigma2.timeout.label", description = "@text/actions-input.enigma2.timeout.description") int timeout) {
        handler.sendQuestion(timeout, text);
    }

    // delegation methods for "legacy" rule support
    public static void sendRcCommand(ThingActions actions, String rcButton) {
        ((Enigma2Actions) actions).sendRcCommand(rcButton);
    }

    public static void sendInfo(ThingActions actions, String info) {
        ((Enigma2Actions) actions).sendInfo(info);
    }

    public static void sendInfo(ThingActions actions, String info, int timeout) {
        ((Enigma2Actions) actions).sendInfo(info, timeout);
    }

    public static void sendWarning(ThingActions actions, String warning) {
        ((Enigma2Actions) actions).sendWarning(warning);
    }

    public static void sendWarning(ThingActions actions, String warning, int timeout) {
        ((Enigma2Actions) actions).sendWarning(warning, timeout);
    }

    public static void sendError(ThingActions actions, String error) {
        ((Enigma2Actions) actions).sendError(error);
    }

    public static void sendError(ThingActions actions, String error, int timeout) {
        ((Enigma2Actions) actions).sendError(error, timeout);
    }

    public static void sendQuestion(ThingActions actions, String text) {
        ((Enigma2Actions) actions).sendQuestion(text);
    }

    public static void sendQuestion(ThingActions actions, String text, int timeout) {
        ((Enigma2Actions) actions).sendQuestion(text, timeout);
    }
}
