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
package org.openhab.binding.netatmo.internal.handler;

import static org.openhab.binding.netatmo.internal.NetatmoBindingConstants.*;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.netatmo.internal.api.ApiBridge;
import org.openhab.binding.netatmo.internal.api.NetatmoException;
import org.openhab.binding.netatmo.internal.api.SecurityApi;
import org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.FloodLightMode;
import org.openhab.binding.netatmo.internal.api.dto.NAHomeStatusModule;
import org.openhab.binding.netatmo.internal.api.dto.NAObject;
import org.openhab.binding.netatmo.internal.handler.channelhelper.AbstractChannelHelper;
import org.openhab.binding.netatmo.internal.handler.channelhelper.PresenceChannelHelper;
import org.openhab.binding.netatmo.internal.providers.NetatmoDescriptionProvider;
import org.openhab.binding.netatmo.internal.webhook.NetatmoServlet;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link PresenceHandler} is the class used to handle Presence camera data
 *
 * @author Sven Strohschein - Initial contribution
 */
@NonNullByDefault
public class PresenceHandler extends CameraHandler {
    private final Logger logger = LoggerFactory.getLogger(PresenceHandler.class);

    private State autoMode = UnDefType.UNDEF;

    public PresenceHandler(Bridge bridge, List<AbstractChannelHelper> channelHelpers, ApiBridge apiBridge,
            NetatmoDescriptionProvider descriptionProvider, NetatmoServlet webhookServlet) {
        super(bridge, channelHelpers, apiBridge, descriptionProvider, webhookServlet);
        channelHelpers.stream().filter(c -> c instanceof PresenceChannelHelper).findFirst()
                .map(PresenceChannelHelper.class::cast).ifPresent(helper -> helper.setFloodLightMode(autoMode));
    }

    @Override
    public void setNewData(NAObject newData) {
        if (newData instanceof NAHomeStatusModule && autoMode == UnDefType.UNDEF) {
            // Auto-mode state shouldn't be updated, because this isn't a dedicated information. When the floodlight
            // is switched on the state within the Netatmo API is "on" and the information if the previous state was
            // "auto" instead of "off" is lost... Therefore the binding handles its own auto-mode state.
            // TODO : this will have to be controlled by a Presence owner against the new api usage of HomeStatus
            // apparently now AUTO is part of the answer.
            NAHomeStatusModule camera = (NAHomeStatusModule) newData;
            autoMode = OnOffType.from(camera.getFloodlight() == FloodLightMode.AUTO);
        }
        super.setNewData(newData);
    }

    @Override
    protected void internalHandleCommand(String channelName, Command command) {
        switch (channelName) {
            case CHANNEL_FLOODLIGHT:
                if (command == OnOffType.ON) {
                    changeFloodlightMode(FloodLightMode.ON);
                } else {
                    switchFloodlightAutoMode(autoMode == OnOffType.ON);
                }
                return;
            case CHANNEL_FLOODLIGHT_AUTO_MODE:
                switchFloodlightAutoMode(command == OnOffType.ON);
                return;
        }
        super.internalHandleCommand(channelName, command);
    }

    private void switchFloodlightAutoMode(boolean isAutoMode) {
        autoMode = OnOffType.from(isAutoMode);
        changeFloodlightMode(isAutoMode ? FloodLightMode.AUTO : FloodLightMode.OFF);
    }

    private void changeFloodlightMode(FloodLightMode mode) {
        NetatmoHandler bridgeHandler = getBridgeHandler();
        SecurityApi api = apiBridge.getRestManager(SecurityApi.class);
        if (bridgeHandler != null && api != null) {
            String url = cameraHelper.getLocalURL();
            if (url != null) {
                try {
                    api.changeFloodLightMode(url, mode);
                    bridgeHandler.expireData();
                } catch (NetatmoException e) {
                    logger.warn("Error changing Presence floodlight '{}' : {}", getId(), e.getMessage());
                }
            } else {
                logger.info("Switching floodlight is only possible on local Presence.");
            }
        }
    }
}
