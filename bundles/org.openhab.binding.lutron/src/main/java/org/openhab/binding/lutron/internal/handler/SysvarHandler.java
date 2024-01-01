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

import static org.openhab.binding.lutron.internal.LutronBindingConstants.CHANNEL_VARSTATE;

import java.math.BigDecimal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lutron.internal.config.SysvarConfig;
import org.openhab.binding.lutron.internal.protocol.SysvarCommand;
import org.openhab.binding.lutron.internal.protocol.lip.LutronCommandType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler responsible for getting/setting sysvar state for HomeWorks QS
 *
 * @author Bob Adair - Initial contribution
 *
 */
@NonNullByDefault
public class SysvarHandler extends LutronHandler {
    private final Logger logger = LoggerFactory.getLogger(SysvarHandler.class);

    private @Nullable SysvarConfig config;
    private int integrationId;

    public SysvarHandler(Thing thing) {
        super(thing);
    }

    @Override
    public int getIntegrationId() {
        SysvarConfig config = this.config;
        if (config != null) {
            return config.integrationId;
        } else {
            throw new IllegalStateException("handler not initialized");
        }
    }

    @Override
    public void initialize() {
        SysvarConfig config = getThing().getConfiguration().as(SysvarConfig.class);
        this.config = config;
        if (config.integrationId <= 0) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No integrationId configured");
        } else {
            integrationId = config.integrationId;
            logger.debug("Initializing Sysvar handler for integration ID {}", integrationId);
            initDeviceState();
        }
    }

    @Override
    protected void initDeviceState() {
        logger.debug("Initializing handler state for sysvar id {}", integrationId);
        Bridge bridge = getBridge();
        if (bridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No bridge configured");
        } else if (bridge.getStatus() == ThingStatus.ONLINE) {
            updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE, "Awaiting initial response");
            querySysvar(SysvarCommand.ACTION_GETSETSYSVAR);
            // handleUpdate() will set thing status to online when response arrives
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        }
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        if (channelUID.getId().equals(CHANNEL_VARSTATE)) {
            // Refresh state when new item is linked.
            querySysvar(SysvarCommand.ACTION_GETSETSYSVAR);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (channelUID.getId().equals(CHANNEL_VARSTATE)) {
            if (command instanceof Number number) {
                int state = number.intValue();
                sysvar(SysvarCommand.ACTION_GETSETSYSVAR, state);
            }
        }
    }

    @Override
    public void handleUpdate(LutronCommandType type, String... parameters) {
        if (type == LutronCommandType.SYSVAR && parameters.length > 1
                && SysvarCommand.ACTION_GETSETSYSVAR.toString().equals(parameters[0])) {
            BigDecimal state = new BigDecimal(parameters[1]);
            if (getThing().getStatus() == ThingStatus.UNKNOWN) {
                updateStatus(ThingStatus.ONLINE);
            }
            updateState(CHANNEL_VARSTATE, new DecimalType(state));
        }
    }
}
