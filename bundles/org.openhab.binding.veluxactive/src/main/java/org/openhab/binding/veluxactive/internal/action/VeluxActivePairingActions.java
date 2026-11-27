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
package org.openhab.binding.veluxactive.internal.action;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.veluxactive.internal.handler.VeluxActiveGatewayHandler;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.ActionOutput;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * Gateway actions for physical-button-authorized signing key retrieval.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
@Component(scope = ServiceScope.PROTOTYPE, service = VeluxActivePairingActions.class)
@ThingActionsScope(name = "veluxactive")
public class VeluxActivePairingActions implements ThingActions {
    private volatile @Nullable VeluxActiveGatewayHandler handler;

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        this.handler = handler instanceof VeluxActiveGatewayHandler gateway ? gateway : null;
    }

    @RuleAction(label = "@text/action.gateway.pair.label", description = "@text/action.gateway.pair.description")
    public @ActionOutput(type = "java.lang.String", label = "@text/action.gateway.pair.output.label") String pair(
            @ActionInput(name = "host", label = "@text/action.gateway.pair.host.label", description = "@text/action.gateway.pair.host.description") String host) {
        VeluxActiveGatewayHandler gateway = handler;
        return gateway == null ? "UNAVAILABLE" : gateway.pair(host);
    }

    @RuleAction(label = "@text/action.gateway.pairing-status.label", description = "@text/action.gateway.pairing-status.description")
    public @ActionOutput(type = "java.lang.String", label = "@text/action.gateway.pairing-status.output.label") String pairingStatus() {
        VeluxActiveGatewayHandler gateway = handler;
        return gateway == null ? "UNAVAILABLE" : gateway.pairingStatus();
    }

    public static String pair(ThingActions actions, String host) {
        if (actions instanceof VeluxActivePairingActions pairing) {
            return pairing.pair(host);
        }
        throw new IllegalArgumentException("Expected VELUX ACTIVE gateway actions.");
    }

    public static String pairingStatus(ThingActions actions) {
        if (actions instanceof VeluxActivePairingActions pairing) {
            return pairing.pairingStatus();
        }
        throw new IllegalArgumentException("Expected VELUX ACTIVE gateway actions.");
    }
}
