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
package org.openhab.binding.neohub.internal;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.BridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NeoBaseHandler} is the openHAB Handler for NeoPlug devices
 * 
 * @author Andrew Fiddian-Green - Initial contribution
 * 
 */
public class NeoBaseHandler extends BaseThingHandler {

    protected final Logger logger = LoggerFactory.getLogger(NeoBaseHandler.class);

    protected NeoBaseConfiguration config;

    /*
<<<<<<< HEAD
     * error messages
     */
    private static final String MSG_HUB_CONFIG = "hub needs to be initialized!";
    private static final String MSG_HUB_COMM = "error communicating with the hub!";
    private static final String MSG_FMT_DEVICE_CONFIG = "device {} needs to configured in hub!";
    private static final String MSG_FMT_DEVICE_COMM = "device {} not communicating with hub!";
    private static final String MSG_FMT_COMMAND_OK = "command for {} succeeded.";
    private static final String MSG_FMT_COMMAND_BAD = "{} is an invalid or empty command!";
    private static final String MSG_MISSING_PARAM = "Missing parameter \"deviceNameInHub\"";

    /*
=======
>>>>>>> Changes suggested by @kaikreuzer and clean branch for pull request
     * an object used to de-bounce state changes between openHAB and the NeoHub
     */
    protected NeoHubDebouncer debouncer = new NeoHubDebouncer();

    public NeoBaseHandler(Thing thing) {
        super(thing);
    }

    // ======== BaseThingHandler methods that are overridden =============

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command == RefreshType.REFRESH) {
            @Nullable
            NeoHubHandler hub;

            if ((hub = getNeoHub()) != null) {
                hub.startFastPollingBurst();
            }
            return;
        }

        toNeoHubSendCommandSet(channelUID.getId(), command);
    }

    @Override
    public void initialize() {
        config = getConfigAs(NeoBaseConfiguration.class);

        if (config == null || config.deviceNameInHub == null || config.deviceNameInHub.isEmpty()) {
<<<<<<< HEAD
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, MSG_MISSING_PARAM);
=======
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Missing parameter \"deviceNameInHub\"");
>>>>>>> Changes suggested by @kaikreuzer and clean branch for pull request
            return;
        }
        refreshStateOnline(getNeoHub());
    }

    // ======== helper methods used by this class or descendants ===========

    /**
     * refresh the handler online state
     * 
     * @return true if the handler is online
     */
    private boolean refreshStateOnline(NeoHubHandler hub) {
        if (hub == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED);
<<<<<<< HEAD
            logger.warn(MSG_HUB_CONFIG);
=======
>>>>>>> Changes suggested by @kaikreuzer and clean branch for pull request
            return false;
        }

        if (!hub.isConfigured(config.deviceNameInHub)) {
<<<<<<< HEAD
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.GONE);
            logger.warn(MSG_FMT_DEVICE_CONFIG, getThing().getLabel());
=======
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.GONE, "Device not configured in hub");
>>>>>>> Changes suggested by @kaikreuzer and clean branch for pull request
            return false;
        }

        if (!hub.isOnline(config.deviceNameInHub)) {
<<<<<<< HEAD
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            logger.warn(MSG_FMT_DEVICE_COMM, getThing().getLabel());
=======
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Device configured, but not communicating");
>>>>>>> Changes suggested by @kaikreuzer and clean branch for pull request
            return false;
        }

        updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
        return true;
    }

    /*
     * this method is called back by the NeoHub handler to inform this handler about
     * polling results from the hub handler
     */
    public void toBaseSendPollResponse(NeoHubHandler hub, NeoHubInfoResponse infoResponse) {
        NeoHubInfoResponse.DeviceInfo myInfo;

        if (refreshStateOnline(hub) && (myInfo = infoResponse.getDeviceInfo(config.deviceNameInHub)) != null) {
            toOpenHabSendChannelValues(myInfo);
        }
    }

    /*
     * internal method used by by sendChannelValuesToOpenHab(). It checks the
     * de-bouncer before actually sending the channel value to openHAB
     */
    protected void toOpenHabSendValueDebounced(String channelId, State state) {
        if (debouncer.timeExpired(channelId)) {
            updateState(channelId, state);
        }
    }

    /*
     * sends a channel command & value from openHAB => NeoHub. It delegates upwards
     * to the NeoHub to handle the command
     */
    protected void toNeoHubSendCommand(String channelId, Command command) {
        String cmdStr = toNeoHubBuildCommandString(channelId, command);

        if (!cmdStr.isEmpty()) {
            NeoHubHandler hub = getNeoHub();

            if (hub != null) {
                /*
                 * issue command, check result, and update status accordingly
                 */
                switch (hub.toNeoHubSendChannelValue(cmdStr)) {
                case SUCCEEDED:
<<<<<<< HEAD
                    logger.debug(MSG_FMT_COMMAND_OK, getThing().getLabel());

                    if (getThing().getStatus() != ThingStatus.ONLINE) {
=======
                    logger.debug("command succeeded.");

                    if (getThing().getStatus() != ThingStatus.ONLINE) {
                        logger.debug("command for {} succeeded, status => online.", getThing().getLabel());
>>>>>>> Changes suggested by @kaikreuzer and clean branch for pull request
                        updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
                    }

                    // initialize the de-bouncer for this channel
                    debouncer.initialize(channelId);

                    break;

                case ERR_COMMUNICATION:
<<<<<<< HEAD
                    logger.warn(MSG_HUB_COMM);
=======
                    logger.warn("hub communication error for {}, status => offline!", getThing().getLabel());
>>>>>>> Changes suggested by @kaikreuzer and clean branch for pull request
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                    break;

                case ERR_INITIALIZATION:
<<<<<<< HEAD
                    logger.debug(MSG_HUB_CONFIG);
=======
                    logger.debug("hub initialization error for {}, status => offline!", getThing().getLabel());
>>>>>>> Changes suggested by @kaikreuzer and clean branch for pull request
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
                    break;
                }
            } else {
<<<<<<< HEAD
                logger.debug(MSG_HUB_CONFIG);
            }
        } else {
            logger.debug(MSG_FMT_COMMAND_BAD, command.toString());
=======
                logger.debug("hub for {} not found!", getThing().getLabel());
            }
        } else {
            logger.debug("invalid or empty command for {}!", getThing().getLabel());
>>>>>>> Changes suggested by @kaikreuzer and clean branch for pull request
        }
    }

    /**
     * internal getter returns the NeoHub handler
     * 
     * @return the neohub handler or null
     */
    private @Nullable NeoHubHandler getNeoHub() {
        @Nullable
        Bridge b;

        @Nullable
        BridgeHandler h;

        if ((b = getBridge()) != null && (h = b.getHandler()) != null && h instanceof NeoHubHandler) {
            return (NeoHubHandler) h;
        }

        return null;
    }

    // ========= methods that MAY / MUST be overridden in descendants ============

    /*
     * NOTE: descendant classes MUST override this method. It builds the command
     * string to be sent to the NeoHub
     */
    protected String toNeoHubBuildCommandString(String channelId, Command command) {
        return "";
    }

    /*
     * NOTE: descendant classes MAY override this method e.g. to send additional
     * commands for dependent channels (if any)
     */
    protected void toNeoHubSendCommandSet(String channelId, Command command) {
        toNeoHubSendCommand(channelId, command);
    }

    /*
     * NOTE: descendant classes MUST override this method method by which the
     * handler informs openHAB about channel state changes
     */
    protected void toOpenHabSendChannelValues(NeoHubInfoResponse.DeviceInfo deviceInfo) {
    }

    protected OnOffType invert(OnOffType value) {
        return OnOffType.from(value == OnOffType.OFF);
    }
}
