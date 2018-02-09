/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.kodi.internal.protocol;

import com.google.gson.JsonObject;

/**
 * This interface has to be implemented for classes which need to be able to receive events from KodiClientSocket
 *
 * @author Paul Frank
 *
 */
public interface KodiClientSocketEventListener {

    void handleEvent(JsonObject json);

    void onConnectionClosed();

    void onConnectionOpened();

}
