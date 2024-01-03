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
package org.openhab.binding.network.internal.action;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.network.internal.handler.NetworkHandler;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The class is responsible to call corresponding actions on {@link NetworkHandler}.
 *
 * @author Wouter Born - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = NetworkActions.class)
@ThingActionsScope(name = "network")
@NonNullByDefault
public class NetworkActions implements ThingActions {

    private final Logger logger = LoggerFactory.getLogger(NetworkActions.class);

    private @Nullable NetworkHandler handler;

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof NetworkHandler networkHandler) {
            this.handler = networkHandler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }

    /**
     * @deprecated Use sendWakeOnLanPacketViaMac or sendWakeOnLanPacketViaIp instead.
     */
    @Deprecated
    @RuleAction(label = "send a WoL packet", description = "Send a Wake-on-LAN packet to wake the device.")
    public void sendWakeOnLanPacket() {
        sendWakeOnLanPacketViaMac();
    }

    public static void sendWakeOnLanPacket(ThingActions actions) {
        ((NetworkActions) actions).sendWakeOnLanPacketViaMac();
    }

    @RuleAction(label = "send a WoL packet", description = "Send a Wake-on-LAN packet to wake the device via Mac.")
    public void sendWakeOnLanPacketViaMac() {
        NetworkHandler localHandler = handler;
        if (localHandler != null) {
            localHandler.sendWakeOnLanPacketViaMac();
        } else {
            logger.warn("Failed to send Wake-on-LAN packet (handler null)");
        }
    }

    public static void sendWakeOnLanPacketViaMac(ThingActions actions) {
        ((NetworkActions) actions).sendWakeOnLanPacketViaMac();
    }

    @RuleAction(label = "send a WoL packet", description = "Send a Wake-on-LAN packet to wake the device via IP.")
    public void sendWakeOnLanPacketViaIp() {
        NetworkHandler localHandler = handler;
        if (localHandler != null) {
            localHandler.sendWakeOnLanPacketViaIp();
        } else {
            logger.warn("Failed to send Wake-on-LAN packet (handler null)");
        }
    }

    public static void sendWakeOnLanPacketViaIp(ThingActions actions) {
        ((NetworkActions) actions).sendWakeOnLanPacketViaIp();
    }
}
