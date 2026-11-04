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
package org.openhab.binding.robonect.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.robonect.internal.model.ModelParser;
import org.openhab.binding.robonect.internal.model.TimerInfo;
import org.openhab.binding.robonect.internal.model.TimersInfo;
import org.openhab.binding.robonect.internal.model.cmd.TimerCommand;

/**
 * API operations for timer endpoints.
 *
 * @author Nico Brüttner - Initial contribution
 */
@NonNullByDefault
public final class RobonectTimerApi {
    private final RobonectTransport transport;

    /**
     * Creates an endpoint API using the shared transport.
     *
     * @param transport the transport used for requests and parsing
     */
    RobonectTimerApi(RobonectTransport transport) {
        this.transport = transport;
    }

    /**
     * Returns all timer configurations reported by the mower.
     *
     * @return the timer configurations including their success status
     */
    public TimersInfo getTimersInfo() {
        return ModelParser.parse(transport.sendCommand(new TimerCommand()), TimersInfo.class);
    }

    /**
     * Updates one value of an individual timer.
     *
     * @param timer non-negative timer number
     * @param parameter API parameter name
     * @param value API parameter value
     * @return the updated timer including its success status
     */
    public TimerInfo updateTimer(int timer, String parameter, String value) {
        return ModelParser.parse(transport.sendCommand(new TimerCommand().withTimer(timer).withValue(parameter, value)),
                TimerInfo.class);
    }
}
