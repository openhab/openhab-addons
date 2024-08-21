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
package org.openhab.binding.lutron.internal.handler;

import static org.openhab.binding.lutron.internal.LutronBindingConstants.*;

import java.math.BigDecimal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lutron.internal.config.FanConfig;
import org.openhab.binding.lutron.internal.protocol.FanSpeedType;
import org.openhab.binding.lutron.internal.protocol.OutputCommand;
import org.openhab.binding.lutron.internal.protocol.lip.LutronCommandType;
import org.openhab.binding.lutron.internal.protocol.lip.LutronOperation;
import org.openhab.binding.lutron.internal.protocol.lip.TargetType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
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
 * Handler responsible for communicating with ceiling fan speed controllers.
 *
 * @author Bob Adair - Initial contribution
 */
@NonNullByDefault
public class FanHandler extends LutronHandler {
    private final Logger logger = LoggerFactory.getLogger(FanHandler.class);

    private FanConfig config = new FanConfig();

    public FanHandler(Thing thing) {
        super(thing);
    }

    @Override
    public int getIntegrationId() {
        if (config.integrationId <= 0) {
            throw new IllegalStateException("handler not initialized");
        } else {
            return config.integrationId;
        }
    }

    @Override
    public void initialize() {
        config = getConfigAs(FanConfig.class);
        if (config.integrationId <= 0) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No integrationId configured");
            return;
        }
        logger.debug("Initializing Fan handler for integration ID {}", getIntegrationId());

        initDeviceState();
    }

    @Override
    protected void initDeviceState() {
        logger.debug("Initializing device state for Fan {}", getIntegrationId());
        Bridge bridge = getBridge();
        if (bridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No bridge configured");
        } else if (bridge.getStatus() == ThingStatus.ONLINE) {
            updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE, "Awaiting initial response");
            queryOutput(TargetType.FAN, OutputCommand.ACTION_ZONELEVEL);
            // handleUpdate() will set thing status to online when response arrives
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        }
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        if (channelUID.getId().equals(CHANNEL_FANSPEED) || channelUID.getId().equals(CHANNEL_FANLEVEL)) {
            // Refresh state when new item is linked.
            queryOutput(TargetType.FAN, OutputCommand.ACTION_ZONELEVEL);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (channelUID.getId().equals(CHANNEL_FANLEVEL)) {
            if (command instanceof Number number) {
                int level = number.intValue();
                FanSpeedType speed = FanSpeedType.toFanSpeedType(level);
                // output(TargetType.FAN, LutronCommand.ACTION_ZONELEVEL, level, null, null);
                sendCommand(new OutputCommand(TargetType.FAN, LutronOperation.EXECUTE, getIntegrationId(),
                        OutputCommand.ACTION_ZONELEVEL, speed, null, null));
            } else if (command.equals(OnOffType.ON)) {
                // output(TargetType.FAN, LutronCommand.ACTION_ZONELEVEL, 100, null, null);
                sendCommand(new OutputCommand(TargetType.FAN, LutronOperation.EXECUTE, getIntegrationId(),
                        OutputCommand.ACTION_ZONELEVEL, FanSpeedType.HIGH, null, null));
            } else if (command.equals(OnOffType.OFF)) {
                // output(TargetType.FAN, LutronCommand.ACTION_ZONELEVEL, 0, null, null);
                sendCommand(new OutputCommand(TargetType.FAN, LutronOperation.EXECUTE, getIntegrationId(),
                        OutputCommand.ACTION_ZONELEVEL, FanSpeedType.OFF, null, null));
            }
        } else if (channelUID.getId().equals(CHANNEL_FANSPEED)) {
            if (command instanceof StringType) {
                FanSpeedType speed = FanSpeedType.toFanSpeedType(command.toString());
                sendCommand(new OutputCommand(TargetType.FAN, LutronOperation.EXECUTE, getIntegrationId(),
                        OutputCommand.ACTION_ZONELEVEL, speed, null, null));
            }
        }
    }

    @Override
    public void handleUpdate(LutronCommandType type, String... parameters) {
        if (type == LutronCommandType.OUTPUT && parameters.length > 1
                && OutputCommand.ACTION_ZONELEVEL.toString().equals(parameters[0])) {
            BigDecimal level = new BigDecimal(parameters[1]);
            if (getThing().getStatus() == ThingStatus.UNKNOWN) {
                updateStatus(ThingStatus.ONLINE);
            }
            updateState(CHANNEL_FANLEVEL, new PercentType(level));
            FanSpeedType fanSpeed = FanSpeedType.toFanSpeedType(level.intValue());
            updateState(CHANNEL_FANSPEED, new StringType(fanSpeed.toString()));
        }
    }
}
