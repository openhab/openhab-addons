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
package org.openhab.binding.omnikinverter.internal.handler;

import java.io.IOException;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.UnknownHostException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.measure.quantity.Power;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.omnikinverter.internal.OmnikInverter;
import org.openhab.binding.omnikinverter.internal.OmnikInverterBindingConstants;
import org.openhab.binding.omnikinverter.internal.OmnikInverterConfiguration;
import org.openhab.binding.omnikinverter.internal.OmnikInverterMessage;
import org.openhab.core.library.types.QuantityType;
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
 * The {@link OmnikInverterHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Hans van den Bogert - Initial contribution
 */
@NonNullByDefault
public class OmnikInverterHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(OmnikInverterHandler.class);

    private @Nullable OmnikInverter inverter;
    private @Nullable ScheduledFuture<?> pollJob;

    public OmnikInverterHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (OmnikInverterBindingConstants.CHANNEL_POWER.equals(channelUID.getId())) {
            if (command instanceof RefreshType) {
                updateData();
            }
        }
    }

    @Override
    public void initialize() {
        OmnikInverterConfiguration config = getConfigAs(OmnikInverterConfiguration.class);

        inverter = new OmnikInverter(config.hostname, config.port, config.serial);
        updateStatus(ThingStatus.UNKNOWN);
        pollJob = scheduler.scheduleWithFixedDelay(this::updateData, 0, 10, TimeUnit.SECONDS);
    }

    @Override
    public void dispose() {
        ScheduledFuture<?> pollJob = this.pollJob;
        if (pollJob != null) {
            pollJob.cancel(true);
            this.pollJob = null;
        }
        super.dispose();
    }

    private void updateData() {
        try {
            if (inverter != null) {
                OmnikInverterMessage message = inverter.pullCurrentStats();

                updateStatus(ThingStatus.ONLINE);

                QuantityType<Power> powerQuantity = new QuantityType<>(message.getPower(), Units.WATT);
                updateState(OmnikInverterBindingConstants.CHANNEL_POWER, powerQuantity);

                updateState(OmnikInverterBindingConstants.CHANNEL_ENERGY_TODAY,
                        new QuantityType<>(message.getEnergyToday(), Units.KILOWATT_HOUR));

                updateState(OmnikInverterBindingConstants.CHANNEL_ENERGY_TOTAL,
                        new QuantityType<>(message.getTotalEnergy(), Units.KILOWATT_HOUR));
            }
        } catch (UnknownHostException | NoRouteToHostException | ConnectException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        } catch (IOException e) {
            logger.debug("Unknown exception when pulling data from the inverter: {}", e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "Unknown error: " + e.getMessage());
        }
    }
}
