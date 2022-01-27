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
package org.openhab.binding.nanoleaf.internal.handler;

import static org.openhab.binding.nanoleaf.internal.NanoleafBindingConstants.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.nanoleaf.internal.NanoleafBadRequestException;
import org.openhab.binding.nanoleaf.internal.NanoleafException;
import org.openhab.binding.nanoleaf.internal.NanoleafNotFoundException;
import org.openhab.binding.nanoleaf.internal.NanoleafUnauthorizedException;
import org.openhab.binding.nanoleaf.internal.OpenAPIUtils;
import org.openhab.binding.nanoleaf.internal.config.NanoleafControllerConfig;
import org.openhab.binding.nanoleaf.internal.model.Effects;
import org.openhab.binding.nanoleaf.internal.model.Write;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.CommonTriggerEvents;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.BridgeHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link NanoleafPanelHandler} is responsible for handling commands to the controller which
 * affect an individual panels
 *
 * @author Martin Raepple - Initial contribution
 * @author Stefan Höhn - Canvas Touch Support
 */
@NonNullByDefault
public class NanoleafPanelHandler extends BaseThingHandler {

    private static final PercentType MIN_PANEL_BRIGHTNESS = PercentType.ZERO;
    private static final PercentType MAX_PANEL_BRIGHTNESS = PercentType.HUNDRED;

    private final Logger logger = LoggerFactory.getLogger(NanoleafPanelHandler.class);

    private HttpClient httpClient;
    // JSON parser for API responses
    private final Gson gson = new Gson();

    // holds current color data per panel
    private Map<String, HSBType> panelInfo = new HashMap<>();

    private @NonNullByDefault({}) ScheduledFuture<?> singleTapJob;
    private @NonNullByDefault({}) ScheduledFuture<?> doubleTapJob;

    public NanoleafPanelHandler(Thing thing, HttpClientFactory httpClientFactory) {
        super(thing);
        this.httpClient = httpClientFactory.getCommonHttpClient();
    }

    @Override
    public void initialize() {
        logger.debug("Initializing handler for panel {}", getThing().getUID());
        Bridge controller = getBridge();
        if (controller == null) {
            initializePanel(new ThingStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED, ""));
        } else if (ThingStatus.OFFLINE.equals(controller.getStatus())) {
            initializePanel(new ThingStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE,
                    "@text/error.nanoleaf.panel.controllerOffline"));
        } else {
            initializePanel(controller.getStatusInfo());
        }
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo controllerStatusInfo) {
        logger.debug("Controller status changed to {} -- {}", controllerStatusInfo,
                controllerStatusInfo.getDescription() + "/" + controllerStatusInfo.getStatus() + "/"
                        + controllerStatusInfo.hashCode());
        if (controllerStatusInfo.getStatus().equals(ThingStatus.OFFLINE)) {
            initializePanel(new ThingStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE,
                    "@text/error.nanoleaf.panel.controllerOffline"));
        } else {
            initializePanel(controllerStatusInfo);
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
                default:
                    logger.warn("Channel with id {} not handled", channelUID.getId());
                    break;
            }
        } catch (NanoleafUnauthorizedException nae) {
            logger.warn("Authorization for command {} for channelUID {} failed: {}", command, channelUID,
                    nae.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/error.nanoleaf.controller.invalidToken");
        } catch (NanoleafException ne) {
            logger.warn("Handling command {} for channelUID {} failed: {}", command, channelUID, ne.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/error.nanoleaf.controller.communication");
        }
    }

    @Override
    public void handleRemoval() {
        logger.debug("Nanoleaf panel {} removed", getThing().getUID());
        super.handleRemoval();
    }

    @Override
    public void dispose() {
        logger.debug("Disposing handler for Nanoleaf panel {}", getThing().getUID());
        stopAllJobs();
        super.dispose();
    }

    private void stopAllJobs() {
        if (singleTapJob != null && !singleTapJob.isCancelled()) {
            logger.debug("Stop single touch job");
            singleTapJob.cancel(true);
            this.singleTapJob = null;
        }
        if (doubleTapJob != null && !doubleTapJob.isCancelled()) {
            logger.debug("Stop double touch job");
            doubleTapJob.cancel(true);
            this.doubleTapJob = null;
        }
    }

    private void initializePanel(ThingStatusInfo panelStatus) {
        updateStatus(panelStatus.getStatus(), panelStatus.getStatusDetail());
        logger.debug("Panel {} status changed to {}-{}", this.getThing().getUID(), panelStatus.getStatus(),
                panelStatus.getStatusDetail());
    }

    private void sendRenderedEffectCommand(Command command) throws NanoleafException {
        logger.debug("Command Type: {}", command.getClass());
        HSBType currentPanelColor = getPanelColor();
        if (currentPanelColor != null) {
            logger.debug("currentPanelColor: {}", currentPanelColor.toString());
        }
        HSBType newPanelColor = new HSBType();

        if (command instanceof HSBType) {
            newPanelColor = (HSBType) command;
        } else if (command instanceof OnOffType && (currentPanelColor != null)) {
            if (OnOffType.ON.equals(command)) {
                newPanelColor = new HSBType(currentPanelColor.getHue(), currentPanelColor.getSaturation(),
                        MAX_PANEL_BRIGHTNESS);
            } else {
                newPanelColor = new HSBType(currentPanelColor.getHue(), currentPanelColor.getSaturation(),
                        MIN_PANEL_BRIGHTNESS);
            }
        } else if (command instanceof PercentType && (currentPanelColor != null)) {
            PercentType brightness = new PercentType(
                    Math.max(MIN_PANEL_BRIGHTNESS.intValue(), ((PercentType) command).intValue()));
            newPanelColor = new HSBType(currentPanelColor.getHue(), currentPanelColor.getSaturation(), brightness);
        } else if (command instanceof IncreaseDecreaseType && (currentPanelColor != null)) {
            int brightness = currentPanelColor.getBrightness().intValue();
            if (command.equals(IncreaseDecreaseType.INCREASE)) {
                brightness = Math.min(MAX_PANEL_BRIGHTNESS.intValue(), brightness + BRIGHTNESS_STEP_SIZE);
            } else {
                brightness = Math.max(MIN_PANEL_BRIGHTNESS.intValue(), brightness - BRIGHTNESS_STEP_SIZE);
            }
            newPanelColor = new HSBType(currentPanelColor.getHue(), currentPanelColor.getSaturation(),
                    new PercentType(brightness));
        } else if (command instanceof RefreshType) {
            logger.debug("Refresh command received");
            return;
        } else {
            logger.warn("Unhandled command type: {}", command.getClass().getName());
            return;
        }
        // store panel's new HSB value
        logger.trace("Setting new color {}", newPanelColor);
        panelInfo.put(getThing().getConfiguration().get(CONFIG_PANEL_ID).toString(), newPanelColor);
        // transform to RGB
        PercentType[] rgbPercent = newPanelColor.toRGB();
        logger.trace("Setting new rgbpercent {} {} {}", rgbPercent[0], rgbPercent[1], rgbPercent[2]);
        int red = rgbPercent[0].toBigDecimal().divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                .multiply(new BigDecimal(255)).intValue();
        int green = rgbPercent[1].toBigDecimal().divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                .multiply(new BigDecimal(255)).intValue();
        int blue = rgbPercent[2].toBigDecimal().divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                .multiply(new BigDecimal(255)).intValue();
        logger.trace("Setting new rgb {} {} {}", red, green, blue);
        Bridge bridge = getBridge();
        if (bridge != null) {
            Effects effects = new Effects();
            Write write = new Write();
            write.setCommand("display");
            write.setAnimType("static");
            String panelID = this.thing.getConfiguration().get(CONFIG_PANEL_ID).toString();
            @Nullable
            BridgeHandler handler = bridge.getHandler();
            if (handler != null) {
                NanoleafControllerConfig config = ((NanoleafControllerHandler) handler).getControllerConfig();
                // Light Panels and Canvas use different stream commands
                if (config.deviceType.equals(CONFIG_DEVICE_TYPE_LIGHTPANELS)
                        || config.deviceType.equals(CONFIG_DEVICE_TYPE_CANVAS)) {
                    logger.trace("Anim Data rgb {} {} {} {}", panelID, red, green, blue);
                    write.setAnimData(String.format("1 %s 1 %d %d %d 0 10", panelID, red, green, blue));
                } else {
                    // this is only used in special streaming situations with canvas which is not yet supported
                    int quotient = Integer.divideUnsigned(Integer.valueOf(panelID), 256);
                    int remainder = Integer.remainderUnsigned(Integer.valueOf(panelID), 256);
                    write.setAnimData(
                            String.format("0 1 %d %d %d %d %d 0 0 10", quotient, remainder, red, green, blue));
                }
                write.setLoop(false);
                effects.setWrite(write);
                Request setNewRenderedEffectRequest = OpenAPIUtils.requestBuilder(httpClient, config, API_EFFECT,
                        HttpMethod.PUT);
                String content = gson.toJson(effects);
                logger.debug("sending effect command from panel {}: {}", getThing().getUID(), content);
                setNewRenderedEffectRequest.content(new StringContentProvider(content), "application/json");
                OpenAPIUtils.sendOpenAPIRequest(setNewRenderedEffectRequest);
            } else {
                logger.warn("Couldn't set rendering effect as Bridge-Handler {} is null", bridge.getUID());
            }
        }
    }

    public void updatePanelColorChannel() {
        @Nullable
        HSBType panelColor = getPanelColor();
        logger.trace("updatePanelColorChannel: panelColor: {}", panelColor);
        if (panelColor != null) {
            updateState(CHANNEL_PANEL_COLOR, panelColor);
        }
    }

    /**
     * Apply the gesture to the panel
     *
     * @param gesture Only 0=single tap and 1=double tap are supported
     */
    public void updatePanelGesture(int gesture) {
        switch (gesture) {
            case 0:
                triggerChannel(CHANNEL_PANEL_TAP, CommonTriggerEvents.SHORT_PRESSED);
                break;
            case 1:
                triggerChannel(CHANNEL_PANEL_TAP, CommonTriggerEvents.DOUBLE_PRESSED);
                break;
        }
    }

    public String getPanelID() {
        String panelID = getThing().getConfiguration().get(CONFIG_PANEL_ID).toString();
        return panelID;
    }

    private @Nullable HSBType getPanelColor() {
        String panelID = getPanelID();

        // get panel color data from controller
        try {
            Effects effects = new Effects();
            Write write = new Write();
            write.setCommand("request");
            write.setAnimName("*Static*");
            effects.setWrite(write);
            Bridge bridge = getBridge();
            if (bridge != null) {
                NanoleafControllerHandler handler = (NanoleafControllerHandler) bridge.getHandler();
                if (handler != null) {
                    NanoleafControllerConfig config = handler.getControllerConfig();
                    logger.debug("Sending Request from Panel for getColor()");
                    Request setPanelUpdateRequest = OpenAPIUtils.requestBuilder(httpClient, config, API_EFFECT,
                            HttpMethod.PUT);
                    setPanelUpdateRequest.content(new StringContentProvider(gson.toJson(effects)), "application/json");
                    ContentResponse panelData = OpenAPIUtils.sendOpenAPIRequest(setPanelUpdateRequest);
                    // parse panel data

                    parsePanelData(panelID, config, panelData);
                }
            }
        } catch (NanoleafNotFoundException nfe) {
            logger.debug("Panel data could not be retrieved as no data was returned (static type missing?) : {}",
                    nfe.getMessage());
        } catch (NanoleafBadRequestException nfe) {
            logger.debug(
                    "Panel data could not be retrieved as request not expected(static type missing / dynamic type on) : {}",
                    nfe.getMessage());
        } catch (NanoleafException nue) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/error.nanoleaf.panel.communication");
            logger.debug("Panel data could not be retrieved: {}", nue.getMessage());
        }

        return panelInfo.get(panelID);
    }

    void parsePanelData(String panelID, NanoleafControllerConfig config, ContentResponse panelData) {
        // panelData is in format (numPanels, (PanelId, 1, R, G, B, W, TransitionTime) * numPanel)
        @Nullable
        Write response = gson.fromJson(panelData.getContentAsString(), Write.class);
        if (response != null) {
            String[] tokenizedData = response.getAnimData().split(" ");
            if (config.deviceType.equals(CONFIG_DEVICE_TYPE_LIGHTPANELS)
                    || config.deviceType.equals(CONFIG_DEVICE_TYPE_CANVAS)) {
                // panelData is in format (numPanels (PanelId 1 R G B W TransitionTime) * numPanel)
                String[] panelDataPoints = Arrays.copyOfRange(tokenizedData, 1, tokenizedData.length);
                for (int i = 0; i < panelDataPoints.length; i++) {
                    if (i % 7 == 0) {
                        String id = panelDataPoints[i];
                        if (id.equals(panelID)) {
                            // found panel data - store it
                            panelInfo.put(panelID,
                                    HSBType.fromRGB(Integer.parseInt(panelDataPoints[i + 2]),
                                            Integer.parseInt(panelDataPoints[i + 3]),
                                            Integer.parseInt(panelDataPoints[i + 4])));
                        }
                    }
                }
            } else {
                // panelData is in format (0 numPanels (quotient(panelID) remainder(panelID) R G B W 0
                // quotient(TransitionTime) remainder(TransitionTime)) * numPanel)
                String[] panelDataPoints = Arrays.copyOfRange(tokenizedData, 2, tokenizedData.length);
                for (int i = 0; i < panelDataPoints.length; i++) {
                    if (i % 8 == 0) {
                        String idQuotient = panelDataPoints[i];
                        String idRemainder = panelDataPoints[i + 1];
                        Integer idNum = Integer.valueOf(idQuotient) * 256 + Integer.valueOf(idRemainder);
                        if (String.valueOf(idNum).equals(panelID)) {
                            // found panel data - store it
                            panelInfo.put(panelID,
                                    HSBType.fromRGB(Integer.parseInt(panelDataPoints[i + 3]),
                                            Integer.parseInt(panelDataPoints[i + 4]),
                                            Integer.parseInt(panelDataPoints[i + 5])));
                        }
                    }
                }
            }
        }
    }
}
