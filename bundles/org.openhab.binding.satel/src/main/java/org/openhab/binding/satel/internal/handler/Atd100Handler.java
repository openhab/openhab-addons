/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.satel.internal.command.ReadZoneTemperature;
import org.openhab.binding.satel.internal.command.SatelCommand;
import org.openhab.binding.satel.internal.config.Atd100Config;
import org.openhab.binding.satel.internal.event.ZoneTemperatureEvent;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link Atd100Handler} is responsible for handling commands, which are
 * sent to one of the channels of an ATD-100 device.
 *
 * @author Krzysztof Goworek - Initial contribution
 */
@NonNullByDefault
public class Atd100Handler extends WirelessChannelsHandler {

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_ATD100);

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private @Nullable ScheduledFuture<?> pollingJob;

    public Atd100Handler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        super.initialize();

        withBridgeHandlerPresent(bridgeHandler -> {
            final ScheduledFuture<?> pollingJob = this.pollingJob;
            if (pollingJob == null || pollingJob.isCancelled()) {
                Atd100Config config = getConfigAs(Atd100Config.class);
                Runnable pollingCommand = () -> {
                    if (bridgeHandler.getThing().getStatus() == ThingStatus.ONLINE) {
                        bridgeHandler.sendCommand(new ReadZoneTemperature(getThingConfig().getId()), true);
                    }
                };
                this.pollingJob = scheduler.scheduleWithFixedDelay(pollingCommand, 0, config.getRefresh(),
                        TimeUnit.MINUTES);
            }
        });
    }

    @Override
    public void dispose() {
        logger.debug("Disposing thing handler.");

        final ScheduledFuture<?> pollingJob = this.pollingJob;
        if (pollingJob != null && !pollingJob.isCancelled()) {
            pollingJob.cancel(true);
        }
        this.pollingJob = null;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (CHANNEL_TEMPERATURE.equals(channelUID.getId())) {
            logger.debug("New command for {}: {}", channelUID, command);

            if (command == RefreshType.REFRESH) {
                withBridgeHandlerPresent(bridgeHandler -> {
                    bridgeHandler.sendCommand(new ReadZoneTemperature(getThingConfig().getId()), true);
                });
            }
        } else {
            super.handleCommand(channelUID, command);
        }
    }

    @Override
    public void incomingEvent(ZoneTemperatureEvent event) {
        logger.trace("Handling incoming event: {}", event);
        if (event.getZoneNbr() == getThingConfig().getId()) {
            updateState(CHANNEL_TEMPERATURE, new QuantityType<>(event.getTemperature(), SIUnits.CELSIUS));
        }
    }

    @Override
    protected boolean isWirelessDevice() {
        return true;
    }

    @Override
    protected Optional<SatelCommand> convertCommand(@Nullable ChannelUID channel, @Nullable Command command) {
        // no commands supported
        return Optional.empty();
    }
}
