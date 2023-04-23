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
package org.openhab.binding.netatmo.internal.handler.capability;

import static org.openhab.binding.netatmo.internal.NetatmoBindingConstants.*;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.FloodLightMode;
import org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.SirenStatus;
import org.openhab.binding.netatmo.internal.api.dto.HomeStatusModule;
import org.openhab.binding.netatmo.internal.handler.CommonInterface;
import org.openhab.binding.netatmo.internal.handler.channelhelper.ChannelHelper;
import org.openhab.binding.netatmo.internal.providers.NetatmoDescriptionProvider;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link PresenceCapability} give to handle Presence Camera specifics
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class PresenceCapability extends CameraCapability {
    private final Logger logger = LoggerFactory.getLogger(PresenceCapability.class);

    public PresenceCapability(CommonInterface handler, NetatmoDescriptionProvider descriptionProvider,
            List<ChannelHelper> channelHelpers) {
        super(handler, descriptionProvider, channelHelpers);
    }

    @Override
    public void updateHomeStatusModule(HomeStatusModule newData) {
        super.updateHomeStatusModule(newData);
        getSecurityCapability().ifPresent(cap -> cap.changeSirenStatus(handler.getId(), SirenStatus.SOUND));
    }

    @Override
    public void handleCommand(String channelName, Command command) {
        if (CHANNEL_FLOODLIGHT.equals(channelName)) {
            if (command instanceof OnOffType) {
                changeFloodlightMode(command == OnOffType.ON ? FloodLightMode.ON : FloodLightMode.OFF);
                return;
            } else if (command instanceof StringType) {
                try {
                    changeFloodlightMode(FloodLightMode.valueOf(command.toString()));
                } catch (IllegalArgumentException e) {
                    logger.info("Incorrect command '{}' received for channel '{}'", command, channelName);
                }
                return;
            }
        } else if (CHANNEL_SIREN.equals(channelName) && command instanceof OnOffType) {
            getSecurityCapability().ifPresent(cap -> cap.changeSirenStatus(handler.getId(),
                    command == OnOffType.ON ? SirenStatus.SOUND : SirenStatus.NO_SOUND));
            return;
        }
        super.handleCommand(channelName, command);
    }

    private void changeFloodlightMode(FloodLightMode mode) {
        getSecurityCapability().ifPresent(cap -> cap.changeFloodlightMode(handler.getId(), mode));
    }
}
