/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.evohome.internal.api.models.v2.request;

/**
 * Builder for heat set point API requests
 *
 * @author Jasper van Zuijlen - Initial contribution
 *
 */
public class HeatSetPointBuilder implements RequestBuilder<HeatSetPoint> {

    private double setPoint;
    private boolean hasSetPoint;
    private boolean cancelSetPoint;

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
            return new HeatSetPoint(setPoint);
        }
        return null;
    }

    public HeatSetPointBuilder setSetPoint(double setPoint) {
        this.hasSetPoint = true;
        this.setPoint = setPoint;
        return this;
    }

    public HeatSetPointBuilder setCancelSetPoint() {
        cancelSetPoint = true;
        return this;
    }

}
