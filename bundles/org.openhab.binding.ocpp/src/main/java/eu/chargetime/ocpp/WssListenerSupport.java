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
package eu.chargetime.ocpp;

import org.eclipse.jdt.annotation.NonNullByDefault;

import eu.chargetime.ocpp.wss.WssFactoryBuilder;

/**
 * Bridges the ChargeTime library's package-private {@link WebSocketListener#enableWSS} so the binding
 * can serve OCPP over TLS while still composing the server itself.
 *
 * <p>
 * The library only exposes a public {@code enableWSS} on its {@code JSONServer}, which builds its own
 * server and does not allow injecting a promise repository. This binding composes {@code Server} +
 * {@link WebSocketListener} directly precisely so it can inject a request-timeout promise repository
 * the library otherwise lacks, and that composition has no public way to turn on TLS. This small
 * same-package accessor closes that gap without giving up the timeout hardening.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
public final class WssListenerSupport {

    private WssListenerSupport() {
    }

    /** Enables TLS on {@code listener} using {@code wssFactoryBuilder}; must be called before it opens. */
    public static void enableWss(WebSocketListener listener, WssFactoryBuilder wssFactoryBuilder) {
        listener.enableWSS(wssFactoryBuilder);
    }
}
