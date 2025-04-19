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
package org.openhab.binding.dirigera.internal.handler.light;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.json.JSONObject;

/**
 * The {@link LightCommand} is holding all information to execute a new light command
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class LightCommand {
    public enum Action {
        ON,
        BRIGHTNESS,
        TEMPERATURE,
        COLOR,
        OFF
    }

    public JSONObject request;
    public Action action;

    public LightCommand(JSONObject request, Action action) {
        this.request = request;
        this.action = action;
    }

    /**
     * Link updates are equal because they are generic, all others false
     *
     * @param other
     * @return
     */
    @Override
    public boolean equals(@Nullable Object other) {
        return (other instanceof LightCommand command && action.equals(command.action));
    }

    @Override
    public String toString() {
        return this.action + ": " + this.request.toString();
    }
}
