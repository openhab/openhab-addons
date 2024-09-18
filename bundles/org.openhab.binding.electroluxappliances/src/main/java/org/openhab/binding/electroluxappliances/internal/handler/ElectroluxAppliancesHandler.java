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
package org.openhab.binding.electroluxappliances.internal.handler;

import static org.openhab.binding.electroluxappliances.internal.ElectroluxAppliancesBindingConstants.CHANNEL_STATUS;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.electroluxappliances.internal.ElectroluxAppliancesConfiguration;
import org.openhab.binding.electroluxappliances.internal.api.ElectroluxGroupAPI;
import org.openhab.binding.electroluxappliances.internal.dto.ApplianceDTO;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.BridgeHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ElectroluxAppliancesHandler} is
 *
 *
 * @author Jan Gustafsson - Initial contribution
 */
@NonNullByDefault
public abstract class ElectroluxAppliancesHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(ElectroluxAppliancesHandler.class);

    private ElectroluxAppliancesConfiguration config = new ElectroluxAppliancesConfiguration();

    public ElectroluxAppliancesHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Command received: {} on channelID: {}", command, channelUID);
        if (CHANNEL_STATUS.equals(channelUID.getId()) || command instanceof RefreshType) {
            Bridge bridge = getBridge();
            if (bridge != null) {
                BridgeHandler bridgeHandler = bridge.getHandler();
                if (bridgeHandler != null) {
                    bridgeHandler.handleCommand(channelUID, command);
                }
            }
        }
    }

    @Override
    public void initialize() {
        config = getConfigAs(ElectroluxAppliancesConfiguration.class);
        updateStatus(ThingStatus.UNKNOWN);

        scheduler.execute(() -> {
            update();
            Map<String, String> properties = refreshProperties();
            updateProperties(properties);
        });
    }

    public void update() {
        ApplianceDTO dto = getApplianceDTO();
        if (dto != null) {
            update(dto);
        } else {
            logger.warn("AppliancedDTO is null!");
        }
    }

    protected @Nullable ElectroluxGroupAPI getElectroluxGroupAPI() {
        Bridge bridge = getBridge();
        if (bridge != null) {
            ElectroluxAppliancesBridgeHandler handler = (ElectroluxAppliancesBridgeHandler) bridge.getHandler();
            if (handler != null) {
                return handler.getElectroluxDeltaAPI();
            }
        }
        return null;
    }

    protected @Nullable ApplianceDTO getApplianceDTO() {
        Bridge bridge = getBridge();
        if (bridge != null) {
            ElectroluxAppliancesBridgeHandler bridgeHandler = (ElectroluxAppliancesBridgeHandler) bridge.getHandler();
            if (bridgeHandler != null) {
                return bridgeHandler.getElectroluxAppliancesThings().get(config.getSerialNumber());
            }
        }
        return null;
    }

    public abstract void update(@Nullable ApplianceDTO dto);

    public abstract Map<String, String> refreshProperties();
}
