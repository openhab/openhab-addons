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
package org.openhab.binding.sonnen.internal;

import static org.openhab.binding.sonnen.internal.SonnenBindingConstants.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.sonnen.internal.communication.SonnenJSONCommunication;
import org.openhab.binding.sonnen.internal.communication.SonnenJsonDataDTO;
import org.openhab.binding.sonnen.internal.communication.SonnenJsonPowerMeterDataDTO;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SonnenHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Christian Feininger - Initial contribution
 */
@NonNullByDefault
public class SonnenHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(SonnenHandler.class);

    private SonnenConfiguration config = new SonnenConfiguration();

    private @Nullable ScheduledFuture<?> refreshJob;

    private SonnenJSONCommunication serviceCommunication;

    private boolean automaticRefreshing = false;

    private boolean sonnenAPIV2 = false;

    private int disconnectionCounter = 0;

    private Map<String, Boolean> linkedChannels = new HashMap<>();

    public SonnenHandler(Thing thing) {
        super(thing);
        serviceCommunication = new SonnenJSONCommunication();
    }

    @Override
    public void initialize() {
        logger.debug("Initializing sonnen handler for thing {}", getThing().getUID());
        config = getConfigAs(SonnenConfiguration.class);
        if (config.refreshInterval < 0 || config.refreshInterval > 1000) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Parameter 'refresh Rate' msut be in the range 0-1000!");
            return;
        }
        if (config.hostIP.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "IP Address must be configured!");
            return;
        }

        if (!config.authToken.isEmpty()) {
            sonnenAPIV2 = true;
        }

        serviceCommunication.setConfig(config);
        updateStatus(ThingStatus.UNKNOWN);
        scheduler.submit(() -> {
            if (updateBatteryData()) {
                for (Channel channel : getThing().getChannels()) {
                    if (isLinked(channel.getUID().getId())) {
                        channelLinked(channel.getUID());
                    }
                }
            }
        });
    }

    /**
     * Calls the service to update the battery data
     *
     * @return true if the update succeeded, false otherwise
     */
    private boolean updateBatteryData() {
        String error = "";
        if (sonnenAPIV2) {
            error = serviceCommunication.refreshBatteryConnectionAPICALLV2(arePowerMeterChannelsLinked());
        } else {
            error = serviceCommunication.refreshBatteryConnectionAPICALLV1();
        }
        if (error.isEmpty()) {
            if (!ThingStatus.ONLINE.equals(getThing().getStatus())) {
                updateStatus(ThingStatus.ONLINE);
                disconnectionCounter = 0;
            }
        } else {
            disconnectionCounter++;
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, error);
            if (disconnectionCounter < 60) {
                return true;
            }
        }
        return error.isEmpty();
    }

    private void verifyLinkedChannel(String channelID) {
        if (isLinked(channelID) && !linkedChannels.containsKey(channelID)) {
            linkedChannels.put(channelID, true);
        }
    }

    @Override
    public void dispose() {
        stopAutomaticRefresh();
        linkedChannels.clear();
        automaticRefreshing = false;
    }

    private void stopAutomaticRefresh() {
        ScheduledFuture<?> job = refreshJob;
        if (job != null) {
            job.cancel(true);
        }
        refreshJob = null;
    }

    /**
     * Start the job refreshing the battery status
     */
    private void startAutomaticRefresh() {
        ScheduledFuture<?> job = refreshJob;
        if (job == null || job.isCancelled()) {
            refreshJob = scheduler.scheduleWithFixedDelay(this::refreshChannels, 0, config.refreshInterval,
                    TimeUnit.SECONDS);
        }
    }

    private void refreshChannels() {
        updateBatteryData();
        for (Channel channel : getThing().getChannels()) {
            updateChannel(channel.getUID().getId());
        }
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        if (!automaticRefreshing) {
            logger.debug("Start automatic refreshing");
            startAutomaticRefresh();
            automaticRefreshing = true;
        }
        verifyLinkedChannel(channelUID.getId());
        updateChannel(channelUID.getId());
    }

    @Override
    public void channelUnlinked(ChannelUID channelUID) {
        linkedChannels.remove(channelUID.getId());
        if (linkedChannels.isEmpty()) {
            automaticRefreshing = false;
            stopAutomaticRefresh();
            logger.debug("Stop automatic refreshing");
        }
    }

    private void updateChannel(String channelId) {
        if (isLinked(channelId)) {
            State state = null;
            SonnenJsonDataDTO data = serviceCommunication.getBatteryData();
            // The sonnen API has two sub-channels, e.g. 4_1 and 4_2, one representing consumption and the
            // other production. E.g. 4_1.kwh_imported represents the total production since the
            // battery was installed.
            SonnenJsonPowerMeterDataDTO[] dataPM = null;
            if (arePowerMeterChannelsLinked()) {
                dataPM = serviceCommunication.getPowerMeterData();
            }

            if (dataPM != null && dataPM.length >= 2) {
                switch (channelId) {
                    case CHANNELENERGYIMPORTEDSTATEPRODUCTION:
                        state = new QuantityType<>(dataPM[0].getKwhImported(), Units.KILOWATT_HOUR);
                        update(state, channelId);
                        break;
                    case CHANNELENERGYEXPORTEDSTATEPRODUCTION:
                        state = new QuantityType<>(dataPM[0].getKwhExported(), Units.KILOWATT_HOUR);
                        update(state, channelId);
                        break;
                    case CHANNELENERGYIMPORTEDSTATECONSUMPTION:
                        state = new QuantityType<>(dataPM[1].getKwhImported(), Units.KILOWATT_HOUR);
                        update(state, channelId);
                        break;
                    case CHANNELENERGYEXPORTEDSTATECONSUMPTION:
                        state = new QuantityType<>(dataPM[1].getKwhExported(), Units.KILOWATT_HOUR);
                        update(state, channelId);
                        break;
                }
            }

            if (data != null) {
                switch (channelId) {
                    case CHANNELBATTERYDISCHARGINGSTATE:
                        update(OnOffType.from(data.isBatteryDischarging()), channelId);
                        break;
                    case CHANNELBATTERYCHARGINGSTATE:
                        update(OnOffType.from(data.isBatteryCharging()), channelId);
                        break;
                    case CHANNELCONSUMPTION:
                        state = new QuantityType<>(data.getConsumptionHouse(), Units.WATT);
                        update(state, channelId);
                        break;
                    case CHANNELBATTERYDISCHARGING:
                        state = new QuantityType<>(data.getbatteryCurrent() > 0 ? data.getbatteryCurrent() : 0,
                                Units.WATT);
                        update(state, channelId);
                        break;
                    case CHANNELBATTERYCHARGING:
                        state = new QuantityType<>(data.getbatteryCurrent() <= 0 ? (data.getbatteryCurrent() * -1) : 0,
                                Units.WATT);
                        update(state, channelId);
                        break;
                    case CHANNELGRIDFEEDIN:
                        state = new QuantityType<>(data.getGridValue() > 0 ? data.getGridValue() : 0, Units.WATT);
                        update(state, channelId);
                        break;
                    case CHANNELGRIDCONSUMPTION:
                        state = new QuantityType<>(data.getGridValue() <= 0 ? (data.getGridValue() * -1) : 0,
                                Units.WATT);
                        update(state, channelId);
                        break;
                    case CHANNELSOLARPRODUCTION:
                        state = new QuantityType<>(data.getSolarProduction(), Units.WATT);
                        update(state, channelId);
                        break;
                    case CHANNELBATTERYLEVEL:
                        state = new QuantityType<>(data.getBatteryChargingLevel(), Units.PERCENT);
                        update(state, channelId);
                        break;
                    case CHANNELFLOWCONSUMPTIONBATTERYSTATE:
                        update(OnOffType.from(data.isFlowConsumptionBattery()), channelId);
                        break;
                    case CHANNELFLOWCONSUMPTIONGRIDSTATE:
                        update(OnOffType.from(data.isFlowConsumptionGrid()), channelId);
                        break;
                    case CHANNELFLOWCONSUMPTIONPRODUCTIONSTATE:
                        update(OnOffType.from(data.isFlowConsumptionProduction()), channelId);
                        break;
                    case CHANNELFLOWGRIDBATTERYSTATE:
                        update(OnOffType.from(data.isFlowGridBattery()), channelId);
                        break;
                    case CHANNELFLOWPRODUCTIONBATTERYSTATE:
                        update(OnOffType.from(data.isFlowProductionBattery()), channelId);
                        break;
                    case CHANNELFLOWPRODUCTIONGRIDSTATE:
                        update(OnOffType.from(data.isFlowProductionGrid()), channelId);
                        break;
                }
            }
        } else {
            update(null, channelId);
        }
    }

    @SuppressWarnings("PMD.SimplifyBooleanReturns")
    private boolean arePowerMeterChannelsLinked() {
        if (isLinked(CHANNELENERGYIMPORTEDSTATEPRODUCTION)) {
            return true;
        } else if (isLinked(CHANNELENERGYEXPORTEDSTATEPRODUCTION)) {
            return true;
        } else if (isLinked(CHANNELENERGYIMPORTEDSTATECONSUMPTION)) {
            return true;
        } else if (isLinked(CHANNELENERGYEXPORTEDSTATECONSUMPTION)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Updates the State of the given channel
     *
     * @param state Given state
     * @param channelId the refereed channelID
     */
    private void update(@Nullable State state, String channelId) {
        logger.debug("Update channel {} with state {}", channelId, (state == null) ? "null" : state.toString());
        updateState(channelId, state != null ? state : UnDefType.UNDEF);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command == RefreshType.REFRESH) {
            updateBatteryData();
            updateChannel(channelUID.getId());
        }
    }
}
