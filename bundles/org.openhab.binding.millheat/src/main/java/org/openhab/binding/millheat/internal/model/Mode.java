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
package org.openhab.binding.millheat.internal.model;

import java.time.LocalDateTime;

/**
 * The {@link Mode} represents a mode with start and end time
 *
 * @author Arne Seime - Initial contribution
 */
public class Mode {
    private final ModeType mode;
    private final LocalDateTime start;
    private final LocalDateTime end;

    public Mode(final ModeType mode, final LocalDateTime start, final LocalDateTime end) {
        this.mode = mode;
        this.start = start;
        this.end = end;
    }

    public ModeType getMode() {
        return mode;
    }

    public LocalDateTime getStart() {
        return start;
    }

    public LocalDateTime getEnd() {
        return end;
    }
}
