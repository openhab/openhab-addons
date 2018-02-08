/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.openwebnet.internal.listener;

import java.util.EventListener;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link GatewayListener} interface define the callback required to be implemented to monitor SerialGateway class.
 *
 * @author Antoine Laydier
 *
 */
@NonNullByDefault
public interface GatewayListener extends EventListener {
    /**
     * Called when SerialGateway get connected
     */
    void onConnect();

    /**
     * Called when SerialGateway get disconnected
     */

    void onDisconnect();
}
