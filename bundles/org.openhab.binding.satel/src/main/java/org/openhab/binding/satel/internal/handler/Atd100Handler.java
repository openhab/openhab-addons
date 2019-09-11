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
package org.openhab.binding.satel.internal.handler;

import static org.openhab.binding.satel.internal.SatelBindingConstants.*;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.measure.quantity.Temperature;

import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.satel.internal.command.ReadZoneTemperature;
import org.openhab.binding.satel.internal.command.SatelCommand;
import org.openhab.binding.satel.internal.config.Atd100Config;
import org.openhab.binding.satel.internal.event.SatelEvent;
import org.openhab.binding.satel.internal.event.ZoneTemperatureEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link Atd100Handler} is responsible for handling commands, which are
 * sent to one of the channels of a ATD-100 device.
 *
 * @author Krzysztof Goworek - Initial contribution
 */
public class Atd100Handler extends WirelessChannelsHandler {

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_ATD100);

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private ScheduledFuture<?> pollingJob;

    public Atd100Handler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        super.initialize();

        if (pollingJob == null || pollingJob.isCancelled()) {
            Atd100Config config = getConfigAs(Atd100Config.class);
            Runnable pollingCommand = () -> {
                if (bridgeHandler.getThing().getStatus() == ThingStatus.ONLINE) {
                    bridgeHandler.sendCommand(new ReadZoneTemperature(thingConfig.getId()), true);
                }
            };
            pollingJob = scheduler.scheduleWithFixedDelay(pollingCommand, 0, config.getRefresh(), TimeUnit.MINUTES);
        }
    }

    @Override
    public void dispose() {
        logger.debug("Disposing thing handler.");

        if (pollingJob != null && !pollingJob.isCancelled()) {
            pollingJob.cancel(true);
            pollingJob = null;
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (CHANNEL_TEMPERATURE.equals(channelUID.getId())) {
            logger.debug("New command for {}: {}", channelUID, command);

            if (command == RefreshType.REFRESH) {
                bridgeHandler.sendCommand(new ReadZoneTemperature(thingConfig.getId()), true);
            }
        } else {
            super.handleCommand(channelUID, command);
        }
    }

    @Override
    public void incomingEvent(SatelEvent event) {
        if (event instanceof ZoneTemperatureEvent) {
            logger.trace("Handling incoming event: {}", event);
            ZoneTemperatureEvent statusEvent = (ZoneTemperatureEvent) event;
            if (statusEvent.getZoneNbr() == thingConfig.getId()) {
                updateState(CHANNEL_TEMPERATURE,
                        new QuantityType<Temperature>(statusEvent.getTemperature(), SIUnits.CELSIUS));
            }
        } else {
            super.incomingEvent(event);
        }
    }

    @Override
    protected boolean isWirelessDevice() {
        return true;
    }

    @Override
    protected SatelCommand convertCommand(ChannelUID channel, Command command) {
        // no commands supported
        return null;
    }

}
