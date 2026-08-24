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
 * Same-package accessor for the library's package-private {@link WebSocketListener#enableWSS}.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
public final class WssListenerSupport {

    private WssListenerSupport() {
    }

    public static void enableWss(WebSocketListener listener, WssFactoryBuilder wssFactoryBuilder) {
        listener.enableWSS(wssFactoryBuilder);
    }
}
