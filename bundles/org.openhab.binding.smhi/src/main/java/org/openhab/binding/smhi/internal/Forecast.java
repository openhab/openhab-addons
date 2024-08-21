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
package org.openhab.binding.smhi.internal;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * A class containing a forecast for a specific point in time.
 *
 * @author Anders Alfredsson - Initial contribution
 */
@NonNullByDefault
public class Forecast implements Comparable<Forecast> {
    private final ZonedDateTime validTime;
    private final Map<String, BigDecimal> parameters;

    public Forecast(ZonedDateTime validTime, Map<String, BigDecimal> parameters) {
        this.validTime = validTime;
        this.parameters = parameters;
    }

    public ZonedDateTime getValidTime() {
        return validTime;
    }

    public Map<String, BigDecimal> getParameters() {
        return parameters;
    }

    public Optional<BigDecimal> getParameter(String parameter) {
        return Optional.ofNullable(parameters.get(parameter));
    }

    @Override
    public int compareTo(Forecast o) {
        return validTime.compareTo(o.validTime);
    }
}
