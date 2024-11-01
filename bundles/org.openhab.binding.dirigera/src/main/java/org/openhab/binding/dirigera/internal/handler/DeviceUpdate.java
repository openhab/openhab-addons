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
package org.openhab.binding.dirigera.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link DeviceUpdate} element handled in device update queue
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class DeviceUpdate {
    public enum Action {
        ADD,
        DISPOSE,
        REMOVE;
    }

    public BaseHandler handler;
    public String deviceId;
    public Action action;

    public DeviceUpdate(BaseHandler handler, String deviceId, Action action) {
        this.handler = handler;
        this.deviceId = deviceId;
        this.action = action;
    }
}
