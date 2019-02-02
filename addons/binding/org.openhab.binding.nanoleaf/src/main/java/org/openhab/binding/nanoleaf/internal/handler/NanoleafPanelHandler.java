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
package org.openhab.binding.nanoleaf.internal.handler;

import static org.openhab.binding.nanoleaf.internal.NanoleafBindingConstants.*;

import java.util.Arrays;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.nanoleaf.internal.NanoleafBindingConstants;
import org.openhab.binding.nanoleaf.internal.NanoleafException;
import org.openhab.binding.nanoleaf.internal.NanoleafUnauthorizedException;
import org.openhab.binding.nanoleaf.internal.OpenAPIUtils;
import org.openhab.binding.nanoleaf.internal.config.NanoleafControllerConfig;
import org.openhab.binding.nanoleaf.internal.config.NanoleafPanelConfig;
import org.openhab.binding.nanoleaf.internal.model.Effects;
import org.openhab.binding.nanoleaf.internal.model.Write;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link NanoleafPanelHandler} is responsible for handling commands to the controller which
 * affect an individual panels
 *
 * @author Martin Raepple - Initial contribution
 */
public class NanoleafPanelHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(NanoleafPanelHandler.class);
    private HttpClient httpClient;
    // JSON parser for API responses
    private final Gson gson = new Gson();

    public NanoleafPanelHandler(Thing thing, HttpClient httpClient) {
        super(thing);
        this.httpClient = httpClient;
    }

    @Override
    public void initialize() {
        logger.debug("initializing handler for panel {}", getThing().getUID());
        updateStatus(ThingStatus.OFFLINE);
        Bridge controller = getBridge();
        if (controller == null) {
            initializePanel(null, null);
        } else {
            initializePanel(controller.getHandler(), controller.getStatus());
        }
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo controllerStatusInfo) {
        logger.debug("Controller status changed to {}", controllerStatusInfo);
        Bridge controller = getBridge();
        if (controller == null) {
            initializePanel(null, controllerStatusInfo.getStatus());
        } else {
            initializePanel(controller.getHandler(), controllerStatusInfo.getStatus());
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Received command {} for channel {}", command, channelUID);
        try {
            switch (channelUID.getId()) {
                case CHANNEL_PANEL_COLOR:
                    sendRenderedEffectCommand(command);
                    break;
                case CHANNEL_PANEL_BRIGHTNESS:
                    sendRenderedEffectCommand(command);
                    break;
                default:
                    logger.warn("Channel with id {} not handled", channelUID.getId());
                    break;
            }
        } catch (NanoleafUnauthorizedException nae) {
            logger.warn("Authorization for command {} for channelUID {} failed: {}", command, channelUID,
                    nae.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Invalid token. Replace with valid token or start pairing again.");
            NanoleafControllerConfig controllerConfig = getConfigAs(NanoleafControllerConfig.class);
            if (StringUtils.isEmpty(controllerConfig.authToken)) {
                Bridge bridge = getBridge();
                if (bridge != null && bridge.getHandler() != null) {
                    ((NanoleafControllerHandler) bridge.getHandler()).startPairingJob();
                }
            }
        } catch (NanoleafException ne) {
            logger.warn("Handling command {} for channelUID {} failed: {}", command, channelUID, ne.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Communication failed. Please check configuration");
        }
    }

    private void initializePanel(ThingHandler controllerHandler, ThingStatus controllerStatus) {
        if (controllerHandler != null && controllerStatus != null) {
            updateStatus(controllerStatus);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED);
        }
        logger.debug("Panel {} status changed to {}", this.getThing().getUID(), controllerStatus);
    }

    private void sendRenderedEffectCommand(Command command) throws NanoleafException {
        Effects effects = new Effects();
        Write write = new Write();
        write.setCommand("display");
        write.setAnimType("static");
        int red = 0;
        int green = 0;
        int blue = 0;
        if (command instanceof HSBType) {
            red = ((HSBType) command).getRed().intValue();
            green = ((HSBType) command).getGreen().intValue();
            blue = ((HSBType) command).getBlue().intValue();
        } else if (command instanceof OnOffType) {
            if (OnOffType.ON.equals(command)) {
                red = 255;
                green = 255;
                blue = 255;
            }
        } else if (command instanceof PercentType) {
            // handle brightness command - either via color or brightness channel
            HSBType currentValue = getPanelData(
                    getThing().getConfiguration().get(NanoleafBindingConstants.CONFIG_PANEL_ID).toString());
            if (currentValue != null) {
                HSBType newValue = new HSBType(currentValue.getHue(), currentValue.getSaturation(),
                        (PercentType) command);
                red = newValue.getRed().intValue();
                green = newValue.getGreen().intValue();
                blue = newValue.getBlue().intValue();
            } else {
                // do nothing
                return;
            }
        } else if (command instanceof RefreshType) {
            logger.debug("Refresh command received");
        } else {
            logger.warn("Unhandled command type: {}", command.getClass().getName());
            return;
        }
        String panelID = this.thing.getConfiguration().get(NanoleafPanelConfig.ID).toString();
        write.setAnimData(String.format("1 %s 1 %d %d %d 0 10", panelID, red, green, blue));
        write.setLoop(false);
        effects.setWrite(write);
        Bridge bridge = getBridge();
        if (bridge != null) {
            Configuration config = bridge.getConfiguration();
            Request setNewRenderedEffectRequest = OpenAPIUtils.requestBuilder(httpClient, config, API_SELECT_EFFECT,
                    HttpMethod.PUT);
            setNewRenderedEffectRequest.content(new StringContentProvider(gson.toJson(effects)), "application/json");
            OpenAPIUtils.sendOpenAPIRequest(setNewRenderedEffectRequest);
        }
    }

    private HSBType getPanelData(String panelID) {
        try {
            Effects effects = new Effects();
            Write write = new Write();
            write.setCommand("request");
            write.setAnimName("*Static*");
            effects.setWrite(write);
            Bridge bridge = getBridge();
            if (bridge != null) {
                Configuration config = bridge.getConfiguration();
                Request setPanelUpdateRequest = OpenAPIUtils.requestBuilder(httpClient, config, API_SELECT_EFFECT,
                        HttpMethod.PUT);
                setPanelUpdateRequest.content(new StringContentProvider(gson.toJson(effects)), "application/json");
                ContentResponse panelData = OpenAPIUtils.sendOpenAPIRequest(setPanelUpdateRequest);
                // parse panel data
                Write response = gson.fromJson(panelData.getContentAsString(), Write.class);
                // panelData is in format (numPanels, (PanelId, 1, R, G, B, W, TransitionTime) * numPanel)
                String[] tokennizedData = response.getAnimData().split(" ");
                String[] panelDataPoints = Arrays.copyOfRange(tokennizedData, 1, tokennizedData.length);
                for (int i = 0; i < panelDataPoints.length; i++) {
                    if ((i % 7) == 0) {
                        String id = panelDataPoints[i];
                        if (id.equals(panelID)) {
                            new HSBType();
                            // found panel data
                            HSBType hsbType = HSBType.fromRGB(Integer.parseInt(panelDataPoints[i + 2]),
                                    Integer.parseInt(panelDataPoints[i + 3]), Integer.parseInt(panelDataPoints[i + 4]));
                            return hsbType;
                        }
                    }
                }
            }
        } catch (NanoleafException nue) {
            logger.warn("Panel data could not be retrieved: " + nue.getMessage());
        }
        return null;
    }
}