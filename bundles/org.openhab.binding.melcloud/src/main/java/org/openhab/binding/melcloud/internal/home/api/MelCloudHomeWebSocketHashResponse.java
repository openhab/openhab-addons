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
package org.openhab.binding.melcloud.internal.home.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Gson deserialization target for the JSON body returned by the MELCloud Home WebSocket token endpoint (a fixed AWS
 * Lambda Function URL, authenticated with the mobile-BFF Bearer access token): {@code {"hash": "...", "userId":
 * "..."}}. The {@code hash} is the short-lived credential appended as a query parameter when opening the
 * {@code wss://ws.melcloudhome.com} connection.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class MelCloudHomeWebSocketHashResponse {

    public @Nullable String hash;
    public @Nullable String userId;
}
