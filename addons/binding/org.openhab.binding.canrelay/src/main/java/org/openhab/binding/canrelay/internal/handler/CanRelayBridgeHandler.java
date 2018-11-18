/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.canrelay.internal.handler;

import static org.openhab.binding.canrelay.internal.CanRelayBindingConstants.*;
import static org.openhab.binding.canrelay.internal.canbus.CanBusDeviceStatus.*;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.canrelay.internal.CanRelayConfiguration;
import org.openhab.binding.canrelay.internal.canbus.CanBusDeviceStatus;
import org.openhab.binding.canrelay.internal.protocol.CanRelayAccess;
import org.openhab.binding.canrelay.internal.protocol.CanRelayChangeListener;
import org.openhab.binding.canrelay.internal.runtime.Runtime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link CanRelayBridgeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Lubos Housa - Initial contribution
 */
@NonNullByDefault
public class CanRelayBridgeHandler extends BaseBridgeHandler implements CanRelayChangeListener {

    private static final String DUPLICIT_BRIDGE_PROPERTY = "duplicitBridge";
    private static final int REFRESH_INIT_START_DELAY = 2;
    private static final int REFRESH_RUN_INTERVAL = 60;
    private static final int REFRESH_RUN_START_DELAY = 5;

    private static final Logger logger = LoggerFactory.getLogger(CanRelayBridgeHandler.class);

    private final CanRelayAccess canRelayAccess;
    private final Runtime runtime;

    @NonNullByDefault({})
    private ScheduledFuture<?> startup, refresh;

    public CanRelayBridgeHandler(Bridge thing, CanRelayAccess canRelayAccess, Runtime runtime) {
        super(thing);
        this.canRelayAccess = canRelayAccess;
        this.runtime = runtime;
    }

    /**
     * Invoked upon connecting to the canRelay in the case it was successful. THis method would first register a
     * "startup" refresh - if after some time this bridge was initiated the canRelayAccess has not updated its cache,
     * then it means this bridge was already stored before and the server just got started. In that case ask can relay
     * access to detect all lights and then post command towards all lights.
     *
     * In addition to that then register a regular "polling" refresh in canrelay that would periodically check
     * consistency between CANBUS and its internal cache and would return missmatches found (these would need to be
     * reflected in the UI things/lights)
     */
    private void registerRefresh() {
        logger.debug("Registering refresh background tasks for CanRelayAccess.");
        startup = scheduler.schedule(() -> {
            canRelayAccess.initCache();
        }, REFRESH_INIT_START_DELAY, TimeUnit.SECONDS);

        refresh = scheduler.scheduleWithFixedDelay(() -> {
            canRelayAccess.refreshCache()
                    .forEach((lightState) -> onLightSwitchChanged(lightState.getNodeID(), lightState.getState()));
        }, REFRESH_RUN_START_DELAY, REFRESH_RUN_INTERVAL, TimeUnit.SECONDS);
    }

    private void unregisterRefresh() {
        logger.debug("Unregistering refresh background tasks for CanRelayAccess.");
        if (startup != null) {
            startup.cancel(false);
        }
        if (refresh != null) {
            refresh.cancel(false);
        }
    }

    @Override
    public void initialize() {
        logger.debug("Initializing the CanRelay bridge...");
        CanRelayConfiguration config = getConfigAs(CanRelayConfiguration.class);

        // set the thing status to UNKNOWN temporarily and let the background task decide for the real status.
        updateStatus(ThingStatus.UNKNOWN);

        if (canRelayAccess.getStatus() != UNITIALIZED) {
            logger.warn(
                    "CanRelay has already been setup before most likely by another bridge. It reports status {}. Not initiating this bridge as a result since only 1 bridge is supported at the moment at runtime.",
                    canRelayAccess.getStatus());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "A CanRelay bridge already exists, the binding only supports 1.");
            // add a marker to this bridge not to perform any disconnect in dispose since the other bridge is connected
            // using CanRelay
            getThing().setProperty(DUPLICIT_BRIDGE_PROPERTY, "");
            return;
        }

        canRelayAccess.registerListener(this);
        runtime.setBridgeUID(thing.getUID());

        scheduler.execute(() -> {
            CanBusDeviceStatus status = canRelayAccess.connect(config.serialPort);
            if (status == CONNECTED) {
                logger.debug("CanRelay bridge initiated succesfully");
                updateStatus(ThingStatus.ONLINE);
                registerRefresh();
            } else {
                logger.warn("Error connecting to can. Setting the bridge to offline status. CanRelayStatus: {}",
                        status);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Error communicating to port " + config.serialPort);
            }
        });
    }

    @Override
    public void dispose() {
        if (!getThing().getProperties().containsKey(DUPLICIT_BRIDGE_PROPERTY)) {
            logger.debug("Disposing the CanRelay bridge...");
            canRelayAccess.unRegisterListener(this);
            runtime.setBridgeUID(null);
            unregisterRefresh();

            canRelayAccess.disconnect();
            super.dispose();
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // ignore any potential commands, all should be targeted via individual light switches
    }

    /**
     * Handle a command from a light child thing of this bridge as detected in the UI by a user. All child things would
     * delegate their commands in here for this main handler to send the respective CANBUS traffic over
     *
     * @param nodeID  id of the light thing that is to be switched on/off (represents real nodeID to be transmitted over
     *                    CANBUS)
     * @param command actual operation to perform (ON/OFF)
     * @return true if command handled successfully, false otherwise
     */
    public boolean handleLightSwitchCommand(Integer nodeID, OnOffType command) {
        logger.debug("Received command {} for light switch with nodeID {}", command, nodeAsString(nodeID));
        return canRelayAccess.handleSwitchCommand(nodeID, command);
    }

    @Override
    public void onLightSwitchChanged(int nodeID, OnOffType command) {
        logger.debug("Received light switch operation on CANBUS. Light {} was switched {}", nodeAsString(nodeID),
                command);
        String nodeIDProperty = String.valueOf(nodeID);

        // CANBUS driven change of a light switch, so just try to find a child thing with this nodeID (representation
        // property) and change its state
        for (Thing light : getThing().getThings()) {
            if (nodeIDProperty.equals(light.getConfiguration().get(CONFIG_NODEID).toString())) {
                CanRelayLightSwitchHandler lightHandler = (CanRelayLightSwitchHandler) light.getHandler();
                if (lightHandler != null) {
                    logger.debug(
                            "Found the light thing and its handler to post {} command to. Updating UI. Found light: {}",
                            command, light);
                    lightHandler.postCommand(CHANNEL_LIGHT_SWITCH, command);
                } else {
                    logger.debug(
                            "The found light thing has no handler, cannot post any command to it. UI will not be updated properly.");
                }
                return;
            }
        }
        // got till here without return in the above loop, so got a nodeID we do not know
        logger.debug("Light with nodeID {} was not found under this bridge. Ignoring the command {}.",
                nodeAsString(nodeID), command);
    }

    @Override
    public void onCanRelayOffline(String error) {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, error);
    }

}
