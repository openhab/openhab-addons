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
package org.openhab.binding.dirigera.internal.handler.light;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.json.JSONObject;

/**
 * The {@link LightUpdate} element handled in light update queue
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class LightUpdate {
    public enum Action {
        COLOR,
        BRIGHTNESS,
    }

    public JSONObject request;
    public Action action;

    public LightUpdate(JSONObject request, Action action) {
        this.request = request;
        this.action = action;
    }

    /**
     * Link updates are equal because they are generic, all others false
     *
     * @param other
     * @return
     */
    public boolean equals(LightUpdate other) {
        return action.equals(other.action);
    }
}
