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
package org.openhab.binding.androidtv.internal.protocol.philipstv.internal.service;

import static org.openhab.binding.androidtv.internal.protocol.philipstv.internal.ConnectionManager.OBJECT_MAPPER;
import static org.openhab.binding.androidtv.internal.protocol.philipstv.internal.PhilipsTvBindingConstants.AMBILIGHT_CACHED_PATH;
import static org.openhab.binding.androidtv.internal.protocol.philipstv.internal.PhilipsTvBindingConstants.AMBILIGHT_CONFIG_PATH;
import static org.openhab.binding.androidtv.internal.protocol.philipstv.internal.PhilipsTvBindingConstants.AMBILIGHT_LOUNGE_PATH;
import static org.openhab.binding.androidtv.internal.protocol.philipstv.internal.PhilipsTvBindingConstants.AMBILIGHT_MODE_PATH;
import static org.openhab.binding.androidtv.internal.protocol.philipstv.internal.PhilipsTvBindingConstants.AMBILIGHT_POWERSTATE_PATH;
import static org.openhab.binding.androidtv.internal.protocol.philipstv.internal.PhilipsTvBindingConstants.AMBILIGHT_TOPOLOGY_PATH;
import static org.openhab.binding.androidtv.internal.protocol.philipstv.internal.PhilipsTvBindingConstants.CHANNEL_AMBILIGHT_BOTTOM_COLOR;
import static org.openhab.binding.androidtv.internal.protocol.philipstv.internal.PhilipsTvBindingConstants.CHANNEL_AMBILIGHT_COLOR;
import static org.openhab.binding.androidtv.internal.protocol.philipstv.internal.PhilipsTvBindingConstants.CHANNEL_AMBILIGHT_HUE_POWER;
import static org.openhab.binding.androidtv.internal.protocol.philipstv.internal.PhilipsTvBindingConstants.CHANNEL_AMBILIGHT_LEFT_COLOR;
import static org.openhab.binding.androidtv.internal.protocol.philipstv.internal.PhilipsTvBindingConstants.CHANNEL_AMBILIGHT_LOUNGE_POWER;
import static org.openhab.binding.androidtv.internal.protocol.philipstv.internal.PhilipsTvBindingConstants.CHANNEL_AMBILIGHT_POWER;
import static org.openhab.binding.androidtv.internal.protocol.philipstv.internal.PhilipsTvBindingConstants.CHANNEL_AMBILIGHT_RIGHT_COLOR;
import static org.openhab.binding.androidtv.internal.protocol.philipstv.internal.PhilipsTvBindingConstants.CHANNEL_AMBILIGHT_STYLE;
import static org.openhab.binding.androidtv.internal.protocol.philipstv.internal.PhilipsTvBindingConstants.CHANNEL_AMBILIGHT_TOP_COLOR;
import static org.openhab.binding.androidtv.internal.protocol.philipstv.internal.PhilipsTvBindingConstants.POWER_OFF;
import static org.openhab.binding.androidtv.internal.protocol.philipstv.internal.PhilipsTvBindingConstants.TV_NOT_LISTENING_MSG;
import static org.openhab.binding.androidtv.internal.protocol.philipstv.internal.PhilipsTvBindingConstants.TV_OFFLINE_MSG;
import static org.openhab.binding.androidtv.internal.protocol.philipstv.internal.PhilipsTvBindingConstants.UPDATE_SETTINGS_PATH;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.openhab.binding.androidtv.internal.protocol.philipstv.internal.ConnectionManager;
import org.openhab.binding.androidtv.internal.protocol.philipstv.internal.PhilipsTVConnectionManager;
import org.openhab.binding.androidtv.internal.protocol.philipstv.internal.WakeOnLanUtil;
import org.openhab.binding.androidtv.internal.protocol.philipstv.internal.config.PhilipsTvConfiguration;
import org.openhab.binding.androidtv.internal.protocol.philipstv.internal.service.api.PhilipsTvService;
import org.openhab.binding.androidtv.internal.protocol.philipstv.internal.service.model.DataDto;
import org.openhab.binding.androidtv.internal.protocol.philipstv.internal.service.model.TvSettingsUpdateDto;
import org.openhab.binding.androidtv.internal.protocol.philipstv.internal.service.model.ValueDto;
import org.openhab.binding.androidtv.internal.protocol.philipstv.internal.service.model.ValuesDto;
import org.openhab.binding.androidtv.internal.protocol.philipstv.internal.service.model.ambilight.AmbilightColorDeltaDto;
import org.openhab.binding.androidtv.internal.protocol.philipstv.internal.service.model.ambilight.AmbilightColorDto;
import org.openhab.binding.androidtv.internal.protocol.philipstv.internal.service.model.ambilight.AmbilightColorSettingsDto;
import org.openhab.binding.androidtv.internal.protocol.philipstv.internal.service.model.ambilight.AmbilightConfigDto;
import org.openhab.binding.androidtv.internal.protocol.philipstv.internal.service.model.ambilight.AmbilightLoungeDto;
import org.openhab.binding.androidtv.internal.protocol.philipstv.internal.service.model.ambilight.AmbilightModeDto;
import org.openhab.binding.androidtv.internal.protocol.philipstv.internal.service.model.ambilight.AmbilightPowerDto;
import org.openhab.binding.androidtv.internal.protocol.philipstv.internal.service.model.ambilight.AmbilightTopologyDto;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Service for handling commands regarding Ambilight settings of the TV
 *
 * @author Benjamin Meyer - Initial contribution
 */
public class AmbilightService implements PhilipsTvService {

    private static final List<String> AMBILIGHT_COLOR_CHANNELS = Stream
            .of(CHANNEL_AMBILIGHT_COLOR, CHANNEL_AMBILIGHT_LEFT_COLOR, CHANNEL_AMBILIGHT_RIGHT_COLOR,
                    CHANNEL_AMBILIGHT_TOP_COLOR, CHANNEL_AMBILIGHT_BOTTOM_COLOR)
            .collect(Collectors.toList());
    private static final int AMBILIGHT_HUE_NODE_ID = 2131230774;
    private static final int AMBILIGHT_BRIGHTNESS_NODE_ID = 2131230769;
    private static final String AMBILIGHT_MODE_MANUAL = "manual";
    private static final String AMBILIGHT_STYLE_FOLLOW_VIDEO = "FOLLOW_VIDEO";
    private static final String AMBILIGHT_STYLE_FOLLOW_COLOR = "FOLLOW_COLOR";
    private static final String AMBILIGHT_ALGORITHM_MANUAL_HUE = "MANUAL HUE";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final PhilipsTVConnectionManager handler;

    private final Predicate<PhilipsTvConfiguration> isWakeOnLanEnabled = config -> config.macAddress != null
            && !config.macAddress.isEmpty();

    private AmbilightTopologyDto ambilightTopology;

    private final ConnectionManager connectionManager;

    public AmbilightService(PhilipsTVConnectionManager handler, ConnectionManager connectionManager) {
        this.handler = handler;
        this.connectionManager = connectionManager;
    }

    @Override
    public void handleCommand(String channel, Command command) {
        try {
            if (CHANNEL_AMBILIGHT_POWER.equals(channel) && (command instanceof OnOffType)) {
                setAmbilightPowerState(command);
            } else if (CHANNEL_AMBILIGHT_POWER.equals(channel) && (command instanceof RefreshType)) {
                AmbilightPowerDto ambilightPowerDto = getAmbilightPowerState();
                handler.postUpdateChannel(CHANNEL_AMBILIGHT_POWER,
                        ambilightPowerDto.isPoweredOn() ? OnOffType.ON : OnOffType.OFF);
            } else if (CHANNEL_AMBILIGHT_HUE_POWER.equals(channel) && (command instanceof OnOffType)) {
                setAmbilightHuePowerState(command);
            } else if (CHANNEL_AMBILIGHT_LOUNGE_POWER.equals(channel) && (command instanceof OnOffType)) {
                setAmbilightLoungePowerState(command);
            } else if (CHANNEL_AMBILIGHT_STYLE.equals(channel) && (command instanceof StringType)) {
                setAmbilightStyle(command.toString());
            } else if (CHANNEL_AMBILIGHT_STYLE.equals(channel) && (command instanceof RefreshType)) {
                AmbilightConfigDto config = getAmbilightConfig();
                String styleWithAlgorithm = String.format("%s %s", config.getStyleName(), config.getMenuSetting());
                handler.postUpdateChannel(CHANNEL_AMBILIGHT_STYLE, new StringType(styleWithAlgorithm));
            } else if (CHANNEL_AMBILIGHT_COLOR.equals(channel) && (command instanceof HSBType)) {
                setAllAmbilightColors((HSBType) command);
            } else if ((CHANNEL_AMBILIGHT_LEFT_COLOR.equals(channel) || CHANNEL_AMBILIGHT_RIGHT_COLOR.equals(channel)
                    || CHANNEL_AMBILIGHT_TOP_COLOR.equals(channel) || CHANNEL_AMBILIGHT_BOTTOM_COLOR.equals(channel))
                    && (command instanceof HSBType)) {
                setAmbilightPixel((HSBType) command, channel);
            } else if (AMBILIGHT_COLOR_CHANNELS.contains(channel) && (command instanceof PercentType)) {
                setAmbilightBrightness(((PercentType) command).intValue());
            } else {
                if (!(command instanceof RefreshType)) {
                    logger.warn("Unknown command: {} for Channel {}", command, channel);
                }
            }
        } catch (Exception e) {
            if (isTvOfflineException(e)) {
                handler.postUpdateThing(ThingStatus.OFFLINE, ThingStatusDetail.NONE, TV_OFFLINE_MSG);
            } else if (isTvNotListeningException(e)) {
                handler.postUpdateThing(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        TV_NOT_LISTENING_MSG);
            } else {
                logger.warn("Error during handling the Ambilight command {} for Channel {}: {}", command, channel,
                        e.getMessage(), e);
            }
        }
    }

    private AmbilightPowerDto getAmbilightPowerState() throws IOException {
        return OBJECT_MAPPER.readValue(connectionManager.doHttpsGet(AMBILIGHT_POWERSTATE_PATH),
                AmbilightPowerDto.class);
    }

    private void setAmbilightPowerState(Command command) throws IOException {
        if (command.equals(OnOffType.OFF)) {
            AmbilightPowerDto ambilightPower = new AmbilightPowerDto();
            ambilightPower.setPower(POWER_OFF);
            String powerStateJson = OBJECT_MAPPER.writeValueAsString(ambilightPower);
            logger.debug("Post Ambilight power state json: {}", powerStateJson);
            connectionManager.doHttpsPost(AMBILIGHT_POWERSTATE_PATH, powerStateJson);
        } else { // power on via setting FOLLOW_VIDEO instead through POWERSTATE_PATH which sets FOLLOW_COLOR
            setAmbilightStyle(String.format("%s %s", AMBILIGHT_STYLE_FOLLOW_VIDEO, "STANDARD"));
        }
    }

    private void setAmbilightHuePowerState(Command command) throws IOException {
        TvSettingsUpdateDto ambilightHuePower = new TvSettingsUpdateDto();
        ValuesDto values = new ValuesDto();

        ValueDto value = new ValueDto();
        value.setNodeid(AMBILIGHT_HUE_NODE_ID);
        value.setAvailable("true");
        value.setControllable("true");

        DataDto data = new DataDto();
        data.setValue(command.equals(OnOffType.ON) ? "true" : "false");

        value.setData(data);
        values.setValue(value);
        ambilightHuePower.setValues(Collections.singletonList(values));

        String ambilightHuePowerJson = OBJECT_MAPPER.writeValueAsString(ambilightHuePower);
        logger.debug("Post Ambilight hue power state json: {}", ambilightHuePowerJson);
        connectionManager.doHttpsPost(UPDATE_SETTINGS_PATH, ambilightHuePowerJson);
    }

    private void setAmbilightLoungePowerState(Command command) throws IOException, InterruptedException {
        AmbilightLoungeDto ambilightLoungeDto = new AmbilightLoungeDto();

        AmbilightColorDto ambilightColorDto = new AmbilightColorDto();
        if (command.equals(OnOffType.ON)) {
            if (isWakeOnLanEnabled.test(handler.config) && !WakeOnLanUtil.isReachable(handler.config.host)) {
                WakeOnLanUtil.wakeOnLan(handler.config.host, handler.config.macAddress);
            }
            ambilightColorDto.setHue(0);
        } else {
            ambilightColorDto.setHue(255);
        }
        ambilightLoungeDto.setColor(ambilightColorDto);

        String setAmbilightLoungeJson = OBJECT_MAPPER.writeValueAsString(ambilightLoungeDto);
        logger.debug("Setting ambilight lounge power state json: {}", setAmbilightLoungeJson);
        connectionManager.doHttpsPost(AMBILIGHT_LOUNGE_PATH, setAmbilightLoungeJson);
    }

    private void setAmbilightStyle(String styleToSet) throws IOException {
        String[] styleWithAlgorithm = styleToSet.split(" ");
        if (styleWithAlgorithm.length != 2) {
            throw new IllegalStateException("Style and/or algorithm is missing.");
        }
        String style = styleWithAlgorithm[0];
        String algorithm = styleWithAlgorithm[1];
        AmbilightConfigDto ambilightConfig = new AmbilightConfigDto();
        ambilightConfig.setStyleName(style);
        ambilightConfig.setMenuSetting(algorithm);
        if (style.equals(AMBILIGHT_STYLE_FOLLOW_COLOR) && algorithm.equals(AMBILIGHT_ALGORITHM_MANUAL_HUE)) {
            ambilightConfig.setAlgorithm(algorithm);
            ambilightConfig.setIsExpert(true);
            AmbilightColorSettingsDto ambilightColorSettingsDto = new AmbilightColorSettingsDto();
            AmbilightColorDeltaDto ambilightColorDeltaDto = new AmbilightColorDeltaDto();
            ambilightColorDeltaDto.setHue(0);
            ambilightColorDeltaDto.setBrightness(0);
            ambilightColorDeltaDto.setSaturation(0);
            ambilightColorSettingsDto.setColorDelta(ambilightColorDeltaDto);
            ambilightColorSettingsDto.setSpeed(255);
        }
        String ambilightConfigJson = OBJECT_MAPPER.writeValueAsString(ambilightConfig);
        logger.debug("Post config for Ambilight style json: {}", ambilightConfigJson);
        connectionManager.doHttpsPost(AMBILIGHT_CONFIG_PATH, ambilightConfigJson);
    }

    private AmbilightConfigDto getAmbilightConfig() throws IOException {
        return OBJECT_MAPPER.readValue(connectionManager.doHttpsGet(AMBILIGHT_CONFIG_PATH), AmbilightConfigDto.class);
    }

    private void setAmbilightMode(String mode) throws IOException {
        AmbilightModeDto ambilightMode = new AmbilightModeDto();
        ambilightMode.setCurrent(mode);
        String ambilightModeJson = OBJECT_MAPPER.writeValueAsString(ambilightMode);
        logger.debug("Post ambilight mode json: {}", ambilightModeJson);
        connectionManager.doHttpsPost(AMBILIGHT_MODE_PATH, ambilightModeJson);
    }

    // private AmbilightModeDto getAmbilightMode() throws IOException {
    // return OBJECT_MAPPER.readValue(connectionManager.doHttpsGet(AMBILIGHT_MODE_PATH), AmbilightModeDto.class);
    // }

    private void setAmbilightBrightness(int brightnessToSet) throws IOException {
        String ambilightBrightnessJson = ServiceUtil.createTvSettingsUpdateJson(AMBILIGHT_BRIGHTNESS_NODE_ID,
                brightnessToSet / 10);
        logger.debug("Post Ambilight brightness json: {}", ambilightBrightnessJson);
        connectionManager.doHttpsPost(UPDATE_SETTINGS_PATH, ambilightBrightnessJson);
    }

    private void setAmbilightPixel(HSBType hsb, String channel) throws IOException {
        if (ambilightTopology == null) {
            ambilightTopology = getAmbilightTopology();
        }
        setAmbilightMode(AMBILIGHT_MODE_MANUAL); // activates the usage of cached values
        String sideToSet = determineAmbilightSide(channel);
        int pixelSize = ambilightTopology.getPixelSizeForGivenSide(sideToSet);

        ObjectNode rootNode = OBJECT_MAPPER.createObjectNode();

        ObjectNode pixel = OBJECT_MAPPER.createObjectNode();
        pixel.put("r", hsb.getRed().intValue());
        pixel.put("g", hsb.getGreen().intValue());
        pixel.put("b", hsb.getBlue().intValue());

        ObjectNode sidePixels = OBJECT_MAPPER.createObjectNode();
        // pixel declaration in json start with 0
        IntStream.range(0, pixelSize).forEach(i -> sidePixels.set(Integer.toString(i), pixel));

        IntStream.rangeClosed(1, ambilightTopology.getLayers()).forEach(i -> {
            ObjectNode layerX = OBJECT_MAPPER.createObjectNode();
            layerX.set(sideToSet, sidePixels);

            rootNode.set("layer" + i, layerX);
        });

        String ambilightPixelJson = OBJECT_MAPPER.writeValueAsString(rootNode);
        logger.debug("Sending {} Ambilight pixel json: {}", sideToSet, ambilightPixelJson);
        connectionManager.doHttpsPost(AMBILIGHT_CACHED_PATH, ambilightPixelJson);
    }

    private AmbilightTopologyDto getAmbilightTopology() throws IOException {
        return OBJECT_MAPPER.readValue(connectionManager.doHttpsGet(AMBILIGHT_TOPOLOGY_PATH),
                AmbilightTopologyDto.class);
    }

    private String determineAmbilightSide(String channel) {
        String sideToSet;
        switch (channel) {
            case CHANNEL_AMBILIGHT_LEFT_COLOR:
                sideToSet = "left";
                break;
            case CHANNEL_AMBILIGHT_RIGHT_COLOR:
                sideToSet = "right";
                break;
            case CHANNEL_AMBILIGHT_TOP_COLOR:
                sideToSet = "top";
                break;
            case CHANNEL_AMBILIGHT_BOTTOM_COLOR:
                sideToSet = "bottom";
                break;
            default:
                throw new IllegalStateException("Unexpected channel for ambilight pixel set: " + channel);
        }
        return sideToSet;
    }

    private void setAllAmbilightColors(HSBType hsb) throws IOException {
        AmbilightConfigDto ambilightConfig = new AmbilightConfigDto();
        ambilightConfig.setIsExpert(true);
        ambilightConfig.setStyleName("FOLLOW_COLOR");
        ambilightConfig.setAlgorithm("MANUAL_HUE");

        AmbilightColorSettingsDto ambilightColorSettings = new AmbilightColorSettingsDto();

        AmbilightColorDto ambilightColor = new AmbilightColorDto(hsb);
        AmbilightColorDeltaDto ambilightColorDelta = new AmbilightColorDeltaDto();
        ambilightColorDelta.setHue(0);
        ambilightColorDelta.setSaturation(0);
        ambilightColorDelta.setBrightness(0);

        ambilightColorSettings.setSpeed(255);
        ambilightColorSettings.setColor(ambilightColor);
        ambilightColorSettings.setColorDelta(ambilightColorDelta);

        ambilightConfig.setColorSettings(ambilightColorSettings);

        String setAmbilightColorsJson = OBJECT_MAPPER.writeValueAsString(ambilightConfig);
        logger.debug("Setting ambilight colors json: {}", setAmbilightColorsJson);
        connectionManager.doHttpsPost(AMBILIGHT_CONFIG_PATH, setAmbilightColorsJson);
    }
}
