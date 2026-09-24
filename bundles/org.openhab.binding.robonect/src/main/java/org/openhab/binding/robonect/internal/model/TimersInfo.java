/* Copyright (c) 2010-2026 Contributors to the openHAB project
 * SPDX-License-Identifier: EPL-2.0 */
package org.openhab.binding.robonect.internal.model;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Response to a timer list request.
 *
 * @author Nico Brüttner - Initial contribution
 */
@NonNullByDefault
public class TimersInfo extends RobonectAnswer {
    private @Nullable List<TimerFull> timer;

    public @Nullable List<TimerFull> getTimers() {
        return timer;
    }
}
