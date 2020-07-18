/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.solaredge.internal.model;

import java.util.List;

/**
 * this class is used to map the aggregate data response of the public API
 *
 * @author Alexander Friese - initial contribution
 */
public class AggregateDataResponsePublicApi {

    public static class MeterTelemetry {
        public String date;
        public Double value;
    }

    public static class MeterTelemetries {
        public String type;
        public List<MeterTelemetry> values;
    }

    public static class EnergyDetails {
        public AggregatePeriod timeUnit;
        public String unit;
        public List<MeterTelemetries> meters;
    }

    private EnergyDetails energyDetails;

    public EnergyDetails getEnergyDetails() {
        return energyDetails;
    }

    public void setEnergyDetails(EnergyDetails energyDetails) {
        this.energyDetails = energyDetails;
    }
}
