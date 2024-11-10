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
package org.openhab.binding.fenecon.internal;

import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
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
        for (String eachChannel : FeneconBindingConstants.ADDRESSES) {
            try {
                @SuppressWarnings("null")
                Optional<FeneconResponse> response = feneconController.requestChannel(eachChannel);

                if (response.isPresent()) {
                    processDataPoint(response.get());
                }

                updateStatus(ThingStatus.ONLINE);
            } catch (FeneconException err) {
                logger.trace("FENECON - connection problem on FENECON channel {}", eachChannel, err);
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
