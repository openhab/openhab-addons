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
package org.openhab.binding.lutron.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lutron.internal.protocol.LutronCommandNew;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.binding.BaseBridgeHandler;

/**
 * Abstract base class for Lutron bridge handlers
 *
 * @author Bob Adair - Initial contribution
 */
@NonNullByDefault
public abstract class LutronBridgeHandler extends BaseBridgeHandler {

    public LutronBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    public abstract void sendCommand(LutronCommandNew command);
}
