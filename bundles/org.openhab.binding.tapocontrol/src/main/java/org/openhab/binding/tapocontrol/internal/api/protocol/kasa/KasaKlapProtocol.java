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
package org.openhab.binding.tapocontrol.internal.api.protocol.kasa;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.tapocontrol.internal.api.TapoDeviceConnector;
import org.openhab.binding.tapocontrol.internal.api.protocol.klap.KlapProtocol;
import org.openhab.binding.tapocontrol.internal.helpers.TapoCredentials;
import org.openhab.binding.tapocontrol.internal.helpers.TapoErrorHandler;

/**
 * Carries the Kasa IOT JSON commands over a KLAP session. Kasa devices with firmware that secures the legacy XOR
 * protocol (discovery reports an {@code IOT.*} device type with KLAP encryption) only accept this combination.
 *
 * @author Christopher Smith - Initial contribution
 */
@NonNullByDefault
public class KasaKlapProtocol extends KasaIotProtocol {
    /**
     * Kasa devices set up in the Kasa app may hold a KLAP key derived from the account e-mail and this fixed password
     * instead of the account password.
     */
    static final String KASA_SETUP_PASSWORD = "kasaSetup";

    private final KlapProtocol klap;

    public KasaKlapProtocol(TapoDeviceConnector connector, boolean dimmer) {
        this(connector, dimmer, new KlapProtocol(connector));
    }

    KasaKlapProtocol(TapoDeviceConnector connector, boolean dimmer, KlapProtocol klap) {
        super(connector, dimmer);
        this.klap = klap;
    }

    @Override
    protected boolean connect(TapoCredentials credentials) throws TapoErrorHandler {
        return klap.login(List.of(credentials, new TapoCredentials(credentials.username(), KASA_SETUP_PASSWORD)));
    }

    @Override
    protected void disconnect() {
        klap.logout();
    }

    @Override
    protected boolean isConnected() {
        return klap.isLoggedIn();
    }

    @Override
    protected String sendCommand(String command, long requestSession) throws TapoErrorHandler {
        synchronized (sessionLock) {
            requireActiveSessionLocked(requestSession);
        }
        return klap.exchange(command);
    }
}
