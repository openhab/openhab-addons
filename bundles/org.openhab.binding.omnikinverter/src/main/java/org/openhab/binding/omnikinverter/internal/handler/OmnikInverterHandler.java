/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.omnikinverter.internal.OmnikInverter;
import org.openhab.binding.omnikinverter.internal.OmnikInverterBindingConstants;
import org.openhab.binding.omnikinverter.internal.OmnikInverterConfiguration;
import org.openhab.binding.omnikinverter.internal.OmnikInverterMessage;
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
    private @Nullable OmnikInverter inverter;

    private final Logger logger = LoggerFactory.getLogger(OmnikInverterHandler.class);

    public OmnikInverterHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (OmnikInverterBindingConstants.CHANNEL_POWER.equals(channelUID.getId())) {
            if (command instanceof RefreshType) {
                updateData();
            }
            // TODO: handle command

            // Note: if communication with thing fails for some reason,
            // indicate that by setting the status with detail information
            // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
            // "Could not control device at IP address x.x.x.x");

        }
    }

    @Override
    public void initialize() {
        OmnikInverterConfiguration config = getConfigAs(OmnikInverterConfiguration.class);

        try {
            inverter = new OmnikInverter(config.hostname, config.port, config.serial);
            scheduler.scheduleWithFixedDelay(this::updateData, 0, 10, TimeUnit.SECONDS);
        } catch (IOException e) {
            logger.debug("Could not instantiate OmnikInverter object: {}", e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR,
                    "Failed to initialize: " + e.getMessage());
        }

    }

    private void updateData() {
        try {
            if (inverter != null) {
                OmnikInverterMessage message = inverter.pullCurrentStats();

                updateStatus(ThingStatus.ONLINE);

                QuantityType<Power> powerQuantity = new QuantityType<>(message.getPower(), SmartHomeUnits.WATT);
                updateState(OmnikInverterBindingConstants.CHANNEL_POWER, powerQuantity);

                updateState(OmnikInverterBindingConstants.CHANNEL_ENERGY_TODAY,
                        new QuantityType<Energy>(message.getEnergyToday(), SmartHomeUnits.KILOWATT_HOUR));
            }
        } catch (UnknownHostException | NoRouteToHostException | ConnectException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        } catch (UnknownHostException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Unknown host provided");
        } catch (java.net.NoRouteToHostException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "No route to host");

        } catch (ConnectException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Host does not allow socket connection");
        } catch (IOException e) {
            logger.debug(e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Unknown communication exception");
        }
    }

    @Override
    public void initialize() {
        OmnikInverterConfiguration config = getConfigAs(OmnikInverterConfiguration.class);

        inverter = new OmnikInverter(config.omnikHostname, config.omnikPort, config.omnikSerial);

        // TODO: Initialize the thing. If done set status to ONLINE to indicate proper working.
        // Long running initialization should be done asynchronously in background.
        scheduler.scheduleWithFixedDelay(this::updateData, 0, 10, TimeUnit.SECONDS);
        // Note: When initialization can NOT be done set the status with more details for further
        // analysis. See also class ThingStatusDetail for all available status details.
        // Add a description to give user information to understand why thing does not work
        // as expected. E.g.
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
        // "Can not access device as username and/or password are invalid");
    }
}
