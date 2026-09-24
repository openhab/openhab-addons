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

import org.eclipse.jdt.annotation.Nullable;

/** Queries or controls the optional Robonect door system. */
public class DoorCommand implements Command {
    public enum Direction {
        OPEN("open"),
        CLOSE("close");

        private final String parameter;

        Direction(String parameter) {
            this.parameter = parameter;
        }
    }

    private @Nullable Direction direction;
    private boolean release;

    public DoorCommand withDirection(Direction direction) {
        this.direction = direction;
        return this;
    }

    public DoorCommand withRelease() {
        release = true;
        return this;
    }

    @Override
    public String toCommandURL(String baseUrl) {
        if (direction != null) {
            return baseUrl + "?cmd=door&" + direction.parameter + "=1";
        }
        return baseUrl + "?cmd=door" + (release ? "&release" : "");
    }
}
