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
package org.openhab.binding.matter.internal.controller.devices.converter;

import static org.openhab.binding.matter.internal.MatterBindingConstants.*;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.Pm1ConcentrationMeasurementCluster;
import org.openhab.binding.matter.internal.handler.MatterBaseThingHandler;
import org.openhab.core.library.unit.Units;

/**
 * A converter for translating {@link Pm1ConcentrationMeasurementCluster} events and attributes to openHAB
 * channels.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class Pm1ConcentrationMeasurementConverter
        extends AbstractConcentrationMeasurementConverter<Pm1ConcentrationMeasurementCluster> {

    public Pm1ConcentrationMeasurementConverter(Pm1ConcentrationMeasurementCluster cluster,
            MatterBaseThingHandler handler, int endpointNumber, String labelPrefix) {
        super(cluster, handler, endpointNumber, labelPrefix, CHANNEL_ID_PM1CONCENTRATIONMEASUREMENT_MEASUREDVALUE,
                CHANNEL_CONCENTRATIONMEASUREMENT_MEASUREDVALUE, CHANNEL_LABEL_PM1CONCENTRATIONMEASUREMENT_MEASUREDVALUE,
                CHANNEL_DESC_PM1CONCENTRATIONMEASUREMENT_MEASUREDVALUE,
                CHANNEL_ID_PM1CONCENTRATIONMEASUREMENT_LEVELVALUE, CHANNEL_CONCENTRATIONMEASUREMENT_LEVELVALUE,
                CHANNEL_ID_PM1CONCENTRATIONMEASUREMENT_PEAKMEASUREDVALUE,
                CHANNEL_CONCENTRATIONMEASUREMENT_PEAKMEASUREDVALUE,
                CHANNEL_ID_PM1CONCENTRATIONMEASUREMENT_AVERAGEMEASUREDVALUE,
                CHANNEL_CONCENTRATIONMEASUREMENT_AVERAGEMEASUREDVALUE);
    }

    @Override
    protected Unit<?> getDefaultUnit() {
        return Units.MICROGRAM_PER_CUBICMETRE;
    }

    @Override
    protected boolean hasLevelIndication() {
        return initializingCluster.featureMap != null && initializingCluster.featureMap.levelIndication;
    }

    @Override
    protected boolean hasPeakMeasurement() {
        return initializingCluster.featureMap != null && initializingCluster.featureMap.peakMeasurement;
    }

    @Override
    protected boolean hasAverageMeasurement() {
        return initializingCluster.featureMap != null && initializingCluster.featureMap.averageMeasurement;
    }
}
