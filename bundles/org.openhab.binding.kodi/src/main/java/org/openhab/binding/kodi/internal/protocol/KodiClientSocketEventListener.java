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
package org.openhab.binding.kodi.internal.protocol;

import com.google.gson.JsonObject;

/**
 * This interface has to be implemented for classes which need to be able to receive events from KodiClientSocket
 *
 * @author Paul Frank - Initial contribution
 */
public interface KodiClientSocketEventListener {

    void handleEvent(JsonObject json);

    void onConnectionClosed();

    void onConnectionOpened();
}
