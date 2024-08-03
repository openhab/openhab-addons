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

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.fenecon.internal.api.FeneconController;
import org.openhab.binding.fenecon.internal.api.FeneconResponse;
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
import org.openhab.core.types.RefreshType;
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

    public FeneconHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        config = getConfigAs(FeneconConfiguration.class);

        updateStatus(ThingStatus.UNKNOWN);

        feneconController = new FeneconController(config);
        pollingJob = scheduler.scheduleWithFixedDelay(this::pollingCode, 0, config.refreshInterval, TimeUnit.SECONDS);
    }

    private void pollingCode() {
        for (String eachChannel : FeneconBindingConstants.ADDRESSES) {
            try {
                @SuppressWarnings("null")
                FeneconResponse response = feneconController.requestChannel(eachChannel);

                processDataPoint(response);
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
                int begin = response.text().indexOf(response.value() + ":");
                int end = response.text().indexOf(",", begin);
                updateState(FeneconBindingConstants.STATE_CHANNEL,
                        new StringType(response.text().substring(begin + 2, end)));
                break;
            case FeneconBindingConstants.ESS_SOC_ADDRESS:
                updateState(FeneconBindingConstants.ESS_SOC_CHANNEL,
                        new QuantityType<>(Integer.valueOf(response.value()), Units.PERCENT));
                break;
            case FeneconBindingConstants.CONSUMPTION_ACTIVE_POWER_ADDRESS:
                updateState(FeneconBindingConstants.CONSUMPTION_ACTIVE_POWER_CHANNEL,
                        new QuantityType<>(Integer.valueOf(response.value()), Units.WATT));
                break;
            case FeneconBindingConstants.PRODUCTION_ACTIVE_POWER_ADDRESS:
                updateState(FeneconBindingConstants.PRODUCTION_ACTIVE_POWER_CHANNEL,
                        new QuantityType<>(Integer.valueOf(response.value()), Units.WATT));
                break;
            case FeneconBindingConstants.GRID_ACTIVE_POWER_ADDRESS:
                // Grid exchange power. Negative values for sell-to-grid; positive for buy-from-grid"
                Integer gridValue = Integer.valueOf(response.value());
                int selltoGridPower = 0;
                int buyFromGridPower = 0;
                if (gridValue < 0) {
                    selltoGridPower = gridValue * -1;
                } else {
                    buyFromGridPower = gridValue;
                }
                updateState(FeneconBindingConstants.EXPORT_TO_GRID_POWER_CHANNEL,
                        new QuantityType<>(selltoGridPower, Units.WATT));
                updateState(FeneconBindingConstants.IMPORT_FROM_GRID_POWER_CHANNEL,
                        new QuantityType<>(buyFromGridPower, Units.WATT));
                break;
            case FeneconBindingConstants.ESS_DISCHARGE_POWER_ADDRESS:
                // Actual AC-side battery discharge power of Energy Storage System.
                // Negative values for charge; positive for discharge
                Integer powerValue = Integer.valueOf(response.value());
                int chargerPower = 0;
                int dischargerPower = 0;
                if (powerValue < 0) {
                    chargerPower = powerValue * -1;
                } else {
                    dischargerPower = powerValue;
                }
                updateState(FeneconBindingConstants.ESS_CHARGER_POWER_CHANNEL,
                        new QuantityType<>(chargerPower, Units.WATT));
                updateState(FeneconBindingConstants.ESS_DISCHARGER_POWER_CHANNEL,
                        new QuantityType<>(dischargerPower, Units.WATT));
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
                logger.trace("FENECON - No channel id to address {} found.", response.address());
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
        if (command == RefreshType.REFRESH) {
            // Noop
        }
    }
}
