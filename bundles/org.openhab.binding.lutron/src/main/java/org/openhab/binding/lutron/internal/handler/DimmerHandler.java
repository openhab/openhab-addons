/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.lutron.internal.handler;

import static org.openhab.binding.lutron.internal.LutronBindingConstants.CHANNEL_LIGHTLEVEL;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

import org.openhab.binding.lutron.internal.action.DimmerActions;
import org.openhab.binding.lutron.internal.config.DimmerConfig;
import org.openhab.binding.lutron.internal.protocol.LutronDuration;
import org.openhab.binding.lutron.internal.protocol.OutputCommand;
import org.openhab.binding.lutron.internal.protocol.lip.LutronCommandType;
import org.openhab.binding.lutron.internal.protocol.lip.TargetType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler responsible for communicating with a light dimmer.
 *
 * @author Allan Tong - Initial contribution
 * @author Bob Adair - Added initDeviceState method, and onLevel and onToLast parameters
 */
public class DimmerHandler extends LutronHandler {
    private final Logger logger = LoggerFactory.getLogger(DimmerHandler.class);
    private DimmerConfig config;
    private LutronDuration fadeInTime;
    private LutronDuration fadeOutTime;
    private final AtomicReference<BigDecimal> lastLightLevel = new AtomicReference<>();

    public DimmerHandler(Thing thing) {
        super(thing);
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singletonList(DimmerActions.class);
    }

    @Override
    public int getIntegrationId() {
        if (config == null) {
            throw new IllegalStateException("handler not initialized");
        }

        return config.integrationId;
    }

    @Override
    public void initialize() {
        config = getThing().getConfiguration().as(DimmerConfig.class);
        if (config.integrationId <= 0) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No integrationId configured");
            return;
        }
        fadeInTime = new LutronDuration(config.fadeInTime);
        fadeOutTime = new LutronDuration(config.fadeOutTime);
        logger.debug("Initializing Dimmer handler for integration ID {}", getIntegrationId());

        initDeviceState();
    }

    @Override
    protected void initDeviceState() {
        logger.debug("Initializing device state for Dimmer {}", getIntegrationId());
        Bridge bridge = getBridge();
        if (bridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No bridge configured");
        } else if (bridge.getStatus() == ThingStatus.ONLINE) {
            updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE, "Awaiting initial response");
            queryOutput(TargetType.DIMMER, OutputCommand.ACTION_ZONELEVEL);
            // handleUpdate() will set thing status to online when response arrives
            lastLightLevel.set(config.onLevel);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        }
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        if (channelUID.getId().equals(CHANNEL_LIGHTLEVEL)) {
            // Refresh state when new item is linked.
            queryOutput(TargetType.DIMMER, OutputCommand.ACTION_ZONELEVEL);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (channelUID.getId().equals(CHANNEL_LIGHTLEVEL)) {
            if (command instanceof Number) {
                int level = ((Number) command).intValue();
                output(TargetType.DIMMER, OutputCommand.ACTION_ZONELEVEL, level, new LutronDuration("0.25"), null);
            } else if (command.equals(OnOffType.ON)) {
                if (config.onToLast) {
                    output(TargetType.DIMMER, OutputCommand.ACTION_ZONELEVEL, lastLightLevel.get(), fadeInTime, null);
                } else {
                    output(TargetType.DIMMER, OutputCommand.ACTION_ZONELEVEL, config.onLevel, fadeInTime, null);
                }
            } else if (command.equals(OnOffType.OFF)) {
                output(TargetType.DIMMER, OutputCommand.ACTION_ZONELEVEL, 0, fadeOutTime, null);
            }
        }
    }

    public void setLightLevel(BigDecimal level, LutronDuration fade, LutronDuration delay) {
        int intLevel = level.intValue();
        output(TargetType.DIMMER, OutputCommand.ACTION_ZONELEVEL, intLevel, fade, delay);
    }

    @Override
    public void handleUpdate(LutronCommandType type, String... parameters) {
        if (type == LutronCommandType.OUTPUT && parameters.length > 1
                && OutputCommand.ACTION_ZONELEVEL.toString().equals(parameters[0])) {
            BigDecimal level = new BigDecimal(parameters[1]);
            if (getThing().getStatus() == ThingStatus.UNKNOWN) {
                updateStatus(ThingStatus.ONLINE);
            }
            if (level.compareTo(BigDecimal.ZERO) == 1) { // if (level > 0)
                lastLightLevel.set(level);
            }
            updateState(CHANNEL_LIGHTLEVEL, new PercentType(level));
        }
    }
}
