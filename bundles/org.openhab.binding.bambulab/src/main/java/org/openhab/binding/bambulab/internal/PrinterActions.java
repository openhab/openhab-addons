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
package org.openhab.binding.bambulab.internal;

import static java.util.Objects.requireNonNull;
import static org.openhab.binding.bambulab.internal.BambuLabBindingConstants.BINDING_ID;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * @author Martin Grzeslowski - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = PrinterActions.class)
@ThingActionsScope(name = BINDING_ID)
@NonNullByDefault
public class PrinterActions implements ThingActions {
    private @Nullable PrinterHandler handler;

    @Override
    public void setThingHandler(ThingHandler thingHandler) {
        this.handler = (PrinterHandler) thingHandler;
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }

    @RuleAction(label = "@text/action.sendCommand.label", description = "@text/action.sendCommand.description")
    public void sendCommand(
            @ActionInput(name = "stringCommand", label = "@text/action.sendCommand.commandLabel", description = "@text/action.sendCommand.commandDescription") String stringCommand) {
        requireNonNull(handler).sendCommand(stringCommand);
    }

    public static void sendCommand(@Nullable ThingActions actions, String stringCommand) {
        ((PrinterActions) requireNonNull(actions)).sendCommand(stringCommand);
    }

    @RuleAction(label = "@text/action.refreshChannels.label", description = "@text/action.refreshChannels.description")
    public void refreshChannels() {
        requireNonNull(handler).refreshChannels();
    }

    public static void refreshChannels(@Nullable ThingActions actions) {
        ((PrinterActions) requireNonNull(actions)).refreshChannels();
    }

    @RuleAction(label = "@text/action.requestLoginCode.label", description = "@text/action.requestLoginCode.description")
    public void requestLoginCode(
            @ActionInput(name = "username", label = "@text/action.requestLoginCode.username", description = "@text/action.requestLoginCode.username.description") String username,
            @ActionInput(name = "password", label = "@text/action.requestLoginCode.password", description = "@text/action.requestLoginCode.password.description") String password)
            throws BambuApiException {
        requireNonNull(handler).requestLoginCode(username, password);
    }

    public static void requestLoginCode(@Nullable ThingActions actions, String username, String password)
            throws BambuApiException {
        ((PrinterActions) requireNonNull(actions)).requestLoginCode(username, password);
    }

    @RuleAction(label = "@text/action.requestAccessCode.label", description = "@text/action.requestAccessCode.description")
    public String requestAccessCode(
            @ActionInput(name = "username", label = "@text/action.requestAccessCode.username", description = "@text/action.requestAccessCode.username.description") String username,
            @ActionInput(name = "code", label = "@text/action.requestAccessCode.code", description = "@text/action.requestAccessCode.code.description") String code)
            throws BambuApiException {
        return requireNonNull(handler).requestAccessCode(username, code);
    }

    public static String requestAccessCode(@Nullable ThingActions actions, String username, String code)
            throws BambuApiException {
        return ((PrinterActions) requireNonNull(actions)).requestAccessCode(username, code);
    }
}
