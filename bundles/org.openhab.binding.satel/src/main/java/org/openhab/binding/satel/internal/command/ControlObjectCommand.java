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
package org.openhab.binding.satel.internal.command;

import java.util.BitSet;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.satel.internal.event.EventDispatcher;
import org.openhab.binding.satel.internal.event.NewStatesEvent;
import org.openhab.binding.satel.internal.types.ControlType;

/**
 * Command class for commands that control (change) state of Integra objects,
 * like partitions (arm, disarm), zones (bypass, unbypass) outputs (on, off,
 * switch), etc.
 *
 * @author Krzysztof Goworek - Initial contribution
 */
@NonNullByDefault
public class ControlObjectCommand extends ControlCommand {

    private static final long REFRESH_DELAY = 1000;

    private ControlType controlType;
    private ScheduledExecutorService scheduler;

    /**
     * Creates new command class instance for specified type of control.
     *
     * @param controlType type of controlled objects
     * @param objects bits that represents objects to control
     * @param userCode code of the user on behalf the control is made
     * @param scheduler scheduler object for scheduling refreshes
     */
    public ControlObjectCommand(ControlType controlType, byte[] objects, String userCode,
            ScheduledExecutorService scheduler) {
        super(controlType.getControlCommand(), objects, userCode);
        this.controlType = controlType;
        this.scheduler = scheduler;
    }

    @Override
    protected void handleResponseInternal(final EventDispatcher eventDispatcher) {
        // force refresh states that might have changed
        final BitSet newStates = controlType.getControlledStates();
        if (!newStates.isEmpty()) {
            // add delay to give a chance to process sent command
            scheduler.schedule(() -> eventDispatcher.dispatchEvent(new NewStatesEvent(newStates)), REFRESH_DELAY,
                    TimeUnit.MILLISECONDS);
        }
    }
}
