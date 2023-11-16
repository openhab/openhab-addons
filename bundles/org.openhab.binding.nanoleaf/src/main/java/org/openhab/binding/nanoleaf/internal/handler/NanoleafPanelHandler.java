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
package org.openhab.binding.nanoleaf.internal.handler;

import static org.openhab.binding.nanoleaf.internal.NanoleafBindingConstants.*;

import java.util.concurrent.ScheduledFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.nanoleaf.internal.NanoleafException;
import org.openhab.binding.nanoleaf.internal.NanoleafUnauthorizedException;
import org.openhab.binding.nanoleaf.internal.OpenAPIUtils;
import org.openhab.binding.nanoleaf.internal.colors.NanoleafPanelColorChangeListener;
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
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.util.ColorUtil;
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
public class NanoleafPanelHandler extends BaseThingHandler implements NanoleafPanelColorChangeListener {

    private static final PercentType MIN_PANEL_BRIGHTNESS = PercentType.ZERO;
    private static final PercentType MAX_PANEL_BRIGHTNESS = PercentType.HUNDRED;

    private final Logger logger = LoggerFactory.getLogger(NanoleafPanelHandler.class);

    private final HttpClient httpClient;
    // JSON parser for API responses
    private final Gson gson = new Gson();
    private HSBType currentPanelColor = HSBType.BLACK;

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
        Bridge bridge = getBridge();
        if (bridge != null) {
            ThingHandler handler = bridge.getHandler();
            if (handler instanceof NanoleafControllerHandler controllerHandler) {
                controllerHandler.getColorInformation().unregisterChangeListener(getPanelID());
            }
        }

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
        updateState(CHANNEL_PANEL_COLOR, currentPanelColor);
        logger.debug("Panel {} status changed to {}-{}", this.getThing().getUID(), panelStatus.getStatus(),
                panelStatus.getStatusDetail());

        Bridge bridge = getBridge();
        if (bridge != null) {
            ThingHandler handler = bridge.getHandler();
            if (handler instanceof NanoleafControllerHandler controllerHandler) {
                controllerHandler.getColorInformation().registerChangeListener(getPanelID(), this);
            }
        }
    }

    private void sendRenderedEffectCommand(Command command) throws NanoleafException {
        logger.debug("Command Type: {}", command.getClass());
        logger.debug("currentPanelColor: {}", currentPanelColor);

        HSBType newPanelColor = new HSBType();
        if (command instanceof HSBType hsbCommand) {
            newPanelColor = hsbCommand;
        } else if (command instanceof OnOffType) {
            if (OnOffType.ON.equals(command)) {
                newPanelColor = new HSBType(currentPanelColor.getHue(), currentPanelColor.getSaturation(),
                        MAX_PANEL_BRIGHTNESS);
            } else {
                newPanelColor = new HSBType(currentPanelColor.getHue(), currentPanelColor.getSaturation(),
                        MIN_PANEL_BRIGHTNESS);
            }
        } else if (command instanceof PercentType type) {
            PercentType brightness = new PercentType(Math.max(MIN_PANEL_BRIGHTNESS.intValue(), type.intValue()));
            newPanelColor = new HSBType(currentPanelColor.getHue(), currentPanelColor.getSaturation(), brightness);
        } else if (command instanceof IncreaseDecreaseType) {
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
        logger.trace("Setting new color {} to panel {}", newPanelColor, getPanelID());
        setPanelColor(newPanelColor);
        // transform to RGB
        int[] rgb = ColorUtil.hsbToRgb(newPanelColor);
        logger.trace("Setting new rgb {} {} {}", rgb[0], rgb[1], rgb[2]);
        Bridge bridge = getBridge();
        if (bridge != null) {
            Effects effects = new Effects();
            Write write = new Write();
            write.setCommand("display");
            write.setAnimType("static");
            Integer panelID = Integer.valueOf(this.thing.getConfiguration().get(CONFIG_PANEL_ID).toString());
            @Nullable
            BridgeHandler handler = bridge.getHandler();
            if (handler != null) {
                NanoleafControllerConfig config = ((NanoleafControllerHandler) handler).getControllerConfig();
                // Light Panels and Canvas use different stream commands
                if (config.deviceType.equals(CONFIG_DEVICE_TYPE_LIGHTPANELS)
                        || config.deviceType.equals(CONFIG_DEVICE_TYPE_CANVAS)) {
                    logger.trace("Anim Data rgb {} {} {} {}", panelID, rgb[0], rgb[1], rgb[2]);
                    write.setAnimData(String.format("1 %s 1 %d %d %d 0 10", panelID, rgb[0], rgb[1], rgb[2]));
                } else {
                    // this is only used in special streaming situations with canvas which is not yet supported
                    int quotient = Integer.divideUnsigned(panelID, 256);
                    int remainder = Integer.remainderUnsigned(panelID, 256);
                    write.setAnimData(
                            String.format("0 1 %d %d %d %d %d 0 0 10", quotient, remainder, rgb[0], rgb[1], rgb[2]));
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

    /**
     * Apply the gesture to the panel
     *
     * @param gesture Only 0=single tap, 1=double tap and 6=long press are supported
     */
    public void updatePanelGesture(int gesture) {
        switch (gesture) {
            case 0:
                triggerChannel(CHANNEL_PANEL_TAP, CommonTriggerEvents.SHORT_PRESSED);
                break;
            case 1:
                triggerChannel(CHANNEL_PANEL_TAP, CommonTriggerEvents.DOUBLE_PRESSED);
                break;
            case 6:
                triggerChannel(CHANNEL_PANEL_TAP, CommonTriggerEvents.LONG_PRESSED);
                break;
        }
    }

    public Integer getPanelID() {
        Object panelId = getThing().getConfiguration().get(CONFIG_PANEL_ID);
        if (panelId instanceof Integer) {
            return (Integer) panelId;
        } else if (panelId instanceof Number numberValue) {
            return numberValue.intValue();
        } else {
            // Fall back to parsing string representation of panel if it is not returning an integer
            String stringPanelId = panelId.toString();
            Integer parsedPanelId = Integer.getInteger(stringPanelId);
            if (parsedPanelId == null) {
                return 0;
            } else {
                return parsedPanelId;
            }
        }
    }

    private void setPanelColor(HSBType color) {
        Integer panelId = getPanelID();
        Bridge bridge = getBridge();
        if (bridge != null) {
            ThingHandler handler = bridge.getHandler();
            if (handler instanceof NanoleafControllerHandler controllerHandler) {
                controllerHandler.getColorInformation().setPanelColor(panelId, color);
            } else {
                logger.debug("Couldn't find handler for panel {}", panelId);
            }
        } else {
            logger.debug("Couldn't find bridge for panel {}", panelId);
        }
    }

    @Override
    public void onPanelChangedColor(HSBType newColor) {
        if (logger.isTraceEnabled()) {
            logger.trace("updatePanelColorChannel: panelColor: {} for panel {}", newColor, getPanelID());
        }

        currentPanelColor = newColor;
        updateState(CHANNEL_PANEL_COLOR, newColor);
    }
}
