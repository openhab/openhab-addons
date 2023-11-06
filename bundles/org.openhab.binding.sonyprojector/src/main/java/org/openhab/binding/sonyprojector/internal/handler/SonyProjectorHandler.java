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
package org.openhab.binding.sonyprojector.internal.handler;

import static org.openhab.binding.sonyprojector.internal.SonyProjectorBindingConstants.*;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.sonyprojector.internal.SonyProjectorException;
import org.openhab.binding.sonyprojector.internal.SonyProjectorModel;
import org.openhab.binding.sonyprojector.internal.SonyProjectorStateDescriptionOptionProvider;
import org.openhab.binding.sonyprojector.internal.communication.SonyProjectorConnector;
import org.openhab.binding.sonyprojector.internal.communication.SonyProjectorStatusPower;
import org.openhab.binding.sonyprojector.internal.communication.sdcp.SonyProjectorSdcpConnector;
import org.openhab.binding.sonyprojector.internal.communication.sdcp.SonyProjectorSdcpSimuConnector;
import org.openhab.binding.sonyprojector.internal.communication.serial.SonyProjectorSerialConnector;
import org.openhab.binding.sonyprojector.internal.communication.serial.SonyProjectorSerialOverIpConnector;
import org.openhab.binding.sonyprojector.internal.communication.serial.SonyProjectorSerialSimuConnector;
import org.openhab.binding.sonyprojector.internal.configuration.SonyProjectorEthernetConfiguration;
import org.openhab.binding.sonyprojector.internal.configuration.SonyProjectorSerialConfiguration;
import org.openhab.binding.sonyprojector.internal.configuration.SonyProjectorSerialOverIpConfiguration;
import org.openhab.core.cache.ExpiringCacheMap;
import org.openhab.core.i18n.ConnectionException;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SonyProjectorHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Markus Wehrle - Initial contribution
 * @author Laurent Garnier - Refactoring, poll thread for regular channels updates, new serial thing type, new channels
 */
@NonNullByDefault
public class SonyProjectorHandler extends BaseThingHandler {

    private static final SonyProjectorModel DEFAULT_MODEL = SonyProjectorModel.VW520;
    private static final long POLLING_INTERVAL = TimeUnit.SECONDS.toSeconds(15);

    private final Logger logger = LoggerFactory.getLogger(SonyProjectorHandler.class);

    private final SonyProjectorStateDescriptionOptionProvider stateDescriptionProvider;
    private final SerialPortManager serialPortManager;
    private final TranslationProvider i18nProvider;

    private final Bundle bundle;

    private final ExpiringCacheMap<String, State> cache;

    private @Nullable ScheduledFuture<?> refreshJob;

    private boolean identifyProjector;
    private SonyProjectorModel projectorModel = DEFAULT_MODEL;
    private SonyProjectorConnector connector = new SonyProjectorSdcpSimuConnector(DEFAULT_MODEL);

    private boolean simu;

    private final Object commandLock = new Object();

    public SonyProjectorHandler(Thing thing, SonyProjectorStateDescriptionOptionProvider stateDescriptionProvider,
            SerialPortManager serialPortManager, TranslationProvider i18nProvider) {
        super(thing);
        this.stateDescriptionProvider = stateDescriptionProvider;
        this.serialPortManager = serialPortManager;
        this.i18nProvider = i18nProvider;
        this.bundle = FrameworkUtil.getBundle(this.getClass());
        this.cache = new ExpiringCacheMap<>(TimeUnit.SECONDS.toMillis(POLLING_INTERVAL));
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String channel = channelUID.getId();
        if (command instanceof RefreshType) {
            State state = cache.get(channel);
            if (state != null) {
                updateState(channel, state);
            }
            return;
        }

        synchronized (commandLock) {
            try {
                connector.open();
            } catch (ConnectionException e) {
                logger.debug("Command {} from channel {} failed: {}", command, channel,
                        e.getMessage(bundle, i18nProvider));
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getRawMessage());
                return;
            }
            try {
                switch (channel) {
                    case CHANNEL_POWER:
                        if (command == OnOffType.ON) {
                            connector.powerOn();
                        } else if (command == OnOffType.OFF) {
                            connector.powerOff();
                        } else {
                            throw new SonyProjectorException("Invalid command value");
                        }
                        break;
                    case CHANNEL_INPUT:
                        connector.setInput(command.toString());
                        break;
                    case CHANNEL_CALIBRATION_PRESET:
                        connector.setCalibrationPreset(command.toString());
                        refreshChannel(CHANNEL_CONTRAST, true);
                        refreshChannel(CHANNEL_BRIGHTNESS, true);
                        refreshChannel(CHANNEL_COLOR, true);
                        refreshChannel(CHANNEL_HUE, true);
                        refreshChannel(CHANNEL_SHARPNESS, true);
                        refreshChannel(CHANNEL_COLOR_TEMP, true);
                        refreshChannel(CHANNEL_IRIS_MODE, true);
                        refreshChannel(CHANNEL_IRIS_MANUAL, true);
                        refreshChannel(CHANNEL_IRIS_SENSITIVITY, true);
                        refreshChannel(CHANNEL_LAMP_CONTROL, true);
                        refreshChannel(CHANNEL_FILM_PROJECTION, true);
                        refreshChannel(CHANNEL_MOTION_ENHANCER, true);
                        refreshChannel(CHANNEL_CONTRAST_ENHANCER, true);
                        refreshChannel(CHANNEL_FILM_MODE, true);
                        refreshChannel(CHANNEL_GAMMA_CORRECTION, true);
                        refreshChannel(CHANNEL_COLOR_SPACE, true);
                        refreshChannel(CHANNEL_NR, true);
                        refreshChannel(CHANNEL_BLOCK_NR, true);
                        refreshChannel(CHANNEL_MOSQUITO_NR, true);
                        refreshChannel(CHANNEL_MPEG_NR, true);
                        refreshChannel(CHANNEL_XVCOLOR, true);
                        break;
                    case CHANNEL_CONTRAST:
                        if (command instanceof DecimalType decimalCommand) {
                            connector.setContrast(decimalCommand.intValue());
                        } else if (command instanceof PercentType percentCommand) {
                            connector.setContrast(percentCommand.intValue());
                        } else {
                            throw new SonyProjectorException("Invalid command value");
                        }
                        break;
                    case CHANNEL_BRIGHTNESS:
                        if (command instanceof DecimalType decimalCommand) {
                            connector.setBrightness(decimalCommand.intValue());
                        } else if (command instanceof PercentType percentCommand2) {
                            connector.setBrightness(percentCommand2.intValue());
                        } else {
                            throw new SonyProjectorException("Invalid command value");
                        }
                        break;
                    case CHANNEL_COLOR:
                        if (command instanceof DecimalType decimalCommand) {
                            connector.setColor(decimalCommand.intValue());
                        } else if (command instanceof PercentType percentCommand3) {
                            connector.setColor(percentCommand3.intValue());
                        } else {
                            throw new SonyProjectorException("Invalid command value");
                        }
                        break;
                    case CHANNEL_HUE:
                        if (command instanceof DecimalType decimalCommand) {
                            connector.setHue(decimalCommand.intValue());
                        } else if (command instanceof PercentType percentCommand4) {
                            connector.setHue(percentCommand4.intValue());
                        } else {
                            throw new SonyProjectorException("Invalid command value");
                        }
                        break;
                    case CHANNEL_SHARPNESS:
                        if (command instanceof DecimalType decimalCommand) {
                            connector.setSharpness(decimalCommand.intValue());
                        } else if (command instanceof PercentType percentCommand5) {
                            connector.setSharpness(percentCommand5.intValue());
                        } else {
                            throw new SonyProjectorException("Invalid command value");
                        }
                        break;
                    case CHANNEL_COLOR_TEMP:
                        connector.setColorTemperature(command.toString());
                        break;
                    case CHANNEL_IRIS_MODE:
                        connector.setIrisMode(command.toString());
                        refreshChannel(CHANNEL_IRIS_MANUAL, true);
                        break;
                    case CHANNEL_IRIS_MANUAL:
                        if (command instanceof DecimalType decimalCommand) {
                            connector.setIrisManual(decimalCommand.intValue());
                        } else if (command instanceof PercentType percentCommand6) {
                            connector.setIrisManual(percentCommand6.intValue());
                        } else {
                            throw new SonyProjectorException("Invalid command value");
                        }
                        break;
                    case CHANNEL_IRIS_SENSITIVITY:
                        connector.setIrisSensitivity(command.toString());
                        break;
                    case CHANNEL_LAMP_CONTROL:
                        connector.setLampControl(command.toString());
                        break;
                    case CHANNEL_FILM_PROJECTION:
                        connector.setFilmProjection(command.toString());
                        break;
                    case CHANNEL_MOTION_ENHANCER:
                        connector.setMotionEnhancer(command.toString());
                        break;
                    case CHANNEL_CONTRAST_ENHANCER:
                        connector.setContrastEnhancer(command.toString());
                        break;
                    case CHANNEL_FILM_MODE:
                        connector.setFilmMode(command.toString());
                        break;
                    case CHANNEL_GAMMA_CORRECTION:
                        connector.setGammaCorrection(command.toString());
                        break;
                    case CHANNEL_COLOR_SPACE:
                        connector.setColorSpace(command.toString());
                        break;
                    case CHANNEL_NR:
                        connector.setNr(command.toString());
                        break;
                    case CHANNEL_BLOCK_NR:
                        connector.setBlockNr(command.toString());
                        break;
                    case CHANNEL_MOSQUITO_NR:
                        connector.setMosquitoNr(command.toString());
                        break;
                    case CHANNEL_MPEG_NR:
                        connector.setMpegNr(command.toString());
                        break;
                    case CHANNEL_XVCOLOR:
                        if (command == OnOffType.ON) {
                            connector.enableXvColor();
                            refreshChannel(CHANNEL_GAMMA_CORRECTION, true);
                        } else if (command == OnOffType.OFF) {
                            connector.disableXvColor();
                            refreshChannel(CHANNEL_GAMMA_CORRECTION, true);
                        } else {
                            throw new SonyProjectorException("Invalid command value");
                        }
                        break;
                    case CHANNEL_PICTURE_MUTING:
                        if (command == OnOffType.ON) {
                            connector.mutePicture();
                        } else if (command == OnOffType.OFF) {
                            connector.unmutePicture();
                        } else {
                            throw new SonyProjectorException("Invalid command value");
                        }
                        break;
                    case CHANNEL_ASPECT:
                        connector.setAspect(command.toString());
                        break;
                    case CHANNEL_OVERSCAN:
                        if (command == OnOffType.ON) {
                            connector.enableOverscan();
                        } else if (command == OnOffType.OFF) {
                            connector.disableOverscan();
                        } else {
                            throw new SonyProjectorException("Invalid command value");
                        }
                        break;
                    case CHANNEL_PICTURE_POSITION:
                        connector.setPicturePosition(command.toString());
                        break;
                    default:
                        throw new SonyProjectorException("Unexpected command");
                }
                logger.debug("Command {} from channel {} succeeded", command, channel);
            } catch (SonyProjectorException e) {
                logger.debug("Command {} from channel {} failed: {}", command, channel, e.getMessage());
                refreshChannel(channel, true);
            }
            connector.close();
        }
    }

    @Override
    public void initialize() {
        logger.debug("Start initializing handler for thing {}", getThing().getUID());

        boolean configOk = false;

        if (getThing().getThingTypeUID().equals(THING_TYPE_ETHERNET)) {
            SonyProjectorEthernetConfiguration config = getConfigAs(SonyProjectorEthernetConfiguration.class);
            String configModel = config.model;
            logger.debug("Ethernet config host {}", config.host);
            logger.debug("Ethernet config port {}", config.port);
            logger.debug("Ethernet config model {}", configModel);
            logger.debug("Ethernet config community {}", config.community);
            if (config.host == null || config.host.isEmpty()) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "@text/offline.config-error-unknown-host");
            } else if (configModel == null || configModel.isEmpty()) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "@text/offline.config-error-unknown-model");
            } else {
                configOk = true;

                connector = simu ? new SonyProjectorSdcpSimuConnector(DEFAULT_MODEL)
                        : new SonyProjectorSdcpConnector(config.host, config.port, config.community, DEFAULT_MODEL);
                identifyProjector = "AUTO".equals(configModel);
                projectorModel = switchToModel("AUTO".equals(configModel) ? null : configModel, true);

                updateStatus(ThingStatus.UNKNOWN);
            }
        } else if (getThing().getThingTypeUID().equals(THING_TYPE_SERIAL)) {
            SonyProjectorSerialConfiguration config = getConfigAs(SonyProjectorSerialConfiguration.class);
            String configModel = config.model;
            logger.debug("Serial config port {}", config.port);
            logger.debug("Serial config model {}", configModel);
            if (config.port == null || config.port.isEmpty()) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "@text/offline.config-error-unknown-port");
            } else if (config.port.toLowerCase().startsWith("rfc2217")) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "@text/offline.config-error-invalid-thing-type");
            } else if (configModel == null || configModel.isEmpty()) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "@text/offline.config-error-unknown-model");
            } else {
                configOk = true;

                connector = simu ? new SonyProjectorSerialSimuConnector(serialPortManager, DEFAULT_MODEL)
                        : new SonyProjectorSerialConnector(serialPortManager, config.port, DEFAULT_MODEL);
                identifyProjector = false;
                projectorModel = switchToModel(configModel, true);

                updateStatus(ThingStatus.UNKNOWN);
            }
        } else if (getThing().getThingTypeUID().equals(THING_TYPE_SERIAL_OVER_IP)) {
            SonyProjectorSerialOverIpConfiguration config = getConfigAs(SonyProjectorSerialOverIpConfiguration.class);
            String configModel = config.model;
            logger.debug("Serial over IP config host {}", config.host);
            logger.debug("Serial over IP config port {}", config.port);
            logger.debug("Serial over IP config model {}", configModel);
            if (config.host == null || config.host.isEmpty()) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "@text/offline.config-error-unknown-host");
            } else if (config.port == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "@text/offline.config-error-unknown-port");
            } else if (config.port <= 0) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "@text/offline.config-error-invalid-port");
            } else if (configModel == null || configModel.isEmpty()) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "@text/offline.config-error-unknown-model");
            } else {
                configOk = true;

                connector = simu ? new SonyProjectorSerialSimuConnector(serialPortManager, DEFAULT_MODEL)
                        : new SonyProjectorSerialOverIpConnector(serialPortManager, config.host, config.port,
                                DEFAULT_MODEL);
                identifyProjector = false;
                projectorModel = switchToModel(configModel, true);

                updateStatus(ThingStatus.UNKNOWN);
            }
        }

        if (!configOk) {
            connector = new SonyProjectorSdcpSimuConnector(DEFAULT_MODEL);
        } else {
            ScheduledFuture<?> refreshJob = this.refreshJob;
            if (refreshJob == null || refreshJob.isCancelled()) {
                this.refreshJob = scheduler.scheduleWithFixedDelay(() -> {
                    pollProjector();
                }, 1, POLLING_INTERVAL, TimeUnit.SECONDS);
            }
        }

        logger.debug("Finished initializing!");
    }

    @Override
    public void dispose() {
        logger.debug("Disposing handler for thing {}", getThing().getUID());
        ScheduledFuture<?> refreshJob = this.refreshJob;
        if (refreshJob != null && !refreshJob.isCancelled()) {
            refreshJob.cancel(true);
            this.refreshJob = null;
        }
        connector.close();
        super.dispose();
    }

    private void pollProjector() {
        synchronized (commandLock) {
            logger.debug("Polling the projector to refresh the channels...");

            try {
                connector.open();
            } catch (ConnectionException e) {
                logger.debug("Poll projector failed: {}", e.getMessage(bundle, i18nProvider), e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getRawMessage());
                return;
            }

            boolean isOn = refreshPowerState();
            refreshModel();
            refreshChannel(CHANNEL_INPUT, isOn);
            refreshChannel(CHANNEL_CALIBRATION_PRESET, isOn);
            refreshChannel(CHANNEL_CONTRAST, isOn);
            refreshChannel(CHANNEL_BRIGHTNESS, isOn);
            refreshChannel(CHANNEL_COLOR, isOn);
            refreshChannel(CHANNEL_HUE, isOn);
            refreshChannel(CHANNEL_SHARPNESS, isOn);
            refreshChannel(CHANNEL_COLOR_TEMP, isOn);
            refreshChannel(CHANNEL_IRIS_MODE, isOn);
            refreshChannel(CHANNEL_IRIS_MANUAL, isOn);
            refreshChannel(CHANNEL_IRIS_SENSITIVITY, isOn);
            refreshChannel(CHANNEL_LAMP_CONTROL, isOn);
            refreshChannel(CHANNEL_FILM_PROJECTION, isOn);
            refreshChannel(CHANNEL_MOTION_ENHANCER, isOn);
            refreshChannel(CHANNEL_CONTRAST_ENHANCER, isOn);
            refreshChannel(CHANNEL_FILM_MODE, isOn);
            refreshChannel(CHANNEL_GAMMA_CORRECTION, isOn);
            refreshChannel(CHANNEL_COLOR_SPACE, isOn);
            refreshChannel(CHANNEL_NR, isOn);
            refreshChannel(CHANNEL_BLOCK_NR, isOn);
            refreshChannel(CHANNEL_MOSQUITO_NR, isOn);
            refreshChannel(CHANNEL_MPEG_NR, isOn);
            refreshChannel(CHANNEL_XVCOLOR, isOn);
            refreshChannel(CHANNEL_PICTURE_MUTING, isOn);
            refreshChannel(CHANNEL_ASPECT, isOn);
            refreshChannel(CHANNEL_OVERSCAN, isOn);
            refreshChannel(CHANNEL_PICTURE_POSITION, isOn);
            refreshChannel(CHANNEL_LAMP_USE_TIME, isOn);

            connector.close();

            updateStatus(ThingStatus.ONLINE);

            logger.debug("End of the polling thread");
        }
    }

    private void refreshModel() {
        if (identifyProjector && getThing().getThingTypeUID().equals(THING_TYPE_ETHERNET)) {
            try {
                String modelName = ((SonyProjectorSdcpConnector) connector).getModelName();
                logger.debug("getModelName returned {}", modelName);
                identifyProjector = false;
                switchToModel(modelName, false);
            } catch (SonyProjectorException e) {
                logger.debug("getModelName failed: {}", e.getMessage());
            }
        }
    }

    private SonyProjectorModel switchToModel(@Nullable String modelName, boolean force) {
        SonyProjectorModel model = DEFAULT_MODEL;
        if (modelName != null && !modelName.isEmpty()) {
            try {
                model = SonyProjectorModel.getFromName(modelName, false);
                logger.debug("Model found: {}", model.getName());
            } catch (SonyProjectorException e) {
                logger.info("Model {} is unknow; consider {} by default", modelName, DEFAULT_MODEL.getName());
            }
        }
        if (force || !model.getName().equals(projectorModel.getName())) {
            connector.setModel(model);
            stateDescriptionProvider.setStateOptions(new ChannelUID(getThing().getUID(), CHANNEL_INPUT),
                    model.getInputStateOptions());
            stateDescriptionProvider.setStateOptions(new ChannelUID(getThing().getUID(), CHANNEL_CALIBRATION_PRESET),
                    model.getCalibrPresetStateOptions());
            stateDescriptionProvider.setStateOptions(new ChannelUID(getThing().getUID(), CHANNEL_COLOR_TEMP),
                    model.getColorTempStateOptions());
            stateDescriptionProvider.setStateOptions(new ChannelUID(getThing().getUID(), CHANNEL_IRIS_MODE),
                    model.getIrisModeStateOptions());
            stateDescriptionProvider.setStateOptions(new ChannelUID(getThing().getUID(), CHANNEL_IRIS_SENSITIVITY),
                    model.getIrisSensitivityStateOptions());
            stateDescriptionProvider.setStateOptions(new ChannelUID(getThing().getUID(), CHANNEL_LAMP_CONTROL),
                    model.getLampControlStateOptions());
            stateDescriptionProvider.setStateOptions(new ChannelUID(getThing().getUID(), CHANNEL_FILM_PROJECTION),
                    model.getFilmProjectionStateOptions());
            stateDescriptionProvider.setStateOptions(new ChannelUID(getThing().getUID(), CHANNEL_MOTION_ENHANCER),
                    model.getMotionEnhancerStateOptions());
            stateDescriptionProvider.setStateOptions(new ChannelUID(getThing().getUID(), CHANNEL_CONTRAST_ENHANCER),
                    model.getContrastEnhancerStateOptions());
            stateDescriptionProvider.setStateOptions(new ChannelUID(getThing().getUID(), CHANNEL_FILM_MODE),
                    model.getFilmModeStateOptions());
            stateDescriptionProvider.setStateOptions(new ChannelUID(getThing().getUID(), CHANNEL_GAMMA_CORRECTION),
                    model.getGammaCorrectionStateOptions());
            stateDescriptionProvider.setStateOptions(new ChannelUID(getThing().getUID(), CHANNEL_COLOR_SPACE),
                    model.getColorSpaceStateOptions());
            stateDescriptionProvider.setStateOptions(new ChannelUID(getThing().getUID(), CHANNEL_NR),
                    model.getNrStateOptions());
            stateDescriptionProvider.setStateOptions(new ChannelUID(getThing().getUID(), CHANNEL_BLOCK_NR),
                    model.getBlockNrStateOptions());
            stateDescriptionProvider.setStateOptions(new ChannelUID(getThing().getUID(), CHANNEL_MOSQUITO_NR),
                    model.getMosquitoNrStateOptions());
            stateDescriptionProvider.setStateOptions(new ChannelUID(getThing().getUID(), CHANNEL_MPEG_NR),
                    model.getMpegNrStateOptions());
            stateDescriptionProvider.setStateOptions(new ChannelUID(getThing().getUID(), CHANNEL_ASPECT),
                    model.getAspectStateOptions());
            stateDescriptionProvider.setStateOptions(new ChannelUID(getThing().getUID(), CHANNEL_PICTURE_POSITION),
                    model.getPicturePositionStateOptions());
        }
        return model;
    }

    private boolean refreshPowerState() {
        boolean on = false;
        State state = UnDefType.UNDEF;
        try {
            SonyProjectorStatusPower value = connector.getStatusPower();
            logger.debug("Get Status Power returned {}", value);
            on = value.isOn();
            state = new StringType(value.name());
        } catch (SonyProjectorException e) {
            logger.debug("Get Status Power failed: {}", e.getMessage());
        }
        updateChannelStateAndCache(CHANNEL_POWER, on ? OnOffType.ON : OnOffType.OFF);
        updateChannelStateAndCache(CHANNEL_POWERSTATE, state);
        return on;
    }

    private @Nullable State requestProjectorValue(String channel, boolean requestValue) {
        State state = null;
        boolean precond;
        switch (channel) {
            case CHANNEL_POWER:
            case CHANNEL_POWERSTATE:
            case CHANNEL_INPUT:
            case CHANNEL_CALIBRATION_PRESET:
            case CHANNEL_CONTRAST:
            case CHANNEL_BRIGHTNESS:
            case CHANNEL_COLOR:
            case CHANNEL_HUE:
            case CHANNEL_SHARPNESS:
            case CHANNEL_COLOR_TEMP:
            case CHANNEL_CONTRAST_ENHANCER:
            case CHANNEL_GAMMA_CORRECTION:
            case CHANNEL_COLOR_SPACE:
            case CHANNEL_NR:
            case CHANNEL_PICTURE_MUTING:
            case CHANNEL_ASPECT:
                precond = true;
                break;
            case CHANNEL_IRIS_MODE:
                precond = projectorModel.isIrisModeAvailable();
                break;
            case CHANNEL_IRIS_MANUAL:
                precond = projectorModel.isIrisManualAvailable();
                break;
            case CHANNEL_IRIS_SENSITIVITY:
                precond = projectorModel.isIrisSensitivityAvailable();
                break;
            case CHANNEL_LAMP_CONTROL:
                precond = projectorModel.isLampControlAvailable();
                break;
            case CHANNEL_FILM_PROJECTION:
                precond = projectorModel.isFilmProjectionAvailable();
                break;
            case CHANNEL_MOTION_ENHANCER:
                precond = projectorModel.isMotionEnhancerAvailable();
                break;
            case CHANNEL_FILM_MODE:
                precond = projectorModel.isFilmModeAvailable();
                break;
            case CHANNEL_BLOCK_NR:
                precond = projectorModel.isBlockNrAvailable();
                break;
            case CHANNEL_MOSQUITO_NR:
                precond = projectorModel.isMosquitoNrAvailable();
                break;
            case CHANNEL_MPEG_NR:
                precond = projectorModel.isMpegNrAvailable();
                break;
            case CHANNEL_XVCOLOR:
                precond = projectorModel.isXvColorAvailable();
                break;
            case CHANNEL_OVERSCAN:
                precond = projectorModel.isOverscanAvailable();
                break;
            case CHANNEL_PICTURE_POSITION:
                precond = projectorModel.isPicturePositionAvailable();
                break;
            case CHANNEL_LAMP_USE_TIME:
                precond = requestValue;
                break;
            default:
                precond = false;
                break;
        }
        if (isLinked(channel) && precond) {
            state = UnDefType.UNDEF;
            if (requestValue) {
                try {
                    switch (channel) {
                        case CHANNEL_POWER:
                            state = connector.getStatusPower().isOn() ? OnOffType.ON : OnOffType.OFF;
                            break;
                        case CHANNEL_POWERSTATE:
                            state = new StringType(connector.getStatusPower().name());
                            break;
                        case CHANNEL_INPUT:
                            state = new StringType(connector.getInput());
                            break;
                        case CHANNEL_CALIBRATION_PRESET:
                            state = new StringType(connector.getCalibrationPreset());
                            break;
                        case CHANNEL_CONTRAST:
                            state = new PercentType(connector.getContrast());
                            break;
                        case CHANNEL_BRIGHTNESS:
                            state = new PercentType(connector.getBrightness());
                            break;
                        case CHANNEL_COLOR:
                            state = new PercentType(connector.getColor());
                            break;
                        case CHANNEL_HUE:
                            state = new PercentType(connector.getHue());
                            break;
                        case CHANNEL_SHARPNESS:
                            state = new PercentType(connector.getSharpness());
                            break;
                        case CHANNEL_COLOR_TEMP:
                            state = new StringType(connector.getColorTemperature());
                            break;
                        case CHANNEL_IRIS_MODE:
                            state = new StringType(connector.getIrisMode());
                            break;
                        case CHANNEL_IRIS_MANUAL:
                            state = new PercentType(connector.getIrisManual());
                            break;
                        case CHANNEL_IRIS_SENSITIVITY:
                            state = new StringType(connector.getIrisSensitivity());
                            break;
                        case CHANNEL_LAMP_CONTROL:
                            state = new StringType(connector.getLampControl());
                            break;
                        case CHANNEL_FILM_PROJECTION:
                            state = new StringType(connector.getFilmProjection());
                            break;
                        case CHANNEL_MOTION_ENHANCER:
                            state = new StringType(connector.getMotionEnhancer());
                            break;
                        case CHANNEL_CONTRAST_ENHANCER:
                            state = new StringType(connector.getContrastEnhancer());
                            break;
                        case CHANNEL_FILM_MODE:
                            state = new StringType(connector.getFilmMode());
                            break;
                        case CHANNEL_GAMMA_CORRECTION:
                            state = new StringType(connector.getGammaCorrection());
                            break;
                        case CHANNEL_COLOR_SPACE:
                            state = new StringType(connector.getColorSpace());
                            break;
                        case CHANNEL_NR:
                            state = new StringType(connector.getNr());
                            break;
                        case CHANNEL_BLOCK_NR:
                            state = new StringType(connector.getBlockNr());
                            break;
                        case CHANNEL_MOSQUITO_NR:
                            state = new StringType(connector.getMosquitoNr());
                            break;
                        case CHANNEL_MPEG_NR:
                            state = new StringType(connector.getMpegNr());
                            break;
                        case CHANNEL_XVCOLOR:
                            state = connector.getXvColor();
                            break;
                        case CHANNEL_PICTURE_MUTING:
                            state = connector.getPictureMuting();
                            break;
                        case CHANNEL_ASPECT:
                            state = new StringType(connector.getAspect());
                            break;
                        case CHANNEL_OVERSCAN:
                            state = connector.getOverscan();
                            break;
                        case CHANNEL_PICTURE_POSITION:
                            state = new StringType(connector.getPicturePosition());
                            break;
                        case CHANNEL_LAMP_USE_TIME:
                            state = new DecimalType(connector.getLampUseTime());
                            break;
                        default:
                            break;
                    }
                    logger.debug("Refresh channel {} with value {}", channel, state);
                } catch (SonyProjectorException e) {
                    logger.debug("Refresh channel {} failed: {}", channel, e.getMessage());
                }
            }
        }
        return state;
    }

    private void refreshChannel(String channel, boolean requestValue) {
        updateChannelStateAndCache(channel, requestProjectorValue(channel, requestValue));
    }

    private void updateChannelStateAndCache(String channel, @Nullable State state) {
        if (state != null) {
            updateState(channel, state);

            if (!cache.containsKey(channel)) {
                cache.put(channel, () -> {
                    synchronized (commandLock) {
                        return requestProjectorValue(channel, true);
                    }
                });
            }
            cache.putValue(channel, state);
        }
    }
}
