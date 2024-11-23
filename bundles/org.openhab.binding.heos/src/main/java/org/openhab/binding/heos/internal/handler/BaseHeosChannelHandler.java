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
package org.openhab.binding.heos.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.heos.internal.api.HeosFacade;
import org.openhab.binding.heos.internal.exception.HeosNotConnectedException;

/**
 * Base class for the channel handlers
 *
 * @author Martin van Wingerden - Initial contribution
 */
@NonNullByDefault
public abstract class BaseHeosChannelHandler implements HeosChannelHandler {
    final HeosBridgeHandler bridge;

    public BaseHeosChannelHandler(HeosBridgeHandler bridge) {
        this.bridge = bridge;
    }

    protected HeosFacade getApi() throws HeosNotConnectedException {
        return bridge.getApiConnection();
    }
}
