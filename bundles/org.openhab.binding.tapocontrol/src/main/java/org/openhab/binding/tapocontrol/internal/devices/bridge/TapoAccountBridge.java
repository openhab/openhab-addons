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
package org.openhab.binding.tapocontrol.internal.devices.bridge;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.tapocontrol.internal.helpers.TapoCredentials;
import org.openhab.binding.tplinkbridge.TPLinkAccount;
import org.openhab.binding.tplinkbridge.TPLinkCredentials;

/**
 * A shared {@code tplinkbridge:account} used as the parent of a Tapo device. The credentials are read from the
 * account on every call, so a changed account configuration applies at the device's next login.
 *
 * @author Christopher Smith - Initial contribution
 */
@NonNullByDefault
public record TapoAccountBridge(TPLinkAccount account, HttpClient httpClient) implements TapoDeviceBridge {

    @Override
    public TapoCredentials getCredentials() {
        TPLinkCredentials credentials = account.getCredentials();
        return new TapoCredentials(credentials.username(), credentials.password());
    }

    @Override
    public HttpClient getHttpClient() {
        return httpClient;
    }

    @Override
    public @Nullable TapoBridgeHandler getTapoBridge() {
        return null;
    }
}
