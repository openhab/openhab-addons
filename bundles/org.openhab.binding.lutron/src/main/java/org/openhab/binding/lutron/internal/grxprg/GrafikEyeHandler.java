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
package org.openhab.binding.lutron.internal.grxprg;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link BaseThingHandler} is responsible for handling a specific grafik eye unit (identified by it's control
 * number). This handler is responsible for handling the commands and management for a single grafik eye unit.
 *
 * @author Tim Roberts - Initial contribution
 */
public class GrafikEyeHandler extends BaseThingHandler {

    /**
     * Logger used by this class
     */
    private Logger logger = LoggerFactory.getLogger(GrafikEyeHandler.class);

    /**
     * Cached instance of the {@link GrafikEyeConfig}. Will be null if disconnected.
     */
    private GrafikEyeConfig config = null;

    /**
     * The current fade for the grafik eye (only used when setting zone intensity). Will initially be set from
     * configuration.
     */
    private int fade = 0;

    /**
     * The polling job to poll the actual state of the grafik eye
     */
    private ScheduledFuture<?> pollingJob;

    /**
     * Constructs the handler from the {@link org.openhab.core.thing.Thing}
     *
     * @param thing a non-null {@link org.openhab.core.thing.Thing} the handler is for
     */
    public GrafikEyeHandler(Thing thing) {
        super(thing);

        if (thing == null) {
            throw new IllegalArgumentException("thing cannot be null");
        }
    }

    /**
     * {@inheritDoc}
     *
     * Handles commands to specific channels. This implementation will offload much of its work to the
     * {@link PrgProtocolHandler}. Basically we validate the type of command for the channel then call the
     * {@link PrgProtocolHandler} to handle the actual protocol. Special use case is the {@link RefreshType}
     * where we call {{@link #handleRefresh(String)} to handle a refresh of the specific channel (which in turn calls
     * {@link PrgProtocolHandler} to handle the actual refresh
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            handleRefresh(channelUID.getId());
            return;
        }

        if (getThing().getStatus() != ThingStatus.ONLINE) {
            // Ignore any command if not online
            return;
        }

        String id = channelUID.getId();

        if (id == null) {
            logger.warn("Called with a null channel id - ignoring");
            return;
        }

        if (id.equals(PrgConstants.CHANNEL_SCENE)) {
            if (command instanceof DecimalType sceneCommand) {
                final int scene = sceneCommand.intValue();
                getProtocolHandler().selectScene(config.getControlUnit(), scene);
            } else {
                logger.error("Received a SCENE command with a non DecimalType: {}", command);
            }
        } else if (id.equals(PrgConstants.CHANNEL_SCENELOCK)) {
            if (command instanceof OnOffType) {
                getProtocolHandler().setSceneLock(config.getControlUnit(), command == OnOffType.ON);
            } else {
                logger.error("Received a SCENELOCK command with a non OnOffType: {}", command);
            }
        } else if (id.equals(PrgConstants.CHANNEL_SCENESEQ)) {
            if (command instanceof OnOffType) {
                getProtocolHandler().setSceneSequence(config.getControlUnit(), command == OnOffType.ON);
            } else {
                logger.error("Received a SCENESEQ command with a non OnOffType: {}", command);
            }
        } else if (id.equals(PrgConstants.CHANNEL_ZONELOCK)) {
            if (command instanceof OnOffType) {
                getProtocolHandler().setZoneLock(config.getControlUnit(), command == OnOffType.ON);
            } else {
                logger.error("Received a ZONELOCK command with a non OnOffType: {}", command);
            }
        } else if (id.startsWith(PrgConstants.CHANNEL_ZONELOWER)) {
            final Integer zone = getTrailingNbr(id, PrgConstants.CHANNEL_ZONELOWER);

            if (zone != null) {
                getProtocolHandler().setZoneLower(config.getControlUnit(), zone);
            }
        } else if (id.startsWith(PrgConstants.CHANNEL_ZONERAISE)) {
            final Integer zone = getTrailingNbr(id, PrgConstants.CHANNEL_ZONERAISE);

            if (zone != null) {
                getProtocolHandler().setZoneRaise(config.getControlUnit(), zone);
            }
        } else if (id.equals(PrgConstants.CHANNEL_ZONEFADE)) {
            if (command instanceof DecimalType zoneFade) {
                setFade(zoneFade.intValue());
            } else {
                logger.error("Received a ZONEFADE command with a non DecimalType: {}", command);
            }
        } else if (id.startsWith(PrgConstants.CHANNEL_ZONEINTENSITY)) {
            final Integer zone = getTrailingNbr(id, PrgConstants.CHANNEL_ZONEINTENSITY);

            if (zone != null) {
                if (command instanceof PercentType intensityPercent) {
                    final int intensity = intensityPercent.intValue();
                    getProtocolHandler().setZoneIntensity(config.getControlUnit(), zone, fade, intensity);
                } else if (command instanceof OnOffType) {
                    getProtocolHandler().setZoneIntensity(config.getControlUnit(), zone, fade,
                            command == OnOffType.ON ? 100 : 0);
                } else if (command instanceof IncreaseDecreaseType) {
                    getProtocolHandler().setZoneIntensity(config.getControlUnit(), zone, fade,
                            command == IncreaseDecreaseType.INCREASE);
                } else {
                    logger.error("Received a ZONEINTENSITY command with a non DecimalType: {}", command);
                }
            }
        } else if (id.startsWith(PrgConstants.CHANNEL_ZONESHADE)) {
            final Integer zone = getTrailingNbr(id, PrgConstants.CHANNEL_ZONESHADE);

            if (zone != null) {
                if (command instanceof PercentType) {
                    logger.info("PercentType is not suppored by QED shades");
                } else if (command == StopMoveType.MOVE) {
                    logger.info("StopMoveType.Move is not suppored by QED shades");
                } else if (command == StopMoveType.STOP) {
                    getProtocolHandler().setZoneIntensity(config.getControlUnit(), zone, fade, 0);
                } else if (command instanceof UpDownType) {
                    getProtocolHandler().setZoneIntensity(config.getControlUnit(), zone, fade,
                            command == UpDownType.UP ? 1 : 2);
                } else {
                    logger.error("Received a ZONEINTENSITY command with a non DecimalType: {}", command);
                }
            }
        } else {
            logger.error("Unknown/Unsupported Channel id: {}", id);
        }
    }

    /**
     * Method that handles the {@link RefreshType} command specifically. Calls the {@link PrgProtocolHandler} to
     * handle the actual refresh based on the channel id.
     *
     * @param id a non-null, possibly empty channel id to refresh
     */
    private void handleRefresh(String id) {
        if (getThing().getStatus() != ThingStatus.ONLINE) {
            return;
        }

        if (id.equals(PrgConstants.CHANNEL_SCENE)) {
            getProtocolHandler().refreshScene();
        } else if (id.equals(PrgConstants.CHANNEL_ZONEINTENSITY)) {
            getProtocolHandler().refreshZoneIntensity(config.getControlUnit());
        } else if (id.equals(PrgConstants.CHANNEL_ZONEFADE)) {
            updateState(PrgConstants.CHANNEL_ZONEFADE, new DecimalType(fade));
        }
    }

    /**
     * Gets the trailing number from the channel id (which usually represents the zone number).
     *
     * @param id a non-null, possibly empty channel id
     * @param channelConstant a non-null, non-empty channel id constant to use in the parse.
     * @return the trailing number or null if a parse exception occurs
     */
    private Integer getTrailingNbr(String id, String channelConstant) {
        try {
            return Integer.parseInt(id.substring(channelConstant.length()));
        } catch (NumberFormatException e) {
            logger.warn("Unknown channel port #: {}", id);
            return null;
        }
    }

    /**
     * Initializes the thing - basically calls {@link #internalInitialize()} to do the work
     */
    @Override
    public void initialize() {
        cancelPolling();
        internalInitialize();
    }

    /**
     * Set's the unit to offline and attempts to reinitialize via {@link #internalInitialize()}
     */
    @Override
    public void thingUpdated(Thing thing) {
        cancelPolling();
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_CONFIGURATION_PENDING);
        this.thing = thing;
        internalInitialize();
    }

    /**
     * If the bridge goes offline, cancels the polling and goes offline. If the bridge goes online, will attempt to
     * re-initialize via {@link #internalInitialize()}
     */
    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        cancelPolling();
        if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE) {
            internalInitialize();
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        }
    }

    /**
     * Initializes the grafik eye. Essentially validates the {@link GrafikEyeConfig}, updates the status to online and
     * starts a status refresh job
     */
    private void internalInitialize() {
        config = getThing().getConfiguration().as(GrafikEyeConfig.class);

        if (config == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Configuration file missing");
            return;
        }

        final String configErr = config.validate();
        if (configErr != null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, configErr);
            return;
        }

        final Bridge bridge = getBridge();
        if (bridge == null || !(bridge.getHandler() instanceof PrgBridgeHandler)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "GrafikEye must have a parent PRG Bridge");
            return;
        }

        final ThingHandler handler = bridge.getHandler();
        if (handler.getThing().getStatus() != ThingStatus.ONLINE) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            return;
        }

        updateStatus(ThingStatus.ONLINE);
        setFade(config.getFade());

        cancelPolling();
        pollingJob = this.scheduler.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                final ThingStatus status = getThing().getStatus();
                if (status == ThingStatus.ONLINE && config != null) {
                    getProtocolHandler().refreshState(config.getControlUnit());
                }
            }
        }, 1, config.getPolling(), TimeUnit.SECONDS);
    }

    /**
     * Helper method to cancel our polling if we are currently polling
     */
    private void cancelPolling() {
        if (pollingJob != null) {
            pollingJob.cancel(true);
            pollingJob = null;
        }
    }

    /**
     * Returns the {@link PrgProtocolHandler} to use
     *
     * @return a non-null {@link PrgProtocolHandler} to use
     */
    private PrgProtocolHandler getProtocolHandler() {
        final Bridge bridge = getBridge();
        if (bridge == null || !(bridge.getHandler() instanceof PrgBridgeHandler)) {
            throw new IllegalArgumentException("Cannot have a Grafix Eye thing outside of the PRG bridge");
        }

        final PrgProtocolHandler handler = ((PrgBridgeHandler) bridge.getHandler()).getProtocolHandler();
        if (handler == null) {
            throw new IllegalArgumentException("No protocol handler set in the PrgBridgeHandler!");
        }
        return handler;
    }

    /**
     * Returns the control unit for this handler
     *
     * @return the control unit
     */
    int getControlUnit() {
        return config.getControlUnit();
    }

    /**
     * Helper method to determine if the given zone is a shade. Off loads it's work to
     * {@link GrafikEyeConfig#isShadeZone(int)}
     *
     * @param zone a zone to check
     * @return true if a shade zone, false otherwise
     */
    boolean isShade(int zone) {
        return config == null ? false : config.isShadeZone(zone);
    }

    /**
     * Helper method to expose the ability to change state outside of the class
     *
     * @param channelId the channel id
     * @param state the new state
     */
    void stateChanged(String channelId, State state) {
        updateState(channelId, state);
    }

    /**
     * Helper method to set the fade level. Will store the fade and update its state.
     *
     * @param fade the new fade
     */
    private void setFade(int fade) {
        if (fade < 0 || fade > 3600) {
            throw new IllegalArgumentException("fade must be between 1-3600");
        }

        this.fade = fade;
        updateState(PrgConstants.CHANNEL_ZONEFADE, new DecimalType(this.fade));
    }
}
