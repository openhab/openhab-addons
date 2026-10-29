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
package org.openhab.binding.yioremote.internal.utils;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link WebsocketInterface} receives the events of a {@link Websocket} connection. The source connection is
 * passed along so that events of an outdated connection can be ignored.
 *
 * @author Michael Loercher - Initial contribution
 */
@NonNullByDefault
public interface WebsocketInterface {

    void onConnect(Websocket source);

    void onClose(Websocket source, int statusCode, @Nullable String reason);

    void onMessage(Websocket source, String message);

    void onError(Websocket source, Throwable cause);
}
