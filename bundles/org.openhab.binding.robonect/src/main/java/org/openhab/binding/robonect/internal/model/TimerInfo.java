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
package org.openhab.binding.robonect.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Response to an individual timer update.
 *
 * @author Nico Brüttner - Initial contribution
 */
@NonNullByDefault
public class TimerInfo extends RobonectAnswer {
    private @Nullable TimerFull timer;

    /**
     * Returns the timer returned by the request.
     *
     * @return the timer returned by the request, or {@code null} if not reported
     */
    public @Nullable TimerFull getTimer() {
        return timer;
    }
}
