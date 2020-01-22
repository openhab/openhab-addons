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
package org.openhab.binding.rootedtoon.internal;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.rootedtoon.internal.client.ToonClient;
import org.openhab.binding.rootedtoon.internal.client.ToonClientEventListener;
import org.openhab.binding.rootedtoon.internal.client.ToonConnectionException;
import org.openhab.binding.rootedtoon.internal.client.model.PowerUsageInfo;
import org.openhab.binding.rootedtoon.internal.client.model.RealtimeUsageInfo;
import org.openhab.binding.rootedtoon.internal.client.model.ThermostatInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link RootedToonHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Daan Meijer - Initial contribution
 */
@NonNullByDefault
public class RootedToonHandler extends BaseThingHandler implements ToonClientEventListener {

    private final Logger logger = LoggerFactory.getLogger(RootedToonHandler.class);

    private @Nullable RootedToonConfiguration config;

    @Nullable
    protected ScheduledFuture<?> refreshJob;

    @Nullable
    protected ScheduledFuture<?> realtimeRefreshJob;

    @Nullable
    private ToonClient client;

    public RootedToonHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void dispose() {
        this.refreshJob.cancel(true);
        this.realtimeRefreshJob.cancel(true);
        this.client = null;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof org.eclipse.smarthome.core.types.RefreshType) {
            updateThermostatInfo();
            updateRealtimeUsageInfo();
        } else {
            try {
                OnOffType wantedValue;
                int toonState;

                double setpoint = -1;

                String str = channelUID.getId();
                switch (str) {
                    case "ProgramEnabled":
                        wantedValue = (OnOffType) command;
                        this.client.setProgramEnabled(wantedValue.equals(OnOffType.ON));
                        this.client.requestThermostatInfo();
                        break;
                    case "SetpointMode":
                        toonState = ((DecimalType) command).intValue();
                        this.client.setActiveState(ToonClient.State.fromToonState(toonState));
                        this.client.requestThermostatInfo();
                        break;
                    case "Setpoint":
                        if (command instanceof DecimalType) {
                            setpoint = ((DecimalType) command).doubleValue();
                        } else if (command instanceof QuantityType) {
                            setpoint = ((QuantityType) command).doubleValue();
                        }
                        if (setpoint != -1) {
                            this.client.setRoomSetpoint(setpoint);
                            this.client.requestThermostatInfo();
                        }
                        break;
                }
                updateChannel(channelUID.getId(), (State) command);
            } catch (ToonConnectionException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            }
        }
    }

    @Override
    public void initialize() {
        this.logger.debug("Start initializing!");

        this.config = getConfigAs(RootedToonConfiguration.class);

        this.client = new ToonClient(this.config.url);
        this.client.addListener(this);

        updateStatus(ThingStatus.UNKNOWN);

        this.scheduler.execute(() -> {

            boolean thingReachable = true;

            thingReachable &= this.client.testConnection();

            if (thingReachable) {
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Can not access device");
            }
        });

        startAutomaticRefresh();

        this.logger.debug("Finished initializing!");
    }

    private void startAutomaticRefresh() {
        if (this.refreshJob != null) {
            this.refreshJob.cancel(true);
        }
        if (this.realtimeRefreshJob != null) {
            this.realtimeRefreshJob.cancel(true);
        }

        this.refreshJob = this.scheduler.scheduleWithFixedDelay(this::updateThermostatInfo, 5L,
                this.config.refreshInterval, TimeUnit.SECONDS);
        this.realtimeRefreshJob = this.scheduler.scheduleWithFixedDelay(this::updateRealtimeUsageInfo, 5L, 8L,
                TimeUnit.SECONDS);
    }

    protected void updateChannel(String channelName, State state) {
        Channel channel = getThing().getChannel(channelName);
        if (channel != null) {
            updateState(channel.getUID(), state);
        }
    }

    private void updateThermostatInfo() {
        try {
            this.client.requestThermostatInfo();
        } catch (ToonConnectionException e) {
            this.logger.error("Error while updating thermostat info", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    private void updateRealtimeUsageInfo() {
        try {
            this.client.requestRealtimeUsageInfo();
        } catch (ToonConnectionException e) {
            this.logger.error("Error while updating realtime usage info", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    @Override
    public void newPowerUsageInfo(PowerUsageInfo powerInfo) {
    }

    @Override
    public void newThermostatInfo(ThermostatInfo thermostatInfo) {
        updateChannel("Temperature", new DecimalType(thermostatInfo.currentTemp));
        updateChannel("Setpoint", new DecimalType(thermostatInfo.currentSetpoint));
        updateChannel("ModulationLevel", new DecimalType(thermostatInfo.currentModulationLevel));

        updateChannel("SetpointMode", new DecimalType(thermostatInfo.activeState));
        updateChannel("ProgramEnabled",
                (thermostatInfo.programState > 0) ? (State) OnOffType.ON : (State) OnOffType.OFF);

        updateChannel("NextSetpoint", new DecimalType(thermostatInfo.nextSetpoint));

        updateChannel("NextSetpointTime", new DateTimeType(
                ZonedDateTime.ofInstant(thermostatInfo.getNextTime().toInstant(), ZoneId.systemDefault())));
        updateChannel("BoilerSetpoint", new DecimalType(thermostatInfo.currentInternalBoilerSetpoint));

        switch (thermostatInfo.burnerInfo) {
            case 0:
                updateChannel("Heating", OnOffType.OFF);
                updateChannel("Tapwater", OnOffType.OFF);
                updateChannel("Preheat", OnOffType.OFF);
            case 1:
                updateChannel("Heating", OnOffType.ON);
                updateChannel("Tapwater", OnOffType.OFF);
                updateChannel("Preheat", OnOffType.OFF);
                return;
            case 2:
                updateChannel("Heating", OnOffType.OFF);
                updateChannel("Tapwater", OnOffType.ON);
                updateChannel("Preheat", OnOffType.OFF);
                return;
            case 3:
                updateChannel("Heating", OnOffType.OFF);
                updateChannel("Tapwater", OnOffType.OFF);
                updateChannel("Preheat", OnOffType.ON);
                return;
        }
        // I have no idea why this was here
        // updateStatus(ThingStatus.OFFLINE);
    }

    @Override
    public void newRealtimeUsageInfo(@Nullable RealtimeUsageInfo realtimeUsageInfo) {

        if (!realtimeUsageInfo.gas.total.isNaN()) {
            updateChannel("GasMeterReading", new DecimalType(realtimeUsageInfo.gas.total.doubleValue()));
        }

        if (!realtimeUsageInfo.gas.flow.isNaN()) {
            updateChannel("GasConsumption", new DecimalType(realtimeUsageInfo.gas.flow.doubleValue()));
        }

        if (!realtimeUsageInfo.elec_delivered_lt.flow.isNaN() && !realtimeUsageInfo.elec_delivered_nt.flow.isNaN()) {
            updateChannel("PowerConsumption", new DecimalType(realtimeUsageInfo.elec_delivered_lt.flow.doubleValue()
                    + realtimeUsageInfo.elec_delivered_nt.flow.doubleValue()));
        }

        if (!realtimeUsageInfo.elec_delivered_nt.total.isNaN()) {
            updateChannel("PowerMeterReading",
                    new DecimalType(realtimeUsageInfo.elec_delivered_nt.total.doubleValue()));
        }

        if (!realtimeUsageInfo.elec_delivered_lt.total.isNaN()) {
            updateChannel("PowerMeterReadingLow",
                    new DecimalType(realtimeUsageInfo.elec_delivered_lt.total.doubleValue()));
        }

        if (!realtimeUsageInfo.elec_delivered_nt.flow.isNaN()) {
            updateChannel("PowerMeterFlow", new DecimalType(realtimeUsageInfo.elec_delivered_nt.flow.doubleValue()));
        }

        if (!realtimeUsageInfo.elec_delivered_lt.flow.isNaN()) {
            updateChannel("PowerMeterFlowLow", new DecimalType(realtimeUsageInfo.elec_delivered_lt.flow.doubleValue()));
        }

        if (!realtimeUsageInfo.elec_received_nt.total.isNaN()) {
            updateChannel("PowerProducedReading",
                    new DecimalType(realtimeUsageInfo.elec_received_nt.total.doubleValue()));
        }
        if (!realtimeUsageInfo.elec_received_lt.total.isNaN()) {
            updateChannel("PowerProducedReadingLow",
                    new DecimalType(realtimeUsageInfo.elec_received_lt.total.doubleValue()));
        }

        if (!realtimeUsageInfo.elec_received_nt.flow.isNaN()) {
            updateChannel("PowerProducedFlow", new DecimalType(realtimeUsageInfo.elec_received_nt.flow.doubleValue()));
        }

        if (!realtimeUsageInfo.elec_received_lt.flow.isNaN()) {
            updateChannel("PowerProducedFlowLow",
                    new DecimalType(realtimeUsageInfo.elec_received_lt.flow.doubleValue()));
        }

        if (!realtimeUsageInfo.elec_solar.total.isNaN()) {
            updateChannel("SolarPowerReading", new DecimalType(realtimeUsageInfo.elec_solar.total.doubleValue()));
        }
        if (!realtimeUsageInfo.elec_solar.flow.isNaN()) {
            updateChannel("SolarPowerFlow", new DecimalType(realtimeUsageInfo.elec_solar.flow.doubleValue()));
        }
    }
}
