/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.robonect.internal.model.cmd;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Queries or controls the optional Robonect door system.
 *
 * @author Nico Brüttner - Initial contribution
 */
@NonNullByDefault
public class DoorCommand implements Command {
    public enum Direction {
        OPEN("open"),
        CLOSE("close");

        private final String parameter;

        /**
         * Creates a door direction with its web request parameter.
         *
         * @param parameter direction parameter name
         */
        Direction(String parameter) {
            this.parameter = parameter;
        }
    }

    private @Nullable Direction direction;
    private boolean release;

    /**
     * Sets the direction to open or close the door.
     *
     * @param direction the requested door direction
     * @return this command
     */
    public DoorCommand withDirection(Direction direction) {
        this.direction = direction;
        return this;
    }

    /**
     * Requests release of the door when no direction has been set.
     *
     * @return this command
     */
    public DoorCommand withRelease() {
        release = true;
        return this;
    }

    /**
     * Builds the door operation URL for this {@link Command}.
     *
     * @param baseUrl base URL to which the command parameters are appended
     * @return the complete request URL
     */
    @Override
    public String toCommandURL(String baseUrl) {
        Direction dir = direction;
        if (dir != null) {
            return baseUrl + "?cmd=door&" + dir.parameter + "=1";
        }
        return baseUrl + "?cmd=door" + (release ? "&release" : "");
    }
}
