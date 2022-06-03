/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.easee.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.binding.BridgeHandler;

/**
 * public interface of the {@link EaseeBridgeHandler}
 *
 * @author Alexander Friese - initial contribution
 */
@NonNullByDefault
public interface EaseeBridgeHandler extends BridgeHandler, EaseeThingHandler {

    /**
     * starts discovery of wallboxes and circuits
     */
    void startDiscovery();
}
