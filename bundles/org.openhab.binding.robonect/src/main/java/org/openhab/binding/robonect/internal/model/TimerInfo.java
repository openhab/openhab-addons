/* Copyright (c) 2010-2026 Contributors to the openHAB project
 * SPDX-License-Identifier: EPL-2.0 */
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

    public @Nullable TimerFull getTimer() {
        return timer;
    }
}
