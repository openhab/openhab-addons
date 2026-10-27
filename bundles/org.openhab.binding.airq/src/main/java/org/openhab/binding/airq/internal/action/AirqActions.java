/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.airq.internal.action;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.airq.internal.AirqHandler;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.ActionOutput;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Thing actions of the air-Q binding.
 *
 * @author Michael Weger - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = AirqActions.class)
@ThingActionsScope(name = "airq")
@NonNullByDefault
public class AirqActions implements ThingActions {

    private final Logger logger = LoggerFactory.getLogger(AirqActions.class);

    private @Nullable AirqHandler handler;

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof AirqHandler airqHandler) {
            this.handler = airqHandler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }

    @RuleAction(label = "@text/action.set-wifi.label", description = "@text/action.set-wifi.description")
    public @ActionOutput(name = "success", type = "java.lang.Boolean", label = "@text/action.set-wifi.output.success.label") boolean setWifi(
            @ActionInput(name = "ssid", label = "@text/action.set-wifi.input.ssid.label", description = "@text/action.set-wifi.input.ssid.description") @Nullable String ssid,
            @ActionInput(name = "password", label = "@text/action.set-wifi.input.password.label", description = "@text/action.set-wifi.input.password.description") @Nullable String password,
            @ActionInput(name = "bssid", label = "@text/action.set-wifi.input.bssid.label", description = "@text/action.set-wifi.input.bssid.description") @Nullable String bssid,
            @ActionInput(name = "hidden", label = "@text/action.set-wifi.input.hidden.label", description = "@text/action.set-wifi.input.hidden.description") boolean hidden,
            @ActionInput(name = "restart", label = "@text/action.set-wifi.input.restart.label", description = "@text/action.set-wifi.input.restart.description") boolean restart) {
        AirqHandler airqHandler = handler;
        if (airqHandler == null) {
            logger.warn("setWifi called without an air-Q Thing");
            return false;
        }
        if (ssid == null || password == null) {
            logger.warn("Not setting Wi-Fi: the SSID and the password are required, the password may be empty");
            return false;
        }
        return airqHandler.setWifi(ssid, password, bssid, hidden, restart);
    }

    public static boolean setWifi(ThingActions actions, @Nullable String ssid, @Nullable String password,
            @Nullable String bssid, boolean hidden, boolean restart) {
        return ((AirqActions) actions).setWifi(ssid, password, bssid, hidden, restart);
    }
}
