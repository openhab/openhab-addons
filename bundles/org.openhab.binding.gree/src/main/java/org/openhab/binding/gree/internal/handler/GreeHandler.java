/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.gree.internal.handler;

import static org.openhab.binding.gree.internal.GreeBindingConstants.*;

import java.net.DatagramSocket;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.gree.internal.GreeConfiguration;
import org.openhab.binding.gree.internal.discovery.GreeAirDevice;
import org.openhab.binding.gree.internal.discovery.GreeDeviceFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link GreeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author John Cunha - Initial contribution
 * @author Markus Michels - Refactoring, adapted to OH 2.5x
 */
@NonNullByDefault
public class GreeHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(GreeHandler.class);
    private GreeConfiguration config = new GreeConfiguration();
    private GreeDeviceFinder deviceFinder = new GreeDeviceFinder();
    private GreeAirDevice thisDevice = new GreeAirDevice();
    private @Nullable DatagramSocket clientSocket;
    private final String defBroadcastAddress;

    private int refreshTime = 0;
    private @Nullable ScheduledFuture<?> refreshTask;
    private long lastRefreshTime = 0;

    public GreeHandler(Thing thing, String broadcastAddress) {
        super(thing);
        this.defBroadcastAddress = broadcastAddress;
    }

    @Override
    public void initialize() {
        // logger.debug("Start initializing!");
        config = getConfigAs(GreeConfiguration.class);
        logger.debug("Config for {} is {}", thing.getUID(), config);

        // set the thing status to UNKNOWN temporarily and let the background task decide for the real status.
        // the framework is then able to reuse the resources from the thing handler initialization.
        // we set this upfront to reliably check status updates in unit tests.
        updateStatus(ThingStatus.UNKNOWN);

        scheduler.execute(() -> {
            initializeThing();
        });
    }

    private void initializeThing() {
        logger.debug("Thing {} is initializing", thing.getUID());

        if (!config.isValid()) {
            logger.debug("Config of {} is invalid. Check configuration", thing.getUID());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Invalid configuration. Check thing configuration.");
            return;
        }

        // Now Scan For Airconditioners
        try {
            refreshTime = config.getRefresh();

            // Create a new Datagram socket with a specified timeout
            DatagramSocket newSocket = new DatagramSocket();
            clientSocket = newSocket;
            clientSocket.setSoTimeout(DATAGRAM_SOCKET_TIMEOUT);

            // Firstly, lets find all Gree Airconditioners on the network
            String broadcastIp = !config.getBroadcastAddress().isEmpty() ? config.getBroadcastAddress()
                    : defBroadcastAddress;
            deviceFinder = new GreeDeviceFinder(broadcastIp);
            deviceFinder.Scan(clientSocket);
            logger.debug("{} units found during discovery", deviceFinder.getScannedDeviceCount());

            // Now check that this one is amongst the air conditioners that responded.
            GreeAirDevice newDevice = deviceFinder.getDeviceByIPAddress(config.getIpAddress());
            if (newDevice != null) {
                // Ok, our device responded
                // Now let's Bind with it
                thisDevice = newDevice;
                thisDevice.bindWithDevice(clientSocket);
                if (thisDevice.getIsBound()) {
                    logger.info("Gree AirConditioner Device {} from was Succesfully bound", thing.getUID());
                    updateStatus(ThingStatus.ONLINE);

                    // Start the automatic refresh cycles
                    startAutomaticRefresh();
                    return;
                }
            }
        } catch (UnknownHostException e) {
            logger.debug("Initialization failed: unknown host {}", config.getBroadcastAddress(), e);
        } catch (Exception e) {
            logger.debug("Initialization failed", e);
        }
        updateStatus(ThingStatus.OFFLINE);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            // The thing is updated by the scheduled automatic refresh so do nothing here.
        } else {
            try {
                switch (channelUID.getIdWithoutGroup()) {
                    case POWER_CHANNEL:
                        thisDevice.setDevicePower(clientSocket, (OnOffType) command == OnOffType.ON ? 1 : 0);
                        break;
                    case MODE_CHANNEL:
                        thisDevice.SetDeviceMode(clientSocket, ((DecimalType) command).intValue());
                        break;
                    case TURBO_CHANNEL:
                        thisDevice.setDeviceTurbo(clientSocket, (OnOffType) command == OnOffType.ON ? 1 : 0);
                        break;
                    case LIGHT_CHANNEL:
                        thisDevice.setDeviceLight(clientSocket, (OnOffType) command == OnOffType.ON ? 1 : 0);
                        break;
                    case TEMP_CHANNEL:
                        thisDevice.setDeviceTempSet(clientSocket, ((DecimalType) command).intValue());
                        break;
                    case SWINGV_CHANNEL:
                        thisDevice.setDeviceSwingVertical(clientSocket, ((DecimalType) command).intValue());
                        break;
                    case WINDSPEED_CHANNEL:
                        thisDevice.setDeviceWindspeed(clientSocket, ((DecimalType) command).intValue());
                        break;
                    case AIR_CHANNEL:
                        thisDevice.setDeviceAir(clientSocket, (OnOffType) command == OnOffType.ON ? 1 : 0);
                        break;
                    case DRY_CHANNEL:
                        thisDevice.setDeviceDry(clientSocket, (OnOffType) command == OnOffType.ON ? 1 : 0);
                        break;
                    case HEALTH_CHANNEL:
                        thisDevice.setDeviceHealth(clientSocket, (OnOffType) command == OnOffType.ON ? 1 : 0);
                        break;
                    case PWRSAV_CHANNEL:
                        thisDevice.setDevicePwrSaving(clientSocket, (OnOffType) command == OnOffType.ON ? 1 : 0);
                        break;
                }
            } catch (Exception e) {
                logger.debug("Unable to execute command {} for channel {}", command, channelUID.getId(), e);
            }
        }
    }

    private boolean isMinimumRefreshTimeExceeded() {
        long currentTime = System.currentTimeMillis();
        long timeSinceLastRefresh = currentTime - lastRefreshTime;
        if (timeSinceLastRefresh < MINIMUM_REFRESH_TIME) {
            return false;
        }
        lastRefreshTime = currentTime;
        return true;
    }

    private void startAutomaticRefresh() {

        Runnable refresher = new Runnable() {
            @Override
            public void run() {

                try {
                    logger.debug("Executing automatic update of values");
                    // safeguard for multiple REFRESH commands
                    if (isMinimumRefreshTimeExceeded()) {
                        logger.debug("Fetching status values from device.");
                        // Get the current status from the Airconditioner
                        thisDevice.getDeviceStatus(clientSocket);
                    } else {
                        logger.trace(
                                "Skipped fetching status values from device because minimum refresh time not reached");
                    }

                    // Update All Channels
                    List<Channel> channels = getThing().getChannels();
                    for (Channel channel : channels) {
                        publishChannelIfLinked(channel.getUID());
                    }

                } catch (Exception e) {
                    logger.debug("Unable to perform auto-update", e);
                }
            }
        };

        refreshTask = scheduler.scheduleWithFixedDelay(refresher, 0, refreshTime, TimeUnit.SECONDS);
        logger.debug("Automatic refresh started ({} second interval)", refreshTime);
    }

    private void publishChannelIfLinked(ChannelUID channelUID) {
        String channelID = channelUID.getId();
        boolean statusChanged = false;
        // if (channelID != null && isLinked(channelID)) {
        if (isLinked(channelID)) {
            State state = null;
            Integer stateValue = null;
            switch (channelID) {
                case POWER_CHANNEL:
                    if (thisDevice.hasStatusValChanged("Pow")) {
                        logger.trace("Pow value has changed!");
                        statusChanged = true;
                        stateValue = thisDevice.getIntStatusVal("Pow");
                        if (stateValue.intValue() != 1) {
                            state = OnOffType.OFF;
                            // state = new StringType("OFF");
                        } else {
                            state = OnOffType.ON;
                            // state = new StringType("ON");
                        }
                    }
                    break;
                case MODE_CHANNEL:
                    if (thisDevice.hasStatusValChanged("Mod")) {
                        logger.trace("Mod value has changed!");
                        statusChanged = true;
                        stateValue = thisDevice.getIntStatusVal("Mod");
                        state = new DecimalType(stateValue);
                    }
                    break;
                case TURBO_CHANNEL:
                    if (thisDevice.hasStatusValChanged("Tur")) {
                        logger.trace("Mod value has changed!");
                        statusChanged = true;
                        stateValue = thisDevice.getIntStatusVal("Tur");
                        if (stateValue.intValue() != 1) {
                            state = OnOffType.OFF;
                            // state = new StringType("OFF");
                        } else {
                            state = OnOffType.ON;
                            // state = new StringType("ON");
                        }
                    }
                    break;
                case LIGHT_CHANNEL:
                    if (thisDevice.hasStatusValChanged("Lig")) {
                        logger.trace("Lig value has changed!");
                        statusChanged = true;
                        stateValue = thisDevice.getIntStatusVal("Lig");
                        if (stateValue.intValue() != 1) {
                            state = OnOffType.OFF;
                            // state = new StringType("OFF");
                        } else {
                            state = OnOffType.ON;
                            // state = new StringType("ON");
                        }
                    }
                    break;
                case TEMP_CHANNEL:
                    if (thisDevice.hasStatusValChanged("SetTem")) {
                        logger.trace("SetTem value has changed!");
                        statusChanged = true;
                        stateValue = thisDevice.getIntStatusVal("SetTem");
                        state = new DecimalType(stateValue);
                    }
                    break;
                case SWINGV_CHANNEL:
                    if (thisDevice.hasStatusValChanged("SwUpDn")) {
                        logger.trace("SwUpDn value has changed!");
                        statusChanged = true;
                        stateValue = thisDevice.getIntStatusVal("SwUpDn");
                        state = new DecimalType(stateValue);
                    }
                    break;
                case WINDSPEED_CHANNEL:
                    if (thisDevice.hasStatusValChanged("WdSpd")) {
                        logger.trace("WdSpd value has changed!");
                        statusChanged = true;
                        stateValue = thisDevice.getIntStatusVal("WdSpd");
                        state = new DecimalType(stateValue);
                    }
                    break;
                case AIR_CHANNEL:
                    if (thisDevice.hasStatusValChanged("Air")) {
                        logger.trace("Air value has changed!");
                        statusChanged = true;
                        stateValue = thisDevice.getIntStatusVal("Air");
                        if (stateValue.intValue() != 1) {
                            state = OnOffType.OFF;
                            // state = new StringType("OFF");
                        } else {
                            state = OnOffType.ON;
                            // state = new StringType("ON");
                        }
                    }
                    break;
                case DRY_CHANNEL:
                    if (thisDevice.hasStatusValChanged("Blo")) {
                        logger.trace("Blo value has changed!");
                        statusChanged = true;
                        stateValue = thisDevice.getIntStatusVal("Blo");
                        if (stateValue.intValue() != 1) {
                            state = OnOffType.OFF;
                            // state = new StringType("OFF");
                        } else {
                            state = OnOffType.ON;
                            // state = new StringType("ON");
                        }
                    }
                    break;
                case HEALTH_CHANNEL:
                    if (thisDevice.hasStatusValChanged("Health")) {
                        logger.trace("Health value has changed!");
                        statusChanged = true;
                        stateValue = thisDevice.getIntStatusVal("Health");
                        if (stateValue.intValue() != 1) {
                            state = OnOffType.OFF;
                            // state = new StringType("OFF");
                        } else {
                            state = OnOffType.ON;
                            // state = new StringType("ON");
                        }
                    }
                    break;
                case PWRSAV_CHANNEL:
                    if (thisDevice.hasStatusValChanged("SvSt")) {
                        logger.trace("SvSt value has changed!");
                        statusChanged = true;
                        stateValue = thisDevice.getIntStatusVal("SvSt");
                        if (stateValue.intValue() != 1) {
                            state = OnOffType.OFF;
                            // state = new StringType("OFF");
                        } else {
                            state = OnOffType.ON;
                            // state = new StringType("ON");
                        }
                    }
                    break;
            }
            if (state != null && statusChanged == true) {
                logger.trace("Updating channel state for ChannelID {} : ", channelID);
                updateState(channelID, state);
            }
        }
    }

    @Override
    public void dispose() {
        logger.debug("Thing {} is disposing", thing.getUID());
        if (clientSocket != null) {
            clientSocket.close();
        }
        if (refreshTask != null) {
            refreshTask.cancel(true);
        }
        lastRefreshTime = 0;
    }
}
