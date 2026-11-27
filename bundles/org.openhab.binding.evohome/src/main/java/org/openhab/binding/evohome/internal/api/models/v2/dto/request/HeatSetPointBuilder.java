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
package org.openhab.binding.evohome.internal.api.models.v2.dto.request;

import java.time.Instant;

/**
 * Builder for heat set point API requests
 *
 * @author Jasper van Zuijlen - Initial contribution
 * @author Kovacs Istvan - Temporary override requests
 *
 */
public class HeatSetPointBuilder implements RequestBuilder<HeatSetPoint> {

    private double setPoint;
    private boolean hasSetPoint;
    private boolean cancelSetPoint;
    private Instant until;

    /**
     * Creates a new heat set point command
     *
     * @return A heat set point command or null when the configuration is invalid
     *
     */
    @Override
    public HeatSetPoint build() {
        if (cancelSetPoint) {
            return new HeatSetPoint();
        }
        if (hasSetPoint) {
            return until == null ? new HeatSetPoint(setPoint) : new HeatSetPoint(setPoint, until);
        }
        return null;
    }

    public HeatSetPointBuilder setSetPoint(double setPoint) {
        this.hasSetPoint = true;
        this.setPoint = setPoint;
        this.until = null;
        this.cancelSetPoint = false;
        return this;
    }

    public HeatSetPointBuilder setTemporarySetPoint(double setPoint, Instant until) {
        setSetPoint(setPoint);
        this.until = until;
        return this;
    }

    public HeatSetPointBuilder setCancelSetPoint() {
        cancelSetPoint = true;
        return this;
    }
}
