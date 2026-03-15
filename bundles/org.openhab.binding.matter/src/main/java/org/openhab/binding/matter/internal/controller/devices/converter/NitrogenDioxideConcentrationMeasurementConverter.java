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
import org.openhab.binding.matter.internal.client.dto.cluster.gen.NitrogenDioxideConcentrationMeasurementCluster;
import org.openhab.binding.matter.internal.handler.MatterBaseThingHandler;
import org.openhab.core.library.unit.Units;

/**
 * A converter for translating {@link NitrogenDioxideConcentrationMeasurementCluster} events and attributes to openHAB
 * channels.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class NitrogenDioxideConcentrationMeasurementConverter
        extends AbstractConcentrationMeasurementConverter<NitrogenDioxideConcentrationMeasurementCluster> {

    public NitrogenDioxideConcentrationMeasurementConverter(NitrogenDioxideConcentrationMeasurementCluster cluster,
            MatterBaseThingHandler handler, int endpointNumber, String labelPrefix) {
        super(cluster, handler, endpointNumber, labelPrefix,
                CHANNEL_ID_NITROGENDIOXIDECONCENTRATIONMEASUREMENT_MEASUREDVALUE,
                CHANNEL_CONCENTRATIONMEASUREMENT_MEASUREDVALUE,
                CHANNEL_LABEL_NITROGENDIOXIDECONCENTRATIONMEASUREMENT_MEASUREDVALUE,
                CHANNEL_DESC_NITROGENDIOXIDECONCENTRATIONMEASUREMENT_MEASUREDVALUE,
                CHANNEL_ID_NITROGENDIOXIDECONCENTRATIONMEASUREMENT_LEVELVALUE,
                CHANNEL_CONCENTRATIONMEASUREMENT_LEVELVALUE,
                CHANNEL_ID_NITROGENDIOXIDECONCENTRATIONMEASUREMENT_PEAKMEASUREDVALUE,
                CHANNEL_CONCENTRATIONMEASUREMENT_PEAKMEASUREDVALUE,
                CHANNEL_ID_NITROGENDIOXIDECONCENTRATIONMEASUREMENT_AVERAGEMEASUREDVALUE,
                CHANNEL_CONCENTRATIONMEASUREMENT_AVERAGEMEASUREDVALUE);
    }

    @Override
    protected Unit<?> getDefaultUnit() {
        return Units.PARTS_PER_MILLION;
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
