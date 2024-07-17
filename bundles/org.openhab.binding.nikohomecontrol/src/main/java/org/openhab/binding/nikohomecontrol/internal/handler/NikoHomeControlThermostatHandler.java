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
package org.openhab.binding.nikohomecontrol.internal.handler;

import static org.openhab.binding.nikohomecontrol.internal.NikoHomeControlBindingConstants.*;
import static org.openhab.binding.nikohomecontrol.internal.protocol.NikoHomeControlConstants.*;
import static org.openhab.core.library.unit.SIUnits.CELSIUS;
import static org.openhab.core.types.RefreshType.REFRESH;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.nikohomecontrol.internal.protocol.NhcThermostat;
import org.openhab.binding.nikohomecontrol.internal.protocol.NhcThermostatEvent;
import org.openhab.binding.nikohomecontrol.internal.protocol.NikoHomeControlCommunication;
import org.openhab.binding.nikohomecontrol.internal.protocol.nhc2.NhcThermostat2;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NikoHomeControlThermostatHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Mark Herwege - Initial Contribution
 */
@NonNullByDefault
public class NikoHomeControlThermostatHandler extends NikoHomeControlBaseHandler implements NhcThermostatEvent {

    private final Logger logger = LoggerFactory.getLogger(NikoHomeControlThermostatHandler.class);

    private volatile @Nullable NhcThermostat nhcThermostat;

    private int overruleTime;

    private volatile @Nullable ScheduledFuture<?> refreshTimer; // used to refresh the remaining overrule time every
                                                                // minute

    public NikoHomeControlThermostatHandler(Thing thing) {
        super(thing);
    }

    @Override
    void handleCommandSelection(ChannelUID channelUID, Command command) {
        NhcThermostat nhcThermostat = this.nhcThermostat;
        if (nhcThermostat == null) {
            logger.debug("thermostat with ID {} not initialized", deviceId);
            return;
        }

        logger.debug("handle command {} for {}", command, channelUID);

        if (REFRESH.equals(command)) {
            thermostatEvent(nhcThermostat.getMeasured(), nhcThermostat.getSetpoint(), nhcThermostat.getMode(),
                    nhcThermostat.getOverrule(), nhcThermostat.getDemand());
            return;
        }

        switch (channelUID.getId()) {
            case CHANNEL_MEASURED:
            case CHANNEL_DEMAND:
            case CHANNEL_HEATING_DEMAND:
                break;
            case CHANNEL_MODE:
                if (command instanceof DecimalType decimalCommand) {
                    nhcThermostat.executeMode(decimalCommand.intValue());
                }
                break;
            case CHANNEL_HEATING_MODE:
                if (command instanceof StringType) {
                    nhcThermostat.executeMode(command.toString());
                }
                break;
            case CHANNEL_SETPOINT:
                // Always set the new setpoint temperature as an overrule
                // If no overrule time is given yet, set the overrule time to the configuration parameter
                int time = nhcThermostat.getOverruletime();
                if (time <= 0) {
                    time = overruleTime;
                }
                if (command instanceof QuantityType<?> quantityCommand) {
                    QuantityType<?> setpoint = quantityCommand.toUnit(CELSIUS);
                    if (setpoint != null) {
                        nhcThermostat.executeOverrule(Math.round(setpoint.floatValue() * 10), time);
                    }
                } else if (command instanceof DecimalType decimalCommand) {
                    BigDecimal setpoint = decimalCommand.toBigDecimal();
                    nhcThermostat.executeOverrule(Math.round(setpoint.floatValue() * 10), time);
                }
                break;
            case CHANNEL_OVERRULETIME:
                if (command instanceof DecimalType decimalCommand) {
                    int overruletime = decimalCommand.intValue();
                    int overrule = nhcThermostat.getOverrule();
                    if (overruletime <= 0) {
                        overruletime = 0;
                        overrule = 0;
                    }
                    nhcThermostat.executeOverrule(overrule, overruletime);
                }
                break;
            default:
                logger.debug("unexpected command for channel {}", channelUID.getId());
        }
    }

    @Override
    public void initialize() {
        initialized = false;

        NikoHomeControlThermostatConfig config = getConfig().as(NikoHomeControlThermostatConfig.class);

        deviceId = config.thermostatId;
        overruleTime = config.overruleTime;

        NikoHomeControlBridgeHandler bridgeHandler = getBridgeHandler();
        if (bridgeHandler == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.configuration-error.invalid-bridge-handler");
            return;
        }

        updateStatus(ThingStatus.UNKNOWN);

        Bridge bridge = getBridge();
        if ((bridge != null) && ThingStatus.ONLINE.equals(bridge.getStatus())) {
            // We need to do this in a separate thread because we may have to wait for the
            // communication to become active
            commStartThread = scheduler.submit(this::startCommunication);
        }
    }

    @Override
    synchronized void startCommunication() {
        NikoHomeControlCommunication nhcComm = getCommunication(getBridgeHandler());

        if (nhcComm == null) {
            return;
        }

        if (!nhcComm.communicationActive()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/offline.communication-error");
            return;
        }

        NhcThermostat nhcThermostat = nhcComm.getThermostats().get(deviceId);
        if (nhcThermostat == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.configuration-error.deviceId");
            return;
        }

        nhcThermostat.setEventHandler(this);

        updateProperties(nhcThermostat);

        String thermostatLocation = nhcThermostat.getLocation();
        if (thing.getLocation() == null) {
            thing.setLocation(thermostatLocation);
        }

        this.nhcThermostat = nhcThermostat;

        initialized = true;
        deviceInitialized();
    }

    @Override
    void refresh() {
        NhcThermostat thermostat = nhcThermostat;
        if (thermostat != null) {
            thermostatEvent(thermostat.getMeasured(), thermostat.getSetpoint(), thermostat.getMode(),
                    thermostat.getOverrule(), thermostat.getDemand());
        }
    }

    @Override
    public void dispose() {
        NikoHomeControlCommunication nhcComm = getCommunication(getBridgeHandler());
        if (nhcComm != null) {
            NhcThermostat thermostat = nhcComm.getThermostats().get(deviceId);
            if (thermostat != null) {
                thermostat.unsetEventHandler();
            }
        }
        nhcThermostat = null;
        super.dispose();
    }

    private void updateProperties(NhcThermostat nhcThermostat) {
        Map<String, String> properties = new HashMap<>();

        if (nhcThermostat instanceof NhcThermostat2 thermostat) {
            properties.put(PROPERTY_DEVICE_TYPE, thermostat.getDeviceType());
            properties.put(PROPERTY_DEVICE_TECHNOLOGY, thermostat.getDeviceTechnology());
            properties.put(PROPERTY_DEVICE_MODEL, thermostat.getDeviceModel());
        }

        thing.setProperties(properties);
    }

    @Override
    public void thermostatEvent(int measured, int setpoint, int mode, int overrule, int demand) {
        NhcThermostat nhcThermostat = this.nhcThermostat;
        if (nhcThermostat == null) {
            logger.debug("thermostat with ID {} not initialized", deviceId);
            return;
        }

        updateState(CHANNEL_MEASURED, new QuantityType<>(measured / 10.0, CELSIUS));

        int overruletime = nhcThermostat.getRemainingOverruletime();
        updateState(CHANNEL_OVERRULETIME, new DecimalType(overruletime));
        // refresh the remaining time every minute
        scheduleRefreshOverruletime(nhcThermostat);

        // If there is an overrule temperature set, use this in the setpoint channel, otherwise use the original
        // setpoint temperature
        if (overruletime == 0) {
            updateState(CHANNEL_SETPOINT, new QuantityType<>(setpoint / 10.0, CELSIUS));
        } else {
            updateState(CHANNEL_SETPOINT, new QuantityType<>(overrule / 10.0, CELSIUS));
        }

        updateState(CHANNEL_MODE, new DecimalType(mode));
        updateState(CHANNEL_HEATING_MODE, new StringType(THERMOSTATMODES[mode]));

        updateState(CHANNEL_DEMAND, new DecimalType(demand));
        updateState(CHANNEL_HEATING_DEMAND, new StringType(THERMOSTATDEMAND[Math.abs(demand) <= 1 ? (demand + 1) : 0]));

        updateStatus(ThingStatus.ONLINE);
    }

    /**
     * Method to update state of overruletime channel every minute with remaining time.
     *
     * @param NhcThermostat object
     */
    private void scheduleRefreshOverruletime(NhcThermostat nhcThermostat) {
        cancelRefreshTimer();

        if (nhcThermostat.getRemainingOverruletime() == 0) {
            return;
        }

        refreshTimer = scheduler.scheduleWithFixedDelay(() -> {
            int remainingTime = nhcThermostat.getRemainingOverruletime();
            updateState(CHANNEL_OVERRULETIME, new DecimalType(remainingTime));
            if (remainingTime == 0) {
                cancelRefreshTimer();
            }
        }, 1, 1, TimeUnit.MINUTES);
    }

    private void cancelRefreshTimer() {
        ScheduledFuture<?> timer = refreshTimer;
        if (timer != null) {
            timer.cancel(true);
        }
        refreshTimer = null;
    }
}
