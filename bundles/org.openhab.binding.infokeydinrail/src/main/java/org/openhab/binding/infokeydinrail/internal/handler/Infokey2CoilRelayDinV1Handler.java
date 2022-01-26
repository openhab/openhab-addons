/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.infokeydinrail.internal.handler;

import static org.openhab.binding.infokeydinrail.internal.InfokeyBindingConstants.*;

import java.util.Objects;

import org.openhab.binding.infokeydinrail.internal.NetClient;
import org.openhab.binding.infokeydinrail.internal.PinMapperBoard;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link Infokey2CoilRelayDinV1Handler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Themistoklis Anastasopoulos - Initial contribution
 */
// @NonNullByDefault
public class Infokey2CoilRelayDinV1Handler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private String serverIP = "127.0.0.1";
    private Integer address;
    private Integer busNumber;
    private InfokeyPinStateHolder pinStateHolder;
    /**
     * the polling interval mcp check interrupt register (optional, defaults to 50ms)
     */
    private static final int POLLING_INTERVAL = 750;

    public Infokey2CoilRelayDinV1Handler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Received command: {} on channelGroup {} on channel {}", command.toFullString(),
                channelUID.getGroupId(), channelUID.getIdWithoutGroup());

        if (!verifyChannel(channelUID)) {
            return;
        }

        String channelGroup = channelUID.getGroupId();

        switch (channelGroup) {
            case CHANNEL_GROUP_PULSE:
                handleOutputCommand(channelUID, command);
            default:
                break;
        }
    }

    @Override
    public void initialize() {
        try {
            checkConfiguration();
            pinStateHolder = new InfokeyPinStateHolder(this.thing);
            updateStatus(ThingStatus.ONLINE);
        } catch (IllegalArgumentException | SecurityException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "An exception occurred while adding pin. Check pin configuration. Exception: " + e.getMessage());
        }
    }

    private boolean verifyChannel(ChannelUID channelUID) {
        if (!isChannelGroupValid(channelUID) || !isChannelValid(channelUID)) {
            logger.warn("Channel group or channel is invalid. Probably configuration problem");
            return false;
        }
        return true;
    }

    private void handleOutputCommand(ChannelUID channelUID, Command command) {
        try {
            if ((command instanceof OnOffType) && (channelUID.getAsString().toUpperCase().contains("PULSE"))) {
                if (((OnOffType) command).equals(OnOffType.ON)) {
                    try {
                        Integer pinNo = PinMapperBoard.get(channelUID.getIdWithoutGroup());

                        Configuration configuration = this.getThing().getChannel(channelUID.getId()).getConfiguration();

                        // get pulse duration from configuration
                        String pulseDuration = Objects.toString(configuration.get(PULSE_DURATION), null);

                        Double thePulseDuration = 0.0;

                        try {
                            thePulseDuration = pulseDuration != null ? Double.parseDouble(pulseDuration) / 1000 : 1;
                        } catch (Exception ex) {
                            thePulseDuration = 1.0;
                        }

                        logger.debug("Execute pulse command for duration {} pin {}!", pulseDuration, pinNo);

                        String callString = "http://localhost:8000/mcp23017_write_no_response/" + busNumber + "/0x"
                                + Integer.toHexString(address) + "/" + pinNo + "/2/" + thePulseDuration;

                        logger.debug("got output pin {} for channel {} and command {} ", pinNo, channelUID, command);
                        logger.debug("callString : {}", callString);

                        // get pin default state in order to handle according
                        // String defaultState = Objects.toString(configuration.get(DEFAULT_STATE), null);

                        NetClient aNetClient = new NetClient();
                        aNetClient.get(callString);

                    } catch (Exception ex) {
                        logger.debug("Ops!", ex);
                    }
                }
            }

        } catch (Exception ex) {
            logger.debug("----------------------> Ops!", ex);
        }
    }

    private boolean isChannelGroupValid(ChannelUID channelUID) {
        if (!channelUID.isInGroup()) {
            logger.debug("Defined channel not in group: {}", channelUID.getAsString());
            return false;
        }
        boolean channelGroupValid = SUPPORTED_CHANNEL_GROUPS.contains(channelUID.getGroupId());
        logger.debug("Defined channel in group: {}. Valid: {}", channelUID.getGroupId(), channelGroupValid);

        return channelGroupValid;
    }

    private boolean isChannelValid(ChannelUID channelUID) {
        boolean channelValid = SUPPORTED_CHANNELS.contains(channelUID.getIdWithoutGroup());
        logger.debug("Is channel {} in supported channels: {}", channelUID.getIdWithoutGroup(), channelValid);
        return channelValid;
    }

    protected void checkConfiguration() {
        Configuration configuration = getConfig();
        address = Integer.parseInt((configuration.get(ADDRESS)).toString(), 16);
        busNumber = Integer.parseInt((configuration.get(BUS_NUMBER)).toString());
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        synchronized (this) {
            logger.debug("channel linked {}", channelUID.getAsString());
            if (!verifyChannel(channelUID)) {
                return;
            }

            super.channelLinked(channelUID);
        }
    }
}
