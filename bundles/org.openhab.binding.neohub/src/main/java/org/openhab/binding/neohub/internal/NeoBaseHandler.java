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
 * The {@link NeoBaseHandler} is the OpenHab Handler for NeoPlug devices
 * 
 * @author Andrew Fiddian-Green - Initial contribution
 * 
 */
public class NeoBaseHandler extends BaseThingHandler {

    protected final Logger logger = LoggerFactory.getLogger(NeoBaseHandler.class);

    protected NeoBaseConfiguration config;

    /*
     * an object used to de-bounce state changes OpenHab <=> NeoHub
     */
    protected NeoHubDebouncer debouncer = new NeoHubDebouncer();

    public NeoBaseHandler(Thing thing) {
        super(thing);
    }

    // ======== BaseThingHandler methods that are overridden =============

    /*
     * overridden method of BaseThingHandler: by which OpenHab issues commands to
     * the handler
     */
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

    /*
     * overridden method of BaseThingHandler: by which OpenHab initializes the
     * handler
     */
    @Override
    public void initialize() {
        config = getConfigAs(NeoBaseConfiguration.class);

        if (config == null || config.deviceNameInHub == null || config.deviceNameInHub.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    String.format("configuration error for %s, status => offline!", getThing().getLabel()));
            return;
        }

        updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE,
                String.format("%s initialized, status => online..", getThing().getLabel()));
    }

    // ======== helper methods used by this class or descendants ===========

    /*
     * this method is called back by the NeoHub handler to inform this handler about
     * polling results from the hub handler
     */
    public void toBaseSendPollResponse(NeoHubInfoResponse pollResponse) {
        NeoHubInfoResponse.DeviceInfo myPollResponse = pollResponse.getDeviceInfo(config.deviceNameInHub);

        if (myPollResponse == null) {
            if (getThing().getStatus() == ThingStatus.ONLINE) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        String.format("hub has no info for %s, status => offline!", getThing().getLabel()));
            }
            return;
        }

        if (myPollResponse.isOffline()) {
            if (getThing().getStatus() == ThingStatus.ONLINE) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        String.format("hub reports %s offline, status => offline!", getThing().getLabel()));
            }
        } else {
            if (getThing().getStatus() != ThingStatus.ONLINE) {
                updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE,
                        String.format("received info for %s from hub, status => online..", getThing().getLabel()));
            }
        }

        toOpenHabSendChannelValues(myPollResponse);
    }

    /*
     * internal method used by by sendChannelValuesToOpenHab() checks the de-bouncer
     * before actually sending the channel value to OpenHAB
     */
    protected void toOpenHabSendValueDebounced(String channelId, State state) {
        if (debouncer.timeExpired(channelId)) {
            updateState(channelId, state);
        }
    }

    /*
     * sends a channel command & value from OpenHab => NeoHub delegates upwards to
     * the NeoHub to handle the command
     */
    protected void toNeoHubSendCommand(String channelId, Command command) {
        String msg;

        String cmdStr = toNeoHubBuildCommandString(channelId, command);

        if (!cmdStr.isEmpty()) {
            // issue command, check result, and update status accordingly
            switch (getNeoHub().toNeoHubSendChannelValue(cmdStr)) {
            case SUCCEEDED:
                if (logger.isDebugEnabled()) {
                    logger.debug("command succeeded..");
                }

                if (getThing().getStatus() != ThingStatus.ONLINE) {
                    msg = String.format("command for %s succeeded, status => online..", getThing().getLabel());
                    logger.info(msg);

                    updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, msg);
                }

                // initialize the de-bouncer for this channel
                debouncer.initialize(channelId);

                break;

            case ERR_COMMUNICATION:
                msg = String.format("hub communication error for %s, status => offline!", getThing().getLabel());
                logger.error(msg);

                if (getThing().getStatus() == ThingStatus.ONLINE) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, msg);
                }
                break;

            case ERR_INITIALIZATION:
                msg = String.format("hub initialization error for %s, status => offline!", getThing().getLabel());
                logger.error(msg);

                if (getThing().getStatus() == ThingStatus.ONLINE) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED, msg);
                }
                break;
            }
        } else {
            msg = String.format("unknown command error for %s, => command ignored!", getThing().getLabel());
            logger.error(msg);
        }
    }

    /*
     * internal getter returns the NeoHub handler
     */
    private NeoHubHandler getNeoHub() {
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
     * NOTE: descendant classes MUST override this method builds the command string
     * to be sent to the NeoHub
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
     * handler informs OpenHab about channel state changes
     */
    protected void toOpenHabSendChannelValues(NeoHubInfoResponse.DeviceInfo device) {
    }

    protected OnOffType invert(OnOffType value) {
        return OnOffType.from(value == OnOffType.OFF);
    }
}
