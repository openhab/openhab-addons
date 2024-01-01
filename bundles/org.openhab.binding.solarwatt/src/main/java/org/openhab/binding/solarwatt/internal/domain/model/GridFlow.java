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
package org.openhab.binding.solarwatt.internal.domain.model;

import static org.openhab.binding.solarwatt.internal.SolarwattBindingConstants.CHANNEL_CURRENT_LIMIT;
import static org.openhab.binding.solarwatt.internal.SolarwattBindingConstants.CHANNEL_FEED_IN_LIMIT;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.solarwatt.internal.domain.dto.DeviceDTO;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

/**
 * Class planning the energy flow out/into the grid.
 *
 * This fields have been identified to exist:
 * com.kiwigrid.kiwiapp.gridflow.GridFlow=[
 * ConfigTimeshifting,
 * ConfigEvStationControl,
 * FractionPVTestLimit,
 * ConfigInverterControl,
 * LogLevel,
 * PowerSetpoint,
 * ConfigPeakshaving,
 * ToEMRequestTag,
 * CurrentLimit,
 * ConfigBatteryControl,
 * ConfigForecastBatteryControl,
 * ConfigEvStationChargingControl,
 * ConfigSgReady,
 * ToCloudDataTag
 * ]
 *
 * @author Sven Carstens - Initial contribution
 */
@NonNullByDefault
public class GridFlow extends Device {
    public static final String SOLAR_WATT_CLASSNAME = "com.kiwigrid.kiwiapp.gridflow.GridFlow";
    private final Logger logger = LoggerFactory.getLogger(GridFlow.class);

    private @Nullable ConfigInverterControl configInverterControl;

    public GridFlow(DeviceDTO deviceDTO) {
        super(deviceDTO);
    }

    @Override
    public void update(DeviceDTO deviceDTO) {
        super.update(deviceDTO);

        this.addAmpereQuantity(CHANNEL_CURRENT_LIMIT, deviceDTO, true);

        try {
            JsonObject rawConfigInverterControl = deviceDTO.getJsonObjectFromTag("ConfigInverterControl");
            Gson gson = new GsonBuilder().create();
            this.configInverterControl = gson.fromJson(rawConfigInverterControl, GridFlow.ConfigInverterControl.class);
        } catch (Exception ex) {
            this.configInverterControl = null;
            this.logger.warn("Could not read ConfigInverterControl", ex);
        }

        GridFlow.ConfigInverterControl localConfigInverterControl = this.configInverterControl;
        if (localConfigInverterControl != null) {
            // the only interesting value is the derailing (i.e. limit power flowing into the grid)
            BigDecimal feedInLimit = localConfigInverterControl.getFeedInLimit();
            if (feedInLimit != null) {
                QuantityType<?> state = new QuantityType<>(feedInLimit.multiply(BigDecimal.valueOf(100)),
                        Units.PERCENT);
                this.stateValues.put(CHANNEL_FEED_IN_LIMIT.getChannelName(), state);

                this.addChannel(CHANNEL_FEED_IN_LIMIT.getChannelName(), Units.PERCENT, "status", false);
            }
        }
    }

    @Override
    protected String getSolarWattLabel() {
        return "GridFlow";
    }

    public static class ConfigInverterControl {
        private @Nullable BigDecimal testModeFeedInLimit;
        private @Nullable ControllerParameter controllerParameter;
        private @Nullable Boolean dynamicControllingOn;
        private @Nullable BigDecimal feedInLimit;
        private @Nullable DynamicControllerParameter dynamicControllerParameter;
        private @Nullable Boolean on;
        private @Nullable Boolean isErrorPVLimiting;
        private @Nullable Boolean activeTestMode;
        private @Nullable Boolean isPVPlantConfigured;
        private @Nullable String selectedRcr;
        private @Nullable Boolean limitByRcr;

        public @Nullable BigDecimal getFeedInLimit() {
            return this.feedInLimit;
        }

        public @Nullable BigDecimal getTestModeFeedInLimit() {
            return this.testModeFeedInLimit;
        }

        public @Nullable ControllerParameter getControllerParameter() {
            return this.controllerParameter;
        }

        public @Nullable Boolean getDynamicControllingOn() {
            return this.dynamicControllingOn;
        }

        public @Nullable DynamicControllerParameter getDynamicControllerParameter() {
            return this.dynamicControllerParameter;
        }

        public @Nullable Boolean getOn() {
            return this.on;
        }

        public @Nullable Boolean getErrorPVLimiting() {
            return this.isErrorPVLimiting;
        }

        public @Nullable Boolean getActiveTestMode() {
            return this.activeTestMode;
        }

        public @Nullable Boolean getPVPlantConfigured() {
            return this.isPVPlantConfigured;
        }

        public @Nullable String getSelectedRcr() {
            return this.selectedRcr;
        }

        public @Nullable Boolean getLimitByRcr() {
            return this.limitByRcr;
        }

        public static class ControllerParameter {
            private @Nullable BigDecimal integrationRate;
            private @Nullable BigInteger outputRampRateLimit;
            private @Nullable BigDecimal differentialRate;
            private @Nullable BigDecimal proportionalRate;
            private @Nullable BigInteger outputValueLimit;
            private @Nullable BigInteger controlFaultSumLimit;

            public @Nullable BigDecimal getIntegrationRate() {
                return this.integrationRate;
            }

            public @Nullable BigInteger getOutputRampRateLimit() {
                return this.outputRampRateLimit;
            }

            public @Nullable BigDecimal getDifferentialRate() {
                return this.differentialRate;
            }

            public @Nullable BigDecimal getProportionalRate() {
                return this.proportionalRate;
            }

            public @Nullable BigInteger getOutputValueLimit() {
                return this.outputValueLimit;
            }

            public @Nullable BigInteger getControlFaultSumLimit() {
                return this.controlFaultSumLimit;
            }
        }

        public static class DynamicControllerParameter {
            private @Nullable BigDecimal integrationRate;
            private @Nullable BigInteger outputRampRateLimit;
            private @Nullable BigDecimal differentialRate;
            private @Nullable BigDecimal proportionalRate;
            private @Nullable BigInteger outputValueLimit;
            private @Nullable BigInteger controlFaultSumLimit;

            public @Nullable BigDecimal getIntegrationRate() {
                return this.integrationRate;
            }

            public @Nullable BigInteger getOutputRampRateLimit() {
                return this.outputRampRateLimit;
            }

            public @Nullable BigDecimal getDifferentialRate() {
                return this.differentialRate;
            }

            public @Nullable BigDecimal getProportionalRate() {
                return this.proportionalRate;
            }

            public @Nullable BigInteger getOutputValueLimit() {
                return this.outputValueLimit;
            }

            public @Nullable BigInteger getControlFaultSumLimit() {
                return this.controlFaultSumLimit;
            }
        }
    }
}
