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
package org.openhab.binding.fenecon.internal;

import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.fenecon.internal.api.AddressComponentChannelUtil;
import org.openhab.binding.fenecon.internal.api.BatteryPower;
import org.openhab.binding.fenecon.internal.api.FeneconController;
import org.openhab.binding.fenecon.internal.api.FeneconResponse;
import org.openhab.binding.fenecon.internal.api.GridPower;
import org.openhab.binding.fenecon.internal.api.State;
import org.openhab.binding.fenecon.internal.exception.FeneconException;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link FeneconHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Philipp Schneider - Initial contribution
 */
@NonNullByDefault
public class FeneconHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(FeneconHandler.class);

    private FeneconConfiguration config = new FeneconConfiguration();
    private @Nullable ScheduledFuture<?> pollingJob;
    private @Nullable FeneconController feneconController;
    private final HttpClient httpClient;

    public FeneconHandler(Thing thing, HttpClient httpClient) {
        super(thing);
        this.httpClient = httpClient;
    }

    @Override
    public void initialize() {
        config = getConfigAs(FeneconConfiguration.class);

        updateStatus(ThingStatus.UNKNOWN);

        feneconController = new FeneconController(config, this.httpClient);
        pollingJob = scheduler.scheduleWithFixedDelay(this::pollingCode, 0, config.refreshInterval, TimeUnit.SECONDS);
    }

    private void pollingCode() {
        List<String> componentRequests = AddressComponentChannelUtil
                .createComponentRequests(FeneconBindingConstants.ADDRESSES);

        for (String eachComponentRequest : componentRequests) {
            try {
                @SuppressWarnings("null")
                List<FeneconResponse> responses = feneconController.requestChannel(eachComponentRequest);

                for (FeneconResponse eachResponse : responses) {
                    processDataPoint(eachResponse);
                }

                updateStatus(ThingStatus.ONLINE);
            } catch (FeneconException err) {
                logger.trace("FENECON - connection problem on FENECON channel {}", eachComponentRequest, err);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, err.getMessage());
                return;
            }
        }

        // Set last successful update cycle
        updateState(FeneconBindingConstants.LAST_UPDATE_CHANNEL, new DateTimeType());
    }

    private void processDataPoint(FeneconResponse response) throws FeneconException {
        switch (response.address()) {
            case FeneconBindingConstants.STATE_ADDRESS:
                // {"address":"_sum/State","type":"INTEGER","accessMode":"RO","text":"0:Ok, 1:Info, 2:Warning,
                // 3:Fault","unit":"","value":0}
                State state = State.get(response);
                updateState(FeneconBindingConstants.STATE_CHANNEL, new StringType(state.state()));
                break;
            case FeneconBindingConstants.ESS_SOC_ADDRESS:
                updateState(FeneconBindingConstants.ESS_SOC_CHANNEL,
                        new QuantityType<>(Integer.valueOf(response.value()), Units.PERCENT));
                break;
            case FeneconBindingConstants.CONSUMPTION_ACTIVE_POWER_ADDRESS:
                updateState(FeneconBindingConstants.CONSUMPTION_ACTIVE_POWER_CHANNEL,
                        new QuantityType<>(Integer.valueOf(response.value()), Units.WATT));
                break;
            case FeneconBindingConstants.CONSUMPTION_ACTIVE_POWER_PHASE1_ADDRESS:
                updateState(FeneconBindingConstants.CONSUMPTION_ACTIVE_POWER_PHASE1_CHANNEL,
                        new QuantityType<>(Integer.valueOf(response.value()), Units.WATT));
                break;
            case FeneconBindingConstants.CONSUMPTION_ACTIVE_POWER_PHASE2_ADDRESS:
                updateState(FeneconBindingConstants.CONSUMPTION_ACTIVE_POWER_PHASE2_CHANNEL,
                        new QuantityType<>(Integer.valueOf(response.value()), Units.WATT));
                break;
            case FeneconBindingConstants.CONSUMPTION_ACTIVE_POWER_PHASE3_ADDRESS:
                updateState(FeneconBindingConstants.CONSUMPTION_ACTIVE_POWER_PHASE3_CHANNEL,
                        new QuantityType<>(Integer.valueOf(response.value()), Units.WATT));
                break;
            case FeneconBindingConstants.CONSUMPTION_MAX_ACTIVE_POWER_ADDRESS:
                updateState(FeneconBindingConstants.CONSUMPTION_MAX_ACTIVE_POWER_CHANNEL,
                        new QuantityType<>(Integer.valueOf(response.value()), Units.WATT));
                break;
            case FeneconBindingConstants.PRODUCTION_MAX_ACTIVE_POWER_ADDRESS:
                updateState(FeneconBindingConstants.PRODUCTION_MAX_ACTIVE_POWER_CHANNEL,
                        new QuantityType<>(Integer.valueOf(response.value()), Units.WATT));
                break;
            case FeneconBindingConstants.PRODUCTION_ACTIVE_POWER_ADDRESS:
                updateState(FeneconBindingConstants.PRODUCTION_ACTIVE_POWER_CHANNEL,
                        new QuantityType<>(Integer.valueOf(response.value()), Units.WATT));
                break;
            case FeneconBindingConstants.GRID_ACTIVE_POWER_ADDRESS:
                // Grid exchange power. Negative values for sell-to-grid; positive for buy-from-grid"
                GridPower gridPower = GridPower.get(response);

                updateState(FeneconBindingConstants.EXPORT_TO_GRID_POWER_CHANNEL,
                        new QuantityType<>(gridPower.sellTo(), Units.WATT));
                updateState(FeneconBindingConstants.IMPORT_FROM_GRID_POWER_CHANNEL,
                        new QuantityType<>(gridPower.buyFrom(), Units.WATT));
                break;
            case FeneconBindingConstants.ESS_DISCHARGE_POWER_ADDRESS:
                // Actual AC-side battery discharge power of Energy Storage System.
                // Negative values for charge; positive for discharge
                BatteryPower batteryPower = BatteryPower.get(response);

                updateState(FeneconBindingConstants.ESS_CHARGER_POWER_CHANNEL,
                        new QuantityType<>(batteryPower.chargerPower(), Units.WATT));
                updateState(FeneconBindingConstants.ESS_DISCHARGER_POWER_CHANNEL,
                        new QuantityType<>(batteryPower.dischargerPower(), Units.WATT));
                break;
            case FeneconBindingConstants.GRID_MODE_ADDRESS:
                // text":"1:On-Grid, 2:Off-Grid","unit":"","value":1
                Integer gridMod = Integer.valueOf(response.value());
                updateState(FeneconBindingConstants.EMERGENCY_POWER_MODE_CHANNEL,
                        gridMod == 2 ? OnOffType.ON : OnOffType.OFF);
                break;
            case FeneconBindingConstants.GRID_SELL_ACTIVE_ENERGY_ADDRESS:
                // {"address":"_sum/GridSellActiveEnergy","type":"LONG","accessMode":"RO","text":"","unit":"Wh_Σ","value":374242}
                updateState(FeneconBindingConstants.EXPORTED_TO_GRID_ENERGY_CHANNEL,
                        new QuantityType<>(Integer.valueOf(response.value()), Units.WATT_HOUR));
                break;
            case FeneconBindingConstants.GRID_BUY_ACTIVE_ENERGY_ADDRESS:
                // "address":"_sum/GridBuyActiveEnergy","type":"LONG","accessMode":"RO","text":"","unit":"Wh_Σ","value":1105}
                updateState(FeneconBindingConstants.IMPORTED_FROM_GRID_ENERGY_CHANNEL,
                        new QuantityType<>(Integer.valueOf(response.value()), Units.WATT_HOUR));
                break;
            case FeneconBindingConstants.FEMS_VERSION_ADDRESS:
                // { "address": "_meta/Version","type": "STRING", "accessMode": "RO", "text": "", "unit": "", "value":
                // "2025.2.3"}
                updateState(FeneconBindingConstants.FEMS_VERSION_CHANNEL, new StringType(response.value()));
                break;
            case FeneconBindingConstants.BATT_INVERTER_AIR_TEMP_ADDRESS:
                // {"address": "batteryInverter0/AirTemperature","type": "INTEGER","accessMode": "RO", "text": "",
                // "unit": "C", "value": 41 }
                updateState(FeneconBindingConstants.BATT_INVERTER_AIR_TEMP_CHANNEL,
                        new QuantityType<>(Integer.valueOf(response.value()), SIUnits.CELSIUS));
                break;
            case FeneconBindingConstants.BATT_INVERTER_RADIATOR_TEMP_ADDRESS:
                // {"address": "batteryInverter0/RadiatorTemperature","type": "INTEGER", "accessMode": "RO", "text": "",
                // "unit": "C", "value": 37 }
                updateState(FeneconBindingConstants.BATT_INVERTER_RADIATOR_TEMP_CHANNEL,
                        new QuantityType<>(Integer.valueOf(response.value()), SIUnits.CELSIUS));
                break;
            case FeneconBindingConstants.BATT_INVERTER_BMS_PACK_TEMP_ADDRESS:
                // {"address": "batteryInverter0/BmsPackTemperature", "type": "INTEGER", "accessMode": "RO", "text": "",
                // "unit": "C", "value": 26 }
                updateState(FeneconBindingConstants.BATT_INVERTER_BMS_PACK_TEMP_CHANNEL,
                        new QuantityType<>(Integer.valueOf(response.value()), SIUnits.CELSIUS));
                break;
            case FeneconBindingConstants.BATT_TOWER_PACK_VOLTAGE_ADDRESS:
                // {"address": "battery0/Tower0PackVoltage", "type": "INTEGER", "accessMode": "RO", "text": "", "unit":
                // "", "value": 2749 }
                // Tower pack voltage in mV
                updateState(FeneconBindingConstants.BATT_TOWER_PACK_VOLTAGE_CHANNEL,
                        new QuantityType<>(Integer.valueOf(response.value()) / 1000.0, Units.VOLT));
                break;
            case FeneconBindingConstants.BATT_TOWER_CURRENT_ADDRESS:
                // {"address": "battery0/Current", "type": "INTEGER", "accessMode": "RO", "text": "", "unit": "A",
                // "value": 9 }
                updateState(FeneconBindingConstants.BATT_TOWER_CURRENT_CHANNEL,
                        new QuantityType<>(Integer.valueOf(response.value()), Units.AMPERE));
                break;
            case FeneconBindingConstants.BATT_SOH_ADDRESS:
                // { "address": "battery0/Soh", "type": "INTEGER", "accessMode": "RO", "text": "", "unit": "%", "value":
                // 100 }
                updateState(FeneconBindingConstants.BATT_SOH_CHANNEL,
                        new QuantityType<>(Integer.valueOf(response.value()), Units.PERCENT));
                break;
            case FeneconBindingConstants.CHARGER0_ACTUAL_POWER_ADDRESS:
                // { "address": "charger0/ActualPower", "type": "INTEGER", "accessMode": "RO", "text": "", "unit": "W",
                // "value": 312 }
                updateState(FeneconBindingConstants.CHARGER0_ACTUAL_POWER_CHANNEL,
                        new QuantityType<>(Integer.valueOf(response.value()), Units.WATT));
                break;
            case FeneconBindingConstants.CHARGER1_ACTUAL_POWER_ADDRESS:
                // { "address": "charger1/ActualPower", "type": "INTEGER", "accessMode": "RO", "text": "", "unit": "W",
                // "value": 33 }
                updateState(FeneconBindingConstants.CHARGER1_ACTUAL_POWER_CHANNEL,
                        new QuantityType<>(Integer.valueOf(response.value()), Units.WATT));
                break;
            case FeneconBindingConstants.CHARGER2_ACTUAL_POWER_ADDRESS:
                // { "address": "charger2/ActualPower", "type": "INTEGER", "accessMode": "RO", "text": "", "unit": "W",
                // "value": 412 }
                updateState(FeneconBindingConstants.CHARGER2_ACTUAL_POWER_CHANNEL,
                        new QuantityType<>(Integer.valueOf(response.value()), Units.WATT));
                break;
            case FeneconBindingConstants.CHARGER0_VOLTAGE_ADDRESS:
                // { "address": "charger0/Voltage", "type": "INTEGER", "accessMode": "RO", "text": "", "unit": "mV",
                // "value": 193000 }
                updateState(FeneconBindingConstants.CHARGER0_VOLTAGE_CHANNEL,
                        new QuantityType<>(Integer.valueOf(response.value()) / 1000.0, Units.VOLT));
                break;
            case FeneconBindingConstants.CHARGER1_VOLTAGE_ADDRESS:
                // { "address": "charger1/Voltage", "type": "INTEGER", "accessMode": "RO", "text": "", "unit": "mV",
                // "value": 193000 }
                updateState(FeneconBindingConstants.CHARGER1_VOLTAGE_CHANNEL,
                        new QuantityType<>(Integer.valueOf(response.value()) / 1000.0, Units.VOLT));
                break;
            case FeneconBindingConstants.CHARGER2_VOLTAGE_ADDRESS:
                // { "address": "charger2/Voltage", "type": "INTEGER", "accessMode": "RO", "text": "", "unit": "mV",
                // "value": 193000 }
                updateState(FeneconBindingConstants.CHARGER2_VOLTAGE_CHANNEL,
                        new QuantityType<>(Integer.valueOf(response.value()) / 1000.0, Units.VOLT));
                break;
            case FeneconBindingConstants.CHARGER0_CURRENT_ADDRESS:
                // {"address": "charger0/Current", "type": "INTEGER", "accessMode": "RO", "text": "", "unit": "mA",
                // "value": 1200 },
                updateState(FeneconBindingConstants.CHARGER0_CURRENT_CHANNEL,
                        new QuantityType<>(Integer.valueOf(response.value()) / 1000.0, Units.AMPERE));
                break;
            case FeneconBindingConstants.CHARGER1_CURRENT_ADDRESS:
                // {"address": "charger1/Current", "type": "INTEGER", "accessMode": "RO", "text": "", "unit": "mA",
                // "value": 1000 },
                updateState(FeneconBindingConstants.CHARGER1_CURRENT_CHANNEL,
                        new QuantityType<>(Integer.valueOf(response.value()) / 1000.0, Units.AMPERE));
                break;
            case FeneconBindingConstants.CHARGER2_CURRENT_ADDRESS:
                // {"address": "charger2/Current", "type": "INTEGER", "accessMode": "RO", "text": "", "unit": "mA",
                // "value": 1100 },
                updateState(FeneconBindingConstants.CHARGER2_CURRENT_CHANNEL,
                        new QuantityType<>(Integer.valueOf(response.value()) / 1000.0, Units.AMPERE));
                break;
            default:
                logger.trace("FENECON - No channel ID to address {} found.", response.address());
                break;
        }
    }

    @Override
    public void dispose() {
        ScheduledFuture<?> job = pollingJob;
        if (job != null) {
            job.cancel(true);
            pollingJob = null;
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // Noop
    }
}
