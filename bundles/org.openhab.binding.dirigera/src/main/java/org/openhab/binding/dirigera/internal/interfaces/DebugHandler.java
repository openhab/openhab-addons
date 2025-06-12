/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.dirigera.internal.interfaces;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.binding.ThingHandler;

/**
 * {@link DebugHandler} interface to control debugging via rule actions
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public interface DebugHandler extends ThingHandler {

    /**
     * Returns the token associated with the DIRIGERA gateway. Regardless on which device this action is called the
     * token from gateway (bridge) is returned.
     *
     * @return token as String
     */
    String getToken();

    /**
     * Returns the JSON representation at this time for a specific device. If action is called on gateway a snapshot
     * from all connected devices is returned.
     *
     * @return device JSON at this time
     */
    String getJSON();

    /**
     * Enables / disables debug for one specific device. If enabled messages are logged on info level regarding
     * - commands send via openHAB
     * - state updates of openHAB
     * - API requests with payload towards gateway
     * - push notifications from gateway
     * - API responses from gateway
     *
     * @param debug boolean flag enabling or disabling debug messages
     */
    void setDebug(boolean debug, boolean all);

    /**
     * Returns the device ID of the device this handler is associated with.
     *
     * @return device ID as String
     */
    String getDeviceId();
}
