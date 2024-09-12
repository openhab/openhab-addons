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
package org.openhab.binding.russound.internal.rio.zone;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.openhab.binding.russound.internal.net.SocketSession;
import org.openhab.binding.russound.internal.rio.AbstractBridgeHandler;
import org.openhab.binding.russound.internal.rio.AbstractRioHandlerCallback;
import org.openhab.binding.russound.internal.rio.AbstractThingHandler;
import org.openhab.binding.russound.internal.rio.RioCallbackHandler;
import org.openhab.binding.russound.internal.rio.RioConstants;
import org.openhab.binding.russound.internal.rio.RioHandlerCallback;
import org.openhab.binding.russound.internal.rio.RioNamedHandler;
import org.openhab.binding.russound.internal.rio.RioPresetsProtocol;
import org.openhab.binding.russound.internal.rio.RioSystemFavoritesProtocol;
import org.openhab.binding.russound.internal.rio.StatefulHandlerCallback;
import org.openhab.binding.russound.internal.rio.controller.RioControllerHandler;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The bridge handler for a Russound Zone. A zone is the main receiving area for music. This implementation must be
 * attached to a {@link RioControllerHandler} bridge.
 *
 * @author Tim Roberts - Initial contribution
 */
public class RioZoneHandler extends AbstractThingHandler<RioZoneProtocol>
        implements RioNamedHandler, RioCallbackHandler {
    // Logger
    private final Logger logger = LoggerFactory.getLogger(RioZoneHandler.class);

    /**
     * The controller identifier we are attached to
     */
    private final AtomicInteger controller = new AtomicInteger(0);

    /**
     * The zone identifier for this instance
     */
    private final AtomicInteger zone = new AtomicInteger(0);

    /**
     * The zone name for this instance
     */
    private final AtomicReference<String> zoneName = new AtomicReference<>(null);

    /**
     * Constructs the handler from the {@link Thing}
     *
     * @param thing a non-null {@link Thing} the handler is for
     */
    public RioZoneHandler(Thing thing) {
        super(thing);
    }

    /**
     * Returns the controller identifier
     *
     * @return the controller identifier
     */
    public int getController() {
        return controller.get();
    }

    /**
     * Returns the zone identifier
     *
     * @return the zone identifier
     */
    @Override
    public int getId() {
        return zone.get();
    }

    /**
     * Returns the zone name
     *
     * @return the zone name
     */
    @Override
    public String getName() {
        final String name = zoneName.get();
        return name == null || name.isEmpty() ? "Zone " + getId() : name;
    }

    /**
     * {@inheritDoc}
     *
     * Handles commands to specific channels. This implementation will offload much of its work to the
     * {@link RioZoneProtocol}. Basically we validate the type of command for the channel then call the
     * {@link RioZoneProtocol} to handle the actual protocol. Special use case is the {@link RefreshType}
     * where we call {{@link #handleRefresh(String)} to handle a refresh of the specific channel (which in turn calls
     * {@link RioZoneProtocol} to handle the actual refresh
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            handleRefresh(channelUID.getId());
            return;
        }

        // if (getThing().getStatus() != ThingStatus.ONLINE) {
        // // Ignore any command if not online
        // return;
        // }

        String id = channelUID.getId();

        if (id == null) {
            logger.debug("Called with a null channel id - ignoring");
            return;
        }

        if (id.equals(RioConstants.CHANNEL_ZONEBASS)) {
            if (command instanceof DecimalType decimalCommand) {
                getProtocolHandler().setZoneBass(decimalCommand.intValue());
            } else {
                logger.debug("Received a ZONE BASS channel command with a non DecimalType: {}", command);
            }

        } else if (id.equals(RioConstants.CHANNEL_ZONETREBLE)) {
            if (command instanceof DecimalType decimalCommand) {
                getProtocolHandler().setZoneTreble(decimalCommand.intValue());
            } else {
                logger.debug("Received a ZONE TREBLE channel command with a non DecimalType: {}", command);
            }

        } else if (id.equals(RioConstants.CHANNEL_ZONEBALANCE)) {
            if (command instanceof DecimalType decimalCommand) {
                getProtocolHandler().setZoneBalance(decimalCommand.intValue());
            } else {
                logger.debug("Received a ZONE BALANCE channel command with a non DecimalType: {}", command);
            }

        } else if (id.equals(RioConstants.CHANNEL_ZONETURNONVOLUME)) {
            if (command instanceof PercentType percentCommand) {
                getProtocolHandler().setZoneTurnOnVolume(percentCommand.intValue() / 100d);
            } else if (command instanceof DecimalType decimalCommand) {
                getProtocolHandler().setZoneTurnOnVolume(decimalCommand.doubleValue());
            } else {
                logger.debug("Received a ZONE TURN ON VOLUME channel command with a non PercentType/DecimalType: {}",
                        command);
            }

        } else if (id.equals(RioConstants.CHANNEL_ZONELOUDNESS)) {
            if (command instanceof OnOffType) {
                getProtocolHandler().setZoneLoudness(command == OnOffType.ON);
            } else {
                logger.debug("Received a ZONE TURN ON VOLUME channel command with a non OnOffType: {}", command);
            }

        } else if (id.equals(RioConstants.CHANNEL_ZONESLEEPTIMEREMAINING)) {
            if (command instanceof DecimalType decimalCommand) {
                getProtocolHandler().setZoneSleepTimeRemaining(decimalCommand.intValue());
            } else {
                logger.debug("Received a ZONE SLEEP TIME REMAINING channel command with a non DecimalType: {}",
                        command);
            }
        } else if (id.equals(RioConstants.CHANNEL_ZONESOURCE)) {
            if (command instanceof DecimalType decimalCommand) {
                getProtocolHandler().setZoneSource(decimalCommand.intValue());
            } else {
                logger.debug("Received a ZONE SOURCE channel command with a non DecimalType: {}", command);
            }

        } else if (id.equals(RioConstants.CHANNEL_ZONESTATUS)) {
            if (command instanceof OnOffType) {
                getProtocolHandler().setZoneStatus(command == OnOffType.ON);
            } else {
                logger.debug("Received a ZONE STATUS channel command with a non OnOffType: {}", command);
            }
        } else if (id.equals(RioConstants.CHANNEL_ZONEPARTYMODE)) {
            if (command instanceof StringType stringCommand) {
                getProtocolHandler().setZonePartyMode(stringCommand.toString());
            } else {
                logger.debug("Received a ZONE PARTY MODE channel command with a non StringType: {}", command);
            }

        } else if (id.equals(RioConstants.CHANNEL_ZONEDONOTDISTURB)) {
            if (command instanceof StringType stringCommand) {
                getProtocolHandler().setZoneDoNotDisturb(stringCommand.toString());
            } else {
                logger.debug("Received a ZONE DO NOT DISTURB channel command with a non StringType: {}", command);
            }

        } else if (id.equals(RioConstants.CHANNEL_ZONEMUTE)) {
            if (command instanceof OnOffType) {
                getProtocolHandler().toggleZoneMute();
            } else {
                logger.debug("Received a ZONE MUTE channel command with a non OnOffType: {}", command);
            }

        } else if (id.equals(RioConstants.CHANNEL_ZONEREPEAT)) {
            if (command instanceof OnOffType) {
                getProtocolHandler().toggleZoneRepeat();
            } else {
                logger.debug("Received a ZONE REPEAT channel command with a non OnOffType: {}", command);
            }

        } else if (id.equals(RioConstants.CHANNEL_ZONESHUFFLE)) {
            if (command instanceof OnOffType) {
                getProtocolHandler().toggleZoneShuffle();
            } else {
                logger.debug("Received a ZONE SHUFFLE channel command with a non OnOffType: {}", command);
            }

        } else if (id.equals(RioConstants.CHANNEL_ZONEVOLUME)) {
            if (command instanceof OnOffType) {
                getProtocolHandler().setZoneStatus(command == OnOffType.ON);
            } else if (command instanceof IncreaseDecreaseType) {
                getProtocolHandler().setZoneVolume(command == IncreaseDecreaseType.INCREASE);
            } else if (command instanceof PercentType percentCommand) {
                getProtocolHandler().setZoneVolume(percentCommand.intValue() / 100d);
            } else if (command instanceof DecimalType decimalCommand) {
                getProtocolHandler().setZoneVolume(decimalCommand.doubleValue());
            } else {
                logger.debug(
                        "Received a ZONE VOLUME channel command with a non OnOffType/IncreaseDecreaseType/PercentType/DecimalTye: {}",
                        command);
            }

        } else if (id.equals(RioConstants.CHANNEL_ZONERATING)) {
            if (command instanceof OnOffType) {
                getProtocolHandler().setZoneRating(command == OnOffType.ON);
            } else {
                logger.debug("Received a ZONE RATING channel command with a non OnOffType: {}", command);
            }

        } else if (id.equals(RioConstants.CHANNEL_ZONEKEYPRESS)) {
            if (command instanceof StringType stringCommand) {
                getProtocolHandler().sendKeyPress(stringCommand.toString());
            } else {
                logger.debug("Received a ZONE KEYPRESS channel command with a non StringType: {}", command);
            }

        } else if (id.equals(RioConstants.CHANNEL_ZONEKEYRELEASE)) {
            if (command instanceof StringType stringCommand) {
                getProtocolHandler().sendKeyRelease(stringCommand.toString());
            } else {
                logger.debug("Received a ZONE KEYRELEASE channel command with a non StringType: {}", command);
            }

        } else if (id.equals(RioConstants.CHANNEL_ZONEKEYHOLD)) {
            if (command instanceof StringType stringCommand) {
                getProtocolHandler().sendKeyHold(stringCommand.toString());
            } else {
                logger.debug("Received a ZONE KEYHOLD channel command with a non StringType: {}", command);
            }

        } else if (id.equals(RioConstants.CHANNEL_ZONEKEYCODE)) {
            if (command instanceof StringType stringCommand) {
                getProtocolHandler().sendKeyCode(stringCommand.toString());
            } else {
                logger.debug("Received a ZONE KEYCODE channel command with a non StringType: {}", command);
            }

        } else if (id.equals(RioConstants.CHANNEL_ZONEEVENT)) {
            if (command instanceof StringType stringCommand) {
                getProtocolHandler().sendEvent(stringCommand.toString());
            } else {
                logger.debug("Received a ZONE EVENT channel command with a non StringType: {}", command);
            }

        } else if (id.equals(RioConstants.CHANNEL_ZONEMMINIT)) {
            getProtocolHandler().sendMMInit();

        } else if (id.equals(RioConstants.CHANNEL_ZONEMMCONTEXTMENU)) {
            getProtocolHandler().sendMMContextMenu();

        } else if (id.equals(RioConstants.CHANNEL_ZONESYSFAVORITES)) {
            if (command instanceof StringType) {
                // Remove any state for this channel to ensure it's recreated/sent again
                // (clears any bad or deleted favorites information from the channel)
                ((StatefulHandlerCallback) getProtocolHandler().getCallback())
                        .removeState(RioConstants.CHANNEL_ZONESYSFAVORITES);

                getProtocolHandler().setSystemFavorites(command.toString());
            } else {
                logger.debug("Received a SYSTEM FAVORITES channel command with a non StringType: {}", command);
            }

        } else if (id.equals(RioConstants.CHANNEL_ZONEFAVORITES)) {
            if (command instanceof StringType) {
                // Remove any state for this channel to ensure it's recreated/sent again
                // (clears any bad or deleted favorites information from the channel)
                ((StatefulHandlerCallback) getProtocolHandler().getCallback())
                        .removeState(RioConstants.CHANNEL_ZONEFAVORITES);

                // schedule the returned callback in the future (to allow the channel to process and to allow russound
                // to process (before re-retrieving information)
                scheduler.schedule(getProtocolHandler().setZoneFavorites(command.toString()), 250,
                        TimeUnit.MILLISECONDS);

            } else {
                logger.debug("Received a ZONE FAVORITES channel command with a non StringType: {}", command);
            }
        } else if (id.equals(RioConstants.CHANNEL_ZONEPRESETS)) {
            if (command instanceof StringType) {
                ((StatefulHandlerCallback) getProtocolHandler().getCallback())
                        .removeState(RioConstants.CHANNEL_ZONEPRESETS);

                getProtocolHandler().setZonePresets(command.toString());
            } else {
                logger.debug("Received a ZONE FAVORITES channel command with a non StringType: {}", command);
            }
        } else {
            logger.debug("Unknown/Unsupported Channel id: {}", id);
        }
    }

    /**
     * Method that handles the {@link RefreshType} command specifically. Calls the {@link RioZoneProtocol} to
     * handle the actual refresh based on the channel id.
     *
     * @param id a non-null, possibly empty channel id to refresh
     */
    private void handleRefresh(String id) {
        if (getThing().getStatus() != ThingStatus.ONLINE) {
            return;
        }

        if (getProtocolHandler() == null) {
            return;
        }

        // Remove the cache'd value to force a refreshed value
        ((StatefulHandlerCallback) getProtocolHandler().getCallback()).removeState(id);

        if (id.equals(RioConstants.CHANNEL_ZONENAME)) {
            getProtocolHandler().refreshZoneName();
        } else if (id.startsWith(RioConstants.CHANNEL_ZONESOURCE)) {
            getProtocolHandler().refreshZoneSource();
        } else if (id.startsWith(RioConstants.CHANNEL_ZONEBASS)) {
            getProtocolHandler().refreshZoneBass();
        } else if (id.startsWith(RioConstants.CHANNEL_ZONETREBLE)) {
            getProtocolHandler().refreshZoneTreble();
        } else if (id.startsWith(RioConstants.CHANNEL_ZONEBALANCE)) {
            getProtocolHandler().refreshZoneBalance();
        } else if (id.startsWith(RioConstants.CHANNEL_ZONELOUDNESS)) {
            getProtocolHandler().refreshZoneLoudness();
        } else if (id.startsWith(RioConstants.CHANNEL_ZONETURNONVOLUME)) {
            getProtocolHandler().refreshZoneTurnOnVolume();
        } else if (id.startsWith(RioConstants.CHANNEL_ZONEDONOTDISTURB)) {
            getProtocolHandler().refreshZoneDoNotDisturb();
        } else if (id.startsWith(RioConstants.CHANNEL_ZONEPARTYMODE)) {
            getProtocolHandler().refreshZonePartyMode();
        } else if (id.startsWith(RioConstants.CHANNEL_ZONESTATUS)) {
            getProtocolHandler().refreshZoneStatus();
        } else if (id.startsWith(RioConstants.CHANNEL_ZONEVOLUME)) {
            getProtocolHandler().refreshZoneVolume();
        } else if (id.startsWith(RioConstants.CHANNEL_ZONEMUTE)) {
            getProtocolHandler().refreshZoneMute();
        } else if (id.startsWith(RioConstants.CHANNEL_ZONEPAGE)) {
            getProtocolHandler().refreshZonePage();
        } else if (id.startsWith(RioConstants.CHANNEL_ZONESHAREDSOURCE)) {
            getProtocolHandler().refreshZoneSharedSource();
        } else if (id.startsWith(RioConstants.CHANNEL_ZONESLEEPTIMEREMAINING)) {
            getProtocolHandler().refreshZoneSleepTimeRemaining();
        } else if (id.startsWith(RioConstants.CHANNEL_ZONELASTERROR)) {
            getProtocolHandler().refreshZoneLastError();
        } else if (id.equals(RioConstants.CHANNEL_ZONESYSFAVORITES)) {
            getProtocolHandler().refreshSystemFavorites();
        } else if (id.equals(RioConstants.CHANNEL_ZONEFAVORITES)) {
            getProtocolHandler().refreshZoneFavorites();
        } else if (id.equals(RioConstants.CHANNEL_ZONEPRESETS)) {
            getProtocolHandler().refreshZonePresets();
        }
        // Can't refresh any others...
    }

    /**
     * Initializes the bridge. Confirms the configuration is valid and that our parent bridge is a
     * {@link RioControllerHandler}. Once validated, a {@link RioZoneProtocol} is set via
     * {@link #setProtocolHandler(AbstractRioProtocol)} and the bridge comes online.
     */
    @Override
    public void initialize() {
        final Bridge bridge = getBridge();
        if (bridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Cannot be initialized without a bridge");
            return;
        }
        if (bridge.getStatus() != ThingStatus.ONLINE) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            return;
        }

        final ThingHandler handler = bridge.getHandler();
        if (handler == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "No handler specified (null) for the bridge!");
            return;
        }

        if (!(handler instanceof RioControllerHandler)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Source must be attached to a controller bridge: " + handler.getClass());
            return;
        }

        final RioZoneConfig config = getThing().getConfiguration().as(RioZoneConfig.class);
        if (config == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Configuration file missing");
            return;
        }

        final int configZone = config.getZone();
        if (configZone < 1 || configZone > 8) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Source must be between 1 and 8: " + configZone);
            return;
        }
        zone.set(configZone);

        final int handlerController = ((RioControllerHandler) handler).getId();
        controller.set(handlerController);

        // Get the socket session from the
        final SocketSession socketSession = getSocketSession();
        if (socketSession == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR, "No socket session found");
            return;
        }

        setProtocolHandler(new RioZoneProtocol(configZone, handlerController, getSystemFavoritesHandler(),
                getPresetsProtocol(), socketSession, new StatefulHandlerCallback(new AbstractRioHandlerCallback() {
                    @Override
                    public void statusChanged(ThingStatus status, ThingStatusDetail detail, String msg) {
                        updateStatus(status, detail, msg);
                    }

                    @Override
                    public void stateChanged(String channelId, State state) {
                        if (channelId.equals(RioConstants.CHANNEL_ZONENAME)) {
                            zoneName.set(state.toString());
                        }
                        updateState(channelId, state);
                        fireStateUpdated(channelId, state);
                    }

                    @Override
                    public void setProperty(String propertyName, String propertyValue) {
                        getThing().setProperty(propertyName, propertyValue);
                    }
                })));

        updateStatus(ThingStatus.ONLINE);
        getProtocolHandler().postOnline();
    }

    /**
     * Returns the {@link RioHandlerCallback} related to the zone
     *
     * @return a possibly null {@link RioHandlerCallback}
     */
    @Override
    public RioHandlerCallback getRioHandlerCallback() {
        final RioZoneProtocol protocolHandler = getProtocolHandler();
        return protocolHandler == null ? null : protocolHandler.getCallback();
    }

    /**
     * Returns the {@link RioPresetsProtocol} related to the system. This simply queries the parent bridge for the
     * protocol
     *
     * @return a possibly null {@link RioPresetsProtocol}
     */
    @SuppressWarnings("rawtypes")
    RioPresetsProtocol getPresetsProtocol() {
        final Bridge bridge = getBridge();
        if (bridge != null && bridge.getHandler() instanceof AbstractBridgeHandler) {
            return ((AbstractBridgeHandler) bridge.getHandler()).getPresetsProtocol();
        }
        return null;
    }

    /**
     * Returns the {@link RioSystemFavoritesProtocol} related to the system. This simply queries the parent bridge for
     * the protocol
     *
     * @return a possibly null {@link RioSystemFavoritesProtocol}
     */
    @SuppressWarnings("rawtypes")
    RioSystemFavoritesProtocol getSystemFavoritesHandler() {
        final Bridge bridge = getBridge();
        if (bridge != null && bridge.getHandler() instanceof AbstractBridgeHandler) {
            return ((AbstractBridgeHandler) bridge.getHandler()).getSystemFavoritesHandler();
        }
        return null;
    }
}
