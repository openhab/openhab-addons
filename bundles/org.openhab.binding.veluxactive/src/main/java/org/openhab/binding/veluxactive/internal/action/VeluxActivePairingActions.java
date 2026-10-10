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

    @RuleAction(label = "Pair Gateway", description = "Start pairing, then press the gateway button. Use pairingStatus to check progress.")
    public @ActionOutput(type = "java.lang.String", label = "Pairing Result") String pair(
            @ActionInput(name = "host", label = "Gateway IP Address", description = "The IP address of this gateway on the local network") String host) {
        VeluxActiveGatewayHandler gateway = handler;
        return gateway == null ? "UNAVAILABLE" : gateway.pair(host);
    }

    @RuleAction(label = "Pairing Status", description = "Read the result of the current gateway pairing attempt.")
    public @ActionOutput(type = "java.lang.String", label = "Pairing Status") String pairingStatus() {
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
