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
package org.openhab.binding.smhi.internal;

import static org.openhab.binding.smhi.internal.SmhiBindingConstants.*;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.types.State;

/**
 * A class containing a forecast for a specific point in time.
 *
 * @author Anders Alfredsson - Initial contribution
 */
@NonNullByDefault
public class Forecast implements Comparable<Forecast> {
    final ZonedDateTime time;
    final ZonedDateTime intervalStartTime;
    final Map<String, BigDecimal> parameters;

    public Forecast(ZonedDateTime time, ZonedDateTime intervalStartTime, Map<String, BigDecimal> parameters) {
        this.time = time;
        this.intervalStartTime = intervalStartTime;
        this.parameters = parameters;
    }

    public ZonedDateTime getTime() {
        return time;
    }

    public ZonedDateTime getIntervalStartTime() {
        return intervalStartTime;
    }

    public Map<String, BigDecimal> getParameters() {
        return parameters;
    }

    public BigDecimal getParameter(String parameter) {
        // TODO: Remove after 6.0 release
        parameter = PMP3G_BACKWARD_COMP.getOrDefault(parameter, parameter);

        return parameters.getOrDefault(parameter, DEFAULT_MISSING_VALUE);
    }

    public State getParameterAsState(String parameter) {
        return Util.getParameterAsState(parameter, getParameter(parameter));
    }

    @Override
    public int compareTo(Forecast o) {
        return time.compareTo(o.time);
    }
}
