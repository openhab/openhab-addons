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

import static org.openhab.binding.dirigera.internal.Constants.THING_TYPE_UNKNNOWN;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.internal.ThingImpl;

/**
 * The {@link DeviceUpdate} element handled in device update queue
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class DeviceUpdate {
    public static final BaseHandler DUMMY_HANDLER = new BaseHandler(new ThingImpl(THING_TYPE_UNKNNOWN, ""), Map.of());

    public enum Action {
        ADD,
        DISPOSE,
        REMOVE,
        LINKS;
    }

    public BaseHandler handler = DUMMY_HANDLER;
    public String deviceId;
    public Action action;

    public DeviceUpdate(@Nullable BaseHandler handler, String deviceId, Action action) {
        if (handler != null) {
            this.handler = handler;
        }
        this.deviceId = deviceId;
        this.action = action;
    }

    /**
     * Link updates are equal because they are generic, all others false
     *
     * @param other
     * @return
     */
    public boolean equals(DeviceUpdate other) {
        return (this.action.equals(other.action)) && handler.equals(handler) && this.deviceId.equals(other.deviceId);
    }
}
