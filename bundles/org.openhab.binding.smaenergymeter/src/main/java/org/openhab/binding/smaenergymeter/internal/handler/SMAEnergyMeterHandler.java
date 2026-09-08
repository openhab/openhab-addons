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
package org.openhab.binding.smaenergymeter.internal.handler;

import static org.openhab.binding.smaenergymeter.internal.SMAEnergyMeterBindingConstants.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.smaenergymeter.internal.configuration.EnergyMeterConfig;
import org.openhab.binding.smaenergymeter.internal.packet.FilteringPayloadHandler;
import org.openhab.binding.smaenergymeter.internal.packet.PacketListener;
import org.openhab.binding.smaenergymeter.internal.packet.PacketListenerRegistry;
import org.openhab.binding.smaenergymeter.internal.packet.PayloadHandler;
import org.openhab.binding.smaenergymeter.internal.packet.ThrottlingPayloadHandler;
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
 * The {@link SMAEnergyMeterHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Osman Basha - Initial contribution
 */
@NonNullByDefault
public class SMAEnergyMeterHandler extends BaseThingHandler implements PayloadHandler {

    private final Logger logger = LoggerFactory.getLogger(SMAEnergyMeterHandler.class);
    private final PacketListenerRegistry listenerRegistry;
    private @Nullable PacketListener listener;
    private @Nullable PayloadHandler handler;
    private String serialNumber = "";

    public SMAEnergyMeterHandler(Thing thing, PacketListenerRegistry listenerRegistry) {
        super(thing);
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command == RefreshType.REFRESH) {
            logger.debug("Refreshing {}", channelUID);
            PacketListener listener = this.listener;
            if (listener != null) {
                listener.request();
            }
        } else {
            logger.warn("This binding is a read-only binding and cannot handle commands");
        }
    }

    @Override
    public void initialize() {
        logger.debug("Initializing SMAEnergyMeter handler '{}'", getThing().getUID());

        EnergyMeterConfig config = getConfigAs(EnergyMeterConfig.class);

        try {
            serialNumber = Objects.requireNonNullElse(config.getSerialNumber(), "");
            if (serialNumber.isBlank()) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING,
                        "Meter serial number missing");
                return;
            }
            String mcastGroup = config.getMcastGroup();
            if (mcastGroup == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING, "mcast group is missing");
                return;
            }
            PacketListener listener = listenerRegistry.getListener(mcastGroup, config.getPort());
            updateStatus(ThingStatus.UNKNOWN);
            logger.debug("Activated handler for SMA Energy Meter with S/N '{}'", serialNumber);

            if (config.getPollingPeriod() <= 1) {
                listener.addPayloadHandler(handler = new FilteringPayloadHandler(this, serialNumber));
            } else {
                listener.addPayloadHandler(handler = new FilteringPayloadHandler(
                        new ThrottlingPayloadHandler(this, TimeUnit.SECONDS.toMillis(config.getPollingPeriod())),
                        serialNumber));
            }
            this.listener = listener;
            logger.debug("Polling job scheduled to run every {} sec. for '{}'", config.getPollingPeriod(),
                    getThing().getUID());
            // we do not set online status here, it will be set only when data is received
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR, e.getMessage());
        }
    }

    @Override
    public void dispose() {
        logger.debug("Disposing SMAEnergyMeter handler '{}'", getThing().getUID());
        PacketListener listener = this.listener;
        PayloadHandler handler = this.handler;
        if (listener != null && handler != null) {
            listener.removePayloadHandler(handler);
            this.listener = null;
        }
    }

    @Override
    public void handle(EnergyMeter energyMeter) {
        updateStatus(ThingStatus.ONLINE);
        updateThingProperties(energyMeter.getSerialNumber());

        logger.debug("Update SMAEnergyMeter {} data '{}'", serialNumber, getThing().getUID());

        updateState(CHANNEL_POWER_IN, energyMeter.getPowerIn());
        updateState(CHANNEL_POWER_OUT, energyMeter.getPowerOut());
        updateState(CHANNEL_ENERGY_IN, energyMeter.getEnergyIn());
        updateState(CHANNEL_ENERGY_OUT, energyMeter.getEnergyOut());

        updateState(CHANNEL_POWER_IN_L1, energyMeter.getPowerInL1());
        updateState(CHANNEL_POWER_OUT_L1, energyMeter.getPowerOutL1());
        updateState(CHANNEL_ENERGY_IN_L1, energyMeter.getEnergyInL1());
        updateState(CHANNEL_ENERGY_OUT_L1, energyMeter.getEnergyOutL1());

        updateState(CHANNEL_POWER_IN_L2, energyMeter.getPowerInL2());
        updateState(CHANNEL_POWER_OUT_L2, energyMeter.getPowerOutL2());
        updateState(CHANNEL_ENERGY_IN_L2, energyMeter.getEnergyInL2());
        updateState(CHANNEL_ENERGY_OUT_L2, energyMeter.getEnergyOutL2());

        updateState(CHANNEL_POWER_IN_L3, energyMeter.getPowerInL3());
        updateState(CHANNEL_POWER_OUT_L3, energyMeter.getPowerOutL3());
        updateState(CHANNEL_ENERGY_IN_L3, energyMeter.getEnergyInL3());
        updateState(CHANNEL_ENERGY_OUT_L3, energyMeter.getEnergyOutL3());

        updateState(CHANNEL_REACTIVE_POWER_IN, energyMeter.getReactivePowerIn());
        updateState(CHANNEL_REACTIVE_POWER_OUT, energyMeter.getReactivePowerOut());
        updateState(CHANNEL_REACTIVE_ENERGY_IN, energyMeter.getReactiveEnergyIn());
        updateState(CHANNEL_REACTIVE_ENERGY_OUT, energyMeter.getReactiveEnergyOut());
        updateState(CHANNEL_REACTIVE_POWER_IN_L1, energyMeter.getReactivePowerInL1());
        updateState(CHANNEL_REACTIVE_POWER_OUT_L1, energyMeter.getReactivePowerOutL1());
        updateState(CHANNEL_REACTIVE_ENERGY_IN_L1, energyMeter.getReactiveEnergyInL1());
        updateState(CHANNEL_REACTIVE_ENERGY_OUT_L1, energyMeter.getReactiveEnergyOutL1());
        updateState(CHANNEL_REACTIVE_POWER_IN_L2, energyMeter.getReactivePowerInL2());
        updateState(CHANNEL_REACTIVE_POWER_OUT_L2, energyMeter.getReactivePowerOutL2());
        updateState(CHANNEL_REACTIVE_ENERGY_IN_L2, energyMeter.getReactiveEnergyInL2());
        updateState(CHANNEL_REACTIVE_ENERGY_OUT_L2, energyMeter.getReactiveEnergyOutL2());
        updateState(CHANNEL_REACTIVE_POWER_IN_L3, energyMeter.getReactivePowerInL3());
        updateState(CHANNEL_REACTIVE_POWER_OUT_L3, energyMeter.getReactivePowerOutL3());
        updateState(CHANNEL_REACTIVE_ENERGY_IN_L3, energyMeter.getReactiveEnergyInL3());
        updateState(CHANNEL_REACTIVE_ENERGY_OUT_L3, energyMeter.getReactiveEnergyOutL3());

        updateState(CHANNEL_APPARENT_POWER_IN, energyMeter.getApparentPowerIn());
        updateState(CHANNEL_APPARENT_POWER_OUT, energyMeter.getApparentPowerOut());
        updateState(CHANNEL_APPARENT_ENERGY_IN, energyMeter.getApparentEnergyIn());
        updateState(CHANNEL_APPARENT_ENERGY_OUT, energyMeter.getApparentEnergyOut());
        updateState(CHANNEL_APPARENT_POWER_IN_L1, energyMeter.getApparentPowerInL1());
        updateState(CHANNEL_APPARENT_POWER_OUT_L1, energyMeter.getApparentPowerOutL1());
        updateState(CHANNEL_APPARENT_ENERGY_IN_L1, energyMeter.getApparentEnergyInL1());
        updateState(CHANNEL_APPARENT_ENERGY_OUT_L1, energyMeter.getApparentEnergyOutL1());
        updateState(CHANNEL_APPARENT_POWER_IN_L2, energyMeter.getApparentPowerInL2());
        updateState(CHANNEL_APPARENT_POWER_OUT_L2, energyMeter.getApparentPowerOutL2());
        updateState(CHANNEL_APPARENT_ENERGY_IN_L2, energyMeter.getApparentEnergyInL2());
        updateState(CHANNEL_APPARENT_ENERGY_OUT_L2, energyMeter.getApparentEnergyOutL2());
        updateState(CHANNEL_APPARENT_POWER_IN_L3, energyMeter.getApparentPowerInL3());
        updateState(CHANNEL_APPARENT_POWER_OUT_L3, energyMeter.getApparentPowerOutL3());
        updateState(CHANNEL_APPARENT_ENERGY_IN_L3, energyMeter.getApparentEnergyInL3());
        updateState(CHANNEL_APPARENT_ENERGY_OUT_L3, energyMeter.getApparentEnergyOutL3());

        updateState(CHANNEL_POWER_FACTOR, energyMeter.getPowerFactor());
        updateState(CHANNEL_POWER_FACTOR_L1, energyMeter.getPowerFactorL1());
        updateState(CHANNEL_POWER_FACTOR_L2, energyMeter.getPowerFactorL2());
        updateState(CHANNEL_POWER_FACTOR_L3, energyMeter.getPowerFactorL3());
        updateState(CHANNEL_CURRENT_L1, energyMeter.getCurrentL1());
        updateState(CHANNEL_CURRENT_L2, energyMeter.getCurrentL2());
        updateState(CHANNEL_CURRENT_L3, energyMeter.getCurrentL3());
        updateState(CHANNEL_VOLTAGE_L1, energyMeter.getVoltageL1());
        updateState(CHANNEL_VOLTAGE_L2, energyMeter.getVoltageL2());
        updateState(CHANNEL_VOLTAGE_L3, energyMeter.getVoltageL3());
        updateState(CHANNEL_FREQUENCY, energyMeter.getFrequency());
        updateState(CHANNEL_VERSION, energyMeter.getVersion());
    }

    private void updateThingProperties(String actualSerialNumber) {
        Map<String, String> currentProperties = editProperties();
        String currentSerialNumber = currentProperties.get(Thing.PROPERTY_SERIAL_NUMBER);
        String currentVendor = currentProperties.get(Thing.PROPERTY_VENDOR);
        if (actualSerialNumber.equals(currentSerialNumber) && "SMA".equals(currentVendor)) {
            return;
        }

        Map<String, String> updatedProperties = new HashMap<>(currentProperties);
        updatedProperties.put(Thing.PROPERTY_SERIAL_NUMBER, actualSerialNumber);
        updatedProperties.put(Thing.PROPERTY_VENDOR, "SMA");
        updateProperties(updatedProperties);
    }
}
