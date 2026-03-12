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
package org.openhab.binding.sensorcommunity.internal.handler;

import static org.openhab.binding.sensorcommunity.internal.SensorCommunityBindingConstants.*;
import static org.openhab.binding.sensorcommunity.internal.utils.Constants.*;

import java.util.List;

import javax.measure.MetricPrefix;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.sensorcommunity.internal.config.RadiationConfiguration;
import org.openhab.binding.sensorcommunity.internal.dto.SensorDataValue;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * The {@link RadiationHandler} is responsible for handling radiation values
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class RadiationHandler extends BaseSensorHandler {
    private static final double[] THRESHOLDS = { 0.15, 0.4, 1.0, 100.0 };

    private RadiationConfiguration radiationConfig = new RadiationConfiguration();
    private State radiationCache = UnDefType.UNDEF;
    private State radiationLevelCache = UnDefType.UNDEF;
    private State radiationCPMCache = UnDefType.UNDEF;
    private State radiationPulseCache = UnDefType.UNDEF;

    public RadiationHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        super.initialize();
        radiationConfig = getConfigAs(RadiationConfiguration.class);
    }

    @Override
    public UpdateStatus updateChannels(@Nullable String json) {
        if (json != null) {
            List<SensorDataValue> valueList = HTTPHandler.getHandler().getLatestValues(json);
            if (valueList != null) {
                if (HTTPHandler.getHandler().isRadiation(valueList)) {
                    valueList.forEach(v -> {
                        if (v.getValueType().equals(RADIATION_CPM)) {
                            int cpm = Integer.parseInt(v.getValue());
                            double microSievert = cpm * radiationConfig.conversionFactor;
                            radiationCache = QuantityType.valueOf(microSievert, MetricPrefix.MICRO(Units.SIEVERT));
                            radiationCPMCache = new DecimalType(cpm);
                            radiationLevelCache = new DecimalType(getRadiationLevel(microSievert));
                            updateState(RADIATION_CHANNEL, radiationCache);
                            updateState(RADIATION_CPM_CHANNEL, radiationCPMCache);
                            updateState(RADIATION_LEVEL_CHANNEL, radiationLevelCache);
                        }
                        if (v.getValueType().equals(RADIATION_PULSES)) {
                            radiationPulseCache = new DecimalType(Integer.parseInt(v.getValue()));
                            updateState(RADIATION_PULSE_CHANNEL, radiationPulseCache);
                        }
                    });
                    return UpdateStatus.OK;
                } else {
                    return UpdateStatus.VALUE_ERROR;
                }
            } else {
                return UpdateStatus.VALUE_EMPTY;
            }
        } else

        {
            return UpdateStatus.CONNECTION_ERROR;
        }
    }

    // see https://community.openhab.org/t/adding-counts-per-minute-to-the-sensor-community-binding/168748/4
    private int getRadiationLevel(double microSievert) {
        for (int i = 0; i < THRESHOLDS.length; i++) {
            if (microSievert < THRESHOLDS[i]) {
                return i;
            }
        }
        return THRESHOLDS.length;
    }

    @Override
    protected void updateFromCache() {
        updateState(RADIATION_CHANNEL, radiationCache);
        updateState(RADIATION_CPM_CHANNEL, radiationCPMCache);
        updateState(RADIATION_LEVEL_CHANNEL, radiationLevelCache);
        updateState(RADIATION_PULSE_CHANNEL, radiationPulseCache);
    }
}
