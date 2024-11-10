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
package org.openhab.binding.electroluxappliance.internal.handler;

import static org.openhab.binding.electroluxappliance.internal.ElectroluxApplianceBindingConstants.CHANNEL_STATUS;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.electroluxappliance.internal.ElectroluxApplianceConfiguration;
import org.openhab.binding.electroluxappliance.internal.api.ElectroluxGroupAPI;
import org.openhab.binding.electroluxappliance.internal.dto.ApplianceDTO;
import org.openhab.core.thing.Bridge;
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
 * The {@link ElectroluxApplianceHandler} is
 *
 *
 * @author Jan Gustafsson - Initial contribution
 */
@NonNullByDefault
public abstract class ElectroluxApplianceHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(ElectroluxApplianceHandler.class);

    private ElectroluxApplianceConfiguration config = new ElectroluxApplianceConfiguration();

    public ElectroluxApplianceHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Command received: {} on channelID: {}", command, channelUID);
        if (CHANNEL_STATUS.equals(channelUID.getId()) || command instanceof RefreshType) {
            final Bridge bridge = getBridge();
            if (bridge != null && bridge.getHandler() instanceof ElectroluxApplianceBridgeHandler bridgeHandler) {
                bridgeHandler.handleCommand(channelUID, command);
            }
        }
    }

    @Override
    public void initialize() {
        config = getConfigAs(ElectroluxApplianceConfiguration.class);
        if (config.getSerialNumber().isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Configuration of Serial Number is mandatory");
        } else {
            updateStatus(ThingStatus.UNKNOWN);

            scheduler.execute(() -> {
                update();
                Map<String, String> properties = refreshProperties();
                updateProperties(properties);
            });
        }
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
        final Bridge bridge = getBridge();
        if (bridge != null && bridge.getHandler() instanceof ElectroluxApplianceBridgeHandler bridgeHandler) {
            return bridgeHandler.getElectroluxDeltaAPI();
        }
        return null;
    }

    protected @Nullable ApplianceDTO getApplianceDTO() {
        final Bridge bridge = getBridge();
        if (bridge != null && bridge.getHandler() instanceof ElectroluxApplianceBridgeHandler bridgeHandler) {
            return bridgeHandler.getElectroluxApplianceThings().get(config.getSerialNumber());
        }
        return null;
    }

    public abstract void update(@Nullable ApplianceDTO dto);

    public abstract Map<String, String> refreshProperties();
}
