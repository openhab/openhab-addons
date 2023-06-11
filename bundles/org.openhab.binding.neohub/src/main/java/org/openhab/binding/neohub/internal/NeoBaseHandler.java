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
package org.openhab.binding.neohub.internal;

import static org.openhab.binding.neohub.internal.NeoHubBindingConstants.*;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.neohub.internal.NeoHubAbstractDeviceData.AbstractRecord;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.BridgeHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NeoBaseHandler} is the openHAB Handler for NeoPlug devices
 *
 * @author Andrew Fiddian-Green - Initial contribution
 *
 */
@NonNullByDefault
public class NeoBaseHandler extends BaseThingHandler {

    protected final Logger logger = LoggerFactory.getLogger(NeoBaseHandler.class);

    protected @Nullable NeoBaseConfiguration config;

    /*
     * error messages
     */
    private static final String MSG_FMT_DEVICE_CONFIG = "device \"{}\" needs to configured in hub!";
    private static final String MSG_FMT_DEVICE_COMM = "device \"{}\" not communicating with hub!";
    private static final String MSG_FMT_COMMAND_OK = "command for \"{}\" succeeded.";
    private static final String MSG_FMT_COMMAND_BAD = "\"{}\" is an invalid or empty command!";
    private static final String MSG_DEVICE_NAME_NOT_CONFIGURED = "the parameter \"deviceNameInHub\" is not configured";

    /*
     * an object used to de-bounce state changes between openHAB and the NeoHub
     */
    protected NeoHubDebouncer debouncer = new NeoHubDebouncer();

    public NeoBaseHandler(Thing thing) {
        super(thing);
    }

    // ======== BaseThingHandler methods that are overridden =============

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        @Nullable
        NeoHubHandler hub;

        if ((hub = getNeoHub()) != null) {
            if (command == RefreshType.REFRESH) {
                hub.startFastPollingBurst();
                return;
            }
        }

        toNeoHubSendCommandSet(channelUID.getId(), command);
    }

    @Override
    public void initialize() {
        NeoBaseConfiguration config = getConfigAs(NeoBaseConfiguration.class);

        if (config.deviceNameInHub.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, MSG_DEVICE_NAME_NOT_CONFIGURED);
            return;
        }

        this.config = config;

        NeoHubHandler hub = getNeoHub();
        if (hub == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, MSG_HUB_CONFIG);
            return;
        }

        updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.CONFIGURATION_PENDING);
    }

    // ======== helper methods used by this class or descendants ===========

    /*
     * this method is called back by the NeoHub handler to inform this handler about
     * polling results from the hub handler
     */
    public void toBaseSendPollResponse(NeoHubAbstractDeviceData deviceData) {
        NeoBaseConfiguration config = this.config;
        if (config == null) {
            return;
        }

        AbstractRecord deviceRecord = deviceData.getDeviceRecord(config.deviceNameInHub);

        if (deviceRecord == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
            logger.warn(MSG_FMT_DEVICE_CONFIG, thing.getLabel());
            return;
        }

        ThingStatus thingStatus = getThing().getStatus();
        if (deviceRecord.offline() && (thingStatus == ThingStatus.ONLINE)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            logger.debug(MSG_FMT_DEVICE_COMM, thing.getLabel());
            return;
        }

        if ((!deviceRecord.offline()) && (thingStatus != ThingStatus.ONLINE)) {
            updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
        }

        toOpenHabSendChannelValues(deviceRecord);
    }

    /*
     * internal method used by by sendChannelValuesToOpenHab(); it checks the
     * de-bouncer before actually sending the channel value to openHAB; or if the
     * device has lost its connection to the RF mesh, either a) send no updates to
     * OpenHAB or b) send state = undefined, depending on the value of a
     * Configuration Parameter
     */
    protected void toOpenHabSendValueDebounced(String channelId, State state, boolean offline) {
        /*
         * if the device has been lost from the RF mesh network there are two possible
         * behaviors: either a) do not report a state value, or b) show an undefined
         * state; the choice of a) or b) depends on whether the channel has the
         * Configuration Parameter holdOnlineState=true
         */
        if (!offline) {
            if (debouncer.timeExpired(channelId)) {
                /*
                 * in normal circumstances just forward the hub's reported state to OpenHAB
                 */
                updateState(channelId, state);
            }
        } else {
            ChannelUID channelUID = new ChannelUID(thing.getUID(), channelId);
            Channel channel = thing.getChannel(channelUID);
            if (channel != null) {
                Configuration config = channel.getConfiguration();
                Object holdOnlineState = config.get(PARAM_HOLD_ONLINE_STATE);
                if (holdOnlineState != null && (holdOnlineState instanceof Boolean)
                        && ((Boolean) holdOnlineState).booleanValue()) {
                    /*
                     * the Configuration Parameter "holdOnlineState" is True so do NOT send a
                     * state update to OpenHAB
                     */
                    return;
                }
            }
            /*
             * the Configuration Parameter "holdOnlineState" is either not existing or
             * it is False so send a state=undefined update to OpenHAB
             */
            updateState(channelUID, UnDefType.UNDEF);
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
                        logger.debug(MSG_FMT_COMMAND_OK, getThing().getLabel());

                        if (getThing().getStatus() != ThingStatus.ONLINE) {
                            updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
                        }

                        // initialize the de-bouncer for this channel
                        debouncer.initialize(channelId);

                        break;

                    case ERR_COMMUNICATION:
                        logger.debug(MSG_HUB_COMM, hub.getThing().getUID());
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                        break;

                    case ERR_INITIALIZATION:
                        logger.warn(MSG_HUB_CONFIG, hub.getThing().getUID());
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
                        break;
                }
            } else {
                logger.debug(MSG_HUB_CONFIG, "unknown");
            }
        } else {
            logger.debug(MSG_FMT_COMMAND_BAD, command.toString());
        }
    }

    /**
     * internal getter returns the NeoHub handler
     *
     * @return the neohub handler or null
     */
    protected @Nullable NeoHubHandler getNeoHub() {
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
    protected void toOpenHabSendChannelValues(AbstractRecord deviceRecord) {
    }

    protected OnOffType invert(OnOffType value) {
        return OnOffType.from(value == OnOffType.OFF);
    }

    protected Unit<?> getTemperatureUnit() {
        @Nullable
        NeoHubHandler hub = getNeoHub();
        if (hub != null) {
            return hub.getTemperatureUnit();
        }
        return SIUnits.CELSIUS;
    }
}
