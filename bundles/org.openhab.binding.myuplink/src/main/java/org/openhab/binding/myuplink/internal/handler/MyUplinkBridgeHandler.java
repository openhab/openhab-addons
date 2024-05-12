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
package org.openhab.binding.myuplink.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.binding.BridgeHandler;

/**
 * public interface of the {@link MyUplinkBridgeHandler}
 *
 * @author Alexander Friese - initial contribution
 */
@NonNullByDefault
public interface MyUplinkBridgeHandler extends BridgeHandler, MyUplinkThingHandler {

    /**
     * starts discovery of Nibe devices
     */
    void startDiscovery();
}
