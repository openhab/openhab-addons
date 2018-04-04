/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.innogysmarthome.internal.listener;

import org.openhab.binding.innogysmarthome.internal.InnogyWebSocket;

/**
 * The {@link EventListener} is called by the {@link InnogyWebSocket} on new Events and if the {@link InnogyWebSocket}
 * closed the connection.
 *
 * @author Oliver Kuhl - Initial contribution
 */
public interface EventListener {

    /**
     * This method is called, whenever a new event comes from the innogy service (like a device change for example).
     *
     * @param msg
     */
    public void onEvent(String msg);

    /**
     * This method is called, when the evenRunner stops abnormally (statuscode <> 1000).
     */
    public void connectionClosed();
}
